/*
 * Copyright (C) 2012 The Android Open Source Project
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
package com.android.internal.policy.impl.keyguard;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;

import android.animation.ObjectAnimator;
import android.app.SearchManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

import static com.android.internal.util.carbon.AwesomeConstants.*;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.util.carbon.AokpRibbonHelper;
import com.android.internal.util.carbon.GlowPadTorchHelper;
import com.android.internal.view.RotationPolicy;
import com.android.internal.util.carbon.LockscreenTargetUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.multiwaveview.GlowPadView;
import com.android.internal.widget.multiwaveview.GlowPadView.OnTriggerListener;
import com.android.internal.R;
import com.android.internal.widget.multiwaveview.TargetDrawable;

public class KeyguardSelectorView extends LinearLayout implements KeyguardSecurityView {
    private static final boolean DEBUG = KeyguardHostView.DEBUG;
    private static final String TAG = "SecuritySelectorView";
    private static final String ASSIST_ICON_METADATA_NAME =
        "com.android.systemui.action_assist_icon";

    private KeyguardSecurityCallback mCallback;
    private GlowPadView mGlowPadView;
    private LinearLayout mRibbon;
    private LinearLayout ribbonView;
    private ObjectAnimator mAnim;
    private View mFadeView;
    private boolean mIsBouncing;
    private boolean mCameraDisabled;
    private boolean mSearchDisabled;
    private boolean mUnlockBroadcasted = false;
    private LockPatternUtils mLockPatternUtils;
    private SecurityMessageDisplay mSecurityMessageDisplay;
    private Drawable mBouncerFrame;
    private String[] mStoredTargets;
    private int mTargetOffset;
    private boolean mIsScreenLarge;
    private UnlockReceiver mUnlockReceiver;
    private int mGlowTorch;
    private boolean mUserRotation;
    private boolean mGlowTorchOn;
    private boolean mGlowPadLock;
    private boolean mLongPress = false;
    private float mBatteryLevel;

    private IntentFilter filter;
    private boolean mReceiverRegistered = false;

    private class H extends Handler {
        public void handleMessage(Message m) {
            switch (m.what) {
            }
        }
    }
    private H mHandler = new H();

    OnTriggerListener mOnTriggerListener = new OnTriggerListener() {

       final Runnable SetLongPress = new Runnable () {
            public void run() {
                if (!mLongPress) {
                    GlowPadTorchHelper.vibrate(mContext);
                    mLongPress = true;
                }
            }
        };

        public void onTrigger(View v, int target) {
            if (mReceiverRegistered) {
                mContext.unregisterReceiver(mUnlockReceiver);
                mReceiverRegistered = false;
            }
            if (!mLongPress) {
                    mHandler.removeCallbacks(SetLongPress);
            }
            if (mStoredTargets == null) {
                final int resId = mGlowPadView.getResourceIdForTarget(target);
                switch (resId) {
                case com.android.internal.R.drawable.ic_action_assist_generic:
                    Intent assistIntent =
                    ((SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE))
                    .getAssistIntent(mContext, true, UserHandle.USER_CURRENT);
                    if (assistIntent != null) {
                        mActivityLauncher.launchActivity(assistIntent, false, true, null, null);
                    } else {
                        Log.w(TAG, "Failed to get intent for assist activity");
                    }
                    mCallback.userActivity(0);
                    break;

                case com.android.internal.R.drawable.ic_lockscreen_camera:
                    mActivityLauncher.launchCamera(null, null);
                    mCallback.userActivity(0);
                    mCallback.dismiss(false);
                    break;

                case com.android.internal.R.drawable.ic_lockscreen_unlock_phantom:
                case com.android.internal.R.drawable.ic_lockscreen_unlock:
                    mCallback.userActivity(0);
                    mCallback.dismiss(false);
                    break;
                }
            } else {
                if (target == mTargetOffset) {
                    mCallback.dismiss(false);
                } else {
                    int realTarget = target - mTargetOffset - 1;
                    String targetUri = realTarget < mStoredTargets.length
                            ? mStoredTargets[realTarget] : null;

                    if (GlowPadView.EMPTY_TARGET.equals(targetUri)) {
                        mCallback.dismiss(false);
                    } else {
                        try {
                            Intent intent = Intent.parseUri(targetUri, 0);
                            mActivityLauncher.launchActivity(intent, false, true, null, null);
                        } catch (URISyntaxException e) {
                            Log.w(TAG, "Invalid lockscreen target " + targetUri);
                        }
                    }
                }
            }
        }

        public void onReleased(View v, int handle) {
            fireTorch();
            if (!mIsBouncing) {
                doTransition(mFadeView, 1.0f);
            }
            if (!mGlowPadLock && mLongPress) {
                mGlowPadLock = true;
                if (mReceiverRegistered) {
                    mContext.unregisterReceiver(mUnlockReceiver);
                    mReceiverRegistered = false;
                }
            }
        }

        public void onGrabbed(View v, int handle) {
            mCallback.userActivity(0);
            doTransition(mFadeView, 0.0f);
            if (mGlowTorch == 1) {
                mHandler.removeCallbacks(checkTorch);
                mHandler.postDelayed(startTorch, GlowPadTorchHelper.TORCH_TIMEOUT);
            }
        }

        public void onGrabbedStateChange(View v, int handle) {
            mHandler.removeCallbacks(SetLongPress);
            mLongPress = false;
        }

        public void onTargetChange(View v, int target) {
            if (target == -1) {
                mHandler.removeCallbacks(SetLongPress);
                mLongPress = false;
                if (mGlowTorchOn) {
                    mCallback.userActivity(0);
                }
            } else {
                fireTorch();
                mHandler.postDelayed(SetLongPress, ViewConfiguration.getLongPressTimeout());
            }
        }

        public void onFinishFinalAnimation() {

        }

    };

    KeyguardUpdateMonitorCallback mInfoCallback = new KeyguardUpdateMonitorCallback() {

        @Override
        public void onDevicePolicyManagerStateChanged() {
            updateTargets();
        }

        @Override
        public void onSimStateChanged(State simState) {
            updateTargets();
        }

        @Override
        public void onRefreshBatteryInfo(KeyguardUpdateMonitor.BatteryStatus batStatus) {
            updateLockscreenBattery(batStatus);
        }
    };

    private final KeyguardActivityLauncher mActivityLauncher = new KeyguardActivityLauncher() {

        @Override
        KeyguardSecurityCallback getCallback() {
            return mCallback;
        }

        @Override
        LockPatternUtils getLockPatternUtils() {
            return mLockPatternUtils;
        }

        @Override
        protected void dismissKeyguardOnNextActivity() {
            getCallback().dismiss(false);
        }

        @Override
        Context getContext() {
            return mContext;
        }
    };

    public KeyguardSelectorView(Context context) {
        this(context, null);
    }

    public KeyguardSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLockPatternUtils = new LockPatternUtils(getContext());
        mTargetOffset = LockscreenTargetUtils.getTargetOffset(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ContentResolver cr = mContext.getContentResolver();
        mGlowPadView = (GlowPadView) findViewById(R.id.glow_pad_view);
        mGlowPadView.setOnTriggerListener(mOnTriggerListener);
        ribbonView = (LinearLayout) findViewById(R.id.keyguard_ribbon_and_battery);
        ribbonView.bringToFront();
        mRibbon = (LinearLayout) ribbonView.findViewById(R.id.ribbon);
        mRibbon.removeAllViews();
        mRibbon.addView(AokpRibbonHelper.getRibbon(mContext,
            Settings.System.getArrayList(cr,
                Settings.System.RIBBON_TARGETS_SHORT[AokpRibbonHelper.LOCKSCREEN]),
            Settings.System.getArrayList(cr,
                Settings.System.RIBBON_TARGETS_LONG[AokpRibbonHelper.LOCKSCREEN]),
            Settings.System.getArrayList(cr,
                Settings.System.RIBBON_TARGETS_ICONS[AokpRibbonHelper.LOCKSCREEN]),
            Settings.System.getBoolean(cr,
                Settings.System.ENABLE_RIBBON_TEXT[AokpRibbonHelper.LOCKSCREEN], true),
            Settings.System.getInt(cr,
                Settings.System.RIBBON_TEXT_COLOR[AokpRibbonHelper.LOCKSCREEN], -1),
            Settings.System.getInt(cr,
                Settings.System.RIBBON_ICON_SIZE[AokpRibbonHelper.LOCKSCREEN], 0),
            Settings.System.getInt(cr,
                Settings.System.RIBBON_ICON_SPACE[AokpRibbonHelper.LOCKSCREEN], 5),
            Settings.System.getBoolean(cr,
                Settings.System.RIBBON_ICON_VIBRATE[AokpRibbonHelper.LOCKSCREEN], true),
            Settings.System.getBoolean(cr,
                Settings.System.RIBBON_ICON_COLORIZE[AokpRibbonHelper.LOCKSCREEN], true), 0));
        updateTargets();

        mGlowTorch = Settings.System.getInt(cr,
                Settings.System.LOCKSCREEN_GLOW_TORCH, 0);
        mGlowTorchOn = false;

        mSecurityMessageDisplay = new KeyguardMessageArea.Helper(this);
        View bouncerFrameView = findViewById(R.id.keyguard_selector_view_frame);
        mBouncerFrame = bouncerFrameView.getBackground();
        mUnlockBroadcasted = false;
        filter = new IntentFilter();
        filter.addAction(UnlockReceiver.ACTION_UNLOCK_RECEIVER);
        if (mUnlockReceiver == null) {
            mUnlockReceiver = new UnlockReceiver();
        }
        mContext.registerReceiver(mUnlockReceiver, filter);
        mReceiverRegistered = true;

        final int unsecureUnlockMethod = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.LOCKSCREEN_UNSECURE_USED, 1);
        final int lockBeforeUnlock = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.LOCK_BEFORE_UNLOCK, 0);

        //bring emergency button on slider lockscreen to front when lockBeforeUnlock is enabled
        //to make it clickable
        if (unsecureUnlockMethod == 0 && lockBeforeUnlock == 1) {
            LinearLayout ecaContainer = (LinearLayout) findViewById(R.id.keyguard_selector_fade_container);
            ecaContainer.bringToFront();
        }
    }

    public void setCarrierArea(View carrierArea) {
        mFadeView = carrierArea;
    }

    public boolean isTargetPresent(int resId) {
        return mGlowPadView.getTargetPosition(resId) != -1;
    }

    @Override
    public void showUsabilityHint() {
        mGlowPadView.ping();
    }

    private void fireTorch() {
        mHandler.removeCallbacks(startTorch);
        if (mGlowTorch == 1 && mGlowTorchOn) {
            mGlowTorchOn = false;
            GlowPadTorchHelper.killTorch(mContext);
            RotationPolicy.setRotationLock(mContext, mUserRotation);
            mHandler.postDelayed(checkTorch, GlowPadTorchHelper.TORCH_CHECK);
        }
    }

    final Runnable startTorch = new Runnable () {
        public void run() {
            if (!mGlowTorchOn) {
                mUserRotation = RotationPolicy.isRotationLocked(mContext);
                RotationPolicy.setRotationLock(mContext, true);
                mGlowTorchOn = GlowPadTorchHelper.startTorch(mContext);
            }
        }
    };

    final Runnable checkTorch = new Runnable () {
        public void run() {
            if (GlowPadTorchHelper.torchActive(mContext)) {
                GlowPadTorchHelper.torchOff(mContext, true);
            }
        }
    };

    private void updateTargets() {
        int currentUserHandle = mLockPatternUtils.getCurrentUser();
        DevicePolicyManager dpm = mLockPatternUtils.getDevicePolicyManager();
        int disabledFeatures = dpm.getKeyguardDisabledFeatures(null, currentUserHandle);
        boolean secureCameraDisabled = mLockPatternUtils.isSecure()
                && (disabledFeatures & DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA) != 0;
        boolean cameraDisabledByAdmin = dpm.getCameraDisabled(null, currentUserHandle)
                || secureCameraDisabled;
        final KeyguardUpdateMonitor monitor = KeyguardUpdateMonitor.getInstance(getContext());
        boolean disabledBySimState = monitor.isSimLocked();
        boolean cameraPresent =
                mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
        boolean searchTargetPresent =
                isTargetPresent(com.android.internal.R.drawable.ic_action_assist_generic);
        mLongPress = false;
        mGlowPadLock = false;

        if (cameraDisabledByAdmin) {
            Log.v(TAG, "Camera disabled by Device Policy");
        } else if (disabledBySimState) {
            Log.v(TAG, "Camera disabled by Sim State");
        }
        boolean currentUserSetup = 0 != Settings.Secure.getIntForUser(
                mContext.getContentResolver(),
                Settings.Secure.USER_SETUP_COMPLETE,
                0 /*default */,
                currentUserHandle);
        boolean searchActionAvailable =
                ((SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE))
                .getAssistIntent(mContext, false, UserHandle.USER_CURRENT) != null;
        mCameraDisabled = cameraDisabledByAdmin || disabledBySimState || !cameraPresent
                || !currentUserSetup;
        mSearchDisabled = disabledBySimState || !searchActionAvailable || !searchTargetPresent
                || !currentUserSetup;
        updateResources();
        updateLockscreenBattery(null);
    }

    public void updateResources() {
        String storedTargets = Settings.System.getStringForUser(mContext.getContentResolver(),
                Settings.System.LOCKSCREEN_TARGETS, UserHandle.USER_CURRENT);
        if (storedTargets == null) {
            // Update the search icon with drawable from the search .apk
            if (!mSearchDisabled) {
                Intent intent = ((SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE))
                        .getAssistIntent(mContext, false, UserHandle.USER_CURRENT);
                if (intent != null) {
                    // XXX Hack. We need to substitute the icon here but haven't formalized
                    // the public API. The "_google" metadata will be going away, so
                    // DON'T USE IT!
                    ComponentName component = intent.getComponent();
                    boolean replaced = mGlowPadView.replaceTargetDrawablesIfPresent(component,
                            ASSIST_ICON_METADATA_NAME + "_google",
                            com.android.internal.R.drawable.ic_action_assist_generic);

                    if (!replaced && !mGlowPadView.replaceTargetDrawablesIfPresent(component,
                                ASSIST_ICON_METADATA_NAME,
                                com.android.internal.R.drawable.ic_action_assist_generic)) {
                            Slog.w(TAG, "Couldn't grab icon from package " + component);
                    }
                }
            }

            mGlowPadView.setEnableTarget(com.android.internal.R.drawable
                    .ic_lockscreen_camera, !mCameraDisabled);
            mGlowPadView.setEnableTarget(com.android.internal.R.drawable
                    .ic_action_assist_generic, !mSearchDisabled);

            // Enable magnetic targets
            mGlowPadView.setMagneticTargets(true);
        } else {
            mStoredTargets = storedTargets.split("\\|");
            ArrayList<TargetDrawable> storedDrawables = new ArrayList<TargetDrawable>();

            final Resources res = getResources();
            final Drawable blankActiveDrawable = res.getDrawable(
                    R.drawable.ic_lockscreen_target_activated);
            final InsetDrawable activeBack = new InsetDrawable(blankActiveDrawable, 0, 0, 0, 0);

            // Disable magnetic target
            mGlowPadView.setMagneticTargets(false);

            // Magnetic target replacement
            final Drawable blankInActiveDrawable = res.getDrawable(
                    com.android.internal.R.drawable.ic_lockscreen_lock_pressed);
            final Drawable unlockActiveDrawable = res.getDrawable(
                    com.android.internal.R.drawable.ic_lockscreen_unlock_activated);

            // Shift targets for landscape lockscreen on phones
            for (int i = 0; i < mTargetOffset; i++) {
                storedDrawables.add(new TargetDrawable(res, null));
            }

            // Add unlock target
            storedDrawables.add(new TargetDrawable(res,
                    res.getDrawable(R.drawable.ic_lockscreen_unlock)));

            for (int i = 0; i < 8 - mTargetOffset - 1; i++) {
                if (i >= mStoredTargets.length) {
                    storedDrawables.add(new TargetDrawable(res, 0));
                    continue;
                }

                String uri = mStoredTargets[i];
                if (uri.equals(GlowPadView.EMPTY_TARGET)) {
                    Drawable d = LockscreenTargetUtils.getLayeredDrawable(
                            mContext, unlockActiveDrawable, blankInActiveDrawable,
                            LockscreenTargetUtils.getInsetForIconType(mContext, null), true);
                    storedDrawables.add(new TargetDrawable(res, d));
                    continue;
                }

                try {
                    Intent intent = Intent.parseUri(uri, 0);
                    Drawable front = null;
                    Drawable back = activeBack;
                    boolean frontBlank = false;
                    String type = null;

                    if (intent.hasExtra(GlowPadView.ICON_FILE)) {
                        type = GlowPadView.ICON_FILE;
                        front = LockscreenTargetUtils.getDrawableFromFile(mContext,
                                intent.getStringExtra(GlowPadView.ICON_FILE));
                    } else if (intent.hasExtra(GlowPadView.ICON_RESOURCE)) {
                        String source = intent.getStringExtra(GlowPadView.ICON_RESOURCE);
                        String packageName = intent.getStringExtra(GlowPadView.ICON_PACKAGE);

                        if (source != null) {
                            front = LockscreenTargetUtils.getDrawableFromResources(mContext,
                                    packageName, source, false);
                            back = LockscreenTargetUtils.getDrawableFromResources(mContext,
                                    packageName, source, true);
                            type = GlowPadView.ICON_RESOURCE;
                            frontBlank = true;
                        }
                    }
                    if (front == null || back == null) {
                        front = LockscreenTargetUtils.getDrawableFromIntent(mContext, intent);
                    }

                    int inset = LockscreenTargetUtils.getInsetForIconType(mContext, type);
                    Drawable drawable = LockscreenTargetUtils.getLayeredDrawable(mContext,
                            back,front, inset, frontBlank);
                    TargetDrawable targetDrawable = new TargetDrawable(res, drawable);

                    ComponentName compName = intent.getComponent();
                    String className = compName == null ? null : compName.getClassName();
                    if (TextUtils.equals(className, "com.android.camera.CameraLauncher")) {
                        targetDrawable.setEnabled(!mCameraDisabled);
                    } else if (TextUtils.equals(className, "SearchActivity")) {
                        targetDrawable.setEnabled(!mSearchDisabled);
                    }

                    storedDrawables.add(targetDrawable);
                } catch (URISyntaxException e) {
                    Log.w(TAG, "Invalid target uri " + uri);
                    storedDrawables.add(new TargetDrawable(res, 0));
                }
            }

            mGlowPadView.setTargetResources(storedDrawables);
        }
    }

    void doTransition(View view, float to) {
        if (mAnim != null) {
            mAnim.cancel();
        }
        mAnim = ObjectAnimator.ofFloat(view, "alpha", to);
        mAnim.start();
    }

    public void setKeyguardCallback(KeyguardSecurityCallback callback) {
        mCallback = callback;
    }

    public void setLockPatternUtils(LockPatternUtils utils) {
        mLockPatternUtils = utils;
    }

    @Override
    public void reset() {
        mGlowPadView.reset(false);
    }

    @Override
    public boolean needsInput() {
        return false;
    }

    @Override
    public void onPause() {
        KeyguardUpdateMonitor.getInstance(getContext()).removeCallback(mInfoCallback);
        if (mReceiverRegistered) {
            mContext.unregisterReceiver(mUnlockReceiver);
            mReceiverRegistered = false;
        }
    }

    @Override
    public void onResume(int reason) {
        KeyguardUpdateMonitor.getInstance(getContext()).registerCallback(mInfoCallback);
        if (!mReceiverRegistered) {
            if (mUnlockReceiver == null) {
               mUnlockReceiver = new UnlockReceiver();
            }
            mContext.registerReceiver(mUnlockReceiver, filter);
            mReceiverRegistered = true;
        }
    }

    @Override
    public KeyguardSecurityCallback getCallback() {
        return mCallback;
    }

    @Override
    public void showBouncer(int duration) {
        mIsBouncing = true;
        KeyguardSecurityViewHelper.
                showBouncer(mSecurityMessageDisplay, mFadeView, mBouncerFrame, duration);
    }

    @Override
    public void hideBouncer(int duration) {
        mIsBouncing = false;
        KeyguardSecurityViewHelper.
                hideBouncer(mSecurityMessageDisplay, mFadeView, mBouncerFrame, duration);
    }

    public void updateLockscreenBattery(KeyguardUpdateMonitor.BatteryStatus status) {
        if (Settings.System.getIntForUser(
                mContext.getContentResolver(),
                Settings.System.BATTERY_AROUND_LOCKSCREEN_RING,
                0 /*default */,
                UserHandle.USER_CURRENT) == 1) {
            if (status != null) mBatteryLevel = status.level;
            float cappedBattery = mBatteryLevel;

            if (mBatteryLevel < 15) {
                cappedBattery = 15;
            }
            else if (mBatteryLevel > 90) {
                cappedBattery = 90;
            }

            final float hue = (cappedBattery - 15) * 1.6f;
            mGlowPadView.setArc(mBatteryLevel * 3.6f, Color.HSVToColor(0x80, new float[]{ hue, 1.f, 1.f }));
        } else {
            mGlowPadView.setArc(0, 0);
        }
    }
    public class UnlockReceiver extends BroadcastReceiver {
        public static final String ACTION_UNLOCK_RECEIVER = "com.android.lockscreen.ACTION_UNLOCK_RECEIVER";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_UNLOCK_RECEIVER)) {
                if (!mUnlockBroadcasted) {
                    mUnlockBroadcasted = true;
                    mCallback.userActivity(0);
                    mCallback.dismiss(false);
                }
            }
            if (mReceiverRegistered) {
                mContext.unregisterReceiver(mUnlockReceiver);
                mReceiverRegistered = false;
            }
        }
    }
}
