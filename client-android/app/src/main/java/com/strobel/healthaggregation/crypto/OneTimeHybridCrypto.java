package com.strobel.healthaggregation.crypto;

import java.security.Key;
import java.util.Arrays;

import javax.crypto.SecretKey;

public class OneTimeHybridCrypto {

    public static Key getForeignPublicRSAKeyFromBytes(byte [] encodedPublicKey) {
        return RSA.getForeignPublicKeyFromBytes(encodedPublicKey);
    }

    private final RSA rsa = new RSA();

    public byte[] getPublicRSAKey() {
        return rsa.getPublicKey();
    }

    public byte[] encrypt(byte[] data, Key key) {
        SecretKey aesKey = AES.generateAESKey();
        assert aesKey != null;
        AES aes = new AES(aesKey);
        byte[] encryptedAESKey = rsa.encrypt(AES.writeKeyToBytes(aesKey), key);
        byte[] encryptedPayload = aes.encrypt(data);
        byte[] result = Arrays.copyOf(encryptedAESKey, encryptedAESKey.length + encryptedPayload.length);
        System.arraycopy(encryptedPayload, 0, result, encryptedAESKey.length, encryptedPayload.length);
        return result;
    }

    public byte[] decrypt(byte[] data) {
        byte[] encryptedAESKey = Arrays.copyOfRange(data, 0, RSA.keysize/8);
        byte[] encryptedPayload = Arrays.copyOfRange(data, RSA.keysize/8, data.length);
        AES aes = new AES(AES.readKeyFromBytes(rsa.decrypt(encryptedAESKey)));
        return aes.decrypt(encryptedPayload);
    }
}
