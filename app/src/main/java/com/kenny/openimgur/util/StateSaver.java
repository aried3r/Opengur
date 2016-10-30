package com.kenny.openimgur.util;

import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LruCache;

import java.util.ArrayList;

public class StateSaver {
    private static final String TAG = StateSaver.class.getSimpleName();

    private LruCache<String, ArrayList<? extends Parcelable>> savedStateCache = null;

    private static final StateSaver instance = new StateSaver();

    public static StateSaver instance() {
        return instance;
    }

    StateSaver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LogUtil.v(TAG, "Build is Targeting N or higher, creating cache");
            savedStateCache = new LruCache<>(1024 * 5);
        } else {
            LogUtil.v(TAG, "Build targeting lower than N, no cache needed");
        }
    }

    public void onSaveState(@NonNull Bundle bundle, @NonNull String key, ArrayList<? extends Parcelable> data) {
        if (savedStateCache != null) {
            LogUtil.v(TAG, "Saving data to cache with key: " + key);
            savedStateCache.put(key, data);
        } else {
            LogUtil.v(TAG, "Saving data to Bundle");
            bundle.putParcelableArrayList(key, data);
        }
    }

    @Nullable
    public <T extends Parcelable> ArrayList<T> getData(@Nullable Bundle bundle, @NonNull String key) {
        if (savedStateCache != null) {
            try {
                LogUtil.v(TAG, "Fetching item from cache, size: " + savedStateCache.size() + " with key: " + key);
                ArrayList<T> data = (ArrayList<T>) savedStateCache.remove(key);
                LogUtil.v(TAG, "Size after fetch: " + savedStateCache.size());
                return data;
            } catch (ClassCastException ex) {
                LogUtil.w(TAG, "Error fetching list from cache", ex);
                return null;
            }
        }

        if (bundle != null) return bundle.getParcelableArrayList(key);
        return null;
    }

    public boolean contains(@Nullable Bundle bundle, @NonNull String key) {
        if (savedStateCache != null) {
            return savedStateCache.get(key) != null;
        }

        return bundle != null && bundle.containsKey(key);
    }

    public boolean remove(@NonNull String key) {
        return savedStateCache != null && savedStateCache.remove(key) != null;
    }
}