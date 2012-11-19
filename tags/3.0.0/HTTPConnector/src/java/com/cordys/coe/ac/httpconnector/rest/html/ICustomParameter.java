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

import com.cordys.coe.ac.httpconnector.config.IMethodConfiguration;
import com.cordys.coe.ac.httpconnector.config.IServerConnection;
import com.cordys.coe.ac.httpconnector.exception.HandlerException;

/**
 * This interface describes a custom parameter. A custom parameter will do
 * complex logic to create post data based on the request.
 * 
 * @author pgussow
 */
public interface ICustomParameter {
	/**
	 * This method should create the additional post data based on the .
	 * 
	 * @param rhp
	 *            The parameter definition.
	 * @param requestNode
	 *            The actual request XML.
	 * @param methodConfiguration
	 *            The configuration of the currently executing method.
	 * @param serverConnection
	 *            The server connection that is being used.
	 * 
	 * @return The additional post data. Note that the implementing method
	 *         should make sure everything is properly encoded.
	 * 
	 * @throws HandlerException
	 *             In case of any exceptions.
	 */
	String getPostData(IRestHTMLParameter rhp, int requestNode,
			IMethodConfiguration methodConfiguration,
			IServerConnection serverConnection) throws HandlerException;
}
