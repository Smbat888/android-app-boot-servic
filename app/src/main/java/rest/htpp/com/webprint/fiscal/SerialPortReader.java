package rest.htpp.com.webprint.fiscal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SerialPortReader {
    InputStream mInputStream;

    public  SerialPortReader() {
        try {
            String path = (new SerialPortFinder()).getAllDevicesPath()[0];
            File file = new File(path);
            byte[] h = {0x04,0x01,0x00,0x30, (byte) 0xFF, (byte) 0xCD};
            byte[] g = new byte[50];
            SerialPort serialPort= new SerialPort(file, 115200, 0);
            serialPort.getOutputStream().write(h);
            serialPort.getOutputStream().write('\n');
           // Thread.sleep(200);
            mInputStream = serialPort.getInputStream();
            (new ReadThread()).start();
            byte[] i = g.clone();
            String[] hexArray = new String[50];
            for(int index = 0; index < i.length; index++) {
                System.out.println(String.format("||||||%02x", i[index]) );

                // maybe you have to convert your byte to int before this can be done
                // (cannot check reight now)
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[64];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public void onDataReceived(byte[] buffer, int size) {
        for(int index = 0; index < size; index++) {
            System.out.println(String.format("&&&&&&&&%02x", buffer[index]) );

            // maybe you have to convert your byte to int before this can be done
            // (cannot check reight now)
        }
    }

}
