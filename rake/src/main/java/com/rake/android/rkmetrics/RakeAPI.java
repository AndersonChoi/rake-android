package com.rake.android.rkmetrics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.DisplayMetrics;
import com.rake.android.rkmetrics.android.SystemInformation;
import com.rake.android.rkmetrics.config.RakeConfig;
import com.rake.android.rkmetrics.network.Endpoint;
import com.rake.android.rkmetrics.persistent.Log;
import com.rake.android.rkmetrics.util.Logger;
import com.rake.android.rkmetrics.util.TimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import static com.rake.android.rkmetrics.config.RakeConfig.LOG_TAG_PREFIX;

public /* TODO final */ class RakeAPI {

    public enum Logging {
        DISABLE("DISABLE"), ENABLE("ENABLE");

        private String mode;
        Logging(String mode) { this.mode = mode; }
        @Override public String toString() { return mode; }
    }

    /**
     * 공개 API 의 일부이며 빌드스크립트에서 사용되는 ENUM 이므로 이름 또는 내부 값 변경시
     * rake-android/rake/build.gradle 도 변경할 것
     */
    public enum Env {
        LIVE("LIVE"),
        DEV("DEV");

        private final String env;
        Env(String env) { this.env = env; }

        @Override public String toString() { return this.env; }
    }

    public enum AutoFlush {
        ON("ON"),
        OFF("OFF");

        private final String value;
        AutoFlush(String value) { this.value = value; }

        @Override
        public String toString() {
            return this.value;
        }
    }

    // TODO: remove nested map
    private static Map<String, Map<Context, RakeAPI>> sInstanceMap = new HashMap<String, Map<Context, RakeAPI>>();

    private String tag;

    private Endpoint endpoint;
    private final Env env;
    private final String token;

    private final Context context;
    private final SystemInformation sysInfo; // TODO: duplicated?
    private final MessageLoop messageLoop; /* singleton */
    private final SharedPreferences storedPreferences;
    private JSONObject superProperties; /* the place where persistent members loaded and stored */

    private RakeAPI(Context appContext, String token, Env env, Endpoint endpoint) {

        this.tag = createTag(LOG_TAG_PREFIX, token, env, endpoint);

        Logger.d(tag, "Creating instance");

        this.context = appContext;

        this.token = token;
        this.env = env;
        this.endpoint = endpoint;

        this.messageLoop = MessageLoop.getInstance(appContext);
        this.sysInfo = getSystemInformation();
        this.storedPreferences = appContext.getSharedPreferences("com.rake.android.rkmetrics.RakeAPI_" + token, Context.MODE_PRIVATE);

        readSuperProperties();
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
     * @param         loggingMode Logging.ENABLE or Logging.DISABLE
     */
    public static RakeAPI getInstance(Context context,
                                      String token,
                                      Env env,
                                      Logging loggingMode) {
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
                Endpoint endpoint = Endpoint.DEFAULT;
                rake = new RakeAPI(appContext, token, env, endpoint);
                instances.put(appContext, rake);
            }

            return rake;
        }
    }

    /**
     * Set flush interval
     *
     * @param milliseconds flush interval (milliseconds)
     */
    public static void setFlushInterval(long milliseconds) {
        Logger.i("Set flush interval to " + milliseconds);
        MessageLoop.setFlushInterval(milliseconds);
    }

    public static long getFlushInterval() {
        return MessageLoop.getFlushInterval();
    }

    public static void setAutoFlush(AutoFlush autoFlush) {
        AutoFlush old = MessageLoop.getAutoFlushOption();
        String message = String.format("Set auto-flush option from %s to %s", old.name(), autoFlush.name());
        Logger.i(message);

        MessageLoop.setAutoFlushOption(autoFlush);
    }

    public static AutoFlush getAutoFlush() { return MessageLoop.getAutoFlushOption(); }

    private static String createTag(String prefix, String token, Env e, Endpoint ep) {
        return String.format("%s (%s, %s, %s)", prefix, token, e, ep);
    }

    private final static ArrayList<String> defaultValueBlackList = new ArrayList<String>() {{
        // black list
        // usage: add("mdn");
    }};

    /**
     * Save JSONObject created using Shuttle.toJSONObject() into SQLite.
     * RakeAPI will flush immediately if RakeAPI.Env.DEV is set see {@link #flush()}
     *
     * @param shuttle pass Shuttle.toJSONObject();
     */
    public void track(JSONObject shuttle) {
        if (null == shuttle) {
            Logger.e(tag, "should not pass null into RakeAPI.track()");
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
            // TODO: thread-unsafe
            propertiesObj.put("base_time", TimeUtil.getBaseFormatter().format(now));
            propertiesObj.put("local_time", TimeUtil.getLocalFormatter().format(now));

            // 4. put properties
            dataObj.put("properties", propertiesObj);

            Logger.d(tag, "track() called\n" + dataObj);

            String uri = endpoint.getURI(env);
            Log log = Log.create(uri, token, dataObj);

            if (null == log) {
                String message = String.format("Invalid `Log` object (TOKEN = [%s], URL = [%s]", token, uri);
                Logger.e(tag, message);
                return;
            }

            synchronized (messageLoop) { messageLoop.track(log); }
            if (Env.DEV == env) { flush(); } /* if Env.DEV, flush immediately */

        } catch (JSONException e) {
            Logger.e(tag, "Exception tracking event ", e);
        }
    }

    /**
     * Change end point
     *
     * - {@link com.rake.android.rkmetrics.network.Endpoint#CHARGED}
     * - {@link com.rake.android.rkmetrics.network.Endpoint#FREE}
     *
     * @param endpoint
     * @see {@link com.rake.android.rkmetrics.network.Endpoint}
     */
    public void setEndpoint(Endpoint endpoint) {
        Endpoint old = this.endpoint;
        this.tag = createTag(LOG_TAG_PREFIX, token, env, endpoint); /* update tag */
        this.endpoint = endpoint;

        String message = String.format("Changed endpoint from %s to %s", old, endpoint);
        Logger.d(tag, message);
    }

    /**
     * Get current end point
     *
     * @return endpoint
     * @see {@link com.rake.android.rkmetrics.network.Endpoint}
     */
    public Endpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Get token
     * @return String
     */
    public String getToken() { return token; }

    /**
     * Enable or disable logging.
     *
     * @param loggingMode Logging.ENABLE or Logging.DISABLE
     * @see {@link com.rake.android.rkmetrics.RakeAPI.Logging}
     */
    public static void setLogging(Logging loggingMode) {
        Logger.loggingMode = loggingMode;
    }

    /**
     * Send log which persisted in SQLite to Rake server.
     */
    public void flush() {
        Logger.d(tag, "flush() called");

        synchronized (messageLoop) {
            messageLoop.flush();
        }
    }

    /**
     * @deprecated as of 0.4.0
     */
    @Deprecated
    public void registerSuperProperties(JSONObject superProperties) {
        Logger.d(tag, "registerSuperProperties");

        for (Iterator<?> iter = superProperties.keys(); iter.hasNext(); ) {
            String key = (String) iter.next();
            try {
                synchronized (this.superProperties) {
                    this.superProperties.put(key, superProperties.get(key));
                }
            } catch (JSONException e) {
                Logger.e(tag, "Exception registering super property.", e);
            }
        }

        storeSuperProperties();
    }

    /**
     * @deprecated as of 0.4.0
     */
    @Deprecated
    public void unregisterSuperProperty(String superPropertyName) {
        Logger.d(tag, "unregisterSuperProperty");
        synchronized (superProperties) { superProperties.remove(superPropertyName); }
        storeSuperProperties();
    }

    /**
     * @deprecated as of 0.4.0
     */
    @Deprecated
    public void registerSuperPropertiesOnce(JSONObject superProperties) {
        Logger.d(tag, "registerSuperPropertiesOnce");

        for (Iterator<?> iter = superProperties.keys(); iter.hasNext(); ) {
            String key = (String) iter.next();
            synchronized (this.superProperties) {
                if (!this.superProperties.has(key)) {
                    try {
                        this.superProperties.put(key, superProperties.get(key));
                    } catch (JSONException e) {
                        Logger.e(tag, "Exception registering super property.", e);
                    }
                }
            }
        }

        storeSuperProperties();
    }

    /**
     * @deprecated as of 0.4.0
     */
    @Deprecated
    public synchronized void clearSuperProperties() {
        Logger.d(tag, "clearSuperProperties");
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

    /**
     * @deprecated as of 0.4.0
     */
    @Deprecated
    private void readSuperProperties() {
        String prefKey = createSharedPrefPropertyKey(token);
        String props = storedPreferences.getString(prefKey, "{}");
        Logger.d(tag, "Loading Super Properties " + props);

        try {
            superProperties = new JSONObject(props);
        } catch (JSONException e) {
            Logger.e(tag, "Cannot parse stored superProperties");
            superProperties = new JSONObject();
            storeSuperProperties();
        }
    }

    private static String createSharedPrefPropertyKey(String token) {
       return "super_properties_for_" + token;
    }

    /**
     * @deprecated as of 0.4.0
     */
    @Deprecated
    private void storeSuperProperties() {
        String prefKey = createSharedPrefPropertyKey(token);
        String props = superProperties.toString();

        Logger.d(tag, "Storing Super Properties " + props);
        SharedPreferences.Editor prefsEditor = storedPreferences.edit();
        prefsEditor.putString(prefKey, props);
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
