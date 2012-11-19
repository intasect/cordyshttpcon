package com.cordys.coe.ac.httpconnector.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClientError;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

import com.cordys.coe.ac.httpconnector.Messages;
import com.eibus.util.logger.CordysLogger;

public class DummySSLSocketFactory implements SecureProtocolSocketFactory {
	static CordysLogger logger = CordysLogger
			.getCordysLogger(DummySSLSocketFactory.class);

	private SSLContext sslcontext = null;

	@Override
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		if (logger.isDebugEnabled()) {
			logger.debug(Messages.BOOLEAN_CREATE_SOCKET);
		}
		return getSSLContext().getSocketFactory().createSocket(socket, host,
				port, autoClose);
	}

	private SSLContext getSSLContext() {
		if (this.sslcontext == null) {
			this.sslcontext = createEasySSLContext();
		}
		return this.sslcontext;
	}

	private static SSLContext createEasySSLContext() {
		try {
			SSLContext context = SSLContext.getInstance("SSL");
			context.init(null,
					new TrustManager[] { new DummyX509TrustManager() }, null);
			return context;
		} catch (Exception e) {
			throw new HttpClientError(e.toString());
		}
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress clientHost,
			int clientPort) throws IOException, UnknownHostException {
		if (logger.isDebugEnabled()) {
			logger.debug(Messages.CLIENT_CREATE_SOCKET);
		}
		return getSSLContext().getSocketFactory().createSocket(host, port,
				clientHost, clientPort);
	}

	@Override
	public Socket createSocket(final String host, final int port,
			final InetAddress localAddress, final int localPort,
			final HttpConnectionParams params) throws IOException,
			UnknownHostException, ConnectTimeoutException {
		if (logger.isDebugEnabled()) {
			logger.debug(Messages.FIVE_PARAMETER_CREATE_SOCKET);
		}
		if (params == null) {
			throw new IllegalArgumentException("Parameters may not be null");
		}
		int timeout = params.getConnectionTimeout();
		SocketFactory socketfactory = getSSLContext().getSocketFactory();
		if (timeout == 0) {
			return socketfactory.createSocket(host, port, localAddress,
					localPort);
		}
		Socket socket = socketfactory.createSocket();
		SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
		SocketAddress remoteaddr = new InetSocketAddress(host, port);
		socket.bind(localaddr);
		socket.connect(remoteaddr, timeout);
		return socket;
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException,
			UnknownHostException {
		if (logger.isDebugEnabled()) {
			logger.debug(Messages.TWO_PARAMETER_CREATE_SOCKET);
		}
		return getSSLContext().getSocketFactory().createSocket(host, port);
	}

	@Override
	public boolean equals(Object obj) {
		return ((obj != null) && obj.getClass().equals(
				DummySSLSocketFactory.class));
	}

	@Override
	public int hashCode() {
		return DummySSLSocketFactory.class.hashCode();
	}
}
