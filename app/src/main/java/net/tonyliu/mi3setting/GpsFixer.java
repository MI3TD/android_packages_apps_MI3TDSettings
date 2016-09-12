package net.tonyliu.mi3setting;

import android.app.Service;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

public class GpsFixer extends Service
        implements
        GpsStatus.Listener
        , GpsStatus.NmeaListener
        , LocationListener {

    private static final boolean VDEBUG = false;
    private static final String TAG = GpsFixer.class.toString();

    private static final long WORKAROUND_INTERVAL_NS = 10 * 1000 * 1000 * 1000;
    private static final String WORKAROUND_COMMAND = "force_xtra_injection";
    private static final int MSG_WORKAROUND = 1337;

    private LocationManager locationManager_;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager_ = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager_.addGpsStatusListener(this);
        locationManager_.addNmeaListener(this);
        locationManager_.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 1000, this);
    }

    @Override
    public void onDestroy() {
        locationManager_.removeUpdates(this);
        locationManager_.removeNmeaListener(this);
        locationManager_.removeGpsStatusListener(this);
        super.onDestroy();
    }

    private Handler handler_ = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int message = msg.what;
            switch (message) {
                case MSG_WORKAROUND:
                    locationManager_.sendExtraCommand(LocationManager.GPS_PROVIDER, WORKAROUND_COMMAND, null);
                    Toast.makeText(GpsFixer.this, R.string.gps_workaround_executed, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private long lastWorkaroundTime_ = 0;

    @Override
    public void onGpsStatusChanged(int event) {
        GpsStatus gpsStatus = locationManager_.getGpsStatus(null);

        int satellites = 0;
        int hasAlmanac = 0;
        int hasEphemeris = 0;
        int usedInFix = 0;
        for (GpsSatellite satellite : gpsStatus.getSatellites()) {
            ++satellites;
            if (satellite.hasAlmanac()) {
                ++hasAlmanac;
            }
            if (satellite.hasEphemeris()) {
                ++hasEphemeris;
            }
            if (satellite.usedInFix()) {
                ++usedInFix;
            }
        }
        int withoutEphemeris = satellites - hasEphemeris;
        boolean needWorkaround = (usedInFix == 0 && satellites >= 3) && (
                satellites == 4 && withoutEphemeris >= 1
                        || satellites <= 6 && withoutEphemeris >= 2
                        || hasEphemeris < 3
                        || withoutEphemeris >= 3
        );

        long n = System.nanoTime();
        if (VDEBUG) {
            Log.v(TAG, "diff" + ((n - lastWorkaroundTime_) / 1000 / 1000 / 1000)
                    + " int " + (n - lastWorkaroundTime_ > WORKAROUND_INTERVAL_NS ? ">" : "<=")
                    + (satellites >= 3 ? " >=3 " : " - ")
                    + (usedInFix == 0 ? " not fixed " : " fixed")
                    + " hasEph " + hasEphemeris + " " + (satellites - hasEphemeris >= 3)
                    + (needWorkaround ? " needfix" : " / no need")
            );
        }

        if (!needWorkaround) {
            return;
        }

        long now = System.nanoTime();
        if (now - lastWorkaroundTime_ > WORKAROUND_INTERVAL_NS) {
            lastWorkaroundTime_ = now;
            handler_.sendEmptyMessage(MSG_WORKAROUND);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
    }
}

