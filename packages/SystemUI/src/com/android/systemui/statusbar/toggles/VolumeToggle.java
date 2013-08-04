package com.android.systemui.statusbar.toggles;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.Settings;
import android.view.View;

import com.android.systemui.R;

public class VolumeToggle extends BaseToggle {

    private AudioManager am;

    @Override
    public void init(Context c, int style) {
        super.init(c, style);
        setIcon(R.drawable.ic_qs_volume);
        setLabel(R.string.quick_settings_volume_label);

        am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onClick(View v) {
        collapseStatusBar();
        dismissKeyguard();

        am.adjustVolume(AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
    }

    @Override
    public boolean onLongClick(View v) {
        dismissKeyguard();
        collapseStatusBar();
        startActivity(new Intent(android.provider.Settings.ACTION_SOUND_SETTINGS));
        return super.onLongClick(v);
    }

}
