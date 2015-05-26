package com.rake.android.rkmetrics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import com.rake.android.rkmetrics.android.SystemInformation;
import com.rake.android.rkmetrics.core.WorkerSupervisor;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class RakeAPI {

    // TODO: remove r0.5.0_c. it requires to modify server dep.
    // version number will be replaced automatically when building.
    public static final String RAKE_LIB_VERSION = "r0.5.0_c0.3.16";
    private static final String TAG = "RakeAPI";

    private boolean isDev = false;

    private static final DateFormat baseTimeFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private static final DateFormat localTimeFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    static { baseTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); }

    // Maps each token to a singleton RakeAPI instance
    private static Map<String, Map<Context, RakeAPI>> sInstanceMap = new HashMap<String, Map<Context, RakeAPI>>();

    private final Context context;
    private final SystemInformation sysInfo;
    private final WorkerSupervisor am;
    private final String token;
    private final SharedPreferences storedPreferences;
    private JSONObject superProperties; /* the place where persistent members loaded and stored */

    // Device Info - black list
    private final static ArrayList<String> defaultValueBlackList = new ArrayList<String>() {{
//        add("mdn");
    }};

    private RakeAPI(Context context, String token) {
        this.context = context;
        this.token = token;

        am = getAnalyticsMessages();
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
                    instance.setRakeServer(context, RakeConfig.BASE_ENDPOINT);
                }
            }
            return instance;
        }
    }

    public static void setFlushInterval(Context context, long milliseconds) {
        WorkerSupervisor msgs = WorkerSupervisor.getInstance(context);
        msgs.setFlushInterval(milliseconds);
    }

    public void track(JSONObject properties) {
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
            if (properties.has("sentinel_meta")) {
                sentinel_meta = properties.getJSONObject("sentinel_meta");
                for (Iterator<?> sentinel_meta_keys = sentinel_meta.keys(); sentinel_meta_keys.hasNext(); ) {
                    String sentinel_meta_key = (String) sentinel_meta_keys.next();
                    dataObj.put(sentinel_meta_key, sentinel_meta.get(sentinel_meta_key));
                }
                properties.remove("sentinel_meta");
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
            if (properties != null) {
                for (Iterator<?> keys = properties.keys(); keys.hasNext(); ) {
                    String key = (String) keys.next();
                    if (fieldOrder != null && fieldOrder.has(key)) {    // field defined in schema
                        if (propertiesObj.has(key) && properties.get(key).toString().length() == 0) {
                            // Do not overwrite super properties with empty string of properties.
                        } else {
                            propertiesObj.put(key, properties.get(key));
                        }
                    } else if (fieldOrder == null) { // no fieldOrder (maybe no shuttle)
                        propertiesObj.put(key, properties.get(key));
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
                        if (fieldOrder.has(key)) {
                            addToProperties = true;
                        } else {
                            addToProperties = false;
                        }
                    } else if (defaultValueBlackList.contains(key)) {
                        addToProperties = false;
                    }

                    if (addToProperties) {
                        propertiesObj.put(key, defaultProperties.get(key));
                    }
                }
            }

            // rake token
            propertiesObj.put("token", token);

            // time
            propertiesObj.put("base_time", baseTimeFormat.format(now));
            propertiesObj.put("local_time", localTimeFormat.format(now));


            // 4. put properties
            dataObj.put("properties", propertiesObj);

            synchronized (am) {
                am.eventsMessage(dataObj);
            }

            if (isDev) {
                flush();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Exception tracking event ", e);
        }
    }

    public void setRakeServer(Context context, String server) {
        WorkerSupervisor msgs = WorkerSupervisor.getInstance(context);
        msgs.setEndpointHost(server);
    }

    public static void setDebug(Boolean debug) {
        RakeConfig.DEBUG = debug;
        Log.d(TAG, "RakeConfig.DEBUG : " + RakeConfig.DEBUG);
    }

    public void flush() {
        if (RakeConfig.DEBUG) {
            Log.d(TAG, "flushEvents");
        }
        synchronized (am) {
            am.postToServer();
        }
    }

    public void registerSuperProperties(JSONObject superProperties) {
        if (RakeConfig.DEBUG) {
            Log.d(TAG, "registerSuperProperties");
        }

        for (Iterator<?> iter = superProperties.keys(); iter.hasNext(); ) {
            String key = (String) iter.next();
            try {
                synchronized (this.superProperties) {
                    this.superProperties.put(key, superProperties.get(key));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Exception registering super property.", e);
            }
        }

        storeSuperProperties();
    }

    public void unregisterSuperProperty(String superPropertyName) {
        synchronized (superProperties) {
            superProperties.remove(superPropertyName);
        }

        storeSuperProperties();
    }


    public void registerSuperPropertiesOnce(JSONObject superProperties) {
        if (RakeConfig.DEBUG) {
            Log.d(TAG, "registerSuperPropertiesOnce");
        }

        for (Iterator<?> iter = superProperties.keys(); iter.hasNext(); ) {
            String key = (String) iter.next();
            synchronized (this.superProperties) {
                if (!this.superProperties.has(key)) {
                    try {
                        this.superProperties.put(key, superProperties.get(key));
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception registering super property.", e);
                    }
                }
            }
        }

        storeSuperProperties();
    }

    public synchronized void clearSuperProperties() {
        if (RakeConfig.DEBUG) {
            Log.d(TAG, "clearSuperProperties");
        }
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
        if (RakeConfig.DEBUG) {
            Log.d(TAG, "Loading Super Properties " + props);
        }

        try {
            superProperties = new JSONObject(props);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot parse stored superProperties");
            superProperties = new JSONObject();
            storeSuperProperties();
        }
    }

    private void storeSuperProperties() {
        String props = superProperties.toString();

        if (RakeConfig.DEBUG)
            Log.d(TAG, "Storing Super Properties " + props);
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
        // on am already queued to send with WorkerSupervisor.
        SharedPreferences.Editor prefsEdit = storedPreferences.edit();
        prefsEdit.clear().commit();
        readSuperProperties();
    }
}
