package net.tonyliu.mi3setting;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.preference.PreferenceFragment;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager_;

    public MainActivity() {
        super();

        switch (Helper.get("persist.audio.vns.mode")) {
            case "1":
            case "2":
                break;
            default:
                try {
                    Helper.set("persist.audio.vns.mode", "2");
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

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
            preferences.edit()
                    .putString("persist.audio.vns.mode", Helper.get("persist.audio.vns.mode"))
                    .apply();

            addPreferencesFromResource(R.xml.mi3td_preferences);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            switch (s) {
                case "persist.audio.vns.mode":
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

