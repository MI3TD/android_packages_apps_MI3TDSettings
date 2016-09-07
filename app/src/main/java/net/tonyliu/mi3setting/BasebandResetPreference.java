package net.tonyliu.mi3setting;

import android.content.Context;
import android.widget.Toast;
import android.util.AttributeSet;

import android.preference.DialogPreference;

public class BasebandResetPreference extends DialogPreference {
    private Context context_;

    public BasebandResetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        context_ = context;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            if (Helper.writeLine("/sys/class/spi_master/spi0/spi0.0/reset", "1")) {
                Toast toast = Toast.makeText(context_, context_.getString(R.string.baseband_resetting), Toast.LENGTH_LONG);
                toast.show();
            } else {
                Helper.showRootFail(context_);
            }
        }
    }
}
