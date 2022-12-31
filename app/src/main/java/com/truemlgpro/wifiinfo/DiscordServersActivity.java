package com.truemlgpro.wifiinfo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import me.anwarshahriar.calligrapher.Calligrapher;

public class DiscordServersActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		new ThemeManager().initializeThemes(this, getApplicationContext());
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discord_servers_activity);
		
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		
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
	}
	
	public void official_onClick(View v) {
		String url = "https://discord.gg/qxE2DFr";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}
}
