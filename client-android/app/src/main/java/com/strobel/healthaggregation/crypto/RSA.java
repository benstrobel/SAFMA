package com.strobel.healthaggregation.crypto;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSA {

    private PrivateKey privateKey;      //Used to decode data which was encoded with the corresponding public key
    private PublicKey publicKey;        // Used to encode data for the owner of the corresponding private key to read
    private Cipher cipher;

    public static final int keysize = 512;

    public RSA(){
        try {
            cipher = Cipher.getInstance("RSA");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keysize);
            KeyPair keys = keyPairGenerator.generateKeyPair();
            privateKey = keys.getPrivate();
            publicKey = keys.getPublic();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            cipher = null;
        }
    }

    public byte [] encrypt(byte [] data) {
        return encrypt(data, publicKey);
    }

    public byte [] encrypt(byte [] data, Key key) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE,key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        try {
            return cipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            return null;
        }
    }

    public byte [] decrypt(byte [] data) {
        try {
            cipher.init(Cipher.DECRYPT_MODE,privateKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        try {
            return cipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            return null;
        }
    }

    public byte[] getPublicKey(){
        return new X509EncodedKeySpec(publicKey.getEncoded()).getEncoded();
    }

    public static Key getForeignPublicKeyFromBytes(byte [] encodedPublicKey){
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

}