package com.truemlgpro.wifiinfo.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.truemlgpro.wifiinfo.utils.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.SharedPreferencesManager;
import com.truemlgpro.wifiinfo.utils.ThemeManager;

import me.anwarshahriar.calligrapher.Calligrapher;

public class ToolsActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.tools_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		CardView cardview_cellular_ip = (CardView) findViewById(R.id.cardview_cellular_data_ip);
		CardView cardview_router_setup = (CardView) findViewById(R.id.cardview_router_setup);
		CardView cardview_ping = (CardView) findViewById(R.id.cardview_ping_tool);
		CardView cardview_subnet_scanner = (CardView) findViewById(R.id.cardview_subnet_scanner);
		CardView cardview_port_scanner = (CardView) findViewById(R.id.cardview_port_scanner);
		CardView cardview_whois = (CardView) findViewById(R.id.cardview_whois_tool);
		CardView cardview_dns_lookup = (CardView) findViewById(R.id.cardview_dns_lookup);

		KeepScreenOnManager.init(getWindow(), getApplicationContext());

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

		cardview_cellular_ip.setOnClickListener(v -> {
			Intent cell_data_public_ip_intent = new Intent(ToolsActivity.this, CellularDataIPActivity.class);
			startActivity(cell_data_public_ip_intent);
		});

		cardview_router_setup.setOnClickListener(v -> {
			Intent router_setup_intent = new Intent(ToolsActivity.this, RouterSetupActivity.class);
			startActivity(router_setup_intent);
		});

		cardview_ping.setOnClickListener(v -> {
			Intent ping_intent = new Intent(ToolsActivity.this, PingToolActivity.class);
			startActivity(ping_intent);
		});

		cardview_subnet_scanner.setOnClickListener(v -> {
			Intent subnet_scanner_intent = new Intent(ToolsActivity.this, SubnetScannerActivity.class);
			startActivity(subnet_scanner_intent);
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
			Intent dns_lookup_intent = new Intent(ToolsActivity.this, DNSLookupActivity.class);
			startActivity(dns_lookup_intent);
		});
	}
}
