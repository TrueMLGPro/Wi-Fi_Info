package com.truemlgpro.wifiinfo.utils;

import android.os.AsyncTask;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLandIPConverter {
	public interface ConversionCallback {
		void onConversionResult(String result);
	}

	public static void convertUrlToIp(String url, ConversionCallback callback) {
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			// If neither is present, default to "http://"
			url = "http://" + url;
		}
		new ConvertUrlToIpTask(callback).execute(url);
	}

	public static void convertIpToUrl(String ip, ConversionCallback callback) {
		new ConvertIpToUrlTask(callback).execute(ip);
	}

	private static String extractDomainFromUrl(String url) {
		// Define the regex pattern to extract the domain.
		Pattern pattern = Pattern.compile("^(https?)(:)(\\/\\/\\/?)"
				+ "([\\w]*(?::[\\w]*)?@)?([\\d\\w\\.-]+)(?::(\\d+))?([\\/\\\\\\w\\.()-]*)?"
				+ "(?:([?][^#]*)?(#.*)?)*");

		Matcher matcher = pattern.matcher(url);

		if (matcher.find()) {
			// Group 5 contains the domain.
			return matcher.group(5);
		}

		return null; // Invalid URL format.
	}

	private static class ConvertUrlToIpTask extends AsyncTask<String, Void, String> {
		private final ConversionCallback callback;

		ConvertUrlToIpTask(ConversionCallback callback) {
			this.callback = callback;
		}

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			String domain = extractDomainFromUrl(url);
			if (domain != null) {
				try {
					InetAddress address = InetAddress.getByName(domain);
					return address.getHostAddress();
				} catch (UnknownHostException e) {
					e.printStackTrace();
					return "Error resolving IP address.";
				}
			} else {
				return "Invalid URL format.";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (callback != null) {
				callback.onConversionResult(result);
			}
		}
	}

	private static class ConvertIpToUrlTask extends AsyncTask<String, Void, String> {
		private final ConversionCallback callback;

		ConvertIpToUrlTask(ConversionCallback callback) {
			this.callback = callback;
		}

		@Override
		protected String doInBackground(String... params) {
			String ip = params[0];
			try {
				InetAddress inetAddress = InetAddress.getByName(ip);
				return inetAddress.getHostName();
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return "Error resolving hostname.";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (callback != null) {
				callback.onConversionResult(result);
			}
		}
	}
}
