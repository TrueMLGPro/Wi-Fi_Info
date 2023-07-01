package com.truemlgpro.wifiinfo.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.models.SubnetDevice;

import java.util.ArrayList;

public class SubnetScannerAdapter extends RecyclerView.Adapter<SubnetScannerAdapter.ViewHolder> {
	private final ArrayList<SubnetDevice> subnetDevicesArrayList;
	private final Context context;

	public SubnetScannerAdapter(ArrayList<SubnetDevice> subnetDevicesArrayList, Context context) {
		this.subnetDevicesArrayList = subnetDevicesArrayList;
		this.context = context;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public RelativeLayout relative_layout_vendor;
		public TextView textview_ip;
		public TextView textview_mac;
		public TextView textview_vendor;
		public TextView textview_device_type;
		public TextView textview_device_ping;

		public ViewHolder(View itemView) {
			super(itemView);
			relative_layout_vendor = itemView.findViewById(R.id.subnet_scanner_item_relative_layout_vendor);
			textview_ip = itemView.findViewById(R.id.subnet_scanner_item_ip);
			textview_mac = itemView.findViewById(R.id.subnet_scanner_item_mac);
			textview_vendor = itemView.findViewById(R.id.subnet_scanner_item_vendor);
			textview_device_type = itemView.findViewById(R.id.subnet_scanner_item_device_type);
			textview_device_ping = itemView.findViewById(R.id.subnet_scanner_item_device_ping);
		}
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subnet_scanner_recycler_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		SubnetDevice subnetDevice = subnetDevicesArrayList.get(position);
		String ipAddress = subnetDevice.getIP();
		String macAddress = subnetDevice.getMAC();
		String deviceVendor = subnetDevice.getDeviceVendor();
		String deviceType = subnetDevice.getDeviceType();
		String pingTimeString = subnetDevice.getDevicePingTime();

		SpannableStringBuilder ipAddressStyled = new SpannableStringBuilder(ipAddress);
		int lastDotIndex = ipAddress.lastIndexOf(".");
		// Set the bold style for the last integer in the IP address
		ipAddressStyled.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
				lastDotIndex + 1, ipAddress.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);

		holder.textview_ip.setText(ipAddressStyled);
		if (!macAddress.equals(context.getString(R.string.na))) {
			holder.relative_layout_vendor.setVisibility(View.VISIBLE);
			holder.textview_mac.setPadding(dpToPixels(8), dpToPixels(4), dpToPixels(8), dpToPixels(4));
			if (deviceVendor != null && !deviceVendor.equals("")) {
				holder.textview_vendor.setText(deviceVendor);
			} else {
				holder.textview_vendor.setText(context.getString(R.string.na));
			}
		} else {
			holder.relative_layout_vendor.setVisibility(View.GONE);
			holder.textview_mac.setPadding(dpToPixels(8), dpToPixels(4), dpToPixels(8), dpToPixels(16));
		}
		holder.textview_mac.setText(macAddress);
		holder.textview_device_type.setText(deviceType);
		holder.textview_device_ping.setText(pingTimeString);

		float pingTime = extractPingTime(pingTimeString);
		if (pingTime <= 100) {
			holder.textview_device_ping.setTextColor(Color.GREEN);
		} else if (pingTime > 100 && pingTime <= 250) {
			holder.textview_device_ping.setTextColor(Color.YELLOW);
		} else {
			holder.textview_device_ping.setTextColor(Color.RED);
		}
	}

	private float extractPingTime(String pingString) {
		String timeString = pingString.replaceAll("[^\\d.]", "");
		return Float.parseFloat(timeString);
	}

	private int dpToPixels(int dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	@Override
	public int getItemCount() {
		return subnetDevicesArrayList.size();
	}

	public void clear() {
		int size = subnetDevicesArrayList.size();
		subnetDevicesArrayList.clear();
		notifyItemRangeRemoved(0, size);
	}
}
