package com.truemlgpro.wifiinfo.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.FontManager;
import com.truemlgpro.wifiinfo.utils.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.LocaleManager;
import com.truemlgpro.wifiinfo.utils.ThemeManager;

public class SettingsActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

		KeepScreenOnManager.init(getWindow(), getApplicationContext());
		FontManager.init(this, getApplicationContext(), true);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setElevation(20);
		actionbar.setTitle(getResources().getString(R.string.settings));

		toolbar.setNavigationOnClickListener(v -> {
			// Back button pressed
			restartActivity();
		});

		getSupportFragmentManager().beginTransaction()
			.replace(R.id.content_frame, new SettingsFragment())
			.commit();
	}

	private void restartActivity() {
		Intent mainActivityIntent = new Intent(SettingsActivity.this, MainActivity.class);
		mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		overridePendingTransition(0, 0);
		startActivity(mainActivityIntent);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		restartActivity();
	}
}
