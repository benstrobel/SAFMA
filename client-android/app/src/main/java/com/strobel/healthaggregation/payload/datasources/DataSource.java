package com.strobel.healthaggregation.payload.datasources;

import android.content.Context;

import java.util.List;
import java.util.Map;

public abstract class DataSource {

    public abstract long[] resolve(Map<String, List<String>> params , Context context);
}
