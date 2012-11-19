package com.cordys.coe.ac.httpconnector.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import com.cordys.coe.ac.httpconnector.Messages;
import com.eibus.util.logger.CordysLogger;

public class DummyX509TrustManager implements X509TrustManager {
	static CordysLogger logger = CordysLogger
			.getCordysLogger(DummyX509TrustManager.class);

	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		if (logger.isDebugEnabled()) {
			logger.debug(Messages.CHECK_CLIENT_TRUSTED_CALLED);
		}
	}

	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		if (logger.isDebugEnabled()) {
			logger.debug(Messages.CHECK_SERVER_TRUSTED_CALLED);
		}
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		if (logger.isDebugEnabled()) {
			logger.debug(Messages.GET_ACCEPTED_ISSUERS_CALLED);
		}
		return null;
	}
}
