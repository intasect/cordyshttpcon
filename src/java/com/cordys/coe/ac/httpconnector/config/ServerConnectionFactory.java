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

import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * This factory provides the means to create a server info object based on the connection info.
 *
 * @author  pgussow
 */
public class ServerConnectionFactory
{
    /**
     * Parses the connection configuration elements into objects.
     *
     * @param   config  XML Configuration.
     *
     * @return  Configuration objects as an array.
     *
     * @throws  ConnectorException  Thrown if the configuration is invalid.
     */
    public static IServerConnection[] createServerConnections(int config)
                                                       throws ConnectorException
    {
        // Create the NS binding.
        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", Node.getNamespaceURI(config));

        int[] connections = XPathHelper.selectNodes(config, "ns:connections/ns:connection", xmi);
        List<IServerConnection> resList = new ArrayList<IServerConnection>();

        for (int connection : connections)
        {
            IServerConnection info = new ServerConnection(connection, xmi);
            resList.add(info);
        }

        return resList.toArray(new ServerConnection[resList.size()]);
    }
}
