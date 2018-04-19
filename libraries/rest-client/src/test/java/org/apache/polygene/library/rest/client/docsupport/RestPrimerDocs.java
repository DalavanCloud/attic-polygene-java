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
package org.apache.polygene.library.rest.client.docsupport;

import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.library.rest.client.api.ContextResourceClient;
import org.apache.polygene.library.rest.client.api.ContextResourceClientFactory;
import org.apache.polygene.library.rest.client.api.ErrorHandler;
import org.apache.polygene.library.rest.client.api.HandlerCommand;
import org.apache.polygene.library.rest.client.spi.NullResponseHandler;
import org.apache.polygene.library.rest.client.spi.ResponseHandler;
import org.apache.polygene.library.rest.client.spi.ResultHandler;
import org.apache.polygene.library.rest.common.Resource;
import org.hamcrest.CoreMatchers;
import org.restlet.Client;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;

import static org.apache.polygene.library.rest.client.api.HandlerCommand.query;
import static org.apache.polygene.library.rest.client.api.HandlerCommand.refresh;
import static org.hamcrest.MatcherAssert.assertThat;

public class RestPrimerDocs
{

    private Module module;

    private ContextResourceClient crc;

    {
// START SNIPPET: 1
    crc.onResource( new ResultHandler<Resource>()
    {
        @Override
        public HandlerCommand handleResult( Resource result, ContextResourceClient client )
        {
            // This may throw IAE if no link with relation
            // "querywithoutvalue" is found in the Resource
            return query( "querywithoutvalue", null );
        }
    } ).
    onQuery( "querywithoutvalue", new ResultHandler<TestResult>()
    {
        @Override
        public HandlerCommand handleResult( TestResult result, ContextResourceClient client )
        {
            assertThat( result.xyz().get(), CoreMatchers.equalTo( "bar" ) );
            return null;
        }
    } );

    crc.start();
// END SNIPPET: 1
    }

    {
// START SNIPPET: 2
// Create Restlet client and bookmark Reference
    Client client = new Client( Protocol.HTTP );
    Reference ref = new Reference( "http://localhost:8888/" );
    ContextResourceClientFactory contextResourceClientFactory = module.newObject( ContextResourceClientFactory.class, client, new NullResponseHandler() );
    contextResourceClientFactory.setAcceptedMediaTypes( MediaType.APPLICATION_JSON );

// Handle logins
    contextResourceClientFactory.setErrorHandler( new ErrorHandler().onError( ErrorHandler.AUTHENTICATION_REQUIRED, new ResponseHandler()
    {
        // Only try to login once
        boolean tried = false;

        @Override
        public HandlerCommand handleResponse( Response response, ContextResourceClient client )
        {
            // If we have already tried to login, fail!
            if (tried)
                throw new ResourceException( response.getStatus() );

            tried = true;
            client.getContextResourceClientFactory().getInfo().setUser( new User("rickard", "secret") );

            // Try again
            return refresh();
        }
    } ).onError( ErrorHandler.RECOVERABLE_ERROR, new ResponseHandler()
    {
        @Override
        public HandlerCommand handleResponse( Response response, ContextResourceClient client )
        {
            // Try to restart this scenario
            return refresh();
        }
    } ) );

    crc = contextResourceClientFactory.newClient( ref );
    // END SNIPPET: 2
    }

    public interface TestResult
            extends ValueComposite
    {
        Property<String> xyz();
    }

}