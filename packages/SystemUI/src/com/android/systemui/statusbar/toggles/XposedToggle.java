
package com.android.systemui.statusbar.toggles;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.android.systemui.R;

public class XposedToggle extends BaseToggle {

    @Override
    protected void init(Context c, int style) {
        super.init(c, style);
        setIcon(R.drawable.ic_qs_xposed);
        setLabel(R.string.quick_settings_xposed_label);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClassName("com.android.settings", "com.android.settings.Settings$XposedSettingsActivity");
        intent.addCategory("android.intent.category.LAUNCHER");

        collapseStatusBar();
        dismissKeyguard();
        startActivity(intent);
    }

}
