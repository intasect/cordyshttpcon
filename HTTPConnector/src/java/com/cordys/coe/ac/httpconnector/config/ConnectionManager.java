package com.cordys.coe.ac.httpconnector.config;

import com.cordys.coe.ac.httpconnector.HttpConfiguration;
import com.cordys.coe.ac.httpconnector.exception.ConnectorException;

public interface ConnectionManager {

	public abstract IServerConnection getConnection(String connectionId)
			throws ConnectorException;

	public abstract void reset();

	public static class Factory {
		public static ConnectionManager getConnectionManager(
				HttpConfiguration configuration) throws ConnectorException {
			return new DefaultConnectionManager(configuration);
		}
	}

}