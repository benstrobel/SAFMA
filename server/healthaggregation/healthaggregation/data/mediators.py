

class ParticipatingDevice:
    def __init__(self, request_id: int, public_key: str):
        self.request_id = request_id
        self.public_key = public_key


class ParticipateResponseMediator:
    def __init__(self, request_id: int, session_key: str, participating_devices: [ParticipatingDevice]):
        self.request_id = request_id
        self.session_key = session_key
        self.participating_devices = participating_devices

