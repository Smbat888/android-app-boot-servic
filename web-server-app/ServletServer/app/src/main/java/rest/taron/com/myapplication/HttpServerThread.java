package rest.taron.com.myapplication;


import android.util.Log;

import java.io.IOException;

/**
 * The http thread which will run android web server separately.
 *
 */
class HttpServerThread extends Thread {

    private static final String TAG = "WebPrintServer";

    private final int mPort;

    HttpServerThread(final int port) {
        mPort = port;
    }

    @Override
    public void run() {
        while (true) {
            final AndroidWebServer androidWebServer = new AndroidWebServer(mPort);
            try {
                androidWebServer.start();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
