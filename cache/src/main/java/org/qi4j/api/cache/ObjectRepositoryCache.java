/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.api.cache;

import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.api.persistence.composite.PersistenceComposite;

/**
 * Proxy caches should implement this.
 */
@ModifiedBy( ObjectRepositoryCacheModifier.class )
@ImplementedBy( ObjectRepositoryCacheImpl.class )
public interface ObjectRepositoryCache
{
    <T extends PersistenceComposite> T getObject( String anIdentity );

    <T extends PersistenceComposite> void addObject( String anIdentity, T anObject );

    void removeObject( String anIdentity );

    void removeAll();
}
