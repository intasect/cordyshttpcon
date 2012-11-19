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

import com.cordys.coe.ac.httpconnector.HttpConfiguration;
import com.cordys.coe.ac.httpconnector.Messages;
import com.cordys.coe.ac.httpconnector.config.store.StoreManager;
import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.cordys.coe.ac.httpconnector.exception.ConnectorExceptionMessages;
import static com.cordys.coe.ac.httpconnector.HttpConnectorConstants.*;
import com.cordys.coe.util.xml.nom.XPathHelper;
import com.eibus.soap.SOAPTransaction;
import com.eibus.util.logger.CordysLogger;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This factory provides the means to create a server info object based on the
 * connection info.
 * 
 * @author pgussow
 */
public class DefaultConnectionManager implements ConnectionManager {
	private Map<String, ServerConnections> connectionMap = new ConcurrentHashMap<String, ServerConnections>();

	private Object lock = new Object();

	private static final CordysLogger logger = CordysLogger
			.getCordysLogger(DefaultConnectionManager.class);

	private HttpConfiguration configuration;

	public DefaultConnectionManager(HttpConfiguration configuration)
			throws ConnectorException {
		this.configuration = configuration;
		getServerConnections(this.configuration.getBaseOrganizationDN(),
				ISV_VERSION);
	}

	@SuppressWarnings("PMD")
	// This is a concurrent hash map, Double check locking problem does not
	// apply here
	private ServerConnections getServerConnections(String organizationDN,
			String level) throws ConnectorException {
		String key = getKey(level, organizationDN);

		ServerConnections connections = connectionMap.get(key);
		if (connections == null) {
			synchronized (lock) {
				connections = connectionMap.get(key);
				if (connections == null) {
					connections = createConnections(organizationDN, level);
				}
			}
		}
		return connections;
	}

	private ServerConnections createConnections(String organizationDN,
			String level) throws ConnectorException {

		String sConfigFileName = configuration.getConfigurationFilePath();
		if ((sConfigFileName == null) || sConfigFileName.equals("")) {
			throw new ConnectorException(
					ConnectorExceptionMessages.CONFIGURATION_FILE_NOT_SET_FOR_THIS_CONNECTOR);
		}

		int configNode = 0;
		ServerConnections connections = null;
		String key = getKey(level, organizationDN);
		try {
			configNode = StoreManager.getStore().load(sConfigFileName,
					organizationDN, level);
			connections = createConnections(configNode);
			connections.open();
			connectionMap.put(key, connections);
		} finally {
			if (configNode != 0) {
				Node.delete(configNode);
				configNode = 0;
			}
		}
		return connections;
	}

	private ServerConnections createConnections(int config)
			throws ConnectorException {
		ServerConnections serverConnections = new ServerConnections();
		if (config == 0) {
			return serverConnections;
		}
		XPathMetaInfo xmi = new XPathMetaInfo();
		xmi.addNamespaceBinding("ns", Node.getNamespaceURI(config));

		int[] connections = XPathHelper.selectNodes(config,
				"ns:connections/ns:connection", xmi);
		for (int connection : connections) {
			IServerConnection info = new ServerConnection(connection, xmi);
			serverConnections.add(info.getId(), info);
		}
		return serverConnections;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.cordys.coe.ac.httpconnector.config.ServerConnectionFactory#getConnection
	 * (java.lang.String)
	 */
	@Override
	public IServerConnection getConnection(String connectionId)
			throws ConnectorException {
		String organizationDN = null;
		IServerConnection connection = null;
		ServerConnections connections;
		if (configuration.isOrganizationAware()) {
			organizationDN = SOAPTransaction.getCurrentSOAPTransaction()
					.getIdentity().getUserOrganization();
			connections = getServerConnections(organizationDN,
					ORGANIZATION_VERSION);
			connection = connections.get(connectionId);
		} else {
			organizationDN = configuration.getBaseOrganizationDN();
			connections = getServerConnections(organizationDN,
					ORGANIZATION_VERSION);
			connection = connections.get(connectionId);
		}
		if (connection == null) {
			connections = connectionMap.get(ISV_SPACE);
			if (connections != null) {
				connection = connections.get(connectionId);
				if (logger.isInfoEnabled()) {
					logger.info(Messages.CONNECTION_FOUND, connectionId,
							"System space");
				}
			}
		} else {
			if (logger.isInfoEnabled()) {
				logger.info(Messages.CONNECTION_FOUND, connectionId,
						organizationDN);
			}
		}
		return connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.cordys.coe.ac.httpconnector.config.ServerConnectionFactory#reset()
	 */
	@Override
	public void reset() {
		synchronized (lock) {
			connectionMap.clear();
		}
	}

	private String getKey(String level, String organizationDN) {
		String key;
		if (level.equals(ISV_VERSION)) {
			key = ISV_SPACE;
		} else {
			key = organizationDN;
		}
		return key;
	}

	// TODO: This method should eventually be removed. It is being used only by a JIRA test case.
	/**
	 * Parses the connection configuration elements into objects.
	 * 
	 * @param config
	 *            XML Configuration.
	 * 
	 * @return Configuration objects as an array.
	 * 
	 * @throws ConnectorException
	 *             Thrown if the configuration is invalid.
	 */
	public static IServerConnection[] createServerConnections(int config)
			throws ConnectorException {
		// Create the NS binding.
		XPathMetaInfo xmi = new XPathMetaInfo();
		xmi.addNamespaceBinding("ns", Node.getNamespaceURI(config));

		int[] connections = XPathHelper.selectNodes(config,
				"ns:connections/ns:connection", xmi);
		List<IServerConnection> resList = new ArrayList<IServerConnection>();
		for (int connection : connections) {
			IServerConnection info = new ServerConnection(connection, xmi);
			resList.add(info);
		}

		return resList.toArray(new ServerConnection[resList.size()]);
	}
}
