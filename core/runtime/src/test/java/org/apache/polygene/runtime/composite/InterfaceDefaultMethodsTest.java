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
 */
package org.apache.polygene.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.apache.polygene.api.common.AppliesTo;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.composite.DefaultMethodsFilter;
import org.apache.polygene.api.concern.ConcernOf;
import org.apache.polygene.api.concern.Concerns;
import org.apache.polygene.api.constraint.ConstraintViolationException;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.sideeffect.SideEffectOf;
import org.apache.polygene.api.sideeffect.SideEffects;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.constraints.annotation.NotEmpty;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.apache.polygene.test.util.Assume.assumeJavaVersion;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

/**
 * Assert that interface default methods are mixed in composites.
 */
public class InterfaceDefaultMethodsTest extends AbstractPolygeneTest
{
    @BeforeAll
    public static void assumeJavaVersionIs8()
    {
        assumeJavaVersion( 8 );
    }

    public interface DefaultMethods
    {
        @UseDefaults( "Hello" )
        Property<String> greeting();

        default String sayHello( String name )
        {
            return greeting().get() + ", " + name + '!';
        }
    }

    public interface OverrideDefaultMethods extends DefaultMethods
    {
        @Override
        default String sayHello( String name )
        {
            return greeting().get() + ", overridden in " + name + '!';
        }
    }

    public static abstract class MixinDefaultMethods implements DefaultMethods
    {
        @Override
        public String sayHello( String name )
        {
            return greeting().get() + ", mixed in " + name + '!';
        }
    }

    public interface DefaultMethodsConstraints extends DefaultMethods
    {
        @Override
        default String sayHello( @NotEmpty String name )
        {
            return greeting().get() + ", " + name + '!';
        }
    }

    @Concerns( DefaultMethodsConcern.class )
    public interface DefaultMethodsConcerns extends DefaultMethods
    {
        @Override
        default String sayHello( String name )
        {
            return greeting().get() + ", " + name + '!';
        }

        default String sayGoodBye( String name )
        {
            return "Good Bye, " + name + '!';
        }
    }

    public static abstract class DefaultMethodsConcern extends ConcernOf<DefaultMethodsConcerns>
        implements DefaultMethodsConcerns
    {
        @Override
        public String sayHello( String name )
        {
            return next.sayHello( "concerned " + name );
        }
    }

    @Concerns( DefaultMethodsGenericConcern.class )
    public interface DefaultMethodsGenericConcerns extends DefaultMethods
    {
        @Override
        default String sayHello( String name )
        {
            return greeting().get() + ", " + name + '!';
        }

        default String sayGoodBye( String name )
        {
            return "Good Bye, " + name + '!';
        }
    }

    @AppliesTo( DefaultMethodsFilter.class )
    public static class DefaultMethodsGenericConcern extends ConcernOf<InvocationHandler>
        implements InvocationHandler
    {
        static int count = 0;

        @Override
        public Object invoke( Object o, Method method, Object[] objects )
            throws Throwable
        {
            count++;
            return next.invoke( o, method, objects );
        }
    }

    @SideEffects( DefaultMethodsSideEffect.class )
    public interface DefaultMethodsSideEffects extends DefaultMethods
    {
        @Override
        default String sayHello( String name )
        {
            return greeting().get() + ", " + name + '!';
        }
    }

    public static abstract class DefaultMethodsSideEffect extends SideEffectOf<DefaultMethodsSideEffects>
        implements DefaultMethodsSideEffects
    {
        static int count;

        @Override
        public String sayHello( String name )
        {
            count++;
            return null;
        }
    }

    @SideEffects( DefaultMethodsGenericSideEffect.class )
    public interface DefaultMethodsGenericSideEffects extends DefaultMethods
    {
        @Override
        default String sayHello( String name )
        {
            return greeting().get() + ", " + name + '!';
        }
    }

    @AppliesTo( DefaultMethodsFilter.class )
    public static class DefaultMethodsGenericSideEffect extends SideEffectOf<InvocationHandler>
        implements InvocationHandler
    {
        static int count = 0;

        @Override
        public Object invoke( Object o, Method method, Object[] objects )
            throws Throwable
        {
            count++;
            return null;
        }
    }

    @Override
    public void assemble( final ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( DefaultMethods.class,
                           OverrideDefaultMethods.class,
                           MixinDefaultMethods.class,
                           DefaultMethodsConstraints.class,
                           DefaultMethodsConcerns.class,
                           DefaultMethodsSideEffects.class,
                           DefaultMethodsGenericConcerns.class,
                           DefaultMethodsGenericSideEffects.class
                         );
    }

    @Test
    public void defaultMethods()
    {
        DefaultMethods composite = transientBuilderFactory.newTransient( DefaultMethods.class );
        assertThat( composite.sayHello( "John" ), equalTo( "Hello, John!" ) );
    }

    @Test
    public void overrideDefaultMethods()
    {
        OverrideDefaultMethods composite = transientBuilderFactory.newTransient( OverrideDefaultMethods.class );
        assertThat( composite.sayHello( "John" ), equalTo( "Hello, overridden in John!" ) );
    }

    @Test
    public void mixinDefaultMethods()
    {
        MixinDefaultMethods composite = transientBuilderFactory.newTransient( MixinDefaultMethods.class );
        assertThat( composite.sayHello( "John" ), equalTo( "Hello, mixed in John!" ) );
    }

    @Test
    public void defaultMethodsConstraints()
    {
        DefaultMethodsConstraints composite = transientBuilderFactory.newTransient( DefaultMethodsConstraints.class );
        try
        {
            composite.sayHello( "" );
        }
        catch( ConstraintViolationException ex )
        {
            assertThat( ex.getMessage(), containsString( "sayHello" ) );
            assertThat( ex.getMessage(), containsString( "DefaultMethodsConstraints" ) );
            assertThat( ex.getMessage(), containsString( "NotEmpty" ) );
        }
    }

    @Test
    public void defaultMethodsConcerns()
    {
        DefaultMethodsConcerns composite = transientBuilderFactory.newTransient( DefaultMethodsConcerns.class );
        assertThat( composite.sayHello( "John" ), equalTo( "Hello, concerned John!" ) );
        assertThat( composite.sayGoodBye( "John" ), equalTo( "Good Bye, John!" ) );
    }

    @Test
    public void defaultMethodsGenericConcerns()
    {
        DefaultMethodsGenericConcerns composite = transientBuilderFactory.newTransient( DefaultMethodsGenericConcerns.class );
        assertThat( composite.sayHello( "John" ), equalTo( "Hello, John!" ) );
        assertThat( composite.sayGoodBye( "John" ), equalTo( "Good Bye, John!" ) );
        assertThat( DefaultMethodsGenericConcern.count, equalTo( 2 ) );
    }

    @Test
    public void defaultMethodsSideEffects()
    {
        DefaultMethodsSideEffects composite = transientBuilderFactory.newTransient( DefaultMethodsSideEffects.class );
        assertThat( composite.sayHello( "John" ), equalTo( "Hello, John!" ) );
        assertThat( composite.sayHello( "John" ), equalTo( "Hello, John!" ) );
        assertThat( composite.sayHello( "John" ), equalTo( "Hello, John!" ) );
        assertThat( DefaultMethodsSideEffect.count, equalTo( 3 ) );
    }

    @Test
    public void defaultMethodsGenericSideEffects()
    {
        DefaultMethodsGenericSideEffects composite = transientBuilderFactory.newTransient( DefaultMethodsGenericSideEffects.class );
        assertThat( composite.sayHello( "John" ), equalTo( "Hello, John!" ) );
        assertThat( composite.sayHello( "John" ), equalTo( "Hello, John!" ) );
        assertThat( composite.sayHello( "John" ), equalTo( "Hello, John!" ) );
        assertThat( DefaultMethodsGenericSideEffect.count, equalTo( 3 ) );
    }
}
