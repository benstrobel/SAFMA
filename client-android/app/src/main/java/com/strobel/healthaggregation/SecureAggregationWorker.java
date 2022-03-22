package com.strobel.healthaggregation;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.strobel.healthaggregation.api.ApiWrapper;
import com.strobel.healthaggregation.crypto.DiffieHellmann;
import com.strobel.healthaggregation.crypto.OneTimeHybridCrypto;
import com.strobel.healthaggregation.crypto.PseudorandomGenerator;
import com.strobel.healthaggregation.mediators.DataCollectionResponseMediator;
import com.strobel.healthaggregation.mediators.DecryptedShareMediator;
import com.strobel.healthaggregation.mediators.AdvertiseKeysResponseMediator;
import com.strobel.healthaggregation.mediators.ParticipatingDeviceMediator;
import com.strobel.healthaggregation.mediators.ParticipatingDeviceSecretShareForThisClientMediator;
import com.strobel.healthaggregation.mediators.ParticipatingDeviceSecretShareMediator;
import com.strobel.healthaggregation.payload.URLResolver;
import com.strobel.healthaggregation.crypto.shamir.ShamirScheme;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SecureAggregationWorker extends Worker {
    private static final String TAG = "SecureAggregationWorker";
    private static final SecureRandom random = new SecureRandom();

    public SecureAggregationWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String requestId = getInputData().getString("request_id");
        String url = getInputData().getString("requested_url");
        int dimensionality = getInputData().getInt("request_expected_dimensionality", -1);
        DiffieHellmann diffieHellmann = new DiffieHellmann();
        OneTimeHybridCrypto hybridCrypto = new OneTimeHybridCrypto();

        Log.d(TAG, "Queueing Request");
        ApiWrapper api = new ApiWrapper(getApplicationContext(), "https://healthaggregation.benstrobel.de");

        String ownPublicKey = Base64.encodeToString(hybridCrypto.getPublicRSAKey(), Base64.DEFAULT);

        AdvertiseKeysResponseMediator advertiseKeysResponse = api.sendAdvertiseKeysRequest(
                requestId,
                ownPublicKey,
                Base64.encodeToString(diffieHellmann.getPublicDiffieHellmannKey(), Base64.DEFAULT)
        );

        String ownSessionKey = advertiseKeysResponse.getSessionKey();

        Log.d(TAG, "Received Response/Error" + advertiseKeysResponse);

        OptionalInt optionalOwnIndex = IntStream
                .range(0, advertiseKeysResponse.getParticipatingDevices().size())
                .filter(i -> advertiseKeysResponse.getParticipatingDevices().get(i).getPublicKey().equals(ownPublicKey))
                .findFirst();

        if(!optionalOwnIndex.isPresent()) {
            throw new RuntimeException("Server responded without including own public key");
        }
        int ownIndex = optionalOwnIndex.getAsInt();
        int ownDeviceId = advertiseKeysResponse.getParticipatingDevices().get(ownIndex).getId();

        int numberOfParticipatingDevices = advertiseKeysResponse.getParticipatingDevices().size();

        if(numberOfParticipatingDevices < 3) {
            return Result.failure();
        }

        byte[][] partnerPublicComponents = advertiseKeysResponse.getParticipatingDevices().stream().map(x -> x.getId() == ownDeviceId ? new byte[]{} : Base64.decode(x.getDiffieHellmannPublicComponent(), Base64.DEFAULT)).toArray(byte[][]::new);

        diffieHellmann.acceptPartnersPublicKeys(partnerPublicComponents);

        long[] returnValue = URLResolver.INSTANCE.resolve(url, getApplicationContext());

        // Will contain a share of every shared secret with an other client for every other client
        // Will hold n^2 * 32 bytes for n participating clients
        // First dimension is the client id the shares are destined for
        // Second dimension is the other client the perturbation secret is shared with
        // Third dimension contains the shared secret share encrypted with the public key of the client the share is destined for
        final List<ParticipatingDeviceSecretShareMediator> secretShares = new ArrayList<>();

        for(int i = 0; i < numberOfParticipatingDevices; i++) {
            if(i == ownIndex) continue;

            byte[] sharedSecret = diffieHellmann.getSharedSecrets()[i];

            ShamirScheme scheme = new ShamirScheme(random, numberOfParticipatingDevices, (int)Math.ceil(((double)numberOfParticipatingDevices)/2));
            final Map<Integer, byte[]> shares = scheme.split(sharedSecret);
            int finalI = i;
            // Share ids are 1 indexed, list is 0 indexed
            shares.forEach((id, share) -> secretShares.add(new ParticipatingDeviceSecretShareMediator(
                    id,
                    advertiseKeysResponse.getParticipatingDevices().get(id-1).getId(),
                    advertiseKeysResponse.getParticipatingDevices().get(finalI).getId(),
                    hybridCrypto.encrypt(share, OneTimeHybridCrypto.getForeignPublicRSAKeyFromBytes(Base64.decode(advertiseKeysResponse.getParticipatingDevices().get(id-1).getPublicKey(), Base64.DEFAULT)))
            )));

            PseudorandomGenerator generator = new PseudorandomGenerator(sharedSecret);
            long [] singlePerturbation = generator.generateRandomPerturbation(returnValue.length);
            BiFunction<Long, Long, Long> func = ownIndex > i ? Long::sum : (x, y) -> x - y;
            for(int x = 0; x < returnValue.length; x++) {
                returnValue[x] = func.apply(returnValue[x], singlePerturbation[x]);
            }
        }

        DataCollectionResponseMediator dataCollectionResponse = api.sendDataCollectionRequest(requestId, ownSessionKey, returnValue, secretShares);

        final Set<Integer> respondingParticipatingDevices = dataCollectionResponse.getSecretShares().stream()
                .map(ParticipatingDeviceSecretShareForThisClientMediator::getSecretWithDevice)
                .collect(Collectors.toSet());

        final Set<Integer> nonRespondingParticipatingDevices = advertiseKeysResponse.getParticipatingDevices().stream()
                .map(ParticipatingDeviceMediator::getId)
                .filter(x -> !respondingParticipatingDevices.contains(x))
                .collect(Collectors.toSet());

        if (nonRespondingParticipatingDevices.size() == 0){
            return Result.success();
        }

        final List<DecryptedShareMediator> decryptedSecretShares = dataCollectionResponse.getSecretShares().stream()
                .filter(x -> nonRespondingParticipatingDevices.contains(x.getSecretOfDroppedDevice()))
                .map(x -> new DecryptedShareMediator(x.getShareId(), x.getSecretOfDroppedDevice() ,x.getSecretWithDevice(), hybridCrypto.decrypt(x.getEncrypedShare())))
                .collect(Collectors.toList());

        api.sendRecoveryRequest(requestId, ownSessionKey, decryptedSecretShares);

        return Result.success();
    }
}
