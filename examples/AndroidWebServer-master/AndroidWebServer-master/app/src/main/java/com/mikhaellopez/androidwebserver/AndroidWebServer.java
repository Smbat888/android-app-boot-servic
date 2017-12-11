package com.mikhaellopez.androidwebserver;

import android.content.res.Resources;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by Mikhael LOPEZ on 14/12/2015.
 */
public class AndroidWebServer extends NanoHTTPD {

    private static final String MIME_XML = "application/xml";
    private static final String ENDPOINT_TO_GET_STATUS = "api/v1/fsstatus.xml";
    private static final String ENDPOINT_TO_PRINT_TEXT = "api/v1/printtxt.xml";
    private static final String ENDPOINT_TO_PRINT_CHART = "api/v1/printimg.xml";

    public AndroidWebServer(int port) {
        super(port);
    }

    public AndroidWebServer(String hostname, int port) {
        super(hostname, port);
    }

    @Override
    public Response serve(IHTTPSession session) {
//        String msg = "<html><body><h1>Hello server</h1>\n";
//        Map<String, String> parms = session.getParms();
//        if (parms.get("username") == null) {
//            msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
//        } else {
//            msg += "<p>Hello, " + parms.get("username") + "!</p>";
//        }
//        return newFixedLengthResponse( msg + "</body></html>\n" );
        try {
            Method method = session.getMethod();
            String uri = session.getUri();
            Map<String, String> parms = session.getParms();
            return serveClock(session, uri, method, parms);

        } catch (IOException ioe) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
        } catch (ResponseException re) {
            return newFixedLengthResponse(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
        } catch (Resources.NotFoundException nfe) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found");
        } catch (Exception ex) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_HTML, "<html><body><h1>Error</h1>" + ex.toString() + "</body></html>");
        }
    }

    private Response serveClock(IHTTPSession session, String uri, Method method, Map<String, String> parms)  throws IOException, ResponseException {
        String responseString = "";
        Response res = null;
        Map<String, String> files = new HashMap<>();
        session.parseBody(files);
        do {
            if(Method.GET.equals(method)) {
                responseString = handleGet(session, parms);
                break;
            }
            String route = null;
            String endpointName = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(session.getInputStream()));
            int contentLength = 0;
            if(Method.POST.equals(method)) {
                String line;
                while (!TextUtils.isEmpty(line = reader.readLine())) {
                    final String contentHeader = "Content-Length: ";
                    if (line.startsWith(contentHeader)) {
                        contentLength = Integer.parseInt(line.substring(contentHeader.length()));
                    }
                    int start = uri.indexOf('/') + 1;
                    int end = uri.indexOf(' ', start);
                    route = uri.substring(start, end);
                    switch (route) {
                        case ENDPOINT_TO_GET_STATUS:
                            endpointName = "Get Status";
                            break;
                        case ENDPOINT_TO_PRINT_TEXT:
                            endpointName = "Print Text";
                            break;
                        case ENDPOINT_TO_PRINT_CHART:
                            endpointName = "Print Chart";
                            break;
                        default:
//                        writeClientError(response,   NOT_FOUND_CODE, "The endpoint is not supported for the first phase");
                    }
                    responseString = handlePost(session);
                    break;
                }
            }

//            throw new Resources.NotFoundException();

            String contentType = "";
            contentType = detectMimeType(route);
            if (null == contentType) {
//                writeClientError(response, BAD_CONTENT_TYPE_CODE, "unsupported content type");
                return null;
            }
            StringBuilder body = readFileFromRequest(reader, contentLength);

            Document xml = stringToXML(body);
            updateXmlFile(xml,endpointName);
            final String responseXML = xmlToString(xml);
            if (null == xml || 0 == responseXML.length()) {
//                writeClientError(response, BAD_REQUEST_CODE, "Malformed or empty XML file");
                return null;
            }
            res = newChunkedResponse(Response.Status.OK, contentType, new FileInputStream(responseXML));
//            res.addHeader("Accept-Ranges", "bytes");
            res.addHeader("Content-Length", " " + responseXML.length());

        } while(false);

        return res;
    }

    private String handleGet(IHTTPSession session, Map<String, String> parms) {
        return "{'name':'status', 'value':''}";
    }

    private String handlePost(IHTTPSession session) throws IOException, ResponseException {
        Map<String, String> files = new HashMap<String, String>();
        session.parseBody(files);

        return files.get("postData");
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
            Log.e("TransformerException", e.getMessage());
        }
        return null;
    }
}
