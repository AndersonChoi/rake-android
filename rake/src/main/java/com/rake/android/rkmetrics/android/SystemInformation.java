package com.rake.android.rkmetrics.android;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.rake.android.rkmetrics.util.Logger;

import java.util.Locale;

import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_VALUE_NETWORK_TYPE_NOT_WIFI;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_VALUE_NETWORK_TYPE_WIFI;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_VALUE_UNKNOWN;

/**
 * Gather android system dependent information
 */
public final class SystemInformation {

    public static String getOsVersion() {
        return TextUtils.isEmpty(Build.VERSION.RELEASE) ? PROPERTY_VALUE_UNKNOWN : Build.VERSION.RELEASE;
    }

    public static String getManufacturer() {
        return TextUtils.isEmpty(Build.MANUFACTURER) ? PROPERTY_VALUE_UNKNOWN : Build.MANUFACTURER;
    }

    public static String getDeviceModel() {
        return TextUtils.isEmpty(Build.MODEL) ? PROPERTY_VALUE_UNKNOWN : Build.MODEL;
    }

    public static String getDeviceId(Context context) {
        if (context == null) {
            return PROPERTY_VALUE_UNKNOWN;
        }

        String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        return TextUtils.isEmpty(deviceId) ? PROPERTY_VALUE_UNKNOWN : deviceId;
    }

    public static String getAppVersionName(Context context) {
        if (context == null) {
            return PROPERTY_VALUE_UNKNOWN;
        }

        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo info = packageManager.getPackageInfo(packageName, 0);
            return info.versionName;
        } catch (Exception e) {
            Logger.e("Can't get versionName from PackageInfo");
        }

        return PROPERTY_VALUE_UNKNOWN;
    }

    public static int getAppVersionCode(Context context) {
        if (context == null) {
            return 0;
        }

        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo info = packageManager.getPackageInfo(packageName, 0);
            return info.versionCode;
        } catch (Exception e) {
            Logger.e("Can't get versionName from PackageInfo");
        }

        return 0;
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        if (context == null) {
            return null;
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getMetrics(displayMetrics);

        return displayMetrics;
    }

    @Deprecated
    public static String getPhoneRadioType(Context context) {
        if (context == null) {
            return PROPERTY_VALUE_UNKNOWN;
        }

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            return PROPERTY_VALUE_UNKNOWN;
        }

        switch (telephonyManager.getPhoneType()) {
            case TelephonyManager.PHONE_TYPE_NONE:
                return "none";
            case TelephonyManager.PHONE_TYPE_GSM:
                return "gsm";
            case TelephonyManager.PHONE_TYPE_CDMA:
                return "cdma";
            case TelephonyManager.PHONE_TYPE_SIP:
                return "sip";
            default:
                return null;
        }
    }

    // Note this is the *current*, not the canonical network, because it
    // doesn't require special permissions to access. Unreliable for CDMA phones,
    public static String getCurrentNetworkOperator(Context context) {
        if (context == null) {
            return null;
        }

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String carrier = telephonyManager != null ? telephonyManager.getNetworkOperatorName() : null;

        return TextUtils.isEmpty(carrier) ? PROPERTY_VALUE_UNKNOWN : carrier;
    }

    public static String getWifiConnected(Context context) {
        if (context == null) {
            return PROPERTY_VALUE_NETWORK_TYPE_NOT_WIFI;
        }

        boolean isWiFi = false;

        if (PackageManager.PERMISSION_GRANTED == context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                networkInfo = connManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    isWiFi = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
                }
            } else {
                networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (networkInfo != null) {
                    isWiFi = networkInfo.isConnected();
                }
            }
        }

        return isWiFi ? PROPERTY_VALUE_NETWORK_TYPE_WIFI : PROPERTY_VALUE_NETWORK_TYPE_NOT_WIFI;
    }

    public static String getPackageName(Context context) {
        if (context == null) {
            return PROPERTY_VALUE_UNKNOWN;
        }

        try {
            PackageManager packageManager = context.getApplicationContext().getPackageManager();
            ApplicationInfo ai = packageManager.getApplicationInfo(context.getPackageName(), 0);
            return packageManager.getApplicationLabel(ai).toString();
        } catch (Exception e) {
            Logger.e("Can't get ApplicationLabel"); /* trivial, DO NOT print stacktrace */
        }

        return PROPERTY_VALUE_UNKNOWN;
    }

    public static boolean isDozeModeEnabled(Context context) {
        if (context == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Marshmallow 버전 미만은 Doze Mode가 없음. 따라서 항상 false.
            return false;
        }

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager != null && powerManager.isDeviceIdleMode();

    }

    public static String getLanguageCode(Context context) {
        if (context == null) {
            return null;
        }

        Configuration configuration = context.getResources().getConfiguration();
        Locale locale = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? configuration.getLocales().get(0) : configuration.locale;
        return locale.getCountry().toUpperCase();
    }

    public static String getCountryCodeFromUSIM(Context context) {
        if (context == null) {
            return null;
        }

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            String usimISO = telephonyManager.getSimCountryIso();
            return (TextUtils.isEmpty(usimISO) || usimISO.length() != 2) ? null : usimISO.toUpperCase();
        }

        return null;
    }

    public static String getCountryCodeFromNetwork(Context context) {
        if (context == null) {
            return null;
        }

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null || telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
            // CDMA 네트워크 상태에서 획득되는 정보는 신뢰할 수 없음 (API 문서 참고)
            return null;
        }

        String networkISO = telephonyManager.getNetworkCountryIso();
        return (TextUtils.isEmpty(networkISO) || networkISO.length() != 2) ? null : networkISO.toUpperCase();
    }
}
