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
 package com.cordys.coe.ac.httpconnector.execution;

import com.cordys.coe.ac.httpconnector.IRequestHandler;
import com.cordys.coe.ac.httpconnector.IResponseHandler;
import com.cordys.coe.ac.httpconnector.config.IMethodConfiguration;
import com.cordys.coe.ac.httpconnector.config.IServerConnection;
import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.cordys.coe.ac.httpconnector.exception.ConnectorExceptionMessages;
import com.cordys.coe.ac.httpconnector.exception.HandlerException;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * This class actually executes the request. This class decouples the application connector
 * framework from the actual execution of the HTTP request.
 *
 * @author  pgussow
 */
public class MethodExecutor
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(MethodExecutor.class);

    /**
     * Send the HTTP request to the web server and returns the response.
     *
     * @param   reqNode           Request XML node.
     * @param   serverConnection  Server connection information.
     * @param   methodInfo        Method information information.
     *
     * @return  Response XML node.
     *
     * @throws  ConnectorException
     * @throws  HandlerException    In case the handlers throw an exception.
     */
    public static int sendRequest(int reqNode, IServerConnection serverConnection,
                                  IMethodConfiguration methodInfo)
                           throws ConnectorException, HandlerException
    {
        // Get request and response handlers for converting the data.
        IRequestHandler requestHandler = methodInfo.getRequestHandler();
        IResponseHandler responseHandler = methodInfo.getResponseHandler();

        // Create the connection
        HttpClient client = serverConnection.getHttpClient();

        client.getParams().setContentCharset("UTF-8");
        client.getParams().setCredentialCharset("UTF-8");
        client.getParams().setHttpElementCharset("UTF-8");

        HttpMethod httpMethod;
        int timeout = serverConnection.getTimeout();

        // Convert the SOAP request XML into an HTTP request.
        httpMethod = requestHandler.process(reqNode, serverConnection, client);
        
        // Set follow redirects
      	httpMethod.setFollowRedirects(true);

        // Set additional HTTP parameters.
        HttpMethodParams hmpMethodParams = httpMethod.getParams();

        if (hmpMethodParams != null)
        {
            // Set the authentication character set to UTF-8, if not set.
            String sCredCharset = hmpMethodParams.getCredentialCharset();

            if ((sCredCharset == null) || (sCredCharset.length() == 0))
            {
                hmpMethodParams.setCredentialCharset("UTF-8");
            }

            if (timeout > 0)
            {
                hmpMethodParams.setSoTimeout(timeout);
            }
        }

        // Send the request and handle the response.
        try
        {
            int statusCode = client.executeMethod(httpMethod);
            int validStatusCode = methodInfo.getValidResponseCode();

            if (statusCode != HttpStatus.SC_OK)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Received HTTP error code: " + statusCode);
                }
            }

            // Convert the response into XML.
            int responseNode = responseHandler.convertResponseToXml(httpMethod, serverConnection,
                                                                    Node.getDocument(reqNode));

            if ((validStatusCode >= 0) && (statusCode != validStatusCode))
            {
                if (responseNode != 0)
                {
                    Node.delete(responseNode);
                    responseNode = 0;
                }

                throw new ConnectorException(ConnectorExceptionMessages.INVALID_STATUS_CODE_RECEIVED_0_EXPECTED_1,
                                             statusCode, validStatusCode);
            }

            return responseNode;
        }
        catch (ConnectorException e)
        {
            throw e;
        }
        catch (HttpException e)
        {
            throw new ConnectorException(e, ConnectorExceptionMessages.HTTP_REQUEST_FAILED_0,
                                         e.getMessage());
        }
        catch (IOException e)
        {
            throw new ConnectorException(e, ConnectorExceptionMessages.HTTP_CONNECTION_FAILED_0,
                                         e.getMessage());
        }
        catch (XMLException e)
        {
            throw new ConnectorException(e,
                                         ConnectorExceptionMessages.INVALID_RESPONSE_XML_RECEIVED);
        }
        finally
        {
            // Release the connection.
            httpMethod.releaseConnection();
        }
    }
}
