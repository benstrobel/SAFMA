package com.strobel.healthaggregation.payload.datasources;

import android.content.Context;

import java.util.List;
import java.util.Map;

public class DummyDataSource extends DataSource{

    @Override
    public long[] resolve(Map<String, List<String>> params, Context context) {
        return new long [] {
                1,
                22,
                1337,
                4,
                420,
                6,
                7
        };
    }
}
