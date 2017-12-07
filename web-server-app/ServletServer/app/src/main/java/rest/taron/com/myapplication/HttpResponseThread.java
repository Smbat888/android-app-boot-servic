package rest.taron.com.myapplication;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class HttpResponseThread extends Thread {

    Socket socket;
    String h1;
    private String msgLog;

    HttpResponseThread(Socket socket, String msg){
        this.socket = socket;
        h1 = msg;
    }

    @Override
    public void run() {
        BufferedReader is;
        PrintWriter os;
        String request;

        try {
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            os = new PrintWriter(socket.getOutputStream(), true);

            String response =
                    "<html><head></head>" +
                            "<body>" +
                            "<h1>" + h1 + "</h1>" +
                            "</body></html>";

            os.print("HTTP/1.0 200" + "\r\n");
            os.print("Content type: text/html" + "\r\n");
            os.print("Content length: " + response.length() + "\r\n");
            os.print("\r\n");
            os.print(response + "\r\n");
            os.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMsgLog() {
        return msgLog;
    }

    public void setMsgLog(String msgLog) {
        this.msgLog = msgLog;
    }

}