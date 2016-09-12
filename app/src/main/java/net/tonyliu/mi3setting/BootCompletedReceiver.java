package net.tonyliu.mi3setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String KEY_RIL_AUTO_RESET = "ril_auto_rest";
    private static final String KEY_GPS_WORKAROUND = "gps_workaround";

    @Override
    public void onReceive(Context context, Intent intent) {
        startServices(context);
    }

    static void startServices(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPreferences.getBoolean(KEY_RIL_AUTO_RESET , true)) {
            context.startService(new Intent(context, BasebandAutoResetter.class));
        }

        if (sharedPreferences.getBoolean(KEY_GPS_WORKAROUND , true)) {
            context.startService(new Intent(context, GpsFixer.class));
        }
    }
}

