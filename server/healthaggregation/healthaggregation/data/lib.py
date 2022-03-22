from collections import defaultdict
from math import ceil

from healthaggregation.data.models import DataRequests, ParticipatingDevice, DroppedClientSecretShares, DataResponse
import numpy as np
import datetime
import time

from healthaggregation.pseudorandomgenerator import PseudoRandomGenerator
from healthaggregation.shamir import signed_byte_to_unsigned_byte, join


def get_active_requests():
    return DataRequests.query.all()


def get_responses(data_request: DataRequests):
    data_sum = np.empty(0)
    response_count = len(data_request.responses)
    if response_count > 0:
        data_sum = np.repeat(0, data_request.expected_dimensionality)
        for i in range(len(data_request.responses)):
            data = np.asarray(data_request.responses[i].data)
            data_sum += data
        recovered_perturbations = list(
            recover_missing_perturbations(data_request.expected_dimensionality, data_request.id)
        )
        for i in range(len(recovered_perturbations)):
            data = np.asarray(recovered_perturbations[i].data)
            data_sum += data
    data_avg = data_sum / response_count
    first_round_participants = ParticipatingDevice.query.filter_by(request_id=data_request.id).count()
    second_round_participants = response_count
    dropped_secret_shares = DroppedClientSecretShares.query.filter_by(request_id=data_request.id).all()
    third_round_participants = set()
    for share in dropped_secret_shares:
        third_round_participants.add(share.secret_with_device)
    third_round_participants = len(third_round_participants)
    print_reconstruction_warning = first_round_participants > second_round_participants and third_round_participants < ceil(
        first_round_participants / 2)
    return data_avg.tolist(), \
        data_sum.tolist(), \
        first_round_participants, \
        second_round_participants, \
        third_round_participants, \
        print_reconstruction_warning


def get_perturbation_seed(secret_shares: [DroppedClientSecretShares]):
    parts = {}

    for share in secret_shares:
        parts[share.share_id] = bytearray(signed_byte_to_unsigned_byte(share.decrypted_share))
    return join(parts)


def recover_missing_perturbations(size: int, request_id: int):
    data_request = DataRequests.query.filter_by(id=request_id).first()
    if data_request is None:
        return
    number_of_participating_clients = len(ParticipatingDevice.query.filter_by(request_id=data_request.id).all())
    dropped_secret_shares = DroppedClientSecretShares.query.filter_by(request_id=data_request.id).all()
    shares_by_dropped_client = defaultdict(list)
    for share in dropped_secret_shares:
        shares_by_dropped_client[(share.secret_of_dropped_device, share.secret_with_device)].append(share)
    for (dropped_device, with_device) in shares_by_dropped_client:
        shares = shares_by_dropped_client[(dropped_device, with_device)]
        if len(shares) >= ceil(number_of_participating_clients/2):
            seed = get_perturbation_seed(shares)
            seeded_random_generator = PseudoRandomGenerator(seed)
            missing_perturbation = list(map(lambda x: int(x) if dropped_device > with_device else -int(x),
                                            seeded_random_generator.generate_random_perturbation(size)))
            yield DataResponse(request=data_request.id, data=missing_perturbation)


def wait_x_since(since: datetime.datetime, timedelta: datetime.timedelta):
    now = datetime.datetime.now()
    if since + timedelta > now:
        time.sleep(timedelta.seconds - (now - since).seconds)
        return True
    else:
        return False
