package rest.htpp.com.webprint.printer;

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

import fi.iki.elonen.NanoHTTPD;

import static rest.htpp.com.webprint.constants.PrintConstants.BOLD;
import static rest.htpp.com.webprint.constants.PrintConstants.CONTENT;
import static rest.htpp.com.webprint.constants.PrintConstants.CONTENT_TYPE;
import static rest.htpp.com.webprint.constants.PrintConstants.FONT_SIZE;
import static rest.htpp.com.webprint.constants.PrintConstants.HEIGHT;
import static rest.htpp.com.webprint.constants.PrintConstants.ITALIC;
import static rest.htpp.com.webprint.constants.PrintConstants.OFFSET;
import static rest.htpp.com.webprint.constants.PrintConstants.POSITION;
import static rest.htpp.com.webprint.constants.PrintConstants.PRINT_PIC_TAG;
import static rest.htpp.com.webprint.constants.PrintConstants.PRINT_TEXT_DATA_TAG;
import static rest.htpp.com.webprint.constants.PrintConstants.PRINT_TEXT_FONT_NAME_TAG;
import static rest.htpp.com.webprint.constants.PrintConstants.PRINT_TEXT_FONT_SIZE_TAG;
import static rest.htpp.com.webprint.constants.PrintConstants.PRINT_TEXT_TAG;


public class Printer {

    private static final String TAG = "Printer";
    private static final PrinterListener printerCallback = new PrinterListener();
    private static JSONObject printRequest;
    private static JSONArray printArray;
    private static JSONObject printJson;


    public static Map<Integer, String> print(final Document xml) {
        final Map<Integer, String> printStatus = new HashMap<>();
        final Element textElement = (Element) xml.getElementsByTagName(PRINT_TEXT_TAG).item(0);
        final Element picElement = (Element) xml.getElementsByTagName(PRINT_PIC_TAG).item(0);
        try {
            // Print text
            if (null != textElement) {
                final String text = getTextContentOfXmlElement(textElement, PRINT_TEXT_DATA_TAG);
                final String fontName =
                        getTextContentOfXmlElement(textElement, PRINT_TEXT_FONT_NAME_TAG);
                final String fontSize =
                        getTextContentOfXmlElement(textElement, PRINT_TEXT_FONT_SIZE_TAG);
                if (null == text || null == fontName) {
                    throw new JSONException("XML file does not contain 'textInfo' tags");
                }
                printText(text, fontName, Integer.parseInt(fontSize));
                NanoHTTPD.Response.Status status = NanoHTTPD.Response.Status.OK;
                printStatus.put(status.getRequestStatus(), status.getDescription());
            }
            //Print image
            if (null != picElement) {
                final String picBytes = picElement.getTextContent();
                if (null == picBytes) {
                    throw new JSONException("XML file does not contain 'prnraw64' tags");
                }
                Thread.sleep(1000);
                printPic(Base64.decode(picBytes, Base64.DEFAULT));
                if (printStatus.isEmpty()) {
                    NanoHTTPD.Response.Status status = NanoHTTPD.Response.Status.OK;
                    printStatus.put(status.getRequestStatus(), status.getDescription());
                }
            }
            if (printStatus.isEmpty()) {
                printStatus.put(NanoHTTPD.Response.Status.BAD_REQUEST.getRequestStatus(),
                        "XML file does not contain necessary tags");
            }
        } catch (JSONException e) {
            printStatus.put(NanoHTTPD.Response.Status.BAD_REQUEST.getRequestStatus(),
                    "Malformed or empty XML file");
        } catch (Exception e) {
            printStatus.put(NanoHTTPD.Response.Status.BAD_REQUEST.getRequestStatus(),
                    "Print failed");
        }
        return printStatus;
    }

    //Helper Methods

    /**
     * Get text content of xml element.
     *
     * @return text content
     */
    private static String getTextContentOfXmlElement(final Element xmlElement,
                                                     final String tagName) {
        return xmlElement.getElementsByTagName(tagName).item(0).getTextContent();
    }

    /**
     * Pint text content
     */
    private static void printText(final String text, final String fontName, final int size)
            throws Exception {
        Log.i(TAG, "Print text");
        printRequest = new JSONObject();
        printArray = new JSONArray();
        printJson = new JSONObject();
        setPrinterSettingsForText(text, fontName, size);
        printArray.put(printRequest);
        printJson.put("spos", printArray);
        ServiceManager.getInstence().getPrinter().
                print(printJson.toString(), null, printerCallback);
    }

    private static void setPrinterSettingsForText(String text, String fontName, int size)
            throws Exception {
        printRequest.put(CONTENT_TYPE, "txt");
        printRequest.put(CONTENT, text);
        printRequest.put(FONT_SIZE, size);
        printRequest.put(POSITION, "left");
        printRequest.put(OFFSET, "0");
        printRequest.put(BOLD, 0);
        printRequest.put(ITALIC, 0);
        printRequest.put(HEIGHT, "-1");
        ServiceManager.getInstence().getPrinter().setPrintFont(fontName);
    }

    /**
     * Print picture content
     */
    private static void printPic(final byte[] decodedString) throws Exception {
        Log.i(TAG, "Print picture");
        printRequest = new JSONObject();
        printArray = new JSONArray();
        printJson = new JSONObject();
        printRequest.put(CONTENT_TYPE, "jpg");
        printRequest.put(POSITION, "center");
        printArray.put(printRequest);
        printJson.put("spos", printArray);
        // Set at the bottom of the empty 3 rows
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        Bitmap[] bitmaps = new Bitmap[]{decodedByte};
        ServiceManager.getInstence().getPrinter().setPrintGray(1000);
        ServiceManager.getInstence().getPrinter().print(printJson.toString(), bitmaps, printerCallback);
    }

    static class PrinterListener implements OnPrinterListener {

        static final String TAG = "PrinterListener";

        @Override
        public void onStart() {
            Log.i(TAG, "Start of print");
        }

        @Override
        public void onFinish() {
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
