package com.cordys.coe.ac.httpconnector.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

public class DummyX509TrustManager implements X509TrustManager
{
	static  CordysLogger uddiLogger = CordysLogger.getCordysLogger(DummyX509TrustManager.class);

	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		uddiLogger.log(Severity.DEBUG,"checkClientTrusted called ");
	}

	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		uddiLogger.log(Severity.DEBUG,"checkServerTrusted called ");
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		uddiLogger.log(Severity.DEBUG,"getAcceptedIssuers called ");
		return null;
	}
}
