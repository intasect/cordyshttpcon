package com.cordys.coe.ac.httpconnector.basic;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static junit.framework.Assert.*;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.xml.sax.SAXException;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import com.cordys.coe.ac.httpconnector.HTTPServer;
import com.cordys.coe.ac.httpconnector.HttpConnector;
import static com.cordys.coe.ac.httpconnector.HttpConnectorConstants.*;
import com.eibus.connector.nom.Connector;
import com.eibus.directory.soap.DN;
import com.eibus.security.identity.Identity;
import com.eibus.soap.ApplicationConnector;
import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;
import com.eibus.soap.MethodDefinition;
import com.eibus.soap.Processor;
import com.eibus.soap.SOAPTransaction;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;
import com.eibus.xml.xpath.XPath;
import com.eibus.xml.xpath.XPathMetaInfo;

import static com.cordys.coe.ac.httpconnector.basic.ResourceManager.*;

@RunWith(PowerMockRunner.class)
// When working with secure HTTP URIs, more packages will have to be excluded to
// ensure that their classes are loaded by the Java System class loader
@PowerMockIgnore({ "java.security*", "org.apache.*" })
@PrepareForTest({ Connector.class, SOAPTransaction.class,
		ApplicationConnector.class })
public class Basic {

	private final String organization = "o=system,cn=cordys,cn=SSD,o=vanenburg.com";
	private static final Document doc = new Document();
	private static XPathMetaInfo info = new XPathMetaInfo();
	private HTTPServer server;

	@Mock
	private Connector connector;
	@Mock
	private Processor processor;
	@Mock
	private SOAPTransaction soapTransaction;
	@Mock
	private BodyBlock requestBlock;
	@Mock
	private BodyBlock responseBlock;
	@Mock
	private MethodDefinition method;
	@Mock
	private Identity identity;
	@Mock
	private DN dn;

	public Basic() {
		info.addNamespaceBinding("xmlst", NS_XML_STORE);
	}

	@Before
	public void prepare() throws Exception {
		int implementation = loadXML(getClass().getResource(
				"implementation.xml"));
		int actualResponse = doc.createElement("response");
		mockStatic(Connector.class);
		mockStatic(SOAPTransaction.class);
		when(Connector.getInstance(anyString())).thenReturn(connector);
		when(
				connector.createSOAPMethod(anyString(), anyString(),
						anyString(), anyString())).thenAnswer(
				new XMLStoreRequest());
		when(connector.sendAndWait(anyInt()))
				.thenAnswer(new XMLStoreResponse());
		when(SOAPTransaction.getCurrentSOAPTransaction()).thenReturn(
				soapTransaction);
		when(soapTransaction.getIdentity()).thenReturn(identity);
		when(identity.getUserOrganization()).thenReturn(organization);
		when(processor.getOrganization()).thenReturn(organization);
		int dummyRequest = doc.createElement("dummy");
		when(requestBlock.getXMLNode()).thenReturn(dummyRequest);
		when(requestBlock.getMethodDefinition()).thenReturn(method);
		when(method.getMethodDN()).thenReturn(dn);
		when(dn.toString()).thenReturn("DUMMY_DN");
		when(requestBlock.getMethodDefinition()).thenReturn(method);
		when(responseBlock.getXMLNode()).thenReturn(actualResponse);
		when(method.getImplementation()).thenReturn(implementation);

		String mockResponse = loadString(getClass().getResource(
				"expectedResponse.xml"));
		server = new HTTPServer(5555, new ResponseHandler(mockResponse));
		server.start();
	}

	@Test
	public void execute() throws SAXException, IOException, XMLException {
		int config = 0;
		int expectedTransformedNode = 0;
		try {
			config = loadXML(getClass().getResource("configuration.xml"));
			HttpConnector connector = new HttpConnector();
			connector.setConfiguration(config);
			connector.open(processor);
			ApplicationTransaction transaction = connector
					.createTransaction(soapTransaction);
			boolean status = transaction.process(requestBlock, responseBlock);
			assertEquals(status, true);
			expectedTransformedNode = loadXML(getClass().getResource(
					"expectedTransformedResponse.xml"));
			String expectedTransformed = Node.writeToString(
					expectedTransformedNode, false);
			String actual = Node.writeToString(responseBlock.getXMLNode(),
					false);
			assertXMLEqual(expectedTransformed, actual);
		} finally {
			Node.delete(config);
			Node.delete(expectedTransformedNode);
		}
	}

	@After
	public void close() throws Exception {
		server.stop();
	}

	public class XMLStoreResponse implements Answer<Integer> {
		private static final String configKey = "/Cordys/HttpConnector/config.xml";
		private static final String xslKey = "bookXSL";

		@Override
		public Integer answer(InvocationOnMock invocation) throws Throwable {
			Object arguments[] = invocation.getArguments();
			int request = 0;
			int response = 0;
			if (arguments.length > 0) {
				request = (Integer) arguments[0];
				String key = Node.getDataElement(request, "key", null);
				if (configKey.equals(key)) {
					response = loadXML(getClass().getResource(
							"getConfigurationResponse.xml"));
				} else if (xslKey.equals(key)) {
					response = loadXML(getClass().getResource(
							"getXSLTResponse.xml"));
				} else {
					throw new RuntimeException(
							"Ket not understood by XML Store mocker");
				}
			}
			return response;
		}
	}

	public class XMLStoreRequest implements Answer<Integer> {

		@Override
		public Integer answer(InvocationOnMock invocation) throws Throwable {
			int soapRequest = loadXML(getClass().getResource(
					"getXMLStoreRequest.xml"));
			int response = XPath.getFirstMatch("//xmlst:" + GET_XML_OBJECT,
					info, soapRequest);
			return response;
		}
	}
}