package com.strobel.healthaggregation.payload;

import android.content.Context;

import com.strobel.healthaggregation.payload.datasources.DummyDataSource;
import com.strobel.healthaggregation.payload.datasources.GoogleFitDataSource;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class URLResolver {

    public static URLResolver INSTANCE = new URLResolver();
    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

    private URLResolver() {

    }

    public long[] resolve(String url, Context context) {
        if(url == null) return new long[] {};
        URI uri = null;
        Map<String, List<String>> params = null;

        try {
            uri = new URI(url);
            params = splitQuery(uri);
        }catch (URISyntaxException | UnsupportedEncodingException ex) {
            return new long[] {};
        }

        if(uri.getPath().equals("test")) {
            return DummyDataSource.getPayload();
        } else if (uri.getPath().equals("googlefit/steps")) {

            long [] result = new long[] {}; // This will break the dimension constraint server side and therefore lead to a dropout of this client

            if(params.get("date") != null && params.get("date").size() == 1){
                LocalDate requested_date = LocalDate.parse(params.get("date").get(0), formatter);

                if (params.get("date").size() == 1){
                    try {
                        result = new long[]{GoogleFitDataSource.getDailyStepCountForDateBlocking(context, requested_date)};
                    } catch (Exception e) { }

                    if(params.get("threshold") != null && params.get("threshold").size() >= 1){
                        int[] threshholds = params.get("threshold").stream().mapToInt(Integer::parseInt).sorted().toArray();
                        long oldResult = result[0];
                        result = new long[threshholds.length-1];
                        for(int i = 0; i < threshholds.length-1; i++) {
                            result[i] = (threshholds[i] < oldResult && oldResult <= threshholds[i+1]) ? 1 : 0;
                        }
                    }
                }
            } else if(params.get("fromDate") != null && params.get("fromDate").size() == 1 && params.get("toDate") != null && params.get("toDate").size() == 1){
                LocalDate fromDate = LocalDate.parse(params.get("fromDate").get(0), formatter);
                LocalDate toDate = LocalDate.parse(params.get("toDate").get(0), formatter);

                try {
                    result = Arrays.stream(GoogleFitDataSource.getDailyStepCountForDateRangeBlocking(context, fromDate, toDate)).asLongStream().toArray();
                } catch (Exception e) { }

                if(params.get("threshold") != null && params.get("threshold").size() == 1){
                    int threshold = Integer.parseInt(params.get("threshold").get(0));
                    for(int i = 0; i < result.length; i++) {
                        result[i] = result[i] > threshold ? 1 : 0;
                    }
                }
            }

            return result;
        }
        return new long[] {};
    }

    private Map<String, List<String>> splitQuery(URI url) throws UnsupportedEncodingException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        if (url.getQuery() == null) return query_pairs;
        final String[] pairs = url.getQuery().split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            query_pairs.get(key).add(value);
        }
        return query_pairs;
    }
}
