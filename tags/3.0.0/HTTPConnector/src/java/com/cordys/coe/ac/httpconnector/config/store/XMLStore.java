package com.cordys.coe.ac.httpconnector.config.store;

import static com.cordys.coe.ac.httpconnector.HttpConnectorConstants.*;

import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.cordys.coe.ac.httpconnector.exception.ConnectorExceptionMessages;
import com.eibus.connector.nom.Connector;
import com.eibus.management.IManagedComponent;
import com.eibus.util.logger.CordysLogger;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPath;
import com.eibus.xml.xpath.XPathMetaInfo;

public class XMLStore implements Store {

	private static Connector connector;
	private static final CordysLogger logger = CordysLogger
			.getCordysLogger(XMLStore.class);
	private XPathMetaInfo xmi;
	private static final String SYSTEM_USER_PREFIX = "cn=SYSTEM,cn=organizational users,";

	public XMLStore(IManagedComponent parent) throws ConnectorException {
		xmi = new XPathMetaInfo();
		xmi.addNamespaceBinding("xmlst", NS_XML_STORE);
		xmi.addNamespaceBinding("soap", NS_SOAP);
		try {
			if (connector == null) {
				connector = Connector.getInstance("HTTP Connector");
				if (!connector.isOpen()) {
					connector.open();
				}
				if (parent != null) {
					connector.createManagedComponent(parent, "HTTP Connector",
							"HTTP Connector");
				}
			}
		} catch (Exception e) {
			throw new ConnectorException(e,
					ConnectorExceptionMessages.FAILED_TO_CREATE_CONNECTOR,
					e.toString());
		}
	}

	@Override
	public int load(String path, String organizationDN, String level)
			throws ConnectorException {
		int request = 0;
		int response = 0;
		try {
			request = connector.createSOAPMethod(
					getOrganizationalSystemUser(organizationDN),
					organizationDN, NS_XML_STORE, GET_XML_OBJECT);
			int key = Node.getDocument(request).createTextElement("key", path,
					request);
			Node.setAttribute(key, VERSION, level);
			response = connector.sendAndWait(request);
			checkSOAPFault(response);
			int configNode = XPath.getFirstMatch("//xmlst:"
					+ GET_XML_OBJECT_RESPONSE, xmi, response);
			if (configNode != 0) {
				configNode = XPath.getFirstMatch("./xmlst:tuple/xmlst:old/*",
						xmi, configNode);
			}
			configNode = Node.unlink(configNode);
			Node.removeAttribute(configNode, "xmlns:SOAP");
			return configNode;
		} catch (ConnectorException e) {
			logger.error(
					e,
					ConnectorExceptionMessages.UNABLE_TO_LOAD_CONFIGURATION_FROM_XMLSTORE);
			throw e;
		} catch (Exception e) {
			logger.error(
					e,
					ConnectorExceptionMessages.UNABLE_TO_LOAD_CONFIGURATION_FROM_XMLSTORE);
			throw new ConnectorException(
					e,
					ConnectorExceptionMessages.UNABLE_TO_LOAD_CONFIGURATION_FROM_XMLSTORE);
		} finally {
			Node.delete(Node.getRoot(request));
			Node.delete(Node.getRoot(response));
		}
	}

	private String getOrganizationalSystemUser(String organizationDN) {
		return SYSTEM_USER_PREFIX + organizationDN;
	}

	private void checkSOAPFault(int response) throws ConnectorException {
		int fault = XPath.getFirstMatch("/soap:Envelope/soap:Body/soap:Fault",
				xmi, response);
		if (fault != 0) {
			String faultString = Node.getDataElement(fault, "faultstring", "");
			throw new ConnectorException(ConnectorExceptionMessages.SOAP_FAULT,
					faultString);
		}
	}
}