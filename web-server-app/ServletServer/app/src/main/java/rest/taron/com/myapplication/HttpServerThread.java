package rest.taron.com.myapplication;


import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServerThread extends Thread {

    private static final String TAG = "WebPrintServer";

    /**
     * The Socket that we listen to.
     */
    Socket mSocket;

    /**
     * For loading files to serve.
     */
    private final AssetManager mAssets;

    /**
     * The port number we listen to
     */
    private final int mPort;

    HttpServerThread(final int port, AssetManager assets) {
        mAssets = assets;
        mPort = port;
    }


    @Override
    public void run() {
        ServerSocket httpServerSocket = null;
        try {
            httpServerSocket = new ServerSocket(mPort);
            while (true) {
                mSocket = httpServerSocket.accept();
                HttpResponseThread httpResponseThread = new HttpResponseThread(
                        mSocket, mAssets);
                httpResponseThread.start();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (null != httpServerSocket) {
                    httpServerSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

    }
}
