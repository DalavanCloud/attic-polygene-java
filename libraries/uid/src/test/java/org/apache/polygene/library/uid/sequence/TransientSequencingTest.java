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
package org.apache.polygene.library.uid.sequence;

import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.uid.sequence.assembly.TransientSequencingAssembler;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class TransientSequencingTest extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new TransientSequencingAssembler().assemble( module );
        module.transients( UnderTestComposite.class );
    }

    @Test
    public void whenTransientSequencingThenNumbersStartAtZero()
        throws Exception
    {
        UnderTest underTest = transientBuilderFactory.newTransient( UnderTest.class );
        assertThat( underTest.currentValue(), equalTo( 0 ) );
    }

    @Test
    public void whenTransientSequencingThenFirstNextValueIsOne()
        throws Exception
    {
        UnderTest underTest = transientBuilderFactory.newTransient( UnderTest.class );
        assertThat( underTest.nextValue(), equalTo( 1 ) );
        assertThat( underTest.currentValue(), equalTo( 1 ) );
    }

    @Test
    public void whenTransientSequencingThenFirst100ValuesAreInSequence()
        throws Exception
    {
        UnderTest underTest = transientBuilderFactory.newTransient( UnderTest.class );
        for( int i = 1; i <= 100; i++ )
        {
            assertThat( underTest.nextValue(), equalTo( i ) );
            assertThat( underTest.currentValue(), equalTo( i ) );
            assertThat( underTest.currentValue(), equalTo( i ) );
            assertThat( underTest.currentValue(), equalTo( i ) );
        }
    }

    public interface UnderTest
    {
        long nextValue();

        long currentValue();
    }

    @Mixins( UnderTestMixin.class )
    public interface UnderTestComposite extends UnderTest, TransientComposite
    {
    }

    public static class UnderTestMixin
        implements UnderTest
    {
        @Service
        private Sequencing service;

        public long nextValue()
        {
            return service.newSequenceValue();
        }

        public long currentValue()
        {
            return service.currentSequenceValue();
        }
    }
}
