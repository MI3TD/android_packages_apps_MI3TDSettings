package net.tonyliu.mi3setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.os.BatteryManager;

public class ForceFastChargePreference extends SwitchPreference {

    private static final String PERSIST_FORCE_FAST_CHARGE = "persist.force_fast_charge";

    // copied from android.os.BatteryManager.EXTRA_MAX_CHARGING_CURRENT;
    private static final String EXTRA_MAX_CHARGING_CURRENT = "max_charging_current";

    private static final int MSG_BATTERY_UPDATE = 302;

    private Context context_;

    private class BatteryStatus {
        public final int plugged;
        public final int maxChargingCurrent;
        public BatteryStatus(int plugged, int maxChargingCurrent) {
            this.plugged = plugged;
            this.maxChargingCurrent = maxChargingCurrent;
        }
    }
    private BatteryStatus batteryStatus_;

    public ForceFastChargePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        context_ = context;

        batteryStatus_ = new BatteryStatus(BatteryManager.BATTERY_STATUS_UNKNOWN, 0);

        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(broadcastReceiver_, filter);

        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, Object newValue) {
                if (!(boolean) newValue) {
                    return true;
                }

                new AlertDialog.Builder(context_)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setChecked(true);
                                getEditor()
                                        .putBoolean(PERSIST_FORCE_FAST_CHARGE, true)
                                        .apply();
                                refreshChargingStatus();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(R.drawable.ic_warning)
                        .setTitle(R.string.force_fast_charge)
                        .setMessage(R.string.force_fast_charge_confirmmessage)
                        .create().show();
                return false;
            }
        });
    }

    public void refreshChargingStatus() {
        if (batteryStatus_.plugged == 0) {
            setSummary(context_.getString(R.string.force_fast_charge_unplugged));
        }
        else {
            setSummary(String.format(context_.getString(R.string.force_fast_charge_current), batteryStatus_.maxChargingCurrent / 1000));
        }
    }

    private final Handler handler_ = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BATTERY_UPDATE:
                    batteryStatus_ = (BatteryStatus) msg.obj;
                    refreshChargingStatus();
                    break;
            }
        }
    };

    private final BroadcastReceiver broadcastReceiver_ = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                final int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                final int maxChargingCurrent = intent.getIntExtra(EXTRA_MAX_CHARGING_CURRENT, -1);
                final Message msg = handler_.obtainMessage(MSG_BATTERY_UPDATE, new BatteryStatus(plugged, maxChargingCurrent));
                handler_.sendMessage(msg);
            }
        }
    };
}

