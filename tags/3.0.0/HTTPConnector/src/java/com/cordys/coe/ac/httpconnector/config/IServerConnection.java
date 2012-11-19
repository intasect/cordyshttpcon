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

import java.net.URL;

import java.util.Map;

import org.apache.commons.httpclient.HttpClient;

/**
 * This interface describes the information for a specific HTTP connection.
 * 
 * @author pgussow
 */
public interface IServerConnection {
	/**
	 * Holds the name for the attribute 'id'.
	 */
	String ATTRIBUTE_ID = "id";
	/**
	 * Contains the default HTTP request timeout in milliseconds.
	 */
	int DEFAULT_TIMEOUT = 30000;
	/**
	 * Holds the name for the tag 'authenticate-always'.
	 */
	String TAG_AUTHENTICATE_ALWAYS = "authenticate-always";
	/**
	 * Holds the name for the tag 'check-certificate'.
	 */
	String TAG_CHECK_CERTIFICATE = "check-certificate";
	/**
	 * Holds the name of the tag 'parameter'.
	 */
	String TAG_PARAMETER = "parameter";
	/**
	 * Holds the name of the tag 'parameters'.
	 */
	String TAG_PARAMETERS = "parameters";
	/**
	 * Holds the name for the tag 'password'.
	 */
	String TAG_PASSWORD = "password";
	/**
	 * Holds the name for the tag 'proxy-host'.
	 */
	String TAG_PROXY_HOST = "proxy-host";
	/**
	 * Holds the name for the tag 'proxy-password'.
	 */
	String TAG_PROXY_PASSWORD = "proxy-password";
	/**
	 * Holds the name for the tag 'proxy-port'.
	 */
	String TAG_PROXY_PORT = "proxy-port";
	/**
	 * Holds the name for the tag 'proxy-username'.
	 */
	String TAG_PROXY_USERNAME = "proxy-username";
	/**
	 * Holds the name for the tag 'timeout'.
	 */
	String TAG_TIMEOUT = "timeout";
	/**
	 * Holds the name for the tag 'url'.
	 */
	String TAG_URL = "url";
	/**
	 * Holds the name for the tag 'username'.
	 */
	String TAG_USERNAME = "username";

	/**
	 * Returns the client.
	 * 
	 * @return Returns the client.
	 */
	HttpClient getHttpClient();

	/**
	 * Returns the id.
	 * 
	 * @return Returns the id.
	 */
	String getId();

	/**
	 * This method gets the parameter with the given name. If the parameter is
	 * not found null is returned.
	 * 
	 * @param name
	 *            The name of the parameter to retrieve.
	 * 
	 * @return The parameter with the given name. If the parameter is not found
	 *         null is returned.
	 */
	IParameter getParameter(String name);

	/**
	 * This method gets the list of parameters. This is a copy of the internal
	 * list.
	 * 
	 * @return The list of parameters. This is a copy of the internal list.
	 */
	Map<String, IParameter> getParameters();

	/**
	 * Returns the password.
	 * 
	 * @return Returns the password.
	 */
	String getPassword();

	/**
	 * Returns the timeout.
	 * 
	 * @return Timeout value.
	 */
	int getTimeout();

	/**
	 * Returns the url.
	 * 
	 * @return Returns the url.
	 */
	URL getUrl();

	/**
	 * Returns the username.
	 * 
	 * @return Returns the username.
	 */
	String getUsername();

	/**
	 * This method returns whether or not this connection has a parameter with
	 * the given name.
	 * 
	 * @param name
	 *            The name of the parameter to check.
	 * 
	 * @return true if this connection has a parameter with the given name.
	 *         Otherwise false.
	 */
	boolean hasParameter(String name);

	/**
	 * Returns the checkServerCertificate.
	 * 
	 * @return Returns the checkServerCertificate.
	 */
	boolean isCheckServerCertificate();

	/**
	 * Initializes the connection.
	 */
	void open();

	/**
	 * Sets the checkServerCertificate.
	 * 
	 * @param checkServerCertificate
	 *            The checkServerCertificate to be set.
	 */
	void setCheckServerCertificate(boolean checkServerCertificate);

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            The id to be set.
	 */
	void setId(String id);

	/**
	 * Sets the password.
	 * 
	 * @param password
	 *            The password to be set.
	 */
	void setPassword(String password);

	/**
	 * Sets the url.
	 * 
	 * @param url
	 *            The url to be set.
	 */
	void setUrl(URL url);

	/**
	 * Sets the username.
	 * 
	 * @param username
	 *            The username to be set.
	 */
	void setUsername(String username);
}
