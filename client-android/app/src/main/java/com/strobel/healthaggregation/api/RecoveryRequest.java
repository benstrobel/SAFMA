package com.strobel.healthaggregation.api;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RecoveryRequest extends HealthAggregationRequest<Void> {
    private final String sessionKey;

    public RecoveryRequest(String url, String sessionKey, @Nullable JSONObject jsonRequest, Consumer<Void> consumer, Object waitObject) {
        super(url, jsonRequest, Void.class, consumer, waitObject);
        this.sessionKey = sessionKey;
    }

    @Override
    public Map<String, String> getHeaders() {
        return new HashMap<String, String>() {{
            put("Authorization", "Bearer " + sessionKey);
        }};
    }
}
