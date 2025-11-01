package com.truemlgpro.wifiinfo.utils.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.ConfigurationCompat;
import androidx.core.os.LocaleListCompat;

import com.truemlgpro.wifiinfo.interfaces.PreferenceDefaults;
import com.truemlgpro.wifiinfo.interfaces.PreferenceKeys;
import com.truemlgpro.wifiinfo.utils.app.SharedPreferencesManager;

/**
 * A helper class for managing locale settings
 */
public final class LocaleManager {

	private LocaleManager() {}

	/**
	 * Initializes locales for Activities based on the selected Locale preference
	 * @param appContext a context to pass, has to be ApplicationContext
	 */
	public static void initializeLocale(Context appContext) {
		String appLocalePref = new SharedPreferencesManager(appContext).retrieveString(PreferenceKeys.KEY_PREF_APP_LANGUAGE, PreferenceDefaults.APP_LANGUAGE);

		LocaleListCompat newLocales = isFollowSystem(appLocalePref)
				? LocaleListCompat.getEmptyLocaleList() // Follow system lang
				: LocaleListCompat.forLanguageTags(normalizeTags(appLocalePref)); // Set selected lang

		applyIfChanged(newLocales);
	}

	private static void applyIfChanged(LocaleListCompat newLocales) {
		LocaleListCompat current = AppCompatDelegate.getApplicationLocales();
		String newTags = newLocales.toLanguageTags();
		String currentTags = current.toLanguageTags();
		if (!newTags.equals(currentTags)) {
			AppCompatDelegate.setApplicationLocales(newLocales);
		}
	}

	private static boolean isFollowSystem(String pref) {
		return pref == null
				|| pref.isEmpty()
				|| PreferenceDefaults.APP_LANGUAGE.equals(pref);
	}

	private static String normalizeTags(String tags) {
		return tags.trim().replace('_', '-');
	}

	public static Context getLocalizedContext(Context baseContext) {
		String pref = new SharedPreferencesManager(baseContext.getApplicationContext())
				.retrieveString(PreferenceKeys.KEY_PREF_APP_LANGUAGE, PreferenceDefaults.APP_LANGUAGE);

		LocaleListCompat desiredLocale = isFollowSystem(pref)
				? ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()) // System locales
				: LocaleListCompat.forLanguageTags(normalizeTags(pref));                   // App locales

		Configuration conf = new Configuration(baseContext.getResources().getConfiguration());
		ConfigurationCompat.setLocales(conf, desiredLocale);
		return baseContext.createConfigurationContext(conf);
	}
}
