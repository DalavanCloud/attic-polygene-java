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
import org.apache.polygene.test.indexing.AbstractEntityFinderTest;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeEach;

public class PostgreSQLEntityFinderTest
    extends AbstractEntityFinderTest
{
    @ClassRule
    public static final DockerRule DOCKER = new DockerRule( "postgres", 3000L, "PostgreSQL init process complete; ready for start up." );

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
}
