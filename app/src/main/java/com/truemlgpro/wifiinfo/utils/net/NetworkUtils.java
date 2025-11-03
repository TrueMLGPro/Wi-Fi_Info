package com.truemlgpro.wifiinfo.utils.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.truemlgpro.wifiinfo.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkUtils {
	private static final int TRANSPORT_ANY = -1;
	private static ConnectivityManager connectivityManager;

	// Background DNS work + main thread handler for callbacks
	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
	private static final Handler MAIN = new Handler(Looper.getMainLooper());

	// Public IP fetches
	private static final ExecutorService PUBLIC_IP_EXECUTOR = Executors.newSingleThreadExecutor();

	public interface ConversionCallback {
		void onConversionResult(String result);
	}

	public static void convertUrlToIp(String url, ConversionCallback callback) {
		final String domain = extractDomain(url);
		if (domain == null || domain.isEmpty()) {
			conversionResult(callback, "Invalid URL or domain.");
			return;
		}
		EXECUTOR.execute(() -> {
			String result;
			try {
				InetAddress address = InetAddress.getByName(domain);
				result = address.getHostAddress();
			} catch (Exception e) {
				result = "Error resolving IP address.";
			}
			conversionResult(callback, result);
		});
	}

	public static void convertIpToUrl(String ip, ConversionCallback callback) {
		EXECUTOR.execute(() -> {
			String result;
			try {
				InetAddress inetAddress = InetAddress.getByName(ip);
				result = inetAddress.getHostName();
			} catch (Exception e) {
				result = "Error resolving hostname.";
			}
			conversionResult(callback, result);
		});
	}

	private static void conversionResult(ConversionCallback callback, String result) {
		if (callback == null) return;
		MAIN.post(() -> callback.onConversionResult(result));
	}

	@NonNull
	public static String getMacAddress(Context context) {
		try {
			List<NetworkInterface> allNetworkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface networkInterface : allNetworkInterfaces) {
				if (!networkInterface.getName().equalsIgnoreCase("wlan0"))
					continue;

				byte[] macBytes = networkInterface.getHardwareAddress();
				if (macBytes == null)
					return context.getString(R.string.na);

				StringBuilder macAddressStringBuilder = new StringBuilder();
				for (byte b : macBytes)
					macAddressStringBuilder.append(String.format("%02X:", b));

				if (macAddressStringBuilder.length() > 0)
					macAddressStringBuilder.deleteCharAt(macAddressStringBuilder.length() - 1);

				return macAddressStringBuilder.toString();
			}
		} catch (Exception e) {
			Log.e("getMacAddress", e.toString());
		}
		return context.getString(R.string.na);
	}

	@Nullable
	public static String getIPv4Address(String... prefInterfaceNames) {
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			if (en == null) return null;
			List<NetworkInterface> allNetworkInterfaces = Collections.list(en);

			if (prefInterfaceNames != null && prefInterfaceNames.length > 0) {
				Set<String> wanted = new HashSet<>();
				for (String n : prefInterfaceNames) {
					if (n != null) wanted.add(n.toLowerCase(Locale.ROOT));
				}
				for (NetworkInterface ni : allNetworkInterfaces) {
					if (!wanted.contains(ni.getName().toLowerCase(Locale.ROOT))) continue;
					for (InetAddress inetAddr : Collections.list(ni.getInetAddresses())) {
						if (!inetAddr.isLoopbackAddress() && inetAddr instanceof Inet4Address) {
							return inetAddr.getHostAddress();
						}
					}
				}
			}

			for (NetworkInterface ni : allNetworkInterfaces) {
				for (InetAddress inetAddr : Collections.list(ni.getInetAddresses())) {
					if (!inetAddr.isLoopbackAddress() && inetAddr instanceof Inet4Address) {
						return inetAddr.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			Log.e("getIPv4Address", e.toString());
		}
		return null;
	}

	@Nullable
	public static String getIPv6Address(String... prefInterfaceNames) {
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			if (en == null) return null;
			List<NetworkInterface> allNetworkInterfaces = Collections.list(en);

			if (prefInterfaceNames != null && prefInterfaceNames.length > 0) {
				Set<String> wanted = new HashSet<>();
				for (String n : prefInterfaceNames) {
					if (n != null) wanted.add(n.toLowerCase(Locale.ROOT));
				}
				for (NetworkInterface ni : allNetworkInterfaces) {
					if (!wanted.contains(ni.getName().toLowerCase(Locale.ROOT))) continue;
					for (InetAddress inetAddr : Collections.list(ni.getInetAddresses())) {
						if (!inetAddr.isLoopbackAddress() && inetAddr instanceof Inet6Address) {
							String host = inetAddr.getHostAddress();
							int percent = host.indexOf('%');
							if (percent >= 0) host = host.substring(0, percent);
							return host;
						}
					}
				}
			}

			for (NetworkInterface ni : allNetworkInterfaces) {
				for (InetAddress inetAddr : Collections.list(ni.getInetAddresses())) {
					if (!inetAddr.isLoopbackAddress() && inetAddr instanceof Inet6Address) {
						String host = inetAddr.getHostAddress();
						int percent = host.indexOf('%');
						if (percent >= 0) host = host.substring(0, percent);
						return host;
					}
				}
			}
		} catch (SocketException e) {
			Log.e("getIPv6Address", e.toString());
		}
		return null;
	}

	@NonNull
	public static String getGatewayIP(@NonNull Context context) {
		connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiCheck = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (!wifiCheck.isConnected())
			return "0.0.0.0";
		WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifiManager.getDhcpInfo();
		int gatewayIP = dhcp.gateway;
		return intToIp(gatewayIP);
	}

	public static String getHostname(@NonNull Context context) {
		try {
			InetAddress hostnameAddr = InetAddress.getByName(getGatewayIP(context));
			return hostnameAddr.getCanonicalHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return context.getString(R.string.na);
	}

	@RequiresApi(api = Build.VERSION_CODES.R)
	public static String getWifiStandard(@NonNull Context context) {
		WifiManager mainWifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = mainWifi.getConnectionInfo();
		int wifiStandard = wifiInfo.getWifiStandard();
		return switch (wifiStandard) {
			case ScanResult.WIFI_STANDARD_LEGACY -> "Wi-Fi 1/2/3 (802.11a/b/g)";
			case ScanResult.WIFI_STANDARD_11N -> "Wi-Fi 4 (802.11n)";
			case ScanResult.WIFI_STANDARD_11AC -> "Wi-Fi 5 (802.11ac)";
			case ScanResult.WIFI_STANDARD_11AX -> "Wi-Fi 6 (802.11ax)";
			case ScanResult.WIFI_STANDARD_11AD -> "WiGig (802.11ad)";
			case ScanResult.WIFI_STANDARD_11BE -> "Wi-Fi 7 (802.11be)";
			case ScanResult.WIFI_STANDARD_UNKNOWN -> context.getString(R.string.na);
			default -> context.getString(R.string.na);
		};
	}

	@Nullable
	public static String getNetworkInterface() {
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetworkInfo != null) {
			for (Network network : connectivityManager.getAllNetworks()) {
				NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
				if (networkInfo.toString().equals(activeNetworkInfo.toString())) {
					LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
					return linkProperties.getInterfaceName();
				}
			}
		}
		return null;
	}

	private static String netPrefixLengthToSubnetMask(int netPrefixLength) {
		int shift = 0xFFFFFFFF << (32 - netPrefixLength);
		return (((shift & 0xFF000000) >> 24) & 0xFF) + "."
				+ (((shift & 0x00FF0000) >> 16) & 0xFF) + "."
				+ (((shift & 0x0000FF00) >> 8) & 0xFF) + "."
				+ ((shift & 0x000000FF) & 0xFF);
	}

	public static String getSubnetMask() {
		try {
			String ipAddr = getIPv4Address();
			if (ipAddr == null)
				return null;
			InetAddress inetAddress = InetAddress.getByName(ipAddr);
			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
			for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
				if (Patterns.IP_ADDRESS.matcher(String.valueOf(address.getAddress()).replaceFirst("/", "")).matches())
					return netPrefixLengthToSubnetMask(address.getNetworkPrefixLength());
			}
		} catch (IOException e) {
			Log.e("getSubnetMask()", e.toString());
		}
		return null;
	}

	public static String getBroadcastAddr() {
		try {
			if (getIPv4Address() == null)
				return null;
			InetAddress inetAddress = InetAddress.getByName(String.valueOf(getIPv4Address()));
			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
			for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
				if (Patterns.IP_ADDRESS.matcher(String.valueOf(address.getAddress()).replaceFirst("/", "")).matches())
					return String.valueOf(address.getBroadcast()).replaceFirst("/", "");
			}
		} catch (IOException e) {
			Log.e("getBroadcastAddr", e.toString());
		}
		return null;
	}

	public static String getLocalhostAddress() {
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			return localHost.toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String intToIp(int ipInt) {
		return ((ipInt & 0xFF) + "."
				+ ((ipInt >> 8) & 0xFF) + "."
				+ ((ipInt >> 16) & 0xFF) + "."
				+ ((ipInt >> 24) & 0xFF));
	}

	public static int convertFrequencyToChannel(int freq) {
		if (freq == 2484) { // 2.4GHz Channel 14
			return 14;
		} else if (freq < 2484) { // 2.4GHz (802.11b/g/n/ax)
			return (freq - 2407) / 5;
		} else if (freq >= 4910 && freq <= 4980) { // 4.9GHz (802.11j)
			return (freq - 4000) / 5;
		} else if (freq < 5925) { // 5GHz (802.11a/h/j/n/ac/ax) - 5.9 GHz (802.11p)
			return (freq - 5000) / 5;
		} else if (freq == 5935) {
			return 2;
		} else if (freq <= 45000) { // 6 GHz+ (802.11ax and 802.11be)
			return (freq - 5950) / 5;
		} else if (freq >= 58320 && freq <= 70200) { // 60GHz (802.11ad/ay)
			return (freq - 56160) / 2160;
		} else {
			return -1;
		}
	}

	public static double convertFreqRssiToDistance(int frequency, int rssi) {
		return Math.pow(10.0D, (27.55D - 20 * Math.log10((double) frequency) + Math.abs(rssi)) / 20.0D);
	}

	public static boolean isSimCardPresent(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return !(tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);
	}

	public static String extractDomain(String link) {
		if (link == null) return null;

		String s = link.trim();
		if (s.isEmpty()) return "";

		// Remove protocol (http://, https://, ftp://, etc.)
		s = s.replaceFirst("^[a-zA-Z][a-zA-Z0-9+.-]*://", "");
		// Handle protocol-relative URLs (//example.com/path)
		s = s.replaceFirst("^//+", "");

		// Keep only the authority part before '/', '?', or '#'
		int cut = s.length();
		int slash = s.indexOf('/');
		int q = s.indexOf('?');
		int hash = s.indexOf('#');
		if (slash >= 0 && slash < cut) cut = slash;
		if (q >= 0 && q < cut) cut = q;
		if (hash >= 0 && hash < cut) cut = hash;
		s = s.substring(0, cut);

		// Drop userinfo if present (user:pass@host)
		int at = s.indexOf('@');
		if (at >= 0) s = s.substring(at + 1);

		// IPv6 in brackets: [host%zone]:port
		if (s.startsWith("[")) {
			int end = s.indexOf(']');
			if (end > 0) {
				String host = s.substring(1, end);
				int pct = host.indexOf('%'); // strip zone id
				if (pct >= 0) host = host.substring(0, pct);
				return host;
			}
			// Malformed bracket case: fall through and best-effort clean
			s = s.replace("[", "");
			int rb = s.indexOf(']');
			if (rb >= 0) s = s.substring(0, rb);
		}

		// Non-bracketed: strip :port if it's a single colon followed by digits
		int firstColon = s.indexOf(':');
		int lastColon = s.lastIndexOf(':');
		if (firstColon >= 0 && firstColon == lastColon) {
			if (lastColon == s.length() - 1) {
				// Trailing colon with no port digits
				s = s.substring(0, lastColon);
			} else {
				String after = s.substring(lastColon + 1);
				if (after.matches("\\d{1,5}")) {
					s = s.substring(0, lastColon);
				}
			}
		}

		// Strip IPv6 zone id (fe80::1%wlan0)
		int pct = s.indexOf('%');
		if (pct >= 0) s = s.substring(0, pct);

		// Drop trailing dot on FQDNs
		if (s.endsWith(".")) s = s.substring(0, s.length() - 1);

		return s;
	}

	// Connectivity checks

	public enum NetworkType {
		ANY,
		WIFI,
		CELLULAR
	}

	private static int toTransport(NetworkType type) {
		switch (type) {
			case WIFI: return NetworkCapabilities.TRANSPORT_WIFI;
			case CELLULAR: return NetworkCapabilities.TRANSPORT_CELLULAR;
			case ANY:
			default: return TRANSPORT_ANY;
		}
	}

	/**
	 * Checks connectivity for a specific transport or for any transport.
	 * @param ctx Android context
	 * @param transport NetworkCapabilities.TRANSPORT_WIFI, TRANSPORT_CELLULAR, or TRANSPORT_ANY
	 * @param requireValidated when true, requires proven working internet (VALIDATED) on API 23+
	 */
	public static boolean isOnline(Context ctx, int transport, boolean requireValidated) {
		ConnectivityManager cm = (ConnectivityManager) ctx.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) return false;

		// SIM check for cellular
		boolean simPresent = isSimCardPresent(ctx);
		if (transport == NetworkCapabilities.TRANSPORT_CELLULAR && !simPresent) {
			return false;
		}

		if (Build.VERSION.SDK_INT >= 23) {
			// Prefer active network first
			Network active = cm.getActiveNetwork();
			if (active != null) {
				NetworkCapabilities caps = cm.getNetworkCapabilities(active);
				if (caps != null) {
					boolean transportOk;
					if (transport == TRANSPORT_ANY) {
						transportOk =
								caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
										|| caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
										|| (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) && simPresent);
					} else {
						transportOk = caps.hasTransport(transport)
								&& (transport != NetworkCapabilities.TRANSPORT_CELLULAR || simPresent);
					}

					if (transportOk) {
						if (!requireValidated) {
							// For ANY, also allow networks that at least claim INTERNET
							if (transport == TRANSPORT_ANY && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
								return true;
							}
							return true;
						} else {
							return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
									&& caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
						}
					}
				}
			}

			// Fallback: scan all networks (covers multi-network cases)
			Network[] nets = cm.getAllNetworks();
			if (nets == null) return false;
			for (Network n : nets) {
				NetworkCapabilities caps = cm.getNetworkCapabilities(n);
				if (caps == null) continue;

				boolean transportOk;
				if (transport == TRANSPORT_ANY) {
					transportOk =
							caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
									|| caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
									|| (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) && simPresent);
				} else {
					transportOk = caps.hasTransport(transport)
							&& (transport != NetworkCapabilities.TRANSPORT_CELLULAR || simPresent);
				}

				if (!transportOk) continue;

				if (!requireValidated) {
					// For ANY, also allow networks that at least claim INTERNET
					if (transport == TRANSPORT_ANY && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
						return true;
					}
					return true;
				} else if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
						&& caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
					return true;
				}
			}
			return false;
		} else {
			// Pre-23: NetworkInfo
			if (transport == TRANSPORT_ANY) {
				NetworkInfo info = cm.getActiveNetworkInfo();
				if (info == null || !info.isConnected()) return false;
				// If active is MOBILE but no SIM, treat as not online
				if (info.getType() == ConnectivityManager.TYPE_MOBILE && !simPresent) return false;
				return true;
			} else if (transport == NetworkCapabilities.TRANSPORT_WIFI) {
				NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				return wifi != null && wifi.isConnected();
			} else if (transport == NetworkCapabilities.TRANSPORT_CELLULAR) {
				if (!simPresent) return false;
				NetworkInfo cell = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				return cell != null && cell.isConnected();
			} else {
				NetworkInfo info = cm.getActiveNetworkInfo();
				return info != null && info.isConnected();
			}
		}
	}

	// Enum-based overloads
	public static boolean isOnline(Context ctx, NetworkType type, boolean requireValidated) {
		return isOnline(ctx, toTransport(type), requireValidated);
	}
	public static boolean isOnline(Context ctx, NetworkType type) {
		return isOnline(ctx, toTransport(type), false);
	}

	// Public IP fetcher

	public interface Cancelable {
		void cancel();
		boolean isCanceled();
	}

	public interface PublicIpCallback {
		void onSuccess(String ip);
		void onError(Exception e);
	}

	private static final String[] DEFAULT_PUBLIC_IP_ENDPOINTS = new String[] {
			"https://public-ip-api.vercel.app/api/ip/",
			"https://api.ipify.org",
			"https://ifconfig.co/ip",
	};

	public static Cancelable fetchPublicIp(PublicIpCallback cb) {
		return fetchPublicIp(5000, 5000, DEFAULT_PUBLIC_IP_ENDPOINTS, cb);
	}

	public static Cancelable fetchPublicIp(
			int connectTimeoutMs,
			int readTimeoutMs,
			String[] endpoints,
			PublicIpCallback cb
	) {
		final AtomicBoolean canceled = new AtomicBoolean(false);

		final Future<?> future = PUBLIC_IP_EXECUTOR.submit(() -> {
			try {
				String ip = null;
				String[] eps = (endpoints != null && endpoints.length > 0) ? endpoints : DEFAULT_PUBLIC_IP_ENDPOINTS;
				for (String url : eps) {
					if (canceled.get() || Thread.currentThread().isInterrupted()) return;
					ip = tryFetchPublicIp(url, connectTimeoutMs, readTimeoutMs, canceled);
					if (ip != null) break;
				}

				if (canceled.get() || Thread.currentThread().isInterrupted()) return;

				if (ip != null) {
					postSuccess(cb, ip);
				} else {
					postError(cb, new IOException("Failed to fetch public IP from all endpoints"));
				}
			} catch (Exception e) {
				if (!canceled.get()) postError(cb, e);
			}
		});

		return new Cancelable() {
			@Override public void cancel() {
				canceled.set(true);
				future.cancel(true);
			}
			@Override public boolean isCanceled() {
				return canceled.get() || future.isCancelled();
			}
		};
	}

	private static void postSuccess(PublicIpCallback cb, String ip) {
		if (cb == null) return;
		MAIN.post(() -> {
			try { cb.onSuccess(ip); } catch (Exception ignored) {}
		});
	}

	private static void postError(PublicIpCallback cb, Exception e) {
		if (cb == null) return;
		MAIN.post(() -> {
			try { cb.onError(e); } catch (Exception ignored) {}
		});
	}

	private static String tryFetchPublicIp(String urlStr, int connectTimeoutMs, int readTimeoutMs, AtomicBoolean canceled) {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setInstanceFollowRedirects(true);
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(connectTimeoutMs);
			conn.setReadTimeout(readTimeoutMs);
			conn.setRequestProperty("Accept", "text/plain, application/json");

			int code = conn.getResponseCode();
			if (canceled.get() || Thread.currentThread().isInterrupted()) return null;

			InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
			if (is == null) return null;

			String body;
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
				StringBuilder sb = new StringBuilder();
				String line;
				while (!canceled.get() && (line = reader.readLine()) != null) {
					sb.append(line);
				}
				body = sb.toString().trim();
			}

			if (canceled.get()) return null;
			return extractIp(body);
		} catch (Exception ignored) {
			return null;
		} finally {
			if (conn != null) conn.disconnect();
		}
	}

	private static String extractIp(String body) {
		if (body == null || body.isEmpty()) return null;
		if (Patterns.IP_ADDRESS.matcher(body).matches()) return body; // plain

		try { // json
			JSONObject json = new JSONObject(body);
			String ip = json.optString("ip", null);
			if (ip != null && Patterns.IP_ADDRESS.matcher(ip).matches()) return ip;
		} catch (Exception ignored) {}

		// fallback formats like "ip: 1.2.3.4"
		int idx = body.indexOf(':');
		if (idx != -1) {
			String tail = body.substring(idx + 1).trim();
			if (Patterns.IP_ADDRESS.matcher(tail).matches()) return tail;
		}
		return null;
	}
}
