/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.library.restlet.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceReference;

@Mixins( RepositoryLocator.Mixin.class )
public interface RepositoryLocator
{
    <T extends HasIdentity> CrudRepository<T> find(Class<T> entityType );

    class Mixin
        implements RepositoryLocator
    {

        private Map<Class, CrudRepository> repositories = new HashMap<>();

        public Mixin( @Service Iterable<ServiceReference<CrudRepository>> repositories )
        {
            StreamSupport.stream( repositories.spliterator(), true ).forEach( ref -> {
                Class type = ref.metaInfo( EntityTypeDescriptor.class ).entityType();
                this.repositories.put( type, ref.get() );
            } );
        }

        @Override
        public <T extends HasIdentity> CrudRepository<T> find(Class<T> entityType )
        {
            @SuppressWarnings( "unchecked" )
            CrudRepository<T> repository = repositories.get( entityType );
            if( repository == null )
            {
                throw new MissingRepositoryException( entityType );
            }
            return repository;
        }
    }
}
