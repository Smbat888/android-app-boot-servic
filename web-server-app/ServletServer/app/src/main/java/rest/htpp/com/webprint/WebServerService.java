package rest.htpp.com.webprint;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

/**
 * The android service class which runs web server in corresponding thread.
 * The class is public to be accessible by android system
 *
 */
public class WebServerService extends Service {

    static final String TAG = "WebServerService";
    static final String NEVER_KILLED_SERVER = "NEVER_KILLED_SERVER";
    public static final String TOAST_TEXT = "Service is connected";
    private static final int SERVER_PORT = 8888;

    private Handler mHandler = new Handler();

    public WebServerService() {
        // Empty constructor
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            runWebServerThread();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent(NEVER_KILLED_SERVER));
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        sendBroadcast(new Intent(NEVER_KILLED_SERVER));
    }

    /**
     * Start http web service thread in separate thread
     *
     */
    public void runWebServerThread() {
        mHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(getBaseContext(), TOAST_TEXT, Toast.LENGTH_LONG).show();
                serverRunner();
            }
        });
    }

    //Helper Methods

    /**
     * Runs server with the specified port
     */
    private void serverRunner() {
        final AndroidWebServer androidWebServer = new AndroidWebServer(SERVER_PORT);
        try {
            androidWebServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
        } catch (IOException e) {
            Log.e(TAG,"Couldn't start server:" + e);
            System.exit(-1);
        }
    }
}