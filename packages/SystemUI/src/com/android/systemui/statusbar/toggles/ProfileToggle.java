
package com.android.systemui.statusbar.toggles;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.app.Profile;
import android.app.ProfileManager;
import com.android.server.ProfileManagerService;

import com.android.systemui.R;

public class ProfileToggle extends BaseToggle {

    private Profile mChosenProfile;
    private ProfileManager mProfileManager;
    private ProfileReceiver mProfileReceiver;

    @Override
    public void init(Context c, int style) {
        super.init(c, style);
        mProfileReceiver = new ProfileReceiver();
        mProfileReceiver.registerSelf();
        mProfileManager = (ProfileManager) mContext.getSystemService(Context.PROFILE_SERVICE);
        setIcon(R.drawable.ic_qs_profiles);
        setLabel(mProfileManager.getActiveProfile().getName());
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent(Intent.ACTION_POWERMENU_PROFILE);
        mContext.sendBroadcast(intent);

        collapseStatusBar();
        dismissKeyguard();
    }

    @Override
    public boolean onLongClick(View v) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClassName("com.android.settings", "com.android.settings.Settings$ProfilesSettingsActivity");
        intent.addCategory("android.intent.category.LAUNCHER");
        
        startActivity(intent);
        collapseStatusBar();
        dismissKeyguard();
        return super.onLongClick(v);
    }

    private class ProfileReceiver extends BroadcastReceiver {
        private boolean mIsRegistered;

        public ProfileReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
        setLabel(mProfileManager.getActiveProfile().getName());
            scheduleViewUpdate();
        }

        private void registerSelf() {
            if (!mIsRegistered) {
                mIsRegistered = true;

                IntentFilter filter = new IntentFilter();
                filter.addAction(ProfileManagerService.INTENT_ACTION_PROFILE_SELECTED);
                mContext.registerReceiver(mProfileReceiver, filter);
            }
        }

        private void unregisterSelf() {
            if (mIsRegistered) {
                mIsRegistered = false;
                mContext.unregisterReceiver(this);
            }
        }
    }

} 
