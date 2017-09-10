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
package org.apache.polygene.entitystore.sql;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import javax.sql.DataSource;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.usecase.UsecaseBuilder;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.sql.assembly.AbstractSQLEntityStoreAssembler;
import org.apache.polygene.entitystore.sql.assembly.MySQLEntityStoreAssembler;
import org.apache.polygene.library.sql.assembly.DataSourceAssembler;
import org.apache.polygene.library.sql.datasource.DataSourceConfiguration;
import org.apache.polygene.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.docker.DockerRule;
import org.apache.polygene.test.entity.AbstractEntityStoreTest;
import org.junit.ClassRule;

public class MySQLEntityStoreTest
    extends AbstractEntityStoreTest
{
    @ClassRule
    public static final DockerRule DOCKER = new DockerRule(
        "mysql",
        new HashMap<String, String>()
        {{
            put( "MYSQL_ROOT_PASSWORD", "" );
            put( "MYSQL_ALLOW_EMPTY_PASSWORD", "yes" );
            put( "MYSQL_DATABASE", "jdbc_test_db" );
            put( "MYSQL_ROOT_HOST", "172.17.0.1" );
        }},
        30000L
//        , "mysqld: ready for connections"   TODO: add this after next release of tdomzal/junit-docker-rule
    );

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // END SNIPPET: assembly
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( config );

        // START SNIPPET: assembly
        // DataSourceService
        new DBCPDataSourceServiceAssembler()
            .identifiedBy( "mysql-datasource-service" )
            .visibleIn( Visibility.module )
            .withConfig( config, Visibility.layer )
            .assemble( module );

        // DataSource
        new DataSourceAssembler()
            .withDataSourceServiceIdentity( "mysql-datasource-service" )
            .identifiedBy( "mysql-datasource" )
            .visibleIn( Visibility.module )
            .withCircuitBreaker()
            .assemble( module );

        // SQL EntityStore
        new MySQLEntityStoreAssembler()
            .visibleIn( Visibility.application )
            .withConfig( config, Visibility.layer )
            .assemble( module );
        // END SNIPPET: assembly
        String mysqlHost = DOCKER.getDockerHost();
        int mysqlPort = DOCKER.getExposedContainerPort( "3306/tcp" );
        config.forMixin( DataSourceConfiguration.class ).declareDefaults()
              .url().set( "jdbc:mysql://" + mysqlHost + ":" + mysqlPort
                          + "/jdbc_test_db?profileSQL=false&useLegacyDatetimeCode=false&serverTimezone=UTC"
                          + "&nullCatalogMeansCurrent=true&nullNamePatternMatchesAll=true" );
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    @Override
    public void tearDown()
        throws Exception
    {
        UnitOfWork uow = this.unitOfWorkFactory.newUnitOfWork(
            UsecaseBuilder.newUsecase( "Delete " + getClass().getSimpleName() + " test data" )
                                                             );
        try
        {
            Connection connection = serviceFinder.findService( DataSource.class ).get().getConnection();
            SqlEntityStoreConfiguration config = uow.get( SqlEntityStoreConfiguration.class,
                                                          AbstractSQLEntityStoreAssembler.DEFAULT_ENTITYSTORE_IDENTITY );
            connection.setAutoCommit( false );
            try( Statement stmt = connection.createStatement() )
            {
                stmt.execute( String.format( "DROP DATABASE FROM %s", config.schemaName().get() ) );
                connection.commit();
            }
        }
        finally
        {
            uow.discard();
            super.tearDown();
        }
    }
}
