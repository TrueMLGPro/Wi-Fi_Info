package com.truemlgpro.wifiinfo;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import me.anwarshahriar.calligrapher.Calligrapher;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity
{

	private TextView splash_text;
	private TextView dev_name;
	private TextView version_txt;
	private ImageView splash_logo;
	private String version;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Boolean keyTheme = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_SWITCH, MainActivity.darkMode);
		Boolean keyAmoledTheme = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_AMOLED_CHECK, MainActivity.amoledMode);

		if (keyTheme) {
			setTheme(R.style.DarkTheme);
		}

		if (keyAmoledTheme) {
			if (keyTheme) {
				setTheme(R.style.AmoledDarkTheme);
			}
		}

		if (!keyTheme) {
			setTheme(R.style.LightTheme);
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_activity);
		
		try {
			PackageInfo pi = this.getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		
		splash_text = (TextView) findViewById(R.id.splash_text);
		splash_logo = (ImageView) findViewById(R.id.splash_logo);
		dev_name = (TextView) findViewById(R.id.dev_name);
		version_txt = (TextView) findViewById(R.id.version_txt);
		
		version_txt.setText("v" + version);
		
		// Logo Animation
		
		ObjectAnimator.ofFloat(splash_logo, View.ALPHA, 0.0f, 1.0f).setDuration(2250).start();
		ObjectAnimator.ofFloat(splash_logo, View.SCALE_X, 0.0f, 0.75f).setDuration(1500).start();
		ObjectAnimator.ofFloat(splash_logo, View.SCALE_Y, 0.0f, 0.75f).setDuration(1500).start();
		
		// App Name Animation
		
		ObjectAnimator.ofFloat(splash_text, View.ALPHA, 0.0f, 1.0f).setDuration(2250).start();
		ObjectAnimator.ofFloat(splash_text, View.SCALE_X, 0.0f, 1.0f).setDuration(1500).start();
		ObjectAnimator.ofFloat(splash_text, View.SCALE_Y, 0.0f, 1.0f).setDuration(1500).start();
		
		// Developer Name Animation
		
		ObjectAnimator.ofFloat(dev_name, View.ALPHA, 0.0f, 1.0f).setDuration(2250).start();
		ObjectAnimator.ofFloat(dev_name, View.SCALE_X, 0.25f, 1.0f).setDuration(1500).start();
		ObjectAnimator.ofFloat(dev_name, View.SCALE_Y, 0.25f, 1.0f).setDuration(1500).start();
		
		// App Version Animation
		
		ObjectAnimator.ofFloat(version_txt, View.ALPHA, 0.0f, 1.0f).setDuration(2250).start();
		ObjectAnimator.ofFloat(version_txt, View.SCALE_X, 0.25f, 1.0f).setDuration(1500).start();
		ObjectAnimator.ofFloat(version_txt, View.SCALE_Y, 0.25f, 1.0f).setDuration(1500).start();
		
		Calligrapher calligrapher = new Calligrapher(this);
		String font = new SharedPreferencesManager(getApplicationContext()).retrieveString(SettingsActivity.KEY_PREF_APP_FONT, MainActivity.appFont);
		calligrapher.setFont(this, font, true);
		
		Thread splashThread = new Thread() {
			@Override
			public void run() {
				SystemClock.sleep(2500);
				Intent intent = new Intent(SplashActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
		};
		splashThread.start();
	}

	@Override
	public void onBackPressed()
	{
		if (android.os.Build.VERSION.SDK_INT < 29) {
			super.onBackPressed();
		}
	}
}
