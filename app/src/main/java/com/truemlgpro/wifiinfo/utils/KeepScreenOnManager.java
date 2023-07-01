package com.truemlgpro.wifiinfo.utils;

import android.content.Context;
import android.view.Window;

import com.truemlgpro.wifiinfo.ui.MainActivity;
import com.truemlgpro.wifiinfo.ui.SettingsActivity;

/**
 * A helper class for managing KEEP_SCREEN_ON flags
 */
public class KeepScreenOnManager {
	/**
	 * Sets or resets the activity window FLAG_KEEP_SCREEN_ON flag depending on SharedPreference value
	 * @param activityWindow a window to pass using getWindow() on Activity
	 * @param appContext a context to pass, has to be an Application Context
	 */
	public static void init(Window activityWindow, Context appContext) {
		boolean keyKeepScreenOn = new SharedPreferencesManager(appContext).retrieveBoolean(SettingsActivity.KEY_PREF_KEEP_SCREEN_ON_SWITCH, MainActivity.keepScreenOn);

		if (keyKeepScreenOn) {
			keepScreenOn(activityWindow);
		} else {
			resetKeepScreenOn(activityWindow);
		}
	}

	private static void keepScreenOn(Window activityWindow) {
		activityWindow.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	private static void resetKeepScreenOn(Window activityWindow) {
		activityWindow.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
}
