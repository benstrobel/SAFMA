package com.strobel.healthaggregation.api;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.strobel.healthaggregation.MainActivity;
import com.strobel.healthaggregation.mediators.DataCollectionResponseMediator;
import com.strobel.healthaggregation.mediators.DecryptedShareMediator;
import com.strobel.healthaggregation.mediators.AdvertiseKeysResponseMediator;
import com.strobel.healthaggregation.mediators.ParticipatingDeviceSecretShareMediator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.function.Consumer;

public class ApiWrapper {

    private static final String TAG = "ApiWrapper";

    private final String serverURL;
    private final Context context;
    private final RequestQueue queue;

    private final int advertiseKeysRequestTimeOutInSecs = 35; // Server waits up to 30 secs to respond
    private final int dataCollectionRequestTimeOutInSecs = 10; // Server waits up to 10 secs to respond


    public ApiWrapper(@NonNull Context context, @NonNull String serverURL) {
        this.context = context;
        this.serverURL = serverURL;
        this.queue = Volley.newRequestQueue(context);
    }

    public void sendRecoveryRequest(String requestId, String sessionKey, List<DecryptedShareMediator> decryptedSecretShares){
        JSONObject requestObject = new JSONObject();
        JSONArray shareArray = new JSONArray();

        decryptedSecretShares.forEach(x -> {
            try {
                shareArray.put(new JSONObject(MainActivity.GSON.toJson(x)));
            } catch (JSONException e) {
                Log.e(TAG, "org.json couldn't deserialize object serialized by gson");
            }
        });

        try {
            requestObject.put("id", requestId);
            requestObject.put("secret_shares", shareArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Object waitObject = new Object();
        RecoveryRequest request = new RecoveryRequest(
                serverURL + "/api/request/recovery",
                sessionKey,
                requestObject,
                x -> {},
                waitObject
                );
        request.setRetryPolicy(new DefaultRetryPolicy(2500, 0, 1.0F));
        queue.add(request);

        try {
            synchronized (waitObject) {
                waitObject.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public DataCollectionResponseMediator sendDataCollectionRequest(String requestId, String sessionKey, long[] data, List<ParticipatingDeviceSecretShareMediator> secretShares) {
        JSONObject requestObject = new JSONObject();
        JSONArray dataArray = new JSONArray();
        for(long d : data) {
            dataArray.put(d);
        }
        JSONArray secretSharesArray = new JSONArray();
        for(ParticipatingDeviceSecretShareMediator secretShare : secretShares) {
            try {
                secretSharesArray.put(new JSONObject(MainActivity.GSON.toJson(secretShare)));
            } catch (JSONException e) {
                Log.e(TAG, "org.json couldn't deserialize object serialized by gson");
            }
        }

        try {
            requestObject.put("id", requestId);
            requestObject.put("data", dataArray);
            requestObject.put("secret_shares", secretSharesArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Object waitObject = new Object();
        final DataCollectionResponseMediator[] response = new DataCollectionResponseMediator[1];

        Consumer<DataCollectionResponseMediator> setter = dataCollectionResponseMediator -> response[0] = dataCollectionResponseMediator;

        DataCollectionRequest request = new DataCollectionRequest(serverURL + "/api/request/data_collection", sessionKey, requestObject, setter, waitObject);
        request.setRetryPolicy(new DefaultRetryPolicy(dataCollectionRequestTimeOutInSecs *1000, 0, 1.0F));
        queue.add(request);

        try {
            synchronized (waitObject) {
                waitObject.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return response[0];
    }

    public AdvertiseKeysResponseMediator sendAdvertiseKeysRequest(String requestId, String ownPublicKey, String diffieHellmannPublicComponent) {
        JSONObject requestObject = new JSONObject();

        try {
            requestObject.put("id", requestId);
            requestObject.put("public_key", ownPublicKey);
            requestObject.put("diffie_hellmann_public_component", diffieHellmannPublicComponent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Object waitObject = new Object();
        final AdvertiseKeysResponseMediator[] response = new AdvertiseKeysResponseMediator[1];

        Consumer<AdvertiseKeysResponseMediator> setter = advertiseKeysResponseMediator -> response[0] = advertiseKeysResponseMediator;

        AdvertiseKeysRequest request = new AdvertiseKeysRequest(serverURL + "/api/request/advertise_keys", requestObject, setter, waitObject);
        request.setRetryPolicy(new DefaultRetryPolicy(advertiseKeysRequestTimeOutInSecs *1000, 0, 1.0F));
        queue.add(request);

        try {
            synchronized (waitObject) {
                waitObject.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return response[0];
    }

}
