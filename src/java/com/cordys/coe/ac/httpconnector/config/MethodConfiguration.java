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

import com.cordys.coe.ac.httpconnector.IRequestHandler;
import com.cordys.coe.ac.httpconnector.IResponseHandler;
import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.cordys.coe.ac.httpconnector.exception.ConnectorExceptionMessages;
import com.cordys.coe.ac.httpconnector.exception.HandlerException;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPath;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * Contains connection information parsed from the configuration XML.
 *
 * @author  mpoyhone
 */
class MethodConfiguration
    implements IMethodConfiguration
{
    /**
     * If <code>true</code>, the response SOAP body contents will be cleared. This is useful only
     * for methods which cannot return the response method element.
     */
    private boolean m_cleanResponseBody;
    /**
     * Contains the connection ID.
     */
    private String m_connectionId;
    /**
     * Contains the actual HTTP method which will be used (GET, POST, etc).
     */
    private EHttpMethod m_httpMethodType;
    /**
     * Contains the HTTP request handler for converting the SOAP request XML into an HTTP request.
     */
    private IRequestHandler m_requestHandler;
    /**
     * Contains the HTTP response handler for converting the HTTP response to SOAP request XML.
     */
    private IResponseHandler m_responseHandler;
    /**
     * Method specific URI. Usually this is the path.
     */
    private String m_uri;
    /**
     * Optional HTTP response code for validating the response.
     */
    private int m_validResponseCode = -1;
    /**
     * Optional XPath expression for validating the response.
     */
    private XPath m_validResponseXPath;
    /**
     * Contains XPath meta-information, e.g. namespace mappings.
     */
    private XPathMetaInfo m_xpathInfo;

    /**
     * Parse the method configuration.
     *
     * @param   implNode   The implementation XML to parse
     * @param   connector  The http connector object.
     * @param   xmi        The proper namespace prefix mapping to use.
     *
     * @throws  ConnectorException  Thrown if the method could not be parsed.
     * @throws  HandlerException    In case the handler throws any exception.
     */
    MethodConfiguration(int implNode, IXSLTStore connector, XPathMetaInfo xmi)
                 throws ConnectorException, HandlerException
    {
        parseConfigurationXML(implNode, connector, xmi);
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IMethodConfiguration#getConnectionId()
     */
    public String getConnectionId()
    {
        return m_connectionId;
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IMethodConfiguration#getHttpMethodType()
     */
    public EHttpMethod getHttpMethodType()
    {
        return m_httpMethodType;
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IMethodConfiguration#getRequestHandler()
     */
    public IRequestHandler getRequestHandler()
    {
        return m_requestHandler;
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IMethodConfiguration#getResponseHandler()
     */
    public IResponseHandler getResponseHandler()
    {
        return m_responseHandler;
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IMethodConfiguration#getUri()
     */
    public String getUri()
    {
        return m_uri;
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IMethodConfiguration#getValidResponseCode()
     */
    public int getValidResponseCode()
    {
        return m_validResponseCode;
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IMethodConfiguration#getValidResponseXPath()
     */
    public XPath getValidResponseXPath()
    {
        return m_validResponseXPath;
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IMethodConfiguration#getXPathMetaInfo()
     */
    public XPathMetaInfo getXPathMetaInfo()
    {
        return m_xpathInfo;
    }

    /**
     * @see  com.cordys.coe.ac.httpconnector.config.IMethodConfiguration#isCleanResponseBody()
     */
    public boolean isCleanResponseBody()
    {
        return m_cleanResponseBody;
    }

    /**
     * This method parses the XML configuration for the method.
     *
     * @param   implNode   The implementation node.
     * @param   connector  The HTTP connector.
     * @param   xmi        The namespace prefix mapping. The prefix ns must be mapped to the actual
     *                     namespace.
     *
     * @throws  ConnectorException
     * @throws  HandlerException    In case the handler throws any exception.
     */
    private void parseConfigurationXML(int implNode, IXSLTStore connector, XPathMetaInfo xmi)
                                throws ConnectorException, HandlerException
    {
        int requestHandlerNode = 0;
        int responseHandlerNode = 0;
        String tmpStr;
        m_xpathInfo = new XPathMetaInfo();

        // Read the connection ID.
        m_connectionId = XPathHelper.getStringValue(implNode, "ns:" + TAG_CONNECTION_ID, xmi, "");

        if (m_connectionId.length() == 0)
        {
            throw new ConnectorException(ConnectorExceptionMessages.CONNECTION_ID_IS_NOT_SET);
        }

        // Read the method URL (or URI).
        tmpStr = XPathHelper.getStringValue(implNode, "ns:" + TAG_URI, xmi, "");

        if (tmpStr.length() > 0)
        {
            m_uri = tmpStr;
        }

        // Read the 'clean response body' parameter
        m_cleanResponseBody = XPathHelper.getBooleanValue(implNode, "ns:" + TAG_CLEAN_RESPONSE_BODY,
                                                          xmi, false);

        // Try to read the HTTP method type (default to POST).
        tmpStr = XPathHelper.getStringValue(implNode, "ns:" + TAG_HTTP_METHOD, xmi, "POST");
        m_httpMethodType = EHttpMethod.valueOf(tmpStr.toUpperCase().trim());

        // Load the request handler.
        if ((requestHandlerNode = XPathHelper.selectSingleNode(implNode,
                                                                   "ns:" + TAG_REQUEST_HANDLER,
                                                                   xmi)) == 0)
        {
            throw new ConnectorException(ConnectorExceptionMessages.REQUEST_HANDLER_ELEMENT_IS_NOT_SET);
        }
        tmpStr = Node.getAttribute(requestHandlerNode, ATTRIBUTE_CLASS);

        if ((tmpStr == null) || (tmpStr.length() == 0))
        {
            throw new ConnectorException(ConnectorExceptionMessages.CLASS_ATTRIBUTE_MISSING_FROM_THE_REQUEST_HANDLER_ELEMENT);
        }

        try
        {
            Class<?> handlerClass = Class.forName(tmpStr);

            m_requestHandler = (IRequestHandler) handlerClass.newInstance();
        }
        catch (Exception e)
        {
            throw new ConnectorException(e,
                                         ConnectorExceptionMessages.UNABLE_TO_LOAD_REQUEST_HANDLER_0,
                                         tmpStr);
        }

        // Load the response handler.
        if ((responseHandlerNode = XPathHelper.selectSingleNode(implNode,
                                                                    "ns:" + TAG_RESPONSE_HANDLER,
                                                                    xmi)) == 0)
        {
            throw new ConnectorException(ConnectorExceptionMessages.RESPONSE_HANDLER_ELEMENT_IS_NOT_SET);
        }
        tmpStr = Node.getAttribute(responseHandlerNode, ATTRIBUTE_CLASS);

        if ((tmpStr == null) || (tmpStr.length() == 0))
        {
            throw new ConnectorException(ConnectorExceptionMessages.CLASS_ATTRIBUTE_MISSING_FROM_THE_RESPONSE_HANDLER_ELEMENT);
        }

        try
        {
            Class<?> handlerClass = Class.forName(tmpStr);

            m_responseHandler = (IResponseHandler) handlerClass.newInstance();
        }
        catch (Exception e)
        {
            throw new ConnectorException(e,
                                         ConnectorExceptionMessages.UNABLE_TO_LOAD_RESPONSE_HANDLER_0,
                                         tmpStr);
        }

        // Read the response XPath.
        tmpStr = XPathHelper.getStringValue(implNode, "ns:" + TAG_VALID_RESPONSE_XPATH, xmi, "");

        if ((tmpStr != null) && (tmpStr.length() > 0))
        {
            m_validResponseXPath = XPath.getXPathInstance(tmpStr);
        }

        // Read the response code.
        try
        {
            tmpStr = XPathHelper.getStringValue(implNode, "ns:" + TAG_VALID_RESPONSE_CODE, "-1");
            m_validResponseCode = Integer.parseInt(tmpStr);
        }
        catch (NumberFormatException e)
        {
            throw new ConnectorException(e,
                                         ConnectorExceptionMessages.INVALID_RESPONSE_CODE_VALUE_0,
                                         tmpStr);
        }

        // Read the namespace mappings
        int[] elems = XPathHelper.selectNodes(implNode,
                                              "ns:" + TAG_NAMESPACES + "/ns:" + TAG_BINDING, xmi);

        for (int node : elems)
        {
            String prefix = Node.getAttribute(node, ATTRIBUTE_PREFIX);
            String uri = Node.getAttribute(node, TAG_URI);

            if (prefix == null)
            {
                throw new ConnectorException(ConnectorExceptionMessages.MISSING_ATTRIBUTE_PREFIX_FROM_NAMESPACE_BINDING_ELEMENT);
            }

            if ((uri == null) || (uri.length() == 0))
            {
                throw new ConnectorException(ConnectorExceptionMessages.MISSING_ATTRIBUTE_URI_FROM_NAMESPACE_BINDING_ELEMENT);
            }

            m_xpathInfo.addNamespaceBinding(prefix, uri);
        }

        m_requestHandler.initialize(requestHandlerNode, connector, this, xmi);
        m_responseHandler.initialize(responseHandlerNode, connector, this, xmi);
    }
}
