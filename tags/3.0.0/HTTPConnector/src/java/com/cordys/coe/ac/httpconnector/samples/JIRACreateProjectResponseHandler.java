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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;

import org.w3c.dom.Document;

/**
 * This class handles the response from the create project. the most important
 * thig is to figure out the project ID.
 * 
 * @author pgussow
 */
public class JIRACreateProjectResponseHandler extends JIRAResponseHandler {
	/**
	 * Holds the pattern to use.
	 */
	private static final Pattern PATTERN_PID = Pattern
			.compile("^http://.+/secure/admin.user/ViewProject\\.jspa\\?pid=(\\d+)$");

	/**
	 * @see com.cordys.coe.ac.httpconnector.samples.JIRAResponseHandler#buildXMLResponse(int,org.w3c.dom.Document,
	 *      com.eibus.xml.nom.Document)
	 */
	@Override
	protected void buildXMLResponse(int resNode, HttpMethod httpMethod,
			Document document, com.eibus.xml.nom.Document doc) throws Exception {
		if (httpMethod.getStatusCode() == 302) {
			// It's a redirect to the details screen for the newly created
			// project. We'll follow
			// the redirect.
			Header location = httpMethod.getResponseHeader("Location");

			if (location != null) {
				String loc = location.getValue();
				// Get the PID via a regex
				Matcher m = PATTERN_PID.matcher(loc);

				if (m.find()) {
					loc = m.group(1);

					// Now we need to get the project ID from the URL.
					int tuple = doc.createElementWithParentNS("tuple", null,
							resNode);
					int old = doc.createElementWithParentNS("old", null, tuple);
					int project = doc.createElementWithParentNS("project",
							null, old);

					doc.createElementWithParentNS("projectid", loc, project);
				}
			}
		}
	}
}
