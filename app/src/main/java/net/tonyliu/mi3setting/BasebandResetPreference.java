package net.tonyliu.mi3setting;

import android.content.Context;
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
            Helper.resetBaseband(getContext());
        }
    }
}
