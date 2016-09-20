package net.tonyliu.mi3setting;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String PERSIST_ANR = "persist.audio.vns.mode";
    private static final String PERSIST_FORCE_FAST_CHARGE = "persist.force_fast_charge";
    private static final String KEY_GPS_WORKAROUND = "gps_workaround";

    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS
    };
    private static final int LOCATION_REQUEST = 1337;

    private MI3TDPreferences mi3TDPreferences_;
    private ForceFastChargePreference forceFastChargePreference_;

    public MainActivity() {
        super();

        switch (Helper.get(PERSIST_ANR)) {
            case "1":
            case "2":
                break;
            default:
                try {
                    Helper.set(PERSIST_ANR, "2");
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mi3TDPreferences_ = new MI3TDPreferences();
        getFragmentManager().beginTransaction().replace(android.R.id.content, mi3TDPreferences_).commit();
        // TEST
        // startService(new Intent(this, BasebandAutoResetter.class));
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver_, batteryIntentFilter_);
        updateChargingStatusHandler_.postDelayed(updateChargingStatus_, 0);
    }

    @Override
    protected void onPause() {
        updateChargingStatusHandler_.removeCallbacks(updateChargingStatus_);
        unregisterReceiver(broadcastReceiver_);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        forceFastChargePreference_ = null;
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final IntentFilter batteryIntentFilter_ = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

    private final BroadcastReceiver broadcastReceiver_ = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                if (forceFastChargePreference_ == null) {
                    forceFastChargePreference_ = (ForceFastChargePreference) mi3TDPreferences_.findPreference(PERSIST_FORCE_FAST_CHARGE);
                }
                forceFastChargePreference_.updateChargingStatus(intent);

                updateChargingStatusHandler_.removeCallbacks(updateChargingStatus_);
                updateChargingStatusHandler_.postDelayed(updateChargingStatus_, 2000);
            }
        }
    };

    private final Handler updateChargingStatusHandler_ = new Handler();
    private final Runnable updateChargingStatus_ = new Runnable(){
        public void run() {
            try {
                if (forceFastChargePreference_ != null) {
                    forceFastChargePreference_.updateChargingStatus();
                }
                updateChargingStatusHandler_.postDelayed(updateChargingStatus_, 1000);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public static class MI3TDPreferences extends PreferenceFragment
            implements OnSharedPreferenceChangeListener {

        private static final String KEY_VERSION = "version";

        Context context_;

        @Override
        public void onAttach(Activity activity) {
            context_ = activity.getApplicationContext();
            super.onAttach(activity);
        }

        @Override
        public void onAttach(Context context) {
            context_ = context;
            super.onAttach(context);
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
            preferences.edit()
                    .putString(PERSIST_ANR, Helper.get(PERSIST_ANR).equals("1") ? "1" : "2")
                    .putBoolean(PERSIST_FORCE_FAST_CHARGE, Helper.get(PERSIST_FORCE_FAST_CHARGE, false))
                    .apply();

            addPreferencesFromResource(R.xml.mi3td_preferences);

            String versionName;
            try {
                versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                versionName = "Error";
            }
            findPreference(KEY_VERSION).setSummary(versionName);

            if (preferences.getBoolean(KEY_GPS_WORKAROUND, true)) {
                startGpsFixService();
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            switch (s) {
                case PERSIST_ANR:
                    String value = sharedPreferences.getString(s, "2");
                    try {
                        Helper.set(s, value);
                    } catch (IOException ignored) {
                        ignored.printStackTrace();
                    }
                    break;

                case PERSIST_FORCE_FAST_CHARGE:
                    boolean forceFastCharge = sharedPreferences.getBoolean(s, false);
                    try {
                        Helper.set(s, forceFastCharge ? "1" : "0");
                    } catch (IOException ignored) {
                        ignored.printStackTrace();
                    }
                    break;

                case KEY_GPS_WORKAROUND:
                    boolean gpsWorkaround = sharedPreferences.getBoolean(s, false);
                    if (gpsWorkaround) {
                        startGpsFixService();
                    } else {
                        context_.stopService(new Intent(context_, GpsFixer.class));
                    }
                    break;
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        private void startGpsFixService() {
            if (Helper.hasPermission(context_, LOCATION_PERMS)) {
                context_.startService(new Intent(context_, GpsFixer.class));
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            switch (requestCode) {
                case LOCATION_REQUEST:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        context_.startService(new Intent(context_, GpsFixer.class));
                    }
                    else {
                        ((SwitchPreference) findPreference(KEY_GPS_WORKAROUND)).setChecked(false);
                    }
                    break;
            }
        }
    }
}

