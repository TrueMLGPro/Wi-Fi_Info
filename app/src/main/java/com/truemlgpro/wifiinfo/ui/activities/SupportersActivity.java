package com.truemlgpro.wifiinfo.ui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textview.MaterialTextView;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.app.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.ui.InsetsController;
import com.truemlgpro.wifiinfo.utils.ui.LocaleManager;
import com.truemlgpro.wifiinfo.utils.ui.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class SupportersActivity extends AppCompatActivity {
	private MaterialTextView pab_text;
	private MaterialTextView anyx_text;
	private MaterialTextView andrew_text;
	private MaterialTextView rouge_text;
	private MaterialTextView madcodez_text;
	private MaterialTextView asfi_text;
	private MaterialTextView akebi_text;
	private MaterialTextView artem_text;
	private MaterialTextView terrin_text;
	private MaterialTextView torneix_text;
	private MaterialTextView ognjen28a_text;
	private MaterialTextView killbayne_text;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());

		super.onCreate(savedInstanceState);
		WindowCompat.enableEdgeToEdge(getWindow());
		setContentView(R.layout.supporters_activity);

		MaterialToolbar toolbar = findViewById(R.id.toolbar);
		pab_text = findViewById(R.id.pab_text);
		anyx_text = findViewById(R.id.anyx_text);
		andrew_text = findViewById(R.id.andrew_text);
		rouge_text = findViewById(R.id.rouge_text);
		madcodez_text = findViewById(R.id.madcodez_text);
		asfi_text = findViewById(R.id.asfi_text);
		akebi_text = findViewById(R.id.akebi_text);
		artem_text = findViewById(R.id.artem_text);
		terrin_text = findViewById(R.id.terrin_text);
		torneix_text = findViewById(R.id.torneix_text);
		ognjen28a_text = findViewById(R.id.ognjen28a_text);
		killbayne_text = findViewById(R.id.killbayne_text);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(true);

		KeepScreenOnManager.init(getWindow(), getApplicationContext());

		toolbar.setNavigationOnClickListener(v -> {
			finish();
		});

		if (ThemeManager.isContrastOverlay(getApplicationContext())) initTextColors();
		initializeOnClickListeners();

		initInsets();
	}

	private void initTextColors() {
		MaterialTextView[] textviews = new MaterialTextView[] {
				pab_text,
				anyx_text,
				andrew_text,
				rouge_text,
				madcodez_text,
				asfi_text,
				akebi_text,
				artem_text,
				terrin_text,
				torneix_text,
				ognjen28a_text,
				killbayne_text,
		};

		boolean darkTheme = ThemeManager.isDarkTheme(getApplicationContext());
		int textColor = darkTheme ?
				getResources().getColor(android.R.color.white)
				: getResources().getColor(android.R.color.black);

		List<Integer> textviewsToColorize = new ArrayList<>();
		for (int i = 0; i < textviews.length; i++) {
			if (textviews[i] != null) textviewsToColorize.add(i);
		}

		for (int j = 0; j < textviewsToColorize.size(); j++) {
			int i = textviewsToColorize.get(j);

			textviews[i].setTextColor(textColor);
		}
	}

	private void initializeOnClickListeners() {
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
		ognjen28a_text.setOnClickListener(v -> copyToClipboard(getString(R.string.supporter_spimbili)));
		killbayne_text.setOnClickListener(v -> copyToClipboard(getString(R.string.supporter_killbayne)));
	}

	private void copyToClipboard(String discordName) {
		ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("Discord", discordName);
		cbm.setPrimaryClip(clip);
		Toast.makeText(getBaseContext(), "Copied to Clipboard: " + discordName, Toast.LENGTH_SHORT).show();
	}

	private void initInsets() {
		AppBarLayout app_bar_layout = findViewById(R.id.appbarlayout_supporters);
		NestedScrollView scroll_view = findViewById(R.id.scrollView);

		InsetsController.setInsets(
				app_bar_layout,
				new InsetsController.Config.Builder()
						.insetTypes(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout())
						.edges(InsetsController.EDGE_TOP | InsetsController.EDGE_HORIZONTAL)
						.applyToPadding()
						.consume(false)
						.build()
		);

		InsetsController.setInsets(
				scroll_view,
				new InsetsController.Config.Builder()
						.insetTypes(WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.displayCutout())
						.edges(InsetsController.EDGE_BOTTOM | InsetsController.EDGE_HORIZONTAL)
						.applyToPadding()
						.consume(false)
						.build()
		);
	}
}
