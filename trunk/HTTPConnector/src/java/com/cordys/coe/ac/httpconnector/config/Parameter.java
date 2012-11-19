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
package com.cordys.coe.ac.httpconnector.config;

import java.util.Locale;

import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.cordys.coe.ac.httpconnector.exception.ConnectorExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;
import com.cordys.util.Base64Encoding;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class implements the connection parameters that can be freely
 * configured.
 * 
 * @author pgussow
 */
class Parameter implements IParameter {
	/**
	 * Holds the name for this parameter.
	 */
	private String m_name;
	/**
	 * Holds the type of parameter.
	 */
	private EParameterType m_type;
	/**
	 * Holds the actual value for this parameter.
	 */
	private Object m_value = null;

	/**
	 * Creates a new Parameter object.
	 * 
	 * @param configuration
	 *            The configuration XML.
	 * @param xmi
	 *            The XPathMetaInfo.
	 * 
	 * @throws ConnectorException
	 *             In case of any exceptions.
	 */
	Parameter(int configuration, XPathMetaInfo xmi) throws ConnectorException {
		// Parse and check the name of the parameter
		m_name = XPathHelper.getStringValue(configuration, "ns:" + TAG_NAME,
				xmi, "");

		if (m_name.length() == 0) {
			throw new ConnectorException(
					ConnectorExceptionMessages.THE_NAME_OF_THE_PARAMETER_MUST_BE_FILLED);
		}

		// Parse the type.
		String type = Node.getAttribute(configuration, ATTRIBUTE_TYPE,
				EParameterType.STRING.name());
		m_type = EParameterType.valueOf(type.toUpperCase(Locale.US));

		// Now get the value.
		boolean decode = false;

		switch (m_type) {
		case ENCODED:
			decode = true;

		case STRING:

			String tempString = XPathHelper.getStringValue(configuration, "ns:"
					+ TAG_VALUE, xmi, "");
			if (tempString.length() == 0) {
				throw new ConnectorException(
						ConnectorExceptionMessages.THE_VALUE_FOR_PARAMETER_0_IS_EMPTY,
						m_name);
			}
			m_value = tempString;

			if (decode) {
				byte[] bytes = tempString.getBytes();
				m_value = new String(new Base64Encoding().decode(bytes));
			}

			break;

		case XML:

			int tempInt = XPathHelper.selectSingleNode(configuration, "ns:"
					+ TAG_VALUE + "/*", xmi);
			if (tempInt == 0) {
				throw new ConnectorException(
						ConnectorExceptionMessages.THE_PARAMETER_0_HAS_NO_CHILD_ELEMENT,
						m_name);
			}
			// The node is cloned. People SHOULD explicitly call the clean()
			// method, but there
			// is a fallback scenario using the finalizer.
			m_value = Node.duplicate(tempInt);
			break;
		}
	}

	/**
	 * @see IParameter#clean()
	 */
	@Override
	public void clean() {
		if ((m_type == EParameterType.XML) && (m_value != null)) {
			int temp = (Integer) m_value;

			if (temp != 0) {
				Node.delete(temp);
			}
			m_value = null;
		}
	}

	/**
	 * @see IParameter#getName()
	 */
	@Override
	public String getName() {
		return m_name;
	}

	/**
	 * @see IParameter#getType()
	 */
	@Override
	public EParameterType getType() {
		return m_type;
	}

	/**
	 * @see IParameter#getValue()
	 */
	@Override
	public Object getValue() {
		return m_value;
	}

	/**
	 * @see IParameter#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		m_name = name;
	}

	/**
	 * @see IParameter#setType(com.cordys.coe.ac.httpconnector.config.EParameterType)
	 */
	@Override
	public void setType(EParameterType type) {
		m_type = type;
	}

	/**
	 * @see IParameter#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Object value) {
		m_value = value;
	}

	/**
	 * This is a fall-back scenario if someone forgets to call the clean method.
	 * 
	 * @throws Throwable
	 *             In case the clean fails.
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		clean();

		super.finalize();
	}
}
