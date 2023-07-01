package com.truemlgpro.wifiinfo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.models.DiscoveredPort;

import java.util.ArrayList;

public class PortScannerAdapter extends RecyclerView.Adapter<PortScannerAdapter.ViewHolder> {
	private final ArrayList<DiscoveredPort> discoveredPortsArrayList;

	public PortScannerAdapter(ArrayList<DiscoveredPort> discoveredPortsArrayList) {
		this.discoveredPortsArrayList = discoveredPortsArrayList;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public TextView textview_open_port;
		public TextView textview_port_service_name;
		public TextView textview_port_service_desc;
		public TextView textview_port_service_protocol;

		public ViewHolder(View itemView) {
			super(itemView);
			textview_open_port = itemView.findViewById(R.id.port_scanner_item_open_port);
			textview_port_service_name = itemView.findViewById(R.id.port_scanner_item_port_service_name);
			textview_port_service_desc = itemView.findViewById(R.id.port_scanner_item_port_service_desc);
			textview_port_service_protocol = itemView.findViewById(R.id.port_scanner_item_port_service_protocol);
		}
	}

	@NonNull
	@Override
	public PortScannerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.port_scanner_recycler_item, parent, false);
		return new PortScannerAdapter.ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull PortScannerAdapter.ViewHolder holder, int position) {
		DiscoveredPort discoveredPort = discoveredPortsArrayList.get(position);
		holder.textview_open_port.setText(discoveredPort.getOpenPort());
		holder.textview_port_service_name.setText(discoveredPort.getPortServiceName());
		holder.textview_port_service_desc.setText(discoveredPort.getPortServiceDescription());
		holder.textview_port_service_protocol.setText(discoveredPort.getPortServiceProtocol());
	}

	@Override
	public int getItemCount() {
		return discoveredPortsArrayList.size();
	}

	public void clear() {
		int size = discoveredPortsArrayList.size();
		discoveredPortsArrayList.clear();
		notifyItemRangeRemoved(0, size);
	}
}
