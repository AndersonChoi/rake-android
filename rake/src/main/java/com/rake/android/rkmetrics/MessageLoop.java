package com.rake.android.rkmetrics;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.rake.android.rkmetrics.RakeAPI.AutoFlush;
import com.rake.android.rkmetrics.android.SystemInformation;
import com.rake.android.rkmetrics.config.RakeConfig;
import com.rake.android.rkmetrics.metric.MetricUtil;
import com.rake.android.rkmetrics.metric.model.Action;
import com.rake.android.rkmetrics.network.HttpRequestSender;
import com.rake.android.rkmetrics.network.RakeProtocolV2;
import com.rake.android.rkmetrics.network.ServerResponse;
import com.rake.android.rkmetrics.persistent.EventTableAdapter;
import com.rake.android.rkmetrics.persistent.Log;
import com.rake.android.rkmetrics.persistent.LogChunk;
import com.rake.android.rkmetrics.persistent.LogTableAdapter;
import com.rake.android.rkmetrics.util.Logger;
import com.rake.android.rkmetrics.util.TimeUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

import static com.rake.android.rkmetrics.RakeAPI.AutoFlush.ON;
import static com.rake.android.rkmetrics.metric.MetricUtil.EMPTY_TOKEN;

/**
 * Manage communication of events with the internal database and the Rake servers (Singleton)
 */
final class MessageLoop {

    public static final int DATA_EXPIRATION_TIME = 1000 * 60 * 60 * 48; /* 48 hours */
    public static final long INITIAL_FLUSH_DELAY = 10 * 1000; /* 10 seconds */
    public static final long DEFAULT_FLUSH_INTERVAL = 60 * 1000; /* 60 seconds */

    private static long autoFlushInterval = DEFAULT_FLUSH_INTERVAL;
    private static final AutoFlush DEFAULT_AUTO_FLUSH = ON;
    private static AutoFlush autoFlushOption = DEFAULT_AUTO_FLUSH;

    public enum Command {
        TRACK(1),
        MANUAL_FLUSH(2),
        AUTO_FLUSH_BY_COUNT(3),
        AUTO_FLUSH_BY_TIMER(4),
        KILL_WORKER(5),
//        RECORD_INSTALL_METRIC(7),
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

            if (m == null) return UNKNOWN;
            else return m;
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

    public boolean queueTrackCommand(Log log) {
        if (null == log) {
            Logger.e("Can't track null `Log`");
            return false;
        }

        Message m = Message.obtain();
        m.what = Command.TRACK.code;
        m.obj = log;

        queueMessage(m);

        return true;
    }

    public void queueFlushCommand() {
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
        final SynchronousQueue<Handler> handlerQueue = new SynchronousQueue<Handler>();

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

    private class MessageHandler extends Handler {

        public MessageHandler() {
            super();

            EventTableAdapter.getInstance(appContext);
            LogTableAdapter.getInstance(appContext);

            Logger.t("[SQLite] Remove expired log (48 hours before)");
            LogTableAdapter.getInstance(appContext)
                    .removeLogByTime(System.currentTimeMillis() - DATA_EXPIRATION_TIME);

            // MessageHandler 생성시 AUTO_FLUSH_BY_TIMER 메시지 전송
            sendEmptyMessageDelayed(Command.AUTO_FLUSH_BY_TIMER.code, INITIAL_FLUSH_DELAY);
        }

        private boolean hasFlushMessage() {
            return hasMessages(Command.MANUAL_FLUSH.code)
                    || hasMessages(Command.AUTO_FLUSH_BY_TIMER.code)
                    || hasMessages(Command.AUTO_FLUSH_BY_COUNT.code);
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

            List<LogChunk> chunks = LogTableAdapter.getInstance(appContext).getLogChunks(RakeConfig.TRACK_MAX_LOG_COUNT);

            if (null == chunks || 0 == chunks.size()) {
                return;
            }

            for (LogChunk chunk : chunks) {
                long startAt = System.nanoTime();

                /** network operation */
                ServerResponse response = send(chunk);

                long endAt = System.nanoTime();

                if (null == response || null == response.getFlushStatus()) {
                    Logger.e("ServerResponse or ServerResponse.getFlushStatus() can't be NULL");
                    LogTableAdapter.getInstance(appContext).removeLogChunk(chunk);
                    return;
                }

                Long operationTime = TimeUtil.convertNanoTimeDurationToMillis(startAt, endAt);

                /**
                 * - 전송된 데이터를 삭제해도 되는지(DONE),
                 * - 전송되지 않았지만 복구 불가능하여 삭제해야만 하는지(DROP)
                 * - 복구 가능한 예외인지 (RETRY) 판단 후 실행
                 */
                switch (response.getFlushStatus()) {
                    case DONE:
                    case DROP:
                        LogTableAdapter.getInstance(appContext).removeLogChunk(chunk);
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

                RakeProtocolV2.reportResponse(
                        response.getResponseBody(),
                        response.getResponseCode()
                );

                MetricUtil.recordFlushMetric(appContext, flushType, operationTime, chunk, response);
            }
        }

        private ServerResponse send(LogChunk chunk) {
            if (chunk == null) {
                Logger.e("Can't flush using null args");
                return null;
            }

            String url = chunk.getUrl() + "/" + chunk.getToken();

            Logger.t(String.format(Locale.US, "[NETWORK] Sending %d log to %s where token = %s", chunk.getCount(), url, chunk.getToken()));

            return HttpRequestSender.handleResponse(
                    url,
                    chunk.getChunk(),
                    HttpRequestSender.getProperFlushMethod(),
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
                        Log log = (Log) msg.obj;
                        int logQueueLength = LogTableAdapter.getInstance(appContext).addLog(log);

                        /* Metric이 아닐 경우에만, 로그 출력 */
                        if (null != log && !log.getToken().equals(MetricUtil.BUILD_CONSTANT_METRIC_TOKEN)) {
                            Logger.t("[SQLite] total log count in SQLite (including metric): " + logQueueLength);
                        }

                        if (logQueueLength >= RakeConfig.TRACK_MAX_LOG_COUNT && isAutoFlushON()) {
                            sendEmptyMessage(Command.AUTO_FLUSH_BY_COUNT.code);
                        }
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
                            EventTableAdapter.getInstance(appContext).deleteDatabase();
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
