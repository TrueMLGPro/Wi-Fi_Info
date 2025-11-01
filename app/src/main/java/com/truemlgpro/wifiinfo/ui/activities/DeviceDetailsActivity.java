package com.truemlgpro.wifiinfo.ui.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.stealthcopter.networktools.SubnetDevices;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.models.DeviceInfoParcel;
import com.truemlgpro.wifiinfo.utils.app.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.ui.InsetsController;
import com.truemlgpro.wifiinfo.utils.ui.LocaleManager;
import com.truemlgpro.wifiinfo.utils.ui.ThemeManager;

import java.util.Locale;

public class DeviceDetailsActivity extends AppCompatActivity {
	public static final String EXTRA_DETAILS = "extra_device_details";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());

		super.onCreate(savedInstanceState);
		WindowCompat.enableEdgeToEdge(getWindow());
		setContentView(R.layout.device_details_activity);

		DeviceInfoParcel info = getIntent().getParcelableExtra(EXTRA_DETAILS);
		if (info == null) {
			finish();
			return;
		}

		MaterialToolbar toolbar = findViewById(R.id.toolbar);
		ImageView ivIcon = findViewById(R.id.icon_device_details);
		TextView tvTitle = findViewById(R.id.textview_title);
		TextView tvIp = findViewById(R.id.textview_ip);
		TextView tvMac = findViewById(R.id.textview_mac);
		TextView tvVendor = findViewById(R.id.textview_vendor);
		TextView tvPing = findViewById(R.id.textview_ping);
		TextView tvType = findViewById(R.id.textview_type);
		TextView tvNetbios = findViewById(R.id.textview_netbios_name_mac);
		TextView tvUpnp = findViewById(R.id.textview_upnp);
		TextView tvNsd = findViewById(R.id.textview_nsd);

		ivIcon.setImageResource(resolveIconFor(info));

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		if (actionbar != null) {
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setDisplayShowHomeEnabled(true);
		}

		KeepScreenOnManager.init(getWindow(), getApplicationContext());

		toolbar.setNavigationOnClickListener(v -> finish());

		String na = getString(R.string.na);

		// deviceName or IP
		String primaryNb = info.netbios != null ? info.netbios.getString("primaryName") : null;
		String title;
		if (!TextUtils.isEmpty(info.deviceName)) {
			title = info.deviceName;
		} else if (!TextUtils.isEmpty(primaryNb)) {
			title = primaryNb;
		} else if (!TextUtils.isEmpty(info.vendor)) {
			title = info.vendor;
		} else {
			title = info.ip;
		}
		tvTitle.setText(title);

		tvIp.setText(getString(R.string.ip_fmt, emptyToNA(info.ip)));
		tvMac.setText(getString(R.string.mac_fmt, emptyToNA(upper(info.mac))));
		tvVendor.setText(getString(R.string.vendor_fmt, emptyToNA(info.vendor)));
		tvPing.setText(getString(R.string.ping_fmt, emptyToNA(info.pingMs)));
		tvType.setText(getString(R.string.type_fmt, emptyToNA(info.deviceType)));

		String nbName = !TextUtils.isEmpty(primaryNb) ? primaryNb : na;
		String nbMac = info.netbios != null ? info.netbios.getString("mac") : null;
		String nbMain = !nbName.equals(na) ? getString(R.string.netbios_fmt, nbName) : na;
		if (!TextUtils.isEmpty(nbMac)) {
			nbMain += getString(R.string.device_details_mac_suffix) + nbMac;
		}
		tvNetbios.setText(nbMain);

		String nbGroup = info.netbios != null ? info.netbios.getString("group") : null;
		TextView tvNetbiosExtras = findViewById(R.id.textview_netbios_extras);
		StringBuilder sbNb = new StringBuilder();
		if (!TextUtils.isEmpty(nbGroup)) {
			sbNb.append(getString(R.string.device_details_workgroup_domain)).append(nbGroup).append("\n\n");
		}

		if (info.netbiosNames != null && !info.netbiosNames.isEmpty()) {
			for (Bundle b : info.netbiosNames) {
				String n = b.getString("name");
				String sfx = b.getString("suffix");
				boolean grp = b.getBoolean("isGroup", false);

				String tag = sfx != null ? "<" + sfx.toUpperCase(java.util.Locale.US) + ">" : "";
				String label = SubnetDevices.NetBiosSuffixUtils.describeSuffix(sfx, grp, n);
				sbNb.append("• ").append(emptyToNA(n))
						.append(tag.isEmpty() ? "" : " " + tag)
						.append(grp ? getString(R.string.device_details_netbios_group_tag) : getString(R.string.device_details_netbios_unique_tag));
				if (!TextUtils.isEmpty(label)) sbNb.append(" - ").append(label);
				sbNb.append("\n");
			}
		} else {
			sbNb.append(na);
		}

		if (!nbMain.equals(na)) {
			tvNetbiosExtras.setText(sbNb.toString().trim());
		} else {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT
			);
			params.setMargins(0, 0, 0, 0);
			tvNetbios.setLayoutParams(params);
			tvNetbiosExtras.setVisibility(View.GONE);
		}

		// UPnP
		if (info.upnp != null && !info.upnp.isEmpty()) {
			String upnp = nonEmptyLine(getString(R.string.device_details_upnp_friendly_name), info.upnp.getString("friendlyName"))
					+ nonEmptyLine(getString(R.string.device_details_upnp_manufacturer), info.upnp.getString("manufacturer"))
					+ nonEmptyLine(getString(R.string.device_details_upnp_model), info.upnp.getString("modelName"))
					+ nonEmptyLine(getString(R.string.device_details_upnp_device_type), info.upnp.getString("deviceType"))
					+ nonEmptyLine(getString(R.string.device_details_upnp_server), info.upnp.getString("server"))
					+ nonEmptyLine(getString(R.string.device_details_upnp_st), info.upnp.getString("st"))
					+ nonEmptyLine(getString(R.string.device_details_upnp_usn), info.upnp.getString("usn"))
					+ nonEmptyLine(getString(R.string.device_details_upnp_location), info.upnp.getString("location"));
			tvUpnp.setText(upnp.trim().isEmpty() ? na : upnp.trim());
		} else {
			tvUpnp.setText(na);
		}

		// NSD services
		if (info.nsdServices != null && !info.nsdServices.isEmpty()) {
			StringBuilder sbNsd = new StringBuilder();
			for (int i = 0; i < info.nsdServices.size(); i++) {
				var b = info.nsdServices.get(i);
				String name = b.getString("name");
				String type = b.getString("type");
				String host = b.getString("host");
				int port = b.getInt("port", -1);

				sbNsd.append("• ").append(emptyToNA(name))
						.append("  [").append(emptyToNA(type)).append("]\n")
						.append("   ").append(emptyToNA(host)).append(port >= 0 ? ":" + port : "")
						.append("\n");

				Bundle attrs = b.getBundle("attributes");
				if (attrs != null && !attrs.isEmpty()) {
					for (String key : attrs.keySet()) {
						String val = attrs.getString(key);
						if (!TextUtils.isEmpty(key)) {
							sbNsd.append("   ").append(key).append(": ").append(emptyToNA(val)).append("\n");
						}
					}
				}

				if (i < info.nsdServices.size() - 1) sbNsd.append("\n");
			}
			tvNsd.setText(sbNsd.toString());
		} else {
			tvNsd.setText(na);
		}

		initInsets();
	}

	private void initInsets() {
		AppBarLayout app_bar_layout = findViewById(R.id.appbarlayout_device_details);
		NestedScrollView scroll_view_root = findViewById(R.id.scroll_view_root_device);

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
				scroll_view_root,
				new InsetsController.Config.Builder()
						.insetTypes(WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.displayCutout())
						.edges(InsetsController.EDGE_BOTTOM | InsetsController.EDGE_HORIZONTAL)
						.applyToPadding()
						.consume(false)
						.build()
		);
	}

	private int resolveIconFor(DeviceInfoParcel info) {
		String gateway = getString(R.string.gateway);
		String yours = getString(R.string.your_device);

		// 1) Router (gateway or UPnP InternetGatewayDevice)
		if (gateway.equalsIgnoreCase(safe(info.deviceType)) || upnpIsGateway(info)) {
			return R.drawable.device_details_router_24px;
		}

		// 2) Computer (NetBIOS present or NSD workstation/SMB/SSH)
		if (hasNetbiosPC(info)
				|| hasNsdType(info, "_workstation._tcp.")
				|| hasNsdType(info, "_smb._tcp.")
				|| hasNsdType(info, "_ssh._tcp.")) {
			return R.drawable.device_details_computer_24px;
		}

		// 3) Media/TV (UPnP MediaServer/Renderer or NSD cast/airplay)
		if (upnpIsMedia(info)
				|| hasNsdType(info, "_googlecast._tcp.")
				|| hasNsdType(info, "_airplay._tcp.")) {
			return R.drawable.device_details_tv_gen_24px;
		}

		// 4) Mobile/Your device or ADB NSD (Android)
		if (yours.equalsIgnoreCase(safe(info.deviceType))
				|| hasNsdType(info, "_adb-tls-connect._tcp")
				|| hasNsdType(info, "_adb._tcp")) {
			return R.drawable.device_details_mobile_24px;
		}

		// 5) Fallback
		return R.drawable.device_details_mobile_question_24px;
	}

	private boolean upnpIsGateway(DeviceInfoParcel info) {
		if (info.upnp == null) return false;
		String dt = info.upnp.getString("deviceType");
		return notEmpty(dt) && dt.toLowerCase(Locale.US).contains("internetgatewaydevice");
	}

	private boolean upnpIsMedia(DeviceInfoParcel info) {
		if (info.upnp == null) return false;
		String dt = info.upnp.getString("deviceType");
		if (!notEmpty(dt)) return false;
		String d = dt.toLowerCase(Locale.US);
		return d.contains("mediaserver") || d.contains("mediarenderer") || d.contains("avtransport");
	}

	private boolean hasNetbiosPC(DeviceInfoParcel info) {
		if (info.netbiosNames == null || info.netbiosNames.isEmpty()) return false;
		// Unique <20> (File Server Service) otherwise any unique name => a workstation/PC
		for (android.os.Bundle b : info.netbiosNames) {
			String sfx = b.getString("suffix");
			boolean grp = b.getBoolean("isGroup", false);
			if (!grp && sfx != null && sfx.equalsIgnoreCase("20")) return true;
		}
		for (android.os.Bundle b : info.netbiosNames) {
			if (!b.getBoolean("isGroup", false)) return true;
		}
		return false;
	}

	private boolean hasNsdType(DeviceInfoParcel info, String typeFrag) {
		if (info.nsdServices == null || info.nsdServices.isEmpty()) return false;
		String f = typeFrag.toLowerCase(Locale.US);
		for (android.os.Bundle b : info.nsdServices) {
			String t = b.getString("type");
			if (t != null) {
				String n = t.toLowerCase(Locale.US);
				if (n.equals(f + ".") || n.contains(f)) return true;
			}
		}
		return false;
	}

	private String nonEmptyLine(String label, String value) {
		if (TextUtils.isEmpty(value)) return "";
		return label + ": " + value + "\n";
	}
	private String safe(String s) { return s == null ? "" : s; }
	private boolean notEmpty(String s) { return s != null && !s.trim().isEmpty(); }
	private String emptyToNA(String v) { return TextUtils.isEmpty(v) ? getString(R.string.na) : v; }
	private String upper(String v) { return v == null ? null : v.toUpperCase(Locale.US); }
}