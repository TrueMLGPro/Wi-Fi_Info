package com.truemlgpro.wifiinfo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import me.anwarshahriar.calligrapher.Calligrapher;

public class SupportersActivity extends AppCompatActivity
{

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
		setContentView(R.layout.supporters_activity);

		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
		pab_text.setOnClickListener(v -> copyToClipboard("Pab#2631"));
		anyx_text.setOnClickListener(v -> copyToClipboard("Anyx#1040"));
		andrew_text.setOnClickListener(v -> copyToClipboard("andre btw.#3452"));
		rouge_text.setOnClickListener(v -> copyToClipboard("air conditioner#7639"));
		madcodez_text.setOnClickListener(v -> copyToClipboard("madcodez#1517"));
		asfi_text.setOnClickListener(v -> copyToClipboard("asfi#4034"));
		akebi_text.setOnClickListener(v -> copyToClipboard("Akebi7204#7204"));
		artem_text.setOnClickListener(v -> copyToClipboard("Apтем#8524"));
		terrin_text.setOnClickListener(v -> copyToClipboard("Terrin Tin#5462"));
		torneix_text.setOnClickListener(v -> copyToClipboard("bajsjägaremästaren#7593"));
		killbayne_text.setOnClickListener(v -> copyToClipboard("Killbayne#2294"));
	}

	private void copyToClipboard(String discord_name) {
		ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("Discord", discord_name);
		cbm.setPrimaryClip(clip);
		Toast.makeText(getBaseContext(), "Copied to Clipboard: " + discord_name, Toast.LENGTH_SHORT).show();
	}
}
