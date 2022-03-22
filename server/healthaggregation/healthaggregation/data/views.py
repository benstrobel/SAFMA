# -*- coding: utf-8 -*-
"""Data views."""
import datetime
import json

from flask import Blueprint, request, Response, url_for, redirect, render_template
from flask_login import login_required, current_user

from healthaggregation.cloudmessaging.firebase_cloud_messaging import send_new_data_request
from healthaggregation.data.forms import CreateRequestForm
from healthaggregation.data.lib import get_responses
from healthaggregation.data.models import DataRequests, ParticipatingDevice, DroppedClientSecretShares
from healthaggregation.utils import flash_errors

blueprint = Blueprint("data", __name__, url_prefix="/data", static_folder="../static")


@blueprint.route("/request/responses", methods=["GET"])
@login_required
def responses():
    if request.args is None or "id" not in request.args:
        return Response({"400 Bad Request"}, status=400)
    data_request = DataRequests.query.filter_by(id=request.args.get("id")).first()
    if data_request is None:
        flash_errors({"id": "This request does not exist"})
        return redirect(url_for("public.home"))
    avg, \
    sum, \
    first_round_participants, \
    second_round_participants, \
    third_round_participants, \
    print_reconstruction_warning = get_responses(data_request)
    return render_template("data/responses.html",
                           request=data_request,
                           avg=avg,
                           sum=sum,
                           first_round_participants=first_round_participants,
                           second_round_participants=second_round_participants,
                           third_round_participants=third_round_participants,
                           print_reconstruction_warning=print_reconstruction_warning)


@blueprint.route("/request/create", methods=["GET", "POST"])
@login_required
def create_request_data():
    """Create a new data request."""
    form = CreateRequestForm(obj=request.form, data={"requesting_user": current_user.id,
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
        return redirect(url_for("public.home"))
    else:
        flash_errors(form)
        return render_template("data/create_request.html", form=form)


@blueprint.route("/request/delete", methods=["GET"])
@login_required
def delete_data_request():
    """Delete a data request."""
    if request.args is None or "id" not in request.args:
        return Response({"400 Bad Request"}, status=400)
    if not current_user.is_admin:
        return Response("403 Forbidden", status=403)
    data_request = DataRequests.query.filter_by(id=request.args.get("id")).first()
    if data_request is None:
        return Response("404 Data Request Not Found", status=404)
    data_request.delete()
    return redirect(url_for("public.home"))
