package com.truemlgpro.wifiinfo;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

public class URLandIPConverter
{

	public static String convertUrl(String url) throws MalformedURLException, UnknownHostException {
		String ip = "";
		try {
			InetAddress ipFromURL = InetAddress.getByName(new URL(url).getHost());
			ip = ipFromURL.getHostAddress();
		} catch (MalformedURLException | UnknownHostException e) {
			e.printStackTrace();
		}
		return ip;
	}
}
