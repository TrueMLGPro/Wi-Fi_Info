package com.truemlgpro.wifiinfo.utils;

import android.content.Context;
import android.view.Window;

import com.truemlgpro.wifiinfo.interfaces.PreferenceDefaults;
import com.truemlgpro.wifiinfo.interfaces.PreferenceKeys;

/**
 * A helper class for managing KEEP_SCREEN_ON flags
 */
public class KeepScreenOnManager {
	/**
	 * Sets or resets the activity window FLAG_KEEP_SCREEN_ON flag depending on the SharedPreference value
	 * @param activityWindow a window to pass using getWindow() on Activity
	 * @param appContext a context to pass, has to be an App Context
	 */
	public static void init(Window activityWindow, Context appContext) {
		boolean keyKeepScreenOn = new SharedPreferencesManager(appContext).retrieveBoolean(PreferenceKeys.KEY_PREF_KEEP_SCREEN_ON, PreferenceDefaults.KEEP_SCREEN_ON);

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
