
package com.android.systemui.statusbar.toggles;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;;
import android.view.View;

import com.android.systemui.R;
import com.android.internal.util.carbon.SysHelpers;

public class TRDSToggle extends StatefulToggle {

    @Override
    public void init(Context c, int style) {
        super.init(c, style);
        scheduleViewUpdate();
    }

    @Override
    protected void doEnable() {
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.UI_INVERTED_MODE, 1);
        SysHelpers.restartSystemUI();
    }

    @Override
    protected void doDisable() {
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.UI_INVERTED_MODE, 0);
        SysHelpers.restartSystemUI();
    }

    @Override
    public boolean onLongClick(View v) {
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        return super.onLongClick(v);
    }

    @Override
    protected void updateView() {
        boolean enabled = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.UI_INVERTED_MODE, 0) == 1;
        setEnabledState(enabled);
        setIcon(enabled ? R.drawable.ic_qs_trds_on : R.drawable.ic_qs_trds_off);
        setLabel(enabled ? R.string.quick_settings_trds_on_label
                : R.string.quick_settings_trds_off_label);
        super.updateView();
    }

}
