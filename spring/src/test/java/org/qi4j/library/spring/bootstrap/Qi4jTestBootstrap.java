/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.library.spring.bootstrap;

import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;

public final class Qi4jTestBootstrap extends Qi4jApplicationBootstrap
{
    private static final String LAYER = "layer";
    private static final String MODULE = "module";

    static final String COMMENT_SERVICE_ID = "commentService";

    public final void assemble( ApplicationAssembly applicationAssembly )
        throws AssemblyException
    {
        LayerAssembly layerAssembly = applicationAssembly.layerAssembly( LAYER );
        ModuleAssembly moduleAssembly = layerAssembly.moduleAssembly( MODULE );
        moduleAssembly.addServices( CommentServiceComposite.class )
            .identifiedBy( COMMENT_SERVICE_ID );
    }
}
