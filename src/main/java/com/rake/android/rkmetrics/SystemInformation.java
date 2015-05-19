package com.rake.android.rkmetrics;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Abstracts away possibly non-present system information classes,
 * and handles permission-dependent queries for default system information.
 */
class SystemInformation {
    public static final String TAG = "RakeAPI";

    private Context context;
    private Boolean hasNFC;
    private Boolean hasTelephony;
    private DisplayMetrics displayMetrics;
    private String appVersionName;
    private Integer appVersionCode;
    private String deviceId;
    private String appBuildDate; /* for dev environment */

    public SystemInformation(Context context) {
        this.context = context;

        PackageManager pm = context.getPackageManager();

        // config versionName, versionCode, buildDate
        configAppVersionAndBuildNumber(pm);
        appBuildDate = configAppBuildDate(pm);

        // We can't count on these features being available, since we need to
        // run on old devices. Thus, the reflection fandango below...
        Class<? extends PackageManager> packageManagerClass = pm.getClass();

        Method hasSystemFeatureMethod = null;
        try {
            hasSystemFeatureMethod = packageManagerClass.getMethod("hasSystemFeature", String.class);
        } catch (NoSuchMethodException e) {
            // Nothing, this is an expected outcome
        }

        Boolean foundNFC = null;
        Boolean foundTelephony = null;
        if (null != hasSystemFeatureMethod) {
            try {
                foundNFC = (Boolean) hasSystemFeatureMethod.invoke(pm, "android.hardware.nfc");
                foundTelephony = (Boolean) hasSystemFeatureMethod.invoke(pm, "android.hardware.telephony");
            } catch (InvocationTargetException e) {
                Log.w(TAG, "System version appeared to support PackageManager.hasSystemFeature, but we were unable to call it.");
            } catch (IllegalAccessException e) {
                Log.w(TAG, "System version appeared to support PackageManager.hasSystemFeature, but we were unable to call it.");
            }
        }

        hasNFC = foundNFC;
        hasTelephony = foundTelephony;
        displayMetrics = new DisplayMetrics();

        Display display = ((WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getMetrics(displayMetrics);

        deviceId = Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getAppBuildDate() {
        return appBuildDate;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getAppVersionName() {
        return appVersionName;
    }

    public Integer getAppVersionCode() {
        return appVersionCode;
    }

    public boolean hasNFC() {
        return hasNFC;
    }

    public boolean hasTelephony() {
        return hasTelephony;
    }

    public DisplayMetrics getDisplayMetrics() {
        return displayMetrics;
    }

    public String getPhoneRadioType() {
        String ret = null;

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != telephonyManager) {
            switch (telephonyManager.getPhoneType()) {
                case 0x00000000: // TelephonyManager.PHONE_TYPE_NONE
                    ret = "none";
                    break;
                case 0x00000001: // TelephonyManager.PHONE_TYPE_GSM
                    ret = "gsm";
                    break;
                case 0x00000002: // TelephonyManager.PHONE_TYPE_CDMA
                    ret = "cdma";
                    break;
                case 0x00000003: // TelephonyManager.PHONE_TYPE_SIP
                    ret = "sip";
                    break;
                default:
                    ret = null;
            }
        }

        return ret;
    }

    private void configAppVersionAndBuildNumber(PackageManager pm) {
        // set App VersionName, VersionCode
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            appVersionName = packageInfo.versionName;
            appVersionCode = packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            Log.w(TAG, "System information constructed with a context that apparently doesn't exist.");
        }
    }

    public String configAppBuildDate(PackageManager pm) {
        String buildDate = null;

        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            TimeZone tz = TimeZone.getTimeZone("Asia/Seoul");
            formatter.setTimeZone(tz);

            buildDate = formatter.format(new Date(time));

            zf.close();
        } catch(NameNotFoundException e) {
            Log.e(TAG, "System information constructed with a context that apparently doesn't exist.");
        } catch(IOException e) {
            Log.e(TAG, "Can't create ZipFile Instance using given ApplicationInfo");
        }

        return buildDate;
    }

    // Note this is the *current*, not the canonical network, because it
    // doesn't require special permissions to access. Unreliable for CDMA phones,
    public String getCurrentNetworkOperator() {
        String ret = null;

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != telephonyManager)
            ret = telephonyManager.getNetworkOperatorName();

        return ret;
    }

    public Boolean isWifiConnected() {
        Boolean ret = null;

        if (PackageManager.PERMISSION_GRANTED == context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
            ConnectivityManager connManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            ret = wifiInfo.isConnected();
        }

        return ret;
    }

}
