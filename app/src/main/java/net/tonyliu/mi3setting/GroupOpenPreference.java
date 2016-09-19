package net.tonyliu.mi3setting;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

public class GroupOpenPreference extends Preference {

    public GroupOpenPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
        Helper helper = new Helper(getContext());
        helper.joinQQGroup(getContext().getResources().getString(R.string.link_qqgroup_open));
    }

}
