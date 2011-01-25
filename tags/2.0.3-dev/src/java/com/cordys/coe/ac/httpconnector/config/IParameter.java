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
 package com.cordys.coe.ac.httpconnector.config;

/**
 * This interface describes the connection parameters that can be configured in the connection
 * store.
 *
 * @author  pgussow
 */
public interface IParameter
{
    /**
     * Holds the name of the attribute 'type'.
     */
    String ATTRIBUTE_TYPE = "type";
    /**
     * Holds the name of the tag 'name'.
     */
    String TAG_NAME = "name";
    /**
     * Holds the name of the tag 'value'.
     */
    String TAG_VALUE = "value";

    /**
     * This method cleans the parameter. If the parameter holds XML it is deleted.
     */
    void clean();

    /**
     * This method gets the name of the parameter.
     *
     * @return  The name of the parameter.
     */
    String getName();

    /**
     * This method gets the type of the parameter.
     *
     * @return  The type of the parameter.
     */
    EParameterType getType();

    /**
     * This method gets the value for this parameter.
     *
     * @return  The value for this parameter.
     */
    Object getValue();

    /**
     * This method sets the name of the parameter.
     *
     * @param  name  The name of the parameter.
     */
    void setName(String name);

    /**
     * This method sets the type of the parameter.
     *
     * @param  type  The type of the parameter.
     */
    void setType(EParameterType type);

    /**
     * This method sets the value for this parameter.
     *
     * @param  value  The value for this parameter.
     */
    void setValue(Object value);
}
