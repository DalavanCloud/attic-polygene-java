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
package org.qi4j.samples.forum.domainevent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;
import org.qi4j.library.rest.server.api.ObjectSelection;
import org.restlet.Request;

/**
 * TODO
 */
@Concerns( DomainEvent.DomainEventConcern.class )
@Retention( RetentionPolicy.RUNTIME )
public @interface DomainEvent
{
    class DomainEventConcern
        extends GenericConcern
    {
        @Structure
        Module module;

        @Structure
        Application application;

        @Override
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            Object result = next.invoke( proxy, method, args );

            UnitOfWork unitOfWork = module.currentUnitOfWork();

            ValueBuilder<DomainEventValue> builder = module.newValueBuilder( DomainEventValue.class );
            DomainEventValue prototype = builder.prototype();
            prototype.version().set( application.version() );
            prototype.timestamp().set( unitOfWork.currentTime() );
            prototype.context().set( proxy.getClass().getSuperclass().getName().split( "\\$" )[ 0 ] );
            prototype.name().set( method.getName() );

            int idx = 0;
            for( Object arg : args )
            {
                idx++;
                String name = "param" + idx;
                ValueBuilder<ParameterValue> parameterBuilder = module.newValueBuilder( ParameterValue.class );
                parameterBuilder.prototype().name().set( name );
                parameterBuilder.prototype().value().set( arg );
                prototype.parameters().get().add( parameterBuilder.newInstance() );
            }

            Iterables.addAll( prototype.selection().get(), Iterables.map( new Function<Object, String>()
            {
                @Override
                public String map( Object o )
                {
                    return o.toString();
                }
            }, ObjectSelection.current().selection() ) );

            final DomainEventValue domainEvent = builder.newInstance();

            unitOfWork.addUnitOfWorkCallback( new UnitOfWorkCallback()
            {
                @Override
                public void beforeCompletion()
                    throws UnitOfWorkCompletionException
                {
                }

                @Override
                public void afterCompletion( UnitOfWorkStatus status )
                {
                    if( status.equals( UnitOfWorkStatus.COMPLETED ) )
                    {
                        Request.getCurrent().getAttributes().put( "event", domainEvent );
                    }
                }
            } );

            return result;
        }
    }
}
