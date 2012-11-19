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

import java.util.concurrent.ConcurrentHashMap;

import com.cordys.coe.ac.httpconnector.config.ConnectionManager;
import com.cordys.coe.ac.httpconnector.config.IMethodConfiguration;
import com.cordys.coe.ac.httpconnector.config.IXSLTStore;
import com.cordys.coe.ac.httpconnector.config.MethodConfigurationFactory;
import com.cordys.coe.ac.httpconnector.config.store.StoreManager;
import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.cordys.coe.ac.httpconnector.exception.HandlerException;
import com.cordys.coe.ac.httpconnector.management.PerformanceCounters;
import com.cordys.coe.coelib.LibraryVersion;
import com.eibus.localization.ILocalizableString;
import com.eibus.management.IManagedComponent;
import com.eibus.soap.ApplicationConnector;
import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;
import com.eibus.soap.Processor;
import com.eibus.soap.SOAPTransaction;
import com.eibus.util.logger.CordysLogger;

/**
 * Main application connector class for HttpConnector.
 */
public class HttpConnector extends ApplicationConnector {
	/**
	 * Contains the logger instance.
	 */
	private static CordysLogger LOG = CordysLogger
			.getCordysLogger(HttpConnector.class);
	/**
	 * Holds the configuration object for this connector.
	 */
	private HttpConfiguration m_configuration;
	/**
	 * Contains cached versions of the method configuration objects mapped by
	 * the method DN.
	 */
	private ConcurrentHashMap<String, IMethodConfiguration> m_methodCache = new ConcurrentHashMap<String, IMethodConfiguration>();

	private ConnectionManager connectionManager;

	private IXSLTStore xslStore;

	private IManagedComponent managedComponent;

	public PerformanceCounters counters;

	/**
	 * This method gets called when the processor is being stopped.
	 * 
	 * @param pProcessor
	 *            The processor that is being stopped.
	 */
	@Override
	public void close(Processor pProcessor) {
		if (LOG.isInfoEnabled()) {
			LOG.info(Messages.CONNECTOR_STOPPED);
		}
	}

	/**
	 * This method creates the transaction that will handle the requests.
	 * 
	 * @param stTransaction
	 *            The SOAP-transaction containing the message.
	 * 
	 * @return The newly created transaction.
	 */
	@Override
	public ApplicationTransaction createTransaction(
			SOAPTransaction stTransaction) {
		return new HttpTransaction(this, m_configuration);
	}

	/**
	 * Returns the configuration object.
	 * 
	 * @return The configuration object for this application connector.
	 */
	public HttpConfiguration getConfigurationObject() {
		return m_configuration;
	}

	/**
	 * Returns the method configuration information for this request.
	 * 
	 * @param req
	 *            Request body block.
	 * 
	 * @return Method configuration structure.
	 * 
	 * @throws ConnectorException
	 *             Thrown if the operation failed.
	 * @throws HandlerException
	 *             In case the handler throws any exception.
	 */
	public IMethodConfiguration getMethodConfig(BodyBlock req)
			throws ConnectorException, HandlerException {
		// Get the method DN and try to fetch a cached version.
		String methodDn = req.getMethodDefinition().getMethodDN().toString();
		IMethodConfiguration res = m_methodCache.get(methodDn);

		if (res != null) {
			// Cached version found.
			return res;
		}

		// No cached version found, so try to create a new one. Only
		// one thread can access this at the time.
		synchronized (this) {
			// Try a again, to see if some other thread managed, to
			// fetch the same method.
			if ((res = m_methodCache.get(methodDn)) != null) {
				return res;
			}

			// Parse the method configuration and insert the
			// new object in the cache.
			res = MethodConfigurationFactory.createMethodConfiguration(
					xslStore, req);
			m_methodCache.put(methodDn, res);
		}

		return res;
	}

	/**
	 * This method gets called when the processor is started. It reads the
	 * configuration of the processor and creates the connector with the proper
	 * parameters. It will also create a client connection to Cordys.
	 * 
	 * @param pProcessor
	 *            The processor that is started.
	 */
	@Override
	public void open(Processor pProcessor) {
		// Check the CoELib version.
		try {
			LibraryVersion.loadAndCheckLibraryVersionFromResource(
					this.getClass(), true);
		} catch (Exception e) {
			LOG.fatal(e, Messages.COELIB_VERSION_MISMATCH);
			throw new IllegalStateException(e.toString());
		}

		try {
			if (LOG.isInfoEnabled()) {
				LOG.info(Messages.CONNECTOR_STARTING);
			}
			StoreManager.getStore(managedComponent);
			m_configuration = new HttpConfiguration(getConfiguration(),
					pProcessor.getOrganization());
			connectionManager = ConnectionManager.Factory
					.getConnectionManager(m_configuration);
			xslStore = IXSLTStore.Factory.getStore(
					pProcessor.getOrganization(),
					m_configuration.isOrganizationAware());
			if (LOG.isInfoEnabled()) {
				LOG.info(Messages.CONNECTOR_STARTED);
			}
		} catch (Exception e) {
			LOG.fatal(e, Messages.CONNECTOR_START_EXCEPTION);
			throw new IllegalStateException(e);
		}
	}

	public ConnectionManager getConnectionFactory() {
		return connectionManager;
	}

	/**
	 * This method gets called when the processor is ordered to rest.
	 * 
	 * @param processor
	 *            The processor that is to be in reset state
	 */
	@Override
	public void reset(Processor processor) {
		if (LOG.isInfoEnabled()) {
			LOG.info(Messages.CONNECTOR_RESET);
		}

		// Clear the method cache.
		synchronized (this) {
			m_methodCache.clear();
		}
		connectionManager.reset();
		xslStore.reset();
	}

	/**
	 * Standard management method. Allows adding custom counters, alert
	 * definitions and problems definitions to this connector. Note that this
	 * method is called by the SOAP processor.
	 * 
	 * @return The JMX managed component created by the super class.
	 */
	@Override
	protected IManagedComponent createManagedComponent() {
		managedComponent = super.createManagedComponent();
		counters = new PerformanceCounters(managedComponent);
		return managedComponent;
	}

	/**
	 * Standard management method.
	 * 
	 * @return JMX type for this application connector.
	 */
	@Override
	protected String getManagedComponentType() {
		return "AppConnector";
	}

	/**
	 * Standard management method.
	 * 
	 * @return JMX description for this application connector.
	 */
	@Override
	protected ILocalizableString getManagementDescription() {
		return Messages.CONNECTOR_MANAGEMENT_DESCRIPTION;
	}

	/**
	 * Standard management method.
	 * 
	 * @return JMX name for this application connector.
	 */
	@Override
	protected String getManagementName() {
		return "HttpConnector";
	}

	@Override
	public IManagedComponent getManagedComponent() {
		return managedComponent;
	}

	public PerformanceCounters getCounters() {
		return counters;
	}
}
