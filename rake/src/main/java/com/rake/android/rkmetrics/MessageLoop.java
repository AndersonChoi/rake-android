package com.rake.android.rkmetrics;

import static com.rake.android.rkmetrics.config.RakeConfig.*;
import static com.rake.android.rkmetrics.MessageLoop.Command.*;
import static com.rake.android.rkmetrics.network.HttpRequestSender.RequestResult;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.rake.android.rkmetrics.config.RakeConfig;
import com.rake.android.rkmetrics.network.HttpRequestSender;
import com.rake.android.rkmetrics.persistent.EventTableAdapter;
import com.rake.android.rkmetrics.persistent.ExtractedEvent;
import com.rake.android.rkmetrics.persistent.Log;
import com.rake.android.rkmetrics.persistent.LogTableAdapter;
import com.rake.android.rkmetrics.persistent.Transferable;
import com.rake.android.rkmetrics.util.RakeLogger;
import com.rake.android.rkmetrics.network.Endpoint;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;

/**
 * Manage communication of events with the internal database and the Rake servers (Singleton)
 */
final class MessageLoop {

    static final int DATA_EXPIRATION_TIME = 1000 * 60 * 60 * 48; /* 48 hours */
    static final long DEFAULT_FLUSH_INTERVAL = 60 * 1000; /* 60 seconds */
    static final long INITIAL_FLUSH_DELAY = 10 * 1000; /* 10 seconds */
    static long FLUSH_INTERVAL = DEFAULT_FLUSH_INTERVAL;

    enum Command {
        TRACK(1),
        MANUAL_FLUSH(2),
        AUTO_FLUSH_CAPACITY(3),
        AUTO_FLUSH_INTERVAL(4),
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

    static synchronized MessageLoop getInstance(Context appContext) {
        if (null == instance) { instance = new MessageLoop(appContext); }

        return instance;
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

    static void setFlushInterval(long millis) {
        FLUSH_INTERVAL = millis;
    }

    static long getFlushInterval() {
        return FLUSH_INTERVAL;
    }

    /* package */

    private void runMessage(Message msg) {
        if (isDead()) {
            // thread died under suspicious circumstances.
            // don't try to send any more events.
            RakeLogger.e(LOG_TAG_PREFIX, "Dead rake worker dropping a message: " + msg);
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
                RakeLogger.i(LOG_TAG_PREFIX, "Starting worker thread " + this.getId());

                android.os.Looper.prepare();

                try {
                    handlerQueue.put(new MessageHandler());
                } catch (InterruptedException e) {
                    throw new RuntimeException("Couldn't build worker thread for Analytics Messages", e);
                }

                try {
                    android.os.Looper.loop();
                } catch (RuntimeException e) {
                    RakeLogger.e(LOG_TAG_PREFIX, "Rake Thread dying from RuntimeException", e);
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
            RakeLogger.d(LOG_TAG_PREFIX, "Avg flush frequency approximately " + seconds + " seconds.");
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

            RakeLogger.t(LOG_TAG_PREFIX, "Remove expired logs (48 hours before)");
            logTableAdapter.removeLogByTime(System.currentTimeMillis() - DATA_EXPIRATION_TIME);

            /* flush legacy table `events` */
            sendEmptyMessageDelayed(FLUSH_EVENT_TABLE.code, INITIAL_FLUSH_DELAY);

            /* send initial auto-flush message */
            sendEmptyMessageDelayed(AUTO_FLUSH_INTERVAL.code, INITIAL_FLUSH_DELAY);
        }

        private void sendLogFromLogTable() {
           updateFlushFrequency();

            Transferable t = logTableAdapter.getTransferable(RakeConfig.TRACK_MAX_LOG_COUNT);

            if (null == t) return; /* flushing empty table */

            String lastId = t.getLastId();
            Set<String> urls = t.getUrls();
            Map<String, Map<String, JSONArray>> logMap = t.getLogMap();

            if (null == lastId || null == urls || urls.isEmpty() || null == logMap || logMap.isEmpty()) {
                RakeLogger.e(LOG_TAG_PREFIX, "Invalid empty Transferable"); /* should not be here! */
                return;
            }

            for(String url : logMap.keySet()) {
                for (String token: logMap.get(url).keySet()) {

                    String stringified = logMap.get(url).get(token).toString();
                    String endpoint = url; /* TODO + "/" + token */
                    RequestResult result = HttpRequestSender.sendRequest(stringified, endpoint);

                    if (RequestResult.SUCCESS == result)
                        logTableAdapter.removeLogById(lastId);
                    else if (RequestResult.FAILURE_RECOVERABLE == result) {
                        // TODO metric logging
                        if (!hasFlushMessage()) sendEmptyMessage(MANUAL_FLUSH.code);
                    } else if (RequestResult.FAILURE_UNRECOVERABLE == result) {
                        // TODO metric logging
                        logTableAdapter.removeLogById(lastId);
                    } else {
                        RakeLogger.e(LOG_TAG_PREFIX, "Invalid RequestResult: " + result);
                    }
                }
            }
        }

        private boolean hasFlushMessage() {
            return hasMessages(MANUAL_FLUSH.code)
                    || hasMessages(AUTO_FLUSH_INTERVAL.code)
                    || hasMessages(AUTO_FLUSH_CAPACITY.code);
        }

        /* to support legacy table `Event` */
        private void sendLogFromEventTable() {
            updateFlushFrequency();
            ExtractedEvent event = eventTableAdapter.getExtractEvent();

            if (event != null) {
                String lastId = event.getLastId();
                String log = event.getLog();

                /* assume that RakeAPI runs with Env.LIVE option */
                RequestResult result = HttpRequestSender.sendRequest(
                        log, Endpoint.LIVE_ENDPOINT_CHARGED.getUri());

                // TODO: remove from MessageLoop. -> HttpRequestSender
                if (RequestResult.SUCCESS == result) {
                    eventTableAdapter.removeEventById(lastId);
                } else if (RequestResult.FAILURE_RECOVERABLE == result) { // try again later
                    sendEmptyMessageDelayed(FLUSH_EVENT_TABLE.code, FLUSH_INTERVAL);
                } else if (RequestResult.FAILURE_UNRECOVERABLE == result){ // give up, we have an unrecoverable failure.
                    eventTableAdapter.removeEventById(lastId);
                } else {
                    RakeLogger.e(LOG_TAG_PREFIX, "Invalid RequestResult: " + result);
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

                    RakeLogger.t(LOG_TAG_PREFIX, "Total log count in SQLite: " + logQueueLength);

                    if (logQueueLength >= RakeConfig.TRACK_MAX_LOG_COUNT) sendLogFromLogTable();
                } else if (command == FLUSH_EVENT_TABLE) {
                    sendLogFromEventTable();
                } else if (command == MANUAL_FLUSH) {
                    sendLogFromLogTable();
                } else if (command == AUTO_FLUSH_CAPACITY) {
                    sendLogFromLogTable();
                } else if (command == AUTO_FLUSH_INTERVAL) {
                    sendLogFromLogTable();

                    if (!hasMessages(AUTO_FLUSH_INTERVAL.code))
                        sendEmptyMessageDelayed(AUTO_FLUSH_INTERVAL.code, FLUSH_INTERVAL);
                } else if (command == KILL_WORKER) {
                    RakeLogger.w(LOG_TAG_PREFIX, "Worker received a hard kill. Dumping all events and force-killing. Thread id " + Thread.currentThread().getId());
                    synchronized (handlerLock) {
                        eventTableAdapter.deleteDatabase();
                        handler = null;
                        android.os.Looper.myLooper().quit();
                    }
                } else { /* UNKNOWN COMMAND */
                    RakeLogger.e(LOG_TAG_PREFIX, "Unexpected message received by Rake worker: " + msg);
                }

            } catch (OutOfMemoryError e) {
                RakeLogger.e(LOG_TAG_PREFIX, "Caught OOM error. Rake will not send any more messages", e);
                // TODO metric

                synchronized (handlerLock) {
                    handler = null;
                    try { android.os.Looper.myLooper().quit(); }
                    catch (Exception tooLate) {
                        RakeLogger.e(LOG_TAG_PREFIX, "Can't halt looper", tooLate);
                    }
                }
            } catch (Exception e) {
                RakeLogger.e(LOG_TAG_PREFIX, "Caught unhandled exception. (ignored)", e);
                // TODO metric
            }
        } // handleMessage
    }
}
