package com.rake.android.rkmetrics.android;

import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.*;
import com.rake.android.rkmetrics.util.Logger;


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
import android.view.Display;
import android.view.WindowManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Gather android system dependent information
 */
public final class SystemInformation { /** singleton */

    /** constructors */

    private SystemInformation() {}

    private SystemInformation(Context context) {
        this.context = context;

        PackageManager pm = context.getPackageManager();
        String name = context.getPackageName();

        // configure versionName, versionCode, buildDate
        appBuildDate = configAppBuildDate(pm, name);

        try {
            PackageInfo info = getPackageInfo(pm, name);
            appVersionName = info.versionName;
            appVersionCode = info.versionCode;
        } catch (NameNotFoundException e) {
            Logger.e("Can't get versionName, versionCode from PackageInfo");
        }

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
                Logger.w("System version appeared to support PackageManager.hasSystemFeature, but we were unable to call it.");
            } catch (IllegalAccessException e) {
                Logger.w("System version appeared to support PackageManager.hasSystemFeature, but we were unable to call it.");
            }
        }

        hasNFC = foundNFC;
        hasTelephony = foundTelephony;
        displayMetrics = new DisplayMetrics();

        Display display = ((WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getMetrics(displayMetrics);

        deviceId = Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        if (null == deviceId) deviceId = PROPERTY_VALUE_UNKNOWN;
    }

    /** instance members */

    private Context context;
    private Boolean hasNFC;
    private Boolean hasTelephony;
    private DisplayMetrics displayMetrics;
    private String appVersionName;
    private Integer appVersionCode;
    private String deviceId;
    private String appBuildDate; /* for dev environment */

    /** static members */

    private static SystemInformation instance;

    public static synchronized SystemInformation getInstance(Context context) {
        if (null == instance) instance = new SystemInformation(context.getApplicationContext());

        return instance;
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

    private PackageInfo getPackageInfo(PackageManager manager, String packageName)
        throws NameNotFoundException {
        return manager.getPackageInfo(packageName, 0);
    }

    public String configAppBuildDate(PackageManager manager, String packageName) {
        String buildDate = null;

        try {
            ApplicationInfo ai = manager.getApplicationInfo(packageName, 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            TimeZone tz = TimeZone.getDefault(); /* current TimeZone */
            formatter.setTimeZone(tz);

            buildDate = formatter.format(new Date(time));

            zf.close();
        } catch(NameNotFoundException e) {
            Logger.e("System information constructed with a context that apparently doesn't exist.");
        } catch(IOException e) {
            Logger.e("Can't create ZipFile Instance using given ApplicationInfo");
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

    public static String getPackageName(Context context) {
        String UNKNOWN_PACKAGE = "UNKNOWN";

        if (null == context) return UNKNOWN_PACKAGE;

        final PackageManager pm = context.getApplicationContext().getPackageManager();
        ApplicationInfo ai = null;

        try {
            ai = pm.getApplicationInfo(context.getPackageName(), 0);
        } catch (Exception e) { /* NameNotFoundException */
            Logger.e("Can't find getApplicationInfo", e);
        }

        return (ai == null) ? UNKNOWN_PACKAGE : (String) pm.getApplicationLabel(ai);
    }
}
