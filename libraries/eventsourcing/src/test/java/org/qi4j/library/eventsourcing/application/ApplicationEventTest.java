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
package org.qi4j.library.eventsourcing.application;

import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.io.Output;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;
import org.qi4j.library.eventsourcing.application.api.ApplicationEvent;
import org.qi4j.library.eventsourcing.application.api.TransactionApplicationEvents;
import org.qi4j.library.eventsourcing.application.factory.ApplicationEventCreationConcern;
import org.qi4j.library.eventsourcing.application.source.ApplicationEventSource;
import org.qi4j.library.eventsourcing.application.source.helper.ApplicationEventParameters;
import org.qi4j.library.eventsourcing.application.source.memory.MemoryApplicationEventStoreService;
import org.qi4j.library.eventsourcing.bootstrap.EventsourcingAssembler;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * User signup usecase with optional mailing list subscription.
 * Subscription is not stored in domain model but is available via application events feed.
 */
public class ApplicationEventTest
    extends AbstractQi4jTest
{
    @Service
    ApplicationEventSource eventSource;

    @Override
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        // START SNIPPET: assemblyAE
        new EventsourcingAssembler()
                .withApplicationEvents()
                .withCurrentUserFromUOWPrincipal()
                .assemble(module);
        // END SNIPPET: assemblyAE

        // START SNIPPET: storeAE
        module.services( MemoryApplicationEventStoreService.class );
        // END SNIPPET: storeAE

        new EntityTestAssembler().assemble( module );

        // START SNIPPET: concernAE
        module.transients( Users.class ).withConcerns( ApplicationEventCreationConcern.class );
        // END SNIPPET: concernAE

        module.entities( UserEntity.class );

    }


    @Test
    public void testApplicationEvent() throws Exception
    {
        Users users = module.newTransient( Users.class );

        Principal administratorPrincipal = new Principal()
        {
            @Override
            public String getName()
            {
                return "administrator";
            }
        };

        UnitOfWork uow1 = module.newUnitOfWork( UsecaseBuilder.newUsecase( "User signup" ) );
        uow1.setMetaInfo( administratorPrincipal );
        users.signup( null, "user1", Arrays.asList( "news-a", "news-b" ) );
        uow1.complete();

        Thread.sleep( 1 ); // For UoWs not getting the same `currentTime`

        UnitOfWork uow2 = module.newUnitOfWork();
        uow2.setMetaInfo( administratorPrincipal );
        users.signup( null, "user2", Collections.EMPTY_LIST );
        uow2.complete();

        Thread.sleep( 1 ); // For UoWs not getting the same `currentTime`

        UnitOfWork uow3 = module.newUnitOfWork();
        uow3.setMetaInfo( administratorPrincipal );
        users.signup( null, "user3", Collections.singletonList( "news-c" ) );
        uow3.complete();

        // receive events from uow2 and later forwards
        EventsInbox afterInbox = new EventsInbox();
        eventSource.transactionsAfter( uow2.currentTime() - 1, Integer.MAX_VALUE ).transferTo( afterInbox );

        assertEquals( 2, afterInbox.getEvents().size() );

        ApplicationEvent signupEvent2 = afterInbox.getEvents().get( 0 ).events().get().get( 0 );
        ApplicationEvent signupEvent3 = afterInbox.getEvents().get( 1 ).events().get().get( 0 );

        assertEquals( "signup", signupEvent2.name().get() );
        assertEquals( "user2", ApplicationEventParameters.getParameter( signupEvent2, "param1" ) );
        assertEquals( "[]", ApplicationEventParameters.getParameter( signupEvent2, "param2" ) );

        assertEquals( "signup", signupEvent3.name().get() );
        assertEquals( "user3", ApplicationEventParameters.getParameter( signupEvent3, "param1" ) );
        assertEquals( "[\"news-c\"]", ApplicationEventParameters.getParameter( signupEvent3, "param2" ) );

        // receive events from uow2 backwards
        EventsInbox beforeInbox = new EventsInbox();
        eventSource.transactionsBefore( uow3.currentTime(), Integer.MAX_VALUE ).transferTo( beforeInbox );

        assertEquals( 2, beforeInbox.getEvents().size() );

        signupEvent2 = beforeInbox.getEvents().get( 0 ).events().get().get( 0 );
        ApplicationEvent signupEvent1 = beforeInbox.getEvents().get( 1 ).events().get().get( 0 );

        assertEquals( "signup", signupEvent2.name().get() );
        assertEquals( "user2", ApplicationEventParameters.getParameter( signupEvent2, "param1" ) );
        assertEquals( "[]", ApplicationEventParameters.getParameter( signupEvent2, "param2" ) );

        assertEquals( "signup", signupEvent1.name().get());
        assertEquals( "user1", ApplicationEventParameters.getParameter( signupEvent1, "param1" ) );
        assertEquals( "[\"news-a\",\"news-b\"]", ApplicationEventParameters.getParameter( signupEvent1, "param2" ) );
    }

    static class EventsInbox implements Output<TransactionApplicationEvents, RuntimeException>
    {
        private List<TransactionApplicationEvents> events = new LinkedList<>();

        @Override
        public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends TransactionApplicationEvents, SenderThrowableType> sender )
            throws RuntimeException, SenderThrowableType
        {
            try
            {
                sender.sendTo( new Receiver<TransactionApplicationEvents, Throwable>()
                {
                    @Override
                    public void receive( TransactionApplicationEvents item ) throws Throwable
                    {
                        events.add(item);
                    }
                });

            }
            catch( Throwable throwable )
            {
                throwable.printStackTrace();
            }
        }

        public List<TransactionApplicationEvents> getEvents()
        {
            return events;
        }
    }

    // START SNIPPET: methodAE
    @Mixins( Users.Mixin.class )
    public interface Users extends TransientComposite
    {
        void signup( @Optional ApplicationEvent evt, String username, List<String> mailinglists );
        // END SNIPPET: methodAE

        abstract class Mixin implements Users
        {
            @Structure
            UnitOfWorkFactory uowFactory;

            @Override
            public void signup( ApplicationEvent evt, String username, List<String> mailinglists )
            {
                if (evt == null)
                {
                    UnitOfWork uow = uowFactory.currentUnitOfWork();

                    EntityBuilder<UserEntity> builder = uow.newEntityBuilder( UserEntity.class );
                    builder.instance().username().set( username );
                    builder.newInstance();
                }
            }
        }
    }

    public interface UserEntity
            extends EntityComposite
    {
        @UseDefaults
        Property<String> username();
    }
}
