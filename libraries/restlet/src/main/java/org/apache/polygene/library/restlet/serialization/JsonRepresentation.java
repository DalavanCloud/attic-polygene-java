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

package org.apache.polygene.library.restlet.serialization;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.serialization.Serialization;
import org.apache.polygene.api.serialization.Serializer;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.spi.PolygeneSPI;
import org.restlet.data.MediaType;
import org.restlet.engine.application.StatusInfo;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;

/**
 * Representation based of Polygene ValueComposites. It can serialize and deserialize
 * automatically in JSON only.<br>
 * <br>
 */
public class JsonRepresentation<T> extends OutputRepresentation
{

    @Structure
    private PolygeneSPI spi;

    @Service
    private Serialization stateSerialization;

    @Structure
    private ModuleDescriptor module;

    /**
     * The (parsed) object to format.
     */
    @Optional
    @Uses
    private volatile T object;

    /**
     * The object class to instantiate.
     */
    @Optional
    @Uses
    private volatile Class<T> objectClass;

    /**
     * The representation to parse.
     */
    @Optional
    @Uses
    private volatile Representation representation;

    public JsonRepresentation()
    {
        super( MediaType.APPLICATION_JSON );
    }

    /**
     * Returns the wrapped object, deserializing the representation with Polygene
     * if necessary.
     *
     * @return The wrapped object.
     *
     * @throws IOException if there is an underlying I/O problem.
     */
    public T getObject()
        throws IOException
    {
        T result = null;

        if( this.object != null )
        {
            result = this.object;
        }
        else if( this.representation != null )
        {
            result = stateSerialization.deserialize( module, objectClass,
                                                     new InputStreamReader( this.representation.getStream() ) );
        }
        return result;
    }

    /**
     * Returns the object class to instantiate.
     *
     * @return The object class to instantiate.
     */
    public Class<T> getObjectClass()
    {
        return objectClass;
    }

    @Override
    public void write( OutputStream outputStream )
        throws IOException
    {
        if( representation != null )
        {
            representation.write( outputStream );
        }
        else if( object instanceof StatusInfo )
        {
            outputStream.write(((StatusInfo) object).getReasonPhrase().getBytes());
        }
        else if( object != null )
        {
            stateSerialization.serialize( Serializer.Options.NO_TYPE_INFO, outputStream, object );
            outputStream.write( '\n' );
        }
    }
}
