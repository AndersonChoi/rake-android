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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;


/**
 * Manage communication of events with the internal database and the Rake servers.
 * <p/>
 * <p>This class straddles the thread boundary between user threads and
 * a logical Rake thread.
 */
final public class WorkerSupervisor {

    // Messages for our thread
    private static int ENQUEUE_EVENTS = 1; // push given JSON message to people DB
    private static int FLUSH_QUEUE = 2; // push given JSON message to events DB
    private static int SET_FLUSH_INTERVAL = 4; // Reset frequency of flush interval
    private static int KILL_WORKER = 5; // Hard-kill the worker thread, discarding all events on the event queue.
    private static int SET_ENDPOINT_HOST = 6; // Use obj.toString() as the first part of the URL for api requests.

    private static final Map<Context, WorkerSupervisor> instances = new HashMap<Context, WorkerSupervisor>();

    private final Object handlerLock = new Object();
    private Handler handler;

    private long flushInterval = RakeConfig.DEFAULT_FLUSH_RAKE;
    private long flushCount = 0;
    private long aveFlushFrequency = 0;
    private long lastFlushTime = -1;

    // Used across thread boundaries
    private final Worker worker;
    private final Context context;

    /**
     * Do not call directly. You should call WorkerSupervisor.getInstance()
     */
    private WorkerSupervisor(Context context) {
        this.context = context;
        worker = new Worker();
    }

    public static WorkerSupervisor getInstance(Context context) {
        synchronized (instances) {
            Context appContext = context.getApplicationContext();

            WorkerSupervisor ret;
            if (!instances.containsKey(appContext)) {
                RakeLogger.d(LOG_TAG_PREFIX, "Constructing new WorkerSupervisor for Context " + appContext);
                ret = new WorkerSupervisor(appContext);
                instances.put(appContext, ret);
            } else {
                RakeLogger.d(LOG_TAG_PREFIX, "WorkerSupervisor for Context " + appContext + " already exists");
                ret = instances.get(appContext);
            }

            return ret;
        }
    }

    public void track(JSONObject trackable) {
        Message m = Message.obtain();
        m.what = ENQUEUE_EVENTS;
        m.obj = trackable;

        worker.runMessage(m);
    }

    public void flush() {
        Message m = Message.obtain();
        m.what = FLUSH_QUEUE;

        worker.runMessage(m);
    }

    public void setFlushInterval(long milliseconds) {
        Message m = Message.obtain();
        m.what = SET_FLUSH_INTERVAL;
        m.obj = new Long(milliseconds);

        worker.runMessage(m);
    }

    public void setEndpointHost(String endpointHost) {
        Message m = Message.obtain();
        m.what = SET_ENDPOINT_HOST;
        m.obj = endpointHost;

        worker.runMessage(m);
    }

    public void hardKill() {
        Message m = Message.obtain();
        m.what = KILL_WORKER;

        worker.runMessage(m);
    }

    // For testing, to allow for Mocking.

    /* package */ boolean isDead() {
        return worker.isDead();
    }

    protected RakeDbAdapter createRakeDbAdapter(Context context) {
        return new RakeDbAdapter(context);
    }

    protected RakeHttpSender getHttpSender(String endpointBase) {
        return new RakeHttpSender(endpointBase);
    }

    // Worker will manage the (at most single) IO thread associated with
    // this WorkerSupervisor instance.
    private class Worker {
        public Worker() {
            handler = createWorkerMessageHandler();
        }

        public boolean isDead() {
            synchronized (handlerLock) {
                return handler == null;
            }
        }

        public void runMessage(Message msg) {
            if (isDead()) {
                // thread died under suspicious circumstances.
                // don't try to send any more events.
                RakeLogger.t(LOG_TAG_PREFIX, "Dead rake worker dropping a message: " + msg);
            } else {
                synchronized (handlerLock) {
                    if (handler != null) handler.sendMessage(msg);
                }
            }
        }

        // NOTE that the returned worker will run FOREVER, unless you send a hard kill which you really shouldn't
        private Handler createWorkerMessageHandler() {
            Handler ret = null;

            final SynchronousQueue<Handler> handlerQueue = new SynchronousQueue<Handler>();

            Thread thread = new Thread() {
                @Override
                public void run() {
                    RakeLogger.i(LOG_TAG_PREFIX, "Starting worker thread " + this.getId());

                    Looper.prepare();

                    try {
                        handlerQueue.put(new WorkerMessageHandler());
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
                ret = handlerQueue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException("Couldn't retrieve handler from worker thread");
            }

            return ret;
        }

        private class WorkerMessageHandler extends Handler {
            public WorkerMessageHandler() {
                super();
                rakeDbAdapter = createRakeDbAdapter(context);
                rakeDbAdapter.cleanupEvents(System.currentTimeMillis() - RakeConfig.DATA_EXPIRATION, RakeDbAdapter.Table.EVENTS);
            }

            @Override
            public void handleMessage(Message msg) {
                try {
                    int queueDepth = -1;

                    if (msg.what == SET_FLUSH_INTERVAL) {
                        Long newIntervalObj = (Long) msg.obj;
                        RakeLogger.t(LOG_TAG_PREFIX, "Changing flush interval to " + newIntervalObj);
                        flushInterval = newIntervalObj.longValue();
                        removeMessages(FLUSH_QUEUE);

                    } else if (msg.what == SET_ENDPOINT_HOST) {
                        endPoint = msg.obj == null ? null : msg.obj.toString();
                        RakeLogger.t(LOG_TAG_PREFIX, "Setting endpoint API host to " + endPoint);

                    } else if (msg.what == ENQUEUE_EVENTS) {
                        JSONObject message = (JSONObject) msg.obj;
                        queueDepth = rakeDbAdapter.addJSON(message, RakeDbAdapter.Table.EVENTS);

                        RakeLogger.t(LOG_TAG_PREFIX, "Queuing event for sending later");
                        RakeLogger.t(LOG_TAG_PREFIX, "    " + message.toString());


                    } else if (msg.what == FLUSH_QUEUE) {
                        RakeLogger.t(LOG_TAG_PREFIX, "Flushing queue due to scheduled or forced flush");
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

                    if (queueDepth >= RakeConfig.BULK_UPLOAD_LIMIT) {
                        RakeLogger.t(LOG_TAG_PREFIX, "Flushing queue due to bulk upload limit");
                        updateFlushFrequency();
                        sendAllData();

                    } else if (queueDepth > 0) {
                        if (!hasMessages(FLUSH_QUEUE)) {
                            RakeLogger.t(LOG_TAG_PREFIX, "Queue depth " + queueDepth + " - Adding flush in " + flushInterval);
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
                RakeLogger.t(LOG_TAG_PREFIX, "Sending records to Rake");

                RakeDbAdapter.Table trackLogTable = RakeDbAdapter.Table.EVENTS;
                String[] eventsData = rakeDbAdapter.generateDataString(trackLogTable);

                if (eventsData != null) {
                    String lastId = eventsData[0];
                    String rawMessage = eventsData[1];

                    RakeHttpSender poster = getHttpSender(endPoint);
                    RakeHttpSender.RequestResult result = poster.postData(rawMessage, ENDPOINT_TRACK_PATH);

                    if (RakeHttpSender.RequestResult.SUCCESS == result) {
                        RakeLogger.t(LOG_TAG_PREFIX, "Posted to " + ENDPOINT_TRACK_PATH);
                        rakeDbAdapter.cleanupEvents(lastId, trackLogTable);
                    } else if (RakeHttpSender.RequestResult.FAILURE_RECOVERABLE == result) { // try again later
                        if (!hasMessages(FLUSH_QUEUE)) { sendEmptyMessageDelayed(FLUSH_QUEUE, flushInterval); }
                    } else if (RakeHttpSender.RequestResult.FAILURE_UNRECOVERABLE == result){ // give up, we have an unrecoverable failure.
                        rakeDbAdapter.cleanupEvents(lastId, trackLogTable);
                    } else {
                        RakeLogger.t(LOG_TAG_PREFIX, "invalid HttpRequestResponse: " + result);
                    }
                }
            }

            private String endPoint = RakeConfig.LIVE_BASE_ENDPOINT;
            private final RakeDbAdapter rakeDbAdapter;
        } // WorkerMessageHandler

        private void updateFlushFrequency() {
            long now = System.currentTimeMillis();
            long newFlushCount = flushCount + 1;

            if (lastFlushTime > 0) {
                long flushInterval = now - lastFlushTime;
                long totalFlushTime = flushInterval + (aveFlushFrequency * flushCount);
                aveFlushFrequency = totalFlushTime / newFlushCount;

                long seconds = aveFlushFrequency / 1000;
                RakeLogger.t(LOG_TAG_PREFIX, "Average send frequency approximately " + seconds + " seconds.");
            }

            lastFlushTime = now;
            flushCount = newFlushCount;
        }
    }
}
