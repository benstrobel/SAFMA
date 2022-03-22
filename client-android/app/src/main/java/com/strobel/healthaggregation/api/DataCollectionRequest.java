package com.strobel.healthaggregation.api;


import androidx.annotation.Nullable;

import com.strobel.healthaggregation.mediators.DataCollectionResponseMediator;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


public class DataCollectionRequest extends HealthAggregationRequest<DataCollectionResponseMediator> {
    private static final String TAG = "DataCollectionRequest";
    private final String sessionKey;

    public DataCollectionRequest(String url, String sessionKey, @Nullable JSONObject jsonRequest, Consumer<DataCollectionResponseMediator> consumer, Object waitObject) {
        super(url, jsonRequest, DataCollectionResponseMediator.class, consumer, waitObject);
        this.sessionKey = sessionKey;
    }

    @Override
    public Map<String, String> getHeaders() {
        return new HashMap<String, String>() {{
            put("Authorization", "Bearer " + sessionKey);
        }};
    }
}
