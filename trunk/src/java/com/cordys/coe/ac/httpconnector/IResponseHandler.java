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
import com.cordys.coe.ac.httpconnector.exception.ConnectorException;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.XMLException;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.io.IOException;

import org.apache.commons.httpclient.HttpMethod;

/**
 * Interface for a class which converts the web server response in to SOAP response XML.
 *
 * @author  mpoyhone
 */
public interface IResponseHandler
{
    /**
     * Converts the response bytes to XML.
     *
     * @param   httpMethod        HTTP method from which the response will be read.
     * @param   serverConnection  Server connection information.
     * @param   doc               NOM document.
     *
     * @return  Response XML root node.
     *
     * @throws  XMLException        Thrown if the XML parsing failed.
     * @throws  ConnectorException
     * @throws  IOException
     */
    int convertResponseToXml(HttpMethod httpMethod, IServerConnection serverConnection,
                             Document doc)
                      throws XMLException, ConnectorException, IOException;

    /**
     * Initializes this handler.
     *
     * @param   configXml  Handler configuration XML from the method configuration.
     * @param   connector  HTTP Connector instance.
     * @param   method     MethodConfiguration to which this handler belongs to.
     * @param   xmi        The XPathMetaInfo object containing the prefix ns mapped to the proper
     *                     namespace.
     *
     * @throws  ConnectorException
     */
    void initialize(int configXml, IXSLTStore connector, IMethodConfiguration method,
                    XPathMetaInfo xmi)
             throws ConnectorException;
}
