
package com.android.systemui.statusbar.toggles;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.android.systemui.R;

public class PowerMenuToggle extends BaseToggle {

    @Override
    protected void init(Context c, int style) {
        super.init(c, style);
        setIcon(R.drawable.ic_qs_powermenu);
        setLabel(R.string.quick_settings_powermenu_label);
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent(Intent.ACTION_POWERMENU);
        mContext.sendBroadcast(intent);

        collapseStatusBar();
        dismissKeyguard();
    }

    @Override
    public boolean onLongClick(View v) {
        Intent intent=new Intent(Intent.ACTION_POWERMENU_REBOOT);
        mContext.sendBroadcast(intent);

        collapseStatusBar();
        dismissKeyguard();
        return super.onLongClick(v);
    }

}
