package com.truemlgpro.wifiinfo.utils;

import android.app.Activity;
import android.content.Context;

import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.ui.MainActivity;
import com.truemlgpro.wifiinfo.ui.SettingsActivity;

/**
 * A helper class for managing themes
 */
public class ThemeManager {
	/**
	 * Initializes themes for Activities based on selected Theme preference
	 * @param activity an activity which called this method
	 * @param appContext a context to pass, has to be an Application Context
	 */
	public static void initializeThemes(Activity activity, Context appContext) {
		boolean keyTheme = new SharedPreferencesManager(appContext).retrieveBoolean(SettingsActivity.KEY_PREF_DARK_MODE_SWITCH, MainActivity.darkMode);
		boolean keyAmoledTheme = new SharedPreferencesManager(appContext).retrieveBoolean(SettingsActivity.KEY_PREF_AMOLED_MODE_CHECK, MainActivity.amoledMode);

		if (keyAmoledTheme) {
			if (keyTheme) {
				activity.setTheme(R.style.AmoledDarkTheme);
			}
		} else {
			activity.setTheme(R.style.DarkTheme);
		}

		if (!keyTheme) {
			activity.setTheme(R.style.LightTheme);
		}
	}
}
