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
 package com.cordys.coe.ac.httpconnector.impl;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;

import com.cordys.coe.ac.httpconnector.IRequestHandler;
import com.cordys.coe.ac.httpconnector.config.IMethodConfiguration;
import com.cordys.coe.ac.httpconnector.config.IServerConnection;
import com.cordys.coe.ac.httpconnector.config.IXSLTStore;
import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.cordys.coe.ac.httpconnector.exception.HandlerException;
import com.cordys.coe.ac.httpconnector.exception.HandlerExceptionMessages;
import com.cordys.coe.ac.httpconnector.utils.Utils;
import com.cordys.coe.ac.httpconnector.utils.XmlUtils;
import com.cordys.coe.util.xml.nom.XPathHelper;
import com.eibus.util.logger.CordysLogger;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPath;
import com.eibus.xml.xpath.XPathMetaInfo;
import com.eibus.xml.xpath.XSLT;

/**
 * Standard HTTP connector request handler. This will execute the XSLT transformation, if configured
 *
 * @author  mpoyhone
 */
public class StandardRequestHandler
    implements IRequestHandler
{
    /**
     * Holds the name of the tag 'xslt'.
     */
    private static final String TAG_XSLT = "xslt";
    /**
     * Holds the name of the tag 'root-xpath'.
     */
    private static final String TAG_ROOT_XPATH = "root-xpath";
    /**
     * Holds the name of the tag 'remove-namespaces'.
     */
    private static final String TAG_REMOVE_NAMESPACES = "remove-namespaces";
    /**
     * Holds the name of the tag 'namespace-uri'.
     */
    private static final String TAG_NAMESPACE_URI = "namespace-uri";
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(StandardRequestHandler.class);
    /**
     * Default content type for the HTTP request.
     */
    protected static final String DEFAULT_CONTENT_TYPE = "text/xml; charset=UTF-8";
    /**
     * Contains the method configuration to which this handler is attached to.
     */
    protected IMethodConfiguration m_method;
    /**
     * Contains an optional request XSLT object.
     */
    protected XSLT m_requestXslt;
    /**
     * If <code>true</code> all namespace definitions are removed.
     */
    private boolean m_removeAllNamespaces;
    /**
     * Contains namespace URI's which will be removed from the final XML.
     */
    private Set<String> m_removeNamespaceUriSet;
    /**
     * Contains an optional request root element selection XPath expression.
     */
    private XPath m_requestRootXPath;

    /**
     * @see  IRequestHandler#initialize(int, IXSLTStore, IMethodConfiguration, XPathMetaInfo)
     */
    public void initialize(int configXml, IXSLTStore connector, IMethodConfiguration method,
                           XPathMetaInfo xmi)
                    throws HandlerException
    {
        m_method = method;

        int tmpNode;

        // Read the XSLT information.
        if ((tmpNode = XPathHelper.selectSingleNode(configXml, "ns:" + TAG_XSLT, xmi)) != 0)
        {
            try
            {
                m_requestXslt = connector.loadXslt(tmpNode);
            }
            catch (ConnectorException e)
            {
                throw new HandlerException(e, e.getMessageObject(), e.getMessageParameters());
            }
        }

        // Read request root element XPath
        if ((tmpNode = XPathHelper.selectSingleNode(configXml, "ns:" + TAG_ROOT_XPATH, xmi)) != 0)
        {
            String value = Node.getDataWithDefault(tmpNode, "");

            if (value.length() > 0)
            {
                m_requestRootXPath = XPath.getXPathInstance(value);
            }
        }

        // Read namespaces to be removed from the request.
        if ((tmpNode = XPathHelper.selectSingleNode(configXml, "ns:" + TAG_REMOVE_NAMESPACES,
                                                        xmi)) != 0)
        {
            int[] nodes = XPathHelper.selectNodes(tmpNode, "ns:" + TAG_NAMESPACE_URI, xmi);

            if ((nodes != null) && (nodes.length > 0))
            {
                for (int node : nodes)
                {
                    String uri = Node.getDataWithDefault(node, "");

                    if (uri.length() > 0)
                    {
                        if ("*".equals(uri))
                        {
                            m_removeAllNamespaces = true;
                            m_removeNamespaceUriSet = null;
                            break;
                        }

                        if (m_removeNamespaceUriSet == null)
                        {
                            m_removeNamespaceUriSet = new HashSet<String>();
                        }

                        m_removeNamespaceUriSet.add(uri);
                    }
                }
            }
        }
    }

    /**
     * @see  IRequestHandler#process(int, IServerConnection, HttpClient)
     */
    public HttpMethod process(int requestNode, IServerConnection connection, HttpClient httpClient)
                       throws HandlerException
    {
        String uri = getRequestUri(requestNode, connection, httpClient);
        EntityEnclosingMethod httpMethod;

        if (LOG.isDebugEnabled())
        {
            LOG.debug("HTTP method is: " + m_method.getHttpMethodType());
        }

        switch (m_method.getHttpMethodType())
        {
            case GET:
                return new GetMethod(uri); // Get method does not have a body.

            case POST:
                httpMethod = new PostMethod(uri);
                break;

            case PUT:
                httpMethod = new PutMethod(uri);
                break;

            case DELETE:
                return new DeleteMethod(uri); // Delete method does not have a body.

            default:
                throw new HandlerException(HandlerExceptionMessages.UNKNOWN_HTTP_METHOD);
        }

        int reqNode = requestNode;

        try
        {
            reqNode = preProcessXml(reqNode, reqNode != requestNode);

            if (m_requestXslt != null)
            {
                reqNode = executeXslt(reqNode, m_method, reqNode != requestNode);
            }

            if (m_requestRootXPath != null)
            {
                reqNode = handleRequestXPath(reqNode, m_method, reqNode != requestNode);
            }

            if ((m_removeNamespaceUriSet != null) || m_removeAllNamespaces)
            {
                XmlUtils.removeNamespacesRecursively(reqNode, m_removeNamespaceUriSet);
            }

            reqNode = postProcessXml(reqNode, reqNode != requestNode);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Final Request XML: " + Node.writeToString(reqNode, true));
            }

            // Get the data that should be posted.
            byte[] reqData = getPostData(reqNode, connection);
            String contentType = getContentType();

            httpMethod.setRequestEntity(new ByteArrayRequestEntity(reqData, contentType));

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Sending data: " + new String(reqData));
            }

            httpMethod.setRequestHeader("Content-type", contentType);
        }
        finally
        {
            if ((reqNode != 0) && (reqNode != requestNode))
            {
                Node.delete(reqNode);
                reqNode = 0;
            }
        }

        return httpMethod;
    }

    /**
     * Converts the XML structure into a byte array.
     *
     * @param   requestNode  node XML node to be converted.
     *
     * @return  A byte array containing the XML data.
     *
     * @throws  HandlerException
     */
    protected byte[] convertXmlToBytes(int requestNode)
                                throws HandlerException
    {
        String xmlStr = Node.writeToString(requestNode, false);

        try
        {
            return xmlStr.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new HandlerException(e,
                                       HandlerExceptionMessages.UNABLE_TO_CONVERT_THE_XML_STRING_TO_A_BYTE_ARRAY);
        }
    }

    /**
     * Executes the XSLT transformation configured for this handler.
     *
     * @param   requestNode  Request node to be transformed.
     * @param   method       Current method configuration.
     * @param   mustDelete   If <code>true</code> the parameter node must be deleted, if a new XML
     *                       node is returned. Otherwise the node cannot be deleted.
     *
     * @return  Transformed XML.
     */
    protected int executeXslt(int requestNode, IMethodConfiguration method, boolean mustDelete)
    {
        int newNode = m_requestXslt.xslTransform(requestNode, method.getXPathMetaInfo());

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Request XML after XSLT transformation: " +
                      Node.writeToString(newNode, true));
        }

        if (mustDelete)
        {
            Node.delete(requestNode);
            requestNode = 0;
        }

        return newNode;
    }

    /**
     * Returns the HTTP request content type.
     *
     * @return  Content type.
     */
    protected String getContentType()
    {
        return DEFAULT_CONTENT_TYPE;
    }

    /**
     * This method returns the data that should be put into the post data.
     *
     * @param   requestNode  The current request XML.
     * @param   connection   The current server connection.
     *
     * @return  The byte data to post.
     *
     * @throws  HandlerException  In case of any exceptions.
     */
    protected byte[] getPostData(int requestNode, IServerConnection connection)
                          throws HandlerException
    {
        return convertXmlToBytes(requestNode);
    }

    /**
     * Returns the URI for this request.
     *
     * @param   requestNode  Incoming request XML node.
     * @param   connection   Current connection.
     * @param   httpClient   HTTP client object.
     *
     * @return  Request URI. This cannot be <code>null</code>.
     *
     * @throws  HandlerException  In case of any exceptions.
     */
    protected String getRequestUri(int requestNode, IServerConnection connection,
                                   HttpClient httpClient)
                            throws HandlerException
    {
        String uri = m_method.getUri();

        if ((uri == null) || (uri.length() == 0))
        {
            URL url = connection.getUrl();
            uri = Utils.getUrlPath(url);
        }

        return uri;
    }

    /**
     * Fetches the request element using the configured XPath.
     *
     * @param   requestNode  Request node to be transformed.
     * @param   method       Current method configuration.
     * @param   mustDelete   If <code>true</code> the parameter node must be deleted, if a new XML
     *                       node is returned. Otherwise the node cannot be deleted.
     *
     * @return  Transformed XML.
     */
    protected int handleRequestXPath(int requestNode, IMethodConfiguration method,
                                     boolean mustDelete)
    {
        int node = m_requestRootXPath.firstMatch(requestNode, method.getXPathMetaInfo());

        if (node != 0)
        {
            node = Node.unlink(node);

            if (mustDelete)
            {
                Node.delete(requestNode);
            }

            return node;
        }

        return requestNode;
    }

    /**
     * Method which can do processing of the request XML after the XSLT transform is executed. This
     * implementation just returns the node as-is.
     *
     * @param   reqNode     Request XML node.
     * @param   mustDelete  If <code>true</code> the parameter node must be deleted, if a new XML
     *                      node is returned. Otherwise the node cannot be deleted.
     *
     * @return  Processed XML.
     *
     * @throws  HandlerException  in case of any errors.
     */
    protected int postProcessXml(int reqNode, boolean mustDelete)
                          throws HandlerException
    {
        return reqNode;
    }

    /**
     * Method which can do processing of the request XML before the XSLT transform is called. This
     * implementation returns the first child element of the request XML as the root is the SOAP
     * request method node.
     *
     * @param   reqNode     Request XML node.
     * @param   mustDelete  If <code>true</code> the parameter node must be deleted, if a new XML
     *                      node is returned. Otherwise the node cannot be deleted.
     *
     * @return  Processed XML.
     *
     * @throws  HandlerException  In case of any errors.
     */
    protected int preProcessXml(int reqNode, boolean mustDelete)
                         throws HandlerException
    {
        return reqNode;
    }
}
