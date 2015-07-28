/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.api.util;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.qi4j.functional.Function;
import org.qi4j.functional.Specification;

import static org.qi4j.functional.Iterables.iterable;

/**
 * Useful methods for handling Methods.
 */
public class Methods
{
    public static final Specification<Type> HAS_METHODS = new Specification<Type>()
    {
        @Override
        public boolean satisfiedBy( Type item )
        {
            return Classes.RAW_CLASS.map( item ).getDeclaredMethods().length > 0;
        }
    };

    public static final Function<Type, Iterable<Method>> METHODS_OF = Classes.forTypes( new Function<Type, Iterable<Method>>()
    {
        @Override
        public Iterable<Method> map( Type type )
        {
            return iterable( Classes.RAW_CLASS.map( type ).getDeclaredMethods() );
        }
    } );
}
