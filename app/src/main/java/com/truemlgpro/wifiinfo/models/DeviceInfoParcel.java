package com.truemlgpro.wifiinfo.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.stealthcopter.networktools.SubnetDevices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceInfoParcel implements Parcelable {
	public String ip;
	public String mac;
	public String vendor;
	public String deviceName; // best name used in list
	public String deviceType;
	public String pingMs;

	public Bundle netbios; // keys: primaryName, mac
	public ArrayList<Bundle> netbiosNames; // { name, suffix, isGroup:boolean }

	// UPnP
	public Bundle upnp; // keys: friendlyName, modelName, manufacturer, server, deviceType, st, usn, location

	// NSD services: list of bundles { name, type, host, port, attributes(Bundle) }
	public ArrayList<Bundle> nsdServices;

	public DeviceInfoParcel() {}

	public static DeviceInfoParcel from(SubnetDevices.NetworkDeviceInfo info, SubnetDevice fallback) {
		if (info == null && fallback == null) return null;
		DeviceInfoParcel p = new DeviceInfoParcel();

		// NetBIOS
		if (info != null) {
			SubnetDevices.NetBiosInfo nb = info.getNetbios();
			if (nb != null) {
				Bundle b = new Bundle();
				if (nb.getPrimaryName() != null) b.putString("primaryName", nb.getPrimaryName());
				if (nb.getMac() != null) b.putString("mac", nb.getMac());
				String group = SubnetDevices.NetBiosSuffixUtils.findGroup(nb);
				if (group != null) b.putString("group", group);
				p.netbios = b;

				List<SubnetDevices.NetBiosName> names = nb.getNames();
				if (names != null && !names.isEmpty()) {
					p.netbiosNames = new ArrayList<>();
					for (SubnetDevices.NetBiosName n : names) {
						Bundle nbB = new Bundle();
						if (n.getName() != null) nbB.putString("name", n.getName());
						if (n.getSuffix() != null) nbB.putString("suffix", n.getSuffix());
						nbB.putBoolean("isGroup", n.isGroup());
						p.netbiosNames.add(nbB);
					}
				}
			}

			// UPnP
			SubnetDevices.UpnpInfo up = info.getUpnp();
			if (up != null) {
				Bundle b = new Bundle();
				if (up.getFriendlyName() != null) b.putString("friendlyName", up.getFriendlyName());
				if (up.getModelName() != null) b.putString("modelName", up.getModelName());
				if (up.getManufacturer() != null) b.putString("manufacturer", up.getManufacturer());
				if (up.getServer() != null) b.putString("server", up.getServer());
				if (up.getDeviceType() != null) b.putString("deviceType", up.getDeviceType());
				if (up.getSt() != null) b.putString("st", up.getSt());
				if (up.getUsn() != null) b.putString("usn", up.getUsn());
				if (up.getLocation() != null) b.putString("location", up.getLocation());
				p.upnp = b;
			}

			// NSD
			List<SubnetDevices.NsdService> svcs = info.getNsdServices();
			if (svcs != null && !svcs.isEmpty()) {
				p.nsdServices = new ArrayList<>();
				for (SubnetDevices.NsdService s : svcs) {
					Bundle b = new Bundle();
					if (s.getName() != null) b.putString("name", s.getName());
					if (s.getType() != null) b.putString("type", s.getType());
					if (s.getHost() != null) b.putString("host", s.getHost());
					b.putInt("port", s.getPort());

					Map<String, String> attrs = s.getAttributes();
					if (attrs != null && !attrs.isEmpty()) {
						Bundle ab = new Bundle();
						for (Map.Entry<String, String> e : attrs.entrySet()) {
							if (e.getKey() != null && e.getValue() != null) {
								ab.putString(e.getKey(), e.getValue());
							}
						}
						b.putBundle("attributes", ab);
					}
					p.nsdServices.add(b);
				}
			}
		}

		if (fallback != null) {
			if (p.ip == null) p.ip = fallback.getIP();
			if (p.mac == null) p.mac = fallback.getMAC();
			if (p.vendor == null) p.vendor = fallback.getDeviceVendor();
			if (p.deviceName == null) p.deviceName = fallback.getDeviceName();
			if (p.deviceType == null) p.deviceType = fallback.getDeviceType();
			if (p.pingMs == null) p.pingMs = fallback.getDevicePingTime();
		}

		return p;
	}

	protected DeviceInfoParcel(Parcel in) {
		ip = in.readString();
		mac = in.readString();
		vendor = in.readString();
		deviceName = in.readString();
		deviceType = in.readString();
		pingMs = in.readString();
		netbios = in.readBundle(getClass().getClassLoader());
		netbiosNames = in.createTypedArrayList(Bundle.CREATOR);
		upnp = in.readBundle(getClass().getClassLoader());
		nsdServices = in.createTypedArrayList(Bundle.CREATOR);
	}

	public static final Creator<DeviceInfoParcel> CREATOR = new Creator<>() {
		@Override
		public DeviceInfoParcel createFromParcel(Parcel in) {
			return new DeviceInfoParcel(in);
		}

		@Override
		public DeviceInfoParcel[] newArray(int size) {
			return new DeviceInfoParcel[size];
		}
	};

	@Override public int describeContents() { return 0; }

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(ip);
		dest.writeString(mac);
		dest.writeString(vendor);
		dest.writeString(deviceName);
		dest.writeString(deviceType);
		dest.writeString(pingMs);
		dest.writeBundle(netbios);
		dest.writeTypedList(netbiosNames);
		dest.writeBundle(upnp);
		dest.writeTypedList(nsdServices);
	}
}