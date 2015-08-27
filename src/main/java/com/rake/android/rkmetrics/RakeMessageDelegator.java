package com.rake.android.rkmetrics;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.rake.android.rkmetrics.config.RakeConfig;
import com.rake.android.rkmetrics.network.RakeHttpSender;
import com.rake.android.rkmetrics.persistent.RakeDbAdapter;
import com.rake.android.rkmetrics.util.RakeLogger;
import org.json.JSONObject;

import java.util.concurrent.SynchronousQueue;

import static com.rake.android.rkmetrics.config.RakeConfig.*;


/**
 * Manage communication of events with the internal database and the Rake servers.
 * <p/>
 * <p>This class straddles the thread boundary between user threads and
 * a logical Rake thread.
 */
final public class RakeMessageDelegator {
    // messages used to communicate between the delegator and the handler
    private static int TRACK = 1;
    private static int FLUSH = 2;
    private static int KILL_WORKER = 3;

    private static volatile Handler handler;
    private static long flushInterval = RakeConfig.DEFAULT_FLUSH_INTERVAL;

    private static RakeMessageDelegator instance;
    private static RakeDbAdapter dbAdapter;

    private final Object handlerLock = new Object();
    private final Context appContext;

    private long flushCount = 0;
    private long avgFlushFrequency = 0;
    private long lastFlushTime = -1;

    private RakeMessageDelegator(Context appContext) {
        this.appContext = appContext;
        handler = createRakeMessageHandler();
    }

    public static synchronized RakeMessageDelegator getInstance(Context appContext) {
        if (null == instance) { instance = new RakeMessageDelegator(appContext); }

        return instance;
    }

    private RakeDbAdapter createRakeDbAdapter() { return RakeDbAdapter.getInstance(appContext); }

    public void track(JSONObject trackable) {
        Message m = Message.obtain();
        m.what = TRACK;
        m.obj = trackable;

        runMessage(m);
    }

    public void flush() {
        Message m = Message.obtain();
        m.what = FLUSH;

        runMessage(m);
    }

    public static void setFlushInterval(long interval /* milliseconds */) {
        flushInterval = interval;
        RakeLogger.d(LOG_TAG_PREFIX, "set flush interval to " + interval);
    }

    public void hardKill() {
        Message m = Message.obtain();
        m.what = KILL_WORKER;

        runMessage(m);
    }

    private void runMessage(Message msg) {
        if (isDead()) {
            // thread died under suspicious circumstances.
            // don't try to send any more events.
            RakeLogger.e(LOG_TAG_PREFIX, "dead rake worker dropping a message: " + msg);
        } else {
            synchronized (handlerLock) {
                if (handler != null) handler.sendMessage(msg);
            }
        }
    }

    private boolean isDead() {
        synchronized (handlerLock) { return handler == null; }
    }

    private Handler createRakeMessageHandler() {
        Handler handler = null;

        final SynchronousQueue<Handler> handlerQueue = new SynchronousQueue<Handler>();

        Thread thread = new Thread() {
            @Override
            public void run() {
                RakeLogger.i(LOG_TAG_PREFIX, "starting worker thread " + this.getId());

                Looper.prepare();

                try {
                    handlerQueue.put(new RakeMessageHandler());
                } catch (InterruptedException e) {
                    throw new RuntimeException("couldn't build worker thread for Analytics Messages", e);
                }

                try {
                    Looper.loop();
                } catch (RuntimeException e) {
                    RakeLogger.e(LOG_TAG_PREFIX, "rake Thread dying from RuntimeException", e);
                }
            }
        };

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

        try {
            handler = handlerQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException("couldn't retrieve handler from worker thread");
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
            RakeLogger.t(LOG_TAG_PREFIX, "avg flush frequency approximately " + seconds + " seconds.");
        }

        lastFlushTime = now;
        flushCount = newFlushCount;
    }

    private class RakeMessageHandler extends Handler {
        public RakeMessageHandler() {
            super();

            dbAdapter = createRakeDbAdapter();
            RakeLogger.t(LOG_TAG_PREFIX, "remove expired logs (48 hours before)");
            dbAdapter.cleanupEvents(
                    System.currentTimeMillis() - RakeConfig.DATA_EXPIRATION_TIME,
                    RakeDbAdapter.Table.EVENTS);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                int logQueueLength = -1;

                if (msg.what == TRACK) {
                    JSONObject message = (JSONObject) msg.obj;
                    logQueueLength = dbAdapter.addJSON(message, RakeDbAdapter.Table.EVENTS);
                    RakeLogger.t(LOG_TAG_PREFIX, "total log count in SQLite: " + logQueueLength);

                } else if (msg.what == FLUSH) {
                    sendTrackedLogFromTable();

                } else if (msg.what == KILL_WORKER) {
                    RakeLogger.w(LOG_TAG_PREFIX, "worker received a hard kill. Dumping all events and force-killing. Thread id " + Thread.currentThread().getId());
                    synchronized (handlerLock) {
                        dbAdapter.deleteDB();
                        handler = null;
                        Looper.myLooper().quit();
                    }

                } else {
                    RakeLogger.e(LOG_TAG_PREFIX, "unexpected message received by Rake worker: " + msg);
                }

                if (logQueueLength >= RakeConfig.TRACK_MAX_LOG_COUNT) {
                    RakeLogger.t(LOG_TAG_PREFIX, "log queue is full");
                    sendTrackedLogFromTable();

                } else if (logQueueLength > 0) {
                    if (!hasMessages(FLUSH)) {
                        sendEmptyMessageDelayed(FLUSH, flushInterval); // schedule flush()
                    }
                }
            } catch (RuntimeException e) {
                RakeLogger.e(LOG_TAG_PREFIX, "worker throw unhandled exception. will not send any more Rake messages", e);

                synchronized (handlerLock) {
                    handler = null;
                    try { Looper.myLooper().quit(); }
                    catch (Exception tooLate) {
                        RakeLogger.e(LOG_TAG_PREFIX, "can't halt looper", tooLate);
                    }
                }

                throw e;
            }
        } // handleMessage

        private void sendTrackedLogFromTable() {
            updateFlushFrequency();
            RakeDbAdapter.Table trackLogTable = RakeDbAdapter.Table.EVENTS;
            String[] event = dbAdapter.generateDataString(trackLogTable);


            if (event != null) {
                String lastId = event[0];
                String rawMessage = event[1];

                RakeHttpSender.RequestResult result = RakeHttpSender.sendPostRequest(
                        rawMessage,
                        RakeAPI.getBaseEndpoint(), // TODO: convert instance method, support multiple urls
                        ENDPOINT_TRACK_PATH);

                // TODO: remove from RakeMessageDelegator. -> RakeHttpSender
                if (RakeHttpSender.RequestResult.SUCCESS == result) {
                    dbAdapter.cleanupEvents(lastId, trackLogTable);
                } else if (RakeHttpSender.RequestResult.FAILURE_RECOVERABLE == result) { // try again later
                    if (!hasMessages(FLUSH)) { sendEmptyMessageDelayed(FLUSH, flushInterval); }
                } else if (RakeHttpSender.RequestResult.FAILURE_UNRECOVERABLE == result){ // give up, we have an unrecoverable failure.
                    dbAdapter.cleanupEvents(lastId, trackLogTable);
                } else {
                    RakeLogger.e(LOG_TAG_PREFIX, "invalid RequestResult: " + result);
                }
            }
        }

    }

}
