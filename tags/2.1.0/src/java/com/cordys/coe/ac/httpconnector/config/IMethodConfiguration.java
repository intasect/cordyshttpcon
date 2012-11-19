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

import com.cordys.coe.ac.httpconnector.IRequestHandler;
import com.cordys.coe.ac.httpconnector.IResponseHandler;

import com.eibus.xml.xpath.XPath;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This method describes the method configuration.
 *
 * @author  pgussow
 */
public interface IMethodConfiguration
{
    /**
     * Holds the name of the attribute 'class'.
     */
    String ATTRIBUTE_CLASS = "class";
    /**
     * Holds the name of the attribute 'prefix'.
     */
    String ATTRIBUTE_PREFIX = "prefix";
    /**
     * Holds the name of the tag 'binding'.
     */
    String TAG_BINDING = "binding";
    /**
     * Holds the name of the tag 'clean-response-body'.
     */
    String TAG_CLEAN_RESPONSE_BODY = "clean-response-body";
    /**
     * Holds the name of the tag 'connection-id'.
     */
    String TAG_CONNECTION_ID = "connection-id";
    /**
     * Holds the name of the tag 'http-method'.
     */
    String TAG_HTTP_METHOD = "http-method";
    /**
     * Holds the name of the tag 'implementation'.
     */
    String TAG_IMPLEMENTATION = "implementation";
    /**
     * Holds the name of the tag 'namespaces'.
     */
    String TAG_NAMESPACES = "namespaces";
    /**
     * Holds the name of the tag 'http-method'.
     */
    String TAG_REQUEST_HANDLER = "request-handler";
    /**
     * Holds the name of the tag 'response-handler'.
     */
    String TAG_RESPONSE_HANDLER = "response-handler";
    /**
     * Holds the name of the tag 'uri'.
     */
    String TAG_URI = "uri";
    /**
     * Holds the name of the tag 'valid-response-code'.
     */
    String TAG_VALID_RESPONSE_CODE = "valid-response-code";
    /**
     * Holds the name of the tag 'valid-response-xpath'.
     */
    String TAG_VALID_RESPONSE_XPATH = "valid-response-xpath";

    /**
     * Returns the connectionId.
     *
     * @return  Returns the connectionId.
     */
    String getConnectionId();

    /**
     * Returns the httpMethodType.
     *
     * @return  Returns the httpMethodType.
     */
    EHttpMethod getHttpMethodType();

    /**
     * Returns the requestHandler.
     *
     * @return  Returns the requestHandler.
     */
    IRequestHandler getRequestHandler();

    /**
     * Returns the responseHandler.
     *
     * @return  Returns the responseHandler.
     */
    IResponseHandler getResponseHandler();

    /**
     * Returns the uri.
     *
     * @return  Returns the uri.
     */
    String getUri();

    /**
     * Returns the validResponseCode.
     *
     * @return  Returns the validResponseCode.
     */
    int getValidResponseCode();

    /**
     * Returns the validResponseXPath.
     *
     * @return  Returns the validResponseXPath.
     */
    XPath getValidResponseXPath();

    /**
     * Returns the xpathInfo.
     *
     * @return  Returns the xpathInfo.
     */
    XPathMetaInfo getXPathMetaInfo();

    /**
     * Returns the cleanResponseBody.
     *
     * @return  Returns the cleanResponseBody.
     */
    boolean isCleanResponseBody();
}
