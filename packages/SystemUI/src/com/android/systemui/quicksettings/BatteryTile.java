package com.android.systemui.quicksettings;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.BatteryCircleMeterView;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;
import com.android.systemui.statusbar.phone.QuickSettingsController;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback;

import java.io.IOException;

public class BatteryTile extends QuickSettingsTile implements BatteryStateChangeCallback{
    private BatteryController mController;

    private int mBatteryLevel = 0;
    private boolean mPluggedIn;
    private BatteryMeterView mBattery;
    private BatteryCircleMeterView mCircleBattery;
    private boolean mBatteryHasPercent;

    public BatteryTile(Context context, QuickSettingsController qsc, BatteryController controller) {
        super(context, qsc, R.layout.quick_settings_tile_battery); 
        
        mController = controller;

        mOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettingsActivity(Intent.ACTION_POWER_USAGE_SUMMARY);
            }
        };
        qsc.registerObservedContent(Settings.System.getUriFor(
                Settings.System.STATUS_BAR_BATTERY), this);
        qsc.registerObservedContent(Settings.System.getUriFor(
                Settings.System.STATUS_BAR_BATTERY_COLOR), this);
        qsc.registerObservedContent(Settings.System.getUriFor(
                Settings.System.STATUS_BAR_BATTERY_TEXT_COLOR), this);
        qsc.registerObservedContent(Settings.System.getUriFor(
                Settings.System.STATUS_BAR_BATTERY_TEXT_CHARGING_COLOR), this);
        qsc.registerObservedContent(Settings.System.getUriFor(
                Settings.System.STATUS_BAR_CIRCLE_BATTERY_ANIMATIONSPEED), this);
    }

    @Override
    void onPostCreate() {
        updateTile();
        mController.addStateChangedCallback(this);
        super.onPostCreate();
    }

    @Override
    public void onDestroy() {
        mController.removeStateChangedCallback(this);
        super.onDestroy();
    }

    @Override
    public void onChangeUri(ContentResolver resolver, Uri uri) {
        BatteryMeterView battery =
                (BatteryMeterView) mTile.findViewById(R.id.image);
        BatteryCircleMeterView circleBattery =
                (BatteryCircleMeterView) mTile.findViewById(R.id.circle_battery);
        if (circleBattery != null) {
            circleBattery.updateSettings();
        }
        if (battery != null) {
            battery.updateSettings();
        }
        updateResources();
    }

    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn) {
        mBatteryLevel = level;
        mPluggedIn = pluggedIn;
        updateResources();
    }

    @Override
    public void updateResources() {
        updateTile();
        super.updateResources();
    }

    private synchronized void updateTile() {
        int batteryStyle = Settings.System.getIntForUser(mContext.getContentResolver(),
            Settings.System.STATUS_BAR_BATTERY, 0, UserHandle.USER_CURRENT);
        boolean batteryHasPercent = batteryStyle == BatteryMeterView.BATTERY_STYLE_ICON_PERCENT
            || batteryStyle == BatteryMeterView.BATTERY_STYLE_PERCENT
            || batteryStyle == BatteryMeterView.BATTERY_STYLE_CIRCLE_PERCENT
            || batteryStyle == BatteryMeterView.BATTERY_STYLE_DOTTED_CIRCLE_PERCENT;
        if (mBatteryLevel == 100) {
            mLabel = mContext.getString(R.string.quick_settings_battery_charged_label);
        } else {
            if (!batteryHasPercent) {
                mLabel = mPluggedIn
                    ? mContext.getString(R.string.quick_settings_battery_charging_label,
                            mBatteryLevel)
                    : mContext.getString(R.string.status_bar_settings_battery_meter_format,
                            mBatteryLevel);
            } else {
                mLabel = mPluggedIn
                    ? mContext.getString(R.string.quick_settings_battery_charging)
                    : mContext.getString(R.string.quick_settings_battery_discharging);
            }
        }
    }

    @Override
    void updateQuickSettings() {
        TextView tv = (TextView) mTile.findViewById(R.id.text);
        if (tv != null) {
            tv.setText(mLabel);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTileTextSize);
            tv.setPadding(0, mTileTextPadding, 0, 0);
        }
    }

}
