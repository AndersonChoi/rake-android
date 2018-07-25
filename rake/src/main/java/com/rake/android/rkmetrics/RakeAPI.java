package com.rake.android.rkmetrics;

import android.content.Context;
import android.content.SharedPreferences;

import com.rake.android.rkmetrics.config.RakeConfig;
import com.rake.android.rkmetrics.metric.MetricUtil;
import com.rake.android.rkmetrics.metric.model.Action;
import com.rake.android.rkmetrics.network.Endpoint;
import com.rake.android.rkmetrics.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The primary interface for integrating Rake with your app.
 * <p>
 * - Copyright: SK Planet
 */
public final class RakeAPI {

    public enum Logging {
        DISABLE("DISABLE"), ENABLE("ENABLE");

        private String mode;

        Logging(String mode) {
            this.mode = mode;
        }

        @Override
        public String toString() {
            return mode;
        }
    }


    public enum Env {
        /*
         * 공개 API 의 일부이며 빌드스크립트에서 사용되는 ENUM 이므로 이름 또는 내부 값 변경시
         * rake-android/rake/build.gradle 도 변경할 것
         */

        LIVE("LIVE"),
        DEV("DEV");

        private final String env;

        Env(String env) {
            this.env = env;
        }

        @Override
        public String toString() {
            return this.env;
        }
    }

    public enum AutoFlush {
        ON("ON"),
        OFF("OFF");

        private final String value;

        AutoFlush(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    // TODO: remove nested map
    private static Map<String, Map<Context, RakeAPI>> sInstanceMap = new HashMap<>();

    private String tag;

    private Endpoint endpoint;
    private static String versionSuffix;
    private String[] autoPropNamesToExclude;
    private final Env env;
    private final String token;

    private final Context context;
    private final SharedPreferences storedPreferences;
    private JSONObject superProperties; /* the place where persistent members loaded and stored */

    private RakeAPI(Context appContext, String token, Env env, Endpoint endpoint) {
        this.tag = createTag(token, env, endpoint);

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
     * @param logging Logging.ENABLE or Logging.DISABLE
     * @throws IllegalArgumentException if one of the parameters is NULL
     * @throws IllegalStateException    if uncaught exception occurred while initializing
     */

    public static RakeAPI getInstance(Context context, String token, Env env, Logging logging) {
        if (null == context || null == token || null == env || null == logging) {
            throw new IllegalArgumentException("Can't initialize RakeAPI using NULL args");
        }

        if ("".equals(token)) {
            throw new IllegalArgumentException("Can't initialize RakeAPI using an empty token (\"\")");
        }

        /* 예외 기록을 위한 try-catch */
        try {
            return _getInstance(context, token, env, logging);
        } catch (Exception e) { /* should not be here */
            MetricUtil.recordInstallErrorMetric(context, env, new Endpoint(context, env).getURI(), token, e);
            Logger.e("Failed to return RakeAPI instance");
            throw new IllegalStateException("Failed to create RakeAPI instance");
        }
    }

    private static RakeAPI _getInstance(Context context, String token, Env env, Logging logging) {
        setLogging(logging);

        synchronized (sInstanceMap) {
            Context appContext = context.getApplicationContext();
            Map<Context, RakeAPI> instances = sInstanceMap.get(token);

            if (instances == null) {
                instances = new HashMap<>();
                sInstanceMap.put(token, instances);
            }

            RakeAPI rake = instances.get(appContext);
            Endpoint endpoint = new Endpoint(appContext, env);
            versionSuffix = endpoint.getVersionSuffix();

            if (rake == null) {
                rake = new RakeAPI(appContext, token, env, endpoint);
                instances.put(appContext, rake);
            } else {
                rake.endpoint = endpoint;
                Logger.w("RakeAPI is already initialized for TOKEN ", token);
            }

            return rake;
        }
    }

    /**
     * Set flush interval.
     *
     * @param milliseconds flush interval (milliseconds)
     */
    public static void setFlushInterval(long milliseconds) {
        Logger.i("Set flush interval to " + milliseconds);
        MessageLoop.setAutoFlushInterval(milliseconds);
    }

    /**
     * Get flush interval.
     *
     * @return flush interval (milliseconds)
     */
    public static long getFlushInterval() {
        return MessageLoop.getAutoFlushInterval();
    }

    /**
     * Enable/disable auto flush. (Default : AutoFlush.ON)
     *
     * @param autoFlush AutoFlush.ON/OFF
     */
    public static void setAutoFlush(AutoFlush autoFlush) {
        AutoFlush old = MessageLoop.getAutoFlushOption();
        String message = String.format("Set auto-flush option from %s to %s", old.name(), autoFlush.name());
        Logger.i(message);

        MessageLoop.setAutoFlushOption(autoFlush);
    }

    /**
     * Get auto flush is enabled or not.
     *
     * @return AutoFlush.ON or AutoFlush.OFF
     */
    public static AutoFlush getAutoFlush() {
        return MessageLoop.getAutoFlushOption();
    }

    /**
     * Get Rake Library Version.
     *
     * @return version value
     */
    public static String getLibVersion() {
        return RakeConfig.RAKE_LIB_VERSION + versionSuffix;
    }

    private static String createTag(String token, Env e, Endpoint ep) {
        return String.format("%s (%s, %s, %s)", Logger.LOG_TAG_PREFIX, token, e, ep.getURI());
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
     * RakeAPI will flush immediately if RakeAPI.Env.DEV is set. See {@link #flush()}
     *
     * @param shuttle pass Shuttle.toJSONObject();
     */
    public void track(JSONObject shuttle) {
        /* 최종 소비자 API 예외 처리 */
        try {
            JSONObject superProps = getSuperProperties();
            if (MessageLoop.getInstance(context).queueTrackCommand(endpoint, token, superProps, shuttle, autoPropNamesToExclude)) {
                if (Env.DEV == env) {
                    // if Env.DEV, flush immediately.
                    MessageLoop.getInstance(context).queueFlushCommand();
                }
            }
        } catch (Exception e) { /* might be JSONException */
            MetricUtil.recordErrorMetric(context, Action.TRACK, token, e);
            Logger.e(tag, "Failed to track due to superProps", e);
        }
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
            MetricUtil.recordErrorMetric(context, Action.FLUSH, token, e);
            Logger.e(tag, "Failed to flush", e);
        }
    }

    /**
     * Change server url's port value. <br/>
     * If you have to send logs through specific non-charging server port, use this API.<br/>
     * <strong>This only changes current instance's port value.(Endpoint.FREE)</strong>.
     * <p>
     *
     * @param port non-charging port number (get it from admin)
     */
    public void setServerPort(int port) {
        if (port < 0 || port > 65535) {
            Logger.w("Invalid port value (" + port + "). Port value should be 0~65535.");
            return;
        }

        Endpoint old = this.endpoint;
        if (this.endpoint.changeURIPort(port)) {
            this.tag = createTag(token, env, endpoint); /* update tag */
            String message = String.format("Changed endpoint from %s to %s", old.getURI(), endpoint.getURI());
            Logger.d(tag, message);
        } else {
            Logger.d("No port value in the Rake server URL. URL is not changed.");
        }
    }

    /**
     * Get current instance's server url.
     *
     * @return url
     */
    public String getServerURL() {
        return this.endpoint.getURI();
    }

    /**
     * Get current instance's token.
     *
     * @return token
     */
    public String getToken() {
        return token;
    }

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
        synchronized (superProperties) {
            superProperties.remove(superPropertyName);
        }
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

    /**
     * @deprecated as of 0.6.0
     *
     * Please use {@link #getAutoProperties(Context)} instead.
     */
    @Deprecated
    public static JSONObject getDefaultProps(Context context, Env env, String token, Date now) throws JSONException {

        return MessageLoop.getInstance(context).getAutoPropertiesByToken(token, versionSuffix, null);
    }

    /**
     * Get auto collection properties which Rake Android collects by default.
     * This method returns different results depending on token.
     *
     * @param context application context
     * @return JSONObject auto collection properties by token.
     *
     */
    public JSONObject getAutoProperties(Context context) throws JSONException {
        return MessageLoop.getInstance(context).getAutoPropertiesByToken(token, versionSuffix, autoPropNamesToExclude);
    }

    /**
     * Exclude auto collection properties from auto collection.
     * For example, if you don't want to collect "device_id" automatically,
     * call this method and send String[] including "device_id".
     *
     * @param propNames auto collection property names to exclude from auto collection.
     *
     */
    public void excludeAutoProperties(String[] propNames) {
        autoPropNamesToExclude = propNames;
    }

    private void readSuperProperties() {
        try {
            String prefKey = createSharedPrefPropertyKey(token);
            String props = storedPreferences.getString(prefKey, "{}");
            Logger.d(tag, "Loading Super Properties " + props);
            superProperties = new JSONObject(props);
        } catch (Exception e) {
            Logger.e(tag, "Cannot parse stored superProperties");
            superProperties = new JSONObject();
            storeSuperProperties();
        } // TODO exception and drop
    }

    private static String createSharedPrefPropertyKey(String token) {
        return "super_properties_for_" + token;
    }

    private void storeSuperProperties() {
        String prefKey = createSharedPrefPropertyKey(token);
        String props = superProperties.toString();

        Logger.d(tag, "Storing Super Properties " + props);
        SharedPreferences.Editor prefsEditor = storedPreferences.edit();
        prefsEditor.putString(prefKey, props);
        prefsEditor.apply();   // synchronous
    }

    void clearPreferences() {
        // Will clear distinct_ids, superProperties,
        // and waiting People Analytics properties. Will have no effect
        // on MessageLoop which was already queued to send
        SharedPreferences.Editor prefsEdit = storedPreferences.edit();
        prefsEdit.clear().apply();
        readSuperProperties();
    }
}
