/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.policy;

import java.util.ArrayList;

import android.bluetooth.BluetoothAdapter.BluetoothStateChangeCallback;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.CharacterStyle;
import android.os.BatteryManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.R;

public class SbBatteryController extends LinearLayout {
    private static final String TAG = "StatusBar.BatteryController";

    private Context mContext;
    private ArrayList<ImageView> mIconViews = new ArrayList<ImageView>();
    private ArrayList<TextView> mLabelViews = new ArrayList<TextView>();

    private ArrayList<BatteryStateChangeCallback> mChangeCallbacks =
            new ArrayList<BatteryStateChangeCallback>();

    public interface BatteryStateChangeCallback {
        public void onBatteryLevelChanged(int level, boolean pluggedIn);
    }

    private ImageView mBatteryIcon;
    private TextView mBatteryText;
    private TextView mBatteryTextCM;
    private TextView mBatteryCenterText;
    private ViewGroup mBatteryGroup;
    private TextView mBatteryTextOnly;
    private TextView mBatteryTextOnly_Low;
    private TextView mBatteryTextOnly_Plugged;

    private static int mBatteryStyle;

    private int mLevel = -1;
    private boolean mPlugged = false;

    private boolean customColor;
    protected int mBatteryTextColor = com.android.internal.R.color.holo_blue_dark;
    private int color = 0;

    public static final int STYLE_ICON_ONLY = 0;
    public static final int STYLE_TEXT_ONLY = 1;
    public static final int STYLE_ICON_TEXT = 2;
    public static final int STYLE_ICON_CENTERED_TEXT = 3;
    public static final int STYLE_ICON_CIRCLE = 4;
    public static final int STYLE_ICON_CIRCLE_PERCENT = 5;
    public static final int STYLE_ICON_DOTTED_CIRCLE_PERCENT = 6;
    public static final int STYLE_ICON_SPEED = 7;
    public static final int STYLE_ICON_SQUARE = 8;
    public static final int STYLE_ICON_GEAR = 9;
    public static final int STYLE_ICON_CM = 10;
    public static final int STYLE_ICON_CARBON = 11;
    public static final int STYLE_HIDE = 12;

    public SbBatteryController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
        mBatteryGroup = (ViewGroup) findViewById(R.id.battery_combo);
        mBatteryIcon = (ImageView) findViewById(R.id.battery);
        mBatteryText = (TextView) findViewById(R.id.battery_text);
        mBatteryTextCM = (TextView) findViewById(R.id.battery_text_cm);
        mBatteryCenterText = (TextView) findViewById(R.id.battery_text_center);
        mBatteryTextOnly = (TextView) findViewById(R.id.battery_text_only);
        mBatteryTextOnly_Low = (TextView) findViewById(R.id.battery_text_only_low);
        mBatteryTextOnly_Plugged = (TextView) findViewById(R.id.battery_text_only_plugged);
        addIconView(mBatteryIcon);

        SettingsObserver settingsObserver = new SettingsObserver(new Handler());
        settingsObserver.observe();
        updateSettings(); // to initialize values
    }

    private void init() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(mBatteryBroadcastReceiver, filter);
    }

    public void addIconView(ImageView v) {
        mIconViews.add(v);
    }

    public void addLabelView(TextView v) {
        mLabelViews.add(v);
    }

    public void addStateChangedCallback(BatteryStateChangeCallback cb) {
        mChangeCallbacks.add(cb);
    }

    private BroadcastReceiver mBatteryBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                final int level = intent.getIntExtra(
                        BatteryManager.EXTRA_LEVEL, 0);
                final boolean plugged = intent.getIntExtra(
                        BatteryManager.EXTRA_PLUGGED, 0) != 0;
                setBatteryIcon(level, plugged);
            }
        }
    };

    private void setBatteryIcon(int level, boolean plugged) {
        mLevel = level;
        mPlugged = plugged;
        ContentResolver cr = mContext.getContentResolver();
        mBatteryStyle = Settings.System.getInt(cr,
                Settings.System.STATUSBAR_BATTERY_ICON, 0);
        int icon;
        switch (mBatteryStyle) {
            case STYLE_ICON_SPEED:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_altcircle
                 : R.drawable.stat_sys_battery_altcircle;
                 break;
            case STYLE_ICON_SQUARE:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_square
                 : R.drawable.stat_sys_battery_square;
                 break;
            case STYLE_ICON_GEAR:
                 icon = plugged ? R.drawable.stat_sys_battery_gear_charge
                 : R.drawable.stat_sys_battery_gear;
                 break;
            case STYLE_ICON_CM:
                icon = plugged ? R.drawable.stat_sys_battery_charge_min
                : R.drawable.stat_sys_battery_min;
                break;
            case STYLE_ICON_CARBON:
                icon = plugged ? R.drawable.stat_sys_battery_carbon_charge
                : R.drawable.stat_sys_battery_carbon;
                break;
            default:
                 icon = plugged ? R.drawable.stat_sys_battery_charge
                 : R.drawable.stat_sys_battery;
                 break;
        }
        int N = mIconViews.size();
        for (int i = 0; i < N; i++) {
            ImageView v = mIconViews.get(i);
            Drawable batteryBitmap = mContext.getResources().getDrawable(icon);
            if (customColor) {
                batteryBitmap.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            } else {
                batteryBitmap.clearColorFilter();
            }
            v.setImageDrawable(batteryBitmap);
            v.setImageLevel(level);
            v.setContentDescription(mContext.getString(
                    R.string.accessibility_battery_level, level));
        }
        N = mLabelViews.size();
        for (int i = 0; i < N; i++) {
            TextView v = mLabelViews.get(i);
            v.setText(mContext.getString(
                    R.string.status_bar_settings_battery_meter_format, level));
        }

        // do my stuff here
        if (mBatteryGroup != null) {
            mBatteryText.setText(Integer.toString(level));
            mBatteryTextCM.setText(Integer.toString(level));
            mBatteryCenterText.setText(Integer.toString(level));
            SpannableStringBuilder formatted = new SpannableStringBuilder(
                    Integer.toString(level) + "%");
            CharacterStyle style = new RelativeSizeSpan(0.7f); // beautiful
                                                               // formatting
            if (level < 10) { // level < 10, 2nd char is %
                formatted.setSpan(style, 1, 2,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            } else if (level < 100) { // level 10-99, 3rd char is %
                formatted.setSpan(style, 2, 3,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            } else { // level 100, 4th char is %
                formatted.setSpan(style, 3, 4,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
            mBatteryTextOnly.setText(formatted);
            mBatteryTextOnly_Low.setText(formatted);
            mBatteryTextOnly_Plugged.setText(formatted);
            if (mBatteryStyle == STYLE_TEXT_ONLY) {
                if (plugged) {
                    mBatteryTextOnly.setVisibility(View.GONE);
                    mBatteryTextOnly_Plugged.setVisibility(View.VISIBLE);
                    mBatteryTextOnly_Low.setVisibility(View.GONE);
                } else if (level < 16) {
                    mBatteryTextOnly.setVisibility(View.GONE);
                    mBatteryTextOnly_Plugged.setVisibility(View.GONE);
                    mBatteryTextOnly_Low.setVisibility(View.VISIBLE);
                } else {
                    mBatteryTextOnly.setVisibility(View.VISIBLE);
                    mBatteryTextOnly_Plugged.setVisibility(View.GONE);
                    mBatteryTextOnly_Low.setVisibility(View.GONE);
                }
            } else {
                mBatteryTextOnly.setVisibility(View.GONE);
                mBatteryTextOnly_Plugged.setVisibility(View.GONE);
                mBatteryTextOnly_Low.setVisibility(View.GONE);
            }

        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System
                    .getUriFor(Settings.System.STATUSBAR_BATTERY_ICON), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_ICON_COLOR), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.ICON_COLOR_BEHAVIOR), false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSettings();
        }
    }

    private void updateSettings() {
        // Slog.i(TAG, "updated settings values");
        ContentResolver cr = mContext.getContentResolver();
        mBatteryStyle = Settings.System.getInt(cr,
                Settings.System.STATUSBAR_BATTERY_ICON, 0);
        color = Settings.System.getInt(cr,
                Settings.System.STATUS_ICON_COLOR, 0);
        customColor = Settings.System.getInt(cr,
                Settings.System.ICON_COLOR_BEHAVIOR, 0) == 1;

        int defaultColor = getResources().getColor(com.android.internal.R.color.holo_blue_dark);

        if (customColor) {
            mBatteryTextColor = Settings.System.getInt(cr,
                    Settings.System.STATUS_ICON_COLOR, defaultColor);
            if (mBatteryTextColor == Integer.MIN_VALUE) {
                // flag to reset the color
                mBatteryTextColor = defaultColor;
            }
            mBatteryTextOnly.setTextColor(mBatteryTextColor);
            mBatteryTextCM.setTextColor(mBatteryTextColor);
        } else {
            mBatteryTextColor = Settings.System.getInt(cr,
                    Settings.System.STATUS_BAR_BATTERY_TEXT_COLOR, -2);
            if (mBatteryTextColor == Integer.MIN_VALUE
                    || mBatteryTextColor == -2) {
                // flag to reset the color
                mBatteryTextColor = defaultColor;
            }
            mBatteryTextOnly.setTextColor(mBatteryTextColor);
            mBatteryTextCM.setTextColor(mBatteryTextColor);
        }

        switch (mBatteryStyle) {
            case STYLE_ICON_ONLY:
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryTextCM.setVisibility(View.GONE);
                mBatteryText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_TEXT_ONLY:
                mBatteryText.setVisibility(View.GONE);
                mBatteryTextCM.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.GONE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_TEXT:
                mBatteryText.setVisibility(View.VISIBLE);
                mBatteryTextCM.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_CENTERED_TEXT:
                mBatteryText.setVisibility(View.GONE);
                mBatteryTextCM.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.VISIBLE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_HIDE:
                mBatteryText.setVisibility(View.GONE);
                mBatteryTextCM.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.GONE);
                setVisibility(View.GONE);
                break;
            case STYLE_ICON_CIRCLE:
                mBatteryText.setVisibility(View.GONE);
                mBatteryTextCM.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.GONE);
                setVisibility(View.VISIBLE);
                break;
			case STYLE_ICON_CIRCLE_PERCENT:
                mBatteryText.setVisibility(View.GONE);
                mBatteryTextCM.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.GONE);
                setVisibility(View.VISIBLE);
                break;
			case STYLE_ICON_DOTTED_CIRCLE_PERCENT:
                mBatteryText.setVisibility(View.GONE);
                mBatteryTextCM.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.GONE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_SPEED:
                mBatteryText.setVisibility(View.GONE);
                mBatteryTextCM.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_SQUARE:
                mBatteryText.setVisibility(View.GONE);
                mBatteryTextCM.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_GEAR:
                mBatteryText.setVisibility(View.GONE);
                mBatteryTextCM.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_CM:
                mBatteryText.setVisibility(View.GONE);
                mBatteryTextCM.setVisibility(View.VISIBLE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_CARBON:
                mBatteryText.setVisibility(View.GONE);
                mBatteryTextCM.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            default:
                mBatteryText.setVisibility(View.GONE);
                mBatteryTextCM.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
        }

        setBatteryIcon(mLevel, mPlugged);

    }
}
