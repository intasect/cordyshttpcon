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
package com.cordys.coe.ac.httpconnector;

/**
 * This class contains some static namespace definitions.
 * 
 * @author pgussow
 */
public class HttpConnectorConstants {
	/**
	 * Holds the namespace for the configuration XML in the XML store.
	 */
	public static final String NS_HTTP_CONNECTOR_CONF_2_0 = "http://httpconnector.coe.cordys.com/2.0/configuration";
	/**
	 * Holds the namespace for the configuration XML for the implementation.
	 */
	public static final String NS_HTTP_CONNECTOR_IMPL_2_0 = "http://httpconnector.coe.cordys.com/2.0/implementation";
	/**
	 * Holds the namespace for the handler error messages.
	 */
	public static final String NS_HTTP_CONNECTOR_HANDLER_ERROR_2_0 = "http://httpconnector.coe.cordys.com/2.0/handler/error";
	/**
	 * Holds the namespace for XML Store connector
	 */
	public static final String NS_XML_STORE = "http://schemas.cordys.com/1.0/xmlstore";

	public static final String NS_SOAP = "http://schemas.xmlsoap.org/soap/envelope/";

	public static final String GET_XML_OBJECT = "GetXMLObject";

	public static final String GET_XML_OBJECT_RESPONSE = "GetXMLObjectResponse";

	public static final String HTTPS_PROTOCOL_PREFIX = "https";

	public static final String VERSION = "version";

	public static final String ISV_VERSION = "isv";

	public static final String ORGANIZATION_VERSION = "organization";

	public static final String ISV_SPACE = "cordys_system_space";

}
