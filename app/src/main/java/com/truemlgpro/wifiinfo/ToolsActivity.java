package com.truemlgpro.wifiinfo;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import me.anwarshahriar.calligrapher.Calligrapher;

public class ToolsActivity extends AppCompatActivity
{
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

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		CardView cardview_url_to_ip = (CardView) findViewById(R.id.cardview_1);
		CardView cardview_cellular_ip = (CardView) findViewById(R.id.cardview_2);
		CardView cardview_router_setup = (CardView) findViewById(R.id.cardview_3);
		CardView cardview_ping = (CardView) findViewById(R.id.cardview_4);
		CardView cardview_lan_devices_scanner = (CardView) findViewById(R.id.cardview_5);
		CardView cardview_port_scanner = (CardView) findViewById(R.id.cardview_6);
		CardView cardview_whois = (CardView) findViewById(R.id.cardview_7);
		CardView cardview_dns_lookup = (CardView) findViewById(R.id.cardview_8);
		
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
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

		cardview_url_to_ip.setOnClickListener(v -> {
			Intent url_to_ip_intent = new Intent(ToolsActivity.this, URLtoIPActivity.class);
			startActivity(url_to_ip_intent);
		});

		cardview_cellular_ip.setOnClickListener(v -> {
			Intent cell_data_public_ip_intent = new Intent(ToolsActivity.this, CellularDataIPActivity.class);
			startActivity(cell_data_public_ip_intent);
		});

		cardview_router_setup.setOnClickListener(v -> {
			Intent router_setup_intent = new Intent(ToolsActivity.this, RouterSetupActivity.class);
			startActivity(router_setup_intent);
		});
		
		cardview_ping.setOnClickListener(v -> {
			Intent ping_intent = new Intent(ToolsActivity.this, PingActivity.class);
			startActivity(ping_intent);
		});
		
		cardview_lan_devices_scanner.setOnClickListener(v -> {
			Intent lan_devices_scanner_intent = new Intent(ToolsActivity.this, LANDevicesScannerActivity.class);
			startActivity(lan_devices_scanner_intent);
		});
		
		cardview_port_scanner.setOnClickListener(v -> {
			Intent port_scanner_intent = new Intent(ToolsActivity.this, PortScannerActivity.class);
			startActivity(port_scanner_intent);
		});

		cardview_whois.setOnClickListener(v -> {
			Intent whois_tool_intent = new Intent(ToolsActivity.this, WhoIsToolActivity.class);
			startActivity(whois_tool_intent);
		});

		cardview_dns_lookup.setOnClickListener(v -> {
			Intent dns_lookup_tool_intent = new Intent(ToolsActivity.this, DNSLookupActivity.class);
			startActivity(dns_lookup_tool_intent);
		});
    }
}
