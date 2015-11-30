package com.rake.android.rkmetrics;

import static com.rake.android.rkmetrics.MessageLoop.Command.*;
import static com.rake.android.rkmetrics.RakeAPI.AutoFlush.*;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.rake.android.rkmetrics.config.RakeConfig;
import com.rake.android.rkmetrics.metric.model.FlushMetric;
import com.rake.android.rkmetrics.network.TransmissionResult;
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

    static final int DATA_EXPIRATION_TIME = 1000 * 60 * 60 * 48; /* 48 hours */

    static final long DEFAULT_FLUSH_INTERVAL = 60 * 1000; /* 60 seconds */
    static final long INITIAL_FLUSH_DELAY = 10 * 1000; /* 10 seconds */
    static long FLUSH_INTERVAL = DEFAULT_FLUSH_INTERVAL;

    static final AutoFlush DEFAULT_AUTO_FLUSH = ON;
    static AutoFlush autoFlushOption = DEFAULT_AUTO_FLUSH;

    enum Command {
        TRACK(1),
        MANUAL_FLUSH(2),
        AUTO_FLUSH_BY_COUNT(3),
        AUTO_FLUSH_BY_TIMER(4),
        KILL_WORKER (5),
        FLUSH_EVENT_TABLE(6), /* to support the legacy table `Event` */
        UNKNOWN(-1);

        private int code;
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

    /* package */ static synchronized void setFlushInterval(long millis) {
        FLUSH_INTERVAL = millis;
    }

    /* package */ static synchronized long getFlushInterval() { return FLUSH_INTERVAL; }

    /* package */ static synchronized void setAutoFlushOption(AutoFlush option) {
        MessageLoop.autoFlushOption = option;

        /* 인스턴스가 존재하면, AUTO_FLUSH_BY_TIMER 루프를 재시작 */
        if (null != instance) {
            instance.activateAutoFlushInterval();
        }
    }

    /* package */ static synchronized AutoFlush getAutoFlushOption() { return MessageLoop.autoFlushOption; }

    /* Instance methods */

    private void activateAutoFlushInterval() {
        Message m = Message.obtain();
        m.what = AUTO_FLUSH_BY_TIMER.code;

        runMessage(m);
    }

    private synchronized boolean isAutoFlushON() {
        return ON == autoFlushOption;
    }

    void track(Log log) {
        Message m = Message.obtain();
        m.what = Command.TRACK.code;
        m.obj = log;

        runMessage(m);
    }

    void flush() {
        Message m = Message.obtain();
        m.what = Command.MANUAL_FLUSH.code;

        runMessage(m);
    }

    void hardKill() {
        Message m = Message.obtain();
        m.what = Command.KILL_WORKER.code;

        runMessage(m);
    }
    private void runMessage(Message msg) {
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
                Logger.d("Starting [Thread " + this.getId() + "]");

                android.os.Looper.prepare();

                try {
                    handlerQueue.put(new MessageHandler());
                } catch (InterruptedException e) {
                    throw new RuntimeException("Couldn't build worker thread for Analytics Messages", e);
                }

                try {
                    android.os.Looper.loop();
                } catch (RuntimeException e) {
                    Logger.e("Rake Thread dying from RuntimeException", e);
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
            Logger.t("Avg flush frequency approximately " + seconds + " seconds.");
        }

        lastFlushTime = now;
        flushCount = newFlushCount;
    }

    private class MessageHandler extends Handler {

        private final EventTableAdapter eventTableAdapter;
        private final LogTableAdapter logTableAdapter;

        public MessageHandler() {
            super();

            eventTableAdapter = EventTableAdapter.getInstance(appContext);
            logTableAdapter = LogTableAdapter.getInstance(appContext);

            Logger.t("Remove expired logs (48 hours before)");
            logTableAdapter.removeLogByTime(System.currentTimeMillis() - DATA_EXPIRATION_TIME);

            /* flush legacy table `events` */
            if (DatabaseAdapter.upgradedFrom4To5)
                sendEmptyMessageDelayed(FLUSH_EVENT_TABLE.code, INITIAL_FLUSH_DELAY);

            sendEmptyMessageDelayed(AUTO_FLUSH_BY_TIMER.code, INITIAL_FLUSH_DELAY);
        }

        private boolean hasFlushMessage() {
            return hasMessages(MANUAL_FLUSH.code)
                    || hasMessages(AUTO_FLUSH_BY_TIMER.code)
                    || hasMessages(AUTO_FLUSH_BY_COUNT.code);
        }

        /**
         * Database Version 5 에 추가된, `log` 테이블에 있는 데이터를 전송
         */
        private void flush() {
            Long startAt = System.currentTimeMillis();
            FlushMetric metric = new FlushMetric();

            updateFlushFrequency();

            List<LogChunk> chunks = logTableAdapter.getLogChunks(RakeConfig.TRACK_MAX_LOG_COUNT);

            if (null == chunks || 0 == chunks.size()) return;

            for (LogChunk chunk : chunks) {
                send(chunk);
            }
            Long endAt = System.currentTimeMillis();

            Long operationTime = (endAt - startAt);

        }

        /**
         * HttpRequestSender 를 이용해서 데이터를 네트워크로 전송하고,
         * 전송된 데이터를 삭제해도 되는지(SUCCESS), 전송되지 않았지만 복구 불가능하여 삭제해야만 하는지(FAILURE_UNRECOVERABLE) 를 판단하여
         * 삭제할 로그의 키값인 LogDeleteKey 를 리턴
         */
        private void send(LogChunk chunk) {

            if (null == chunk) {
                Logger.e("Can't flush using Null args");
                return; // TODO: return FlushMetric
            }

            TransmissionResult result =
                    HttpRequestSender.sendRequest(chunk.getChunk(), chunk.getUrl() /* TODO + token */);

            switch (result) {
                case SUCCESS:
                    logTableAdapter.removeLogChunk(chunk);
                    break;
                case FAILURE_UNRECOVERABLE:
                    logTableAdapter.removeLogChunk(chunk);
                    break;
                case FAILURE_RECOVERABLE:
                    if (!hasFlushMessage()) sendEmptyMessage(MANUAL_FLUSH.code);
                    break;

                default:
                    Logger.e("Unknown TransmissionResult");
                    return;
            }

            String message = String.format("Sending %d log to %s with token %s",
                    chunk.getCount(), chunk.getUrl(), chunk.getToken());

            Logger.t(message);
        }

        /**
         * Database Version 4 까지 사용하던 `event` 테이블을 플러시
         */
        private void flushEventTable() {
            updateFlushFrequency();
            ExtractedEvent event = eventTableAdapter.getExtractEvent();

            if (event != null) {
                String lastId = event.getLastId();
                String log = event.getLog();
                String url = Endpoint.CHARGED.getURI(RakeAPI.Env.LIVE);

                /* assume that RakeAPI runs with Env.LIVE option */
                String message = String.format("Sending %d events to %s", event.getLogCount(), url);
                Logger.t(message);

                TransmissionResult result = HttpRequestSender.sendRequest(log, url);

                // TODO: remove from MessageLoop. -> HttpRequestSender
                if (TransmissionResult.SUCCESS == result) {
                    eventTableAdapter.removeEventById(lastId);
                } else if (TransmissionResult.FAILURE_RECOVERABLE == result) { // try again later
                    sendEmptyMessageDelayed(FLUSH_EVENT_TABLE.code, FLUSH_INTERVAL);
                } else if (TransmissionResult.FAILURE_UNRECOVERABLE == result){ // give up, we have an unrecoverable failure.
                    eventTableAdapter.removeEventById(lastId);
                } else {
                    Logger.e("Invalid TransmissionResult: " + result);
                }
            }
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                Command command = Command.fromCode(msg.what);

                if (command == TRACK) {
                    Log log = (Log) msg.obj;
                    int logQueueLength = logTableAdapter.addLog(log);

                    Logger.t("Total log count in SQLite: " + logQueueLength);

                    if (logQueueLength >= RakeConfig.TRACK_MAX_LOG_COUNT && isAutoFlushON()) {
                        // TODO sendEmptyDelayed message
                        flush();
                    }

                } else if (command == FLUSH_EVENT_TABLE) {
                    flushEventTable();
                } else if (command == MANUAL_FLUSH) {
                    flush();
                } else if (command == AUTO_FLUSH_BY_COUNT && isAutoFlushON()) {
                    flush();

                } else if (command == AUTO_FLUSH_BY_TIMER && isAutoFlushON()) {
                    flush();

                    if (!hasMessages(AUTO_FLUSH_BY_TIMER.code) && isAutoFlushON())
                        sendEmptyMessageDelayed(AUTO_FLUSH_BY_TIMER.code, FLUSH_INTERVAL);

                } else if (command == KILL_WORKER) {
                    Logger.w("Worker received a hard kill. Dumping all events and force-killing. Thread id " + Thread.currentThread().getId());
                    synchronized (handlerLock) {
                        eventTableAdapter.deleteDatabase();
                        handler = null;
                        android.os.Looper.myLooper().quit();
                    }

                } else { /* UNKNOWN COMMAND */
                    Logger.e("Unexpected message received by Rake worker: " + msg);
                }

            } catch (OutOfMemoryError e) {
                Logger.e("Caught OOM error. Rake will not send any more messages", e);
                // TODO metric

                synchronized (handlerLock) {
                    handler = null;
                    try { android.os.Looper.myLooper().quit(); }
                    catch (Exception tooLate) {
                        Logger.e("Can't halt looper", tooLate);
                    }
                }
            } catch (Exception e) {
                Logger.e("Caught unhandled exception. (ignored)", e);
                // TODO metric
            }
        } // handleMessage
    }
}
