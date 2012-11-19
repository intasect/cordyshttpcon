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
package com.cordys.coe.ac.httpconnector.exception;

import com.cordys.coe.ac.httpconnector.HttpConnectorConstants;
import com.cordys.coe.exception.ServerLocalizableException;

import com.eibus.localization.IStringResource;

import com.eibus.soap.BodyBlock;
import com.eibus.soap.fault.Fault;
import com.eibus.soap.fault.FaultDetail;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

import java.util.ArrayList;

/**
 * Holds exceptions is thrown by the request and response handlers.
 * 
 * @author pgussow
 */
public class HandlerException extends ServerLocalizableException {
	/**  */
	private static final long serialVersionUID = 1L;
	/**
	 * Holds additional error messages that will be added to the SOAP fault
	 * details.
	 */
	private ArrayList<String> m_errorDetails = new ArrayList<String>();

	/**
	 * Creates a new HandlerException object.
	 * 
	 * @param srMessage
	 *            The message for the exception.
	 * @param aoParameters
	 *            The parameters for the message.
	 */
	public HandlerException(IStringResource srMessage, Object... aoParameters) {
		super(srMessage, aoParameters);
	}

	/**
	 * Creates a new HandlerException object.
	 * 
	 * @param tCause
	 *            The root cause for this exception.
	 * @param srMessage
	 *            The message for the exception.
	 * @param aoParameters
	 *            The parameters for the message.
	 */
	public HandlerException(Throwable tCause, IStringResource srMessage,
			Object... aoParameters) {
		super(tCause, srMessage, aoParameters);
	}

	/**
	 * This method adds an additional error message which will be placed in the
	 * SOAP Fault detail.
	 * 
	 * @param message
	 *            The message to add.
	 */
	public void addAdditionalErrorMessage(String message) {
		m_errorDetails.add(message);
	}

	/**
	 * @see com.cordys.coe.exception.ServerLocalizableException#toSOAPFault(com.eibus.soap.BodyBlock)
	 */
	@Override
	public Fault toSOAPFault(BodyBlock bbResponse) {
		Fault fault = super.toSOAPFault(bbResponse);

		if (m_errorDetails.size() > 0) {
			FaultDetail fd = fault.getDetail();

			Document doc = Node.getDocument(bbResponse.getXMLNode());

			int messages = doc.createElementNS("messages", null, "ns",
					HttpConnectorConstants.NS_HTTP_CONNECTOR_HANDLER_ERROR_2_0,
					0);

			for (String message : m_errorDetails) {
				Node.createElementWithParentNS("message", message, messages);
			}

			fd.addDetailEntry(messages);
		}

		return fault;
	}
}
