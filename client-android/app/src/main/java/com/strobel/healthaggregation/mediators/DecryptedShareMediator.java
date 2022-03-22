package com.strobel.healthaggregation.mediators;

import com.google.gson.annotations.SerializedName;

public class DecryptedShareMediator {
    @SerializedName("share_id")
    private final int shareId;
    @SerializedName("secret_of_dropped_device")
    private final int secretOfDroppedDevice;
    @SerializedName("secret_with_device")
    private final int secretWithDevice;
    @SerializedName("decrypted_share")
    private final byte[] decrypedShare;

    public DecryptedShareMediator(int shareId, int secretOfDroppedDevice, int secretWithDevice, byte[] decrypedShare) {
        this.shareId = shareId;
        this.secretOfDroppedDevice = secretOfDroppedDevice;
        this.secretWithDevice = secretWithDevice;
        this.decrypedShare = decrypedShare;
    }

    public int getShareId() {
        return shareId;
    }

    public int getSecretOfDroppedDevice() {
        return secretOfDroppedDevice;
    }

    public int getSecretWithDevice() {
        return secretWithDevice;
    }

    public byte[] getDecrypedShare() {
        return decrypedShare;
    }
}
