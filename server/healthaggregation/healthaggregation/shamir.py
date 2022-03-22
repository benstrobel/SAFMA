from gf256 import GF256
from typing import Dict


def signed_byte_to_unsigned_byte(input):
    output = []
    for b in input:
        output.append(b & 0xff)
    return output


def interpolate(points: [bytearray]):
    x = GF256(0)
    y = GF256(0)
    for i in range(len(points)):
        aX = points[i][0]
        aY = points[i][1]
        li = GF256(1)
        for j in range(len(points)):
            bX = points[j][0]
            if i != j:
                divisor = (GF256(aX) - GF256(bX))
                li = li * ((x - GF256(bX)) / divisor) if divisor.__int__() != 0 else GF256(0)
        y = y + (li * GF256(aY))
    return y.__int__()


def join(parts: Dict[int, bytearray]):
    if len(parts) <= 0:
        print("No parts provided")
        return
    lengths = set(map(lambda x: len(x), parts.values()))
    if len(lengths) > 1:
        print("Varying lengths of part values")
        return
    length = lengths.pop()
    secret: bytearray = bytearray(length)
    for i in range(length):
        points: [bytearray] = [bytearray(2) for x in range(len(parts))]
        j = 0
        for k, v in parts.items():
            points[j][0] = k
            points[j][1] = v[i]
            j += 1
        secret[i] = interpolate(points)
    return bytes(secret)

