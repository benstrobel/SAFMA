package com.strobel.healthaggregation.mediators;

import com.google.gson.annotations.SerializedName;

public class ParticipatingDeviceSecretShareMediator {
    @SerializedName("share_id")
    private final int shareId;
    @SerializedName("encrypted_for_device")
    private final int encryptedForDevice;
    @SerializedName("other_device")
    private final int otherDevice;
    @SerializedName("encrypted_share")
    private final byte[] encrypedShare;

    public ParticipatingDeviceSecretShareMediator(int shareId, int encryptedForDevice, int otherDevice, byte[] encrypedShare) {
        this.shareId = shareId;
        this.encryptedForDevice = encryptedForDevice;
        this.otherDevice = otherDevice;
        this.encrypedShare = encrypedShare;
    }

    public int getShareId() {
        return shareId;
    }

    public int getEncryptedForDevice() {
        return encryptedForDevice;
    }

    public byte[] getEncrypedShare() {
        return encrypedShare;
    }

    public int getOtherDevice() {
        return otherDevice;
    }
}
