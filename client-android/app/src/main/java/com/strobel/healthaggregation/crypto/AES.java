package com.strobel.healthaggregation.crypto;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class AES {

    private Key key;
    private Cipher cipher;
    public static final int keysize = 256;

    public static SecretKey generateAESKey(){
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(keysize);
            return keygen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte [] writeKeyToBytes(SecretKey key){
        return key.getEncoded();
    }

    public static SecretKey readKeyFromBytes(byte [] encodedkey){
        return new SecretKeySpec(encodedkey,"AES");
    }

    public static void writeKeyToFile(SecretKey key, File targetFile){
        byte[] bytes = key.getEncoded();
        FileOutputStream keyfos = null;
        try {
            keyfos = new FileOutputStream(targetFile);
            keyfos.write(bytes);
            keyfos.close();
        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static SecretKey readKeyFromFile(File sourceFile){
        try {
            FileInputStream fis = new FileInputStream(sourceFile);
            byte[] encodedKey = new byte[(int) sourceFile.length()];
            fis.read(encodedKey);
            fis.close();
            SecretKey key = new SecretKeySpec(encodedKey,"AES");
            return key;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public AES(SecretKey key) {
        this.key = key;
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            cipher = null;
        }
    }

    public byte [] encrypt(byte [] data) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte [] decrypt(byte [] data) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
