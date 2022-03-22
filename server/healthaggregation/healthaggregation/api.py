import datetime
import json

from flask import Blueprint, Response, request
from flask_jwt_extended import jwt_required, current_user, create_access_token

from healthaggregation.auth import is_user
from healthaggregation.cloudmessaging.firebase_cloud_messaging import send_new_data_request
from healthaggregation.data.forms import CreateRequestForm
from healthaggregation.data.lib import get_responses, wait_x_since
from healthaggregation.data.mediators import ParticipateResponseMediator
from healthaggregation.data.models import DataRequests, ParticipatingDeviceSecretShares, DataResponse, \
    ParticipatingDevice, DroppedClientSecretShares
from healthaggregation.user.forms import CreateForm
from healthaggregation.user.models import User

blueprint = Blueprint("api", __name__, url_prefix="/api", static_folder="../static")

token_expiry = datetime.timedelta(minutes=30)
first_round_max_time = datetime.timedelta(seconds=5)
second_round_max_time = datetime.timedelta(seconds=5)
third_round_max_time = datetime.timedelta(seconds=10)

# User Endpoints


@blueprint.route("/request", methods=["POST"])
@jwt_required()
def create_request_data():
    if not is_user(current_user):
        return Response("403 Forbidden", status=403)
    form = CreateRequestForm(obj=request.json, meta={"csrf": False}, data={"requesting_user": current_user.id,
                                                                           "created_at": datetime.datetime.utcnow()})
    if request.form is not None and ("requesting_user" in request.form or "created_at" in request.form):
        return Response(json.dumps({"error": "You can't overwrite the requesting user or the timestamp"}), status=400)
    if form.validate_on_submit():
        req = DataRequests.create(
            requesting_user=form.requesting_user.data,
            created_at=datetime.datetime.now(),
            requested_url=form.requested_url.data,
            expected_dimensionality=form.expected_dimensionality.data
        )
        send_new_data_request(req)
        return {"id": req.id}
    return Response(json.dumps(form.errors), status=422, mimetype="application/json")


@blueprint.route("/request", methods=["DELETE"])
@jwt_required()
def delete_data_request():
    if not is_user(current_user):
        return Response("403 Forbidden", status=403)
    if not current_user.is_admin:
        return Response("403 Forbidden", status=403)
    if request.json is None or "id" not in request.json:
        return Response({"400 Bad Request"}, status=400)
    data_request = DataRequests.query.filter_by(id=request.json["id"]).first()
    if data_request is None:
        return Response("404 Data Request Not Found", status=404)
    data_request.delete()
    return {"id": data_request.id}


@blueprint.route("/request", methods=["GET"])
@jwt_required()
def responses():
    if not is_user(current_user):
        return Response("403 Forbidden", status=403)
    if request.json is None or "id" not in request.json:
        return Response({"400 Bad Request"}, status=400)
    data_request = DataRequests.query.filter_by(id=request.json["id"]).first()
    avg, \
    sum, \
    first_round_participants, \
    second_round_participants, \
    third_round_participants, \
    print_reconstruction_warning = get_responses(data_request)
    return {
        "avg": avg,
        "sum": sum,
        "first_round_participants": first_round_participants,
        "second_round_participants": second_round_participants,
        "third_round_participants": third_round_participants,
        "aggregation_incomplete_or_aborted": print_reconstruction_warning
    }


@blueprint.route("/user/auth", methods=["POST"])
def auth():
    if request.json is None:
        return Response({"400 Bad Request"}, status=400)
    username = request.json.get("username", None)
    password = request.json.get("password", None)
    if username is None or password is None:
        return Response({"400 Bad Request"}, status=400)
    user = User.query.filter_by(username=username).first()
    if not user:
        return Response("401 Unauthorized", status=401)
    if not user.check_password(password):
        return Response("401 Unauthorized", status=401)
    access_token = create_access_token(identity=user, expires_delta=token_expiry)
    return {"access_token": access_token}


@blueprint.route("/user", methods=["GET"])
@jwt_required()
def list_users():
    if not is_user(current_user):
        return Response("403 Forbidden", status=403)
    users = User.query
    return [x.json for x in users]


@blueprint.route("/user", methods=["POST"])
@jwt_required()
def create_user():
    if not is_user(current_user):
        return Response("403 Forbidden", status=403)
    if not current_user.is_admin:
        return Response("403 Forbidden", status=403)
    form = CreateForm(obj=request.json, meta={"csrf": False})
    if form.validate_on_submit():
        user = User.create(
            username=form.username.data,
            email=form.email.data,
            first_name=form.first_name.data,
            last_name=form.last_name.data,
            password=form.password.data,
            active=True,
        )
        return {"id": user.id}
    return Response(json.dumps(form.errors), status=422, mimetype="application/json")
    pass


@blueprint.route("/user", methods=["PATCH"])
@jwt_required()
def promote_user_to_admin():
    if not is_user(current_user):
        return Response("403 Forbidden", status=403)
    if not current_user.is_admin:
        return Response("403 Forbidden", status=403)
    if request.json is None or "id" not in request.json:
        return Response({"400 Bad Request"}, status=400)
    user = User.query.filter_by(id=request.json["id"]).first()
    if user is None:
        return Response("404 User Not Found", status=404)
    user.is_admin = True
    user.update()
    return {"id": user.id}


@blueprint.route("/user", methods=["DELETE"])
@jwt_required()
def delete_user():
    if not is_user(current_user):
        return Response("403 Forbidden", status=403)
    if not current_user.is_admin:
        return Response("403 Forbidden", status=403)
    if request.json is None or "id" not in request.json:
        return Response({"400 Bad Request"}, status=400)
    user = User.query.filter_by(id=request.json["id"]).first()
    if user is None:
        return Response("404 User Not Found", status=404)
    if user.id == current_user.id:
        return Response("401 Unauthorized, you can' delete yourself", status=401)
    user.delete()
    return {"id": user.id}


# Participating Device Endpoints


@blueprint.route("/request/advertise_keys", methods=["POST"])
def advertise_keys():
    if request.json is None \
            or "id" not in request.json \
            or "public_key" not in request.json \
            or "diffie_hellmann_public_component" not in request.json:
        return Response({"400 Bad Request"}, status=400)
    data_request = DataRequests.query.filter_by(id=request.json["id"]).first()
    if data_request is None:
        return Response("This request does not exist", status=404)
    public_key = request.json["public_key"]
    diffie_hellmann_public_component = request.json["diffie_hellmann_public_component"]
    existing_device = ParticipatingDevice.query.filter_by(request_id=data_request.id, public_key=public_key).first()
    if existing_device is not None:
        return Response()
    participating_device = ParticipatingDevice.create(
        request_id=data_request.id,
        public_key=public_key,
        diffie_hellmann_public_component=diffie_hellmann_public_component
    )
    access_token = create_access_token(identity=participating_device, expires_delta=token_expiry)
    if not wait_x_since(data_request.created_at, first_round_max_time):
        return Response(status=410)
    participating_devices = list(map(lambda x: x.json(),
                                     ParticipatingDevice.query.filter_by(request_id=data_request.id).all()
                                     ))
    return json.dumps(ParticipateResponseMediator(data_request.id, access_token, participating_devices).__dict__)


@blueprint.route("/request/data_collection", methods=["POST"])
@jwt_required()
def data_collection():
    if is_user(current_user):
        return Response("403 Forbidden", status=403)
    if request.json is None or "id" not in request.json or "data" not in request.json or "secret_shares" not in request.json:
        return Response({"400 Bad Request"}, status=400)
    data_request = DataRequests.query.filter_by(id=request.json["id"]).first()
    if data_request is None:
        return Response("This request does not exist", status=404)
    if not isinstance(request.json["data"], list) \
            or len(request.json["data"]) <= 0 \
            or len(request.json["data"]) != data_request.expected_dimensionality \
            or not isinstance(request.json["data"][0], int):
        return Response({"400 Bad Request"}, status=400)
    for secret_share in request.json["secret_shares"]:
        if "encrypted_for_device" not in secret_share \
                or "other_device" not in secret_share \
                or "encrypted_share" not in secret_share \
                or "share_id" not in secret_share:
            return Response({"400 Bad Request"}, status=400)
        ParticipatingDeviceSecretShares.create(
            request_id=data_request.id,
            encrypted_for_device=secret_share["encrypted_for_device"],
            secret_of_dropped_device=secret_share["other_device"],
            secret_with_device=current_user.id,
            share_id=secret_share["share_id"],
            encrypted_share=secret_share["encrypted_share"]
        )
    DataResponse.create(
        request=data_request.id,
        data=request.json["data"]
    )
    wait_x_since(data_request.created_at, first_round_max_time + second_round_max_time)
    secret_shares_for_this_client = ParticipatingDeviceSecretShares.query \
        .filter_by(request_id=data_request.id, encrypted_for_device=current_user.id) \
        .all()
    return {"request_id": data_request.id,
            "secret_shares": list(map(lambda x: x.json(), secret_shares_for_this_client))}


@blueprint.route("/request/recovery", methods=["POST"])
@jwt_required()
def recovery():
    if is_user(current_user):
        return Response("403 Forbidden", status=403)
    if request.json is None \
            or "id" not in request.json \
            or "secret_shares" not in request.json:
        return Response({"400 Bad Request"}, status=400)
    data_request = DataRequests.query.filter_by(id=request.json["id"]).first()
    if data_request is None:
        return Response("This request does not exist", status=404)
    for share in request.json["secret_shares"]:
        if "share_id" not in share \
                or "secret_of_dropped_device" not in share \
                or "secret_with_device" not in share \
                or "decrypted_share" not in share:
            return Response({"400 Bad Request"}, status=400)
        DroppedClientSecretShares.create(
            request_id=data_request.id,
            secret_of_dropped_device=share["secret_of_dropped_device"],
            secret_with_device=share["secret_with_device"],
            share_id=share["share_id"],
            decrypted_share=share["decrypted_share"]
        )
    return {}
