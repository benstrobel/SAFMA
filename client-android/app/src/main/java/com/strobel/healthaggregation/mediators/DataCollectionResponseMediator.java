package com.strobel.healthaggregation.mediators;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataCollectionResponseMediator {
    @SerializedName("request_id")
    private final int requestId;
    @SerializedName("secret_shares")
    private final List<ParticipatingDeviceSecretShareForThisClientMediator> secretShares;

    public DataCollectionResponseMediator(int requestId, List<ParticipatingDeviceSecretShareForThisClientMediator> secretShares) {
        this.requestId = requestId;
        this.secretShares = secretShares;
    }

    public int getRequestId() {
        return requestId;
    }

    public List<ParticipatingDeviceSecretShareForThisClientMediator> getSecretShares() {
        return secretShares;
    }
}
