package com.truemlgpro.wifiinfo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import me.anwarshahriar.calligrapher.Calligrapher;

public class DiscordServersActivity extends AppCompatActivity
{

	private Toolbar toolbar;
	private Button true_mlg_pro_button;
	
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
		setContentView(R.layout.discord_servers_activity);
		
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		true_mlg_pro_button = (Button) findViewById(R.id.true_mlg_pro_button);
		
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
		
	}
	
	public void official_onClick(View v) {
		String url = "https://discord.gg/qxE2DFr";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}
	
}
