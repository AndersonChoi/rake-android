package com.rake.android.rkmetrics.core;

import static com.rake.android.rkmetrics.config.RakeConfig.LOG_TAG_PREFIX;
import static com.rake.android.rkmetrics.config.RakeConfig.ENDPOINT_TRACK_PATH;

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


/**
 * Manage communication of events with the internal database and the Rake servers.
 * <p/>
 * <p>This class straddles the thread boundary between user threads and
 * a logical Rake thread.
 */
final public class RakeMessageDelegator {

    // Messages for our thread
    private static int ENQUEUE_EVENTS = 1; // push given JSON message to people DB
    private static int FLUSH_QUEUE = 2; // push given JSON message to events DB
    private static int KILL_WORKER = 5; // Hard-kill the worker thread, discarding all events on the event queue.

    private static volatile Handler handler;
    private static long flushInterval = RakeConfig.DEFAULT_FLUSH_INTERVAL;
    private static RakeMessageDelegator instance;

    private final Object handlerLock = new Object();
    private String baseEndpoint = RakeConfig.LIVE_BASE_ENDPOINT;
    private long flushCount = 0;
    private long avgFlushFrequency = 0;
    private long lastFlushTime = -1;
    private final Context appContext;

    private RakeMessageDelegator(Context context) {
        this.appContext = context.getApplicationContext();
        handler = createRakeMessageHandlerOnce();
    }

    public static synchronized RakeMessageDelegator getInstance(Context context) {
        if (null == instance) { instance = new RakeMessageDelegator(context); }

        return instance;
    }

    public void track(JSONObject trackable) {
        Message m = Message.obtain();
        m.what = ENQUEUE_EVENTS;
        m.obj = trackable;

        runMessage(m);
    }

    public void flush() {
        Message m = Message.obtain();
        m.what = FLUSH_QUEUE;
        m.obj = baseEndpoint;

        runMessage(m);
    }

    public static void setFlushInterval(long interval /* milliseconds */) {
        flushInterval = interval;
        RakeLogger.d(LOG_TAG_PREFIX, "Changing flush interval to " + interval);
    }

    public void setEndpointHost(String baseEndpoint) {
        this.baseEndpoint = baseEndpoint;
        RakeLogger.d(LOG_TAG_PREFIX, "Setting endpoint API host to " + baseEndpoint);
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
            RakeLogger.e(LOG_TAG_PREFIX, "Dead rake worker dropping a message: " + msg);
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

    // NOTE that the returned worker will run FOREVER, unless you send a hard kill which you really shouldn't
    private Handler createRakeMessageHandlerOnce() {
        Handler h = null;

        final SynchronousQueue<Handler> handlerQueue = new SynchronousQueue<Handler>();

        Thread thread = new Thread() {
            @Override
            public void run() {
                RakeLogger.i(LOG_TAG_PREFIX, "Starting worker thread " + this.getId());

                Looper.prepare();

                try {
                    handlerQueue.put(new RakeMessageHandler());
                } catch (InterruptedException e) {
                    throw new RuntimeException("Couldn't build worker thread for Analytics Messages", e);
                }

                try {
                    Looper.loop();
                } catch (RuntimeException e) {
                    RakeLogger.e(LOG_TAG_PREFIX, "Rake Thread dying from RuntimeException", e);
                }
            }
        };

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

        try {
            h = handlerQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException("Couldn't retrieve handler from worker thread");
        }

        return h;
    }

    private static RakeDbAdapter createRakeDbAdapter(Context context) {
        return new RakeDbAdapter(context);
    }

    private void updateFlushFrequency() {
        long now = System.currentTimeMillis();
        long newFlushCount = flushCount + 1;

        if (lastFlushTime > 0) {
            long flushInterval = now - lastFlushTime;
            long totalFlushTime = flushInterval + (avgFlushFrequency * flushCount);
            avgFlushFrequency = totalFlushTime / newFlushCount;

            long seconds = avgFlushFrequency / 1000;
            RakeLogger.t(LOG_TAG_PREFIX, "Average send frequency approximately " + seconds + " seconds.");
        }

        lastFlushTime = now;
        flushCount = newFlushCount;
    }

    private class RakeMessageHandler extends Handler {
        private final RakeDbAdapter rakeDbAdapter;

        public RakeMessageHandler() {
            super();
            rakeDbAdapter = createRakeDbAdapter(appContext);
            rakeDbAdapter.cleanupEvents(System.currentTimeMillis() - RakeConfig.DATA_EXPIRATION_TIME, RakeDbAdapter.Table.EVENTS);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                int logQueueLength = -1;

                if (msg.what == ENQUEUE_EVENTS) {
                    JSONObject message = (JSONObject) msg.obj;
                    logQueueLength = rakeDbAdapter.addJSON(message, RakeDbAdapter.Table.EVENTS);
                    RakeLogger.t(LOG_TAG_PREFIX, "save JSONObject to SQLite: \n" + message.toString());
                    RakeLogger.t(LOG_TAG_PREFIX, "total log count in SQLite: " + logQueueLength);

                } else if (msg.what == FLUSH_QUEUE) {
                    RakeLogger.t(LOG_TAG_PREFIX, "flush SQLite");
                    updateFlushFrequency();
                    sendAllData();

                } else if (msg.what == KILL_WORKER) {
                    RakeLogger.w(LOG_TAG_PREFIX, "Worker received a hard kill. Dumping all events and force-killing. Thread id " + Thread.currentThread().getId());
                    synchronized (handlerLock) {
                        rakeDbAdapter.deleteDB();
                        handler = null;
                        Looper.myLooper().quit();
                    }

                } else {
                    RakeLogger.e(LOG_TAG_PREFIX, "Unexpected message received by Rake worker: " + msg);
                }

                if (logQueueLength >= RakeConfig.TRACK_MAX_LOG_COUNT) {
                    RakeLogger.t(LOG_TAG_PREFIX, "Flushing queue due to bulk upload limit");
                    updateFlushFrequency();
                    sendAllData();

                } else if (logQueueLength > 0) {
                    if (!hasMessages(FLUSH_QUEUE)) {
                        RakeLogger.t(LOG_TAG_PREFIX, "Queue depth " + logQueueLength + " - Adding flush in " + flushInterval);
                        // The hasMessages check is a courtesy for the common case
                        // of delayed flushes already enqueued from inside of this thread.
                        // Callers outside of this thread can still send
                        // a flush right here, so we may end up with two flushes
                        // in our queue, but we're ok with that.
                        sendEmptyMessageDelayed(FLUSH_QUEUE, flushInterval);
                    }
                }
            } catch (RuntimeException e) {
                RakeLogger.e(LOG_TAG_PREFIX, "Worker threw an unhandled exception- will not send any more Rake messages", e);

                synchronized (handlerLock) {
                    handler = null;
                    try { Looper.myLooper().quit(); }
                    catch (Exception tooLate) {
                        RakeLogger.e(LOG_TAG_PREFIX, "Could not halt looper", tooLate);
                    }
                }

                throw e;
            }
        } // handleMessage

        private void sendAllData() {
            RakeDbAdapter.Table trackLogTable = RakeDbAdapter.Table.EVENTS;
            String[] event = rakeDbAdapter.generateDataString(trackLogTable);

            if (event != null) {
                String lastId = event[0];
                String rawMessage = event[1];

                RakeHttpSender.RequestResult result = RakeHttpSender.sendPostRequest(rawMessage, baseEndpoint, ENDPOINT_TRACK_PATH);

                if (RakeHttpSender.RequestResult.SUCCESS == result) {
                    rakeDbAdapter.cleanupEvents(lastId, trackLogTable);
                } else if (RakeHttpSender.RequestResult.FAILURE_RECOVERABLE == result) { // try again later
                    if (!hasMessages(FLUSH_QUEUE)) { sendEmptyMessageDelayed(FLUSH_QUEUE, flushInterval); }
                } else if (RakeHttpSender.RequestResult.FAILURE_UNRECOVERABLE == result){ // give up, we have an unrecoverable failure.
                    rakeDbAdapter.cleanupEvents(lastId, trackLogTable);
                } else {
                    RakeLogger.e(LOG_TAG_PREFIX, "invalid RequestResult: " + result);
                }
            }
        }

    }

}
