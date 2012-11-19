package com.cordys.coe.ac.httpconnector.basic;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

public class ResponseHandler extends AbstractHandler {

	private String response;

	public ResponseHandler(String response) {
		this.response = response;
	}

	@Override
	public void handle(String target, HttpServletRequest req,
			HttpServletResponse res, int dispatch) throws IOException,
			ServletException {
		Request baseRequest = req instanceof Request ? (Request) req
				: HttpConnection.getCurrentConnection().getRequest();
		if (baseRequest == null) {
			throw new RuntimeException("Failed to understand request");
		}
		res.setStatus(SC_OK);
		res.setContentType("text/xml;charset=utf-8");
		IOUtils.write(response, res.getOutputStream());
		baseRequest.setHandled(true);
	}

}
