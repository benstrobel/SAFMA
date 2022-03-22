from flask_wtf import FlaskForm

from wtforms import IntegerField, DateField, StringField
from wtforms.validators import DataRequired, Length, NumberRange

from healthaggregation.user.models import User

import re


class CreateRequestForm(FlaskForm):
    """Create request form."""

    requesting_user = IntegerField("Requesting User", validators=[DataRequired()])
    created_at = DateField("Date", validators=[DataRequired()])
    requested_url = StringField("Requested URL", validators=[DataRequired(), Length(min=0, max=256)])
    expected_dimensionality = IntegerField("Expected Dimensionality",
                                           validators=[DataRequired(), NumberRange(min=0)])

    def __init__(self, *args, **kwargs):
        """Create instance."""
        super(CreateRequestForm, self).__init__(*args, **kwargs)

    def validate(self, extra_validators=None):
        """Validate the form."""
        initial_validation = super(CreateRequestForm, self).validate()
        if not initial_validation:
            return False
        user = User.query.filter_by(id=self.requesting_user.data).first()
        if not user:
            self.requesting_user.errors.append("This user doesn't exist")
            return False
        return True
