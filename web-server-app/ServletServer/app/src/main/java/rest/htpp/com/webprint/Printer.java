package rest.htpp.com.webprint;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.basewin.aidl.OnPrinterListener;
import com.basewin.services.PrinterBinder;
import com.basewin.services.ServiceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

import rest.htpp.com.webprint.Constants.PrintConstants;

public class Printer {

    private static final String TAG = "Printer";
    private static final PrinterListener printerCallback = new PrinterListener();
    private static JSONObject printRequest;
    private static JSONArray printArray;
    private static JSONObject printJson;


    public static Map<Integer,String> print(final Document xml) {
        final Map<Integer,String> printStatus = new HashMap<>();
        final Element textElement = (Element) xml.getElementsByTagName(PrintConstants.PRINT_TEXT_TAG).item(0);
        final Element picElement = (Element) xml.getElementsByTagName(PrintConstants.PRINT_PIC_TAG).item(0);
        try {
            if (null != textElement) {
                final String text = textElement.getElementsByTagName(PrintConstants.PRINT_TEXT_DATA_TAG).item(0).getTextContent();
                final String fontName = textElement.getElementsByTagName(PrintConstants.PRINT_TEXT_FONT_NAME_TAG).item(0).getTextContent();
                final int fontSize = Integer.valueOf(textElement.getElementsByTagName(PrintConstants.PRINT_TEXT_FONT_SIZE_TAG).item(0).getTextContent());
                if(null == text || null == fontName ) {
                    throw new JSONException("XML file does not contain 'textInfo' tags");
                }
                printText(text, fontName, fontSize);
                printStatus.put(200,"OK");
            }
            if (null != picElement) {
                final String picBytes = picElement.getTextContent();
                if(null == picBytes ) {
                    throw new JSONException("XML file does not contain 'prnraw64' tags");
                }
                Thread.sleep(1000);
                printPic(Base64.decode(picBytes, Base64.DEFAULT));
                if(printStatus.isEmpty()){
                    printStatus.put(200,"OK");
                }
            }
            if (printStatus.isEmpty()){
                printStatus.put(400, "XML file does not contain necessary tags");
            }
        } catch (JSONException e) {
            printStatus.put(400, "Malformed or empty XML file");
        } catch (Exception e) {
            printStatus.put(400, "Print failed");
        }
        return printStatus;
    }

    /**
     * Pint text content
     */
    private static void printText(final String text, final String fontName, final int size) throws Exception {
        printRequest = new JSONObject();
        printArray = new JSONArray();
        printJson = new JSONObject();
        // Add text printing
        printRequest.put(PrintConstants.CONTENT_TYPE, "txt");
        printRequest.put(PrintConstants.CONTENT, text);
        printRequest.put(PrintConstants.FONT_SIZE, size);
        printRequest.put(PrintConstants.POSITION, "left");
        printRequest.put(PrintConstants.OFFSET, "0");
        printRequest.put(PrintConstants.BOLD, 0);
        printRequest.put(PrintConstants.ITALIC, 0);
        printRequest.put(PrintConstants.HEIGHT, "-1");
        ServiceManager.getInstence().getPrinter().setPrintFont(fontName);
        printArray.put(printRequest);
        printJson.put("spos", printArray);
        ServiceManager.getInstence().getPrinter().
                print(printJson.toString(), null, printerCallback);

    }

    /**
     * Print picture content
     */
    private static void printPic(final byte[] decodedString) throws Exception {
        Log.i(TAG, "Print picture");
        // Add text printer
        // Add picture
        printRequest = new JSONObject();
        printArray = new JSONArray();
        printJson = new JSONObject();
        printRequest.put(PrintConstants.CONTENT_TYPE, "jpg");
        printRequest.put(PrintConstants.POSITION, "center");
        printArray.put(printRequest);
        printJson.put("spos", printArray);
        // Set at the bottom of the empty 3 rows
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        Bitmap[] bitmaps = new Bitmap[]{decodedByte};
        ServiceManager.getInstence().getPrinter().setPrintGray(1000);
        ServiceManager.getInstence().getPrinter().print(printJson.toString(), bitmaps, printerCallback);

    }

    static class PrinterListener implements OnPrinterListener {

        final String TAG = "PrinterListener";

        @Override
        public void onStart() {
            // Print start
            Log.i(TAG, "start print");
        }

        @Override
        public void onFinish() {
            // End of the print
            Log.i(TAG, "End of print");
        }

        @Override
        public void onError(int errorCode, String detail) {
            // print error
            if (errorCode == PrinterBinder.PRINTER_ERROR_NO_PAPER) {
                Log.e(TAG, "paper runs out during printing");
            }
            if (errorCode == PrinterBinder.PRINTER_ERROR_OVER_HEAT) {
                Log.i(TAG, "over heat during printing");
            }
            if (errorCode == PrinterBinder.PRINTER_ERROR_OTHER) {
                Log.i(TAG, "other error happen during printing");
            }
        }
    }

    private Printer() {
    }
}
