from typing import Union

from healthaggregation.data.models import ParticipatingDevice
from healthaggregation.extensions import jwt
from healthaggregation.user.models import User


def is_user(user_or_participating_device: Union[User, ParticipatingDevice]):
    return not hasattr(user_or_participating_device, "request_id")

# enable automatic user loading via `flask_jwt_extended` according to
# https://flask-jwt-extended.readthedocs.io/en/stable/automatic_user_loading/


# Register a callback function that takes whatever object is passed in as the
# identity when creating JWTs and converts it to a JSON serializable format.
@jwt.user_identity_loader
def user_identity_lookup(identity):
    if is_user(identity):
        return {"id": identity.id}
    else:
        return {"request_id": identity.request_id, "id": identity.id}


# Register a callback function that loades a user from your database whenever
# a protected route is accessed. This should return any python object on a
# successful lookup, or None if the lookup failed for any reason (for example
# if the user has been deleted from the database).
@jwt.user_lookup_loader
def user_lookup_callback(_jwt_header, jwt_data):
    identity = jwt_data["sub"]
    if "request_id" in identity:
        return ParticipatingDevice.query.filter_by(request_id=identity["request_id"], id=identity["id"]).one_or_none()
    else:
        return User.query.filter_by(id=identity["id"]).one_or_none()
