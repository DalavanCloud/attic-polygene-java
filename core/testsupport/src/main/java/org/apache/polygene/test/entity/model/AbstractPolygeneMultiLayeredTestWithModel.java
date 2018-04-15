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
package org.apache.polygene.test.entity.model;

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.composite.TransientBuilderFactory;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.object.ObjectFactory;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.ApplicationDescriptor;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.value.ValueBuilderFactory;
import org.apache.polygene.bootstrap.ApplicationAssembly;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.LayerAssembly;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.memory.MemoryEntityStoreService;
import org.apache.polygene.spi.serialization.JsonSerialization;
import org.apache.polygene.test.AbstractPolygeneBaseTest;
import org.apache.polygene.test.entity.model.legal.LegalService;
import org.apache.polygene.test.entity.model.legal.Will;
import org.apache.polygene.test.entity.model.legal.WillAmount;
import org.apache.polygene.test.entity.model.legal.WillItem;
import org.apache.polygene.test.entity.model.legal.WillPercentage;
import org.apache.polygene.test.entity.model.monetary.CheckBookSlip;
import org.apache.polygene.test.entity.model.monetary.Currency;
import org.apache.polygene.test.entity.model.people.Address;
import org.apache.polygene.test.entity.model.people.City;
import org.apache.polygene.test.entity.model.people.Country;
import org.apache.polygene.test.entity.model.people.PeopleRepository;
import org.apache.polygene.test.entity.model.people.Person;
import org.apache.polygene.test.entity.model.people.PhoneNumber;
import org.apache.polygene.test.entity.model.people.Rent;

public abstract class AbstractPolygeneMultiLayeredTestWithModel extends AbstractPolygeneBaseTest
{
    private static final String FRIEND = "Friend";
    private static final String COLLEAGUE = "Colleague";

    protected static final String ACCESS_LAYER = "Access Layer";
    protected static final String DOMAIN_LAYER = "Domain Layer";
    protected static final String INFRASTRUCTURE_LAYER = "Infrastructure Layer";
    protected static final String CONFIGURATION_LAYER = "Configuration Layer";
    protected static final String CONFIGURATION_MODULE = "Configuration Module";
    protected static final String SERIALIZATION_MODULE = "Serialization Module";
    protected static final String STORAGE_MODULE = "Storage Module";
    protected static final String MONETARY_MODULE = "Monetary Module";
    protected static final String PEOPLE_MODULE = "People Module";
    protected static final String LEGAL_MODULE = "Legal Module";
    protected static final String TEST_CASE_MODULE = "TestCase Module";

    protected ModuleAssembly configModule;

    @Structure
    protected Module configurationModule;

    @Structure
    protected ValueBuilderFactory valueBuilderFactory;

    @Structure
    protected TransientBuilderFactory transientBuilderFactory;

    @Structure
    protected UnitOfWorkFactory unitOfWorkFactory;

    @Structure
    protected ObjectFactory objectFactory;

    @Override
    protected void defineApplication( ApplicationAssembly applicationAssembly )
        throws AssemblyException
    {
        LayerAssembly accessLayer = applicationAssembly.layer( ACCESS_LAYER );
        LayerAssembly domainLayer = applicationAssembly.layer( DOMAIN_LAYER );
        LayerAssembly infrastructureLayer = applicationAssembly.layer( INFRASTRUCTURE_LAYER );
        LayerAssembly configLayer = applicationAssembly.layer( CONFIGURATION_LAYER );
        accessLayer.uses( domainLayer.uses( infrastructureLayer.uses( configLayer ) ) );
        defineConfigModule( configLayer.module( CONFIGURATION_MODULE ) );
        defineSerializationModule( configLayer.module( SERIALIZATION_MODULE ) );
        defineStorageModule( infrastructureLayer.module( STORAGE_MODULE ) );
        defineMonetaryModule( domainLayer.module( MONETARY_MODULE ) );
        definePeopleModule( domainLayer.module( PEOPLE_MODULE ) );
        defineLegalModule( domainLayer.module( LEGAL_MODULE ) );
        defineTestModule( accessLayer.module( TEST_CASE_MODULE ) );
    }

    @Override
    protected Application newApplicationInstance( ApplicationDescriptor applicationModel )
    {
        Application application = super.newApplicationInstance( applicationModel );
        Module module = application.findModule( "Access Layer", "TestCase Module" );
        module.injectTo( this );
        return application;
    }

    protected void defineTestModule( ModuleAssembly module )
    {
        module.defaultServices();
        module.objects( this.getClass() );
    }

    protected void definePeopleModule( ModuleAssembly module )
    {
        module.defaultServices();
        module.entities( Address.class, Country.class, City.class, PhoneNumber.class );
        module.entities( Person.class ).visibleIn( Visibility.layer );
        module.services( PeopleRepository.class ).visibleIn( Visibility.application );
        module.values( Rent.class );
        module.objects( Rent.Builder.class ).visibleIn( Visibility.application );
    }

    protected void defineLegalModule( ModuleAssembly module )
    {
        module.defaultServices();
        module.services( LegalService.class ).visibleIn( Visibility.application );
        module.entities( Will.class );
        module.values( WillAmount.class, WillItem.class, WillPercentage.class );
    }

    protected void defineMonetaryModule( ModuleAssembly module )
    {
        module.defaultServices();
        module.values( Currency.class ).visibleIn( Visibility.layer );
        module.transients( CheckBookSlip.class );
        module.transients( Currency.Builder.class ).visibleIn( Visibility.application );
    }

    protected void defineSerializationModule( ModuleAssembly module )
    {
        module.defaultServices();
        module.services( JsonSerialization.class ).visibleIn( Visibility.application );
    }

    protected abstract void defineStorageModule( ModuleAssembly module );

    protected void defineConfigModule( ModuleAssembly module )
    {
        module.defaultServices();
        module.services( MemoryEntityStoreService.class ).visibleIn( Visibility.module );
        configModule = module;
    }
}
