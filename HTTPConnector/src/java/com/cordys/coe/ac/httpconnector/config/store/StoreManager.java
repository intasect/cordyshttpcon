package com.cordys.coe.ac.httpconnector.config.store;

import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.eibus.management.IManagedComponent;

public class StoreManager {

	private static XMLStore xmlStore = null;

	public static synchronized Store getStore() throws ConnectorException {
		return getStore(null);
	}

	public static synchronized Store getStore(IManagedComponent parent)
			throws ConnectorException {
		if (xmlStore == null) {
			xmlStore = new XMLStore(parent);
		}
		return xmlStore;
	}
}
