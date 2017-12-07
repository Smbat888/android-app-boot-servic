package rest.taron.com.myapplication;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServerThread extends Thread {

    static final int HttpServerPORT = 8888;
    @Override
    public void run() {
        Socket socket = null;
        ServerSocket httpServerSocket = null;
        try {
            httpServerSocket = new ServerSocket(HttpServerPORT);
            while(true){
                socket = httpServerSocket.accept();

                HttpResponseThread httpResponseThread = new HttpResponseThread(
                                socket,
                                "Hello from web server");
                httpResponseThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != httpServerSocket) {
                    httpServerSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
