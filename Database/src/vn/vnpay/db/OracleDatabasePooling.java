package vn.vnpay.db;

import oracle.ucp.UniversalConnectionPoolAdapter;
import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class OracleDatabasePooling {
    static Log logger = LogFactory.getLog(OracleDatabasePooling.class);
    static DatabaseProps dp = DatabaseProps.getInstance();

    static final String USER = "ora.database.user";
    static final String PASSWORD = "ora.database.password";
    static final String URL = "ora.database.url";
    static final String INITIAL_POOL_SIZE = "ora.database.Initial-Pool-Size";
    static final String MAX_POOL_SIZE = "ora.database.Max-Pool-Size";
    static final String MIN_POOL_SIZE = "ora.database.Min-Pool-Size";


    static final String QUEUE_NAME = "ora.queue-name";
    private PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
    private UniversalConnectionPoolManager mgr = UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();

    private static Connection conn = null;
    protected static String queueName = "";

    /**
     * DatabasePooling
     *
     * @throws java.sql.SQLException
     */
    public OracleDatabasePooling()
            throws SQLException, UniversalConnectionPoolException {
        logger.info("Oracle Initializes the connection pooling");

        //https://docs.oracle.com/cd/E11882_01/java.112/e12265/optimize.htm#CFHDGJGG
        pds.setConnectionPoolName("JDBC_UCP");
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        pds.setURL(dp.getString(URL));
        pds.setUser(dp.getString(USER));
        pds.setPassword(dp.getString(PASSWORD));
        pds.setInitialPoolSize(dp.getInt(INITIAL_POOL_SIZE));
        pds.setMaxPoolSize(dp.getInt(MAX_POOL_SIZE));
        pds.setMinPoolSize(dp.getInt(MIN_POOL_SIZE));
        pds.setMaxStatements(100); //Enabling Statement Caching
        pds.setMaxConnectionReuseTime(300); //Setting the Maximum Connection Reuse Time
        pds.setMaxConnectionReuseCount(100); //Setting the Maximum Connection Reuse Count
        pds.setTimeToLiveConnectionTimeout(18000); //Setting the Time-To-Live Connection Timeout
        pds.setConnectionWaitTimeout(10); //Setting the Connection Wait Timeout
        pds.setInactiveConnectionTimeout(60);//Setting the Inactive Connection Timeout


        mgr.createConnectionPool((UniversalConnectionPoolAdapter) pds);
        mgr.startConnectionPool("JDBC_UCP");
    }

    public String getQueueName() {
        return dp.getString(QUEUE_NAME);
    }

    /**
     * getConnection
     *
     * @return Connection
     * @throws java.sql.SQLException
     */
    public Connection getConnection()
            throws SQLException {
        try {
            conn = pds.getConnection();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            // Turn off auto-commit for better performance
            assert conn != null;
            conn.setAutoCommit(false);
        }
        if (conn == null) {
            logger.error("Maximum number of connection in pool exceeded");
        }
        return conn;
    }


    /**
     * close
     */
    public void close() {
        int totalConnsCount = pds.getStatistics().getTotalConnectionsCount();

        logger.debug("Close the connection pooling: " + totalConnsCount);
        try {
            conn.close();
            conn = null;
            try {
                mgr.destroyConnectionPool("JDBC_UCP");
            } catch (UniversalConnectionPoolException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
