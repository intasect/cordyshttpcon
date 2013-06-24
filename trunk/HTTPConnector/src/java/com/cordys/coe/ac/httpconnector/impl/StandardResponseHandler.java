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
package com.cordys.coe.ac.httpconnector.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.httpclient.HttpMethod;

import com.cordys.coe.ac.httpconnector.IResponseHandler;
import com.cordys.coe.ac.httpconnector.config.IMethodConfiguration;
import com.cordys.coe.ac.httpconnector.config.IServerConnection;
import com.cordys.coe.ac.httpconnector.config.IXSLTStore;
import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.cordys.coe.ac.httpconnector.exception.ConnectorExceptionMessages;
import com.cordys.coe.ac.httpconnector.utils.XmlUtils;
import com.cordys.coe.util.xml.nom.XPathHelper;
import com.eibus.util.logger.CordysLogger;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;
import com.eibus.xml.xpath.XPath;
import com.eibus.xml.xpath.XPathMetaInfo;
import com.eibus.xml.xpath.XSLT;

/**
 * HTTP connector response handler for Kiwi.
 * 
 * @author mpoyhone
 */
public class StandardResponseHandler implements IResponseHandler {
	/**
	 * Logger for log messages from this class.
	 */
	private static final CordysLogger LOG = CordysLogger
			.getCordysLogger(StandardResponseHandler.class);
	/**
	 * Holds the name of the tag 'xslt'.
	 */
	private static final String TAG_XSLT = "xslt";
	/**
	 * Holds the name of the tag 'root-xpath'.
	 */
	private static final String TAG_ROOT_XPATH = "root-xpath";
	/**
	 * Holds the name of the tag 'remove-namespaces'.
	 */
	private static final String TAG_REMOVE_NAMESPACES = "remove-namespaces";
	/**
	 * Holds the name of the tag 'namespace-uri'.
	 */
	private static final String TAG_NAMESPACE_URI = "namespace-uri";
	/**
	 * Contains the method configuration to which this handler is attached to.
	 */
	protected IMethodConfiguration method;
	/**
	 * If <code>true</code> all namespace definitions are removed.
	 */
	private boolean m_removeAllNamespaces;
	/**
	 * Contains namespace URI's which will be removed from the final XML.
	 */
	private Set<String> m_removeNamespaceUriSet;
	/**
	 * Contains an optional response root element selection XPath expression.
	 */
	private XPath m_responseRootXPath;

	private IXSLTStore xslStore;

	private int xsltNode;

	private XSLT xslt;

	boolean isInXMLStore = false;

	/**
	 * @see IResponseHandler#convertResponseToXml(HttpMethod, IServerConnection,
	 *      Document)
	 */
	@Override
	public int convertResponseToXml(HttpMethod httpMethod,
			IServerConnection serverConnection, Document doc)
			throws XMLException, ConnectorException, IOException {
		int resNode = convertToXml(httpMethod, doc);

		if (resNode == 0) {
			return 0;
		}

		boolean success = false;

		if (LOG.isDebugEnabled()) {
			LOG.debug("Response XML: " + Node.writeToString(resNode, true));
		}

		try {
			XPath validResponseXPath = method.getValidResponseXPath();

			if (validResponseXPath != null) {
				if (!validResponseXPath.evaluateBooleanResult(resNode)) {
					throw new ConnectorException(
							ConnectorExceptionMessages.THE_RESPONSE_XML_DID_NOT_MATCH_THE_VALID_RESPONSE_XPATH_THAT_WAS_CONFIGURED_IN_THE_METHOD_IMPLEMENTATION);
				}
			}

			resNode = preProcessXml(resNode);

			if (xsltNode != 0) {
				resNode = executeXslt(resNode, method);
			}

			if (m_responseRootXPath != null) {
				resNode = handleResponseXPath(resNode, method);
			}

			if ((m_removeNamespaceUriSet != null) || m_removeAllNamespaces) {
				XmlUtils.removeNamespacesRecursively(resNode,
						m_removeNamespaceUriSet);
			}

			resNode = postProcessXml(resNode);

			success = true;
		} finally {
			if (!success) {
				if (resNode != 0) {
					Node.delete(resNode);
					resNode = 0;
				}
			}
		}

		return resNode;
	}

	/**
	 * @see IResponseHandler#initialize(int, IXSLTStore, IMethodConfiguration,
	 *      XPathMetaInfo)
	 */
	@Override
	public void initialize(int configXml, IXSLTStore xslStore,
			IMethodConfiguration method, XPathMetaInfo xmi)
			throws ConnectorException {
		this.xslStore = xslStore;
		this.method = method;

		int tmpNode;

		// Read the XSLT information.
		if ((tmpNode = XPathHelper.selectSingleNode(configXml,
				"ns:" + TAG_XSLT, xmi)) != 0) {
			xsltNode = Node.duplicate(tmpNode);
			if (Node.getAttribute(xsltNode, "file", null) == null
					&& Node.getAttribute(xsltNode, "xmlstore", null) != null) {
				isInXMLStore = true;
			}
		}

		// Read response root element XPath
		if ((tmpNode = XPathHelper.selectSingleNode(configXml, "ns:"
				+ TAG_ROOT_XPATH, xmi)) != 0) {
			String value = Node.getDataWithDefault(tmpNode, "");

			if (value.length() > 0) {
				m_responseRootXPath = XPath.getXPathInstance(value);
			}
		}

		// Read namespaces to be removed from the response.
		if ((tmpNode = XPathHelper.selectSingleNode(configXml, "ns:"
				+ TAG_REMOVE_NAMESPACES, xmi)) != 0) {
			int[] nodes = XPathHelper.selectNodes(tmpNode, "ns:"
					+ TAG_NAMESPACE_URI, xmi);

			if ((nodes != null) && (nodes.length > 0)) {
				for (int node : nodes) {
					String uri = Node.getDataWithDefault(node, "");

					if (uri.length() > 0) {
						if ("*".equals(uri)) {
							m_removeAllNamespaces = true;
							m_removeNamespaceUriSet = null;
							break;
						}

						if (m_removeNamespaceUriSet == null) {
							m_removeNamespaceUriSet = new HashSet<String>();
						}

						m_removeNamespaceUriSet.add(uri);
					}
				}
			}
		}
	}

	/**
	 * Converts the HTTP response to XML.
	 * 
	 * @param httpMethod
	 *            HTTP method object from which the response data is read.
	 * @param doc
	 *            NOM document for creating the XML.
	 * 
	 * @return Converted NOM XML.
	 * 
	 * @throws IOException
	 * @throws XMLException
	 */
	protected int convertToXml(HttpMethod httpMethod, Document doc)
			throws IOException, XMLException {
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

		 if (responseBody == null || responseBody.length == 0){
			return 0;
		}

		// Convert the response into XML.
		return doc.load(responseBody);
	}

	/**
	 * Executes the XSLT transformation configured for this handler.
	 * 
	 * @param responseNode
	 *            Response node to be transformed.
	 * @param method
	 *            Current method configuration.
	 * 
	 * @return Transformed XML.
	 * @throws ConnectorException
	 */
	protected int executeXslt(int responseNode, IMethodConfiguration method)
			throws ConnectorException {
		XSLT xslt = this.xslt;
		if (xslt == null) {
			xslt = xslStore.loadXslt(xsltNode);
			if (!isInXMLStore) {
				this.xslt = xslt;
			}
		}

		int newNode = xslt.xslTransform(responseNode);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Response XML after XSLT transformation: "
					+ Node.writeToString(newNode, true));
		}

		Node.delete(responseNode);
		responseNode = 0;

		return newNode;
	}

	/**
	 * This method will log the response as it was received from the web server.
	 * 
	 * @param httpMethod
	 *            The HTTP method.
	 * 
	 * @return The response body.
	 * 
	 * @throws IOException
	 *             In case the response could not be read for some reason.
	 */
	protected byte[] getHTTPResponse(HttpMethod httpMethod) throws IOException {
		byte[] returnValue = httpMethod.getResponseBody();

		if (LOG.isDebugEnabled()) {
			if (LOG.isDebugEnabled()) {
				try {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Received response data: "
								+ new String(returnValue, "UTF-8"));
					}
				} catch (Exception ignored) {
				}
			}
		}

		return returnValue;
	}

	/**
	 * Fetches the response element using the configured XPath.
	 * 
	 * @param responseNode
	 *            Response node to be transformed.
	 * @param method
	 *            Current method configuration.
	 * 
	 * @return Transformed XML.
	 */
	protected int handleResponseXPath(int responseNode,
			IMethodConfiguration method) {
		int node = m_responseRootXPath.firstMatch(responseNode,
				method.getXPathMetaInfo());

		if (node != 0) {
			node = Node.unlink(node);
			Node.delete(responseNode);

			return node;
		}

		return responseNode;
	}

	/**
	 * Method which can do processing of the response XML after the XSLT
	 * transform is executed. If a new node is returned, the parameter node must
	 * be deleted by this method.
	 * 
	 * <p>
	 * This implementation just returns the node as-is.
	 * </p>
	 * 
	 * @param reqNode
	 *            Request XML node.
	 * 
	 * @return Processed XML.
	 */
	protected int postProcessXml(int reqNode) {
		return reqNode;
	}

	/**
	 * Method which can do processing of the response XML before the XSLT
	 * transform is called. If a new node is returned, the parameter node must
	 * be deleted by this method.
	 * 
	 * <p>
	 * This implementation just returns the node as-is.
	 * </p>
	 * 
	 * @param reqNode
	 *            Request XML node.
	 * 
	 * @return Processed XML.
	 * 
	 * @throws ConnectorException
	 *             Thrown if the operation failed.
	 */
	protected int preProcessXml(int reqNode) throws ConnectorException {
		return reqNode;
	}
}
