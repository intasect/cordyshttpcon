package com.cordys.coe.ac.httpconnector.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.cordys.coe.ac.httpconnector.HttpConnectorConstants.*;

import com.cordys.coe.ac.httpconnector.HttpConnectorConstants;
import com.cordys.coe.ac.httpconnector.Messages;
import com.cordys.coe.ac.httpconnector.config.store.StoreManager;
import com.cordys.coe.ac.httpconnector.exception.ConnectorException;
import com.cordys.coe.ac.httpconnector.exception.ConnectorExceptionMessages;
import com.eibus.soap.SOAPTransaction;
import com.eibus.util.logger.CordysLogger;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XSLT;

public class DefaultXSLTStore implements IXSLTStore {

	private String baseOrganizationDN;
	private static final CordysLogger logger = CordysLogger
			.getCordysLogger(DefaultXSLTStore.class);
	private boolean organizationAware = false;
	private Map<String, XSLTCollection> xsltMappings = new ConcurrentHashMap<String, XSLTCollection>();

	protected DefaultXSLTStore(String baseOrganizationDN,
			boolean organizationAware) {
		this.baseOrganizationDN = baseOrganizationDN;
		this.organizationAware = organizationAware;
		XSLTCollection xslts = new XSLTCollection();
		xsltMappings.put(ISV_SPACE, xslts);
	}

	@Override
	public XSLT loadXslt(int node) throws ConnectorException {
		String filePath = Node.getAttribute(node, "file");
		String xmlstorePath = Node.getAttribute(node, "xmlstore");
		if ((filePath != null) && (xmlstorePath != null)) {
			throw new ConnectorException(
					ConnectorExceptionMessages.BOTH_FILE_AND_XMLSTORE_ATTRIBUTES_CANNOT_BE_SET_FOR_XSLT);
		}
		XSLT xslt = null;
		String organizationDN = null;

		if (filePath != null) {
			xslt = XSLT.parseFromFile(filePath);
			if (logger.isInfoEnabled()) {
				logger.info(Messages.XSLT_FOUND_IN, filePath, "file system");
			}
		}
		if (xslt != null) {
			return xslt;
		}

		if (xmlstorePath != null) {
			if (organizationAware) {
				organizationDN = SOAPTransaction.getCurrentSOAPTransaction()
						.getIdentity().getUserOrganization();
				xslt = get(xmlstorePath, organizationDN);
			} else {
				xslt = get(xmlstorePath, baseOrganizationDN);
			}
		}

		if (xslt != null) {
			if (logger.isInfoEnabled()) {
				logger.info(Messages.XSLT_FOUND_IN, "inline",
						"in method implementation");
			}
			return xslt;
		}

		int xsltNode = Node.getFirstElement(node);
		if (xsltNode == 0) {
			ConnectorException exception = new ConnectorException(
					ConnectorExceptionMessages.EXPECTED_INLINE_XSLT_CONFIGURATION);
			logger.error(
					exception,
					ConnectorExceptionMessages.EXPECTED_INLINE_XSLT_CONFIGURATION);
			throw exception;
		}
		xslt = XSLT.parseFromString(Node.writeToString(xsltNode, false));
		return xslt;
	}

	private XSLT get(String xmlstorePath, String organizationDN)
			throws ConnectorException {
		XSLT xslt = null;
		XSLTCollection xslts = xsltMappings.get(organizationDN);
		if (xslts == null) {
			synchronized (this) {
				xslts = xsltMappings.get(organizationDN);
				if (xslts == null) {
					xslts = new XSLTCollection();
					xsltMappings.put(organizationDN, xslts);
				}
			}

		}
		xslt = xslts.get(xmlstorePath);
		if (xslt == null && !xslts.isLoaded(xmlstorePath)) {
			xslt = loadFromStore(xmlstorePath, organizationDN, xslts);
		}
		return xslt;

	}

	private XSLT loadFromStore(String xmlstorePath, String organizationDN,
			XSLTCollection xslts) throws ConnectorException {
		XSLT xslt = loadSpecificVersionFromStore(xmlstorePath, organizationDN,
				ORGANIZATION_VERSION);
		if (xslt == null) {
			XSLTCollection isvCollection = xsltMappings.get(ISV_SPACE);
			xslt = isvCollection.get(xmlstorePath);
			if (xslt == null && !isvCollection.isLoaded(xmlstorePath)) {
				xslt = loadSpecificVersionFromStore(xmlstorePath,
						organizationDN, ISV_VERSION);

				isvCollection.add(xmlstorePath, xslt);
			}
		}
		xslts.add(xmlstorePath, xslt);
		return xslt;
	}

	private XSLT loadSpecificVersionFromStore(String xmlstorePath,
			String organizationDN, String level) throws ConnectorException {
		int configNode = 0;
		XSLT xslt = null;
		try {
			configNode = StoreManager.getStore().load(xmlstorePath,
					organizationDN, level);
			if (configNode > 0) {
				xslt = XSLT.parseFromString(Node.writeToString(configNode,
						false));
				if (logger.isInfoEnabled()) {
					String location = organizationDN;
					if (level.equals(HttpConnectorConstants.ISV_VERSION)) {
						location = "System space";
					}
					logger.info(Messages.XSLT_FOUND_IN, xmlstorePath, location);
				}
			}

		} finally {
			if (configNode != 0) {
				Node.delete(configNode);
				configNode = 0;
			}
		}
		return xslt;
	}

	@Override
	public void reset() {
		synchronized (this) {
			xsltMappings.clear();
		}
	}
}
