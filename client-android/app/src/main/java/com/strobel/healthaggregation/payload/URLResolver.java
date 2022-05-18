package com.strobel.healthaggregation.payload;

import android.content.Context;

import com.strobel.healthaggregation.payload.datasources.DataSource;
import com.strobel.healthaggregation.payload.datasources.DummyDataSource;
import com.strobel.healthaggregation.payload.datasources.GoogleFitDataSource;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class URLResolver {

    public static URLResolver INSTANCE = new URLResolver();

    private Map<String, DataSource> dataSources = new HashMap<>();

    private URLResolver() {

    }

    public void registerDataSource(String URL, DataSource dataSource) {
        dataSources.put(URL, dataSource);
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

        for(Map.Entry<String, DataSource> entry : dataSources.entrySet()){
            if(entry.getKey().equals(url)){
                return entry.getValue().resolve(params, context);
            }
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
