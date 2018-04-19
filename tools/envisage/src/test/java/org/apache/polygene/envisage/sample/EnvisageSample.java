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

package org.apache.polygene.envisage.sample;

import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.envisage.Envisage;
import org.apache.polygene.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.apache.polygene.test.util.Assume.assumeDisplayPresent;

public class EnvisageSample
    extends AbstractPolygeneTest
{

    public static void main( String[] args )
        throws Exception
    {
        EnvisageSample sample = new EnvisageSample();
        sample.runSample();
    }

    @BeforeAll
    public static void assumeDisplay()
    {
        assumeDisplayPresent();
    }

    @Test
    public void runSample()
        throws Exception
    {
        setUp();
        createTestData();
        //createTestData2();
        //createTestData3();

        new Envisage().run( applicationModel );
//        Thread.sleep( 1113000 );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( CarEntity.class );
        module.entities( AnimalEntity.class );
        new RdfMemoryStoreAssembler().assemble( module );
        new EntityTestAssembler().assemble( module );
    }

    public void createTestData()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            createCar( "Volvo", "S80", 2007 );
            createCar( "Volvo", "C70", 2006 );
            createCar( "Ford", "Transit", 2007 );
            createCar( "Ford", "Mustang", 2007 );
            createCar( "Ford", "Mustang", 2006 );
            createCar( "Ford", "Mustang", 2005 );

            createAnimal( "Cat", "Miaow" );
            createAnimal( "Duck", "Kwek Kwek" );
            createAnimal( "Dog", "Guk Guk" );
            createAnimal( "Cow", "Moooo" );

            uow.complete();
        }
        catch( UnitOfWorkCompletionException e )
        {
            // Can not happen.
            e.printStackTrace();
        }
    }

    private Identity createCar(String manufacturer, String model, int year )
    {
        UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();
        EntityBuilder<Car> builder = uow.newEntityBuilder( Car.class );
        Car prototype = builder.instanceFor( CarEntity.class );
        prototype.manufacturer().set( manufacturer );
        prototype.model().set( model );
        prototype.year().set( year );
        CarEntity entity = (CarEntity) builder.newInstance();
        return entity.identity().get();
    }

    private Identity createAnimal( String name, String sound )
    {
        UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();
        EntityBuilder<Animal> builder = uow.newEntityBuilder( Animal.class );
        Animal prototype = builder.instanceFor( AnimalEntity.class );
        prototype.name().set( name );
        prototype.sound().set( sound );
        AnimalEntity entity = (AnimalEntity) builder.newInstance();
        return entity.identity().get();
    }

    public interface Car
    {
        Property<String> manufacturer();

        Property<String> model();

        Property<Integer> year();
    }

    public interface CarEntity
        extends Car, HasIdentity
    {
    }

    public interface Animal
    {
        Property<String> name();

        Property<String> sound();
    }

    public interface AnimalEntity
        extends Animal, HasIdentity
    {
    }

}
