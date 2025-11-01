package com.truemlgpro.wifiinfo.ui.transition;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.PathInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.ref.WeakReference;

public final class ThemeTransition {
	private static int sRevealCx = -1, sRevealCy = -1;
	private static int sPrevStatusBarColor = Integer.MIN_VALUE;
	private static int sPrevNavBarColor = Integer.MIN_VALUE;
	private static volatile boolean sInProgress = false;

	private ThemeTransition() {}

	public static boolean isInProgress() { return sInProgress; }
	static void markOverlayFinished() { sInProgress = false; }

	public static int peekPrevStatusBarColor() { return sPrevStatusBarColor; }
	public static int peekPrevNavBarColor() { return sPrevNavBarColor; }

	public static void startThemeReveal(@NonNull AppCompatActivity activity,
										@NonNull Runnable applyThemeAndRecreate) {
		sInProgress = true;
		sPrevStatusBarColor = activity.getWindow().getStatusBarColor();
		sPrevNavBarColor = activity.getWindow().getNavigationBarColor();

		captureWindowRGB565(activity, bmp -> {
			ThemeRevealActivity.sScreenshotRef = new WeakReference<>(bmp);

			View root = activity.getWindow().getDecorView();
			int cx = Math.max(1, root.getWidth()) / 2;
			int cy = Math.max(1, root.getHeight()) / 2;
			sRevealCx = cx; sRevealCy = cy;

			// Register "overlay is visible" receiver BEFORE starting overlay
			LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(activity);
			IntentFilter filter = new IntentFilter(ThemeRevealActivity.ACTION_OVERLAY_VISIBLE);
			BroadcastReceiver overlayVisible = new BroadcastReceiver() {
				boolean fired = false;
				@Override public void onReceive(Context c, Intent i) {
					if (fired) return; fired = true;
					lbm.unregisterReceiver(this);
					runOnMain(applyThemeAndRecreate);
				}
			};
			lbm.registerReceiver(overlayVisible, filter);

			// Safety fallback (in case overlay visible broadcast is missed)
			new Handler(Looper.getMainLooper()).postDelayed(() -> {
				try { lbm.unregisterReceiver(overlayVisible); } catch (Throwable ignore) {}
				runOnMain(applyThemeAndRecreate);
			}, 120L);

			// Start overlay
			Intent overlay = new Intent(activity, ThemeRevealActivity.class)
					.putExtra(ThemeRevealActivity.EXTRA_CX, cx)
					.putExtra(ThemeRevealActivity.EXTRA_CY, cy);
			activity.startActivity(overlay);
			activity.overridePendingTransition(0, 0);
		});
	}

	public static void startThemeRevealFrom(@NonNull AppCompatActivity activity,
											@NonNull View anchor,
											@NonNull Runnable applyThemeAndRecreate) {
		sInProgress = true;
		sPrevStatusBarColor = activity.getWindow().getStatusBarColor();
		sPrevNavBarColor = activity.getWindow().getNavigationBarColor();

		captureWindowRGB565(activity, bmp -> {
			ThemeRevealActivity.sScreenshotRef = new WeakReference<>(bmp);

			int[] loc = new int[2];
			anchor.getLocationOnScreen(loc);
			int cx = loc[0] + anchor.getWidth() / 2;
			int cy = loc[1] + anchor.getHeight() / 2;
			sRevealCx = cx; sRevealCy = cy;

			LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(activity);
			IntentFilter filter = new IntentFilter(ThemeRevealActivity.ACTION_OVERLAY_VISIBLE);
			BroadcastReceiver overlayVisible = new BroadcastReceiver() {
				boolean fired = false;
				@Override public void onReceive(Context c, Intent i) {
					if (fired) return; fired = true;
					lbm.unregisterReceiver(this);
					runOnMain(applyThemeAndRecreate);
				}
			};
			lbm.registerReceiver(overlayVisible, filter);
			new Handler(Looper.getMainLooper()).postDelayed(() -> {
				try { lbm.unregisterReceiver(overlayVisible); } catch (Throwable ignore) {}
				runOnMain(applyThemeAndRecreate);
			}, 120L);

			Intent overlay = new Intent(activity, ThemeRevealActivity.class)
					.putExtra(ThemeRevealActivity.EXTRA_CX, cx)
					.putExtra(ThemeRevealActivity.EXTRA_CY, cy);
			activity.startActivity(overlay);
			activity.overridePendingTransition(0, 0);
		});
	}

	// Call from the new MainActivity after first stable frame (+2 vsyncs)
	public static void notifyThemeReadyAfterFirstDraw(@NonNull AppCompatActivity activity, long durationMs) {
		final View content = activity.findViewById(android.R.id.content);
		content.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			boolean sent;
			@Override public boolean onPreDraw() {
				if (sent) return true;
				sent = true;
				content.getViewTreeObserver().removeOnPreDrawListener(this);

				content.post(() -> {
					Choreographer ch = Choreographer.getInstance();
					ch.postFrameCallback(ft -> ch.postFrameCallback(ft2 ->
							notifyThemeReadyAndAnimateBars(activity, durationMs)
					));
				});
				return true;
			}
		});
	}

	// Blend system bars from old -> new while starting the reveal
	public static void notifyThemeReadyAndAnimateBars(@NonNull AppCompatActivity activity, long durationMs) {
		int targetStatus = activity.getWindow().getStatusBarColor();
		int targetNav = activity.getWindow().getNavigationBarColor();
		final int startStatus = (sPrevStatusBarColor == Integer.MIN_VALUE) ? targetStatus : sPrevStatusBarColor;
		final int startNav = (sPrevNavBarColor == Integer.MIN_VALUE) ? targetNav : sPrevNavBarColor;

		if (startStatus != targetStatus) activity.getWindow().setStatusBarColor(startStatus);
		if (startNav != targetNav) activity.getWindow().setNavigationBarColor(startNav);

		ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);
		va.setDuration(durationMs <= 0 ? 360L : durationMs);
		va.setInterpolator(new PathInterpolator(0.4f, 0f, 0.2f, 1f));
		va.addUpdateListener(a -> {
			float t = (float) a.getAnimatedValue();
			if (startStatus != targetStatus) {
				activity.getWindow().setStatusBarColor(ColorUtils.blendARGB(startStatus, targetStatus, t));
			}
			if (startNav != targetNav) {
				activity.getWindow().setNavigationBarColor(ColorUtils.blendARGB(startNav, targetNav, t));
			}
		});
		va.start();

		int cx = sRevealCx, cy = sRevealCy;
		if (cx < 0 || cy < 0) {
			View root = activity.getWindow().getDecorView();
			cx = root.getWidth() / 2; cy = root.getHeight() / 2;
		}
		Intent i = new Intent(ThemeRevealActivity.ACTION_THEME_READY)
				.putExtra(ThemeRevealActivity.EXTRA_CX, cx)
				.putExtra(ThemeRevealActivity.EXTRA_CY, cy);
		LocalBroadcastManager.getInstance(activity).sendBroadcast(i);

		sRevealCx = sRevealCy = -1;
		sPrevStatusBarColor = sPrevNavBarColor = Integer.MIN_VALUE;
	}

	// Screenshot helpers (RGB_565)
	private interface BitmapCallback { void onBitmap(@NonNull Bitmap bmp); }

	private static void captureWindowRGB565(Activity act, @NonNull BitmapCallback cb) {
		View root = act.getWindow().getDecorView();
		int w = Math.max(1, root.getWidth());
		int h = Math.max(1, root.getHeight());

		if (Build.VERSION.SDK_INT >= 26) {
			Bitmap argb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			PixelCopy.request(act.getWindow(), argb, result -> {
				Bitmap out;
				if (result == PixelCopy.SUCCESS) {
					out = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
					new Canvas(out).drawBitmap(argb, 0f, 0f, null);
					argb.recycle();
				} else {
					out = drawFallbackRGB565(root, w, h);
				}
				cb.onBitmap(out);
			}, new Handler(Looper.getMainLooper()));
		} else {
			cb.onBitmap(drawFallbackRGB565(root, w, h));
		}
	}

	private static Bitmap drawFallbackRGB565(View root, int w, int h) {
		Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bmp);
		root.draw(c);
		return bmp;
	}

	private static void runOnMain(@NonNull Runnable r) {
		if (Looper.myLooper() == Looper.getMainLooper()) r.run();
		else new Handler(Looper.getMainLooper()).post(r);
	}
}