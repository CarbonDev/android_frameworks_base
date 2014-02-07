package com.android.systemui.quicksettings;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;
import com.android.systemui.statusbar.phone.QuickSettingsController;

public class BatterySaverTile extends QuickSettingsTile{
    private boolean mEnabled = false;

    public BatterySaverTile(Context context, final QuickSettingsController qsc) {
        super(context, qsc);

        mOnClick = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean checkModeOn = Settings.Global.getInt(mContext.getContentResolver(),
                                        Settings.Global.BATTERY_SAVER_OPTION, 0) == 1;
                Settings.Global.putInt(mContext.getContentResolver(),
                                        Settings.Global.BATTERY_SAVER_OPTION, checkModeOn ? 0 : 1);
                Settings.Global.putInt(mContext.getContentResolver(),
                                        Settings.Global.BATTERY_SAVER_OPTION, mEnabled ? 0 : 1);
                Intent scheduleSaver = new Intent();
                scheduleSaver.setAction(Intent.ACTION_BATTERY_SERVICES);
                mContext.sendBroadcast(scheduleSaver);
                //updateResources();
                if (isFlipTilesEnabled()) {
                    flipTile(0);
                }
            }
        };

        mOnLongClick = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //startSettingsActivity(Intent.ACTION_POWER_USAGE_SUMMARY);
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.android.settings",
                    "com.android.settings.Settings$BatterySaverSettingsConfigActivity");
                startSettingsActivity(intent);
                return true;
            }
        };

        qsc.registerObservedContent(Settings.Global.getUriFor(Settings.Global.BATTERY_SAVER_OPTION), this);
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
        mEnabled = Settings.Global.getInt(mContext.getContentResolver(),
                     Settings.Global.BATTERY_SAVER_OPTION, 0) == 1;
        if (mEnabled) {
            mDrawable = R.drawable.ic_qs_battery_saver_on;
            mLabel = mContext.getString(R.string.quick_settings_battery_saver_label);
        } else {
            mDrawable = R.drawable.ic_qs_battery_saver_off;
            mLabel = mContext.getString(R.string.quick_settings_battery_saver_off_label);
        }
    }

    @Override
    public void onChangeUri(ContentResolver resolver, Uri uri) {
        updateResources();
    }
}
