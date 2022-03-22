import json

from pyfcm import FCMNotification

from healthaggregation.settings import FCM_API_KEY

TIME_TO_LIVE = 120
push_service = FCMNotification(api_key=FCM_API_KEY)


def send_new_data_request(request):
    __push_data_message__("data_requests", data_dict={
        "requested_at": request.created_at.isoformat(),
        "requested_url": request.requested_url,
        "request_id": request.id,
        "request_expected_dimensionality": request.expected_dimensionality
    })


def __push_notification_message__(topic, message_body):
    push_service.notify_topic_subscribers(topic_name=topic,
                                          message_body=message_body,
                                          time_to_live=TIME_TO_LIVE)


def __push_data_message__(topic, data_dict):
    push_service.topic_subscribers_data_message(topic_name=topic,
                                                data_message=data_dict,
                                                time_to_live=TIME_TO_LIVE)
