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

package org.apache.polygene.runtime;

import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

/**
 * JAVADOC
 */
public class PolygeneAPITest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.transients( TestTransient.class );
        module.entities( TestEntity.class );
        module.values( TestValue.class );
        module.services( TestService.class );
    }

    @Test
    public void testGetModuleOfComposite()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        TestEntity testEntity = unitOfWork.newEntity( TestEntity.class );

        api.moduleOf( testEntity );

        unitOfWork.discard();

        api.moduleOf( valueBuilderFactory.newValue( TestValue.class ) );

        api.moduleOf( transientBuilderFactory.newTransient( TestTransient.class ) );

        api.moduleOf( serviceFinder.findService( TestService.class ).get() );
    }

    public interface TestTransient
        extends TransientComposite
    {
    }

    public interface TestEntity
        extends EntityComposite
    {
    }

    public interface TestValue
        extends ValueComposite
    {
    }

    public interface TestService
        extends ServiceComposite
    {
    }
}