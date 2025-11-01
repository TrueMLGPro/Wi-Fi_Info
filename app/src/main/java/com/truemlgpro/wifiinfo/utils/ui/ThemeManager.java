package com.truemlgpro.wifiinfo.utils.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.google.android.material.color.DynamicColors;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.interfaces.PreferenceDefaults;
import com.truemlgpro.wifiinfo.interfaces.PreferenceKeys;
import com.truemlgpro.wifiinfo.interfaces.PreferenceValues;
import com.truemlgpro.wifiinfo.utils.app.SharedPreferencesManager;

/**
 * A helper class for managing themes
 */
public final class ThemeManager {
	/**
	 * Initializes themes for Activities based on selected Theme preference
	 * @param activity an Activity to apply a theme to
	 * @param appContext a Context to pass, has to be ApplicationContext
	 */
	public static void initializeThemes(Activity activity, Context appContext) {
		SharedPreferencesManager prefs = new SharedPreferencesManager(appContext);
		boolean keyDarkTheme = prefs.retrieveBoolean(PreferenceKeys.KEY_PREF_DARK_THEME, PreferenceDefaults.DARK_THEME);
		boolean keyAmoledTheme = prefs.retrieveBoolean(PreferenceKeys.KEY_PREF_AMOLED_THEME, PreferenceDefaults.AMOLED_THEME);
		boolean keyMonetTheme = prefs.retrieveBoolean(PreferenceKeys.KEY_PREF_MONET_THEME, PreferenceDefaults.MONET_THEME);
		String keyCardStyleAmoled = prefs.retrieveString(PreferenceKeys.KEY_PREF_CARD_STYLE_AMOLED, PreferenceDefaults.CARD_STYLE_AMOLED);
		String keyThemeContrast = prefs.retrieveString(PreferenceKeys.KEY_PREF_THEME_CONTRAST_VARIANT, PreferenceDefaults.THEME_CONTRAST_VARIANT);

		int baseTheme = keyDarkTheme ? R.style.Theme_App_Dark : R.style.Theme_App_Light;
		activity.setTheme(baseTheme);

		if (keyMonetTheme && DynamicColors.isDynamicColorAvailable()) {
			DynamicColors.applyToActivityIfAvailable(activity);
		}

		int lockedStatusBar = resolveSystemBarColor(activity, android.R.attr.statusBarColor);
		int lockedNavBar = resolveSystemBarColor(activity, android.R.attr.navigationBarColor);

		int contrastOverlay = resolveContrastOverlay(keyDarkTheme, keyThemeContrast);
		if (contrastOverlay != 0) {
			activity.getTheme().applyStyle(contrastOverlay, true);
		}

		int amoledOverlay = resolveAmoledOverlay(keyAmoledTheme, keyDarkTheme, keyCardStyleAmoled);
		if (amoledOverlay != 0) {
			activity.getTheme().applyStyle(amoledOverlay, true);
		}

		activity.getWindow().setStatusBarColor(lockedStatusBar);
		activity.getWindow().setNavigationBarColor(lockedNavBar);
	}

	private static int resolveSystemBarColor(Context context, int attr) {
		TypedValue tv = new TypedValue();
		Resources.Theme theme = context.getTheme();
		if (theme.resolveAttribute(attr, tv, true)) {
			if (tv.resourceId != 0) return ContextCompat.getColor(context, tv.resourceId);
			if (tv.type >= TypedValue.TYPE_FIRST_COLOR_INT && tv.type <= TypedValue.TYPE_LAST_COLOR_INT) return tv.data;
		}
		return Color.BLACK;
	}

	private static int resolveContrastOverlay(boolean dark, String contrastKey) {
		if (contrastKey == null) return 0;
		String key = contrastKey.trim().toLowerCase();

		if (PreferenceValues.THEME_CONTRAST_VARIANT_DISABLED.equals(key)) {
			return 0;
		}

		boolean high = key.contains("high");

		if (dark) {
			return high ? R.style.ThemeOverlay_App_Dark_HighContrast
					: R.style.ThemeOverlay_App_Dark_MediumContrast;
		} else {
			return high ? R.style.ThemeOverlay_App_Light_HighContrast
					: R.style.ThemeOverlay_App_Light_MediumContrast;
		}
	}

	private static int resolveAmoledOverlay(boolean amoled, boolean dark, String amoledCard) {
		if (!amoled || !dark) return 0;
		if (PreferenceValues.CARD_STYLE_AMOLED_FILLED.equals(amoledCard)) {
			return R.style.ThemeOverlay_App_Dark_AmoledLite;
		} else if ("outlined_card_style".equals(amoledCard)) {
			return R.style.ThemeOverlay_App_Dark_AmoledFull;
		}
		return 0;
	}

	/**
	 * Checks if current theme is a dark theme or not
	 * @param appContext a Context to pass, has to be ApplicationContext
	 */
	public static boolean isDarkTheme(Context appContext) {
		return new SharedPreferencesManager(appContext).retrieveBoolean(PreferenceKeys.KEY_PREF_DARK_THEME, PreferenceDefaults.DARK_THEME);
	}

	/**
	 * Gets the color resource from the attr resource ID
	 * @param context a Context to pass, used to get the Theme object
	 * @param attrResId an attribute resource ID to resolve into a Color
	 */
	public static int getThemeColor(Context context, int attrResId) {
		TypedValue typedValue = new TypedValue();
		Resources.Theme theme = context.getTheme();
		boolean found = theme.resolveAttribute(attrResId, typedValue, true);
		if (!found) {
			return Color.BLACK;
		}
		if (typedValue.resourceId != 0) {
			return ContextCompat.getColor(context, typedValue.resourceId);
		} else {
			return typedValue.data;
		}
	}

	public static boolean isContrastOverlay(Context appContext) {
		String contrastKey = new SharedPreferencesManager(appContext)
				.retrieveString(PreferenceKeys.KEY_PREF_THEME_CONTRAST_VARIANT, PreferenceDefaults.THEME_CONTRAST_VARIANT);
        return !contrastKey.equals(PreferenceValues.THEME_CONTRAST_VARIANT_DISABLED);
    }
}
