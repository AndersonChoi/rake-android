package com.rake.android.rkmetrics;


import static com.rake.android.rkmetrics.config.RakeConfig.LOG_TAG_PREFIX;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.DisplayMetrics;
import com.rake.android.rkmetrics.android.SystemInformation;
import com.rake.android.rkmetrics.config.RakeConfig;
import com.rake.android.rkmetrics.metric.MetricUtil;
import com.rake.android.rkmetrics.metric.model.Action;
import com.rake.android.rkmetrics.network.Endpoint;
import com.rake.android.rkmetrics.persistent.Log;
import com.rake.android.rkmetrics.util.Logger;
import com.rake.android.rkmetrics.util.TimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public final class RakeAPI {

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
    private final SharedPreferences storedPreferences;
    private JSONObject superProperties; /* the place where persistent members loaded and stored */

    private RakeAPI(Context appContext, String token, Env env, Endpoint endpoint) {

        this.tag = createTag(LOG_TAG_PREFIX, token, env, endpoint);

        Logger.d(tag, "Creating instance");

        this.context = appContext;

        this.token = token;
        this.env = env;
        this.endpoint = endpoint;

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
     * @param logging Logging.ENABLE or Logging.DISABLE
     *
     * @throws IllegalArgumentException if one of the parameters is NULL
     * @throws IllegalStateException if uncaught exception occurred while initializing
     */

    public static RakeAPI getInstance(Context context, String token, Env env, Logging logging) {
        if (null == context || null == token || null == env || null == logging) {
            throw new IllegalArgumentException("Can't initialize RakeAPI using NULL args");
        }

        /* 예외 기록을 위한 try-catch */
        try {
            return _getInstance(context, token, env, logging);
        } catch (Exception e) { /* should not be here */
            Logger.e("Failed to return RakeAPI instance");
            MetricUtil.recordErrorStatusMetric(context, Action.INSTALL, token, e);
            throw new IllegalStateException("Failed to return RakeAPI instance");
        }
    }

    private static RakeAPI _getInstance(Context context,
                                      String token,
                                      Env env,
                                      Logging logging) {

        long startAt = System.currentTimeMillis();

        setLogging(logging);

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

                long endAt = System.currentTimeMillis();

                /** record metric */
                MessageLoop.getInstance(context)
                        .queueInstallMetric(endAt - startAt, token, env, endpoint.getURI(env));
            } else {
                Logger.e("RakeAPI is already initialized for TOKEN ", token);
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

    public JSONObject getSuperProperties() throws JSONException {
        JSONObject props = new JSONObject();

        synchronized (superProperties) {
            for (Iterator<?> keys = superProperties.keys(); keys.hasNext(); ) {
                String key = (String) keys.next();

                props.put(key, superProperties.get(key));
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
        Date now = new Date();

        JSONObject superProps = null;
        JSONObject defaultProps = null;

        /* 최종 소비자 API 예외 처리 */
        try {
            superProps = getSuperProperties();
            defaultProps = getDefaultProps(context, env, token, now);

            JSONObject validShuttle = createValidShuttle(shuttle, superProps, defaultProps);

            String uri = endpoint.getURI(env);
            Log log = Log.create(uri, token, validShuttle);

            if (MessageLoop.getInstance(context).queueTrackCommand(log)) {
                Logger.d(tag, "Tracked JSONObject\n" + validShuttle);

                if (Env.DEV == env) /* if Env.DEV, flush immediately */
                    MessageLoop.getInstance(context).queueFlushCommand();
            }
        } catch (Exception e) { /* might be JSONException */
            Logger.e(tag, "Failed to track due to superProps or defaultProps", e);
            MetricUtil.recordErrorStatusMetric(context, Action.TRACK, token, e);
            return;
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
        Logger.d(tag, "Flush");

        /* 최종 소비자 API 예외 처리 */
        try {
            MessageLoop.getInstance(context).queueFlushCommand();
        } catch (Exception e) {
            MetricUtil.recordErrorStatusMetric(context, Action.FLUSH, token, e);
            Logger.e(tag, "Failed to flush", e);
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

    public static JSONObject getDefaultProps(Context context,
                                             Env env,
                                             String token,
                                             Date now) throws JSONException {

        SystemInformation sys = SystemInformation.getInstance(context);

        JSONObject defaultProps = new JSONObject();

        defaultProps.put(PROPERTY_NAME_TOKEN, token);
        defaultProps.put(PROPERTY_NAME_BASE_TIME, TimeUtil.getBaseFormatter().format(now));
        defaultProps.put(PROPERTY_NAME_LOCAL_TIME, TimeUtil.getLocalFormatter().format(now));

        defaultProps.put(PROPERTY_NAME_RAKE_LIB, PROPERTY_VALUE_RAKE_LIB);
        defaultProps.put(PROPERTY_NAME_RAKE_LIB_VERSION, RakeConfig.RAKE_LIB_VERSION);
        defaultProps.put(PROPERTY_NAME_OS_NAME, PROPERTY_VALUE_OS_NAME);
        defaultProps.put(PROPERTY_NAME_OS_VERSION,
                Build.VERSION.RELEASE == null ? PROPERTY_VALUE_UNKNOWN : Build.VERSION.RELEASE);
        defaultProps.put(PROPERTY_NAME_MANUFACTURER,
                Build.MANUFACTURER == null ? PROPERTY_VALUE_UNKNOWN : Build.MANUFACTURER);
        defaultProps.put(PROPERTY_NAME_DEVICE_MODEL,
                Build.MODEL == null ? PROPERTY_VALUE_UNKNOWN : Build.MODEL);
        defaultProps.put(PROPERTY_NAME_DEVICE_ID, sys.getDeviceId());

        DisplayMetrics displayMetrics = sys.getDisplayMetrics();
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        StringBuilder resolutionBuilder = new StringBuilder();

        // TODO: is it correct?
        defaultProps.put(PROPERTY_NAME_SCREEN_HEIGHT, displayWidth);
        defaultProps.put(PROPERTY_NAME_SCREEN_WIDTH, displayHeight);
        defaultProps.put(PROPERTY_NAME_SCREEN_RESOLUTION, resolutionBuilder.append(displayWidth).append("*").append(displayHeight).toString());

        /** application versionName, buildDate(iff dev mode) */
        String appVersionName = sys.getAppVersionName();
        String appBuildDate = sys.getAppBuildDate();

        if (Env.DEV == env && null != appBuildDate)
            appVersionName += "_" + sys.getAppBuildDate();

        defaultProps.put(PROPERTY_NAME_APP_VERSION,
                appVersionName == null ? PROPERTY_VALUE_UNKNOWN : appVersionName);

        String carrier = sys.getCurrentNetworkOperator();
        defaultProps.put(PROPERTY_NAME_CARRIER_NAME,
                (null != carrier && carrier.length() > 0) ? carrier : PROPERTY_VALUE_UNKNOWN);

        Boolean isWifi = sys.isWifiConnected();
        defaultProps.put(PROPERTY_NAME_NETWORK_TYPE, (isWifi == null) ?
                PROPERTY_VALUE_UNKNOWN : (isWifi.booleanValue() == true) ?
                PROPERTY_VALUE_NETWORK_TYPE_WIFI : PROPERTY_VALUE_NETWORK_TYPE_NOT_WIFI);

        defaultProps.put(PROPERTY_NAME_LANGUAGE_CODE,
                context.getResources().getConfiguration().locale.getCountry());

        return defaultProps;
    }

    /**
     * @deprecated as of 0.4.0
     */
    @Deprecated
    private void readSuperProperties() {
        try {
            String prefKey = createSharedPrefPropertyKey(token);
            String props = storedPreferences.getString(prefKey, "{}");
            Logger.d(tag, "Loading Super Properties " + props);
            superProperties = new JSONObject(props);
        } catch (JSONException e) {
            Logger.e(tag, "Cannot parse stored superProperties");
            superProperties = new JSONObject();
            storeSuperProperties();
        } // TODO exception and drop
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

    void clearPreferences() {
        // Will clear distinct_ids, superProperties,
        // and waiting People Analytics properties. Will have no effect
        // on MessageLoop which was already queued to send
        SharedPreferences.Editor prefsEdit = storedPreferences.edit();
        prefsEdit.clear().commit();
        readSuperProperties();
    }
}
