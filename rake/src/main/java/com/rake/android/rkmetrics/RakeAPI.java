package com.rake.android.rkmetrics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.DisplayMetrics;
import com.rake.android.rkmetrics.android.SystemInformation;
import com.rake.android.rkmetrics.config.RakeConfig;
import com.rake.android.rkmetrics.util.RakeLogger;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import static com.rake.android.rkmetrics.config.RakeConfig.DEV_HOST;
import static com.rake.android.rkmetrics.config.RakeConfig.LIVE_HOST;
import static com.rake.android.rkmetrics.config.RakeConfig.LOG_TAG_PREFIX;

public class RakeAPI {

    public enum Logging {
        DISABLE("DISABLE"), ENABLE("ENABLE");

        private String mode;
        Logging(String mode) { this.mode = mode; }
        @Override public String toString() { return mode; }
    }

    public enum Env {
        LIVE("LIVE"), DEV("DEV");

        private String env;
        Env(String env) { this.env = env; }
        @Override public String toString() { return this.env; }
    }

    private static Map<String, Map<Context, RakeAPI>> sInstanceMap = new HashMap<String, Map<Context, RakeAPI>>();
    // TODO: move into MessageHandler
    private static String baseEndpoint = RakeConfig.EMPTY_BASE_ENDPOINT;

    private Env env = Env.DEV;
    private final String loggingTag;
    private final Context context;
    private final SystemInformation sysInfo;
    private final MessageLoop rakeMessageDelegator;
    private final String token;
    private final SharedPreferences storedPreferences;
    private JSONObject superProperties; /* the place where persistent members loaded and stored */

    private final static ArrayList<String> defaultValueBlackList = new ArrayList<String>() {{
        // black list
        // usage: add("mdn");
    }};

    private RakeAPI(Context appContext, String token) {
        this.context = appContext;
        this.token = token;
        this.loggingTag = String.format("%s[%s]", RakeConfig.LOG_TAG_PREFIX, token);

        rakeMessageDelegator = MessageLoop.getInstance(appContext);
        sysInfo = getSystemInformation();

        storedPreferences = appContext.getSharedPreferences("com.rake.android.rkmetrics.RakeAPI_" + token, Context.MODE_PRIVATE);
        readSuperProperties();
    }

    /**
     * Create RakeAPI instance. (singleton per {@code token})
     *
     * @param context       Android Application Context
     * @param token         Rake Token
     * @param isDevServer   indicate whether RakeAPI send log to development server or live server
     *                      If {@code false} used,
     *                      RakeAPI will send log to <strong>development server</strong> ({@code pg.rake.skplanet.com})
     *                      If {@code true} is used,
     *                      RakeAPI will send log to <strong>live server </strong> ({@code rake.skplanet.com}).
     *
     *
     * @throws IllegalArgumentException if RakeAPI called multiple times with different {@code isDevServer} value.
     * @deprecated          As of 0.3.17, replaced by {@link #getInstance(Context, String, Env, Logging)}}
     */
    public static RakeAPI getInstance(Context context, String token, Boolean isDevServer) {
        Env env = (isDevServer == true) ? Env.DEV : Env.LIVE;
        return getInstance(context, token, env, RakeLogger.loggingMode /* use current logging mode */);
    }

    /**
     * Create RakeAPI instance. (singleton per {@code token})
     *
     * @param context Android Application Context
     * @param token   Rake Token
     * @param env     indicate whether RakeAPI send log to development server or live server
     *                If {@link com.rake.android.rkmetrics.RakeAPI.Env#DEV} used,
     *                RakeAPI will send log to <strong>development server</strong> ({@code pg.rake.skplanet.com})
     *                If {@link com.rake.android.rkmetrics.RakeAPI.Env#LIVE} used,
     *                RakeAPI will send log to <strong>live server </strong> ({@code rake.skplanet.com})
     *
     * @throws IllegalArgumentException if RakeAPI called multiple times with different {@code RakeAPI.Env}.
     * @param         loggingMode Logging.ENABLE or Logging.DISABLE
     */
    public static RakeAPI getInstance(Context context, String token, Env env, Logging loggingMode) {
        setLogging(loggingMode);

        synchronized (sInstanceMap) {
            Context appContext = context.getApplicationContext();
            Map<Context, RakeAPI> instances = sInstanceMap.get(token);

            if (instances == null) {
                instances = new HashMap<Context, RakeAPI>();
                sInstanceMap.put(token, instances);
            }

            RakeAPI rake = instances.get(appContext);

            if (rake == null) {
                // url should be set before initializing rake instance
                if (Env.DEV == env) setBaseEndpoint(RakeConfig.DEV_BASE_ENDPOINT);
                else setBaseEndpoint(RakeConfig.LIVE_BASE_ENDPOINT);

                rake = new RakeAPI(appContext, token);
                instances.put(appContext, rake);
                rake.env = env;
            }

            return rake;
        }
    }

    /**
     * Set flush interval
     *
     * @param context android application context
     * @param milliseconds flush interval (milliseconds)
     */
    public static void setFlushInterval(Context context, long milliseconds) {
        MessageLoop.setFlushInterval(milliseconds);
    }

    /**
     * Save JSONObject (shuttle) into SQLite.
     * RakeAPI will flush immediately if RakeAPI.Env.DEV is set see {@link #flush()}
     *
     * @param shuttle pass Shuttle.getJSONObject();
     */
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
            propertiesObj.put("base_time", RakeConfig.baseTimeFormat.format(now));
            propertiesObj.put("local_time", RakeConfig.localTimeFormat.format(now));

            // 4. put properties
            dataObj.put("properties", propertiesObj);

            RakeLogger.d(loggingTag, "track() called\n" + dataObj);

            synchronized (rakeMessageDelegator) { rakeMessageDelegator.track(dataObj); }
            if (Env.DEV == env) { flush(); }

        } catch (JSONException e) {
            RakeLogger.e(loggingTag, "Exception tracking event ", e);
        }
    }

    public void setRakeServer(Context context, String server) {
        RakeAPI.setBaseEndpoint(server);
    }

    /**
     * @param baseEndpoint
     * @throws IllegalArgumentException if RakeAPI called multiple times with different {@code baseEndpoint}.
     */
    private static synchronized void setBaseEndpoint(String baseEndpoint) throws IllegalArgumentException {
        if (null == baseEndpoint) {
            RakeLogger.e(LOG_TAG_PREFIX, "MessageLoop.baseEndpoint can't be null");
        }

        RakeAPI.checkInvalidEndpoint(baseEndpoint);

        RakeAPI.baseEndpoint = baseEndpoint;
        RakeLogger.d(LOG_TAG_PREFIX, "Set endpoint to " + baseEndpoint);
    }

    private static void checkInvalidEndpoint(String baseEndpoint) {
        /*
         * MessageLoop have only one host type (DEV_HOST or LIVE_HOST). not both of them
         * See, JIRA RAKE-390
         */

        if  ((RakeAPI.baseEndpoint.startsWith(DEV_HOST) && baseEndpoint.startsWith(LIVE_HOST)) ||
                (RakeAPI.baseEndpoint.startsWith(LIVE_HOST) && baseEndpoint.startsWith(DEV_HOST))) {
            throw new IllegalArgumentException(
                    "Can't use both RakeAPI.Env.DEV and RakeAPI.Env.LIVE at the same time");
        }
    }

    /* package */ static synchronized String getBaseEndpoint() {
        return RakeAPI.baseEndpoint;
    }

    /**
     * Enable or disable logging.
     *
     * @param debug indicate whether enable logging or not
     * @deprecated As of 0.3.17, replaced by
     *             {@link #setLogging(Logging)}
     *             {@link #getInstance(Context, String, Env, Logging)}
     */

    public static void setDebug(Boolean debug) {
        if (debug)  setLogging(Logging.ENABLE);
        else setLogging(Logging.DISABLE);
    }

    /**
     * Enable or disable logging.
     *
     * @param loggingMode Logging.ENABLE or Logging.DISABLE
     * @see {@link com.rake.android.rkmetrics.RakeAPI.Logging}
     */
    public static void setLogging(Logging loggingMode) {
        RakeLogger.loggingMode = loggingMode;
    }

    /**
     * Send log which persisted in SQLite to Rake server.
     */
    public void flush() {
        RakeLogger.d(loggingTag, "flush() called");

        synchronized (rakeMessageDelegator) {
            rakeMessageDelegator.flush();
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
        ret.put("rake_lib_version", RakeConfig.RAKE_LIB_VERSION);
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
        if (Env.DEV == env && null != appBuildDate) appVersionName += "_" + sysInfo.getAppBuildDate();
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

    private SystemInformation getSystemInformation() {
        return new SystemInformation(context);
    }

    void clearPreferences() {
        // Will clear distinct_ids, superProperties,
        // and waiting People Analytics properties. Will have no effect
        // on MessageLoop which was already queued to send
        SharedPreferences.Editor prefsEdit = storedPreferences.edit();
        prefsEdit.clear().commit();
        readSuperProperties();
    }
}
