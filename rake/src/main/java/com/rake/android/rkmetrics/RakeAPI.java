package com.rake.android.rkmetrics;


import static com.rake.android.rkmetrics.config.RakeConfig.LOG_TAG_PREFIX;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.*;

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

    public JSONObject getSuperProperties() {
        JSONObject props = new JSONObject();

        synchronized (superProperties) {
            for (Iterator<?> keys = superProperties.keys(); keys.hasNext(); ) {
                String key = (String) keys.next();

                try { props.put(key, superProperties.get(key)); }
                catch (JSONException e) { /* logging and ignore it */
                    Logger.e("Failed to insert a super property", e);
                }
            }
        }

        return props;
    }

    /**
     * Save JSONObject created using Shuttle.toJSONObject() into SQLite.
     * RakeAPI will flush immediately if RakeAPI.Env.DEV is set see {@link #flush()}
     *
     * @param shuttle pass Shuttle.toJSONObject();
     */
    public void track(JSONObject shuttle) {
        if (!isShuttle(shuttle)) {
            Logger.e(tag, "Passed JSONObject is null or was not created using Shuttle.toJSONObject");
            return;
        }


        JSONObject superProps = getSuperProperties();

        JSONObject validShuttleFormat = _track(shuttle, superProps);


        if (null == validShuttleFormat) return;

        String uri = endpoint.getURI(env);
        Log log = Log.create(uri, token, validShuttleFormat);

        synchronized (messageLoop) {
            if (messageLoop.track(log))
                Logger.d(tag, "Tracked JSONObject\n" + validShuttleFormat);
            if (Env.DEV == env) messageLoop.flush(); /* if Env.DEV, flush immediately */
        }
    }

    private JSONObject _track(JSONObject shuttle, JSONObject superProps) {
        if (null == shuttle || null == superProps)
            return null;

        Date now = new Date();
        JSONObject validShuttleFormat = new JSONObject();

        try {
            // 1. super properties TODO: remove
            JSONObject sentinel_meta = shuttle.getJSONObject(FIELD_NAME_SENTINEL_META);
            for (Iterator<?> iter = sentinel_meta.keys(); iter.hasNext(); ) {
                String key = (String) iter.next();
                validShuttleFormat.put(key, sentinel_meta.get(key));
            }
            shuttle.remove(FIELD_NAME_SENTINEL_META);

            JSONObject fieldOrder = validShuttleFormat.getJSONObject(META_FIELD_NAME_FIELD_ORDER);

            // 2. Insert user-collected fields
            for (Iterator<?> keys = shuttle.keys(); keys.hasNext(); ) {
                String key = (String) keys.next();
                Object value = shuttle.get(key);

                if (superProps.has(key) && value.toString().length() == 0) {
                    // DO NOT overwrite superProps if user inserted nothing
                } else superProps.put(key, value);
            }

            // 3. Insert auto-collected fields including token, base_time, local_time
            // TODO token, time -> getDefault
            JSONObject defaultProps = getDefaultProps();

            superProps.put(PROPERTY_NAME_TOKEN, token);
            superProps.put(PROPERTY_NAME_BASE_TIME, TimeUtil.getBaseFormatter().format(now));
            superProps.put(PROPERTY_NAME_LOCAL_TIME, TimeUtil.getLocalFormatter().format(now));

            for (Iterator<?> keys = defaultProps.keys(); keys.hasNext(); ) {
                String key = (String) keys.next();
                boolean addToProperties = true;

                if (fieldOrder.has(key)) addToProperties = true;
                else addToProperties = false;

                if (addToProperties) { superProps.put(key, defaultProps.get(key)); }
            }

            // 4. put properties
            validShuttleFormat.put(FIELD_NAME_PROPERTIES, superProps);

        } catch (Exception e) {
            Logger.e(tag, "Failed to track", e);
            return null;
        }

        return validShuttleFormat;
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
        Logger.d(tag, "Flush");

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

    private JSONObject getDefaultProps() throws JSONException {
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
