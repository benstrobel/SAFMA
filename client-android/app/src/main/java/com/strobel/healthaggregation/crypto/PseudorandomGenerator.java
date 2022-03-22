package com.strobel.healthaggregation.crypto;

import com.goterl.lazysodium.LazySodiumAndroid;
import com.goterl.lazysodium.SodiumAndroid;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class PseudorandomGenerator {

    private static final String TAG = "PseudorandomGenerator";
    private static final LazySodiumAndroid sodium = new LazySodiumAndroid(new SodiumAndroid());
    private static final ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);

    private BigInteger seed;

    public PseudorandomGenerator(byte [] seed) {
        this.seed = new BigInteger(seed);
    }

    private byte[] getAndIncrementSeed() {
        byte [] seed = this.seed.toByteArray();
        this.seed = this.seed.add(BigInteger.ONE);
        try {
            // The LibSodium Pseudorandomgenerator only works with a 32 byte seed
            return Arrays.copyOf(MessageDigest.getInstance("SHA-256").digest(seed), 32);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] generateRandomBytes(int x) {
        return sodium.randomBytesDeterministic(x, getAndIncrementSeed());
    }

    public long[] generateRandomPerturbation(int length) {
        long[] ret = new long[length];
        for(int i = 0; i < ret.length; i++) {
            byteBuffer.clear();
            byteBuffer.put(generateRandomBytes(Long.BYTES));
            byteBuffer.flip();
            ret[i] = byteBuffer.getLong();
        }
        return ret;
    }
}