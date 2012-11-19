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
 * Holds the definition of Rest HTML parameters.
 * 
 * @author pgussow
 */
public interface IRestHTMLParameter {
	/**
	 * This method gets the default value for the parameter.
	 * 
	 * @return The default value for the parameter.
	 */
	String getDefaultValue();

	/**
	 * This method gets the destination for this parameter.
	 * 
	 * @return The destination for this parameter.
	 */
	EDestination getDestination();

	/**
	 * This method gets the name of the parameter as it should be in the URL.
	 * 
	 * @return The name of the parameter as it should be in the URL.
	 */
	String getName();

	/**
	 * This method gets whether or not the parameter is optional.
	 * 
	 * @return Whether or not the parameter is optional.
	 */
	boolean getOptional();

	/**
	 * This method gets the type of parameter.
	 * 
	 * @return The type of parameter.
	 */
	ERestHTMLParameterType getType();

	/**
	 * This method gets the value of the parameter.
	 * 
	 * @return The value of the parameter.
	 */
	String getValue();

	/**
	 * This method returns whether or not the parameter has a default value.
	 * 
	 * @return Whether or not the parameter has a default value.
	 */
	boolean hasDefaultValue();

	/**
	 * This method sets the default value for the parameter.
	 * 
	 * @param defaultValue
	 *            The default value for the parameter.
	 */
	void setDefaultValue(String defaultValue);

	/**
	 * This method sets the destination for this parameter.
	 * 
	 * @param destination
	 *            The destination for this parameter.
	 */
	void setDestination(EDestination destination);

	/**
	 * This method sets the name of the parameter as it should be in the URL.
	 * 
	 * @param name
	 *            The name of the parameter as it should be in the URL.
	 */
	void setName(String name);

	/**
	 * This method sets whether or not the parameter is optional.
	 * 
	 * @param optional
	 *            Whether or not the parameter is optional.
	 */
	void setOptional(boolean optional);

	/**
	 * This method sets the type of parameter.
	 * 
	 * @param type
	 *            The type of parameter.
	 */
	void setType(ERestHTMLParameterType type);

	/**
	 * This method sets the value of the parameter.
	 * 
	 * @param value
	 *            The value of the parameter.
	 */
	void setValue(String value);
}
