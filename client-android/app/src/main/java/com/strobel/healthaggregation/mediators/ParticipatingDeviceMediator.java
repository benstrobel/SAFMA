package com.strobel.healthaggregation.mediators;

import com.google.gson.annotations.SerializedName;

public class ParticipatingDeviceMediator {
    @SerializedName("id")
    private final int id;
    @SerializedName("request_id")
    private final int requestId;
    @SerializedName("public_key")
    private final String publicKey;
    @SerializedName("diffie_hellmann_public_component")
    private final String diffieHellmannPublicComponent;

    public ParticipatingDeviceMediator(int id, int requestId, String publicKey, String diffieHellmannPublicComponent) {
        this.id = id;
        this.requestId = requestId;
        this.publicKey = publicKey;
        this.diffieHellmannPublicComponent = diffieHellmannPublicComponent;
    }

    public int getId() {
        return id;
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
