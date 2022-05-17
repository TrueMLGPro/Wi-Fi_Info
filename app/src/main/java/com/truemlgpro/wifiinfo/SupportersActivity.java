package com.truemlgpro.wifiinfo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import me.anwarshahriar.calligrapher.Calligrapher;

public class SupportersActivity extends AppCompatActivity
{
	
	private Toolbar toolbar;
	private ScrollView scrollView;
	private TextView supporters_textview;
	private TextView pab_text;
	private TextView anyx_text;
	private TextView andrew_text;
	private TextView rouge_text;
	private TextView madcodez_text;
	private TextView reiven_text;
	private TextView artem_text;
	private TextView terrin_text;
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

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		supporters_textview = (TextView) findViewById(R.id.supporters_textview);
		pab_text = (TextView) findViewById(R.id.pab_text);
		anyx_text = (TextView) findViewById(R.id.anyx_text);
		andrew_text = (TextView) findViewById(R.id.andrew_text);
		rouge_text = (TextView) findViewById(R.id.rouge_text);
		madcodez_text = (TextView) findViewById(R.id.madcodez_text);
		reiven_text = (TextView) findViewById(R.id.reiven_text);
		artem_text = (TextView) findViewById(R.id.artem_text);
		terrin_text = (TextView) findViewById(R.id.terrin_text);
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
		pab_text.setOnClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Discord", "Pab#1234");
			cbm.setPrimaryClip(clip);

			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Pab#1234", Toast.LENGTH_SHORT).show();
		});

		anyx_text.setOnClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Discord", "Anyx#1040");
			cbm.setPrimaryClip(clip);

			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Anyx#1040", Toast.LENGTH_SHORT).show();
		});

		andrew_text.setOnClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Discord", "andre btw.#3934");
			cbm.setPrimaryClip(clip);

			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "andre btw.#3934", Toast.LENGTH_SHORT).show();
		});

		rouge_text.setOnClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Discord", "[ ]#4556");
			cbm.setPrimaryClip(clip);

			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "[ ]#4556", Toast.LENGTH_SHORT).show();
		});

		madcodez_text.setOnClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Discord", "madcodez#1517");
			cbm.setPrimaryClip(clip);

			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "madcodez#1517", Toast.LENGTH_SHORT).show();
		});

		reiven_text.setOnClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Discord", "ReivenAlt#2501");
			cbm.setPrimaryClip(clip);

			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "ReivenAlt#2501", Toast.LENGTH_SHORT).show();
		});

		artem_text.setOnClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Discord", "Apтем#8524");
			cbm.setPrimaryClip(clip);

			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Apтем#8524", Toast.LENGTH_SHORT).show();
		});

		terrin_text.setOnClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Discord", "Terrin Tin#5462");
			cbm.setPrimaryClip(clip);

			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Terrin Tin#5462", Toast.LENGTH_SHORT).show();
		});

		killbayne_text.setOnClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Discord", "Killbayne#6969");
			cbm.setPrimaryClip(clip);

			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Killbayne#6969", Toast.LENGTH_SHORT).show();
		});
	}
	
}
