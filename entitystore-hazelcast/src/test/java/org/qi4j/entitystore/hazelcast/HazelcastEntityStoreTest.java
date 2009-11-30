package org.qi4j.entitystore.hazelcast;

import org.junit.After;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

/**
 * @author Paul Merlin <paul@nosphere.org>
 */
public class HazelcastEntityStoreTest
        extends AbstractEntityStoreTest
{

    @Override
    public void assemble(ModuleAssembly module)
            throws AssemblyException
    {
        super.assemble(module);
        module.addServices(HazelcastEntityStoreService.class, UuidIdentityGeneratorService.class);
        ModuleAssembly config = module.layerAssembly().moduleAssembly("config");
        config.addEntities(HazelcastConfiguration.class).visibleIn(Visibility.layer);
        config.addServices(MemoryEntityStoreService.class);
    }

    @Test
    @Override
    public void givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification()
            throws UnitOfWorkCompletionException
    {
        super.givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification();
    }

    @After
    @Override
    public void tearDown()
            throws Exception
    {
        super.tearDown();
        // TODO : delete test data
    }

}
