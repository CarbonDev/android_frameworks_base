/*
 * This class is part of SlimRoms SlimActions.java
 * crDroid don't need all the class
 * Let's use only TRDS for now
 *
 * Class was re-named either
 *
 * Thanks SlimRoms and kufikugel
 */
 
package com.android.internal.util.slim;

import android.app.Activity;
import android.app.IUiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.IWindowManager;
import android.widget.Toast;

import com.android.internal.statusbar.IStatusBarService;

public class TRDSActions {

    public static void processAction(Context context, String action, boolean isLongpress) {

        final IStatusBarService barService = IStatusBarService.Stub.asInterface(
                    ServiceManager.getService(Context.STATUS_BAR_SERVICE));

        if (action.equals(TRDSConstant.ACTION_THEME_SWITCH)) {
                boolean autoLightMode = Settings.Secure.getIntForUser(
                        context.getContentResolver(),
                        Settings.Secure.UI_THEME_AUTO_MODE, 0,
                        UserHandle.USER_CURRENT) == 1;
                boolean state = context.getResources().getConfiguration().uiThemeMode
                        == Configuration.UI_THEME_MODE_HOLO_DARK;
                if (autoLightMode) {
                    try {
                        barService.collapsePanels();
                    } catch (RemoteException ex) {
                    }
                    Toast.makeText(context,
                            com.android.internal.R.string.theme_auto_switch_mode_error,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                // Handle a switch change
                // we currently switch between holodark and hololight till either
                // theme engine is ready or lightheme is ready. Currently due of
                // missing light themeing hololight = system base theme
                final IUiModeManager uiModeManagerService = IUiModeManager.Stub.asInterface(
                        ServiceManager.getService(Context.UI_MODE_SERVICE));
                try {
                    uiModeManagerService.setUiThemeMode(state
                            ? Configuration.UI_THEME_MODE_HOLO_LIGHT
                            : Configuration.UI_THEME_MODE_HOLO_DARK);
                } catch (RemoteException e) {
                }
                return;
        }            
    }
}
