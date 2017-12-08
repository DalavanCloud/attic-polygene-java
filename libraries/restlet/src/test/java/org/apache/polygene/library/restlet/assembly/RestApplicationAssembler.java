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

package org.apache.polygene.library.restlet.assembly;

import java.util.LinkedHashMap;
import java.util.function.BinaryOperator;
import org.apache.polygene.api.activation.PassivationException;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.bootstrap.ApplicationAssembly;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.LayerAssembly;
import org.apache.polygene.bootstrap.layered.LayerAssembler;
import org.apache.polygene.bootstrap.layered.LayeredApplicationAssembler;
import org.apache.polygene.bootstrap.layered.ModuleAssembler;

public class RestApplicationAssembler extends LayeredApplicationAssembler
{
    private final LinkedHashMap<Class<? extends LayerAssembler>, LayerAssembly> assemblies = new LinkedHashMap<>();
    private Class<? extends LayerAssembler>[] layers;

    @SafeVarargs
    public RestApplicationAssembler( String name, String version, Application.Mode mode, Class<? extends LayerAssembler>... layers )
        throws AssemblyException
    {
        super( name, version, mode );
        this.layers = layers;
    }

    @Override
    protected void assembleLayers( ApplicationAssembly assembly )
    {
        LayerAssembly layerBelow = null;
        for( Class<? extends LayerAssembler> layer : layers )
        {
            LayerAssembly layerAssembly = createLayer( layer );
            assemblies.put( layer, layerAssembly );
            if( layerBelow != null )
            {
                layerAssembly.uses( layerBelow );
            }
            layerBelow = layerAssembly;
        }
    }

    public LayerAssembly layer( Class<? extends LayerAssembler> layerClass )
    {
        return assemblies.get( layerClass );
    }

    public void addShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            try
            {
                stop();
            }
            catch( PassivationException e )
            {
                e.printStackTrace();
            }
        } ) );
    }
}
