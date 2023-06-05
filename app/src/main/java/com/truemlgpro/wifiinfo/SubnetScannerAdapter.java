package com.truemlgpro.wifiinfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SubnetScannerAdapter extends RecyclerView.Adapter<SubnetScannerAdapter.ViewHolder> {
	private ArrayList<SubnetDevice> subnetDevicesArrayList;

	public SubnetScannerAdapter(ArrayList<SubnetDevice> subnetDevicesArrayList) {
		this.subnetDevicesArrayList = subnetDevicesArrayList;
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
		holder.textview_ip.setText(subnetDevice.getIP());
		holder.textview_mac.setText(subnetDevice.getMAC());
		holder.textview_device_type.setText(subnetDevice.getDeviceType());
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

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public TextView textview_ip;
		public TextView textview_mac;
		public TextView textview_device_type;

		public ViewHolder(View itemView) {
			super(itemView);
			textview_ip = itemView.findViewById(R.id.subnet_scanner_item_ip);
			textview_mac = itemView.findViewById(R.id.subnet_scanner_item_mac);
			textview_device_type = itemView.findViewById(R.id.subnet_scanner_item_device_type);
		}
	}
}
