/*
 * Copyright 2011 AOKP by Mike Wilson - Zaphod-Beeblebrox
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

package com.android.systemui.liquid;

import java.net.URISyntaxException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContentUris;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Vibrator;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.widget.Toast;

import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.R;

/*
 * Helper classes for managing custom actions
 */

public class LiquidTarget {

    final String TAG = "LiquidTarget";

    public final static String ACTION_HOME = "**home**";
    public final static String ACTION_BACK = "**back**";
    public final static String ACTION_SCREENSHOT = "**screenshot**";
    public final static String ACTION_MENU = "**menu**";
    public final static String ACTION_POWER = "**power**";
    public final static String ACTION_NOTIFICATIONS = "**notifications**";
    public final static String ACTION_RECENTS = "**recents**";
    public final static String ACTION_IME = "**ime**";
    public final static String ACTION_KILL = "**kill**";
    public final static String ACTION_ASSIST = "**assist**";
    public final static String ACTION_CUSTOM = "**custom**";
    public final static String ACTION_SILENT = "**ring_silent**";
    public final static String ACTION_VIB = "**ring_vib**";
    public final static String ACTION_SILENT_VIB = "**ring_vib_silent**";
    public final static String ACTION_EVENT = "**event**";
    public final static String ACTION_ALARM = "**alarm**";
    public final static String ACTION_TODAY = "**today**";
    public final static String ACTION_CLOCKOPTIONS = "**clockoptions**";
	public final static String ACTION_VOICEASSIST = "**voiceassist**";
	public final static String ACTION_TORCH = "**torch**";
    public final static String ACTION_NULL = "**null**";

    private int mInjectKeyCode;
    private Context mContext;
    private Handler mHandler;

    final Object mScreenshotLock = new Object();
    ServiceConnection mScreenshotConnection = null;

    public LiquidTarget (Context context){
        mContext = context;
        mHandler = new Handler();
    }

    public boolean launchAction (String action){

        try {
            ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
        } catch (RemoteException e) {
        }

        if (action == null || action.equals(ACTION_NULL)) {
            return false;
        }
        if (action.equals(ACTION_TORCH)) {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(ComponentName.unflattenFromString("com.aokp.Torch/.TorchActivity"));
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return true;
        }

        if (action.equals(ACTION_VIB)) {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            if(am != null){
                if(am.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE) {
                    am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    Vibrator vib = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                    if(vib != null){
                        vib.vibrate(50);
                    }
                }else{
                    am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, (int)(ToneGenerator.MAX_VOLUME * 0.85));
                    if(tg != null){
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP);
                    }
                }
            }
            return true;
        }
        if (action.equals(ACTION_SILENT)) {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            if(am != null){
                if(am.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                    am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                }else{
                    am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, (int)(ToneGenerator.MAX_VOLUME * 0.85));
                    if(tg != null){
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP);
                    }
                }
            }
            return true;
        }
            // we must have a custom uri
        try {
            Intent intent = Intent.parseUri(action, 0);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return true;
            } catch (URISyntaxException e) {
                    Log.e(TAG, "URISyntaxException: [" + action + "]");
            } catch (ActivityNotFoundException e){
                    Log.e(TAG, "ActivityNotFound: [" + action + "]");
            }
        return false; // we didn't handle the action!
    }

}
