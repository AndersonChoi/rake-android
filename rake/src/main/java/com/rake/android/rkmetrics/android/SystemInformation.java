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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_VALUE_UNKNOWN;

/**
 * Gather android system dependent information
 */
public final class SystemInformation {

    public static String getAppBuildDate(Context context) {
        if (context == null) {
            return PROPERTY_VALUE_UNKNOWN;
        }

        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();

        try {
            ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            TimeZone tz = TimeZone.getDefault(); /* current TimeZone */
            formatter.setTimeZone(tz);
            zf.close();

            return formatter.format(new Date(time));
        } catch (Exception e) {
            Logger.e("Can't get Build Date from classes.dex"); /* trivial, DO NOT print stacktrace */
        }

        return PROPERTY_VALUE_UNKNOWN;
    }

    public static String getDeviceId(Context context) {
        if (context == null) {
            return PROPERTY_VALUE_UNKNOWN;
        }

        String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        return deviceId != null ? deviceId : PROPERTY_VALUE_UNKNOWN;
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
            return -1;
        }

        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo info = packageManager.getPackageInfo(packageName, 0);
            return info.versionCode;
        } catch (Exception e) {
            Logger.e("Can't get versionName from PackageInfo");
        }

        return -1;
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

        return telephonyManager != null ? telephonyManager.getNetworkOperatorName() : null;
    }

    public static boolean isWifiConnected(Context context) {
        if (context == null) {
            return false;
        }

        if (PackageManager.PERMISSION_GRANTED == context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    return networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
                }
            } else {
                return connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
            }
        }

        return false;
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
            return (TextUtils.isEmpty(usimISO) || usimISO.length() != 0) ? null : usimISO.toUpperCase();
        }

        return null;
    }

    public static String getCountryCodeFromNetwork(Context context) {
        if (context == null) {
            return null;
        }

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            return null;
        }

        if (telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
            String networkISO = telephonyManager.getNetworkCountryIso();
            return (TextUtils.isEmpty(networkISO) || networkISO.length() != 0) ? null : networkISO.toUpperCase();
        }

        // CDMA 네트워크 상태에서 획득되는 정보는 신뢰할 수 없음 (API 문서 참고)
        return null;
    }
}
