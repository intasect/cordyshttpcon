/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.httpconnector.jetty;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.security.BasicAuthenticator;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.security.UserRealm;

/**
 * A stub web server.
 *
 * @author mpoyhone
 */
public class WebServerStub
{
    /**
     * Stub response.
     */
    private static final String responseXml = 
        "<bsazipcodecheck>" +
          "<messageheader>" +
            "<originator>STUB</originator>" +
            "<recipient>Cordys</recipient>" +
            "<messagetype>STUB RESPONSE</messagetype>" +
            "<messageversion>9999</messageversion>" +
            "<timestamp>99990115235959</timestamp>" +
          "</messageheader>" +
          "<messagebody>" +
            "<portfolio-id>xxx</portfolio-id>" +
            "<enduserinfo>" +
              "<requested-housenumber>yyy</requested-housenumber>" +
              "<requested-housenrext>xxx</requested-housenrext>" +
              "<requested-zipcode>zzz</requested-zipcode>" +
            "</enduserinfo>" +
            "<distributionpoint>" +
              "<distribution-point>string</distribution-point>" +
              "<technologyavailability>" +
                "<technology-type>string</technology-type>" +
                "<availability>string</availability>" +
                "<plandate>string</plandate>" +
                "<serviceable>string</serviceable>" +
                "<bandwidth-up>string</bandwidth-up>" +
                "<bandwidth-down>string</bandwidth-down>" +
                "<min-bandwidth-up>string</min-bandwidth-up>" +
                "<min-bandwidth-down>string</min-bandwidth-down>" +
              "</technologyavailability>" +
              "<accessclassavailability>" +
                "<access-class>string</access-class>" +
                "<availability>string</availability>" +
                "<plandate>string</plandate>" +
              "</accessclassavailability>" +
              "<carriertypeavailability>" +
                "<carrier-type>string</carrier-type>" +
                "<availability>string</availability>" +
                "<plandate>string</plandate>" +
                "<nl-type>string</nl-type>" +
              "</carriertypeavailability>" +
            "</distributionpoint>" +
            "<errorinfo>" +
              "<errorcode>100</errorcode>" +
            "</errorinfo>" +
          "</messagebody>" +
        "</bsazipcodecheck>";
    private static final String soapResponseXml =
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://www.fortis.com/LetterSuiteService/200801\" xmlns:ns1=\"http://www.fortis.com/LetterSuite/200801\">\r\n" + 
        "   <soapenv:Header/>\r\n" + 
        "   <soapenv:Body>\r\n" + 
        "      <ns:lettersuite-response>\r\n" + 
        "         <ns1:overal-status>Mock Status</ns1:overal-status>\r\n" + 
        "         <ns1:responsedocuments/>\r\n" + 
        "      </ns:lettersuite-response>\r\n" + 
        "   </soapenv:Body>\r\n" + 
        "</soapenv:Envelope>";
    
    private static void handleRequest(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
        throws IOException, ServletException
    {
        //response.getWriter().println(responseXml);
        response.getWriter().println(soapResponseXml);
        
/*        
        try
        {
            Thread.sleep((long) (500 * Math.random()));
        }
        catch (InterruptedException e)
        {
        }*/
    }
    
    public static void main(String[] args) throws Exception
    {
        Handler handler=new AbstractHandler()
        {
            public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
                throws IOException, ServletException
            {
                Principal userPrincipal = request.getUserPrincipal();
                
                if (userPrincipal == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                } else {
                    //response.setContentType("application/xml");
                    response.setContentType("text/xml; charset=utf-8");
                    response.setStatus(HttpServletResponse.SC_OK);

                    handleRequest(target, request, response, dispatch);
                }
             
                ((Request)request).setHandled(true);
            }
        };
        
        Server server = new Server(8080);   
        SslSocketConnector sslConn = new SslSocketConnector();
        
        sslConn.setPort(8443);
        sslConn.setMaxIdleTime(30000);
        sslConn.setHandshakeTimeout(2000);
        
        String keystore = "D:\\projects\\SSL-Certificates\\output\\cnd1033-server-keystore.jks";
        String keystorePassword = "password";
        String truststore = keystore;
        String truststorePassword = keystorePassword;
        
        sslConn.setKeystore(keystore);
        sslConn.setPassword("password");
        sslConn.setKeyPassword(keystorePassword);
        sslConn.setTruststore(truststore);
        sslConn.setTrustPassword(truststorePassword);
        
        sslConn.setWantClientAuth(true);
        
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);;
        constraint.setRoles(new String[]{"user","admin","moderator"});
        constraint.setAuthenticate(true);

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*");

        SecurityHandler sh = new SecurityHandler();
        sh.setUserRealm(new HashUserRealm("MyRealm","test/realm.properties"));
        sh.setConstraintMappings(new ConstraintMapping[]{cm});
        sh.addHandler(handler);

        
/*        SecurityHandler secHandler = new SecurityHandler();
        
        secHandler.setAuthenticator(new DummyUserAuthenticator());*/
        
        server.addConnector(sslConn);
        server.setHandler(sh);
        
/*        HashUserRealm myrealm = new HashUserRealm("MyRealm", "test/realm.properties");
        
        server.setUserRealms(new UserRealm[]{myrealm});*/

        server.start();
        server.join();
    }
    
    private static class DummyUserAuthenticator extends BasicAuthenticator
    {
        /**
         * @see org.mortbay.jetty.security.BasicAuthenticator#authenticate(org.mortbay.jetty.security.UserRealm, java.lang.String, org.mortbay.jetty.Request, org.mortbay.jetty.Response)
         */
        @Override
        public Principal authenticate(UserRealm realm, String pathInContext,
                Request request, Response response) throws IOException
        {
            response.setStatus(Response.SC_UNAUTHORIZED);
            return null;
        }
        
    }
    
    /*
    private static class DummyUserRealm implements UserRealm {
        private Map<String, Principal> usermap = new HashMap<String, Principal>();
        
        @Override
        public boolean reauthenticate(Principal arg0)
        {
            return false;
        }
        
        @Override
        public Principal pushRole(Principal arg0, String arg1)
        {
            return null;
        }
        
        @Override
        public Principal popRole(Principal arg0)
        {
            return null;
        }
        
        @Override
        public void logout(Principal arg0)
        {
            
        }
        
        @Override
        public boolean isUserInRole(Principal arg0, String arg1)
        {
            return false;
        }
        
        @Override
        public Principal getPrincipal(String username)
        {
            return usermap.get(username);
        }
        
        @Override
        public String getName()
        {
            return "TESTREALM";
        }
        
        @Override
        public void disassociate(Principal arg0)
        {
            
        }
        
        @Override
        public Principal authenticate(final String username, Object password, Request arg2)
        {
            if (! "testuser".equals(username)) {
                return null;
            }

            if (! "test123".equals(password)) {
                return null;
            }

            Principal res = new Principal() {
                
                @Override
                public String getName()
                {
                    return username;
                }
            };
            
            usermap.put(username, res);
            
            return res;
        }
    };*/
}
