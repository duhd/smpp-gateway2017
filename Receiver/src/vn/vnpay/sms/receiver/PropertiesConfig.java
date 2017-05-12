package vn.vnpay.sms.receiver;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

public class PropertiesConfig
        extends PropertiesConfiguration {

    public static final String LISTEN_PORT = "listen-port";
    public static final String RECEIVER_TIMEOUT = "receive-timeout";
    public static final String ACCEPT_TIMEOUT = "accept-timeout";
    public static final String QUEUE_NAME = "queue-name";

    private static Log logger = LogFactory.getLog(PropertiesConfig.class);

    /**
     * Paths to search for the API properties file. These should always end in
     * the '/' character except for the last entry which should be PropertiesConfig blank
     * string.
     */
    private static final String[] SEARCH_PATH = {
            "/", "../","/conf/"
    };

    /**
     * Name of the resource to load properties from.
     */
    private static final String PROPS_RESOURCE = "config.properties";

    /**
     * The singleton instance of the engine configuration.
     */
    private static PropertiesConfig instance = null;

    /**
     * The file the properties got loaded from (including path info).
     */
    private String propsFile = PROPS_RESOURCE;


    /**
     * Load the engine properties. This method searches for the properties resource
     * in PropertiesConfig number of places and uses the <code>Class.getResourceAsStream</code>
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
            logger.error("Could not load engine properties:\n" + ioe);
        }
    }


    /**
     * Load the properties from PropertiesConfig stream. This method actually just calls
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
    public static PropertiesConfig getInstance() {
        if (instance == null) {
            instance = new PropertiesConfig();
            instance.loadProperties();
        }
        return instance;
    }
}
