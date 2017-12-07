package rest.taron.com.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class WebServerService extends Service {

    private Handler mHandler = new Handler();

    public WebServerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            runOnNewThread();
        } catch (Exception e) {
            Log.d("WebServerService: ", e.getMessage());
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent("YouWillNeverKillMe"));
    }

    public void runOnNewThread() throws Exception {
        mHandler.post(new Runnable() {
            public void run() {
                for (int i = 0; i < 10; i++) {
                    Toast.makeText(getBaseContext(), "test", Toast.LENGTH_LONG).show();
                }
                HttpServerThread httpServerThread = new HttpServerThread(MainActivity.SERVER_PORT, getBaseContext().getAssets());
                httpServerThread.start();
            }
        });
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        sendBroadcast(new Intent("YouWillNeverKillMe"));
    }
}
