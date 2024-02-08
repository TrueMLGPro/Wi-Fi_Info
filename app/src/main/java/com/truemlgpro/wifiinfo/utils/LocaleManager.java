package com.truemlgpro.wifiinfo.utils;

import android.content.Context;
import android.content.res.Resources;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.ConfigurationCompat;
import androidx.core.os.LocaleListCompat;

import com.truemlgpro.wifiinfo.interfaces.PreferenceDefaults;
import com.truemlgpro.wifiinfo.interfaces.PreferenceKeys;

/**
 * A helper class for managing locale settings
 */
public class LocaleManager {
	/**
	 * Initializes locales for Activities based on the selected Locale preference
	 * @param appContext a context to pass, has to be an App Context
	 */
	public static void initializeLocale(Context appContext) {
		String appLocalePref = new SharedPreferencesManager(appContext).retrieveString(PreferenceKeys.KEY_PREF_APP_LANGUAGE, PreferenceDefaults.APP_LANG);
		if (PreferenceDefaults.APP_LANG.equals(appLocalePref)) {
			setLocale(getDefaultSystemLocale());
		} else {
			setLocale(appLocalePref);
		}
	}

	private static void setLocale(String locale) {
		LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(locale);
		AppCompatDelegate.setApplicationLocales(appLocale);
	}

	public static String getDefaultSystemLocale() {
		return String.valueOf(ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(1));
	}
}
