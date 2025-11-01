package com.truemlgpro.wifiinfo.ui.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.ui.InsetsController;
import com.truemlgpro.wifiinfo.utils.ui.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class ToolsFragment extends Fragment {
	private MaterialCardView cardview_cellular_ip;
	private MaterialCardView cardview_router_setup;
	private MaterialCardView cardview_ping;
	private MaterialCardView cardview_subnet_scanner;
	private MaterialCardView cardview_port_scanner;
	private MaterialCardView cardview_whois;
	private MaterialCardView cardview_dns_lookup;
	private MaterialCardView cardview_ssl_cert_viewer;

	private NavController navController;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tools, container, false);
		initViews(view);
		initOnClickListeners();

		navController = NavHostFragment.findNavController(this);
		applySteppedCardColors(view);
		return view;
	}

	private void initViews(View view) {
		AppBarLayout app_bar_layout = view.findViewById(R.id.appbarlayout_tools);
		NestedScrollView scroll_view = view.findViewById(R.id.cards_scrollview);
		cardview_cellular_ip = view.findViewById(R.id.cardview_cellular_data_ip);
		cardview_router_setup = view.findViewById(R.id.cardview_router_setup);
		cardview_ping = view.findViewById(R.id.cardview_ping_tool);
		cardview_subnet_scanner = view.findViewById(R.id.cardview_subnet_scanner);
		cardview_port_scanner = view.findViewById(R.id.cardview_port_scanner);
		cardview_whois = view.findViewById(R.id.cardview_whois_tool);
		cardview_dns_lookup = view.findViewById(R.id.cardview_dns_lookup);
		cardview_ssl_cert_viewer = view.findViewById(R.id.cardview_ssl_cert_viewer);

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
						.insetTypes(WindowInsetsCompat.Type.displayCutout())
						.edges(InsetsController.EDGE_HORIZONTAL)
						.applyToPadding()
						.consume(false)
						.build()
		);
	}

	private void initOnClickListeners() {
		cardview_cellular_ip.setOnClickListener(v -> navController.navigate(R.id.action_toolsFragment_to_cellularDataIPActivity));
		cardview_router_setup.setOnClickListener(v -> navController.navigate(R.id.action_toolsFragment_to_routerSetupActivity));
		cardview_ping.setOnClickListener(v -> navController.navigate(R.id.action_toolsFragment_to_pingToolActivity));
		cardview_subnet_scanner.setOnClickListener(v -> navController.navigate(R.id.action_toolsFragment_to_subnetScannerActivity));
		cardview_port_scanner.setOnClickListener(v -> navController.navigate(R.id.action_toolsFragment_to_portScannerActivity));
		cardview_whois.setOnClickListener(v -> navController.navigate(R.id.action_toolsFragment_to_whoIsToolActivity));
		cardview_dns_lookup.setOnClickListener(v -> navController.navigate(R.id.action_toolsFragment_to_DNSLookupActivity));
		cardview_ssl_cert_viewer.setOnClickListener(v -> navController.navigate(R.id.action_toolsFragment_to_sslCertificateViewerActivity));
	}

	private void applySteppedCardColors(View root) {
		MaterialCardView[] cards = new MaterialCardView[] {
				cardview_cellular_ip,
				cardview_router_setup,
				cardview_ping,
				cardview_subnet_scanner,
				cardview_port_scanner,
				cardview_whois,
				cardview_dns_lookup,
				cardview_ssl_cert_viewer,
		};

		MaterialCardView[] chips = new MaterialCardView[] {
				root.findViewById(R.id.cardview_bg_icon_cellular_data_ip),
				root.findViewById(R.id.cardview_bg_icon_router_setup),
				root.findViewById(R.id.cardview_bg_icon_ping_tool),
				root.findViewById(R.id.cardview_bg_icon_subnet_scanner),
				root.findViewById(R.id.cardview_bg_icon_port_scanner),
				root.findViewById(R.id.cardview_bg_icon_whois_tool),
				root.findViewById(R.id.cardview_bg_icon_dns_lookup),
				root.findViewById(R.id.cardview_bg_icon_ssl_cert_viewer),
		};

		ImageView[] icons = new ImageView[] {
				root.findViewById(R.id.icon_cellular_data_ip),
				root.findViewById(R.id.icon_device_details),
				root.findViewById(R.id.icon_ping_tool),
				root.findViewById(R.id.icon_subnet_scanner),
				root.findViewById(R.id.icon_port_scanner),
				root.findViewById(R.id.icon_whois_tool),
				root.findViewById(R.id.icon_dns_lookup),
				root.findViewById(R.id.icon_ssl_cert_viewer),
		};

		boolean isDark = ThemeManager.isDarkTheme(root.getContext());

		List<Integer> cardsToColorize = new ArrayList<>();
		for (int i = 0; i < cards.length; i++) {
			if (cards[i] != null) cardsToColorize.add(i);
		}

		int attrColorPrimary = androidx.appcompat.R.attr.colorPrimary;
		int attrColorPrimaryContainer = com.google.android.material.R.attr.colorPrimaryContainer;
		int attrColorBackground = com.google.android.material.R.attr.colorOnBackground;

		// Switch the colors depending on the theme
		int startBgTint = MaterialColors.getColor(root, isDark ? attrColorPrimary : attrColorPrimaryContainer);
		int endBgTint = MaterialColors.getColor(root, isDark ? attrColorPrimaryContainer : attrColorPrimary);

		int startIconTint = MaterialColors.getColor(root, isDark ? attrColorBackground : attrColorPrimary);
		int endIconTint = MaterialColors.getColor(root, isDark ? attrColorPrimary : attrColorBackground);

		int cardAmt = cardsToColorize.size();
		for (int j = 0; j < cardAmt; j++) {
			int i = cardsToColorize.get(j);

			float t = (cardAmt > 1) ? (float) j / (cardAmt - 1) : 0f;
			int stepColorBg = ColorUtils.blendARGB(startBgTint, endBgTint, t);
			ColorStateList cslBg = ColorStateList.valueOf(stepColorBg);

			int stepColorIcon = ColorUtils.blendARGB(startIconTint, endIconTint, t);
			ColorStateList cslIcon = ColorStateList.valueOf(stepColorIcon);

			cards[i].setCardBackgroundColor(cslBg);
			chips[i].setCardBackgroundColor(cslBg);
			if (!ThemeManager.isContrastOverlay(root.getContext().getApplicationContext())) {
				ImageViewCompat.setImageTintList(icons[i], cslIcon);
			}
		}
	}
}
