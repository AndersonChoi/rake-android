package com.rake.android.rkmetrics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.DisplayMetrics;
import com.rake.android.rkmetrics.android.SystemInformation;
import com.rake.android.rkmetrics.config.RakeConfig;
import com.rake.android.rkmetrics.config.RakeLoggingMode;
import com.rake.android.rkmetrics.core.WorkerSupervisor;
import com.rake.android.rkmetrics.util.RakeLogger;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author SK Planet
 */
public class RakeAPI {

    // TODO: remove r0.5.0_c. it requires to modify server dep.
    // version number will be replaced automatically when building.
    public static final String RAKE_LIB_VERSION = "r0.5.0_c0.3.17";

    private static final DateFormat baseTimeFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private static final DateFormat localTimeFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    static { baseTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); }

    private static Map<String, Map<Context, RakeAPI>> sInstanceMap = new HashMap<String, Map<Context, RakeAPI>>();

    private boolean isDev = false;
    private final String loggingTag;
    private final Context context;
    private final SystemInformation sysInfo;
    private final WorkerSupervisor workerSupervisor;
    private final String token;
    private final SharedPreferences storedPreferences;
    private JSONObject superProperties; /* the place where persistent members loaded and stored */

    private final static ArrayList<String> defaultValueBlackList = new ArrayList<String>() {{
        // black list
        // usage: add("mdn");
    }};

    private RakeAPI(Context context, String token) {
        this.context = context;
        this.token = token;
        this.loggingTag = String.format("%s[%s]", RakeConfig.LOG_TAG_PREFIX, token);

        workerSupervisor = getAnalyticsMessages();
        sysInfo = getSystemInformation();

        storedPreferences = context.getSharedPreferences("com.rake.android.rkmetrics.RakeAPI_" + token, Context.MODE_PRIVATE);
        readSuperProperties();
    }

    public static RakeAPI getInstance(Context context, String token, Boolean isDevServer) {
        synchronized (sInstanceMap) {
            Context appContext = context.getApplicationContext();
            Map<Context, RakeAPI> instances = sInstanceMap.get(token);

            if (instances == null) {
                instances = new HashMap<Context, RakeAPI>();
                sInstanceMap.put(token, instances);
            }

            RakeAPI instance = instances.get(appContext);

            if (instance == null) {
                instance = new RakeAPI(appContext, token);
                instances.put(appContext, instance);

                instance.isDev = isDevServer;
                if (isDevServer) {
                    instance.setRakeServer(context, RakeConfig.DEV_BASE_ENDPOINT);
                } else {
                    instance.setRakeServer(context, RakeConfig.LIVE_BASE_ENDPOINT);
                }
            }
            return instance;
        }
    }

    public static void setFlushInterval(Context context, long milliseconds) {
        WorkerSupervisor msgs = WorkerSupervisor.getInstance(context);
        msgs.setFlushInterval(milliseconds);
    }

    public void track(JSONObject shuttle) {
        if (null == shuttle) {
            RakeLogger.e(loggingTag, "should not pass null into RakeAPI.track()");
            return;
        }

        Date now = new Date();

        try {
            JSONObject dataObj = new JSONObject();
            JSONObject propertiesObj = new JSONObject();

            // 1. super properties
            synchronized (superProperties) {
                for (Iterator<?> keys = superProperties.keys(); keys.hasNext(); ) {
                    String key = (String) keys.next();
                    propertiesObj.put(key, superProperties.get(key));
                }
            }

            JSONObject sentinel_meta;
            if (shuttle.has("sentinel_meta")) {
                sentinel_meta = shuttle.getJSONObject("sentinel_meta");
                for (Iterator<?> sentinel_meta_keys = sentinel_meta.keys(); sentinel_meta_keys.hasNext(); ) {
                    String sentinel_meta_key = (String) sentinel_meta_keys.next();
                    dataObj.put(sentinel_meta_key, sentinel_meta.get(sentinel_meta_key));
                }
                shuttle.remove("sentinel_meta");
            } else {
                // no sentinel shuttle
                // need to do something here?
                // get/make sentinel_meta for this project
            }

            JSONObject fieldOrder;
            try {
                fieldOrder = (JSONObject) dataObj.get("_$fieldOrder");
            } catch (JSONException e) {
                fieldOrder = null;
            }

            // 2-2. custom properties
            if (shuttle != null) {
                for (Iterator<?> keys = shuttle.keys(); keys.hasNext(); ) {
                    String key = (String) keys.next();
                    if (fieldOrder != null && fieldOrder.has(key)) {    // field defined in schema
                        if (propertiesObj.has(key) && shuttle.get(key).toString().length() == 0) {
                            // Do not overwrite super properties with empty string of properties.
                        } else {
                            propertiesObj.put(key, shuttle.get(key));
                        }
                    } else if (fieldOrder == null) { // no fieldOrder (maybe no shuttle)
                        propertiesObj.put(key, shuttle.get(key));
                    }
                }
            }

            // 3. auto : device info
            // get only values in fieldOrder
            JSONObject defaultProperties = getDefaultEventProperties();
            if (defaultProperties != null) {
                for (Iterator<?> keys = defaultProperties.keys(); keys.hasNext(); ) {
                    String key = (String) keys.next();
                    boolean addToProperties = true;

                    if (fieldOrder != null) {
                        if (fieldOrder.has(key)) { addToProperties = true; }
                        else { addToProperties = false; }

                    } else if (defaultValueBlackList.contains(key)) {
                        addToProperties = false;
                    }

                    if (addToProperties) { propertiesObj.put(key, defaultProperties.get(key)); }
                }
            }

            // rake token
            propertiesObj.put("token", token);

            // time
            propertiesObj.put("base_time", baseTimeFormat.format(now));
            propertiesObj.put("local_time", localTimeFormat.format(now));

            // 4. put properties
            dataObj.put("properties", propertiesObj);

            synchronized (workerSupervisor) { workerSupervisor.track(dataObj); }
            if (isDev) { flush(); }

        } catch (JSONException e) {
            RakeLogger.e(loggingTag, "Exception tracking event ", e);
        }
    }

    public void setRakeServer(Context context, String server) {
        WorkerSupervisor msgs = WorkerSupervisor.getInstance(context);
        msgs.setEndpointHost(server);
    }

    /**
     * enable, disable logging
     *
     * @param debug indicate whether enable logging or not
     * @deprecated As of 0.3.17, replaced by
     *             {@link #enableLogging(RakeLoggingMode)}
     */
    public static void setDebug(Boolean debug) {
        if (debug)  enableLogging(RakeLoggingMode.YES);
        else enableLogging(RakeLoggingMode.NO);
    }

    /**
     * enable, disable logging
     *
     * @param loggingMode RakeLoggingMode.YES or RakeLoggingMode.NO
     * @see com.rake.android.rkmetrics.config.RakeLoggingMode
     */
    public static void enableLogging(RakeLoggingMode loggingMode) {
        RakeLogger.loggingMode = loggingMode;
    }

    public void flush() {
        RakeLogger.d(loggingTag, "flush");

        synchronized (workerSupervisor) {
            workerSupervisor.flush();
        }
    }

    public void registerSuperProperties(JSONObject superProperties) {
        RakeLogger.d(loggingTag, "registerSuperProperties");

        for (Iterator<?> iter = superProperties.keys(); iter.hasNext(); ) {
            String key = (String) iter.next();
            try {
                synchronized (this.superProperties) {
                    this.superProperties.put(key, superProperties.get(key));
                }
            } catch (JSONException e) {
                RakeLogger.e(loggingTag, "Exception registering super property.", e);
            }
        }

        storeSuperProperties();
    }

    public void unregisterSuperProperty(String superPropertyName) {
        RakeLogger.d(loggingTag, "unregisterSuperProperty");
        synchronized (superProperties) { superProperties.remove(superPropertyName); }
        storeSuperProperties();
    }


    public void registerSuperPropertiesOnce(JSONObject superProperties) {
        RakeLogger.d(loggingTag, "registerSuperPropertiesOnce");

        for (Iterator<?> iter = superProperties.keys(); iter.hasNext(); ) {
            String key = (String) iter.next();
            synchronized (this.superProperties) {
                if (!this.superProperties.has(key)) {
                    try {
                        this.superProperties.put(key, superProperties.get(key));
                    } catch (JSONException e) {
                        RakeLogger.e(loggingTag, "Exception registering super property.", e);
                    }
                }
            }
        }

        storeSuperProperties();
    }

    public synchronized void clearSuperProperties() {
        RakeLogger.d(loggingTag, "clearSuperProperties");
        superProperties = new JSONObject();
    }

    private JSONObject getDefaultEventProperties() throws JSONException {
        JSONObject ret = new JSONObject();

        ret.put("rake_lib", "android");
        ret.put("rake_lib_version", RAKE_LIB_VERSION);
        ret.put("os_name", "Android");
        ret.put("os_version", Build.VERSION.RELEASE == null ? "UNKNOWN" : Build.VERSION.RELEASE);
        ret.put("manufacturer", Build.MANUFACTURER == null ? "UNKNOWN" : Build.MANUFACTURER);
        ret.put("device_model", Build.MODEL == null ? "UNKNOWN" : Build.MODEL);
        ret.put("device_id", sysInfo.getDeviceId());

        DisplayMetrics displayMetrics = sysInfo.getDisplayMetrics();
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        StringBuilder resolutionBuilder = new StringBuilder();

        // TODO
        ret.put("screen_height", displayWidth);
        ret.put("screen_width", displayHeight);
        ret.put("resolution", resolutionBuilder.append(displayWidth).append("*").append(displayHeight).toString());

        // application versionName, buildDate(iff dev mode)
        String appVersionName = sysInfo.getAppVersionName();
        String appBuildDate = sysInfo.getAppBuildDate();
        if (isDev && null != appBuildDate) appVersionName += "_" + sysInfo.getAppBuildDate();
        ret.put("app_version", appVersionName == null ? "UNKNOWN" : appVersionName);

        String carrier = sysInfo.getCurrentNetworkOperator();
        ret.put("carrier_name", (null != carrier && carrier.length() > 0) ? carrier : "UNKNOWN");

        Boolean isWifi = sysInfo.isWifiConnected();
        ret.put("network_type", isWifi == null ? "UNKNOWN" : isWifi.booleanValue() == true ? "WIFI" : "NOT WIFI");

        ret.put("language_code", context.getResources().getConfiguration().locale.getCountry());

        return ret;
    }

    private void readSuperProperties() {
        String props = storedPreferences.getString("super_properties", "{}");
        RakeLogger.d(loggingTag, "Loading Super Properties " + props);

        try {
            superProperties = new JSONObject(props);
        } catch (JSONException e) {
            RakeLogger.e(loggingTag, "Cannot parse stored superProperties");
            superProperties = new JSONObject();
            storeSuperProperties();
        }
    }

    private void storeSuperProperties() {
        String props = superProperties.toString();

        RakeLogger.d(loggingTag, "Storing Super Properties " + props);
        SharedPreferences.Editor prefsEditor = storedPreferences.edit();
        prefsEditor.putString("super_properties", props);
        prefsEditor.commit();   // synchronous
    }


    private WorkerSupervisor getAnalyticsMessages() {
        return WorkerSupervisor.getInstance(context);
    }

    private SystemInformation getSystemInformation() {
        return new SystemInformation(context);
    }

    void clearPreferences() {
        // Will clear distinct_ids, superProperties,
        // and waiting People Analytics properties. Will have no effect
        // on workerSupervisor already queued to send with WorkerSupervisor.
        SharedPreferences.Editor prefsEdit = storedPreferences.edit();
        prefsEdit.clear().commit();
        readSuperProperties();
    }
}
