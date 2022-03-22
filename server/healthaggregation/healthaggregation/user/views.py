# -*- coding: utf-8 -*-
"""User views."""
from flask import Blueprint, render_template, flash, redirect, url_for, request, Response
from flask_login import login_required, current_user

from healthaggregation.user.forms import CreateForm
from healthaggregation.user.models import User
from healthaggregation.utils import flash_errors

blueprint = Blueprint("user", __name__, url_prefix="/users", static_folder="../static")


@blueprint.route("/")
@login_required
def members():
    """List members."""
    users = User.query
    return render_template("users/members.html", users=users)


@blueprint.route("/create", methods=["GET", "POST"])
@login_required
def create():
    """Register new user."""
    if not current_user.is_admin:
        return Response("403 Forbidden", status=403)
    form = CreateForm(request.form)
    if form.validate_on_submit():
        User.create(
            username=form.username.data,
            email=form.email.data,
            first_name=form.first_name.data,
            last_name=form.last_name.data,
            password=form.password.data,
            active=True,
        )
        return redirect(url_for("user.members"))
    else:
        flash_errors(form)
    return render_template("users/create.html", form=form)


@blueprint.route("/makeadmin", methods=["GET", "POST"])
@login_required
def makeadmin():
    """Promote a user to admin"""
    if request.args is None or "id" not in request.args:
        return Response({"400 Bad Request"}, status=400)
    if not current_user.is_admin:
        return Response("403 Forbidden", status=403)
    user = User.query.filter_by(id=request.args.get("id")).first()
    if user is None:
        return Response("404 Account Not Found", status=404)
    user.is_admin = True
    user.update()
    return redirect(url_for("user.members"))


@blueprint.route("/delete", methods=["GET", "POST"])
@login_required
def delete():
    """Delete a user."""
    if request.args is None or "id" not in request.args:
        return Response({"400 Bad Request"}, status=400)
    if not current_user.is_admin:
        return Response("403 Forbidden", status=403)
    user = User.query.filter_by(id=request.args.get("id")).first()
    if user is None:
        return Response("404 Account Not Found", status=404)
    if user.id == current_user.id:
        flash("You can't delete your own account", "info")
        return redirect(url_for("user.members"))
    user.delete()
    return redirect(url_for("user.members"))
