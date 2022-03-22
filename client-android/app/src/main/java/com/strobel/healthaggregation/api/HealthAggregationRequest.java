package com.strobel.healthaggregation.api;

import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.strobel.healthaggregation.MainActivity;
import com.strobel.healthaggregation.mediators.DummyMediator;

import org.json.JSONObject;

import java.util.function.Consumer;

public abstract class HealthAggregationRequest<T> extends JsonObjectRequest {
    private static final String TAG = "HealthAggregationRequest";
    private final Consumer<T> consumer;
    private final Object waitObject;

    public HealthAggregationRequest(String url, @Nullable JSONObject jsonRequest, Class<T> cls, Consumer<T> consumer, Object waitObject) {

        super(Request.Method.POST,
                url,
                jsonRequest,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        consumer.accept(MainActivity.GSON.fromJson(jsonObject.toString(), cls));
                        synchronized (waitObject) {
                            waitObject.notify();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                        synchronized (waitObject) {
                            waitObject.notify();
                        }
                    }
                });
        this.consumer = consumer;
        this.waitObject = waitObject;
    }
}
