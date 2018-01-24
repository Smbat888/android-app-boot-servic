package rest.htpp.com.webprint.constants;

public final class ServerConstants {

    public static final String NEVER_KILLED_SERVER = "NEVER_KILLED_SERVER";
    public static final String TOAST_TEXT = "Service is connected";
    public static final int SERVER_PORT = 8888;

    public static final String MIME_XML = "application/xml";

    // Endpoints
    public static final String ENDPOINT_TO_GET_STATUS = "/api/v1/fsstatus.xml";
    public static final String ENDPOINT_TO_PRINT_TEXT = "/api/v1/printtxt.xml";
    public static final String ENDPOINT_TO_PRINT_CHART = "/api/v1/printimg.xml";
    public static final String ENDPOINT_TO_PRINT_JOBS = "/api/v1/printers/printjobs.xml";
    public static final String ENDPOINT_TO_GET_FISCAL_STATUS = "/api/v1/fiscstor/fsstatus.xml";

    private ServerConstants() {
    }
}
