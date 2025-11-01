package com.truemlgpro.wifiinfo.ui.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Choreographer;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.truemlgpro.wifiinfo.R;

import java.lang.ref.WeakReference;

public class ThemeRevealActivity extends AppCompatActivity {
	public static final String EXTRA_CX = "cx"; // screen coords
	public static final String EXTRA_CY = "cy";
	public static final String ACTION_THEME_READY = "com.truemlgpro.wifiinfo.ACTION_THEME_READY";
	public static final String ACTION_OVERLAY_VISIBLE = "com.truemlgpro.wifiinfo.ACTION_OVERLAY_VISIBLE";

	static WeakReference<Bitmap> sScreenshotRef;

	private ImageView screenshotView;
	private int fallbackCx = -1, fallbackCy = -1;
	private boolean revealStarted = false;
	private LocalBroadcastManager lbm;

	private final BroadcastReceiver readyReceiver = new BroadcastReceiver() {
		@Override public void onReceive(Context context, Intent intent) {
			int cx = intent.getIntExtra(EXTRA_CX, fallbackCx);
			int cy = intent.getIntExtra(EXTRA_CY, fallbackCy);
			if (revealStarted) return;
			revealStarted = true;

			startCircularConceal(cx, cy);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(0, 0);
		super.onCreate(savedInstanceState);

		// Ensure overlay aligns with captured window
		WindowCompat.enableEdgeToEdge(getWindow());

		setContentView(R.layout.theme_reveal_activity);
		screenshotView = findViewById(R.id.screenshot);
		screenshotView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		screenshotView.setScaleType(ImageView.ScaleType.FIT_XY);

		Bitmap bmp = sScreenshotRef != null ? sScreenshotRef.get() : null;
		if (bmp == null) { finishClean(); return; }
		screenshotView.setImageBitmap(bmp);

		fallbackCx = getIntent().getIntExtra(EXTRA_CX, -1);
		fallbackCy = getIntent().getIntExtra(EXTRA_CY, -1);

		lbm = LocalBroadcastManager.getInstance(this);
		lbm.registerReceiver(readyReceiver, new IntentFilter(ACTION_THEME_READY));
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		// Tell ThemeTransition the overlay is fully visible; safe to recreate MainActivity now.
		LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_OVERLAY_VISIBLE));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try { if (lbm != null) lbm.unregisterReceiver(readyReceiver); } catch (Throwable ignore) {}
		if (sScreenshotRef != null) sScreenshotRef.clear();
	}

	private void startCircularConceal(int cxScreen, int cyScreen) {
		final int startCxScreen = cxScreen, startCyScreen = cyScreen;
		screenshotView.post(() -> {
			int w = screenshotView.getWidth(), h = screenshotView.getHeight();
			if (w == 0 || h == 0) { finishClean(); return; }

			// Convert screen coords -> view-local coords
			int[] loc = new int[2];
			screenshotView.getLocationOnScreen(loc);
			int cx = startCxScreen >= 0 ? startCxScreen - loc[0] : w / 2;
			int cy = startCyScreen >= 0 ? startCyScreen - loc[1] : h / 2;
			if (cx < 0 || cx > w) cx = w / 2;
			if (cy < 0 || cy > h) cy = h / 2;

			float maxR = (float) Math.hypot(Math.max(cx, w - cx), Math.max(cy, h - cy));

			Animator a = ViewAnimationUtils.createCircularReveal(screenshotView, cx, cy, maxR, 0f);
			a.setDuration(360L);
			a.setInterpolator(new PathInterpolator(0.4f, 0f, 0.2f, 1f));
			a.addListener(new AnimatorListenerAdapter() {
				@Override public void onAnimationEnd(Animator animation) {
					// Hide the image but keep the overlay alive for two frames to avoid last-frame flash
					screenshotView.setVisibility(View.INVISIBLE);
					Choreographer ch = Choreographer.getInstance();
					ch.postFrameCallback(ft -> ch.postFrameCallback(ft2 -> finishClean()));
				}
			});
			a.start();
		});
	}

	private void finishClean() {
		finish();
		overridePendingTransition(0, 0);
		ThemeTransition.markOverlayFinished();
	}
}
