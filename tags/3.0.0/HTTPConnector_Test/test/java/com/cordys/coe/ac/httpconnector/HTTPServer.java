package com.cordys.coe.ac.httpconnector;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

public class HTTPServer {
	public final int port;
	private Server server;

	public HTTPServer(int port, AbstractHandler responseHandler) {
		this.port = port;
		server = new Server(port);
		server.setHandler(responseHandler);
	}

	public void start() throws Exception {
		server.start();
	}

	public void stop() throws Exception {
		server.stop();
	}
}
