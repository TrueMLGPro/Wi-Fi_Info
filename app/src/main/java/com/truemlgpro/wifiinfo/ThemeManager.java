package com.truemlgpro.wifiinfo;

import android.app.Activity;
import android.content.Context;

/**
 * A helper class for managing themes
 */
public class ThemeManager {
	/**
	 * Initializes themes for Activities based on selected Theme preferences
	 * @param activity an activity which called this method
	 * @param context a context to pass, has to be an Application Context
	 */
	public void initializeThemes(Activity activity, Context context) {
		boolean keyTheme = new SharedPreferencesManager(context).retrieveBoolean(SettingsActivity.KEY_PREF_SWITCH, MainActivity.darkMode);
		boolean keyAmoledTheme = new SharedPreferencesManager(context).retrieveBoolean(SettingsActivity.KEY_PREF_AMOLED_CHECK, MainActivity.amoledMode);

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
