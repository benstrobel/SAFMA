"""empty message

Revision ID: 9b5c27a72d45
Revises: bb21838e5e8e
Create Date: 2022-03-03 19:35:18.132695

"""
from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision = '9b5c27a72d45'
down_revision = 'bb21838e5e8e'
branch_labels = None
depends_on = None


def upgrade():
    op.execute("insert into users (username,email,password,created_at,active,is_admin) VALUES('admin', "
               "'admin@localhost.de', CAST('$2b$13$kVsoQlq.n5YtRFCdhwDrcOv9WQFuc95ZdnQkUGEncd2MqQD9VxG5y' AS BLOB), '2021-11-24 10:05:25.299547', 1, 1);")


def downgrade():
    op.execute("delete from users where username = 'admin';")
