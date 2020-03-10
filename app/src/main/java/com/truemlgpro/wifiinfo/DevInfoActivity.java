package com.truemlgpro.wifiinfo;

import android.app.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import me.anwarshahriar.calligrapher.*;
import android.support.v4.widget.*;
import android.view.*;
import android.widget.*;

public class DevInfoActivity extends AppCompatActivity
{

	private Toolbar toolbar;
	private DrawerLayout mDrawerLayout;
	private TextView txt_app_ver;
	private TextView txt_android_ver;
	private TextView txt_sdk_ver;
	private TextView txt_device_brand;
	private TextView txt_product_name;
	private TextView txt_device_model;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dev_info_activity);
		
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
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
		
		txt_android_ver.setText("Android Version: " + Build.VERSION.RELEASE);
		txt_sdk_ver.setText("Android SDK Version: " + Build.VERSION.SDK_INT);
		txt_device_brand.setText("Device Brand: " + Build.BRAND);
		txt_product_name.setText("Product Name: " + Build.PRODUCT);
		txt_device_model.setText("Device Model: " + Build.MODEL);
	}
	
}
