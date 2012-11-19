package com.cordys.coe.ac.httpconnector.config.store;

import com.cordys.coe.ac.httpconnector.exception.ConnectorException;

public interface Store {

	int load(String path, String organizationDN, String level)
			throws ConnectorException;

}
