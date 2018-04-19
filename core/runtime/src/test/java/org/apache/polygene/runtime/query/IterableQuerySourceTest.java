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
package org.apache.polygene.runtime.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.query.Query;
import org.apache.polygene.api.query.QueryBuilder;
import org.apache.polygene.api.query.QueryBuilderFactory;
import org.apache.polygene.api.query.QueryExpressions;
import org.apache.polygene.api.query.grammar.OrderBy;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ClassScanner;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.apache.polygene.runtime.query.model.City;
import org.apache.polygene.runtime.query.model.Describable;
import org.apache.polygene.runtime.query.model.Domain;
import org.apache.polygene.runtime.query.model.Female;
import org.apache.polygene.runtime.query.model.Male;
import org.apache.polygene.runtime.query.model.Nameable;
import org.apache.polygene.runtime.query.model.Person;
import org.apache.polygene.runtime.query.model.Pet;
import org.apache.polygene.runtime.query.model.entities.DomainEntity;
import org.apache.polygene.runtime.query.model.entities.PetEntity;
import org.apache.polygene.runtime.query.model.values.ContactValue;
import org.apache.polygene.runtime.query.model.values.ContactsValue;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.polygene.api.query.QueryExpressions.eq;
import static org.apache.polygene.api.query.QueryExpressions.ge;
import static org.apache.polygene.api.query.QueryExpressions.gt;
import static org.apache.polygene.api.query.QueryExpressions.isNotNull;
import static org.apache.polygene.api.query.QueryExpressions.isNull;
import static org.apache.polygene.api.query.QueryExpressions.lt;
import static org.apache.polygene.api.query.QueryExpressions.matches;
import static org.apache.polygene.api.query.QueryExpressions.not;
import static org.apache.polygene.api.query.QueryExpressions.or;
import static org.apache.polygene.api.query.QueryExpressions.orderBy;
import static org.apache.polygene.api.query.QueryExpressions.property;
import static org.apache.polygene.api.query.QueryExpressions.templateFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.fail;

public class IterableQuerySourceTest
{

    private UnitOfWork uow;
    private QueryBuilderFactory qbf;

    @BeforeEach
    public void setUp()
        throws UnitOfWorkCompletionException, ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                ClassScanner.findClasses( DomainEntity.class ).forEach( module::entities );

                module.values( ContactsValue.class, ContactValue.class );
                new EntityTestAssembler().assemble( module );
            }
        };
        uow = assembler.module().unitOfWorkFactory().newUnitOfWork();
        Network.populate( uow, assembler.module() );
        uow.complete();
        uow = assembler.module().unitOfWorkFactory().newUnitOfWork();
        Network.refresh( uow );
        qbf = assembler.module();
    }

    @AfterEach
    public void tearDown()
    {
        if( uow != null )
        {
            uow.discard();
        }
    }

    private static void verifyUnorderedResults( final Iterable<? extends Nameable> results,
                                                final String... names
    )
    {
        final List<String> expected = new ArrayList<String>( Arrays.asList( names ) );

        for( Nameable entity : results )
        {
            String name = entity.name().get();
            assertThat( name + " returned but not expected", expected.remove( name ), is( true ) );
        }

        for( String notReturned : expected )
        {
            fail( notReturned + " was expected but not returned" );
        }
    }

    private static void verifyOrderedResults( final Iterable<? extends Nameable> results,
                                              final String... names
    )
    {
        final List<String> expected = new ArrayList<String>( Arrays.asList( names ) );

        for( Nameable entity : results )
        {
            String firstExpected = null;
            if( expected.size() > 0 )
            {
                firstExpected = expected.get( 0 );
            }
            if( firstExpected == null )
            {
                fail( entity.name().get() + " returned but not expected" );
            }
            else if( !firstExpected.equals( entity.name().get() ) )
            {
                fail( entity.name().get() + " is not in the expected order" );
            }
            expected.remove( 0 );
        }
        for( String notReturned : expected )
        {
            fail( notReturned + " was expected but not returned" );
        }
    }

    @Test
    public void givenQueryWhenExecutedReturnAll()
    {
        final QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        final Query<Person> query = qb.newQuery( Network.persons() );
        System.out.println( query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe", "Jack Doe", "Vivian Smith" );
    }

    @Test
    public void givenEqQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Domain> qb = qbf.newQueryBuilder( Domain.class );
        final Nameable nameable = templateFor( Nameable.class );
        final Query<Domain> query = qb.where(
            eq( nameable.name(), "Gaming" )
        ).newQuery( Network.domains() );
        verifyUnorderedResults( query, "Gaming" );
    }

    @Test
    public void givenMixinTypeQueryWhenExecutedReturnAll()
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        Query<Nameable> query = qb.newQuery( Network.nameables() );
        verifyUnorderedResults(
            query,
            "Joe Doe", "Ann Doe", "Jack Doe", "Vivian Smith",
            "Penang", "Kuala Lumpur",
            "Cooking", "Gaming", "Programming", "Cars"
        );
    }

    @Test
    public void givenEqQueryOnValueWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person personTemplate = templateFor( Person.class );
        City placeOfBirth = personTemplate.placeOfBirth().get();
        Query<Person> query = qb.where(
            eq( placeOfBirth.name(), "Kuala Lumpur" )
        ).newQuery( Network.persons() );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe", "Vivian Smith" );
    }

    @Test
    public void givenEqQueryOnAssociationAndPropertyWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = qb.where(
            eq( person.mother().get().placeOfBirth().get().name(), "Kuala Lumpur" )
        ).newQuery( Network.persons() );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void givenEqQueryOnAssociationWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        City kl = uow.get( City.class, StringIdentity.identityOf( "kualalumpur" ) );
        Query<Person> query = qb.where(
            eq( person.mother().get().placeOfBirth(), kl )
        ).newQuery( Network.persons() );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void givenGeQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = qb.where(
            ge( person.yearOfBirth(), 1973 )
        ).newQuery( Network.persons() );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe", "Vivian Smith" );
    }

    @Test
    public void givenAndQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        Person person = templateFor( Person.class );
        Query<Nameable> query = qb.where(
            ge( person.yearOfBirth(), 1900 ).and( eq( person.placeOfBirth().get().name(), "Penang" ) )
        ).newQuery( Network.nameables() );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void givenMultipleAndQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        Person person = templateFor( Person.class );
        Query<Nameable> query = qb.where(
            ge( person.yearOfBirth(), 1900 ).
                and( lt( person.yearOfBirth(), 2000 ) ).
                and( eq( person.placeOfBirth().get().name(), "Penang" ) )
        ).newQuery( Network.nameables() );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void givenOrQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = qb.where(
            or(
                eq( person.yearOfBirth(), 1970 ),
                eq( person.yearOfBirth(), 1975 )
            )
        ).newQuery( Network.persons() );
        verifyUnorderedResults( query, "Jack Doe", "Ann Doe" );
    }

    @Test
    public void givenMultipleOrQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = qb.where(
            or(
                eq( person.yearOfBirth(), 1970 ),
                eq( person.yearOfBirth(), 1975 ),
                eq( person.yearOfBirth(), 1990 )
            )
        ).newQuery( Network.persons() );
        verifyUnorderedResults( query, "Jack Doe", "Ann Doe", "Joe Doe" );
    }

    @Test
    public void givenOrQueryOnFemalesWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Female> qb = qbf.newQueryBuilder( Female.class );
        Person person = templateFor( Person.class );
        Query<Female> query = qb.where(
            or(
                eq( person.yearOfBirth(), 1970 ),
                eq( person.yearOfBirth(), 1975 )
            )
        ).newQuery( Network.females() );
        verifyUnorderedResults( query, "Ann Doe" );
    }

    @Test
    public void givenNotQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = qb.where(
            not(
                eq( person.yearOfBirth(), 1975 )
            )
        ).newQuery( Network.persons() );
        verifyUnorderedResults( query, "Jack Doe", "Joe Doe", "Vivian Smith" );
    }

    @Test
    public void givenIsNotNullQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = qb.where(
            isNotNull( person.email() )
        ).newQuery( Network.persons() );
        verifyUnorderedResults( query, "Joe Doe", "Vivian Smith" );
    }

    @Test
    public void givenIsNullQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = qb.where(
            isNull( person.email() )
        ).newQuery( Network.persons() );
        verifyUnorderedResults( query, "Ann Doe", "Jack Doe" );
    }

    @Test
    public void givenIsNotNullOnMixinTypeWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Male person = templateFor( Male.class );
        Query<Person> query = qb.where(
            isNotNull( person.wife() )
        ).newQuery( Network.persons() );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void givenIsNullOnMixinTypeWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Male> qb = qbf.newQueryBuilder( Male.class );
        Male person = templateFor( Male.class );
        Query<Male> query = qb.where(
            isNull( person.wife() )
        ).newQuery( Network.males() );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void givenIsNullOnAssociationWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Male person = templateFor( Male.class );
        Query<Person> query = qb.where(
            isNull( person.wife() )
        ).newQuery( Network.persons() );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe", "Vivian Smith" );
    }

    @Test
    public void givenOrderAndMaxResultsQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return only 2 entities
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = qb.newQuery( Network.nameables() );
        query.orderBy( orderBy( nameable.name() ) );
        query.maxResults( 2 );
        verifyOrderedResults(
            query,
            "Ann Doe", "Cars"
        );
    }

    @Test
    public void givenOrderAndFirstAndMaxResultsQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return only 3 entities starting with forth one
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = qb.newQuery( Network.nameables() );
        query.orderBy( orderBy( nameable.name() ) );
        query.firstResult( 3 );
        query.maxResults( 3 );
        verifyOrderedResults(
            query,
            "Gaming", "Jack Doe", "Joe Doe"
        );
    }

    @Test
    public void givenOrderByOnMixinTypeQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return all Nameable entities sorted by name
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = qb.newQuery( Network.nameables() );
        query.orderBy( orderBy( nameable.name() ) );
        verifyOrderedResults(
            query,
            "Ann Doe", "Cars", "Cooking", "Gaming", "Jack Doe", "Joe Doe", "Kuala Lumpur", "Penang", "Programming", "Vivian Smith"
        );
    }

    @Test
    public void givenGtAndOrderByQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return all Nameable entities with a name > "D" sorted by name
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = qb.where(
            gt( nameable.name(), "D" )
        ).newQuery( Network.nameables() );
        query.orderBy( orderBy( nameable.name() ) );
        verifyOrderedResults(
            query,
            "Gaming", "Jack Doe", "Joe Doe", "Kuala Lumpur", "Penang", "Programming", "Vivian Smith"
        );
    }

    @Test
    public void givenGtAndOrderByDescendingQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        // should return all Persons born after 1973 (Ann and Joe Doe) sorted descending by name
        Person person = templateFor( Person.class );
        Query<Person> query = qb.where(
            gt( person.yearOfBirth(), 1973 )
        ).newQuery( Network.persons() );
        query.orderBy( orderBy( person.name(), OrderBy.Order.DESCENDING ) );
        verifyOrderedResults( query, "Vivian Smith", "Joe Doe", "Ann Doe" );
    }

    @Test
    public void givenOrderByMultipleQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        // should return all Persons sorted by name of the city they were born, and then by year they were born
        Person person = templateFor( Person.class );
        Query<Person> query = qb.newQuery( Network.persons() );
        query.orderBy( orderBy( person.placeOfBirth().get().name() ),
                       orderBy( person.yearOfBirth() ) );
        verifyOrderedResults( query, "Ann Doe", "Joe Doe", "Vivian Smith", "Jack Doe" );
    }

    @Test
    public void givenMatchesQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        Nameable nameable = templateFor( Nameable.class );
        // should return Jack and Joe Doe
        Query<Nameable> query = qb.where(
            matches( nameable.name(), "J.*Doe" )
        ).newQuery( Network.nameables() );
        verifyUnorderedResults(
            query,
            "Jack Doe", "Joe Doe"
        );
    }

    // TODO solve ManyAssociation filtering for iterables
    // @Test
    public void givenOneOfQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Domain interests = person.interests().get( 0 );
        Query<Person> query = qb.where( eq( interests.name(), "Cars" ) ).newQuery( Network.persons() );
        verifyOrderedResults( query, "Jack Doe" );
    }

    @Test
    public void givenManyAssociationContainsQueryWhenExecutedThenReturnCorrect()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Domain value = Network.domains().iterator().next();
        Query<Person> query = qb.where( QueryExpressions.contains( person.interests(), value ) )
            .newQuery( Network.persons() );
        for( Person person1 : query )
        {
            System.out.println( person1.name() );
        }
        verifyOrderedResults( query, "Joe Doe", "Vivian Smith" );
    }

    @Test
    public void givenEntitiesWithInternalStateWhenQueriedThenReturnCorrect()
    {
        QueryBuilder<PetEntity> qb = qbf.newQueryBuilder( PetEntity.class );
        Pet.PetState pet = templateFor( Pet.PetState.class );
        Nameable petOwner = templateFor( Nameable.class, pet.owner() );
        Query<PetEntity> query = qb.where( eq( petOwner.name(), "Jack Doe" ) ).newQuery( Network.pets() );
        verifyOrderedResults( query, "Rex" );
    }

    @Test
    public void givenEntitiesWithFieldPropertyByNameWhenQueriedThenReturnCorrect()
    {
        QueryBuilder<PetEntity> qb = qbf.newQueryBuilder( PetEntity.class );
        Query<PetEntity> query = qb.where( eq( property( Describable.Mixin.class, "description" ), "Rex is a great dog" ) )
            .newQuery( Network.pets() );
        verifyOrderedResults( query, "Rex" );
    }

    @Test
    public void givenEntitiesWithFieldPropertyWhenQueriedThenReturnCorrect()
    {
        QueryBuilder<PetEntity> qb = qbf.newQueryBuilder( PetEntity.class );
        Query<PetEntity> query = qb.where( eq( templateFor( Describable.Mixin.class ).description, "Rex is a great dog" ) )
            .newQuery( Network.pets() );
        verifyOrderedResults( query, "Rex" );
    }
}