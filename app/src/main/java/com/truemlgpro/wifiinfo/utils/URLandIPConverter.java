package com.truemlgpro.wifiinfo.utils;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

public class URLandIPConverter {
	public static String convertUrl(String url) throws MalformedURLException, UnknownHostException {
		try {
			InetAddress ipFromURL = InetAddress.getByName(new URL(url).getHost());
			return ipFromURL.getHostAddress();
		} catch (MalformedURLException | UnknownHostException e) {
			e.printStackTrace();
		}
		return "";
	}
}
