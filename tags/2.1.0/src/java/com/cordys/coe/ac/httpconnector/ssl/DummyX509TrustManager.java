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
package com.cordys.coe.ac.httpconnector.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import com.eibus.util.logger.CordysLogger;

/**
 * An accept-ignore-all x509 trustmanager.
 * 
 * @author jpluimers
 */
public class DummyX509TrustManager implements X509TrustManager
{
	private static final CordysLogger LOG = CordysLogger.getCordysLogger(DummyX509TrustManager.class);

	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("checkClientTrusted called");
		}
	}

	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("checkServerTrusted called");
		}
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getAcceptedIssuers called");
		}
		return null;
	}
}
