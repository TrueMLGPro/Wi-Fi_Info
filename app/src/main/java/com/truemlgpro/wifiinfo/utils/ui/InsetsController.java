package com.truemlgpro.wifiinfo.utils.ui;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class InsetsController {
	private InsetsController() {}
	// Edge flags
	public static final int EDGE_LEFT = 1;
	public static final int EDGE_TOP = 1 << 1;
	public static final int EDGE_RIGHT = 1 << 2;
	public static final int EDGE_BOTTOM = 1 << 3;
	public static final int EDGE_HORIZONTAL = EDGE_LEFT | EDGE_RIGHT;
	public static final int EDGE_VERTICAL = EDGE_TOP | EDGE_BOTTOM;
	public static final int EDGE_ALL = EDGE_HORIZONTAL | EDGE_VERTICAL;

	@IntDef(flag = true, value = {EDGE_LEFT, EDGE_TOP, EDGE_RIGHT, EDGE_BOTTOM})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Edges {}

	public static final class Config {
		public final int insetTypes;
		@Edges public final int edges;
		public final boolean applyToMargin; // true = margin, false = padding
		public final boolean consumeInsets;
		public final boolean ignoreVisibility; // true => getInsetsIgnoringVisibility()
		public final float multiply; // scale the insets (e.g., 1f, 0.5f)
		public final Rect extra; // additional px to add per side

		private Config(Builder b) {
			this.insetTypes = b.insetTypes;
			this.edges = b.edges;
			this.applyToMargin = b.applyToMargin;
			this.consumeInsets = b.consumeInsets;
			this.ignoreVisibility = b.ignoreVisibility;
			this.multiply = b.multiply;
			this.extra = new Rect(b.extra);
		}

		public static final class Builder {
			private int insetTypes = WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout();
			@Edges private int edges = EDGE_ALL;
			private boolean applyToMargin = true;
			private boolean consumeInsets = true;
			private boolean ignoreVisibility = false;
			private float multiply = 1f;
			private final Rect extra = new Rect(0, 0, 0, 0);

			public Builder insetTypes(int insetTypes) {
				this.insetTypes = insetTypes; return this;
			}

			public Builder edges(@Edges int edges) {
				this.edges = edges; return this;
			}

			public Builder applyToMargin() {
				this.applyToMargin = true; return this;
			}

			public Builder applyToPadding() {
				this.applyToMargin = false; return this;
			}

			public Builder consume(boolean consume) {
				this.consumeInsets = consume; return this;
			}

			public Builder ignoreVisibility(boolean ignore) {
				this.ignoreVisibility = ignore; return this;
			}

			public Builder multiply(float factor) {
				this.multiply = factor; return this;
			}

			public Builder extra(Rect extra) {
				if (extra != null) this.extra.set(extra); return this;
			}

			public Builder extra(int left, int top, int right, int bottom) {
				this.extra.set(left, top, right, bottom); return this;
			}

			public Config build() {
				return new Config(this);
			}
		}
	}

	public static void setInsets(View view) {
		setInsets(view, new Config.Builder().applyToMargin().edges(EDGE_ALL).consume(true).build());
	}

	public static void setInsets(View view, Config config) {
		final int basePaddingLeft = view.getPaddingLeft();
		final int basePaddingTop = view.getPaddingTop();
		final int basePaddingRight = view.getPaddingRight();
		final int basePaddingBottom = view.getPaddingBottom();

		int baseMarginLeft, baseMarginTop, baseMarginRight, baseMarginBottom;
		final ViewGroup.LayoutParams lp = view.getLayoutParams();
		if (lp instanceof ViewGroup.MarginLayoutParams mlp) {
			baseMarginLeft = mlp.leftMargin;
			baseMarginTop = mlp.topMargin;
			baseMarginRight = mlp.rightMargin;
			baseMarginBottom = mlp.bottomMargin;
		} else {
			baseMarginBottom = 0;
			baseMarginRight = 0;
			baseMarginTop = 0;
			baseMarginLeft = 0;
		}

		ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
			Insets raw = config.ignoreVisibility
					? windowInsets.getInsetsIgnoringVisibility(config.insetTypes)
					: windowInsets.getInsets(config.insetTypes);

			int leftInset = Math.round(raw.left * config.multiply) + config.extra.left;
			int topInset = Math.round(raw.top * config.multiply) + config.extra.top;
			int rightInset = Math.round(raw.right * config.multiply) + config.extra.right;
			int bottomInset = Math.round(raw.bottom * config.multiply) + config.extra.bottom;

			final boolean applyLeft = (config.edges & EDGE_LEFT) != 0;
			final boolean applyTop = (config.edges & EDGE_TOP) != 0;
			final boolean applyRight = (config.edges & EDGE_RIGHT) != 0;
			final boolean applyBottom = (config.edges & EDGE_BOTTOM) != 0;

			if (config.applyToMargin) {
				ViewGroup.LayoutParams params = v.getLayoutParams();
				if (params instanceof ViewGroup.MarginLayoutParams mlp) {
					mlp.leftMargin = applyLeft ? (baseMarginLeft + leftInset) : baseMarginLeft;
					mlp.topMargin = applyTop ? (baseMarginTop + topInset) : baseMarginTop;
					mlp.rightMargin = applyRight ? (baseMarginRight + rightInset) : baseMarginRight;
					mlp.bottomMargin = applyBottom ? (baseMarginBottom + bottomInset) : baseMarginBottom;
					v.setLayoutParams(mlp);
				}
			} else {
				int pl = applyLeft ? (basePaddingLeft + leftInset) : basePaddingLeft;
				int pt = applyTop ? (basePaddingTop + topInset) : basePaddingTop;
				int pr = applyRight ? (basePaddingRight + rightInset) : basePaddingRight;
				int pb = applyBottom ? (basePaddingBottom + bottomInset) : basePaddingBottom;
				v.setPadding(pl, pt, pr, pb);
			}

			return config.consumeInsets ? WindowInsetsCompat.CONSUMED : windowInsets;
		});

		requestApplyInsetsWhenAttached(view);
	}

	private static void requestApplyInsetsWhenAttached(View view) {
		if (view.isAttachedToWindow()) {
			ViewCompat.requestApplyInsets(view);
		} else {
			view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
				@Override public void onViewAttachedToWindow(View v) {
					v.removeOnAttachStateChangeListener(this);
					ViewCompat.requestApplyInsets(v);
				}
				@Override public void onViewDetachedFromWindow(View v) {}
			});
		}
	}
}