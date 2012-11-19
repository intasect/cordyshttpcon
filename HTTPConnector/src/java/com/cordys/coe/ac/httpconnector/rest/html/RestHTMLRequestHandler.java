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
package com.cordys.coe.ac.httpconnector.rest.html;

import com.cordys.coe.ac.httpconnector.IRequestHandler;
import com.cordys.coe.ac.httpconnector.config.IMethodConfiguration;
import com.cordys.coe.ac.httpconnector.config.IParameter;
import com.cordys.coe.ac.httpconnector.config.IServerConnection;
import com.cordys.coe.ac.httpconnector.config.IXSLTStore;
import com.cordys.coe.ac.httpconnector.exception.HandlerException;
import com.cordys.coe.ac.httpconnector.exception.HandlerExceptionMessages;
import com.cordys.coe.ac.httpconnector.impl.StandardRequestHandler;
import com.cordys.coe.ac.httpconnector.utils.Utils;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.net.URLEncoder;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;

/**
 * This request handler is used to handle HTML based requests.
 * 
 * @author pgussow
 */
public class RestHTMLRequestHandler extends StandardRequestHandler {
	/**
	 * Holds the logger to use.
	 */
	private static final CordysLogger LOG = CordysLogger
			.getCordysLogger(RestHTMLRequestHandler.class);
	/**
	 * Holds the name of the tag 'parameters'.
	 */
	private static final String TAG_PARAMETERS = "parameters";
	/**
	 * Holds the name of the tag 'parameter'.
	 */
	private static final String TAG_PARAMETER = "parameter";
	/**
	 * Holds all parameters for the body.
	 */
	private Map<String, IRestHTMLParameter> m_bodyParameters = new LinkedHashMap<String, IRestHTMLParameter>();
	/**
	 * Holds all parameters for the query string .
	 */
	private Map<String, IRestHTMLParameter> m_requestParameters = new LinkedHashMap<String, IRestHTMLParameter>();

	/**
	 * @see IRequestHandler#initialize(int, IXSLTStore, IMethodConfiguration,
	 *      XPathMetaInfo)
	 */
	@Override
	public void initialize(int configXml, IXSLTStore connector,
			IMethodConfiguration method, XPathMetaInfo xmi)
			throws HandlerException {
		super.initialize(configXml, connector, method, xmi);

		// Parse the parameters
		int[] parameters = XPathHelper.selectNodes(configXml, "ns:"
				+ TAG_PARAMETERS + "/ns:" + TAG_PARAMETER, xmi);

		for (int parameter : parameters) {
			IRestHTMLParameter rhp = new RestHTMLParameter(parameter, xmi);

			switch (rhp.getDestination()) {
			case BODY:
				m_bodyParameters.put(rhp.getName(), rhp);
				break;

			case REQUEST:
				m_requestParameters.put(rhp.getName(), rhp);
				break;
			}
		}
	}

	/**
	 * @see com.cordys.coe.ac.httpconnector.impl.StandardRequestHandler#getContentType()
	 */
	@Override
	protected String getContentType() {
		return "application/x-www-form-urlencoded";
	}

	/**
	 * @see com.cordys.coe.ac.httpconnector.impl.StandardRequestHandler#getPostData(int,
	 *      com.cordys.coe.ac.httpconnector.config.IServerConnection)
	 */
	@Override
	protected byte[] getPostData(int requestNode, IServerConnection connection)
			throws HandlerException {
		StringBuilder sb = new StringBuilder(1024);

		boolean first = true;

		for (IRestHTMLParameter rhp : m_bodyParameters.values()) {
			if (rhp.getType() == ERestHTMLParameterType.XPATH_MULTI) {
				int[] matches = XPathHelper.selectNodes(requestNode,
						rhp.getValue(), m_method.getXPathMetaInfo());

				if (matches.length > 0) {
					for (int count = 0; count < matches.length; count++) {
						String value = Node.getDataWithDefault(matches[count],
								"");

						if (!first) {
							sb.append("&");
						}
						sb.append(rhp.getName()).append("=");

						try {
							sb.append(URLEncoder.encode(value, "UTF-8"));
						} catch (Exception e) {
							// Can be ignored.
						}
						first = false;
					}
				}
			} else if (rhp.getType() == ERestHTMLParameterType.CUSTOM) {
				// A custom XML handler.
				try {
					Class<?> clazz = Class.forName(rhp.getValue());
					ICustomParameter cp = (ICustomParameter) clazz
							.newInstance();
					String additionalData = cp.getPostData(rhp, requestNode,
							m_method, connection);

					if ((additionalData != null)
							&& (additionalData.length() > 0)) {
						if (!first) {
							sb.append("&");
						} else {
							first = false;
						}

						sb.append(additionalData);
					}
				} catch (Exception e) {
					throw new HandlerException(
							e,
							HandlerExceptionMessages.ERROR_EXECUTING_CUSTOM_PARAMETER_FOR_CLASS_0,
							rhp.getValue());
				}
			} else {
				// Check if the parameter should be sent.
				boolean include = true;
				String value = rhp.getValue();

				switch (rhp.getType()) {
				case XPATH:
					value = XPathHelper.getStringValue(requestNode, value,
							m_method.getXPathMetaInfo(), rhp.getDefaultValue());
					break;

				case CONNECTION_URI:
					value = Utils.getUrlPath(connection.getUrl());
					break;

				case FIXED:
					// The value is the already the final value
					break;

				case CONNECTION_PARAMETER:

					IParameter connParam = connection.getParameter(value);
					if (connParam != null) {
						// In this case the XML parameters are not used since we
						// cannot put XML
						// in the request.
						value = connParam.getValue().toString();
					}
					break;
				default:
				}

				if ((value == null) || (value.length() == 0)) {
					if (rhp.getOptional() == false) {
						throw new HandlerException(
								HandlerExceptionMessages.THE_PARAMETER_0_HAS_NO_VALUE,
								rhp.getName());
					}

					// Now we need to decide whether or not we're going to
					// include this parameter
					// and what the final value is
					if (rhp.hasDefaultValue()) {
						value = rhp.getDefaultValue();
					} else {
						// It's an optional parameter without a default
						// parameter. Ignore it.
						include = false;
					}
				}

				if (include) {
					if (!first) {
						sb.append("&");
					}
					sb.append(rhp.getName()).append("=");

					try {
						sb.append(URLEncoder.encode(value, "UTF-8"));
					} catch (Exception e) {
						// Can be ignored.
					}
					first = false;
				}
			}
		}

		return sb.toString().getBytes();
	}

	/**
	 * @see com.cordys.coe.ac.httpconnector.impl.StandardRequestHandler#getRequestUri(int,com.cordys.coe.ac.httpconnector.config.IServerConnection,
	 *      org.apache.commons.httpclient.HttpClient)
	 */
	@Override
	protected String getRequestUri(int requestNode,
			IServerConnection connection, HttpClient httpClient)
			throws HandlerException {
		StringBuilder returnValue = new StringBuilder(1024);
		String uri = m_method.getUri();
		returnValue.append("/");

		if ((uri != null) && (uri.length() > 0)) {
			returnValue.append(uri).append("?");

			boolean first = true;

			for (IRestHTMLParameter rhp : m_requestParameters.values()) {
				boolean include = true;
				String value = rhp.getValue();

				switch (rhp.getType()) {
				case XPATH:
					value = XPathHelper.getStringValue(requestNode, value,
							m_method.getXPathMetaInfo(), rhp.getDefaultValue());
					break;

				case CONNECTION_URI:
					value = Utils.getUrlPath(connection.getUrl());
					break;

				case FIXED:
					// The value is the already the final value
					break;

				case CONNECTION_PARAMETER:

					IParameter connParam = connection.getParameter(value);
					if (connParam != null) {
						// In this case the XML parameters are not used since we
						// cannot put XML
						// in the request.
						value = connParam.getValue().toString();
					}
					break;
				default:
				}

				// Check if the default value should be passed on.
				if (value == null) {
					if (rhp.getOptional() == false) {
						throw new HandlerException(
								HandlerExceptionMessages.FOR_PARAMETER_0_NO_VALUE_COULD_BE_FOUND,
								rhp.getName());
					}

					if (rhp.hasDefaultValue()) {
						value = rhp.getDefaultValue();
					} else {
						include = false;
					}
				}

				// Add the parameter to the request.
				if (include) {
					if (!first) {
						returnValue.append("&");
					}
					returnValue.append(rhp.getName()).append("=");

					try {
						returnValue.append(URLEncoder.encode(value, "UTF-8"));
					} catch (Exception e) {
						// Can be ignored.
					}
					first = false;
				}
			}
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("REST request URI: " + returnValue.toString());
		}

		return returnValue.toString();
	}
}
