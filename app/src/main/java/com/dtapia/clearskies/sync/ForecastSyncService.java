package com.dtapia.clearskies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ForecastSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static ForecastSyncAdapter sForecastSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("ForecastSyncService", "onCreate - ForecastSyncService");
        synchronized (sSyncAdapterLock) {
            if (sForecastSyncAdapter == null) {
                sForecastSyncAdapter = new ForecastSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sForecastSyncAdapter.getSyncAdapterBinder();
    }
}