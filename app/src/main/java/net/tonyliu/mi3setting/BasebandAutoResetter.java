package net.tonyliu.mi3setting;

import android.annotation.Nullable;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;


public class BasebandAutoResetter extends Service {

    private static final int RESET_DELAY = 120;

    private final Handler handler_ = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler_.postDelayed(new Runnable() {
            @Override
            public void run() {
                BasebandAutoResetter self = BasebandAutoResetter.this;

                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                switch (telephonyManager.getSimState()) {
                    case TelephonyManager.SIM_STATE_UNKNOWN:
                    case TelephonyManager.SIM_STATE_ABSENT:
                        new Helper(self).resetBaseband();
                }
                self.stopSelf();
            }
        }, RESET_DELAY * 1000);
        return super.onStartCommand(intent, flags, startId);
    }
}
