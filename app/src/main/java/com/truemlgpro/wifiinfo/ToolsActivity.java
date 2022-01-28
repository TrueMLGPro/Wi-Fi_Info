package com.truemlgpro.wifiinfo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import me.anwarshahriar.calligrapher.Calligrapher;

public class ToolsActivity extends AppCompatActivity
{

	private Toolbar toolbar;
	private CardView cardview_1;
	private CardView cardview_2;
	private CardView cardview_3;
	private CardView cardview_4;
	private CardView cardview_5;
	private CardView cardview_6;
	private CardView cardview_7;
	private CardView cardview_8;

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
        setContentView(R.layout.tools_activity);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		cardview_1 = (CardView) findViewById(R.id.cardview_1);
		cardview_2 = (CardView) findViewById(R.id.cardview_2);
		cardview_3 = (CardView) findViewById(R.id.cardview_3);
		cardview_4 = (CardView) findViewById(R.id.cardview_4);
		cardview_5 = (CardView) findViewById(R.id.cardview_5);
		cardview_6 = (CardView) findViewById(R.id.cardview_6);
		cardview_7 = (CardView) findViewById(R.id.cardview_7);
		cardview_8 = (CardView) findViewById(R.id.cardview_8);
		
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		Calligrapher calligrapher = new Calligrapher(this);
		String font = new SharedPreferencesManager(getApplicationContext()).retrieveString(SettingsActivity.KEY_PREF_APP_FONT, MainActivity.appFont);
		calligrapher.setFont(this, font, true);

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

		cardview_1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					Intent url_to_ip_intent = new Intent(ToolsActivity.this, URLtoIPActivity.class);
					startActivity(url_to_ip_intent);
				}
		});

		cardview_2.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					Intent cell_data_public_ip_intent = new Intent(ToolsActivity.this, CellularDataIPActivity.class);
					startActivity(cell_data_public_ip_intent);
				}
		});

		cardview_3.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					Intent router_setup_intent = new Intent(ToolsActivity.this, RouterSetupActivity.class);
					startActivity(router_setup_intent);
				}
		});
		
		cardview_4.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					Intent ping_intent = new Intent(ToolsActivity.this, PingActivity.class);
					startActivity(ping_intent);
				}
		});
		
		cardview_5.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					Intent lan_devices_scanner_intent = new Intent(ToolsActivity.this, LANDevicesScannerActivity.class);
					startActivity(lan_devices_scanner_intent);
				}
		});
		
		cardview_6.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					Intent port_scanner_intent = new Intent(ToolsActivity.this, PortScannerActivity.class);
					startActivity(port_scanner_intent);
				}
		});

		cardview_7.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				Intent whois_tool_intent = new Intent(ToolsActivity.this, WhoIsToolActivity.class);
				startActivity(whois_tool_intent);
			}
		});

		cardview_8.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				Intent dns_resolver_tool_intent = new Intent(ToolsActivity.this, DNSLookupActivity.class);
				startActivity(dns_resolver_tool_intent);
			}
		});
    }
}
