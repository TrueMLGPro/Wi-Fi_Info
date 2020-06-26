package com.truemlgpro.wifiinfo;

import android.app.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.support.v4.widget.*;
import android.view.*;
import android.widget.*;
import me.anwarshahriar.calligrapher.*;
import br.com.bloder.magic.view.*;
import android.view.View.*;
import android.content.*;
import android.content.pm.*;

public class DevInfoActivity extends AppCompatActivity
{

	private Toolbar toolbar;
	private DrawerLayout mDrawerLayout;
	private MagicButton magic_ds;
	private MagicButton magic_yt;
	private MagicButton magic_git;
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
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dev_info_activity);
		
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		magic_ds = (MagicButton) findViewById(R.id.magic_button1);
		magic_yt = (MagicButton) findViewById(R.id.magic_button2);
		magic_git = (MagicButton) findViewById(R.id.magic_button3);
		txt_app_ver = (TextView) findViewById(R.id.txt_app_ver);
		txt_android_ver = (TextView) findViewById(R.id.txt_android_ver);
		txt_sdk_ver = (TextView) findViewById(R.id.txt_sdk_ver);
		txt_device_brand = (TextView) findViewById(R.id.txt_device_brand);
		txt_product_name = (TextView) findViewById(R.id.txt_product_name);
		txt_device_model = (TextView) findViewById(R.id.txt_device_model);
		
		Calligrapher calligrapher = new Calligrapher(this);
		calligrapher.setFont(this, "fonts/GoogleSans-Medium.ttf", true);
		
		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setElevation(20);
		
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Back button pressed
				finish();
			}
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
		
		magic_ds.setMagicButtonClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("Discord", "🐱️TrueMLGPro🌟#3121");
				cbm.setPrimaryClip(clip);
				
				Toast.makeText(getBaseContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
			}
		});
		
		magic_yt.setMagicButtonClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("Youtube", "TheAwesomePlay :D");
				cbm.setPrimaryClip(clip);
				
				Toast.makeText(getBaseContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
			}
		});
		
		magic_git.setMagicButtonClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("GitHub", "TrueMLGPro/Wi-Fi_Info");
				cbm.setPrimaryClip(clip);
				
				Toast.makeText(getBaseContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
}
