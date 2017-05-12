package vn.vnpay.sms.client;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

public class SmsGatewayProps
        extends PropertiesConfiguration {


    public static final String SMPP_DEDUPLICATION = ".smpp.deduplication";
    public static final String QUEUE_NAME = ".queue-name";
    private static Log logger = LogFactory.getLog(SmsGatewayProps.class);

    public static final String SMPP_GATEWAY_ID = "smpp.gateway-id";

    public static final String SMPP_HOST_POSTFIX = ".smpp.host";
    public static final String SMPP_PORT_POSTFIX = ".smpp.port";
    public static final String SMPP_USER_POSTFIX = ".smpp.user";
    public static final String SMPP_PASSWORD_POSTFIX = ".smpp.password";
    public static final String SMPP_SYSTEM_TYPE_POSTFIX = ".smpp.system-type";
    public static final String SMPP_BIND_TYPE_POSTFIX = ".smpp.bind-type";
    public static final String SMPP_TON_POSTFIX = ".smpp.ton";
    public static final String SMPP_NPI_POSTFIX = ".smpp.npi";
    public static final String SMPP_ADDRESS_RANGE_POSTFIX = ".smpp.address-range";
    public static final String SMPP_CONCATENATED_SUPPORT = ".smpp.concatenated-support";
    public static final String SMPP_MESSAGE_PAYLOAD = ".smpp.message_payload";
    public static final String SMPP_MAX_THROTTLLING = ".smpp.max-throttling";
    public static final String SMPP_RESEND_MAX_THROTTLLING = ".smpp.resend-max-throttling";
    public static final String SMPP_ALIVE_INTERVAL = "smpp.wait-alive-interval";


    /**
     * Paths to search for the API properties file. These should always end in
     * the '/' character except for the last entry which should be a blank
     * string.
     */
    private static final String[] SEARCH_PATH = {
            "/", "../","/conf/"
    };

    /**
     * Name of the resource to load properties from.
     */
    private static final String PROPS_RESOURCE = "gateway.properties";

    /**
     * The singleton instance of the engine configuration.
     */
    private static SmsGatewayProps instance = null;

    /**
     * The file the properties got loaded from (including path info).
     */
    private String propsFile = PROPS_RESOURCE;


    /**
     * Load the engine properties. This method searches for the properties resource
     * in a number of places and uses the <code>Class.getResourceAsStream</code>
     * and the <code>Properties.load</code> method to load them.
     */
    private void loadProperties() {
        try {

            File is = null;
            Class c = getClass();

            for (int i = 0; i < SEARCH_PATH.length && is == null; i++) {
                propsFile = SEARCH_PATH[i] + PROPS_RESOURCE;
                is = new File(c.getResource(propsFile).getFile());
            }

            if (is != null) {
                //System.out.println(is.getAbsolutePath());
                loadProperties(is);
            } else {
                logger.error("Could not find API properties to load");
            }

        } catch (IOException ioe) {
            logger.error("Could not load engine properties", ioe);
        }
    }


    /**
     * Load the properties from a stream. This method actually just calls
     * <code>Properties.load</code> but includes some useful debugging output
     * too.
     *
     * @param is File
     * @throws java.io.IOException
     */
    private void loadProperties(File is)
            throws IOException {
        try {
            instance.setFile(is);
            instance.setReloadingStrategy(new FileChangedReloadingStrategy());
            instance.setAutoSave(true);

            instance.load();

            //logger.info("Loaded engine properties from " + propsFile);
        } catch (Exception e) {
            logger.error("Failed to load properties from " + propsFile);
        }
    }


    /**
     * Get the singleton <code>PropsConfiguration</code> instance.
     *
     * @return PropsConfiguration
     */
    public static SmsGatewayProps getInstance() {
        if (instance == null) {
            instance = new SmsGatewayProps();
            instance.loadProperties();
        }
        return instance;
    }
}
