package net.tonyliu.mi3setting;

import android.content.Context;
import android.util.AttributeSet;

import android.preference.DialogPreference;
import android.view.View;
import android.view.ViewGroup;

public class SystemSwitchPreference extends DialogPreference {

    private final Helper helper_;

    public SystemSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        helper_ = new Helper(context);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        setSummary(getContext().getString(R.string.systemswitch_summary, Helper.getCurrentSystem()));
        return super.onCreateView(parent);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            helper_.switchSystem();
        }
    }
}
