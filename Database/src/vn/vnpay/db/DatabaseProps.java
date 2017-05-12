package vn.vnpay.db;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

public class DatabaseProps
        extends PropertiesConfiguration {
    static Log logger = LogFactory.getLog(DatabaseProps.class);

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
    private static final String PROPS_RESOURCE = "database.properties";

    /**
     * The singleton instance of the engine configuration.
     */
    private static DatabaseProps instance = null;

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
            logger.error("Failed to load properties from " + propsFile, e);
        }
    }


    /**
     * Get the singleton <code>PropsConfiguration</code> instance.
     *
     * @return DatabaseProps
     */
    public static DatabaseProps getInstance() {
        if (instance == null) {
            instance = new DatabaseProps();
            instance.loadProperties();
        }

        return instance;
    }
}
