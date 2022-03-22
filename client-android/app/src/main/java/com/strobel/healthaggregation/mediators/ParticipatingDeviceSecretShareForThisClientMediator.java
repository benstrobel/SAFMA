package com.strobel.healthaggregation.mediators;

import com.google.gson.annotations.SerializedName;

public class ParticipatingDeviceSecretShareForThisClientMediator {
    @SerializedName("request_id")
    private final int requestId;
    @SerializedName("encrypted_for_device")
    private final int encryptedForDevice;
    @SerializedName("secret_of_dropped_device")
    private final int secretOfDroppedDevice;
    @SerializedName("secret_with_device")
    private final int secretWithDevice;
    @SerializedName("encrypted_share")
    private final byte[] encrypedShare;
    @SerializedName("share_id")
    private final int shareId;

    public ParticipatingDeviceSecretShareForThisClientMediator(int requestId, int encryptedForDevice, int secretOfDroppedDevice, int secretWithDevice, byte[] encrypedShare, int shareId) {
        this.requestId = requestId;
        this.encryptedForDevice = encryptedForDevice;
        this.secretOfDroppedDevice = secretOfDroppedDevice;
        this.secretWithDevice = secretWithDevice;
        this.encrypedShare = encrypedShare;
        this.shareId = shareId;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getEncryptedForDevice() {
        return encryptedForDevice;
    }

    public int getSecretOfDroppedDevice() {
        return secretOfDroppedDevice;
    }

    public int getSecretWithDevice() {
        return secretWithDevice;
    }

    public byte[] getEncrypedShare() {
        return encrypedShare;
    }

    public int getShareId() {
        return shareId;
    }
}
