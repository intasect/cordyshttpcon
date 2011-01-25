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
 package com.cordys.coe.ac.httpconnector.utils;

import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.NodeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * XML related utility methods.
 *
 * @author  mpoyhone
 */
public class XmlUtils
{
    /**
     * Adds the namespace declarations to the given node.
     *
     * @param  node          NOM node.
     * @param  declarations  Map of namespace declarations [prefix, URI] to be added.
     */
    public static void addNamespaceDeclarations(int node, Map<String, String> declarations)
    {
        for (Map.Entry<String, String> declEntry : declarations.entrySet())
        {
            String prefix = declEntry.getKey();
            String uri = declEntry.getValue();

            if ((prefix != null) && (prefix.length() > 0))
            {
                Node.setAttribute(node, "xmlns:" + prefix, uri);
            }
            else
            {
                Node.setAttribute(node, "xmlns", uri);
            }
        }
    }

    /**
     * Removes all namespace declarations and namespace prefixes from the XML. This removes all
     * whitespace text nodes.
     *
     * @param  node              Root node.
     * @param  removeWhitespace  If <code>true</code>, whitespace text node are removed.
     */
    public static void cleanXml(int node, boolean removeWhitespace)
    {
        int attrCount = Node.getNumAttributes(node);

        for (int i = 0; i < attrCount; i++)
        {
            String name = Node.getAttributeName(node, i);

            if ("xmlns".equals(name))
            {
                Node.removeAttribute(node, name);
            }
            else
            {
                String prefix = Node.getAttributePrefix(node, i);

                if ((prefix != null) && prefix.equals("xmlns"))
                {
                    Node.removeAttribute(node, name);
                }
            }
        }

        int child = Node.getFirstChild(node);

        while (child != 0)
        {
            int type = Node.getType(child);

            switch (type)
            {
                case NodeType.ELEMENT:
                    cleanXml(child, removeWhitespace);
                    break;

                case NodeType.DATA:
                    if (removeWhitespace)
                    {
                        String value = Node.getData(child);

                        if (value != null)
                        {
                            int count = value.length();
                            boolean isWhitespace = true;

                            for (int i = 0; i < count; i++)
                            {
                                if (!Character.isWhitespace(value.charAt(i)))
                                {
                                    isWhitespace = false;
                                    break;
                                }
                            }

                            if (isWhitespace)
                            {
                                int nextChild = Node.getNextSibling(child);

                                Node.delete(child);
                                child = nextChild;
                                continue;
                            }
                        }
                    }
                    break;
            }

            child = Node.getNextSibling(child);
        }

        String prefix = Node.getPrefix(node);

        if (prefix != null)
        {
            Node.setName(node, Node.getLocalName(node));
        }
    }

    /**
     * Returns all namespace declarations as a list.
     *
     * @param   node  NOM node.
     *
     * @return  A map containing attribute name to URI mappings.
     */
    public static Map<String, String> getNamespaceDeclarations(int node)
    {
        Map<String, String> res = new HashMap<String, String>();

        for (int i = 0, count = Node.getNumAttributes(node); i < count; i++)
        {
            String name = Node.getAttributeName(node, i + 1);

            if (name.startsWith("xmlns"))
            {
                if (name.length() == 5)
                {
                    // Default namespace.
                    res.put("", Node.getAttribute(node, name, ""));
                }
                else if ((name.length() > 6) && (name.charAt(5) == ':'))
                {
                    // Add the prefix.
                    res.put(name.substring(6), Node.getAttribute(node, name, ""));
                }
            }
        }

        return res;
    }

    /**
     * Removes the namespace declarations from the given node.
     *
     * @param  node          NOM node.
     * @param  declarations  Map of namespace declarations [prefix, URI] to be removed.
     */
    public static void removeNamespaceDeclarations(int node, Map<String, String> declarations)
    {
        for (Map.Entry<String, String> declEntry : declarations.entrySet())
        {
            String prefix = declEntry.getKey();

            if ((prefix != null) && (prefix.length() > 0))
            {
                Node.removeAttribute(node, "xmlns:" + prefix);
            }
            else
            {
                Node.removeAttribute(node, "xmlns");
            }
        }
    }

    /**
     * Recursively removes the given namespace URI's or all if the set is null.
     *
     * @param  node    Current node.
     * @param  uriSet  Namespace URI set or <code>null</code>.
     */
    public static void removeNamespacesRecursively(int node, Set<String> uriSet)
    {
        // First remove recursively namespace prefixes from nodes.
        String nodePrefix = Node.getPrefix(node);

        if (nodePrefix != null)
        {
            if ((uriSet == null) || uriSet.contains(Node.getNamespaceURI(node)))
            {
                Node.setName(node, Node.getLocalName(node));
            }
        }

        // Finally remove the namespace definition attributes.
        for (int child = Node.getFirstElement(node); child != 0; child = Node.getNextElement(child))
        {
            removeNamespacesRecursively(child, uriSet);
        }

        List<String> removeAttribs = new ArrayList<String>(16);
        List<String[]> addAttribs = new ArrayList<String[]>(16);

        for (int i = 0, count = Node.getNumAttributes(node); i < count; i++)
        {
            String name = Node.getAttributeName(node, i + 1);

            if ((name.startsWith("xmlns:") && (name.length() > 6)) || name.equals("xmlns"))
            {
                if ((uriSet == null) || uriSet.contains(Node.getAttribute(node, name)))
                {
                    removeAttribs.add(name);
                }
            }
            else
            {
                String uri = Node.getAttributeNamespaceURI(node, i + 1);

                if (uri != null)
                {
                    if ((uriSet == null) || uriSet.contains(uri))
                    {
                        addAttribs.add(new String[]
                                       {
                                           Node.getAttributeLocalName(node, i + 1),
                                           Node.getAttribute(node, name)
                                       });
                    }
                }
            }
        }

        for (String attr : removeAttribs)
        {
            Node.removeAttribute(node, attr);
        }

        for (String[] attr : addAttribs)
        {
            Node.setAttribute(node, attr[0], attr[1]);
        }
    }
}
