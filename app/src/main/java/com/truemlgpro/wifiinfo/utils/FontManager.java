package com.truemlgpro.wifiinfo.utils;

import android.app.Activity;
import android.content.Context;

import com.truemlgpro.wifiinfo.interfaces.PreferenceDefaults;
import com.truemlgpro.wifiinfo.interfaces.PreferenceKeys;

import me.anwarshahriar.calligrapher.Calligrapher;

/**
 * A helper class for managing fonts
 */
public class FontManager {
	/**
	 * Sets a font for the Activity depending on the SharedPreference value
	 * @param activity an Activity to pass to Calligrapher, used for changing the font for all TextViews
	 * @param appContext a context to pass, has to be an App Context
	 * @param includeActionBar defines if Calligrapher should change the font for the ActionBar
	 */
	public static void init(Activity activity, Context appContext, boolean includeActionBar) {
		Calligrapher calligrapher = new Calligrapher(activity);
		String font = new SharedPreferencesManager(appContext).retrieveString(PreferenceKeys.KEY_PREF_APP_FONT, PreferenceDefaults.APP_FONT);
		calligrapher.setFont(activity, font, includeActionBar);
	}
}
