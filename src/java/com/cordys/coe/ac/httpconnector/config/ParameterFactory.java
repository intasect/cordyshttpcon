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

import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This factory can create the parameter objects.
 *
 * @author  pgussow
 */
public class ParameterFactory
{
    /**
     * This method creates the parameter list based on the parameters/parameter XPath.
     *
     * @param   connection  The connection XML.
     * @param   xmi         The proper XMI to use.
     *
     * @return  The list of parameters.
     *
     * @throws  ConnectorException  In case of any exceptions.
     */
    public static Map<String, IParameter> createParameters(int connection, XPathMetaInfo xmi)
                                                    throws ConnectorException
    {
        Map<String, IParameter> returnValue = new LinkedHashMap<String, IParameter>();

        int[] parameters = XPathHelper.selectNodes(connection,
                                                   "ns:" + IServerConnection.TAG_PARAMETERS +
                                                   "/ns:" + IServerConnection.TAG_PARAMETER, xmi);

        for (int parameter : parameters)
        {
            IParameter param = new Parameter(parameter, xmi);
            returnValue.put(param.getName(), param);
        }

        return returnValue;
    }
}
