package com.rake.android.rkmetrics;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;

import com.rake.android.rkmetrics.RakeAPI.AutoFlush;
import com.rake.android.rkmetrics.android.SystemInformation;
import com.rake.android.rkmetrics.config.RakeConfig;
import com.rake.android.rkmetrics.db.LogTable;
import com.rake.android.rkmetrics.db.log.Log;
import com.rake.android.rkmetrics.db.log.LogBundle;
import com.rake.android.rkmetrics.metric.MetricUtil;
import com.rake.android.rkmetrics.metric.model.Action;
import com.rake.android.rkmetrics.network.Endpoint;
import com.rake.android.rkmetrics.network.HttpRequestSender;
import com.rake.android.rkmetrics.network.HttpResponse;
import com.rake.android.rkmetrics.shuttle.ShuttleProfiler;
import com.rake.android.rkmetrics.util.Logger;
import com.rake.android.rkmetrics.util.TimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

import static com.rake.android.rkmetrics.RakeAPI.AutoFlush.ON;
import static com.rake.android.rkmetrics.metric.MetricUtil.EMPTY_TOKEN;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_APP_BUILD_NUMBER;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_APP_RELEASE;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_APP_VERSION;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_BASE_TIME;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_CARRIER_NAME;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_DEVICE_ID;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_DEVICE_MODEL;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_LANGUAGE_CODE;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_LOCAL_TIME;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_MANUFACTURER;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_NETWORK_TYPE;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_OS_NAME;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_OS_VERSION;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_RAKE_LIB;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_RAKE_LIB_VERSION;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_SCREEN_HEIGHT;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_SCREEN_RESOLUTION;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_SCREEN_WIDTH;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_TOKEN;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_VALUE_OS_NAME;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_VALUE_RAKE_LIB;

/**
 * Manage communication of events with the internal database and the Rake servers (Singleton)
 */
final class MessageLoop {

    private static final int DATA_EXPIRATION_TIME = 1000 * 60 * 60 * 48; /* 48 hours */
    private static final long INITIAL_FLUSH_DELAY = 10 * 1000; /* 10 seconds */
    static final long DEFAULT_FLUSH_INTERVAL = 60 * 1000; /* 60 seconds */

    private static long autoFlushInterval = DEFAULT_FLUSH_INTERVAL;
    private static final AutoFlush DEFAULT_AUTO_FLUSH = ON;
    private static AutoFlush autoFlushOption = DEFAULT_AUTO_FLUSH;

    enum Command {
        TRACK(1),
        MANUAL_FLUSH(2),
        AUTO_FLUSH_BY_COUNT(3),
        AUTO_FLUSH_BY_TIMER(4),
        KILL_WORKER(5),
        UNKNOWN(-1);

        private int code;

        public int getCode() {
            return code;
        }

        Command(int code) {
            this.code = code;
        }

        private static final Map<Integer, Command> messagesByCode = new HashMap<>();

        static {
            for (Command m : Command.values()) {
                messagesByCode.put(m.code, m);
            }
        }

        public static Command fromCode(int code) {
            Command m = messagesByCode.get(code);
            return m == null ? UNKNOWN : m;
        }
    }

    private static volatile Handler handler;

    private static MessageLoop instance;

    private final Object handlerLock = new Object();
    private final Context appContext;

    private long flushCount = 0;
    private long avgFlushFrequency = 0;
    private long lastFlushTime = -1;

    private MessageLoop(Context appContext) {
        this.appContext = appContext;

        // TODO try-catch: retry
        handler = createMessageHandler();
    }

    /* Class methods */
    /* package */
    static synchronized MessageLoop getInstance(Context appContext) {
        if (null == instance) {
            instance = new MessageLoop(appContext);
        }

        return instance;
    }

    /* package */
    static void setAutoFlushInterval(long millis) {
        autoFlushInterval = millis;
    }

    /* package */
    static long getAutoFlushInterval() {
        return autoFlushInterval;
    }

    /* package */
    static void setAutoFlushOption(AutoFlush option) {
        MessageLoop.autoFlushOption = option;

        /* 인스턴스가 존재하면, AUTO_FLUSH_BY_TIMER 루프를 재시작 */
        if (null != instance) {
            instance.activateAutoFlushInterval();
        }
    }

    /* package */
    static AutoFlush getAutoFlushOption() {
        return MessageLoop.autoFlushOption;
    }

    /* Instance methods */

    private void activateAutoFlushInterval() {
        Message m = Message.obtain();
        m.what = Command.AUTO_FLUSH_BY_TIMER.code;

        queueMessage(m);
    }

    private synchronized boolean isAutoFlushON() {
        return ON == autoFlushOption;
    }

    JSONObject getAutoPropertiesByToken(String token, String versionSuffix, String[] autoPropNamesToExclude) throws JSONException {
        Date now = new Date();

        JSONObject autoProps = new JSONObject();

        autoProps.put(PROPERTY_NAME_TOKEN, token);
        autoProps.put(PROPERTY_NAME_BASE_TIME, TimeUtil.getBaseFormatter().format(now));
        autoProps.put(PROPERTY_NAME_LOCAL_TIME, TimeUtil.getLocalFormatter().format(now));

        autoProps.put(PROPERTY_NAME_RAKE_LIB_VERSION, RakeConfig.RAKE_LIB_VERSION + versionSuffix);

        autoProps.put(PROPERTY_NAME_CARRIER_NAME, SystemInformation.getCurrentNetworkOperator(appContext));
        autoProps.put(PROPERTY_NAME_NETWORK_TYPE, SystemInformation.getWifiConnected(appContext));
        autoProps.put(PROPERTY_NAME_LANGUAGE_CODE, SystemInformation.getLanguageCode(appContext));

        autoProps.put(PROPERTY_NAME_RAKE_LIB, PROPERTY_VALUE_RAKE_LIB);
        autoProps.put(PROPERTY_NAME_OS_NAME, PROPERTY_VALUE_OS_NAME);
        autoProps.put(PROPERTY_NAME_OS_VERSION, SystemInformation.getOsVersion());
        autoProps.put(PROPERTY_NAME_MANUFACTURER, SystemInformation.getManufacturer());
        autoProps.put(PROPERTY_NAME_DEVICE_MODEL, SystemInformation.getDeviceModel());
        autoProps.put(PROPERTY_NAME_DEVICE_ID, SystemInformation.getDeviceId(appContext));
        DisplayMetrics displayMetrics = SystemInformation.getDisplayMetrics(appContext);
        if (displayMetrics != null) {
            int displayWidth = displayMetrics.widthPixels;
            int displayHeight = displayMetrics.heightPixels;

            autoProps.put(PROPERTY_NAME_SCREEN_HEIGHT, displayWidth);
            autoProps.put(PROPERTY_NAME_SCREEN_WIDTH, displayHeight);
            autoProps.put(PROPERTY_NAME_SCREEN_RESOLUTION, "" + displayWidth + "*" + displayHeight);
        }

            /*
             DILTFCO-14 :
                app_version : iOS는 앱의 build count, Android는 앱의 version을 수집중 (current state)
                app_release : 앱의 version
                app_build_number: 앱의 build count
            */
        String appVersion = SystemInformation.getAppVersionName(appContext);
        autoProps.put(PROPERTY_NAME_APP_VERSION, appVersion);
        autoProps.put(PROPERTY_NAME_APP_RELEASE, appVersion);
        autoProps.put(PROPERTY_NAME_APP_BUILD_NUMBER, SystemInformation.getAppVersionCode(appContext));

        if (autoPropNamesToExclude != null) {
            for (String propName : autoPropNamesToExclude) {
                if (autoProps.has(propName)) {
                    autoProps.remove(propName);
                }
            }
        }

        return autoProps;
    }

    boolean queueTrackCommand(Endpoint endpoint, String token, JSONObject superProps, JSONObject shuttle, String[] autoPropNamesToExclude) {
        if (endpoint == null || token == null || shuttle == null) {
            Logger.e("Can't track null `track values`");
            return false;
        }

        Message m = Message.obtain();
        m.what = Command.TRACK.code;
        m.obj = new TrackValues(endpoint, token, superProps, shuttle, autoPropNamesToExclude);
        queueMessage(m);

        return true;
    }

    void queueFlushCommand() {
        Message m = Message.obtain();
        m.what = Command.MANUAL_FLUSH.code;

        queueMessage(m);
    }

    public void hardKill() {
        Message m = Message.obtain();
        m.what = Command.KILL_WORKER.code;

        queueMessage(m);
    }

    private void queueMessage(Message msg) {
        if (isDead()) {
            // thread died under suspicious circumstances.
            // don't try to send any more events.
            Logger.e("Dead rake worker dropping a message: " + msg);
        } else {
            synchronized (handlerLock) {
                if (handler != null) handler.sendMessage(msg);
            }
        }
    }

    private boolean isDead() {
        synchronized (handlerLock) {
            return handler == null;
        }
    }

    private Handler createMessageHandler() {
        final SynchronousQueue<Handler> handlerQueue = new SynchronousQueue<>();

        Thread thread = new Thread() {
            @Override
            public void run() {
                android.os.Looper.prepare();

                try {
                    handlerQueue.put(new MessageHandler());
                } catch (InterruptedException e) {
                    throw new RuntimeException("Can't build", e);
                }

                try {
                    android.os.Looper.loop();
                } catch (RuntimeException e) {
                    MetricUtil.recordErrorMetric(appContext, Action.EMPTY, EMPTY_TOKEN, e);
                    Logger.e("Looper.loop() was not prepared", e);
                }
            }
        };

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

        Handler handler;

        try {
            handler = handlerQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException("Couldn't retrieve handler from worker thread");
        }

        return handler;
    }

    private void updateFlushFrequency() {
        long now = System.currentTimeMillis();
        long newFlushCount = flushCount + 1;

        if (lastFlushTime > 0) {
            long flushInterval = now - lastFlushTime;
            long totalFlushTime = flushInterval + (avgFlushFrequency * flushCount);
            avgFlushFrequency = totalFlushTime / newFlushCount;

            long seconds = avgFlushFrequency / 1000;
            Logger.t("[SCHEDULE] Avg flush frequency approximately " + seconds + " seconds.");
        }

        lastFlushTime = now;
        flushCount = newFlushCount;
    }

    private class TrackValues {
        private String uri;
        private String versionSuffix;
        private String token;
        private JSONObject superProps;
        private JSONObject shuttle;
        private String[] autoPropNamesToExclude;

        TrackValues(Endpoint endpoint, String token, JSONObject superProps, JSONObject shuttle, String[] autoPropNamesToExclude) {
            this.uri = endpoint.getURI();
            this.versionSuffix = endpoint.getVersionSuffix();
            this.token = token;
            this.superProps = superProps;
            this.shuttle = shuttle;
            this.autoPropNamesToExclude = autoPropNamesToExclude;
        }
    }

    private class MessageHandler extends Handler {

        MessageHandler() {
            super();

            Logger.t("[SQLite] Remove expired log (48 hours before)");

            try {
                LogTable.getInstance(appContext).removeLogsBefore(System.currentTimeMillis() - DATA_EXPIRATION_TIME);
            } catch (Exception e) {
                Logger.e("[SQLite] Exception occurred while removing expired log (48 hours before)", e);
            }

            // MessageHandler 생성시 AUTO_FLUSH_BY_TIMER 메시지 전송
            sendEmptyMessageDelayed(Command.AUTO_FLUSH_BY_TIMER.code, INITIAL_FLUSH_DELAY);
        }

        private boolean hasFlushMessage() {
            return hasMessages(Command.MANUAL_FLUSH.code)
                    || hasMessages(Command.AUTO_FLUSH_BY_TIMER.code)
                    || hasMessages(Command.AUTO_FLUSH_BY_COUNT.code);
        }

        private void track(TrackValues trackValues) throws JSONException {
            JSONObject autoProps = getAutoPropertiesByToken(trackValues.token, trackValues.versionSuffix, trackValues.autoPropNamesToExclude);
            JSONObject validShuttle = ShuttleProfiler.createValidShuttle(trackValues.shuttle, trackValues.superProps, autoProps);

            Log log = new Log(trackValues.uri, trackValues.token, validShuttle);
            long logQueueLength = LogTable.getInstance(appContext).addLog(log);

            Logger.d("Tracked JSONObject\n" + validShuttle);

            /* Metric이 아닐 경우에만, 로그 출력 */
            if (!log.getToken().equals(MetricUtil.BUILD_CONSTANT_METRIC_TOKEN)) {
                Logger.t("[SQLite] total log count in SQLite (including metric): " + logQueueLength);
            }

            if (logQueueLength >= RakeConfig.TRACK_MAX_LOG_COUNT && isAutoFlushON()) {
                sendEmptyMessage(Command.AUTO_FLUSH_BY_COUNT.code);
            }
        }

        /**
         * Database Version 5 에 추가된, `log` 테이블에 있는 데이터를 전송
         */
        private void flush(String flushType) {
            if (SystemInformation.isDozeModeEnabled(appContext)) {
                Logger.d("Doze mode is enabled. Network is not available now, so flush() will not be executed. ");
                return;
            }

            updateFlushFrequency();

            List<LogBundle> logBundles = LogTable.getInstance(appContext).getLogBundles(RakeConfig.TRACK_MAX_LOG_COUNT);

            if (logBundles == null || logBundles.size() == 0) {
                return;
            }

            for (LogBundle logBundle : logBundles) {
                long startAt = System.nanoTime();

                /* network operation */
                HttpResponse httpResponse = send(logBundle);

                long endAt = System.nanoTime();

                if (null == httpResponse || null == httpResponse.getFlushStatus()) {
                    Logger.e("ServerResponse or ServerResponse.getFlushStatus() can't be NULL");
                    LogTable.getInstance(appContext).removeLogBundle(logBundle);
                    return;
                }

                Long operationTime = TimeUtil.convertNanoTimeDurationToMillis(startAt, endAt);

                /*
                  - 전송된 데이터를 삭제해도 되는지(DONE),
                  - 전송되지 않았지만 복구 불가능하여 삭제해야만 하는지(DROP)
                  - 복구 가능한 예외인지 (RETRY) 판단 후 실행
                 */
                switch (httpResponse.getFlushStatus()) {
                    case DONE:
                    case DROP:
                        LogTable.getInstance(appContext).removeLogBundle(logBundle);
                        break;
                    case RETRY:
                        if (!hasFlushMessage()) {
                            sendEmptyMessage(Command.MANUAL_FLUSH.code);
                        }
                        break; // TODO flush database, RAKE-383, RAKE-381
                    default:
                        Logger.e("Unknown FlushStatus");
                        return;
                }

                Logger.t("[NETWORK] Server returned code: " + httpResponse.getResponseCode() + ", body: " + httpResponse.getResponseBody());

                MetricUtil.recordFlushMetric(appContext, flushType, operationTime, logBundle, httpResponse);
            }
        }

        private HttpResponse send(LogBundle logBundle) {
            if (logBundle == null) {
                Logger.e("Can't flush using null args");
                return null;
            }

            String url = logBundle.getUrl() + "/" + logBundle.getToken();

            Logger.t(String.format(Locale.US, "[NETWORK] Sending %d log to %s where token = %s", logBundle.getCount(), url, logBundle.getToken()));

            return HttpRequestSender.handleResponse(
                    url,
                    logBundle.getLogsByJSONString(),
                    HttpRequestSender.procedure
            );
        }

        /**
         * Database Version 4 까지 사용하던 `event` 테이블을 플러시, 0.4.0 초과 버전부터 삭제할 것
         */

        @Override
        public void handleMessage(Message msg) {
            try {
                switch (Command.fromCode(msg.what)) {
                    case TRACK:
                        track((TrackValues) msg.obj);
                        break;
                    case MANUAL_FLUSH:
                        flush(Command.MANUAL_FLUSH.name());
                        break;
                    case AUTO_FLUSH_BY_COUNT:
                        if (isAutoFlushON()) {
                            flush(Command.AUTO_FLUSH_BY_COUNT.name());
                        }
                        break;
                    case AUTO_FLUSH_BY_TIMER:
                        if (isAutoFlushON()) {
                            /* BY_TIMER 메시지를 받았을 때 다시 자신을 보냄으로써 autoFlushInterval 만큼 반복 */
                            if (!hasMessages(Command.AUTO_FLUSH_BY_TIMER.code)) {
                                sendEmptyMessageDelayed(Command.AUTO_FLUSH_BY_TIMER.code, autoFlushInterval);
                            }
                            flush(Command.AUTO_FLUSH_BY_TIMER.name());
                        }
                        break;
                    case KILL_WORKER:
                        Logger.w("Worker received a hard kill. Dumping all events and force-killing. Thread id " + Thread.currentThread().getId());
                        synchronized (handlerLock) {
                            handler = null;
                            android.os.Looper.myLooper().quit();
                        }
                        break;
                    default:
                        Logger.e("Unexpected message received by Rake worker: " + msg);
                        break;
                }

            } catch (OutOfMemoryError e) {
                Logger.e("Caught OOM error. Rake will not send any more messages", e);
                MetricUtil.recordErrorMetric(appContext, Action.EMPTY, EMPTY_TOKEN, e);

                synchronized (handlerLock) {
                    handler = null;
                    try {
                        android.os.Looper.myLooper().quit();
                    } catch (Exception tooLate) {
                        Logger.e("Can't halt looper", tooLate);
                    }
                }
            } catch (Exception e) {
                Logger.e("Caught unhandled exception. (ignored)", e);
                MetricUtil.recordErrorMetric(appContext, Action.EMPTY, EMPTY_TOKEN, e);
            }
        }

    }
}
