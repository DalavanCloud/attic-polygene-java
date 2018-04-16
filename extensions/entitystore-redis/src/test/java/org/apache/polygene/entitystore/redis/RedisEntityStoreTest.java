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
package org.apache.polygene.entitystore.redis;

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.redis.assembly.RedisEntityStoreAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.docker.DockerRule;
import org.apache.polygene.test.entity.AbstractEntityStoreTest;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeEach;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisEntityStoreTest
    extends AbstractEntityStoreTest
{
    @ClassRule
    public static final DockerRule DOCKER = new DockerRule( "redis", 6379 );

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
        new RedisEntityStoreAssembler().withConfig( config, Visibility.layer ).assemble( module );
        // END SNIPPET: assembly
        RedisEntityStoreConfiguration redisConfig = config.forMixin( RedisEntityStoreConfiguration.class )
                                                          .declareDefaults();
        redisConfig.host().set( DOCKER.getDockerHost() );
        redisConfig.port().set( DOCKER.getExposedContainerPort( "6379/tcp" ) );
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    private JedisPool jedisPool;

    @Override
    @BeforeEach
    public void setUp()
        throws Exception
    {
        super.setUp();
        RedisEntityStoreService es = serviceFinder.findService( RedisEntityStoreService.class ).get();
        jedisPool = es.jedisPool();
    }

    @Override
    public void tearDown()
    {
        try( Jedis jedis = jedisPool.getResource() )
        {
            jedis.flushDB();
        }
        super.tearDown();
    }
}
