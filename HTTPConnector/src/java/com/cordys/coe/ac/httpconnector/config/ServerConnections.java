package com.cordys.coe.ac.httpconnector.config;

import java.util.HashMap;
import java.util.Map;

public class ServerConnections {

	private Map<String, IServerConnection> connections = new HashMap<String, IServerConnection>();

	public void add(String id, final IServerConnection connection) {
		connections.put(id, connection);
	}

	public void open() {
		for (IServerConnection connection : connections.values()) {
			connection.open();
		}
	}

	public IServerConnection get(String id) {
		return connections.get(id);
	}
}
