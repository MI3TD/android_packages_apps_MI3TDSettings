package net.tonyliu.mi3setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String PERSIST_ANR = "persist.audio.vns.mode";
    private static final String PERSIST_FORCE_FAST_CHARGE = "persist.force_fast_charge";

    private MI3TDPreferences mi3TDPreferences_;
    private ForceFastChargePreference forceFastChargePreference_;

    // copied from android.os.BatteryManager.EXTRA_MAX_CHARGING_CURRENT;
    private static final String EXTRA_MAX_CHARGING_CURRENT = "max_charging_current";

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver_, batteryIntentFilter_);
    }

    @Override
    protected void onPause() {
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
                final int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                final int maxChargingCurrent = intent.getIntExtra(EXTRA_MAX_CHARGING_CURRENT, -1);

                if (forceFastChargePreference_ == null) {
                    forceFastChargePreference_ = (ForceFastChargePreference) mi3TDPreferences_.findPreference(PERSIST_FORCE_FAST_CHARGE);
                }
                forceFastChargePreference_.updateChargingStatus(plugged, maxChargingCurrent);
            }
        }
    };

    public static class MI3TDPreferences extends PreferenceFragment
            implements OnSharedPreferenceChangeListener {

        private static final String KEY_VERSION = "version";

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
    }
}

