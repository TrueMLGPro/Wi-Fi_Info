package com.truemlgpro.wifiinfo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import br.com.bloder.magic.view.MagicButton;
import me.anwarshahriar.calligrapher.Calligrapher;

public class DevInfoActivity extends AppCompatActivity
{

	private Toolbar toolbar;
	private TextView alt_ds_txt;
	private TextView alt_gh_txt;
	private TextView alt_pr_txt;
	private MagicButton magic_ds;
	private MagicButton magic_gh;
	private MagicButton magic_pr;
	private TextView txt_app_ver;
	private TextView txt_android_ver;
	private TextView txt_sdk_ver;
	private TextView txt_device_brand;
	private TextView txt_product_name;
	private TextView txt_device_model;
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
		setContentView(R.layout.dev_info_activity);
		
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		alt_ds_txt = (TextView) findViewById(R.id.alt_ds_textview);
		alt_gh_txt = (TextView) findViewById(R.id.alt_gh_textview);
		alt_pr_txt = (TextView) findViewById(R.id.alt_pr_textview);
		magic_ds = (MagicButton) findViewById(R.id.magic_button1);
		magic_gh = (MagicButton) findViewById(R.id.magic_button2);
		magic_pr = (MagicButton) findViewById(R.id.magic_button3);
		txt_app_ver = (TextView) findViewById(R.id.txt_app_ver);
		txt_android_ver = (TextView) findViewById(R.id.txt_android_ver);
		txt_sdk_ver = (TextView) findViewById(R.id.txt_sdk_ver);
		txt_device_brand = (TextView) findViewById(R.id.txt_device_brand);
		txt_product_name = (TextView) findViewById(R.id.txt_product_name);
		txt_device_model = (TextView) findViewById(R.id.txt_device_model);
		
		Calligrapher calligrapher = new Calligrapher(this);
		String font = new SharedPreferencesManager(getApplicationContext()).retrieveString(SettingsActivity.KEY_PREF_APP_FONT, MainActivity.appFont);
		calligrapher.setFont(this, font, true);
		
		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setElevation(20);
		
		toolbar.setNavigationOnClickListener(v -> {
			// Back button pressed
			finish();
		});
		
		try {
			PackageInfo pi = this.getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		
		txt_app_ver.setText("App Version: " + version);
		txt_android_ver.setText("Android Version: " + Build.VERSION.RELEASE);
		txt_sdk_ver.setText("Android SDK Version: " + Build.VERSION.SDK_INT);
		txt_device_brand.setText("Device Brand: " + Build.BRAND);
		txt_product_name.setText("Product Name: " + Build.PRODUCT);
		txt_device_model.setText("Device Model: " + Build.MODEL);
		
		if (android.os.Build.VERSION.SDK_INT == 22) {
			alt_ds_txt.setVisibility(View.VISIBLE);
			alt_gh_txt.setVisibility(View.VISIBLE);
			alt_pr_txt.setVisibility(View.VISIBLE);
			initializeOnClickListenersAPI22();
		} else {
			magic_ds.setVisibility(View.VISIBLE);
			magic_gh.setVisibility(View.VISIBLE);
			magic_pr.setVisibility(View.VISIBLE);
			initializeOnClickListeners();
		}
		
	}
		
	public void initializeOnClickListenersAPI22() {
		alt_ds_txt.setOnClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Discord", "🐱️TrueMLGPro🌟#2674");
			cbm.setPrimaryClip(clip);

			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "🐱️TrueMLGPro🌟#2674", Toast.LENGTH_SHORT).show();
		});

		alt_gh_txt.setOnClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("GitHub", "TrueMLGPro/Wi-Fi_Info");
			cbm.setPrimaryClip(clip);

			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "TrueMLGPro/Wi-Fi_Info", Toast.LENGTH_SHORT).show();
		});
	
		alt_pr_txt.setOnClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Patreon", "TrueMLGPro");
			cbm.setPrimaryClip(clip);

			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "TrueMLGPro", Toast.LENGTH_SHORT).show();
		});
	}

	public void initializeOnClickListeners() {
		magic_ds.setMagicButtonClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Discord", "🐱️TrueMLGPro🌟#2674");
			cbm.setPrimaryClip(clip);

			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "🐱️TrueMLGPro🌟#2674", Toast.LENGTH_SHORT).show();
		});

		magic_gh.setMagicButtonClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("GitHub", "TrueMLGPro/Wi-Fi_Info");
			cbm.setPrimaryClip(clip);

			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "TrueMLGPro/Wi-Fi_Info", Toast.LENGTH_SHORT).show();
		});

		magic_pr.setMagicButtonClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Patreon", "TrueMLGPro");
			cbm.setPrimaryClip(clip);

			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "TrueMLGPro", Toast.LENGTH_SHORT).show();
		});
	}
}

