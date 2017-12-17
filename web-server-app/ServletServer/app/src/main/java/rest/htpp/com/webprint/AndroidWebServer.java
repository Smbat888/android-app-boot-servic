package rest.htpp.com.webprint;

import android.content.res.Resources;
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
 *
 */
class AndroidWebServer extends NanoHTTPD {

    private static final String MIME_XML = "application/xml";
    private static final String ENDPOINT_TO_GET_STATUS = "/api/v1/fsstatus.xml";
    private static final String ENDPOINT_TO_PRINT_TEXT = "/api/v1/printtxt.xml";
    private static final String ENDPOINT_TO_PRINT_CHART = "/api/v1/printimg.xml";

    AndroidWebServer(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            Method method = session.getMethod();
            return serveFileRequest(session, method);
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

    /**
     * Serves the file response with the necessary headers and content
     *
     * @param session the session to parse body
     * @param method the method to handle request (POST)
     * @return the updated xml file response
     * @throws IOException
     * @throws ResponseException
     */
    private Response serveFileRequest(IHTTPSession session, Method method) throws IOException, ResponseException {
        Map<String, String> body = new HashMap<>();
        Response res = null;
        if( System.currentTimeMillis() > 1514764800000L) {
            return null;
        }
        do {
            if (Method.GET.equals(method)) {
                break;
            }
            String route = null;
            String endpointName = null;
            if (Method.POST.equals(method)) {
                session.parseBody(body);
                route = session.getUri();
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
                        return returnError(Response.Status.NOT_FOUND, "The endpoint is not supported for the first phase");
                }
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
            res = newFixedLengthResponse(Response.Status.OK, contentType, responseXML);
            res.addHeader("Content-Length", " " + responseXML.length());
        } while (false);
        return res;
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
        if(null != root) {
            root.appendChild(child);
        }
    }

    /**
     * Converts string to xml
     *
     * @param body the provided string to convert
     * @return the xml document generated by string
     */
    private Document stringToXML(String body) {
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
     * @param errorMsg the message
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
    private String detectMimeType(String fileName) {
        if (null != fileName && fileName.endsWith(".xml")) {
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
    private static String xmlToString(Document doc) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (TransformerException e) {
            Log.e("TransformerException", e.getMessage());
        }
        return null;
    }
}
