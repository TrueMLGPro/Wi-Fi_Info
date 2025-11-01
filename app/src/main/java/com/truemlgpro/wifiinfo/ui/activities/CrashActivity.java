package com.truemlgpro.wifiinfo.ui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.ui.InsetsController;
import com.truemlgpro.wifiinfo.utils.app.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.ui.LocaleManager;
import com.truemlgpro.wifiinfo.utils.ui.ThemeManager;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;

public class CrashActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());

		super.onCreate(savedInstanceState);
		WindowCompat.enableEdgeToEdge(getWindow());
		setContentView(R.layout.crash_activity);

		KeepScreenOnManager.init(getWindow(), getApplicationContext());

		MaterialButton restartButton = findViewById(R.id.restart_button);
		MaterialButton copyLogsButton = findViewById(R.id.copy_logs_button);
		MaterialTextView errorDetailsTextView = findViewById(R.id.error_details);

		final CaocConfig config = CustomActivityOnCrash.getConfigFromIntent(getIntent());

		if (config == null) {
			// This should never happen - Just finish the activity to avoid a recursive crash.
			finish();
			return;
		}

		if (config.isShowRestartButton() && config.getRestartActivityClass() != null) {
			restartButton.setText(R.string.crash_activity_restart_app);
			restartButton.setOnClickListener(v -> CustomActivityOnCrash.restartApplication(CrashActivity.this, config));
		} else {
			restartButton.setOnClickListener(v -> CustomActivityOnCrash.closeApplication(CrashActivity.this, config));
		}

		errorDetailsTextView.setText(CustomActivityOnCrash.getAllErrorDetailsFromIntent(CrashActivity.this, getIntent()));
		copyLogsButton.setOnClickListener(v -> copyErrorToClipboard());

		Integer defaultErrorActivityDrawableId = config.getErrorDrawable();
		ImageView errorImageView = findViewById(R.id.customactivityoncrash_error_activity_image);

		if (defaultErrorActivityDrawableId != null) {
			errorImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), defaultErrorActivityDrawableId, getTheme()));
		}

		initInsets();
	}

	private void initInsets() {
		RelativeLayout content_root = findViewById(R.id.content_root_crash);

		InsetsController.setInsets(
				content_root,
				new InsetsController.Config.Builder()
						.insetTypes(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout())
						.edges(InsetsController.EDGE_VERTICAL | InsetsController.EDGE_HORIZONTAL)
						.applyToPadding()
						.consume(false)
						.build()
		);
	}

	private void copyErrorToClipboard() {
		String errorInformation = CustomActivityOnCrash.getAllErrorDetailsFromIntent(CrashActivity.this, getIntent());

		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		if (clipboard != null) {
			ClipData clip = ClipData.newPlainText("Error information", errorInformation);
			clipboard.setPrimaryClip(clip);
			Toast.makeText(CrashActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
		}
	}
}