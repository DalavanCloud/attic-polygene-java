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

package org.apache.polygene.library.restlet.resource;

import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueBuilderFactory;
import org.apache.polygene.library.restlet.FormField;
import org.apache.polygene.library.restlet.RestForm;
import org.apache.polygene.library.restlet.RestLink;
import org.apache.polygene.library.restlet.identity.IdentityManager;
import org.apache.polygene.library.restlet.repository.RepositoryLocator;
import org.restlet.data.Method;

@Mixins( CreationResource.Mixin.class )
public interface CreationResource<T extends HasIdentity> extends ServerResource<T>
{
    abstract class Mixin<T extends HasIdentity>
        implements CreationResource<T>
    {
        @Structure
        private ValueBuilderFactory vbf;

        @Structure
        private UnitOfWorkFactory uowf;

        @This
        private Parameters<T> parameters;

        @Service
        private ResourceBuilder resourceBuilder;

        @Service
        private IdentityManager identityManager;

        @Service
        private RepositoryLocator locator;

        @Override
        public RestLink post( RestForm form )
        {
            String name = form.field( "name" ).value().get();
            Class<? extends HasIdentity> entityType = parameters.entityType().get();
            Identity identity = identityManager.generate( entityType, name );
            locator.find( entityType ).create( identity );
            doParameterization( form, entityType, identity );
            return resourceBuilder.createBaseLink( identity.toString(), parameters.request().get().getResourceRef(), Method.GET, "" );
        }

        private <P> void doParameterization( RestForm form, Class entityType, Identity identity )
        {
            if( !CreationParameterized.class.isAssignableFrom( entityType ) )
            {
                return;
            }
            @SuppressWarnings( "unchecked" )
            CreationParameterized<P> created = (CreationParameterized<P>) locator.find( entityType ).get( identity );
            P parameterization = createParameterizationValue( form, created );
            created.parameterize( parameterization );
        }

        private <V> V createParameterizationValue( final RestForm form, CreationParameterized<V> created )
        {
            Class<V> valueType = created.parametersType();
            ValueBuilder<V> vb = vbf.newValueBuilderWithState(
                valueType,
                propertyName -> mapField( form, propertyName ),
                association -> null,
                association -> null,
                association -> null
                                                             );
            return vb.newInstance();
        }

        private Object mapField( RestForm form, PropertyDescriptor propertyName )
        {
            String name = propertyName.qualifiedName().name();
            FormField field = form.field( name );
            if( field == null )
            {
                UnitOfWork uow = uowf.currentUnitOfWork();
                String usecase = "";
                if( uow != null )
                {
                    usecase = uow.usecase().name();
                }
                throw new IllegalArgumentException( "Field named '" + name + "' is required and not present in usecase " + usecase );
            }
            return field.value().get();
        }
    }
}
