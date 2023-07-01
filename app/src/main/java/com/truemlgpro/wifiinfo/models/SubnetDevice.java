package com.truemlgpro.wifiinfo.models;

public class SubnetDevice {
	private final String ip;
	private final String mac;
	private final String vendor;
	private final String deviceType;
	private final String devicePing;

	public SubnetDevice(String ip, String mac, String vendor, String deviceType, String devicePing) {
		this.ip = ip;
		this.mac = mac;
		this.vendor = vendor;
		this.deviceType = deviceType;
		this.devicePing = devicePing;
	}

	public String getIP() {
		return ip;
	}

	public String getMAC() {
		return mac;
	}

	public String getDeviceVendor() {
		return vendor;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public String getDevicePingTime() {
		return devicePing;
	}
}
