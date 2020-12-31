package com.truemlgpro.wifiinfo;

import java.net.*;

public class URLandIPConverter
{
	public static String convertUrl(String url) throws MalformedURLException, UnknownHostException {
		String ip = "";
		try {
			InetAddress ipFromURL = InetAddress.getByName(new URL(url).getHost());
			ip = ipFromURL.getHostAddress();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new MalformedURLException();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new UnknownHostException();
		}
		return ip;
	}
}
