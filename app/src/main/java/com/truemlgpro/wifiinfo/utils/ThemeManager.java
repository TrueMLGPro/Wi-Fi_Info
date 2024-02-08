package com.truemlgpro.wifiinfo.utils;

import android.app.Activity;
import android.content.Context;

import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.interfaces.PreferenceDefaults;
import com.truemlgpro.wifiinfo.interfaces.PreferenceKeys;

/**
 * A helper class for managing themes
 */
public class ThemeManager {
	/**
	 * Initializes themes for Activities based on selected Theme preference
	 * @param activity an activity to apply a theme to
	 * @param appContext a context to pass, has to be an Application Context
	 */
	public static void initializeThemes(Activity activity, Context appContext) {
		boolean keyDarkTheme = new SharedPreferencesManager(appContext).retrieveBoolean(PreferenceKeys.KEY_PREF_DARK_MODE, PreferenceDefaults.DARK_MODE);
		boolean keyAmoledTheme = new SharedPreferencesManager(appContext).retrieveBoolean(PreferenceKeys.KEY_PREF_AMOLED_MODE, PreferenceDefaults.AMOLED_MODE);

		if (keyAmoledTheme) {
			if (keyDarkTheme) {
				activity.setTheme(R.style.AmoledDarkTheme);
			}
		} else {
			activity.setTheme(R.style.DarkTheme);
		}

		if (!keyDarkTheme) {
			activity.setTheme(R.style.LightTheme);
		}
	}

	/**
	 * Checks if current theme is a dark theme or not
	 * @param appContext a context to pass, has to be an Application Context
	 */
	public static boolean isDarkTheme(Context appContext) {
		return new SharedPreferencesManager(appContext).retrieveBoolean(PreferenceKeys.KEY_PREF_DARK_MODE, PreferenceDefaults.DARK_MODE);
	}
}
