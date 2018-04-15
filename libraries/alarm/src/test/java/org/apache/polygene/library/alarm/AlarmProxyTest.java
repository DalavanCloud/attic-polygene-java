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

package org.apache.polygene.library.alarm;

import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AlarmProxyTest extends AbstractPolygeneTest
{
    // START SNIPPET: documentation
    @Service
    private AlarmProxy.Factory factory;

    private AlarmProxy myAlarmPoint;
    // END SNIPPET: documentation

    // START SNIPPET: documentation
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new AlarmSystemAssembler().assemble( module );
        // END SNIPPET: documentation
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void givenAlarmPointWhenActivateExpectActivationEvent()
        throws Exception
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
// START SNIPPET: documentation
            myAlarmPoint = factory.create( StringIdentity.identityOf( "This Alarm Identity" ), "ProActiveCRM", "Sales", AlarmClass.B );
            myAlarmPoint.history().maxSize().set( 20 );
// END SNIPPET: documentation

// START SNIPPET: documentation
            myAlarmPoint.activate();
// END SNIPPET: documentation
            uow.complete();
            assertThat( myAlarmPoint.history().activateCounter(), equalTo( 1 ) );
            AlarmEvent event = myAlarmPoint.history().firstEvent();
            assertThat( event, notNullValue() );
            assertThat( event.identity().get(), equalTo( StringIdentity.identityOf( "This Alarm Identity" ) ) );
            assertThat( event.newStatus().get().name( null ), equalTo( "Activated" ) );
            assertThat( event.oldStatus().get().name( null ), equalTo( "Normal" ) );
        }
        catch( Exception e )
        {
            uow.discard();
            throw e;
        }
    }
}
