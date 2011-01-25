
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

import com.cordys.coe.ac.httpconnector.config.IMethodConfiguration;
import com.cordys.coe.ac.httpconnector.config.IServerConnection;
import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.cordys.coe.ac.httpconnector.exception.ConnectorExceptionMessages;
import com.cordys.coe.ac.httpconnector.exception.HandlerException;
import com.cordys.coe.ac.httpconnector.execution.MethodExecutor;
import com.cordys.coe.ac.httpconnector.utils.XmlUtils;
import com.cordys.coe.exception.ServerLocalizableException;
import com.cordys.coe.util.general.ExceptionUtil;

import com.eibus.connector.nom.Connector;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is the Implementation of ApplicationTransaction. This class will receive the request
 * process it if it is a valid one.
 */
public class HttpTransaction
    implements ApplicationTransaction
{
    /**
     * Contains the logger instance.
     */
    private static CordysLogger LOG = CordysLogger.getCordysLogger(HttpTransaction.class);
    /**
     * The request type by which the request is to be redirected to different classes.
     */
    private static final String SERVICE_TYPE = "HTTP";
    /**
     * Holds the application connector object for this transaction.
     */
    private HttpConnector acAppConnector;
    /**
     * Contains the value request types.
     */
    private HashMap<String, String> hmSeviceTypes;

    /**
     * Creates the transaction object.
     *
     * @param  acAppConnector  The application connector object.
     * @param  acConfig        The configuration of the application connector.
     * @param  cConnector      The connector to use to send messages to Cordys.
     */
    public HttpTransaction(HttpConnector acAppConnector, HttpConfiguration acConfig,
                           Connector cConnector)
    {
        this.acAppConnector = acAppConnector;

        hmSeviceTypes = new HashMap<String, String>();
        hmSeviceTypes.put(SERVICE_TYPE, SERVICE_TYPE);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Transaction created.");
        }
    }

    /**
     * This will be called when a transaction is being aborted.
     */
    public void abort()
    {
        if (LOG.isInfoEnabled())
        {
            LOG.info(Messages.TRANSACTION_ABORT);
        }
    }

    /**
     * This method returns returns if this transaction can process requests of the given type.
     *
     * @param   sType  The type of message that needs to be processed
     *
     * @return  true if the type can be processed. Otherwise false.
     */
    public boolean canProcess(String sType)
    {
        boolean bReturn = false;

        if (hmSeviceTypes.containsKey(sType))
        {
            bReturn = true;
        }
        return bReturn;
    }

    /**
     * This method is called when the transaction is committed.
     */
    public void commit()
    {
        if (LOG.isInfoEnabled())
        {
            LOG.info(Messages.TRANSACTION_COMMIT);
        }
    }

    /**
     * This method processes the received request.
     *
     * @param   bbRequest   The request body block.
     * @param   bbResponse  The response body block.
     *
     * @return  true if the connector has to send the response. If someone else sends the response
     *          false is returned.
     */
    public boolean process(BodyBlock bbRequest, BodyBlock bbResponse)
    {
        boolean bReturn = true;
        int reqNode = 0;

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Incoming SOAP request: " + Node.writeToString(bbRequest.getXMLNode(), true));
        }

        try
        {
            IMethodConfiguration methodInfo = acAppConnector.getMethodConfig(bbRequest);
            IServerConnection serverConnection = acAppConnector.getServerConnection(methodInfo
                                                                                    .getConnectionId());

            if (serverConnection == null)
            {
                throw new ConnectorException(ConnectorExceptionMessages.NO_CONNECTION_FOUND_WITH_ID_0,
                                             methodInfo.getConnectionId());
            }

            // Get the data XML node and unlink it, so that the XSLT can work properly.
            reqNode = Node.unlink(bbRequest.getXMLNode());

            // Send the HTTP request.
            int resNode = MethodExecutor.sendRequest(reqNode, serverConnection, methodInfo);

            if (resNode != 0)
            {
                int resFirstChild = Node.getFirstChild(resNode);
                int resLastChild = Node.getLastChild(resNode);

                if (resFirstChild != 0)
                {
                    int responseMethodNode = bbResponse.getXMLNode();
                    Map<String, String> rootNamespaces = XmlUtils.getNamespaceDeclarations(resNode);

                    XmlUtils.addNamespaceDeclarations(responseMethodNode, rootNamespaces);
                    Node.appendToChildren(resFirstChild, resLastChild, responseMethodNode);

                    for (int tmpNode = Node.getFirstChild(responseMethodNode); tmpNode != 0;)
                    {
                        XmlUtils.removeNamespaceDeclarations(tmpNode, rootNamespaces);
                        tmpNode = Node.getNextElement(tmpNode);
                    }
                }

                Node.delete(resNode);
                resNode = 0;
            }

            int responseEnvelope = Node.getRoot(bbResponse.getXMLNode());

            if (methodInfo.isCleanResponseBody())
            {
                int bodyNode = Node.getParent(bbResponse.getXMLNode());
                int resFirstChild = Node.getFirstChild(bodyNode);
                int resLastChild = Node.getLastChild(bodyNode);

                if (resFirstChild != 0)
                {
                    Node.delete(resFirstChild, resLastChild);
                }
            }

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Sending SOAP response: " + Node.writeToString(responseEnvelope, true));
            }
        }
        catch (HandlerException he)
        {
            // Either the request/response handler threw an exception, so we need to make a proper
            // soap fault.
        	he.setPreferredLocale(ServerLocalizableException.PreferredLocale.SOAP_LOCALE);
        	he.toSOAPFault(bbResponse);
        	
        }
        catch (Throwable tException)
        {
            String sMessage = tException.getLocalizedMessage();
            LOG.error(tException, Messages.TRANSACTION_ERROR, sMessage);

            ServerLocalizableException sle = null;

            // What we'll do here is see which exception in the causes is a
            // ServerLocalizableException. That message we will use to send as the main message to
            // the end user. If no SLE could be found the current exception is wrapped into a new
            // SLE exception.
            int iMaxHop = 30;
            int iCurrentHop = 0;
            Throwable tCurrent = tException;

            while ((tCurrent != null) && (iCurrentHop < iMaxHop))
            {
                if (tCurrent instanceof ServerLocalizableException)
                {
                    sle = (ServerLocalizableException) tCurrent;
                }
                tCurrent = tCurrent.getCause();

                // The whole hop-thing is to make sure that if there are cyclic references in the
                // exception causes the code won't go into an endless loop.
                iCurrentHop++;
            }

            if (sle == null)
            {
                // No SLE could be found, so wrap the current exception.
                sle = new ConnectorException(tException,
                                             ConnectorExceptionMessages.ERROR_EXECUTING_REQUEST_0,
                                             ExceptionUtil.getSimpleErrorTrace(tException, true));
            }

            // Create the proper SOAP fault.
            sle.setPreferredLocale(ServerLocalizableException.PreferredLocale.SOAP_LOCALE);
            sle.toSOAPFault(bbResponse);

            if (bbRequest.isAsync())
            {
                bbRequest.continueTransaction();
                bReturn = false;
            }
        }
        finally
        {
            if (reqNode != 0)
            {
                Node.delete(reqNode);
                reqNode = 0;
            }
        }

        if (LOG.isDebugEnabled() && (bReturn == true))
        {
            LOG.debug("Outgoing SOAP request:\n" +
                      Node.writeToString(bbResponse.getXMLNode(), false));
        }

        return bReturn;
    }
}
