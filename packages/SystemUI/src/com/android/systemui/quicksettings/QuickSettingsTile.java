package com.android.systemui.quicksettings;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.Animator.AnimatorListener;
import android.app.ActivityManagerNative;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.R;
import java.util.Random;

import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.phone.QuickSettingsController;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;
import com.android.systemui.statusbar.phone.QuickSettingsTileView;

public class QuickSettingsTile implements OnClickListener {

    protected final Context mContext;
    protected QuickSettingsContainerView mContainer;
    protected QuickSettingsTileView mTile;
    protected OnClickListener mOnClick;
    protected OnLongClickListener mOnLongClick;
    protected final int mTileLayout;
    protected int mDrawable;
    protected String mLabel;
    protected BaseStatusBar mStatusbarService;
    protected QuickSettingsController mQsc;
    protected int mTileTextSize;
    protected int mTileTextColor;
    protected SharedPreferences mPrefs;

    private static final int DEFAULT_QUICK_TILES_BG_COLOR = 0xff161616;
    private static final int DEFAULT_QUICK_TILES_BG_PRESSED_COLOR = 0xff212121;

    private Handler mHandler = new Handler();

    public QuickSettingsTile(Context context, QuickSettingsController qsc) {
        this(context, qsc, R.layout.quick_settings_tile_basic);
    }

    public QuickSettingsTile(Context context, QuickSettingsController qsc, int layout) {
        mContext = context;
        mDrawable = R.drawable.ic_notifications;
        mLabel = mContext.getString(R.string.quick_settings_label_enabled);
        mStatusbarService = qsc.mStatusBarService;
        mQsc = qsc;
        mTileLayout = layout;
        setTextSize(mQsc.getTileTextSize());
        setTextColor(mQsc.getTileTextColor());
        mPrefs = mContext.getSharedPreferences("quicksettings", Context.MODE_PRIVATE);
    }

    public void setupQuickSettingsTile(LayoutInflater inflater,
            QuickSettingsContainerView container) {
        mTile = (QuickSettingsTileView) inflater.inflate(
                R.layout.quick_settings_tile, container, false);
        mTile.setContent(mTileLayout, inflater);
        mContainer = container;
        mContainer.addView(mTile);
        onPostCreate();
        updateQuickSettings();
        mTile.setOnClickListener(this);
        mTile.setOnLongClickListener(mOnLongClick);
        setColor();
        setRandomColor();
    }

    public final void setTextSize(int size) {
        mTileTextSize = size;
    }

    public final void setTextColor(int color) {
        mTileTextColor = color;
    }

    public void setLabelVisibility(boolean visible) {
        TextView tv = (TextView) mTile.findViewById(R.id.text);
        if (tv != null) {
            tv.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        View sepPadding = mTile.findViewById(R.id.separator_padding);
        if (sepPadding != null) {
            sepPadding.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    void onPostCreate() {}

    public void onDestroy() {}

    public void onReceive(Context context, Intent intent) {}

    public void onChangeUri(ContentResolver resolver, Uri uri) {}

    public void updateResources() {
        if(mTile != null) {
            updateQuickSettings();
        }
    }

    void updateQuickSettings() {
        TextView tv = (TextView) mTile.findViewById(R.id.text);
        if (tv != null) {
            tv.setText(mLabel);
            tv.setTextSize(1, mTileTextSize);
            if (mTileTextColor != -2) {
                tv.setTextColor(mTileTextColor);
            }
        }
        ImageView image = (ImageView) mTile.findViewById(R.id.image);
        if (image != null) {
            image.setImageResource(mDrawable);
        }
    }

    public boolean isFlipTilesEnabled() {
        return (Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QUICK_SETTINGS_TILES_FLIP, 1) == 1);
    }

    public boolean isRandom() {
        return (Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QUICK_TILES_BG_COLOR_RANDOM, 0) == 1);
    }

    public void flipTile(int delay){
        final AnimatorSet anim = (AnimatorSet) AnimatorInflater.loadAnimator(
                mContext, R.anim.flip_right);
        anim.setTarget(mTile);
        anim.setDuration(200);
        anim.addListener(new AnimatorListener(){

            @Override
            public void onAnimationEnd(Animator animation) {
                setRandomColor();
            }
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        Runnable doAnimation = new Runnable(){
            @Override
            public void run() {
                anim.start();
            }
        };

        mHandler.postDelayed(doAnimation, delay);
    }

    void startSettingsActivity(String action) {
        Intent intent = new Intent(action);
        startSettingsActivity(intent);
    }

    void startSettingsActivity(Intent intent) {
        startSettingsActivity(intent, true);
    }

    private void startSettingsActivity(Intent intent, boolean onlyProvisioned) {
        if (onlyProvisioned && !mStatusbarService.isDeviceProvisioned()) return;
        try {
            // Dismiss the lock screen when Settings starts.
            ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
        } catch (RemoteException e) {
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ContentResolver resolver = mContext.getContentResolver();
        boolean floatingWindow = Settings.System.getBoolean(resolver, Settings.System.QS_FLOATING_WINDOW, false) == true;
        if (floatingWindow) {
            intent.addFlags(Intent.FLAG_FLOATING_WINDOW);
        }
        mContext.startActivityAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
        mStatusbarService.collapse();
    }

    @Override
    public void onClick(View v) {
        if (mOnClick != null) {
            mOnClick.onClick(v);
        }

        ContentResolver resolver = mContext.getContentResolver();
        boolean shouldCollapse = Settings.System.getInt(resolver, Settings.System.QS_COLLAPSE_PANEL, 0) == 1;
        if (shouldCollapse) {
            mQsc.mBar.collapseAllPanels(true);
        }
        if (isRandom()) {
            setRandomColor();
        }
    }

    private void setColor() {

        int bgColor = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QUICK_TILES_BG_COLOR, -2);
        int presColor = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QUICK_TILES_BG_PRESSED_COLOR, -2);

        if (bgColor != -2 || presColor != -2) {
            if (bgColor == -2 && !isRandom()) {
                bgColor = DEFAULT_QUICK_TILES_BG_COLOR;
            }
            if (presColor == -2) {
                presColor = DEFAULT_QUICK_TILES_BG_PRESSED_COLOR;
            }
            ColorDrawable bgDrawable = new ColorDrawable(bgColor);
            ColorDrawable presDrawable = new ColorDrawable(presColor);
            StateListDrawable states = new StateListDrawable();
            states.addState(new int[] {android.R.attr.state_pressed}, presDrawable);
            states.addState(new int[] {}, bgDrawable);
            mTile.setBackground(states);
        }
    }

    public void setRandomColor() {

        ContentResolver cr = mContext.getContentResolver();
        int blueDark = Settings.System.getInt(cr,
                Settings.System.RANDOM_COLOR_ONE, android.R.color.holo_blue_dark);
        int greenDark = Settings.System.getInt(cr,
                Settings.System.RANDOM_COLOR_TWO, android.R.color.holo_green_dark);
        int redDark = Settings.System.getInt(cr,
                Settings.System.RANDOM_COLOR_THREE, android.R.color.holo_red_dark);
        int orangeDark = Settings.System.getInt(cr,
                Settings.System.RANDOM_COLOR_FOUR, android.R.color.holo_orange_dark);
        int purple = Settings.System.getInt(cr,
                Settings.System.RANDOM_COLOR_FIVE, android.R.color.holo_purple);
        int blueBright = Settings.System.getInt(cr,
                Settings.System.RANDOM_COLOR_SIX, android.R.color.holo_blue_bright);
        int presColor = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QUICK_TILES_BG_PRESSED_COLOR, -2);

        if(isRandom()) {
            int[] Colors = new int[] {
                blueDark,
                greenDark,
                redDark,		
                orangeDark,
                purple,
                blueBright
            };
            if (presColor == -2) {
                presColor = DEFAULT_QUICK_TILES_BG_PRESSED_COLOR;
            }
            Random generator = new Random();
            ColorDrawable bgDrawable = new ColorDrawable(Colors[generator.nextInt(Colors.length)]);
            ColorDrawable presDrawable = new ColorDrawable(presColor);		
            StateListDrawable states = new StateListDrawable();		
            states.addState(new int[] {android.R.attr.state_pressed}, presDrawable);		
            states.addState(new int[] {}, bgDrawable);
            mTile.setBackground(states);
        } else {
            setColor();
        }
    }
}
