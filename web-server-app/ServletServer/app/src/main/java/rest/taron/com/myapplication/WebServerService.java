package rest.taron.com.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * The android service class which runs web server in corresponding thread.
 * The class is public to be accessible by android system
 *
 */
public class WebServerService extends Service {

    static final String NEVER_KILLED_SERVER = "NEVER_KILLED_SERVER";
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
            Log.d("WebServerService: ", e.getMessage());
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
     * @throws Exception
     */
    public void runWebServerThread() throws Exception {
        mHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(getBaseContext(), "Service is connected", Toast.LENGTH_LONG).show();
                HttpServerThread httpServerThread = new HttpServerThread(SERVER_PORT);
                httpServerThread.start();
            }
        });
    }
}
