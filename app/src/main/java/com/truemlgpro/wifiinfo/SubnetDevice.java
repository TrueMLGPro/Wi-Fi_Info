package com.truemlgpro.wifiinfo;

public class SubnetDevice {
	private final String ip;
	private final String mac;
	private final String deviceType;

	public SubnetDevice(String ip, String mac, String deviceType) {
		this.ip = ip;
		this.mac = mac;
		this.deviceType = deviceType;
	}

	public String getIP() {
		return ip;
	}

	public String getMAC() {
		return mac;
	}

	public String getDeviceType() {
		return deviceType;
	}
}
