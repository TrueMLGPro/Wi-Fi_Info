package com.truemlgpro.wifiinfo.interfaces;

public interface PreferenceDefaults {
	boolean DARK_THEME = true;
	boolean AMOLED_THEME = false;
	boolean MONET_THEME = false;
	String CARD_STYLE_AMOLED = PreferenceValues.CARD_STYLE_AMOLED_FILLED;
	String THEME_CONTRAST_VARIANT = PreferenceValues.THEME_CONTRAST_VARIANT_DISABLED;
	String APP_LANGUAGE = PreferenceValues.APP_LANGUAGE_DEFAULT;
	boolean KEEP_SCREEN_ON = false;
	String CARD_UPDATE_INTERVAL = "1000";
	boolean START_ON_BOOT = false;
	boolean SHOW_NOTIFICATION = true;
	String NOTIFICATION_UPDATE_INTERVAL = "1000";
	boolean COLORIZE_NOTIFICATION = false;
	boolean VISUALIZE_SIGNAL_STRENGTH = false;
	boolean SERVICE_SCREEN_STATE = false;
	boolean NEVER_SHOW_GEO_DIALOG = false;
	boolean NEVER_SHOW_PERMISSION_REQ_DIALOG = false;
	int GPS_DENIAL_COUNT = 0;
}
