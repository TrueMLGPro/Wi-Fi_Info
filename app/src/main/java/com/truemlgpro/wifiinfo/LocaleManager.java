package com.truemlgpro.wifiinfo;

import android.content.Context;
import android.content.res.Resources;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.ConfigurationCompat;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

/**
 * A helper class for managing locale settings
 */
public class LocaleManager {
	/**
	 * Initializes locales for Activities based on selected Locale preference
	 * @param appContext a context to pass, has to be an Application Context
	 */
	public static void initializeLocale(Context appContext) {
		String appLocalePref = new SharedPreferencesManager(appContext).retrieveString(SettingsActivity.KEY_PREF_APP_LANGUAGE, MainActivity.appLang);
		if (appLocalePref.equals("default_lang")) {
			String defaultSystemLocale = String.valueOf(getDefaultSystemLocale());
			setLocale(defaultSystemLocale);
		} else {
			setLocale(appLocalePref);
		}
	}

	private static void setLocale(String locale) {
		LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(locale);
		AppCompatDelegate.setApplicationLocales(appLocale);
	}

	private static Locale getDefaultSystemLocale() {
		return ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
	}
}
