package com.cordys.coe.ac.httpconnector.basic;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import com.eibus.xml.nom.XMLException;

public class ResourceManager {
	private static com.eibus.xml.nom.Document document = new com.eibus.xml.nom.Document();

	public final static int loadXML(final URL resource) throws IOException,
			XMLException {
		int node = document.parseString(loadString(resource));
		return node;
	}

	public final static String loadString(final URL resource)
			throws IOException {
		StringBuilder builder = null;
		if (resource != null) {
			InputStream is = resource.openStream();
			InputStreamReader reader = new InputStreamReader(is, "UTF-8");
			int ch;
			builder = new StringBuilder();
			while ((ch = reader.read()) != -1) {
				builder.append((char) ch);
			}
		}
		return builder.toString();
	}
}
