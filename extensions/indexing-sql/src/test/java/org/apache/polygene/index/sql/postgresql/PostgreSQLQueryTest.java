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
package org.apache.polygene.index.sql.postgresql;

import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.docker.DockerRule;
import org.apache.polygene.test.indexing.AbstractQueryTest;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * PostgreSQL Query Tests.
 * <p>Many features are not supported.</p>
 */
// See org.apache.polygene.index.sql.support.skeletons.SQLCompatEntityStateWrapper that filter out unsupported properties.
public class PostgreSQLQueryTest
    extends AbstractQueryTest
{
    @ClassRule
    public static final DockerRule DOCKER = new DockerRule( "postgres",
                                                            10000L,
                                                            "PostgreSQL init process complete; ready for start up." );

    @Override
    public void assemble( ModuleAssembly mainModule )
        throws AssemblyException
    {
        super.assemble( mainModule );
        String host = DOCKER.getDockerHost();
        int port = DOCKER.getExposedContainerPort( "5432/tcp" );
        SQLTestHelper.assembleWithMemoryEntityStore( mainModule, host, port );
    }

    @Override
    @BeforeEach
    public void setUp()
        throws Exception
    {
        try
        {
            super.setUp();
        }
        catch( Exception e )
        {
            // Let's check if exception was because database was not available
            if( this.module != null )
            {
                SQLTestHelper.setUpTest( this.serviceFinder );
            }

            // If we got this far, the database must have been available, and exception must have
            // had other reason!
            throw e;
        }
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script04_ne()
    {
        super.script04_ne();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script12_ne()
    {
        super.script04_ne();
    }

    @Test
    @Ignore( "NamedAssociation are not supported by SQL Indexing" )
    @Override
    public void script35()
    {
        super.script35();
    }

    @Test
    @Ignore( "NamedAssociation are not supported by SQL Indexing" )
    @Override
    public void script36()
    {
        super.script36();
    }

    @Test
    @Ignore( "Queries on Enums are not supported by SQL Indexing" )
    @Override
    public void script38()
    {
        super.script38();
    }

    @Test
    @Ignore( "Queries on Enums and NeSpecification are not supported by SQL Indexing" )
    @Override
    public void script39()
    {
        super.script39();
    }

    @Test
    @Ignore( "Date is not supported by SQL Indexing" )
    @Override
    public void script40_Date()
    {
        super.script40_Date();
    }

    @Test
    @Ignore( "DateTime is not supported by SQL Indexing" )
    @Override
    public void script40_DateTime()
    {
        super.script40_DateTime();
    }

    @Test
    @Ignore( "LocalDate is not supported by SQL Indexing" )
    @Override
    public void script40_LocalDate()
    {
        super.script40_LocalDate();
    }

    @Test
    @Ignore( "LocalDateTime is not supported by SQL Indexing" )
    @Override
    public void script40_LocalDateTime()
    {
        super.script40_LocalDateTime();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script41_Instant()
    {
        super.script41_Instant();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script41_DateTime()
    {
        super.script41_DateTime();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script41_LocalDate()
    {
        super.script41_LocalDate();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script41_LocalDateTime()
    {
        super.script41_LocalDateTime();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script42_Instant()
    {
        super.script42_Instant();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script42_DateTime()
    {
        super.script42_DateTime();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script42_LocalDate()
    {
        super.script42_LocalDate();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script42_LocalDateTime()
    {
        super.script42_LocalDateTime();
    }

    @Test
    @Ignore( "Date is not supported by SQL Indexing" )
    @Override
    public void script43_Date()
    {
        super.script43_Date();
    }

    @Test
    @Ignore( "DateTime is not supported by SQL Indexing" )
    @Override
    public void script43_DateTime()
    {
        super.script43_DateTime();
    }

    @Test
    @Ignore( "LocalDate is not supported by SQL Indexing" )
    @Override
    public void script43_LocalDate()
    {
        super.script43_LocalDate();
    }

    @Test
    @Ignore( "LocalDateTime is not supported by SQL Indexing" )
    @Override
    public void script43_LocalDateTime()
    {
        super.script43_LocalDateTime();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script51_BigInteger()
    {
        super.script51_BigInteger();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script51_BigDecimal()
    {
        super.script51_BigDecimal();
    }
}
