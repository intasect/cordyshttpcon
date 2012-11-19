package com.cordys.coe.ac.httpconnector.exception;

import com.eibus.localization.message.Message;
import com.eibus.localization.message.MessageSet;

public class ConnectorExceptionMessages {

	public static final MessageSet MESSAGE_SET = MessageSet
			.getMessageSet("com.cordys.coe.ac.httpconnector.exception.ConnectorExceptionMessages");

	/** Error parsing the HTML response */
	public static final Message ERROR_PARSING_THE_HTML_RESPONSE = MESSAGE_SET
			.getMessage("ERROR_PARSING_THE_HTML_RESPONSE");

	/** The 'name' of the parameter must be filled. */
	public static final Message THE_NAME_OF_THE_PARAMETER_MUST_BE_FILLED = MESSAGE_SET
			.getMessage("THE_NAME_OF_THE_PARAMETER_MUST_BE_FILLED");

	/** The parameter {0} has no child element. */
	public static final Message THE_PARAMETER_0_HAS_NO_CHILD_ELEMENT = MESSAGE_SET
			.getMessage("THE_PARAMETER_0_HAS_NO_CHILD_ELEMENT");

	/**
	 * The response XML did not match the 'Valid response XPath' that was
	 * configured in the method implementation.
	 */
	public static final Message THE_RESPONSE_XML_DID_NOT_MATCH_THE_VALID_RESPONSE_XPATH_THAT_WAS_CONFIGURED_IN_THE_METHOD_IMPLEMENTATION = MESSAGE_SET
			.getMessage("THE_RESPONSE_XML_DID_NOT_MATCH_THE_VALID_RESPONSE_XPATH_THAT_WAS_CONFIGURED_IN_THE_METHOD_IMPLEMENTATION");

	/** The value for parameter {0} is empty */
	public static final Message THE_VALUE_FOR_PARAMETER_0_IS_EMPTY = MESSAGE_SET
			.getMessage("THE_VALUE_FOR_PARAMETER_0_IS_EMPTY");

	/** Both file and xmlstore attributes cannot be set for XSLT. */
	public static final Message BOTH_FILE_AND_XMLSTORE_ATTRIBUTES_CANNOT_BE_SET_FOR_XSLT = MESSAGE_SET
			.getMessage("BOTH_FILE_AND_XMLSTORE_ATTRIBUTES_CANNOT_BE_SET_FOR_XSLT");

	/** Configuration file not found from XMLStore: {0} */
	public static final Message CONFIGURATION_FILE_NOT_FOUND_FROM_XMLSTORE_0 = MESSAGE_SET
			.getMessage("CONFIGURATION_FILE_NOT_FOUND_FROM_XMLSTORE_0");

	/** Configuration file not set for this connector. */
	public static final Message CONFIGURATION_FILE_NOT_SET_FOR_THIS_CONNECTOR = MESSAGE_SET
			.getMessage("CONFIGURATION_FILE_NOT_SET_FOR_THIS_CONNECTOR");

	/** Error executing request: {0} */
	public static final Message ERROR_EXECUTING_REQUEST_0 = MESSAGE_SET
			.getMessage("ERROR_EXECUTING_REQUEST_0");

	/** Expected inline XSLT configuration. */
	public static final Message EXPECTED_INLINE_XSLT_CONFIGURATION = MESSAGE_SET
			.getMessage("EXPECTED_INLINE_XSLT_CONFIGURATION");

	/** HTTP connection failed: {0} */
	public static final Message HTTP_CONNECTION_FAILED_0 = MESSAGE_SET
			.getMessage("HTTP_CONNECTION_FAILED_0");

	/** HTTP request failed: {0} */
	public static final Message HTTP_REQUEST_FAILED_0 = MESSAGE_SET
			.getMessage("HTTP_REQUEST_FAILED_0");

	/** Invalid response code value: {0} */
	public static final Message INVALID_RESPONSE_CODE_VALUE_0 = MESSAGE_SET
			.getMessage("INVALID_RESPONSE_CODE_VALUE_0");

	/** Invalid response received from XMLStore. */
	public static final Message INVALID_RESPONSE_RECEIVED_FROM_XMLSTORE = MESSAGE_SET
			.getMessage("INVALID_RESPONSE_RECEIVED_FROM_XMLSTORE");

	/** Invalid response XML received. */
	public static final Message INVALID_RESPONSE_XML_RECEIVED = MESSAGE_SET
			.getMessage("INVALID_RESPONSE_XML_RECEIVED");

	/** Invalid status code received: {0} (expected: {1}). */
	public static final Message INVALID_STATUS_CODE_RECEIVED_0_EXPECTED_1 = MESSAGE_SET
			.getMessage("INVALID_STATUS_CODE_RECEIVED_0_EXPECTED_1");

	/** Missing attribute 'prefix' from namespace binding element. */
	public static final Message MISSING_ATTRIBUTE_PREFIX_FROM_NAMESPACE_BINDING_ELEMENT = MESSAGE_SET
			.getMessage("MISSING_ATTRIBUTE_PREFIX_FROM_NAMESPACE_BINDING_ELEMENT");

	/** Missing attribute 'uri' from namespace binding element. */
	public static final Message MISSING_ATTRIBUTE_URI_FROM_NAMESPACE_BINDING_ELEMENT = MESSAGE_SET
			.getMessage("MISSING_ATTRIBUTE_URI_FROM_NAMESPACE_BINDING_ELEMENT");

	/** No connection found with ID: {0} */
	public static final Message NO_CONNECTION_FOUND_WITH_ID_0 = MESSAGE_SET
			.getMessage("NO_CONNECTION_FOUND_WITH_ID_0");

	/** Unable to load configuration from XMLStore. */
	public static final Message UNABLE_TO_LOAD_CONFIGURATION_FROM_XMLSTORE = MESSAGE_SET
			.getMessage("UNABLE_TO_LOAD_CONFIGURATION_FROM_XMLSTORE");

	/** Unable to load response handler: {0} */
	public static final Message UNABLE_TO_LOAD_RESPONSE_HANDLER_0 = MESSAGE_SET
			.getMessage("UNABLE_TO_LOAD_RESPONSE_HANDLER_0");

	/** Class attribute missing from the response handler element. */
	public static final Message CLASS_ATTRIBUTE_MISSING_FROM_THE_RESPONSE_HANDLER_ELEMENT = MESSAGE_SET
			.getMessage("CLASS_ATTRIBUTE_MISSING_FROM_THE_RESPONSE_HANDLER_ELEMENT");

	/** Response handler element is not set. */
	public static final Message RESPONSE_HANDLER_ELEMENT_IS_NOT_SET = MESSAGE_SET
			.getMessage("RESPONSE_HANDLER_ELEMENT_IS_NOT_SET");

	/** Unable to load request handler: {0} */
	public static final Message UNABLE_TO_LOAD_REQUEST_HANDLER_0 = MESSAGE_SET
			.getMessage("UNABLE_TO_LOAD_REQUEST_HANDLER_0");

	/** Class attribute missing from the request handler element. */
	public static final Message CLASS_ATTRIBUTE_MISSING_FROM_THE_REQUEST_HANDLER_ELEMENT = MESSAGE_SET
			.getMessage("CLASS_ATTRIBUTE_MISSING_FROM_THE_REQUEST_HANDLER_ELEMENT");

	/** Connection ID is not set. */
	public static final Message CONNECTION_ID_IS_NOT_SET = MESSAGE_SET
			.getMessage("CONNECTION_ID_IS_NOT_SET");

	/** Request handler element is not set. */
	public static final Message REQUEST_HANDLER_ELEMENT_IS_NOT_SET = MESSAGE_SET
			.getMessage("REQUEST_HANDLER_ELEMENT_IS_NOT_SET");

	/** The tag config-path must be filled. */
	public static final Message THE_TAG_CONFIGPATH_MUST_BE_FILLED = MESSAGE_SET
			.getMessage("THE_TAG_CONFIGPATH_MUST_BE_FILLED");

	/** 'Configuration' element not found. */
	public static final Message CONFIGURATION_ELEMENT_NOT_FOUND = MESSAGE_SET
			.getMessage("CONFIGURATION_ELEMENT_NOT_FOUND");

	/** Root-tag of the configuration should be 'configuration' */
	public static final Message ROOTTAG_OF_THE_CONFIGURATION_SHOULD_BE_CONFIGURATION = MESSAGE_SET
			.getMessage("ROOTTAG_OF_THE_CONFIGURATION_SHOULD_BE_CONFIGURATION");

	/** Configuration not found */
	public static final Message CONFIGURATION_NOT_FOUND = MESSAGE_SET
			.getMessage("CONFIGURATION_NOT_FOUND");

	/** Invalid proxy server port value: {0} */
	public static final Message INVALID_PROXY_SERVER_PORT_VALUE_0 = MESSAGE_SET
			.getMessage("INVALID_PROXY_SERVER_PORT_VALUE_0");

	/** Proxy server port must be set. */
	public static final Message PROXY_SERVER_PORT_MUST_BE_SET = MESSAGE_SET
			.getMessage("PROXY_SERVER_PORT_MUST_BE_SET");

	/** Invalid server URL: {0} */
	public static final Message INVALID_SERVER_URL_0 = MESSAGE_SET
			.getMessage("INVALID_SERVER_URL_0");

	/** Connection element 'url' is missing. */
	public static final Message CONNECTION_ELEMENT_URL_IS_MISSING = MESSAGE_SET
			.getMessage("CONNECTION_ELEMENT_URL_IS_MISSING");

	/** Connection attribute 'id' is missing. */
	public static final Message CONNECTION_ATTRIBUTE_ID_IS_MISSING = MESSAGE_SET
			.getMessage("CONNECTION_ATTRIBUTE_ID_IS_MISSING");

	/** Organization is empty */
	public static final Message EMPTY_ORGANIZATION = MESSAGE_SET
			.getMessage("EMPTY_ORGANIZATION");

	/** Failed to create socket factory: {0} */
	public static final Message FAILED_TO_CREATE_SOCKET_FACTORY = MESSAGE_SET
			.getMessage("FAILED_TO_CREATE_SOCKET_FACTORY");

	/** SOAP Fault has occured: {0} */
	public static final Message SOAP_FAULT = MESSAGE_SET
			.getMessage("SOAP_FAULT");

	/** Failed to create connector: {0} */
	public static final Message FAILED_TO_CREATE_CONNECTOR = MESSAGE_SET
			.getMessage("FAILED_TO_CREATE_CONNECTOR");

}