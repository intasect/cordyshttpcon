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

import com.cordys.coe.ac.httpconnector.config.IMethodConfiguration;
import com.cordys.coe.ac.httpconnector.config.IServerConnection;
import com.cordys.coe.ac.httpconnector.config.IXSLTStore;
import com.cordys.coe.ac.httpconnector.exception.HandlerException;

import com.eibus.xml.xpath.XPathMetaInfo;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;

/**
 * Interface for a class which converts the SOAP request XML into data which is sent to the web
 * server.
 *
 * @author  mpoyhone
 */
public interface IRequestHandler
{
    /**
     * Initializes this handler.
     *
     * @param   configXml  Handler configuration XML from the method configuration.
     * @param   connector  HTTP Connector instance.
     * @param   method     MethodConfiguration to which this handler belongs to.
     * @param   xmi        The XPathMetaInfo object containing the prefix ns mapped to the proper
     *                     namespace.
     *
     * @throws  HandlerException  In case of any exceptions.
     */
    void initialize(int configXml, IXSLTStore connector, IMethodConfiguration method,
                    XPathMetaInfo xmi)
             throws HandlerException;

    /**
     * Converts the request XML in HTTP method which can be sent using the HTTP connection.
     *
     * @param   requestNode  Request XML root node.
     * @param   connection   Current connection.
     * @param   httpClient   HTTP client instance.
     *
     * @return  Converted byte array.
     *
     * @throws  HandlerException  Thrown if the operation failed.
     */
    HttpMethod process(int requestNode, IServerConnection connection, HttpClient httpClient)
                throws HandlerException;
}
