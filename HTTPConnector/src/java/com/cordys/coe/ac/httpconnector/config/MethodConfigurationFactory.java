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

import com.cordys.coe.ac.httpconnector.HttpConnectorConstants;
import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.cordys.coe.ac.httpconnector.exception.HandlerException;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.soap.BodyBlock;
import com.eibus.soap.MethodDefinition;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This factory can create new MethodConfiguration objects.
 * 
 * @author pgussow
 */
public class MethodConfigurationFactory {
	/**
	 * Holds the logger to use.
	 */
	private static final CordysLogger LOG = CordysLogger
			.getCordysLogger(MethodConfigurationFactory.class);
	/**
	 * Holds the XPathMetaInfo object for this class.
	 */
	private static XPathMetaInfo s_xmi;

	static {
		s_xmi = new XPathMetaInfo();
		s_xmi.addNamespaceBinding("ns",
				HttpConnectorConstants.NS_HTTP_CONNECTOR_IMPL_2_0);
	}

	/**
	 * This method creates a new method configuration object based on the
	 * current HttpConnector configuration.
	 * 
	 * @param conn
	 *            The HttpConnector that should be used.
	 * @param req
	 *            The actual request as it was received.
	 * 
	 * @return The corresponding method configuration object.
	 * 
	 * @throws ConnectorException
	 *             In case of any exceptions.
	 * @throws HandlerException
	 *             In case the handler throws any exception.
	 */
	public static IMethodConfiguration createMethodConfiguration(
			IXSLTStore conn, BodyBlock req) throws ConnectorException,
			HandlerException {
		IMethodConfiguration returnValue = null;

		MethodDefinition md = req.getMethodDefinition();

		if (LOG.isDebugEnabled()) {
			LOG.debug("Creating method configuration for method "
					+ md.getMethodDN());
		}

		int implementation = md.getImplementation();

		int realImpl = XPathHelper.selectSingleNode(implementation, "ns:"
				+ IMethodConfiguration.TAG_IMPLEMENTATION, s_xmi);

		if (realImpl == 0) {
			// Let's try compatibility mode
			if (LOG.isDebugEnabled()) {
				LOG.debug("Method implementation in compatibility mode.");
			}

			XPathMetaInfo xmi = new XPathMetaInfo();
			xmi.addNamespaceBinding("ns", Node.getNamespaceURI(implementation));

			returnValue = new MethodConfiguration(implementation, conn, xmi);
		} else {
			returnValue = new MethodConfiguration(realImpl, conn, s_xmi);
		}

		return returnValue;
	}

	/**
	 * This method creates the method configuration based on the implementation
	 * XML.
	 * 
	 * @param conn
	 *            The HttpConnector to use.
	 * @param implementation
	 *            The actual implementation for the method.
	 * 
	 * @return The created {@link IMethodConfiguration} object.
	 * 
	 * @throws ConnectorException
	 *             In case of any exceptions.
	 * @throws HandlerException
	 *             In case the handler throws any exception.
	 */
	public static IMethodConfiguration createMethodConfiguration(
			IXSLTStore conn, int implementation) throws ConnectorException,
			HandlerException {
		IMethodConfiguration returnValue = null;

		XPathMetaInfo xmi = new XPathMetaInfo();
		xmi.addNamespaceBinding("ns", Node.getNamespaceURI(implementation));

		returnValue = new MethodConfiguration(implementation, conn, xmi);

		return returnValue;
	}
}
