# -*- coding: utf-8 -*-
"""Data models."""
import datetime as dt

from sqlalchemy import ForeignKey, func, event

from healthaggregation.database import PkModel, Model, Column, db, reference_col, relationship


class ParticipatingDevice(Model):
    __tablename__ = "participating_devices"

    request_id = Column(ForeignKey("data_requests.id"), primary_key=True)
    id = Column(db.Integer, primary_key=True)
    public_key = Column(db.String, nullable=False, primary_key=False)
    diffie_hellmann_public_component = Column(db.String, nullable=False)

    def __init__(self, **kwargs):
        """Create instance."""
        super().__init__(**kwargs)

    def __str__(self):
        """Represent instance as a string."""
        return f"<ParticipatingDevice({self.request_id!r}:{self.public_key!r})>"

    def __repr__(self):
        """Represent instance as a unique string."""
        return f"<ParticipatingDevice({self.request_id!r}:{self.public_key!r})>"

    def json(self):
        return {"request_id": self.request_id,
                "public_key": self.public_key,
                "diffie_hellmann_public_component": self.diffie_hellmann_public_component,
                "id": self.id
                }


@event.listens_for(ParticipatingDevice, 'before_insert')
def calculate_device_id(mapper, connect, target):
    existing_participating_device = ParticipatingDevice \
        .query \
        .filter_by(request_id=target.request_id) \
        .group_by(ParticipatingDevice.request_id) \
        .having(func.max(ParticipatingDevice.id)) \
        .first()
    target.id = existing_participating_device.id + 1 if existing_participating_device else 1


class DataRequests(PkModel):
    __tablename__ = "data_requests"

    requesting_user = reference_col("users", nullable=False)
    created_at = Column(db.DateTime, nullable=False, default=dt.datetime.utcnow)
    requested_url = Column(db.String(256), nullable=False)
    expected_dimensionality = Column(db.Integer, nullable=False)

    responses = relationship("DataResponse", cascade="all,delete", backref="request_obj")
    participating_devices = relationship("ParticipatingDevice", cascade="all,delete", backref="request_obj")
    participating_device_shares = relationship("ParticipatingDeviceSecretShares",
                                               cascade="all,delete",
                                               backref="request_obj")
    dropped_client_shares = relationship("DroppedClientSecretShares",
                                         cascade="all,delete",
                                         backref="request_obj")

    def __init__(self, **kwargs):
        """Create instance."""
        super().__init__(**kwargs)


class DataResponse(PkModel):
    __tablename__ = "data_responses"

    request = reference_col("data_requests", nullable=False)
    data = Column(db.PickleType, nullable=False)

    def __init__(self, **kwargs):
        """Create instance."""
        super().__init__(**kwargs)


class ParticipatingDeviceSecretShares(Model):
    __tablename__ = "participating_device_secret_shares"

    request_id = Column(ForeignKey("data_requests.id"), primary_key=True)
    encrypted_for_device = Column(db.Integer, nullable=False, primary_key=True)
    secret_of_dropped_device = Column(db.Integer, nullable=False, primary_key=True)
    secret_with_device = Column(db.Integer, nullable=False, primary_key=True)
    share_id = Column(db.Integer, nullable=False, primary_key=False)
    encrypted_share = Column(db.PickleType, nullable=False)

    def __init__(self, **kwargs):
        """Create instance."""
        super().__init__(**kwargs)

    def json(self):
        return {"request_id": self.request_id,
                "encrypted_for_device": self.encrypted_for_device,
                "secret_of_dropped_device": self.secret_of_dropped_device,
                "secret_with_device": self.secret_with_device,
                "share_id": self.share_id,
                "encrypted_share": self.encrypted_share
                }


class DroppedClientSecretShares(Model):
    __tablename__ = "dropped_client_secret_shares"

    request_id = Column(ForeignKey("data_requests.id"), primary_key=True)
    secret_of_dropped_device = Column(db.Integer, nullable=False, primary_key=True)
    secret_with_device = Column(db.Integer, nullable=False, primary_key=True)
    share_id = Column(db.Integer, nullable=False, primary_key=True)
    decrypted_share = Column(db.PickleType, nullable=False)

    def __init__(self, **kwargs):
        """Create instance."""
        super().__init__(**kwargs)
