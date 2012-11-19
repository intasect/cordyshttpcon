package com.cordys.coe.ac.httpconnector.config;

import java.util.HashMap;
import java.util.Map;

import com.eibus.xml.xpath.XSLT;

public class XSLTCollection {

	private Map<String, XSLT> xsls = new HashMap<String, XSLT>();

	public synchronized void add(String path, XSLT xslt) {
		xsls.put(path, xslt);
	}

	public synchronized XSLT get(String path) {
		return xsls.get(path);
	}

	public synchronized boolean isLoaded(String path) {
		return xsls.containsKey(path);
	}
}
