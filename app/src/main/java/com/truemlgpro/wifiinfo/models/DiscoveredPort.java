package com.truemlgpro.wifiinfo.models;

public class DiscoveredPort {
	private final String openPort;
	private final String portServiceName;
	private final String portServiceDescription;
	private final String portServiceProtocol;

	public DiscoveredPort(String openPort, String portServiceName, String portServiceDescription, String portServiceProtocol) {
		this.openPort = openPort;
		this.portServiceName = portServiceName;
		this.portServiceDescription = portServiceDescription;
		this.portServiceProtocol = portServiceProtocol;
	}

	public String getOpenPort() {
		return openPort;
	}

	public String getPortServiceName() {
		return portServiceName;
	}

	public String getPortServiceDescription() {
		return portServiceDescription;
	}

	public String getPortServiceProtocol() {
		return portServiceProtocol;
	}
}
