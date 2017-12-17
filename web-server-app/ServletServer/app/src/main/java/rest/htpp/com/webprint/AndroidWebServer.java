package rest.htpp.com.webprint;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
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
 * The android web server class to serve http POST requests with specified endpoints
 */
class AndroidWebServer extends NanoHTTPD {

    private static final String ENDPOINT_TO_GET_STATUS = "/api/v1/fsstatus.xml";
    private static final String ENDPOINT_TO_PRINT_TEXT = "/api/v1/printtxt.xml";
    private static final String ENDPOINT_TO_PRINT_CHART = "/api/v1/printimg.xml";
    private static final String MIME_XML = "application/xml";
    private static final String TAG = "TransformerException";

    AndroidWebServer(int port) {
        super(port);
    }

    @Override
    public Response serve(final IHTTPSession session) {
        try {
            final Method method = session.getMethod();
            return serveFileRequest(session, method);
        } catch (IOException ioe) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT,
                    "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
        } catch (ResponseException re) {
            return newFixedLengthResponse(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
        } catch (Resources.NotFoundException nfe) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found");
        } catch (Exception ex) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_HTML,
                    "<html><body><h1>Error</h1>" + ex.toString() + "</body></html>");
        }
    }

    //Helper Methods

    /**
     * Serves the file response with the necessary headers and content
     *
     * @param session the session to parse body
     * @param method  the method to handle request (POST)
     * @return the updated xml file response
     * @throws IOException
     * @throws ResponseException
     */
    private Response serveFileRequest(final IHTTPSession session, final Method method)
            throws IOException, ResponseException {
        //TODO REMOVE
        if (System.currentTimeMillis() > 1514764800000L) {
            return null;
        }
        if (Method.POST.equals(method)) {
            return collectResponse(session);
        }
        return null;
    }

    /**
     * Collects http response for given request
     * @param session  the session to parse body
     * @return response with corresponding xml file  and headers
     * @throws IOException
     * @throws ResponseException
     */
    private Response collectResponse(final IHTTPSession session)
            throws IOException, ResponseException {
        final Map<String, String> body = new HashMap<>();
        String route = null;
        String endpointName = null;
        session.parseBody(body);
        route = session.getUri();
        endpointName = getEndpintName(route);
        if (null == endpointName) {
            return returnError(Response.Status.NOT_FOUND,
                    "The endpoint is not supported for the first phase");
        }
        final String contentType = detectMimeType(route);
        if (null == contentType) {
            return returnError(Response.Status.BAD_REQUEST, "Unsupported content type");
        }
        Document xml = stringToXML(body.get("postData"));
        updateXmlFile(xml, endpointName);
        final String responseXML = xmlToString(xml);
        if (null == xml || null == responseXML || 0 == responseXML.length()) {
            return returnError(Response.Status.BAD_REQUEST, "Malformed or empty XML file");
        }
        Response response = newFixedLengthResponse(Response.Status.OK, contentType, responseXML);
        response.addHeader("Content-Length", " " + responseXML.getBytes().length);
        return response;
    }

    /**
     * Gets corresponding string value for given endpoint.
     *
     * @param route to check endpoint name
     * @return endpoint name
     */
    private String getEndpintName(final String route) {
        switch (route) {
            case ENDPOINT_TO_GET_STATUS:
                return "Get Status";
            case ENDPOINT_TO_PRINT_TEXT:
                return "Print Text";
            case ENDPOINT_TO_PRINT_CHART:
                return "Print Chart";
            default:
                return null;
        }
    }

    /**
     * Updates xml document to append simple child element with custom value
     *
     * @param xml the provided document to update
     * @param msg the message to append to created child element
     */
    private void updateXmlFile(final Document xml, final String msg) {
        if (null == xml) {
            return;
        }
        final Element child = xml.createElement("child");
        child.setAttribute("name", "updatedByServer");
        child.setTextContent(msg);
        final Element root = xml.getDocumentElement();
        if (null != root) {
            root.appendChild(child);
        }
    }

    /**
     * Converts string to xml
     *
     * @param body the provided string to convert
     * @return the xml document generated by string
     */
    private Document stringToXML(final String body) {
        Document xml;
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            xml = dBuilder.parse(new InputSource(new StringReader(body)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return null;
        }
        return xml;
    }

    /**
     * Writes a bad request error response (HTTP/1.0 400-499) to the given output stream.
     *
     * @param errorCode the code
     * @param errorMsg  the message
     */
    private Response returnError(final Response.IStatus errorCode, final String errorMsg) {
        return newFixedLengthResponse(errorCode, MIME_PLAINTEXT, errorMsg);
    }

    /**
     * Detects the MIME type from the {@code fileName}.
     *
     * @param fileName The name of the file.
     * @return A MIME type.
     */
    private String detectMimeType(final String fileName) {
        if (!TextUtils.isEmpty(fileName) && fileName.endsWith(".xml")) {
            return MIME_XML;
        }
        return null;
    }

    /**
     * Converts xml to string by using Android native TransformerFactory
     *
     * @param doc provided document to convert
     * @return the result string
     */
    private static String xmlToString(final Document doc) {
        final TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            final StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (TransformerException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }
}
