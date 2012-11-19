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

import java.text.MessageFormat;

import org.apache.commons.httpclient.HttpClient;

import com.cordys.coe.ac.httpconnector.config.IMethodConfiguration;
import com.cordys.coe.ac.httpconnector.config.IParameter;
import com.cordys.coe.ac.httpconnector.config.IServerConnection;
import com.cordys.coe.ac.httpconnector.config.IXSLTStore;
import com.cordys.coe.ac.httpconnector.exception.HandlerException;
import com.cordys.coe.ac.httpconnector.exception.HandlerExceptionMessages;
import com.cordys.coe.ac.httpconnector.utils.Utils;
import com.cordys.coe.util.xml.nom.XPathHelper;
import com.eibus.util.logger.CordysLogger;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPath;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * HTTP connector request handler for REST request.
 *
 * @author  mpoyhone
 */
public class RestRequestHandler extends StandardRequestHandler
{
    /**
     * Holds the name of the tag 'uri-parameters'.
     */
    private static final String TAG_URI_PARAMETERS = "uri-parameters";
    /**
     * Holds the name of the tag 'parameter'.
     */
    private static final String TAG_PARAMETER = "parameter";
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(RestRequestHandler.class);
    /**
     * Contains URI parameters that are read from the method implementation.
     */
    private UriParameter[] urlParamArray = null;

    /**
     * @see  StandardRequestHandler#initialize(int, IXSLTStore, IMethodConfiguration,
     *       XPathMetaInfo)
     */
    @Override public void initialize(int configXml, IXSLTStore connector,
                                     IMethodConfiguration method, XPathMetaInfo xmi)
                              throws HandlerException
    {
        super.initialize(configXml, connector, method, xmi);

        int[] params = XPathHelper.selectNodes(configXml,
                                               "ns:" + TAG_URI_PARAMETERS + "/ns:" + TAG_PARAMETER,
                                               xmi);
        urlParamArray = new UriParameter[params.length];

        for (int i = 0; i < params.length; i++)
        {
            int paramNode = params[i];
            UriParameter p = new UriParameter();
            String typeStr = Node.getAttribute(paramNode, "type", "");

            p.type = EUriParameterType.getByConfigName(typeStr);

            if (p.type == null)
            {
                throw new HandlerException(HandlerExceptionMessages.INVALID_URI_PARAMETER_TYPE_0,
                                             typeStr);
            }

            switch (p.type)
            {
                case XPATH:

                    String xpathStr = Node.getDataWithDefault(paramNode, "");

                    if ((xpathStr == null) || (xpathStr.length() == 0))
                    {
                        throw new HandlerException(HandlerExceptionMessages.PARAMETER_XPATH_IS_NOT_SET);
                    }

                    p.value = XPath.getXPathInstance(xpathStr);
                    break;

                case FIXED:
                case CONNECTION_PARAMETER:
                    p.value = Node.getDataWithDefault(paramNode, "");
                    break;
            }

            urlParamArray[i] = p;
        }
    }

    /**
     * @see  StandardRequestHandler#getRequestUri(int, IServerConnection, HttpClient)
     */
    @Override protected String getRequestUri(int requestNode, IServerConnection connection,
                                             HttpClient httpClient)
    {
        String uri = m_method.getUri();

        if ((uri == null) || (uri.length() == 0))
        {
            return "/";
        }

        Object[] args = new Object[urlParamArray.length];

        for (int i = 0; i < args.length; i++)
        {
            String value = null;
            UriParameter param = urlParamArray[i];

            switch (param.type)
            {
                case XPATH:
                    value = XPathHelper.getStringValue(requestNode, (XPath) param.value, null);
                    break;

                case CONNECTION_URI:
                    value = Utils.getUrlPath(connection.getUrl());
                    break;

                case FIXED:
                    value = (String) param.value;
                    break;

                case CONNECTION_PARAMETER:

                    IParameter connParam = connection.getParameter((String) param.value);
                    if (connParam != null)
                    {
                        // In this case the XML parameters are not used since we cannot put XML in
                        // the request.
                        value = connParam.getValue().toString();
                    }
                    break;
            }

            args[i] = ((value != null) ? value : "");
        }

        uri = MessageFormat.format(uri, args);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("REST request URI: " + uri);
        }

        return uri;
    }

    /**
     * Holds the URI parameter types.
     *
     * @author  pgussow
     */
    private enum EUriParameterType
    {
        CONNECTION_URI("connection-uri"),
        XPATH("xpath"),
        FIXED("fixed"),
        CONNECTION_PARAMETER("connection-parameter");

        /**
         * Holds the configuration name for the URI parameter type.
         */
        private String configName;

        /**
         * Creates a new EUriParameterType object.
         *
         * @param  configName  The configuration name.
         */
        private EUriParameterType(String configName)
        {
            this.configName = configName;
        }

        /**
         * This method gets the proper enum based on the configuration value.
         *
         * @param   configValue  The configuration value.
         *
         * @return  The matching enum. If the configValue is not found null is returned.
         */
        public static EUriParameterType getByConfigName(String configValue)
        {
            for (EUriParameterType type : values())
            {
                if (type.configName.equals(configValue))
                {
                    return type;
                }
            }

            return null;
        }
    }

    /**
     * URI parameter parsed from the configuration.
     *
     * @author  mpoyhone
     */
    private static class UriParameter
    {
        /**
         * Holds the type for the parameter.
         */
        private EUriParameterType type;
        /**
         * Holds the value if the parameter is fixed.
         */
        private Object value;
    }
}
