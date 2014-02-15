package com.android.systemui.quicksettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;
import com.android.systemui.statusbar.phone.QuickSettingsController;

public class CarbonTile extends QuickSettingsTile{

    public CarbonTile(Context context, final QuickSettingsController qsc) {
        super(context, qsc);

        mOnClick = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                qsc.mBar.collapseAllPanels(true);
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setComponent(ComponentName
                        .unflattenFromString("com.carbon.fibers/.SettingsActivity"));
                intent.addCategory("android.intent.category.LAUNCHER");
                startSettingsActivity(intent);
            }
        };
    }

    @Override
    void onPostCreate() {
        updateTile();
        super.onPostCreate();
    }

    @Override
    public void updateResources() {
        updateTile();
        super.updateResources();
    }

    private synchronized void updateTile() {
        mDrawable = R.drawable.ic_qs_carbon;
        mLabel = mContext.getString(R.string.quick_settings_carbon_label);
    }
}
