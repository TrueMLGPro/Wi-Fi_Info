package com.truemlgpro.wifiinfo;

import android.app.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.support.v4.widget.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import me.anwarshahriar.calligrapher.*;

public class SupportersActivity extends AppCompatActivity
{
	
	private Toolbar toolbar;
	private ScrollView scrollView;
	private TextView supporters_textview;
	private TextView anyx_text;
	private TextView andrew_text;
	private TextView rouge_text;
	private TextView madcodez_text;
	private TextView never_text;
	private TextView artem_text;
	private TextView terrin_text;
	private TextView killbayne_text;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Boolean keyTheme = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_SWITCH, MainActivity.darkMode);
		Boolean keyAmoledTheme = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_AMOLED_CHECK, MainActivity.amoledMode);

		if (keyTheme == true) {
			setTheme(R.style.DarkTheme);
		}

		if (keyAmoledTheme == true) {
			if (keyTheme == true) {
				setTheme(R.style.AmoledDarkTheme);
			}
		}

		if (keyTheme == false) {
			setTheme(R.style.LightTheme);
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.supporters_activity);

		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		supporters_textview = (TextView) findViewById(R.id.supporters_textview);
		anyx_text = (TextView) findViewById(R.id.anyx_text);
		andrew_text = (TextView) findViewById(R.id.andrew_text);
		rouge_text = (TextView) findViewById(R.id.rouge_text);
		madcodez_text = (TextView) findViewById(R.id.madcodez_text);
		never_text = (TextView) findViewById(R.id.never_text);
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

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Back button pressed
					finish();
				}
		});
		
		initializeOnClickListeners();
	}
	
	public void initializeOnClickListeners() {
		anyx_text.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("Discord", "Anyx#9407");
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Anyx#9407", Toast.LENGTH_SHORT).show();
				}
			});

		andrew_text.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("Discord", "GW Andrew#3934");
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "GW Andrew#3934", Toast.LENGTH_SHORT).show();
				}
			});

		rouge_text.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("Discord", "Rouge#4556");
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Rouge#4556", Toast.LENGTH_SHORT).show();
				}
			});
		madcodez_text.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("Discord", "madcodez#1517");
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "madcodez#1517", Toast.LENGTH_SHORT).show();
				}
			});

		never_text.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("Discord", "NeverRzAltNutsss#0141");
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "NeverRzAltNutsss#0141", Toast.LENGTH_SHORT).show();
				}
			});

		artem_text.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("Discord", "Apтем#8524");
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Apтем#8524", Toast.LENGTH_SHORT).show();
				}
			});
		terrin_text.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("Discord", "Terrin Tin#5462");
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Terrin Tin#5462", Toast.LENGTH_SHORT).show();
				}
			});

		killbayne_text.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("Discord", "Killbayne#0076");
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Killbayne#0076", Toast.LENGTH_SHORT).show();
				}
			});
	}
	
}
