package net.tonyliu.mi3setting;

import android.content.Context;
import android.widget.Toast;
import android.util.AttributeSet;

import android.preference.DialogPreference;

public class BasebandResetPreference extends DialogPreference {

    public BasebandResetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            if (Helper.writeLine("/sys/class/spi_master/spi0/spi0.0/reset", "1")) {
                Toast toast = Toast.makeText(getContext(), getContext().getString(R.string.baseband_resetting), Toast.LENGTH_LONG);
                toast.show();
            } else {
                Helper.showRootFail(getContext());
            }
        }
    }
}
