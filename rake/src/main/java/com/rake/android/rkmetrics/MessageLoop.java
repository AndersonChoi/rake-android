package com.rake.android.rkmetrics;

import static com.rake.android.rkmetrics.MessageLoop.Command.*;
import static com.rake.android.rkmetrics.RakeAPI.AutoFlush.*;
import static com.rake.android.rkmetrics.metric.MetricUtil.*;
import static com.rake.android.rkmetrics.metric.model.Status.*;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.rake.android.rkmetrics.config.RakeConfig;
import com.rake.android.rkmetrics.metric.MetricUtil;
import com.rake.android.rkmetrics.metric.model.Action;
import com.rake.android.rkmetrics.metric.model.FlushType;
import com.rake.android.rkmetrics.metric.model.Header;
import com.rake.android.rkmetrics.metric.model.InstallMetric;
import com.rake.android.rkmetrics.metric.model.Status;
import com.rake.android.rkmetrics.network.RakeProtocolV1;
import com.rake.android.rkmetrics.network.ServerResponseMetric;
import com.rake.android.rkmetrics.network.HttpRequestSender;
import com.rake.android.rkmetrics.persistent.DatabaseAdapter;
import com.rake.android.rkmetrics.persistent.EventTableAdapter;
import com.rake.android.rkmetrics.persistent.ExtractedEvent;
import com.rake.android.rkmetrics.persistent.Log;
import com.rake.android.rkmetrics.persistent.LogChunk;
import com.rake.android.rkmetrics.persistent.LogTableAdapter;
import com.rake.android.rkmetrics.util.Logger;
import com.rake.android.rkmetrics.network.Endpoint;
import com.rake.android.rkmetrics.RakeAPI.AutoFlush;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

/**
 * Manage communication of events with the internal database and the Rake servers (Singleton)
 */
final class MessageLoop {

    public static final int DATA_EXPIRATION_TIME = 1000 * 60 * 60 * 48; /* 48 hours */
    public static final long INITIAL_FLUSH_DELAY = 10 * 1000; /* 10 seconds */
    public static final long DEFAULT_FLUSH_INTERVAL = 60 * 1000; /* 60 seconds */
    public static final long INITIAL_EVENT_FLUSH_DELAY = 10 * 1000; /* 10 seconds */

    private static long autoFlushInterval = DEFAULT_FLUSH_INTERVAL;
    private static final AutoFlush DEFAULT_AUTO_FLUSH = ON;
    private static AutoFlush autoFlushOption = DEFAULT_AUTO_FLUSH;

    public enum Command {
        TRACK(1),
        MANUAL_FLUSH(2),
        AUTO_FLUSH_BY_COUNT(3),
        AUTO_FLUSH_BY_TIMER(4),
        KILL_WORKER (5),
        FLUSH_EVENT_TABLE(6), /* to support the legacy table `Event` */
        RECORD_INSTALL_METRIC(7),
        UNKNOWN(-1);

        private int code;
        public int getCode() { return code; }
        Command(int code) { this.code = code; }

        private static final Map<Integer, Command> messagesByCode = new HashMap<Integer, Command>();

        static {
            for (Command m : Command.values()) {
                messagesByCode.put(m.code, m);
            }
        }

        public static Command fromCode(int code) {
            Command m = messagesByCode.get(code);

            if (m == null) return UNKNOWN; else return m;
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
    /* package */ static synchronized MessageLoop getInstance(Context appContext) {
        if (null == instance) { instance = new MessageLoop(appContext); }

        return instance;
    }

    /* package */ static void setAutoFlushInterval(long millis) {
        autoFlushInterval = millis;
    }

    /* package */ static long getAutoFlushInterval() { return autoFlushInterval; }

    /* package */ static void setAutoFlushOption(AutoFlush option) {
        MessageLoop.autoFlushOption = option;

        /* 인스턴스가 존재하면, AUTO_FLUSH_BY_TIMER 루프를 재시작 */
        if (null != instance) {
            instance.activateAutoFlushInterval();
        }
    }

    /* package */ static AutoFlush getAutoFlushOption() { return MessageLoop.autoFlushOption; }

    /* Instance methods */

    private void activateAutoFlushInterval() {
        Message m = Message.obtain();
        m.what = AUTO_FLUSH_BY_TIMER.code;

        queueMessage(m);
    }

    private synchronized boolean isAutoFlushON() { return ON == autoFlushOption; }

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
        synchronized (handlerLock) { return handler == null; }
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

        Handler handler = null;

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
            Logger.t("[METRIC] Avg flush frequency approximately " + seconds + " seconds.");
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

            /* flush legacy table `events` */
            if (DatabaseAdapter.upgradedFrom4To5)
                sendEmptyMessageDelayed(FLUSH_EVENT_TABLE.code, INITIAL_EVENT_FLUSH_DELAY);

            sendEmptyMessageDelayed(AUTO_FLUSH_BY_TIMER.code, INITIAL_FLUSH_DELAY);
        }

        private boolean hasFlushMessage() {
            return hasMessages(MANUAL_FLUSH.code)
                    || hasMessages(AUTO_FLUSH_BY_TIMER.code)
                    || hasMessages(AUTO_FLUSH_BY_COUNT.code);
        }

        /** Database Version 5 에 추가된, `log` 테이블에 있는 데이터를 전송 */
        private void flush(FlushType flushType) {
            if (null == flushType) {
                Logger.e("Can't flush with an empty FlushType");
                return;
            }

            updateFlushFrequency();

            List<LogChunk> chunks = LogTableAdapter.getInstance(appContext)
                    .getLogChunks(RakeConfig.TRACK_MAX_LOG_COUNT);

            if (null == chunks || 0 == chunks.size()) return;

            for (LogChunk chunk : chunks) {
                Long startAt = System.currentTimeMillis();

                /** network operation */
                ServerResponseMetric responseMetric = send(chunk);

                Long endAt = System.currentTimeMillis();

                if (null == responseMetric) {
                    Logger.e("ServerResponseMetric can't be NULL");
                    LogTableAdapter.getInstance(appContext).removeLogChunk(chunk);
                    return;
                }

                Long operationTime = (endAt - startAt);
                Status status = responseMetric.getFlushStatus();

                if (null == status) {
                    Logger.e("Status can't be NULL");
                    LogTableAdapter.getInstance(appContext).removeLogChunk(chunk);
                    return;
                }

                /**
                 * - 전송된 데이터를 삭제해도 되는지(DONE),
                 * - 전송되지 않았지만 복구 불가능하여 삭제해야만 하는지(DROP)
                 * - 복구 가능한 예외인지 (RETRY) 판단 후 실행
                 */
                switch (status) {
                    case DONE:
                    case DROP:
                        LogTableAdapter.getInstance(appContext).removeLogChunk(chunk);
                        break;
                    case RETRY:
                        // TODO flush database, RAKE-383, RAKE-381
                        if (!hasFlushMessage()) sendEmptyMessage(MANUAL_FLUSH.code);
                        break;
                    default:
                        Logger.e("Unknown FlushStatus");
                        return;
                }


                /** 메트릭 전송용 토큰이 아닌 경우에만 */
                if (MetricUtil.isNotMetricToken(chunk.getToken())) {
                    /** Network, Database 연산에 대해 report */
                    String message = String.format("[SQLite] Extracting %d rows from the [%s] table where token = %s",
                            chunk.getCount(), LogTableAdapter.LogContract.TABLE_NAME, chunk.getToken());
                    Logger.t(message);

                    RakeProtocolV1.reportResponse(responseMetric.getResponseBody(), responseMetric.getResponseCode());

                    /** `flush` 메트릭을 기록 */
                    MetricUtil.recordFlushMetric(appContext, status, flushType, operationTime, chunk, responseMetric);
                }
            }
        }
        private ServerResponseMetric send(LogChunk chunk) {

            if (null == chunk) {
                Logger.e("Can't flush using null args");
                return null;
            }

            /** Metric Token 일 경우 로깅을 하지 않음 */
            if (MetricUtil.isNotMetricToken(chunk.getToken())) {
                String message = String.format("[NETWORK] Sending %d log to %s where token = %s",
                        chunk.getCount(), chunk.getUrl(), chunk.getToken());
                Logger.t(message);
            }

            /* TODO: + token */
            ServerResponseMetric responseMetric = HttpRequestSender.sendRequest(chunk.getChunk(), chunk.getUrl());

            return responseMetric;
        }

        /**
         * Database Version 4 까지 사용하던 `event` 테이블을 플러시, 0.4.0 초과 버전부터 삭제할 것
         */
        private void flushEventTable() {
            updateFlushFrequency();
            ExtractedEvent event = EventTableAdapter.getInstance(appContext).getExtractEvent();

            if (event != null) {
                String lastId = event.getLastId();
                String log = event.getLog();
                String url = Endpoint.CHARGED.getURI(RakeAPI.Env.LIVE);

                /* assume that RakeAPI runs with Env.LIVE option */
                String message = String.format("[NETWORK] Sending %d events to %s", event.getLogCount(), url);
                Logger.t(message);

                ServerResponseMetric responseMetric = HttpRequestSender.sendRequest(log, url);

                if (null == responseMetric) {
                    Logger.e("ServerResponseMetric can't be null");
                    return;
                }

                Status status = responseMetric.getFlushStatus();

                RakeProtocolV1.reportResponse
                        (responseMetric.getResponseBody(), responseMetric.getResponseCode());

                if (DONE == status || DROP == status) {
                    // if DROP, we have an unrecoverable failure.
                    EventTableAdapter.getInstance(appContext).removeEventById(lastId);
                } else if (RETRY == status) {
                    sendEmptyMessageDelayed(FLUSH_EVENT_TABLE.code, autoFlushInterval);
                } else {
                    Logger.e("Invalid TransmissionResult: " + status);
                }
            }
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                Command command = Command.fromCode(msg.what);

                if (command == TRACK) {
                    Log log = (Log) msg.obj;
                    int logQueueLength = LogTableAdapter.getInstance(appContext).addLog(log);

                    /** Metric 이 아닐 경우에만, 로깅 */
                    if (null != log && !log.getToken().equals(MetricUtil.BUILD_CONSTANT_METRIC_TOKEN))
                        Logger.t("[SQLite] total log count in SQLite (including metric): " + logQueueLength);

                    if (logQueueLength >= RakeConfig.TRACK_MAX_LOG_COUNT && isAutoFlushON()) {
                        sendEmptyMessage(AUTO_FLUSH_BY_COUNT.code);
                    }

                } else if (command == FLUSH_EVENT_TABLE) {
                    flushEventTable();
                } else if (command == MANUAL_FLUSH) {
                    flush(FlushType.MANUAL_FLUSH);
                } else if (command == AUTO_FLUSH_BY_COUNT && isAutoFlushON()) {
                    flush(FlushType.AUTO_FLUSH_BY_COUNT);
                } else if (command == AUTO_FLUSH_BY_TIMER && isAutoFlushON()) {
                    /** BY_TIMER 메시지를 받았을 때 다시 자신을 보냄으로써 autoFlushInterval 만큼 반복 */
                    if (!hasMessages(AUTO_FLUSH_BY_TIMER.code) && isAutoFlushON())
                        sendEmptyMessageDelayed(AUTO_FLUSH_BY_TIMER.code, autoFlushInterval);

                    flush(FlushType.AUTO_FLUSH_BY_TIMER);
                } else if (command == KILL_WORKER) {
                    Logger.w("Worker received a hard kill. Dumping all events and force-killing. Thread id " + Thread.currentThread().getId());
                    synchronized (handlerLock) {
                        EventTableAdapter.getInstance(appContext).deleteDatabase();
                        handler = null;
                        android.os.Looper.myLooper().quit();
                    }

                } else { /* UNKNOWN COMMAND */
                    Logger.e("Unexpected message received by Rake worker: " + msg);
                }

            } catch (OutOfMemoryError e) {
                Logger.e("Caught OOM error. Rake will not send any more messages", e);
                MetricUtil.recordErrorMetric(appContext, Action.EMPTY, EMPTY_TOKEN, e);

                synchronized (handlerLock) {
                    handler = null;
                    try { android.os.Looper.myLooper().quit(); }
                    catch (Exception tooLate) {
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
