package rest.taron.com.myapplication;

import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

class HttpResponseThread extends Thread {

    private static final String TAG = "WebPrintServer";
    private static final String ENDPOINT_TO_GET_STATUS = "api/v1/fsstatus.xml";
    private static final String ENDPOINT_TO_PRINT_TEXT = "api/v1/printtxt.xml";
    private static final String ENDPOINT_TO_PRINT_CHART = "api/v1/printimg.xml";

    private static final String BAD_REQUEST_CODE = "400";
    private static final String NOT_FOUND_CODE = "404";
    private static final String BAD_CONTENT_TYPE_CODE = "415";

    /**
     * The Socket that we listen to.
     */
    Socket mSocket;

    /**
     * For loading files to serve.
     */
    private final AssetManager mAssets;

    HttpResponseThread(Socket socket, AssetManager assets) {
        mSocket = socket;
        mAssets = assets;
    }

    @Override
    public void run() {
        try {
            handle(mSocket);
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Respond to a request from a client.
     *
     * @param socket The client socket.
     * @throws IOException
     */
    private void handle(final Socket socket) throws IOException {
        BufferedReader reader = null;
        PrintWriter response = null;
        String route = null;
        String line;
        String endpointName = null;
        int contentLength = 0;
        try {
            response = new PrintWriter(socket.getOutputStream());
            // Read HTTP headers and parse out the route.
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!TextUtils.isEmpty(line = reader.readLine())) {
                final String contentHeader = "Content-Length: ";
                if (line.startsWith(contentHeader)) {
                    contentLength = Integer.parseInt(line.substring(contentHeader.length()));
                } else if (line.startsWith("POST /")) {
                    int start = line.indexOf('/') + 1;
                    int end = line.indexOf(' ', start);
                    route = line.substring(start, end);
                    switch (route) {
                        case ENDPOINT_TO_GET_STATUS:
                            endpointName = "Get Status";
                            break;
                        case ENDPOINT_TO_PRINT_TEXT:
                            endpointName = "Print Text";
                            break;
                        case ENDPOINT_TO_PRINT_CHART:
                            endpointName =  "Print Chart";
                            break;
                        default:
                            writeClientError(response,   NOT_FOUND_CODE, "The endpoint is not supported for the first phase");
                            return;
                    }
                }
            }
            String contentType = detectMimeType(route);
            if (null == contentType) {
                writeClientError(response, BAD_CONTENT_TYPE_CODE, "unsupported content type");
                return;
            }
            StringBuilder body = readFileFromRequest(reader, contentLength);

            Document xml = stringToXML(body);
            final String responseXML = xmlToString(xml);
            if (null == xml || 0 == responseXML.length()) {
                writeClientError(response, BAD_REQUEST_CODE, "Malformed or empty XML file");
                return;
            }
            updateXmlFile(xml,endpointName);
            // Output stream that we send the response to
            response.print("HTTP/1.0 200 OK" + "\r\n");
            response.print("Content type: " + contentType + "\r\n");
            response.print("Content length: " + responseXML.length() + "\r\n");
            response.print("\r\n");
            response.write(responseXML);
            response.flush();
        } finally {
            if (null != response) {
                response.close();
            }
            if (null != reader) {
                reader.close();
            }
        }
    }

    private void updateXmlFile(final Document xml, final String msg) {
        Element child = xml.createElement("child");
        child.setAttribute("name", "updatedByServer");
        child.setTextContent(msg);
        Element root = xml.getDocumentElement();
        root.appendChild(child);
    }

    private Document stringToXML(StringBuilder body) {
        Document xml = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            xml = dBuilder.parse(new InputSource(new StringReader(body.toString())));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return null;
        }
        return xml;
    }

    @NonNull
    private StringBuilder readFileFromRequest(BufferedReader reader, int contentLength) throws IOException {
        StringBuilder body = new StringBuilder();
        int c = 0;
        for (int i = 0; i < contentLength; i++) {
            c = reader.read();
            body.append((char) c);
        }
        return body;
    }

    /**
     * Writes a server error response (HTTP/1.0 500) to the given output stream.
     *
     * @param output The output stream.
     */
    private void writeServerError(final PrintStream output) {
        output.println("HTTP/1.0 500 Internal Server Error");
        output.flush();
    }

    /**
     * Writes a bad request error response (HTTP/1.0 400-499) to the given output stream.
     *
     * @param output The output stream.
     */
    private void writeClientError(final PrintWriter output, final String errorCode, final String errorMsg) {
        output.print("HTTP/1.0 " + errorCode + " " + errorMsg.toUpperCase() + "\r\n");
        output.flush();
    }

    /**
     * Detects the MIME type from the {@code fileName}.
     *
     * @param fileName The name of the file.
     * @return A MIME type.
     */
    private String detectMimeType(String fileName) {
        if (fileName.endsWith(".xml")) {
            return "application/xml";
        }
        return null;
    }

    private static String xmlToString(Document doc) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            // below code to remove XML declaration
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (TransformerException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }
}