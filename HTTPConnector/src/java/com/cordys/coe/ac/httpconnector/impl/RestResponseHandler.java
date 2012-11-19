package com.cordys.coe.ac.httpconnector.impl;

import java.io.IOException;

import org.apache.commons.httpclient.HttpMethod;

import com.cordys.coe.ac.httpconnector.IResponseHandler;
import com.cordys.coe.ac.httpconnector.config.IServerConnection;
import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.eibus.util.logger.CordysLogger;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

/**
 * HTTP connector response handler for REST request.
 * 
 * @author jpluimer
 */
public class RestResponseHandler extends StandardResponseHandler {
	/**
	 * Logger for log messages from this class.
	 */
	private static final CordysLogger LOG = CordysLogger
			.getCordysLogger(RestResponseHandler.class);

	/**
	 * @see IResponseHandler#convertResponseToXml(HttpMethod, IServerConnection,
	 *      Document)
	 */
	@Override
	public int convertResponseToXml(HttpMethod httpMethod,
			IServerConnection serverConnection, Document doc)
			throws XMLException, ConnectorException, IOException {
		byte[] responseBody = httpMethod.getResponseBody();

		if (LOG.isDebugEnabled()) {
			try {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Received response data: "
							+ new String(responseBody, "UTF-8"));
				}
			} catch (Exception ignored) {
			}
		}

		if (responseBody.length == 0) {
			return 0;
		}

		// Need to wrap result in a node as in HttpTransaction.process() method.
		// as only child Nodes of the response are copied to the SOAPResponse.
		int resultNode = doc.createElement("result");
		int responseNode = doc.load(responseBody);
		Node.appendToChildren(responseNode, resultNode);

		return resultNode;
	}
}
