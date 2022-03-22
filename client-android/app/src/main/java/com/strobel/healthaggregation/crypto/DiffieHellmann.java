package com.strobel.healthaggregation.crypto;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyAgreement;

// Implemented Diffie Hellmann Key Agreement with help of example provided by orcale https://docs.oracle.com/javase/7/docs/technotes/guides/security/crypto/CryptoSpec.html#DH2Ex

public class DiffieHellmann {
    private KeyPair diffieHellmannKeyPair;
    private KeyAgreement[] keyAgreements;
    private byte[][] sharedSecrets = null;

    public DiffieHellmann() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
            keyPairGenerator.initialize(512);
            diffieHellmannKeyPair = keyPairGenerator.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public DiffieHellmann(int keysize) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
            keyPairGenerator.initialize(keysize);
            diffieHellmannKeyPair = keyPairGenerator.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public byte[] getPublicDiffieHellmannKey() {
        return diffieHellmannKeyPair.getPublic().getEncoded();
    }

    /**
     * Accepts the partners public diffie hellmann keys / components and calculates
     * shared secret for each provided partner
     *
     * @param partnerPublicKeys Expects partners public keys in partners x keylength format
     */
    public void acceptPartnersPublicKeys(byte [][] partnerPublicKeys) {
        if(keyAgreements != null || sharedSecrets != null) throw  new RuntimeException("Already accepted Partner Keys");

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("DH");
            keyAgreements = new KeyAgreement[partnerPublicKeys.length];
            sharedSecrets = new byte[partnerPublicKeys.length][];
            for(int i = 0; i < partnerPublicKeys.length; i++) {
                byte[] partnerPublicKey = partnerPublicKeys[i];
                if (partnerPublicKey.length == 0) continue; // Exception for own public key
                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(partnerPublicKey);
                PublicKey partnersPubKey = keyFactory.generatePublic(x509KeySpec);

                keyAgreements[i] = KeyAgreement.getInstance("DH");
                keyAgreements[i].init(diffieHellmannKeyPair.getPrivate());

                keyAgreements[i].doPhase(partnersPubKey, true);
                sharedSecrets[i] = keyAgreements[i].generateSecret();
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the shared secrets calculated when accepting the public keys / components of the partners.
     * The indices of the partners are the same as the ones provided to the accept method
     *
     * @return Null or Shared secrets in partners x keylength format
     */
    public byte[][] getSharedSecrets() {
        return sharedSecrets;
    }
}
