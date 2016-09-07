package net.tonyliu.mi3setting;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.preference.PreferenceFragment;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String PERSIST_ANR = "persist.audio.vns.mode";

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

        getFragmentManager().beginTransaction().replace(android.R.id.content, new MI3TDPreferences()).commit();
    }

    public static class MI3TDPreferences extends PreferenceFragment
            implements OnSharedPreferenceChangeListener  {

        private static final String KEY_VERSION = "version";

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
            preferences.edit()
                    .putString(PERSIST_ANR, Helper.get(PERSIST_ANR))
                    .apply();

            addPreferencesFromResource(R.xml.mi3td_preferences);

            String versionName;
            try {
                versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            }  catch (PackageManager.NameNotFoundException e) {
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

