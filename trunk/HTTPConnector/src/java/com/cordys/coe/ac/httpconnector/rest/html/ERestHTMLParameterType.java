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

/**
 * Holds teh different types of parameters.
 * 
 * @author pgussow
 */
public enum ERestHTMLParameterType {
	CONNECTION_URI("connection-uri"), XPATH("xpath"), XPATH_MULTI("xpath_multi"), FIXED(
			"fixed"), CONNECTION_PARAMETER("connection-parameter"), CUSTOM(
			"custom");

	/**
	 * Holds the configuration name for the URI parameter type.
	 */
	private String configName;

	/**
	 * Creates a new EUriParameterType object.
	 * 
	 * @param configName
	 *            The configuration name.
	 */
	private ERestHTMLParameterType(String configName) {
		this.configName = configName;
	}

	/**
	 * This method gets the proper enum based on the configuration value.
	 * 
	 * @param configValue
	 *            The configuration value.
	 * 
	 * @return The matching enum. If the configValue is not found null is
	 *         returned.
	 */
	public static ERestHTMLParameterType getByConfigName(String configValue) {
		for (ERestHTMLParameterType type : values()) {
			if (type.configName.equals(configValue)) {
				return type;
			}
		}

		return null;
	}
}
