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

import java.net.URL;

/**
 * General utility methods.
 *
 * @author  mpoyhone
 */
public class Utils
{
    /**
     * Returns the path portion from the given URL. This includes the path, query and fragment.
     *
     * @param   url  URL.
     *
     * @return  Path from the URL.
     */
    public static String getUrlPath(URL url)
    {
        StringBuilder buf = new StringBuilder(80);
        String tmp;

        if ((tmp = url.getPath()) != null)
        {
            if (!tmp.startsWith("/"))
            {
                buf.append("/");
            }

            buf.append(tmp);
        }

        if ((tmp = url.getQuery()) != null)
        {
            buf.append("?");
            buf.append(tmp);
        }

        if ((tmp = url.getRef()) != null)
        {
            buf.append("#");
            buf.append(tmp);
        }

        return buf.toString();
    }
}
