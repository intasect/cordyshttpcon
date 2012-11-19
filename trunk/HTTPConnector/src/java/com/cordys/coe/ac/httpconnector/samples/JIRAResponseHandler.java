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
package com.cordys.coe.ac.httpconnector.samples;

import com.cordys.coe.ac.httpconnector.IResponseHandler;
import com.cordys.coe.ac.httpconnector.config.IServerConnection;
import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.cordys.coe.ac.httpconnector.exception.ConnectorExceptionMessages;
import com.cordys.coe.ac.httpconnector.exception.HandlerException;
import com.cordys.coe.ac.httpconnector.exception.HandlerExceptionMessages;
import com.cordys.coe.ac.httpconnector.impl.StandardResponseHandler;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.XMLException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpMethod;

import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.parser.HtmlParser;
import org.lobobrowser.html.test.SimpleUserAgentContext;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class will handle the responses from HTML.
 * 
 * @author pgussow
 */
public class JIRAResponseHandler extends StandardResponseHandler {
	/**
	 * @see IResponseHandler#convertResponseToXml(HttpMethod, IServerConnection,
	 *      Document)
	 */
	@Override
	public int convertResponseToXml(HttpMethod httpMethod,
			IServerConnection serverConnection, Document doc)
			throws XMLException, ConnectorException, IOException {
		int resNode = doc.createElement("result");

		try {
			// Get the actual response from the web server.
			byte[] responseBody = getHTTPResponse(httpMethod);

			// Parse the HTML into a DOM tree2
			org.w3c.dom.Document document = parseResponse(responseBody);

			// Now we use XPath to locate "span" elements that have the errMsg
			// class set.
			checkErrors(document, httpMethod);

			// Response seems ok.
			buildXMLResponse(resNode, httpMethod, document, doc);
		} catch (Exception e) {
			// In case of an exception we need to clean.
			if (resNode != 0) {
				com.eibus.xml.nom.Node.delete(resNode);
				resNode = 0;
			}

			throw new ConnectorException(e,
					ConnectorExceptionMessages.ERROR_PARSING_THE_HTML_RESPONSE);
		}

		return resNode;
	}

	/**
	 * This method can be overridden to create a response XML structure.
	 * 
	 * @param resNode
	 *            The parent response node.
	 * @param httpMethod
	 *            The actual HTTP method that was executed.
	 * @param document
	 *            The HTML response.
	 * @param doc
	 *            The XML document to use.
	 * 
	 * @throws Exception
	 *             In case of any exceptions
	 */
	protected void buildXMLResponse(int resNode, HttpMethod httpMethod,
			org.w3c.dom.Document document, Document doc) throws Exception {
		doc.createTextElement("status", "ok", resNode);
	}

	/**
	 * This method checks the HTML for errors during processing.
	 * 
	 * @param document
	 *            The HTML document.
	 * @param httpMethod
	 *            The actual HTTP method that was executed.
	 * 
	 * @throws HandlerException
	 *             In case the response contains any functional errors.
	 * @throws XPathExpressionException
	 *             In case one of the XPaths fail.
	 */
	protected void checkErrors(org.w3c.dom.Document document,
			HttpMethod httpMethod) throws HandlerException,
			XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xpath.evaluate(
				"//span[@class='errMsg']/text()", document,
				XPathConstants.NODESET);
		int length = nodeList.getLength();

		if (length != 0) {
			// The first error message found will be used.
			HandlerException he = new HandlerException(
					HandlerExceptionMessages.ERROR_CONVERTING_THE_RESPONSE_TO_PROPER_XML,
					nodeList.item(0).getNodeValue());

			for (int i = 0; i < length; i++) {
				Node node = nodeList.item(i);
				he.addAdditionalErrorMessage(node.getNodeValue());
			}
			throw he;
		}

		// There is another possibility of which errors might be returned. There
		// is a td with
		// class formErrors. And it that holds the errors as list items.
		nodeList = (NodeList) xpath
				.evaluate(
						"//td[@class='formErrors']/div[@class='errorArea']/ul/li/text()",
						document, XPathConstants.NODESET);
		length = nodeList.getLength();

		if (length != 0) {
			// The first error message found will be used.
			HandlerException he = new HandlerException(
					HandlerExceptionMessages.ERROR_CONVERTING_THE_RESPONSE_TO_PROPER_XML,
					nodeList.item(0).getNodeValue());

			for (int i = 0; i < length; i++) {
				Node node = nodeList.item(i);
				he.addAdditionalErrorMessage(node.getNodeValue());
			}
			throw he;
		}

		if (httpMethod.getStatusCode() == 500) {
			// Find the short description
			Node n = (Node) xpath.evaluate("//b[.='Cause: ']", document,
					XPathConstants.NODE);
			String shortError = n.getNextSibling().getNextSibling()
					.getNodeValue().trim();

			// The first error message found will be used.
			HandlerException he = new HandlerException(
					HandlerExceptionMessages.ERROR_CONVERTING_THE_RESPONSE_TO_PROPER_XML,
					"System Error: " + shortError);

			// Find the stacktrace if available.
			he.addAdditionalErrorMessage((String) xpath.evaluate(
					"//pre[@id='stacktrace']/text()", document,
					XPathConstants.STRING));

			throw he;
		}
	}

	/**
	 * This method parses the response HTML into a DOM tree.
	 * 
	 * @param responseBody
	 *            The response HTML.
	 * 
	 * @return The parsed document.
	 * 
	 * @throws Exception
	 *             In case the parsing fails.
	 */
	protected org.w3c.dom.Document parseResponse(byte[] responseBody)
			throws Exception {
		// Parse the HTML response to be able to find errors.
		// Disable most Cobra logging.
		UserAgentContext uacontext = new SimpleUserAgentContext();

		// In this case we will use a standard XML document
		// as opposed to Cobra's HTML DOM implementation.
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		ByteArrayInputStream bais = new ByteArrayInputStream(responseBody);
		org.w3c.dom.Document document = builder.newDocument();

		// Here is where we use Cobra's HTML parser.
		HtmlParser parser = new HtmlParser(uacontext, document);
		parser.parse(bais);

		return document;
	}
}
