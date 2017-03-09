/*
 * Copyright 2015 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.nohttp.tools;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.NoHttp;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * <p>
 * Check the network utility class.
 * </p>
 * Created in Jul 31, 2015 1:19:47 PM.
 *
 * @author Yan Zhenjie.
 */
public class NetUtil {

    public enum NetType {
        Any,

        Wifi,

        Mobile
    }

    /**
     * Class name of the {@link android.provider.Settings}.
     */
    private static final String ANDROID_PROVIDER_SETTINGS = "android.provider.Settings";

    private static ConnectivityManager sConnectivityManager;

    private static ConnectivityManager getConnectivityManager() {
        if (sConnectivityManager == null) {
            synchronized (NetUtil.class) {
                if (sConnectivityManager == null)
                    sConnectivityManager = (ConnectivityManager) NoHttp.getContext().getSystemService(Context
                            .CONNECTIVITY_SERVICE);
            }
        }
        return sConnectivityManager;
    }

    /**
     * Open network settings page.
     */
    public static void openSetting() {
        if (Build.VERSION.SDK_INT > AndroidVersion.GINGERBREAD_MR1)
            openSetting("ACTION_WIFI_SETTINGS");
        else
            openSetting("ACTION_WIRELESS_SETTINGS");
    }

    private static void openSetting(String actionName) {
        try {
            Class<?> settingsClass = Class.forName(ANDROID_PROVIDER_SETTINGS);
            Field actionWifiSettingsField = settingsClass.getDeclaredField(actionName);
            Intent settingIntent = new Intent(actionWifiSettingsField.get(null).toString());
            settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            NoHttp.getContext().startActivity(settingIntent);
        } catch (Throwable e) {
            Logger.w(e);
        }
    }

    /**
     * Check the network is enable.
     *
     * @return Available returns true, unavailable returns false.
     */
    public static boolean isNetworkAvailable() {
        return isNetworkAvailable(NetType.Any);
    }

    /**
     * To determine whether a WiFi network is available.
     *
     * @return Open return true, close returns false.
     */
    public static boolean isWifiConnected() {
        return isNetworkAvailable(NetType.Wifi);
    }

    /**
     * To determine whether a mobile phone network is available.
     *
     * @return Open return true, close returns false.
     */
    public static boolean isMobileConnected() {
        return isNetworkAvailable(NetType.Mobile);
    }

    /**
     * According to the different type of network to determine whether the network connection.
     *
     * @param netType from {@link NetType}.
     * @return Connection state return true, otherwise it returns false.
     */
    public static boolean isNetworkAvailable(NetType netType) {
        getConnectivityManager();
        Class<?> connectivityManagerClass = sConnectivityManager.getClass();
        if (Build.VERSION.SDK_INT >= AndroidVersion.LOLLIPOP) {
            try {
                Method getAllNetworksMethod = connectivityManagerClass.getMethod("getAllNetworks");
                getAllNetworksMethod.setAccessible(true);
                Object[] networkArray = (Object[]) getAllNetworksMethod.invoke(sConnectivityManager);
                for (Object network : networkArray) {
                    Method getNetworkInfoMethod = connectivityManagerClass.getMethod("getNetworkInfo", Class
                            .forName("android.net.Network"));
                    getNetworkInfoMethod.setAccessible(true);
                    NetworkInfo networkInfo = (NetworkInfo) getNetworkInfoMethod.invoke(sConnectivityManager,
                            network);
                    if (isConnected(netType, networkInfo))
                        return true;
                }
            } catch (Throwable e) {
            }
        } else {
            try {
                Method getAllNetworkInfoMethod = connectivityManagerClass.getMethod("getAllNetworkInfo");
                getAllNetworkInfoMethod.setAccessible(true);
                Object[] networkInfoArray = (Object[]) getAllNetworkInfoMethod.invoke(sConnectivityManager);
                for (Object object : networkInfoArray) {
                    if (isConnected(netType, (NetworkInfo) object))
                        return true;
                }
            } catch (Throwable e) {
            }
        }
        return false;
    }

    /**
     * According to the different type of network to determine whether the network connection.
     *
     * @param netType     from {@link NetType}.
     * @param networkInfo from {@link NetworkInfo}.
     * @return Connection state return true, otherwise it returns false.
     */
    private static boolean isConnected(NetType netType, NetworkInfo networkInfo) {
        switch (netType) {
            case Any:
                return networkInfo != null && isConnected(networkInfo);
            case Wifi:
                return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI &&
                        isConnected(networkInfo);
            case Mobile:
                return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE &&
                        isConnected(networkInfo);
        }
        return false;
    }

    /**
     * Whether network connection.
     *
     * @param networkInfo from {@link NetworkInfo}.
     * @return Connection state return true, otherwise it returns false.
     */
    private static boolean isConnected(NetworkInfo networkInfo) {
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    /**
     * Check the GPRS whether available.
     *
     * @return Open return true, close returns false.
     */
    public static boolean isGPRSOpen() {
        getConnectivityManager();
        Class<?> cmClass = sConnectivityManager.getClass();
        try {
            Method getMobileDataEnabledMethod = cmClass.getMethod("getMobileDataEnabled");
            getMobileDataEnabledMethod.setAccessible(true);
            return (Boolean) getMobileDataEnabledMethod.invoke(sConnectivityManager);
        } catch (Throwable e) {
        }
        return false;
    }

    /**
     * Open or close the GPRS.
     *
     * @param isEnable Open to true, close to false.
     */
    public static void setGPRSEnable(boolean isEnable) {
        getConnectivityManager();
        Class<?> cmClass = sConnectivityManager.getClass();
        try {
            Method setMobileDataEnabledMethod = cmClass.getMethod("setMobileDataEnabled", boolean.class);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(sConnectivityManager, isEnable);
        } catch (Throwable e) {
        }
    }

    /**
     * Tet local ip address.
     *
     * @return Such as: {@code 192.168.1.1}.
     */
    public static String getLocalIPAddress() {
        Enumeration<NetworkInterface> enumeration = null;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            Logger.w(e);
        }
        if (enumeration != null) {
            // 遍历所用的网络接口
            while (enumeration.hasMoreElements()) {
                NetworkInterface nif = enumeration.nextElement();// 得到每一个网络接口绑定的地址
                Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                if (inetAddresses != null)
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress ip = inetAddresses.nextElement();
                        if (!ip.isLoopbackAddress() && isIPv4Address(ip.getHostAddress())) {
                            return ip.getHostAddress();
                        }
                    }
            }
        }
        return "";
    }

    /**
     * Ipv4 address check.
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile("^(" +
            "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +
            "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    /**
     * Check if valid IPV4 address.
     *
     * @param input the address string to check for validity.
     * @return True if the input parameter is a valid IPv4 address.
     */
    public static boolean isIPv4Address(String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

	/* ===========以下是IPv6的检查========== */

    // 未压缩过的IPv6地址检查
    private static final Pattern IPV6_STD_PATTERN = Pattern.compile("^[0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4}){7}$");
    // 压缩过的IPv6地址检查
    private static final Pattern IPV6_HEX_COMPRESSED_PATTERN = Pattern.compile("^(([0-9A-Fa-f]{1,4}" +
            "(:[0-9A-Fa-f]{1,4}){0,5})?)" +                                                              // 0-6
            "::" + "(([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4}){0,5})?)$");// 0-6 hex fields

    /**
     * Check whether the parameter is effective standard (uncompressed) IPv6 address.
     *
     * @param input IPV6 address.
     * @return True or false.
     * @see #isIPv6HexCompressedAddress(String)
     */
    public static boolean isIPv6StdAddress(final String input) {
        return IPV6_STD_PATTERN.matcher(input).matches();
    }

    /**
     * Check whether the parameter is effective compression IPv6 address.
     *
     * @param input IPV6 address.
     * @return True or false.
     * @see #isIPv6StdAddress(String)
     */
    public static boolean isIPv6HexCompressedAddress(final String input) {
        int colonCount = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == ':') {
                colonCount++;
            }
        }
        return colonCount <= 7 && IPV6_HEX_COMPRESSED_PATTERN.matcher(input).matches();
    }

    /**
     * Check whether the IPV6 address of compressed or uncompressed.
     *
     * @param input IPV6 address.
     * @return True or false.
     * @see #isIPv6HexCompressedAddress(String)
     * @see #isIPv6StdAddress(String)
     */
    public static boolean isIPv6Address(final String input) {
        return isIPv6StdAddress(input) || isIPv6HexCompressedAddress(input);
    }

}
