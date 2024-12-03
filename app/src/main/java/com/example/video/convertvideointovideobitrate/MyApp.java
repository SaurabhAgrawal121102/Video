package com.example.video.convertvideointovideobitrate;

import android.app.Application;


import androidx.media3.common.util.UnstableApi;
import androidx.media3.database.DatabaseProvider;
import androidx.media3.database.ExoDatabaseProvider;
import androidx.media3.datasource.cache.Cache;
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;

import java.io.File;

@UnstableApi
public class MyApp extends Application {
    public static Cache simpleCache;

    @Override
    public void onCreate() {
        super.onCreate();
        File cacheDir = new File(getCacheDir(), "media_cache");
        long cacheSize = 100 * 1024 * 1024; // 100MB
        DatabaseProvider databaseProvider = new ExoDatabaseProvider(this);
        simpleCache = new SimpleCache(cacheDir, new LeastRecentlyUsedCacheEvictor(cacheSize), databaseProvider);
    }
}
