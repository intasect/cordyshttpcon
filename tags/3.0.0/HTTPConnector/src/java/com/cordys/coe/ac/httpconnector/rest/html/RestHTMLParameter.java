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

import com.cordys.coe.ac.httpconnector.exception.HandlerException;
import com.cordys.coe.ac.httpconnector.exception.HandlerExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class can parse the rest HTML parameters.
 * 
 * @author pgussow
 */
class RestHTMLParameter implements IRestHTMLParameter {
	/**
	 * Holds the default value for the parameter.
	 */
	private String m_defaultValue;
	/**
	 * Holds the destination for this parameter.
	 */
	private EDestination m_destination;
	/**
	 * Holds the name of the parameter as it should be in the URL.
	 */
	private String m_name;
	/**
	 * Holds whether or not the parameter is optional.
	 */
	private boolean m_optional;
	/**
	 * Holds the type of parameter.
	 */
	private ERestHTMLParameterType m_type;
	/**
	 * Holds the value of the parameter.
	 */
	private String m_value;

	/**
	 * Creates a new RestHtmlParameter object.
	 * 
	 * @param parameter
	 *            The parameter XML.
	 * @param xmi
	 *            The XPathMetaInfo object with the ns prefix mapped to the
	 *            proper namespace.
	 * 
	 * @throws HandlerException
	 *             In case of any exceptions.
	 */
	RestHTMLParameter(int parameter, XPathMetaInfo xmi) throws HandlerException {
		// Get the name of the request parameter
		m_name = XPathHelper.getStringValue(parameter, "ns:" + "name", xmi, "");

		if (m_name.length() == 0) {
			throw new HandlerException(
					HandlerExceptionMessages.FOR_THE_RESTHTMLREQUESTHANDLER_THE_TAG_NAME_MUST_BE_FILLED);
		}

		// Get whether or not the parameter is optional.
		m_optional = XPathHelper.getBooleanValue(parameter, "ns:" + "optional",
				xmi, false);

		// Get the destination.
		String temp = XPathHelper.getStringValue(parameter,
				"ns:" + "destination", xmi, "").toUpperCase();

		if (temp.length() == 0) {
			throw new HandlerException(
					HandlerExceptionMessages.FOR_THE_RESTHTMLREQUESTHANDLER_THE_TAG_DESTINATION_MUST_BE_FILLED);
		}
		m_destination = EDestination.valueOf(temp);

		// Get the parameter type.
		temp = XPathHelper.getStringValue(parameter, "ns:" + "type", xmi, "");

		if (temp.length() == 0) {
			throw new HandlerException(
					HandlerExceptionMessages.FOR_THE_RESTHTMLREQUESTHANDLER_THE_TAG_DESTINATION_MUST_BE_FILLED);
		}
		m_type = ERestHTMLParameterType.getByConfigName(temp);

		// Get the actual value
		m_value = XPathHelper.getStringValue(parameter, "ns:" + "value", xmi,
				"");

		if ((m_optional == false) && (m_value.length() == 0)) {
			throw new HandlerException(
					HandlerExceptionMessages.THE_VALUE_MUST_BE_FILLED_SINCE_THIS_IS_A_MANDATORY_PARAMETER,
					m_name);
		}

		// Get the default value
		m_defaultValue = XPathHelper.getStringValue(parameter, "ns:"
				+ "default", xmi, null);

		if (m_defaultValue == null) {
			int tmp = XPathHelper.selectSingleNode(parameter,
					"ns:" + "default", xmi);

			if (tmp != 0) {
				m_defaultValue = "";
			}
		}
	}

	/**
	 * @see com.cordys.coe.ac.httpconnector.rest.html.IRestHTMLParameter#getDefaultValue()
	 */
	@Override
	public String getDefaultValue() {
		return m_defaultValue;
	}

	/**
	 * @see com.cordys.coe.ac.httpconnector.rest.html.IRestHTMLParameter#getDestination()
	 */
	@Override
	public EDestination getDestination() {
		return m_destination;
	}

	/**
	 * @see com.cordys.coe.ac.httpconnector.rest.html.IRestHTMLParameter#getName()
	 */
	@Override
	public String getName() {
		return m_name;
	}

	/**
	 * @see com.cordys.coe.ac.httpconnector.rest.html.IRestHTMLParameter#getOptional()
	 */
	@Override
	public boolean getOptional() {
		return m_optional;
	}

	/**
	 * @see com.cordys.coe.ac.httpconnector.rest.html.IRestHTMLParameter#getType()
	 */
	@Override
	public ERestHTMLParameterType getType() {
		return m_type;
	}

	/**
	 * @see com.cordys.coe.ac.httpconnector.rest.html.IRestHTMLParameter#getValue()
	 */
	@Override
	public String getValue() {
		return m_value;
	}

	/**
	 * @see com.cordys.coe.ac.httpconnector.rest.html.IRestHTMLParameter#hasDefaultValue()
	 */
	@Override
	public boolean hasDefaultValue() {
		return m_defaultValue != null;
	}

	/**
	 * @see com.cordys.coe.ac.httpconnector.rest.html.IRestHTMLParameter#setDefaultValue(java.lang.String)
	 */
	@Override
	public void setDefaultValue(String defaultValue) {
		m_defaultValue = defaultValue;
	}

	/**
	 * @see com.cordys.coe.ac.httpconnector.rest.html.IRestHTMLParameter#setDestination(com.cordys.coe.ac.httpconnector.rest.html.EDestination)
	 */
	@Override
	public void setDestination(EDestination destination) {
		m_destination = destination;
	}

	/**
	 * @see com.cordys.coe.ac.httpconnector.rest.html.IRestHTMLParameter#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		m_name = name;
	}

	/**
	 * @see com.cordys.coe.ac.httpconnector.rest.html.IRestHTMLParameter#setOptional(boolean)
	 */
	@Override
	public void setOptional(boolean optional) {
		m_optional = optional;
	}

	/**
	 * @see com.cordys.coe.ac.httpconnector.rest.html.IRestHTMLParameter#setType(com.cordys.coe.ac.httpconnector.rest.html.ERestHTMLParameterType)
	 */
	@Override
	public void setType(ERestHTMLParameterType type) {
		m_type = type;
	}

	/**
	 * @see com.cordys.coe.ac.httpconnector.rest.html.IRestHTMLParameter#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value) {
		m_value = value;
	}
}
