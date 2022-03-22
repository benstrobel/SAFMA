from nacl.bindings import crypto_hash_sha256
from nacl.utils import randombytes_deterministic

java_long_byte_size = 8


class PseudoRandomGenerator:

    def __init__(self, seed: bytes):
        self.seed = int.from_bytes(seed, byteorder='big', signed=True)
        self.byte_length = len(seed)

    def __get_and_increment_seed__(self):
        seed_bytes = self.seed.to_bytes(self.byte_length, byteorder='big', signed=True)
        self.seed += 1
        # The LibSodium Pseudorandomgenerator only works with a 32 byte seed
        return crypto_hash_sha256(seed_bytes)[:32]

    def generate_random_bytes(self, size: int):
        return randombytes_deterministic(size, self.__get_and_increment_seed__())

    def generate_random_perturbation(self, length: int):
        perturbation = []
        for i in range(length):
            perturbation.append(int.from_bytes(self.generate_random_bytes(java_long_byte_size), byteorder='big', signed=True))
        return perturbation
