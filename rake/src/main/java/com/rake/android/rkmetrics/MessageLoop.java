package com.rake.android.rkmetrics;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.rake.android.rkmetrics.config.RakeConfig;
import com.rake.android.rkmetrics.network.HttpRequestSender;
import com.rake.android.rkmetrics.persistent.DatabaseAdapter;
import com.rake.android.rkmetrics.util.RakeLogger;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

import static com.rake.android.rkmetrics.config.RakeConfig.*;
import static com.rake.android.rkmetrics.MessageLoop.Command.*;


/**
 * Manage communication of events with the internal database and the Rake servers.
 * <p/>
 * <p>This class straddles the thread boundary between user threads and
 * a logical Rake thread.
 */
final public class MessageLoop {

    public enum Command {
        TRACK(1),
        MANUAL_FLUSH(2),
        AUTO_FLUSH_FULL(3),
        AUTO_FLUSH_INTERVAL(4),
        KILL_WORKER (5),
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

    // messages used to communicate between the delegator and the handler
//    private static int TRACK = 1;
//    private static int MANUAL_FLUSH = 2;
//    private static int KILL_WORKER = 3;
//    private static int AUTO_FLUSH_FULL = 4;
//    private static int AUTO_FLUSH_INTERVAL = 5;

    private static volatile Handler handler;
    private static long flushInterval = RakeConfig.DEFAULT_FLUSH_INTERVAL;

    private static MessageLoop instance;
    private static DatabaseAdapter dbAdapter;

    private final Object handlerLock = new Object();
    private final Context appContext;

    private long flushCount = 0;
    private long avgFlushFrequency = 0;
    private long lastFlushTime = -1;

    private MessageLoop(Context appContext) {
        this.appContext = appContext;
        handler = createRakeMessageHandler();
    }

    public static synchronized MessageLoop getInstance(Context appContext) {
        if (null == instance) { instance = new MessageLoop(appContext); }

        return instance;
    }

    private DatabaseAdapter createRakeDbAdapter() { return DatabaseAdapter.getInstance(appContext); }

    public void track(JSONObject trackable) {
        Message m = Message.obtain();
        m.what = Command.TRACK.code;
        m.obj = trackable;

        runMessage(m);
    }

    public void flush() {
        Message m = Message.obtain();
        m.what = Command.MANUAL_FLUSH.code;

        runMessage(m);
    }

    public static void setFlushInterval(long interval /* milliseconds */) {
        flushInterval = interval;
        RakeLogger.d(LOG_TAG_PREFIX, "Set flush interval to " + interval);
    }

    public void hardKill() {
        Message m = Message.obtain();
        m.what = Command.KILL_WORKER.code;

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
        synchronized (handlerLock) { return handler == null; }
    }

    private Handler createRakeMessageHandler() {
        final SynchronousQueue<Handler> handlerQueue = new SynchronousQueue<Handler>();

        Thread thread = new Thread() {
            @Override
            public void run() {
                RakeLogger.i(LOG_TAG_PREFIX, "Starting worker thread " + this.getId());

                android.os.Looper.prepare();

                try {
                    handlerQueue.put(new RakeMessageHandler());
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
            RakeLogger.t(LOG_TAG_PREFIX, "Avg flush frequency approximately " + seconds + " seconds.");
        }

        lastFlushTime = now;
        flushCount = newFlushCount;
    }

    private class RakeMessageHandler extends Handler {
        public RakeMessageHandler() {
            super();

            dbAdapter = createRakeDbAdapter();
            RakeLogger.t(LOG_TAG_PREFIX, "Remove expired logs (48 hours before)");
            dbAdapter.removeEvent(System.currentTimeMillis() - RakeConfig.DATA_EXPIRATION_TIME);

            /* send initial auto-flush message */
            sendEmptyMessageDelayed(AUTO_FLUSH_INTERVAL.code, flushInterval);
        }

        private void sendTrackedLogFromTable() {
            updateFlushFrequency();
            String[] event = dbAdapter.getEventList();

            if (event != null) {
                // TODO mapper class
                String lastId = event[0];
                String rawMessage = event[1];

                // TODO: convert instance method, support multiple urls
                HttpRequestSender.RequestResult result = HttpRequestSender.sendRequest(
                        rawMessage,
                        RakeAPI.getBaseEndpoint() + ENDPOINT_TRACK_PATH);

                // TODO: remove from MessageLoop. -> HttpRequestSender
                if (HttpRequestSender.RequestResult.SUCCESS == result) {
                    dbAdapter.removeEvent(lastId);
                } else if (HttpRequestSender.RequestResult.FAILURE_RECOVERABLE == result) { // try again later
                    if (!hasMessages(MANUAL_FLUSH.code)) {
                        sendEmptyMessageDelayed(MANUAL_FLUSH.code, flushInterval);
                    }
                } else if (HttpRequestSender.RequestResult.FAILURE_UNRECOVERABLE == result){ // give up, we have an unrecoverable failure.
                    dbAdapter.removeEvent(lastId);
                } else {
                    RakeLogger.e(LOG_TAG_PREFIX, "invalid RequestResult: " + result);
                }
            }
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                Command command = Command.fromCode(msg.what);

                if (command == TRACK) {
                    JSONObject message = (JSONObject) msg.obj;
                    int logQueueLength = dbAdapter.addEvent(message);
                    RakeLogger.t(LOG_TAG_PREFIX, "Total log count in SQLite: " + logQueueLength);

                    if (logQueueLength >= RakeConfig.TRACK_MAX_LOG_COUNT) sendTrackedLogFromTable();

                } else if (command == MANUAL_FLUSH) {
                    sendTrackedLogFromTable();
                } else if (command == AUTO_FLUSH_FULL) {
                    sendTrackedLogFromTable();
                } else if (command == AUTO_FLUSH_INTERVAL) {
                    sendTrackedLogFromTable();

                    if (!hasMessages(AUTO_FLUSH_INTERVAL.code))
                        sendEmptyMessageDelayed(AUTO_FLUSH_INTERVAL.code, flushInterval);
                } else if (command == KILL_WORKER) {
                    RakeLogger.w(LOG_TAG_PREFIX, "Worker received a hard kill. Dumping all events and force-killing. Thread id " + Thread.currentThread().getId());
                    synchronized (handlerLock) {
                        dbAdapter.deleteDB();
                        handler = null;
                        android.os.Looper.myLooper().quit();
                    }
                } else { /* UNKNOWN COMMAND */
                    RakeLogger.e(LOG_TAG_PREFIX, "Unexpected message received by Rake worker: " + msg);
                }

            } catch (RuntimeException e) {
                RakeLogger.e(LOG_TAG_PREFIX, "Worker throw unhandled exception. will not send any more Rake messages", e);

                synchronized (handlerLock) {
                    handler = null;
                    try { android.os.Looper.myLooper().quit(); }
                    catch (Exception tooLate) {
                        RakeLogger.e(LOG_TAG_PREFIX, "Can't halt looper", tooLate);
                    }
                }

                throw e;
            }
        } // handleMessage
    }
}
