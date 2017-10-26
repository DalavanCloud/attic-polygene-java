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

import java.io.IOException;
import java.util.Collections;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.object.ObjectFactory;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueBuilderFactory;
import org.apache.polygene.library.restlet.Command;
import org.apache.polygene.library.restlet.FormField;
import org.apache.polygene.library.restlet.RestForm;
import org.apache.polygene.library.restlet.RestLink;
import org.apache.polygene.library.restlet.crud.EntityRef;
import org.apache.polygene.library.restlet.identity.IdentityManager;
import org.apache.polygene.library.restlet.serialization.PolygeneConverter;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.routing.Route;
import org.restlet.routing.Router;

@Mixins( ResourceBuilder.Mixin.class )
public interface ResourceBuilder
{
    EntityRef createEntityRef( Identity name, Reference base );

    EntityRef createEntityRef( Identity name, RestLink get, RestLink put, RestLink delete );

    RestLink createBaseLink( String name, Reference base, Method method, String description );

    RestLink createLeafLink( String name, Reference base, Method method, String description );

    Command createCommand( Reference base );

    RestForm createNameForm( Reference base, String formName );

    FormField createFormField( String name, String type );

    <T extends HasIdentity> Representation toRepresentation( Class<T> type, T composite );

    <T extends HasIdentity> T toObject( Class<T> type, Representation representation )
        throws IOException;

    Route findRoute( String name, Router router );

    class Mixin
        implements ResourceBuilder
    {
        @Service
        private IdentityManager identityManager;

        private final PolygeneConverter converter;

        @Structure
        private ValueBuilderFactory vbf;

        public Mixin( @Structure ObjectFactory objectFactory )
        {
            converter = new PolygeneConverter( objectFactory );
        }

        @Override
        public EntityRef createEntityRef( Identity identity, Reference base )
        {
            String name = identityManager.extractName( identity );

            RestLink get = createBaseLink( name, base, Method.GET, "Fetch " + name );
            RestLink put = createBaseLink( name, base, Method.PUT, "Save " + name );
            RestLink delete = createBaseLink( name, base, Method.DELETE, "Delete " + name );
            return createEntityRef( identity, get, put, delete );
        }

        @Override
        public EntityRef createEntityRef( Identity identity, RestLink get, RestLink put, RestLink delete )
        {
            ValueBuilder<EntityRef> refBuilder = vbf.newValueBuilder( EntityRef.class );
            EntityRef refPrototype = refBuilder.prototype();
            refPrototype.name().set( identityManager.extractName( identity ) );
            refPrototype.get().set( get );
            refPrototype.put().set( put );
            refPrototype.delete().set( delete );
            return refBuilder.newInstance();
        }

        @Override
        public RestLink createBaseLink( String name, Reference base, Method method, String description )
        {
            ValueBuilder<RestLink> builder = vbf.newValueBuilder( RestLink.class );
            RestLink prototype = builder.prototype();
            String path = base.toUri().resolve( name ).getPath();
            prototype.path().set( path.endsWith( "/" ) ? path : path + "/" );
            prototype.method().set( method.getName() );
            prototype.description().set( description );
            return builder.newInstance();
        }

        @Override
        public RestLink createLeafLink( String name, Reference base, Method method, String description )
        {
            ValueBuilder<RestLink> builder = vbf.newValueBuilder( RestLink.class );
            RestLink prototype = builder.prototype();
            String path = base.toUri().resolve( name ).getPath();
            prototype.path().set( path );
            prototype.method().set( method.getName() );
            prototype.description().set( description );
            return builder.newInstance();
        }

        public Command createCommand( Reference base )
        {
            RestForm form = createNameForm( base, "create" );
            ValueBuilder<Command> builder = vbf.newValueBuilder( Command.class );
            builder.prototype().name().set( "create" );
            builder.prototype().form().set( form );
            return builder.newInstance();
        }

        public RestForm createNameForm( Reference base, String formName )
        {
            ValueBuilder<RestForm> builder = vbf.newValueBuilder( RestForm.class );
            builder.prototype().link().set( createBaseLink( formName, base, Method.POST, "" ) );
            builder.prototype().fields().set( Collections.singletonList( createFormField( "name", FormField.TEXT ) ) );
            return builder.newInstance();
        }

        public FormField createFormField( String name, String type )
        {
            ValueBuilder<FormField> builder = vbf.newValueBuilder( FormField.class );
            builder.prototype().name().set( name );
            builder.prototype().type().set( type );
            return builder.newInstance();
        }

        @Override
        public <T extends HasIdentity> Representation toRepresentation( Class<T> type, T composite )
        {
            return converter.toRepresentation( composite, new Variant(), null );
        }

        @Override
        public <T extends HasIdentity> T toObject( Class<T> type, Representation representation )
            throws IOException
        {
            return converter.toObject( representation, type, null );
        }

        @Override
        public Route findRoute( String name, Router router )
        {
            return router.getRoutes().stream().filter( route -> name.equals( route.getName() ) ).findFirst().orElse( null );
        }
    }
}
