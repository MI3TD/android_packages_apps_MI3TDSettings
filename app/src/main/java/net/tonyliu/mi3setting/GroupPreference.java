package net.tonyliu.mi3setting;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;

public class GroupPreference extends Preference {

    private final String group_;

    public GroupPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GroupPreference, 0, 0);
        try {
            group_ = a.getString(R.styleable.AlipayDonate_code);

            if (group_ == null) {
                throw new IllegalArgumentException("GroupPreference: error - code is not specified");
            }
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onClick() {
        Helper helper = new Helper(getContext());
        helper.joinQQGroup(group_);
    }

}
