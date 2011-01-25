
/**
 * Copyright 2006 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys HTTP Connector. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.cordys.coe.ac.httpconnector;

import com.cordys.coe.ac.httpconnector.config.IXSLTStore;
import com.cordys.coe.ac.httpconnector.config.IMethodConfiguration;
import com.cordys.coe.ac.httpconnector.config.IServerConnection;
import com.cordys.coe.ac.httpconnector.config.MethodConfigurationFactory;
import com.cordys.coe.ac.httpconnector.config.ServerConnectionFactory;
import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.cordys.coe.ac.httpconnector.exception.ConnectorExceptionMessages;
import com.cordys.coe.ac.httpconnector.exception.HandlerException;
import com.cordys.coe.coelib.LibraryVersion;
import com.cordys.coe.util.soap.SOAPWrapper;
import com.cordys.coe.util.xmlstore.XMLStoreWrapper;

import com.eibus.connector.nom.Connector;

import com.eibus.localization.ILocalizableString;

import com.eibus.management.IManagedComponent;

import com.eibus.soap.ApplicationConnector;
import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;
import com.eibus.soap.Processor;
import com.eibus.soap.SOAPTransaction;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XSLT;

import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Main application connector class for HttpConnector.
 */
public class HttpConnector extends ApplicationConnector
    implements IXSLTStore
{
    /**
     * Contains the logger instance.
     */
    private static CordysLogger LOG = CordysLogger.getCordysLogger(HttpConnector.class);
    /**
     * Holds the name of the connector.
     */
    private static final String CONNECTOR_NAME = "HttpConnector Connector";
    /**
     * Holds the configuration object for this connector.
     */
    private HttpConfiguration m_configuration;
    /**
     * Contains the server connection objects mapped by their ID's.
     */
    private Map<String, IServerConnection> m_connectionMap = new HashMap<String, IServerConnection>();
    /**
     * Holds the connector to use for sending messages to Cordys.
     */
    private Connector m_connector;
    /**
     * Contains cached versions of the method configuration objects mapped by the method DN.
     */
    private ConcurrentHashMap<String, IMethodConfiguration> m_methodCache = new ConcurrentHashMap<String, IMethodConfiguration>();

    /**
     * This method gets called when the processor is being stopped.
     *
     * @param  pProcessor  The processor that is being stopped.
     */
    @Override public void close(Processor pProcessor)
    {
        if (LOG.isInfoEnabled())
        {
            LOG.info(Messages.CONNECTOR_STOPPED);
        }
    }

    /**
     * This method creates the transaction that will handle the requests.
     *
     * @param   stTransaction  The SOAP-transaction containing the message.
     *
     * @return  The newly created transaction.
     */
    @Override public ApplicationTransaction createTransaction(SOAPTransaction stTransaction)
    {
        return new HttpTransaction(this, m_configuration, m_connector);
    }

    /**
     * Returns the configuration object.
     *
     * @return  The configuration object for this application connector.
     */
    public HttpConfiguration getConfigurationObject()
    {
        return m_configuration;
    }

    /**
     * Returns the method configuration information for this request.
     *
     * @param   req  Request body block.
     *
     * @return  Method configuration structure.
     *
     * @throws  ConnectorException  Thrown if the operation failed.
     * @throws  HandlerException    In case the handler throws any exception.
     */
    public IMethodConfiguration getMethodConfig(BodyBlock req)
                                         throws ConnectorException, HandlerException
    {
        // Get the method DN and try to fetch a cached version.
        String methodDn = req.getMethodDefinition().getMethodDN().toString();
        IMethodConfiguration res = m_methodCache.get(methodDn);

        if (res != null)
        {
            // Cached version found.
            return res;
        }

        // No cached version found, so try to create a new one. Only
        // one thread can access this at the time.
        synchronized (this)
        {
            // Try a again, to see if some other thread managed, to
            // fetch the same method.
            if ((res = m_methodCache.get(methodDn)) != null)
            {
                return res;
            }

            // Parse the method configuration and insert the
            // new object in the cache.
            res = MethodConfigurationFactory.createMethodConfiguration(this, req);
            m_methodCache.put(methodDn, res);
        }

        return res;
    }

    /**
     * Returns DN of the SYSTEM user in the current organization.
     *
     * @return  Organizational SYSTEM user DN.
     */
    public String getOrganizationalSystemUser()
    {
        String orgDn = getProcessor().getOrganization();

        return "cn=SYSTEM,cn=organizational users," + orgDn;
    }

    /**
     * Returns the configured server connector from the given ID.
     *
     * @param   id  Server connection ID.
     *
     * @return  Server connection object or <code>null</code> if none was found.
     */
    public IServerConnection getServerConnection(String id)
    {
        return m_connectionMap.get(id);
    }

    /**
     * Loads a file from the XML store with the given path.
     *
     * @param   path  XML store file path.
     *
     * @return  XML file root node.
     *
     * @throws  ConnectorException  Thrown if the operation failed.
     */
    public int loadXmlStoreFile(String path)
                         throws ConnectorException
    {
        // Set the XMLStore SOAP connection information.
        SOAPWrapper swSoap = new SOAPWrapper(m_connector);

        swSoap.setUser(getOrganizationalSystemUser());

        try
        {
            XMLStoreWrapper xmlStoreWrapper = new XMLStoreWrapper(swSoap);
            int configNode;

            // Get the file from XMLStore.
            configNode = xmlStoreWrapper.getXMLObject(path);

            // Find the actual file node from the response.
            if ((configNode != 0) && (Node.getNumChildren(configNode) > 0))
            {
                // Get the response node
                configNode = Find.firstMatch(configNode, "?<tuple><old><>");

                // The check that the response is valid.
                if (configNode == 0)
                {
                    // No it was not.
                    throw new ConnectorException(ConnectorExceptionMessages.INVALID_RESPONSE_RECEIVED_FROM_XMLSTORE);
                }
            }

            // Check if we have a file node.
            if (configNode == 0)
            {
                // No, it probably wasn't found.
                throw new ConnectorException(ConnectorExceptionMessages.CONFIGURATION_FILE_NOT_FOUND_FROM_XMLSTORE_0,
                                             path);
            }

            configNode = Node.unlink(configNode);

            Node.removeAttribute(configNode, "xmlns:SOAP");

            return configNode;
        }
        catch (ConnectorException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ConnectorException(e,
                                         ConnectorExceptionMessages.UNABLE_TO_LOAD_CONFIGURATION_FROM_XMLSTORE);
        }
        finally
        {
            swSoap.freeXMLNodes();
        }
    }

    /**
     * Reads the XSLT from the configuration XML node.
     *
     * @param   node  XSLT configuration node.
     *
     * @return  Parse XSLT object.
     *
     * @throws  ConnectorException  Thrown if the operation failed.
     * 
     * @see IXSLTStore#loadXslt(int)
     */
    public XSLT loadXslt(int node)
                  throws ConnectorException
    {
        String filePath = Node.getAttribute(node, "file");
        String xmlstorePath = Node.getAttribute(node, "xmlstore");

        if ((filePath != null) && (xmlstorePath != null))
        {
            throw new ConnectorException(ConnectorExceptionMessages.BOTH_FILE_AND_XMLSTORE_ATTRIBUTES_CANNOT_BE_SET_FOR_XSLT);
        }

        if (filePath != null)
        {
            return XSLT.parseFromFile(filePath);
        }
        else if (xmlstorePath != null)
        {
            int configNode = 0;

            try
            {
                configNode = loadXmlStoreFile(xmlstorePath);

                return XSLT.parseFromString(Node.writeToString(configNode, false));
            }
            finally
            {
                if (configNode != 0)
                {
                    Node.delete(configNode);
                    configNode = 0;
                }
            }
        }
        else
        {
            int xsltNode = Node.getFirstElement(node);

            if (xsltNode == 0)
            {
                throw new ConnectorException(ConnectorExceptionMessages.EXPECTED_INLINE_XSLT_CONFIGURATION);
            }

            return XSLT.parseFromString(Node.writeToString(xsltNode, false));
        }
    }

    /**
     * This method gets called when the processor is started. It reads the configuration of the
     * processor and creates the connector with the proper parameters. It will also create a client
     * connection to Cordys.
     *
     * @param  pProcessor  The processor that is started.
     */
    @Override public void open(Processor pProcessor)
    {
        // Check the CoELib version.
        try
        {
            LibraryVersion.loadAndCheckLibraryVersionFromResource(this.getClass(), true);
        }
        catch (Exception e)
        {
            LOG.fatal(e, Messages.COELIB_VERSION_MISMATCH);
            throw new IllegalStateException(e.toString());
        }

        try
        {
            if (LOG.isInfoEnabled())
            {
                LOG.info(Messages.CONNECTOR_STARTING);
            }

            // Get the configuration
            m_configuration = new HttpConfiguration(getConfiguration());

            // Open the client connector
            m_connector = Connector.getInstance(CONNECTOR_NAME);

            if (!m_connector.isOpen())
            {
                m_connector.open();
            }

            // Load the server configuration.
            loadConfiguration();

            if (LOG.isInfoEnabled())
            {
                LOG.info(Messages.CONNECTOR_STARTED);
            }
        }
        catch (Exception e)
        {
            LOG.fatal(e, Messages.CONNECTOR_START_EXCEPTION);
            throw new IllegalStateException(e);
        }
    }

    /**
     * This method gets called when the processor is ordered to rest.
     *
     * @param  processor  The processor that is to be in reset state
     */
    @Override public void reset(Processor processor)
    {
        if (LOG.isInfoEnabled())
        {
            LOG.info(Messages.CONNECTOR_RESET);
        }

        // Clear the method cache.
        m_methodCache.clear();
    }

    /**
     * Standard management method. Allows adding custom coumters, alert definitions and problems
     * definitions to this connector. Note that this method is called by the SOAP processor.
     *
     * @return  The JMX managed component created by the super class.
     */
    @Override protected IManagedComponent createManagedComponent()
    {
        IManagedComponent mc = super.createManagedComponent();

        return mc;
    }

    /**
     * Standard management method.
     *
     * @return  JMX type for this application connector.
     */
    @Override protected String getManagedComponentType()
    {
        return "AppConnector";
    }

    /**
     * Standard management method.
     *
     * @return  JMX description for this application connector.
     */
    @Override protected ILocalizableString getManagementDescription()
    {
        return Messages.CONNECTOR_MANAGEMENT_DESCRIPTION;
    }

    /**
     * Standard management method.
     *
     * @return  JMX name for this application connector.
     */
    @Override protected String getManagementName()
    {
        return "HttpConnector";
    }

    /**
     * Loads the connector configuration from the XML Store.
     *
     * @throws  ConnectorException
     */
    private void loadConfiguration()
                            throws ConnectorException
    {
        // Get configuration file name
        String sConfigFileName = m_configuration.getConfigurationFilePath();

        if ((sConfigFileName == null) || sConfigFileName.equals(""))
        {
            throw new ConnectorException(ConnectorExceptionMessages.CONFIGURATION_FILE_NOT_SET_FOR_THIS_CONNECTOR);
        }

        int configNode = 0;

        try
        {
            configNode = loadXmlStoreFile(sConfigFileName);

            IServerConnection[] infos = ServerConnectionFactory.createServerConnections(configNode);

            for (IServerConnection info : infos)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Parsed server connection: " + info);
                }

                m_connectionMap.put(info.getId(), info);
            }

            for (IServerConnection info : infos)
            {
                info.open();
            }
        }
        finally
        {
            if (configNode != 0)
            {
                Node.delete(configNode);
                configNode = 0;
            }
        }
    }
}
