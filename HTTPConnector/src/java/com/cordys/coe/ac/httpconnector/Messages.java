package com.cordys.coe.ac.httpconnector;

import com.eibus.localization.message.Message;
import com.eibus.localization.message.MessageSet;

public class Messages {

	public static final MessageSet MESSAGE_SET = MessageSet
			.getMessageSet("com.cordys.coe.ac.httpconnector.Messages");

	/** HttpConnector connector */
	public static final Message CONNECTOR_MANAGEMENT_DESCRIPTION = MESSAGE_SET
			.getMessage("connectorManagementDescription");

	/** Coelib version mismatch. */
	public static final Message COELIB_VERSION_MISMATCH = MESSAGE_SET
			.getMessage("coelibVersionMismatch");

	/** Starting HttpConnector connector */
	public static final Message CONNECTOR_STARTING = MESSAGE_SET
			.getMessage("connectorStarting");

	/** HttpConnector connector started. */
	public static final Message CONNECTOR_STARTED = MESSAGE_SET
			.getMessage("connectorStarted");

	/** An error occurred while starting the HttpConnector connector. */
	public static final Message CONNECTOR_START_EXCEPTION = MESSAGE_SET
			.getMessage("connectorStartException");

	/** HttpConnector connector stopped. */
	public static final Message CONNECTOR_STOPPED = MESSAGE_SET
			.getMessage("connectorStopped");

	/** Resetting HttpConnector connector */
	public static final Message CONNECTOR_RESET = MESSAGE_SET
			.getMessage("connectorReset");

	/** Aborted the transaction. */
	public static final Message TRANSACTION_ABORT = MESSAGE_SET
			.getMessage("transactionAbort");

	/** Committed the transaction. */
	public static final Message TRANSACTION_COMMIT = MESSAGE_SET
			.getMessage("transactionCommit");

	/** An error occurred while processing the SOAP request: {0} */
	public static final Message TRANSACTION_ERROR = MESSAGE_SET
			.getMessage("transactionError");

	/** Request Processing */
	public static final Message REQUEST_PROCESSING = MESSAGE_SET
			.getMessage("requestProcessing");

	/** Request transformation time */
	public static final Message REQUEST_TRANSFORMATION_TIME = MESSAGE_SET
			.getMessage("requestTransformationTime");

	/** Response transformation time */
	public static final Message RESPONSE_TRANSFORMATION_TIME = MESSAGE_SET
			.getMessage("responseTransformationTime");

	/** HTTP request processing time */
	public static final Message HTTP_SEND_AND_RECEIVE = MESSAGE_SET
			.getMessage("httpSendAndReceive");

	/** Loading Configuration for {0} */
	public static final Message LOADING_CONFIGURATION = MESSAGE_SET
			.getMessage("loadingConfiguration");

	/** Using DummySSLProtocolSocketFactory */
	public static final Message USING_DUMMY_SOCKET_FACTORY = MESSAGE_SET
			.getMessage("usingDummySocketFactory");

	/** Using CordysSSLProtocolSocketFactory */
	public static final Message USING_CORDYS_SOCKET_FACTORY = MESSAGE_SET
			.getMessage("usingCordysSocketFactory");

	/** Create socket called with boolean parameter */
	public static final Message BOOLEAN_CREATE_SOCKET = MESSAGE_SET
			.getMessage("booleanCreateSocket");

	/** Create socket called with client port */
	public static final Message CLIENT_CREATE_SOCKET = MESSAGE_SET
			.getMessage("clientCreateSocket");

	/** Create socket called with five parameters */
	public static final Message FIVE_PARAMETER_CREATE_SOCKET = MESSAGE_SET
			.getMessage("fiveParameterCreateSocket");

	/** Create socket called with two parameters */
	public static final Message TWO_PARAMETER_CREATE_SOCKET = MESSAGE_SET
			.getMessage("twoParameterCreateSocket");

	/** checkClientTrusted of DummyX509TrustManager called */
	public static final Message CHECK_CLIENT_TRUSTED_CALLED = MESSAGE_SET
			.getMessage("checkClientTrustedCalled");

	/** checkServerTrusted of DummyX509TrustManager called */
	public static final Message CHECK_SERVER_TRUSTED_CALLED = MESSAGE_SET
			.getMessage("checkServerTrustedCalled");

	/** getAcceptedIssuers of DummyX509TrustManager called */
	public static final Message GET_ACCEPTED_ISSUERS_CALLED = MESSAGE_SET
			.getMessage("getAcceptedIssuersCalled");

	/** Connection {0} found in organization {1} */
	public static final Message CONNECTION_FOUND = MESSAGE_SET
			.getMessage("connectionFound");

	/** Organization awareness is set to {0} */
	public static final Message ORGANIZATION_AWARENESS_MODE = MESSAGE_SET
			.getMessage("organizationAwarenessMode");

	/** XSLT {0} found in {1} */
	public static final Message XSLT_FOUND_IN = MESSAGE_SET
			.getMessage("xsltFoundIn");

}