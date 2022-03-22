package com.strobel.healthaggregation.api;

import androidx.annotation.Nullable;

import com.strobel.healthaggregation.mediators.AdvertiseKeysResponseMediator;

import org.json.JSONObject;

import java.util.function.Consumer;

public class AdvertiseKeysRequest extends HealthAggregationRequest<AdvertiseKeysResponseMediator>{
    public AdvertiseKeysRequest(String url, @Nullable JSONObject jsonRequest, Consumer<AdvertiseKeysResponseMediator> consumer, Object waitObject) {
        super(url, jsonRequest, AdvertiseKeysResponseMediator.class, consumer, waitObject);
    }
}
