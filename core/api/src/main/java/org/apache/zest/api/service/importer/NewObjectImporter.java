/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.apache.zest.api.service.importer;

import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.object.ObjectFactory;
import org.apache.zest.api.service.ImportedServiceDescriptor;
import org.apache.zest.api.service.ServiceImporter;
import org.apache.zest.api.service.ServiceImporterException;
import org.apache.zest.functional.Iterables;

/**
 * Import Services using a new registered Object instance.
 */
public final class NewObjectImporter<T>
    implements ServiceImporter<T>
{
    @Structure
    private ObjectFactory obf;

    @Override
    @SuppressWarnings( "unchecked" )
    public T importService( ImportedServiceDescriptor serviceDescriptor )
        throws ServiceImporterException
    {
        return obf.newObject( (Class<T>) serviceDescriptor.types().findFirst().orElse( null ));
    }

    @Override
    public boolean isAvailable( T instance )
    {
        return true;
    }
}