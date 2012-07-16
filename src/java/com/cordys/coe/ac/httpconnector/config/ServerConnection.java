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
 package com.cordys.coe.ac.httpconnector.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.cordys.coe.ac.httpconnector.exception.ConnectorExceptionMessages;
import com.cordys.coe.ac.httpconnector.ssl.DummySSLSocketFactory;
import com.cordys.coe.util.xml.nom.XPathHelper;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.Native;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * Contains configuration information for the web server connection.
 *
 * @author  mpoyhone
 * @author  pgussow
 */
class ServerConnection
    implements IServerConnection
{
    /**
     * Holds the parameters that are configured for this connection.
     */
    Map<String, IParameter> m_parameters;
    /**
     * If <code>true</code> authentication information is always sent before the server asks for it.
     */
    private boolean m_authenticateAlways;
    /**
     * If <code>true</code> web server certificate is checked for validity.
     */
    private boolean m_checkServerCertificate = true;
    /**
     * Contains the actual HTTP client.
     */
    private HttpClient m_client;
    /**
     * Contains the HTTP connection manager.
     */
    private MultiThreadedHttpConnectionManager m_connManager;
    /**
     * Connection ID.
     */
    private String m_id;
    /**
     * Connection password.
     */
    private String m_password;
    /**
     * Proxy server hostname.
     */
    private String m_proxyHost;
    /**
     * Proxy server username.
     */
    private String m_proxyPassword;
    /**
     * Proxy server port.
     */
    private int m_proxyPort;
    /**
     * Proxy server username.
     */
    private String m_proxyUsername;
    /**
     * Timeout value for the HTTP request.
     */
    private int m_timeout = DEFAULT_TIMEOUT;
    /**
     * Connection URL.
     */
    private URL m_url;
    /**
     * Connection user name.
     */
    private String m_username;

    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(ServerConnection.class);

    /**
     * Creates a new ServerConnection object.
     *
     * @param   connection  The connection XML.
     * @param   xmi         The proper XMI to use with prefix ns bound to the proepr namespace.
     *
     * @throws  ConnectorException  In case of any exceptions.
     */
    ServerConnection(int connection, XPathMetaInfo xmi)
              throws ConnectorException
    {
        String tmpStr;

        // Get the connection ID.
        m_id = Node.getAttribute(connection, ATTRIBUTE_ID);

        if ((m_id == null) || (m_id.length() == 0))
        {
            throw new ConnectorException(ConnectorExceptionMessages.CONNECTION_ATTRIBUTE_ID_IS_MISSING);
        }

        // Get and parse the base URL for the connection.
        tmpStr = XPathHelper.getStringValue(connection, "ns:" + TAG_URL, xmi, "");

        if (tmpStr.length() == 0)
        {
            throw new ConnectorException(ConnectorExceptionMessages.CONNECTION_ELEMENT_URL_IS_MISSING);
        }

        try
        {
            m_url = new URL(tmpStr);
        }
        catch (MalformedURLException e1)
        {
            throw new ConnectorException(e1, ConnectorExceptionMessages.INVALID_SERVER_URL_0,
                                         tmpStr);
        }

        // Get the optional timeout
        m_timeout = XPathHelper.getIntegerValue(connection, "ns:" + TAG_TIMEOUT, xmi,
                                                DEFAULT_TIMEOUT);

        m_username = XPathHelper.getStringValue(connection, "ns:" + TAG_USERNAME, xmi, null);
        m_password = XPathHelper.getStringValue(connection, "ns:" + TAG_PASSWORD, xmi, null);

        // Decode the Base64 encoded password.
        if (m_password != null)
        {
            byte[] bytes = m_password.getBytes();
            m_password = new String(Native.decodeBinBase64(bytes, bytes.length));
        }

        m_proxyHost = XPathHelper.getStringValue(connection, "ns:" + TAG_PROXY_HOST, xmi, null);

        if ((m_proxyHost != null) && (m_proxyHost.length() > 0))
        {
            tmpStr = XPathHelper.getStringValue(connection, "ns:" + TAG_PROXY_PORT, xmi, null);

            if ((tmpStr == null) || (tmpStr.length() == 0))
            {
                throw new ConnectorException(ConnectorExceptionMessages.PROXY_SERVER_PORT_MUST_BE_SET);
            }

            try
            {
                m_proxyPort = Integer.parseInt(tmpStr);
            }
            catch (NumberFormatException e1)
            {
                throw new ConnectorException(e1,
                                             ConnectorExceptionMessages.INVALID_PROXY_SERVER_PORT_VALUE_0,
                                             tmpStr);
            }

            m_proxyUsername = XPathHelper.getStringValue(connection, "ns:" + TAG_PROXY_USERNAME,
                                                         xmi, null);
            m_proxyPassword = XPathHelper.getStringValue(connection, "ns:" + TAG_PROXY_PASSWORD,
                                                         xmi, null);

            if (m_proxyPassword != null)
            {
                byte[] bytes = m_proxyPassword.getBytes();
                m_proxyPassword = new String(Native.decodeBinBase64(bytes, bytes.length));
            }
        }

        m_checkServerCertificate = XPathHelper.getBooleanValue(connection, TAG_CHECK_CERTIFICATE,
                                                               xmi, true);
        m_authenticateAlways = XPathHelper.getBooleanValue(connection, TAG_AUTHENTICATE_ALWAYS, xmi,
                                                           false);

        // Now create the parameter list
        m_parameters = ParameterFactory.createParameters(connection, xmi);
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IServerConnection#getHttpClient()
     */
    public HttpClient getHttpClient()
    {
        return m_client;
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IServerConnection#getId()
     */
    public String getId()
    {
        return m_id;
    }

    /**
     * This method gets the parameter with the given name. If the parameter is not found null is
     * returned.
     *
     * @param   name  The name of the parameter to retrieve.
     *
     * @return  The parameter with the given name. If the parameter is not found null is returned.
     */
    public IParameter getParameter(String name)
    {
        return m_parameters.get(name);
    }

    /**
     * This method gets the list of parameters. This is a copy of the internal list.
     *
     * @return  The list of parameters. This is a copy of the internal list.
     */
    public Map<String, IParameter> getParameters()
    {
        return new LinkedHashMap<String, IParameter>(m_parameters);
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IServerConnection#getPassword()
     */
    public String getPassword()
    {
        return m_password;
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IServerConnection#getTimeout()
     */
    public int getTimeout()
    {
        return m_timeout;
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IServerConnection#getUrl()
     */
    public URL getUrl()
    {
        return m_url;
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IServerConnection#getUsername()
     */
    public String getUsername()
    {
        return m_username;
    }

    /**
     * This method returns whether or not this connection has a parameter with the given name.
     *
     * @param   name  The name of the parameter to check.
     *
     * @return  true if this connection has a parameter with the given name. Otherwise false.
     */
    public boolean hasParameter(String name)
    {
        return m_parameters.containsKey(name);
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IServerConnection#isCheckServerCertificate()
     */
    public boolean isCheckServerCertificate()
    {
        return m_checkServerCertificate;
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IServerConnection#open()
     */
    @SuppressWarnings("deprecation")
		public void open()
    {    	
        m_connManager = new MultiThreadedHttpConnectionManager();
        m_client = new HttpClient(m_connManager);

        // Set the host information in the client.
        HostConfiguration hostConfig = m_client.getHostConfiguration();
        String protoName = m_url.getProtocol();
        String hostName = m_url.getHost();
        int port;

        port = m_url.getPort();

        if (port <= 0)
        {
            port = m_url.getDefaultPort();
        }

        if ("https".equals(protoName) && !m_checkServerCertificate)
        {
        		if (LOG.isInfoEnabled())
        		{
        		    LOG.info("Using DummySSLProtocolSocketFactory");
        		}
            Protocol proto = new Protocol("https",
                                          (ProtocolSocketFactory)
                                          new DummySSLSocketFactory(), port);

            hostConfig.setHost(hostName, port, proto);
        }
        else
        {
        		if (LOG.isInfoEnabled())
      			{
        				LOG.info("Using Default SSLProtocolSocketFactory");
      			}
            hostConfig.setHost(hostName, port, Protocol.getProtocol(protoName));
        }

        if (m_proxyHost != null)
        {
            hostConfig.setProxy(m_proxyHost, m_proxyPort);

            if (m_proxyUsername != null)
            {
                Credentials defaultcreds = new UsernamePasswordCredentials(m_proxyUsername,
                                                                           m_proxyPassword);

                m_client.getState().setProxyCredentials(new AuthScope(hostName, port,
                                                                      AuthScope.ANY_REALM),
                                                        defaultcreds);
            }
        }

        if (m_username != null)
        {
            if (m_authenticateAlways)
            {
                m_client.getParams().setAuthenticationPreemptive(true);
            }
             
            Credentials defaultcreds = new UsernamePasswordCredentials(m_username, m_password);

            m_client.getState().setCredentials(new AuthScope(hostName, port, AuthScope.ANY_REALM),
                                               defaultcreds);
        }
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IServerConnection#setCheckServerCertificate(boolean)
     */
    public void setCheckServerCertificate(boolean checkServerCertificate)
    {
        this.m_checkServerCertificate = checkServerCertificate;
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IServerConnection#setId(java.lang.String)
     */
    public void setId(String id)
    {
        this.m_id = id;
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IServerConnection#setPassword(java.lang.String)
     */
    public void setPassword(String password)
    {
        this.m_password = password;
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IServerConnection#setUrl(java.net.URL)
     */
    public void setUrl(URL url)
    {
        this.m_url = url;
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IServerConnection#setUsername(java.lang.String)
     */
    public void setUsername(String username)
    {
        this.m_username = username;
    }

    /**
     * @see  java.lang.Object#toString()
     */
    @Override public String toString()
    {
        StringBuilder sb = new StringBuilder(100);

        sb.append("{ ID: ").append(m_id);
        sb.append(", URL: ").append(m_url);

        if (m_username != null)
        {
            sb.append(", USERNAME: ").append(m_username);
        }
        sb.append(" }");

        return sb.toString();
    }
}
