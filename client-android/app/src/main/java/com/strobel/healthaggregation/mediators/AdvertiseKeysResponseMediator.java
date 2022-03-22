package com.strobel.healthaggregation.mediators;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdvertiseKeysResponseMediator {
    @SerializedName("request_id")
    private final int requestId;
    @SerializedName("session_key")
    private final String sessionKey;
    @SerializedName("participating_devices")
    private final List<ParticipatingDeviceMediator> participatingDevices;

    public AdvertiseKeysResponseMediator(int requestId, String sessionKey, List<ParticipatingDeviceMediator> participatingDevices) {
        this.requestId = requestId;
        this.sessionKey = sessionKey;
        this.participatingDevices = participatingDevices;
    }

    public int getRequestId() {
        return requestId;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public List<ParticipatingDeviceMediator> getParticipatingDevices() {
        return participatingDevices;
    }
}
