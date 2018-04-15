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
package org.apache.polygene.index.rdf.qi66;

import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.index.rdf.query.RdfQueryParserFactory;
import org.apache.polygene.index.rdf.query.RdfQueryService;
import org.apache.polygene.library.rdf.entity.EntityStateSerializer;
import org.apache.polygene.library.rdf.entity.EntityTypeSerializer;
import org.apache.polygene.library.rdf.repository.MemoryRepositoryService;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test for Qi-66
 */
public class Qi66IssueTest
    extends AbstractPolygeneTest
{
    private static final String ACCOUNT_NAME = "polygene";

    @Test
    public final void testCompleteAfterFind()
        throws Exception
    {
        Identity accountIdentity = newPolygeneAccount();

        UnitOfWork work = unitOfWorkFactory.newUnitOfWork();
        AccountComposite account = work.get( AccountComposite.class, accountIdentity );
        assertThat( account, notNullValue() );

        try
        {
            work.complete();
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            fail( "No exception can be thrown." );
        }
    }

    /**
     * Creates a new Apache Polygene account.
     *
     * @return The reference of Polygene account.
     *
     * @throws UnitOfWorkCompletionException Thrown if creational fail.
     */
    private Identity newPolygeneAccount()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork work = unitOfWorkFactory.newUnitOfWork();
        EntityBuilder<AccountComposite> entityBuilder = work.newEntityBuilder( AccountComposite.class );
        AccountComposite accountComposite = entityBuilder.instance();
        accountComposite.name().set( ACCOUNT_NAME );
        accountComposite = entityBuilder.newInstance();
        Identity accoutnIdentity = accountComposite.identity().get();
        work.complete();

        return accoutnIdentity;
    }

    public final void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( AccountComposite.class );
        new EntityTestAssembler().assemble( module );
        module.services( RdfQueryService.class,
                         RdfQueryParserFactory.class,
                         MemoryRepositoryService.class );
        module.objects( EntityStateSerializer.class, EntityTypeSerializer.class );
    }
}
