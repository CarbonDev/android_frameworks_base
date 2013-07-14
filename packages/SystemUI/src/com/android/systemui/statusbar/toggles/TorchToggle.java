
package com.android.systemui.statusbar.toggles;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;

import static com.android.internal.util.carbon.AwesomeConstants.*;
import com.android.internal.util.cm.TorchConstants;
import com.android.systemui.R;
import com.android.systemui.carbon.AwesomeAction;

public class TorchToggle extends StatefulToggle {
    TorchObserver mObserver = null;

    private static final IntentFilter TORCH_STATE_FILTER =
            new IntentFilter(TorchConstants.ACTION_STATE_CHANGED);

    @Override
    public void init(Context c, int style) {
        super.init(c, style);
        mObserver = new TorchObserver(mHandler);
        mObserver.observe();
    }

    @Override
    protected void cleanup() {
        if (mObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }
        super.cleanup();
    }

    @Override
    protected void doEnable() {
        Intent i = new Intent(TorchConstants.ACTION_TOGGLE_STATE);
        mContext.sendBroadcast(i);
    }

    @Override
    protected void doDisable() {
        Intent i = new Intent(TorchConstants.ACTION_TOGGLE_STATE);
        mContext.sendBroadcast(i);
    }

    @Override
    public boolean onLongClick(View v) {
        startActivity(TorchConstants.INTENT_LAUNCH_APP);
        return super.onLongClick(v);
    }

    @Override
    protected void updateView() {
         Intent stateIntent = mContext.registerReceiver(null, TORCH_STATE_FILTER);
         boolean enabled = stateIntent != null
                 && stateIntent.getIntExtra(TorchConstants.EXTRA_CURRENT_STATE, 0) != 0;
        setIcon(enabled
                ? R.drawable.ic_qs_torch_on
                : R.drawable.ic_qs_torch_off);
        setLabel(enabled
                ? R.string.quick_settings_torch_on_label
                : R.string.quick_settings_torch_off_label);
        updateCurrentState(enabled ? State.ENABLED : State.DISABLED);
        super.updateView();
    }

    protected class TorchObserver extends ContentObserver {
        TorchObserver(Handler handler) {
            super(handler);
            observe();
        }

        void observe() {
         Intent stateIntent = mContext.registerReceiver(null, TORCH_STATE_FILTER);
         boolean enabled = stateIntent != null
                 && stateIntent.getIntExtra(TorchConstants.EXTRA_CURRENT_STATE, 0) != 0;
            onChange(false);
        }

        @Override
        public void onChange(boolean selfChange) {
            scheduleViewUpdate();
        }
    }

}
