package com.truemlgpro.wifiinfo.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.truemlgpro.wifiinfo.utils.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.SharedPreferencesManager;
import com.truemlgpro.wifiinfo.utils.ThemeManager;

import me.anwarshahriar.calligrapher.Calligrapher;

public class SupportersActivity extends AppCompatActivity {
	private TextView pab_text;
	private TextView anyx_text;
	private TextView andrew_text;
	private TextView rouge_text;
	private TextView madcodez_text;
	private TextView asfi_text;
	private TextView akebi_text;
	private TextView artem_text;
	private TextView terrin_text;
	private TextView torneix_text;
	private TextView killbayne_text;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.supporters_activity);

		KeepScreenOnManager.init(getWindow(), getApplicationContext());

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		pab_text = (TextView) findViewById(R.id.pab_text);
		anyx_text = (TextView) findViewById(R.id.anyx_text);
		andrew_text = (TextView) findViewById(R.id.andrew_text);
		rouge_text = (TextView) findViewById(R.id.rouge_text);
		madcodez_text = (TextView) findViewById(R.id.madcodez_text);
		asfi_text = (TextView) findViewById(R.id.asfi_text);
		akebi_text = (TextView) findViewById(R.id.akebi_text);
		artem_text = (TextView) findViewById(R.id.artem_text);
		terrin_text = (TextView) findViewById(R.id.terrin_text);
		torneix_text = (TextView) findViewById(R.id.torneix_text);
		killbayne_text = (TextView) findViewById(R.id.killbayne_text);

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

		initializeOnClickListeners();
	}

	public void initializeOnClickListeners() {
		pab_text.setOnClickListener(v -> copyToClipboard(getString(R.string.supporter_pab)));
		anyx_text.setOnClickListener(v -> copyToClipboard(getString(R.string.supporter_anyx)));
		andrew_text.setOnClickListener(v -> copyToClipboard(getString(R.string.supporter_andrebtw)));
		rouge_text.setOnClickListener(v -> copyToClipboard(getString(R.string.supporter_air_conditioner)));
		madcodez_text.setOnClickListener(v -> copyToClipboard(getString(R.string.supporter_madcodez)));
		asfi_text.setOnClickListener(v -> copyToClipboard(getString(R.string.supporter_asfi)));
		akebi_text.setOnClickListener(v -> copyToClipboard(getString(R.string.supporter_akebi)));
		artem_text.setOnClickListener(v -> copyToClipboard(getString(R.string.supporter_artem)));
		terrin_text.setOnClickListener(v -> copyToClipboard(getString(R.string.supporter_terrin_tin)));
		torneix_text.setOnClickListener(v -> copyToClipboard(getString(R.string.supporter_torneix)));
		killbayne_text.setOnClickListener(v -> copyToClipboard(getString(R.string.supporter_killbayne)));
	}

	private void copyToClipboard(String discordName) {
		ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("Discord", discordName);
		cbm.setPrimaryClip(clip);
		Toast.makeText(getBaseContext(), "Copied to Clipboard: " + discordName, Toast.LENGTH_SHORT).show();
	}
}
