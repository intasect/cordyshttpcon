/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.httpconnector.utils;

import junit.framework.TestCase;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

/**
 * Test cases for XmlUtils class.
 *
 * @author mpoyhone
 */
public class XmlUtilsTest extends TestCase
{
    private static final Document doc = new Document();

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected@Override
 void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Test method for {@link com.cordys.coe.ac.httpconnector.utils.XmlUtils#cleanXml(int, boolean)}.
     * @throws Exception if an error occurred.
     */
    public void testCleanXml_RemoveNamespaces() throws Exception
    {
        int node = 0;
        
        try
        {
            node = doc.parseString("<root xmlns='abc' xmlns:n1='n1:abc'><a/><b xmlns=''/><n1:c/></root>");
            XmlUtils.cleanXml(node, false);
            
            assertEquals("<root><a/><b/><c/></root>", Node.writeToString(node, false));
        }
        finally
        {
            if (node != 0)
            {
                Node.delete(node);
                node = 0;
            }
        }
    }

    /**
     * Test method for {@link com.cordys.coe.ac.httpconnector.utils.XmlUtils#cleanXml(int, boolean)}.
     * @throws Exception if an error occurred.
     */
    public void testCleanXml_RemoveNamespaces_AttributePrefix() throws Exception
    {
        int node = 0;
        
        try
        {
            node = doc.parseString("<root xmlns:n1='n1:abc' n1:attr=\"value\"></root>");
            XmlUtils.cleanXml(node, false);
            
            assertEquals("<root attr=\"value\"/>", Node.writeToString(node, false));
        }
        finally
        {
            if (node != 0)
            {
                Node.delete(node);
                node = 0;
            }
        }
    }
        
    /**
     * Test method for {@link com.cordys.coe.ac.httpconnector.utils.XmlUtils#cleanXml(int, boolean)}.
     * @throws Exception if an error occurred.
     */
    public void testCleanXml_RemoveWhitespace() throws Exception
    {
        int node = 0;
        
        try
        {
            node = doc.parseString("<root>    <a/>\r\n<b>  \n  </b><c>va  lue</c></root>");
            XmlUtils.cleanXml(node, true);

            assertEquals("<root><a/><b/><c>va  lue</c></root>", Node.writeToString(node, false));
        }
        finally
        {
            if (node != 0)
            {
                Node.delete(node);
                node = 0;
            }
        }
    }
}
