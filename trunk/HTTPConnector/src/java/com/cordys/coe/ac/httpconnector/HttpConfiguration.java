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

import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.cordys.coe.ac.httpconnector.exception.ConnectorExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import static com.eibus.util.Util.isStringEmpty;
import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class holds the configuration details for the HttpConnector.
 */
public class HttpConfiguration {
	/**
	 * Holds the name of the tag 'configuration'.
	 */
	private static final String TAG_CONFIGURATION_LC = "configuration";
	/**
	 * Holds the name of the tag 'Configuration'.
	 */
	private static final String TAG_CONFIGURATION = "Configuration";
	/**
	 * Holds the name of the tag 'config-path'.
	 */
	private static final String TAG_CONFIG_PATH = "config-path";

	private static final String SYSTEM_PREFIX = "o=system";

	/**
	 * Contains the logger instance.
	 */
	private static CordysLogger LOG = CordysLogger
			.getCordysLogger(HttpConfiguration.class);
	/**
	 * Holds the location of the configuration file in the XML store.
	 */
	private String m_configPath;

	private String baseOrganizationDN;

	// This flag will be set to true only when the service container is running
	// in system organization
	private boolean organizationAware = false;

	/**
	 * Creates the constructor.This loads the configuration object and pass it
	 * to XMLProperties for processing.
	 * 
	 * @param iConfigNode
	 *            The XML-node that contains the configuration.
	 * 
	 * @throws ConnectorException
	 *             Thrown if the operation failed.
	 */
	public HttpConfiguration(int iConfigNode, String baseOrganizationDN)
			throws ConnectorException {
		if (iConfigNode == 0) {
			throw new ConnectorException(
					ConnectorExceptionMessages.CONFIGURATION_NOT_FOUND);
		}

		if (!Node.getName(iConfigNode).equals(TAG_CONFIGURATION_LC)) {
			throw new ConnectorException(
					ConnectorExceptionMessages.ROOTTAG_OF_THE_CONFIGURATION_SHOULD_BE_CONFIGURATION);
		}

		// Create XPath meta info
		XPathMetaInfo xmi = new XPathMetaInfo();
		xmi.addNamespaceBinding("ns",
				HttpConnectorConstants.NS_HTTP_CONNECTOR_CONF_2_0);

		int child = XPathHelper.selectSingleNode(iConfigNode, "ns:"
				+ TAG_CONFIGURATION, xmi);

		if ((child == 0) || !Node.getName(child).equals(TAG_CONFIGURATION)) {
			throw new ConnectorException(
					ConnectorExceptionMessages.CONFIGURATION_ELEMENT_NOT_FOUND);
		}

		// Get the path to the configuration file.
		m_configPath = XPathHelper.getStringValue(child, "//ns:"
				+ TAG_CONFIG_PATH, xmi, "");

		if (m_configPath.length() == 0) {
			throw new ConnectorException(
					ConnectorExceptionMessages.THE_TAG_CONFIGPATH_MUST_BE_FILLED);
		}

		if (isStringEmpty(baseOrganizationDN)) {
			throw new ConnectorException(
					ConnectorExceptionMessages.EMPTY_ORGANIZATION);
		}

		this.baseOrganizationDN = baseOrganizationDN;
		if (runningInSystem(this.baseOrganizationDN)) {
			organizationAware = true;
		}
		if (LOG.isInfoEnabled()) {
			LOG.info("The current configuration:\n"
					+ Node.writeToString(iConfigNode, false));
			LOG.info(Messages.ORGANIZATION_AWARENESS_MODE, organizationAware);
		}
	}

	private boolean runningInSystem(String organizationDN) {
		if (organizationDN.startsWith(SYSTEM_PREFIX)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the configuration file XML store path.
	 * 
	 * @return Configuration file path.
	 */
	public String getConfigurationFilePath() {
		return m_configPath;
	}

	public String getBaseOrganizationDN() {
		return baseOrganizationDN;
	}

	public boolean isOrganizationAware() {
		return organizationAware;
	}
}
