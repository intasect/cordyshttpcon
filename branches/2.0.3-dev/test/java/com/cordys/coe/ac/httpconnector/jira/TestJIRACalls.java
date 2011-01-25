package com.cordys.coe.ac.httpconnector.jira;

import com.cordys.coe.ac.httpconnector.config.IMethodConfiguration;
import com.cordys.coe.ac.httpconnector.config.IServerConnection;
import com.cordys.coe.ac.httpconnector.config.IXSLTStore;
import com.cordys.coe.ac.httpconnector.config.MethodConfigurationFactory;
import com.cordys.coe.ac.httpconnector.config.ServerConnectionFactory;
import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.cordys.coe.ac.httpconnector.execution.MethodExecutor;
import com.cordys.coe.util.FileUtils;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.config.LoggerConfigurator;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XSLT;

/**
 * This class tests the REST access to the Jira USER browser.
 *
 * @author  pgussow
 */
public class TestJIRACalls
{
    /**
     * Holds the XML document.
     */
    private static Document s_doc = new Document();
    /**
     * Holds the logger to use.
     */
    private CordysLogger LOG = CordysLogger.getCordysLogger(TestJIRACalls.class);
    /**
     * Holds the method configuration.
     */
    private IMethodConfiguration m_mc;
    /**
     * Holds teh request to execute.
     */
    private int m_request;
    /**
     * Holds the server connection to use.
     */
    private IServerConnection m_sc;

    /**
     * Creates a new TestUserBrowser object.
     */
    public TestJIRACalls()
    {
    }

    /**
     * Main method.
     *
     * @param  saArguments  Commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            TestJIRACalls tub = new TestJIRACalls();
//            tub.setup("CreateUser");
            tub.setup("UserBrowser");

            tub.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This method actually executes the request.
     *
     * @throws  Exception  In case of any exceptions.
     */
    public void execute()
                 throws Exception
    {
        int response = MethodExecutor.sendRequest(m_request, m_sc, m_mc);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Response:\n" + Node.writeToString(response, true));
        }
    }

    /**
     * This method sets up the class and loads all XMLs.
     *
     * @param   method  The name of the method to test
     *
     * @throws  Exception
     */
    public void setup(String method)
               throws Exception
    {
        LoggerConfigurator.initLogger("../InboudEmailConnector_main/test/Log4jConfiguration.xml");

        // Load the server connection.
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Loading server connection information and opening the connection");
        }

        int connection = s_doc.parseString(FileUtils.readTextResourceContents("connections.xml",
                                                                              getClass()));
        m_sc = ServerConnectionFactory.createServerConnections(connection)[0];
        m_sc.open();

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Loading method configuration");
        }

        int methodImpl = s_doc.parseString(FileUtils.readTextResourceContents(method + "-impl.xml",
                                                                              getClass()));
        m_mc = MethodConfigurationFactory.createMethodConfiguration(new LocalHTTPConnector(),
                                                                    methodImpl);

        m_request = s_doc.parseString(FileUtils.readTextResourceContents(method + "-request.xml",
                                                                         getClass()));
    }

    /**
     * Local class to simulate the Http connector.
     *
     * @author  pgussow
     */
    public class LocalHTTPConnector
        implements IXSLTStore
    {
        /**
         * @see  com.cordys.coe.ac.httpconnector.config.IXSLTStore#loadXslt(int)
         */
        @Override public XSLT loadXslt(int node)
                                throws ConnectorException
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Calling the load XSLT method");
            }

            return null;
        }
    }
}
