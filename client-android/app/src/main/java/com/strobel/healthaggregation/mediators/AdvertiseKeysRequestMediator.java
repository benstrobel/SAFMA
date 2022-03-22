package com.strobel.healthaggregation.mediators;

public class AdvertiseKeysRequestMediator {
    private final int requestId;
    private final String publicKey;
    private final String diffieHellmannPublicComponent;

    public AdvertiseKeysRequestMediator(int requestId, String publicKey, String diffieHellmannPublicComponent) {
        this.requestId = requestId;
        this.publicKey = publicKey;
        this.diffieHellmannPublicComponent = diffieHellmannPublicComponent;
    }

    public int getRequestId() {
        return requestId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getDiffieHellmannPublicComponent() {
        return diffieHellmannPublicComponent;
    }
}
