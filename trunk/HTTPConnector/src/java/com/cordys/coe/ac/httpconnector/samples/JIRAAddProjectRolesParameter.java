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

import com.cordys.coe.ac.httpconnector.config.IMethodConfiguration;
import com.cordys.coe.ac.httpconnector.config.IServerConnection;
import com.cordys.coe.ac.httpconnector.exception.HandlerException;
import com.cordys.coe.ac.httpconnector.exception.HandlerExceptionMessages;
import com.cordys.coe.ac.httpconnector.rest.html.ICustomParameter;
import com.cordys.coe.ac.httpconnector.rest.html.IRestHTMLParameter;
import com.cordys.coe.util.xml.nom.XPathHelper;

import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

/**
 * This class makes the complex post data for adding project roles to a certain
 * user.
 * 
 * @author pgussow
 */
public class JIRAAddProjectRolesParameter implements ICustomParameter {
	/**
	 * @see ICustomParameter#getPostData(IRestHTMLParameter, int,
	 *      IMethodConfiguration, IServerConnection)
	 */
	@Override
	public String getPostData(IRestHTMLParameter rhp, int requestNode,
			IMethodConfiguration methodConfiguration,
			IServerConnection serverConnection) throws HandlerException {
		StringBuilder returnValue = new StringBuilder(1024);

		int[] projects = XPathHelper.selectNodes(requestNode,
				"ns:projects/ns:project",
				methodConfiguration.getXPathMetaInfo());

		for (int project : projects) {
			String projectid = XPathHelper.getStringValue(project,
					"ns:projectid", methodConfiguration.getXPathMetaInfo(), "");

			if (projectid.length() == 0) {
				throw new HandlerException(
						HandlerExceptionMessages.MISSING_PROJECT_ID);
			}

			int[] roles = XPathHelper.selectNodes(project, "ns:role",
					methodConfiguration.getXPathMetaInfo());

			if (roles.length > 0) {
				// We'll include it.
				returnValue.append("project_shown=").append(encode(projectid));

				for (int role : roles) {
					String roleid = XPathHelper.getStringValue(role, "ns:id",
							methodConfiguration.getXPathMetaInfo(), "");
					String newValue = XPathHelper.getStringValue(role,
							"ns:new", methodConfiguration.getXPathMetaInfo(),
							"");
					String oldValue = XPathHelper.getStringValue(role,
							"ns:old", methodConfiguration.getXPathMetaInfo(),
							"");

					if (roleid.trim().length() == 0) {
						throw new HandlerException(
								HandlerExceptionMessages.THE_ROLE_ID_MUST_BE_FILLED);
					}

					if (newValue.trim().length() == 0) {
						throw new HandlerException(
								HandlerExceptionMessages.THE_ROLE_VALUE_MUST_BE_FILLED);
					}

					if (oldValue.trim().length() == 0) {
						throw new HandlerException(
								HandlerExceptionMessages.THE_OLD_ROLE_VALUE_MUST_BE_FILLED);
					}

					boolean turningOn = newValue.equalsIgnoreCase("on");
					boolean wasTurnedOn = oldValue.equalsIgnoreCase("on");

					String postVarname = projectid + "_" + roleid;

					// Add the post data for it.
					if (!((wasTurnedOn == true) && (turningOn == false))) {
						returnValue.append("&").append(postVarname).append("=");
						returnValue.append((turningOn ? "on" : "off"));
					}

					// Add the orig (for now always the opposite to make sure
					// they are really set.
					returnValue.append("&").append(postVarname)
							.append("_orig=");
					returnValue.append(((wasTurnedOn) ? "true" : "false"));
				}
			}
		}

		return returnValue.toString();
	}

	/**
	 * This method encodes the data.
	 * 
	 * @param data
	 * 
	 * @return The encoded data.
	 */
	private String encode(String data) {
		try {
			return URLEncoder.encode(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return data;
		}
	}
}
