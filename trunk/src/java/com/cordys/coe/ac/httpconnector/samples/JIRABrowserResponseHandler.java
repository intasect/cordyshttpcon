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

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpMethod;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class gets the users from the response and puts them into an XML structure.
 *
 * @author  pgussow
 */
public class JIRABrowserResponseHandler extends JIRAResponseHandler
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(JIRABrowserResponseHandler.class);

    /**
     * @see  com.cordys.coe.ac.httpconnector.samples.JIRAResponseHandler#buildXMLResponse(int,org.apache.commons.httpclient.HttpMethod,
     *       org.w3c.dom.Document, com.eibus.xml.nom.Document)
     */
    @Override protected void buildXMLResponse(int resNode, HttpMethod httpMethod,
                                              org.w3c.dom.Document document, Document doc)
                                       throws Exception
    {
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xpath.evaluate("//table[@class='grid']/tr[@class='vcard']",
                                                      document, XPathConstants.NODESET);

        int nrOfUsers = nodeList.getLength();

        if (nrOfUsers > 0)
        {
            for (int count = 0; count < nrOfUsers; count++)
            {
                Node userNode = nodeList.item(count);

                int tuple = doc.createElementWithParentNS("tuple", null, resNode);

                int old = doc.createElementWithParentNS("old", null, tuple);
                int user = doc.createElementWithParentNS("user", null, old);

                // Get the name
                String username = (String) xpath.evaluate(".//span[@class='username']/text()",
                                                          userNode, XPathConstants.STRING);

                doc.createElementWithParentNS("username", username, user);

                // Get the email address
                String emailAddress = (String) xpath.evaluate(".//span[@class='email']/text()",
                                                              userNode, XPathConstants.STRING);

                doc.createElementWithParentNS("email", emailAddress, user);

                // Get the full name
                String fn = (String) xpath.evaluate(".//span[@class='fn']/text()", userNode,
                                                    XPathConstants.STRING);

                doc.createElementWithParentNS("fullname", fn, user);

                // Get the groups for this user
                NodeList nl = (NodeList) xpath.evaluate(".//td/a[../br]/text()", userNode,
                                                        XPathConstants.NODESET);
                int groups = doc.createElementWithParentNS("groups", null, user);

                for (int groupsCount = 0; groupsCount < nl.getLength(); groupsCount++)
                {
                    String group = nl.item(groupsCount).getNodeValue();
                    doc.createElementWithParentNS("group", group, groups);
                }
            }
        }
        else if (LOG.isDebugEnabled())
        {
            LOG.debug("No users found");
        }
    }
}
