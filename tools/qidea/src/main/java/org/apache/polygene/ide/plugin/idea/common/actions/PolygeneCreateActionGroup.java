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

package org.apache.polygene.ide.plugin.idea.common.actions;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;

import static org.apache.polygene.ide.plugin.idea.common.resource.PolygeneResourceBundle.message;

/**
 * @since 0.1
 */
public final class PolygeneCreateActionGroup extends DefaultActionGroup
{
    public PolygeneCreateActionGroup()
    {
        super( message( "polygene.action.group.title" ), true );
        getTemplatePresentation().setDescription( message( "polygene.action.group.description" ) );
    }

    public void update( AnActionEvent e )
    {
        Presentation presentation = e.getPresentation();
        presentation.setVisible( shouldActionGroupVisible( e ) );
    }

    private boolean shouldActionGroupVisible( AnActionEvent e )
    {
        Module module = e.getData( LangDataKeys.MODULE );
        if( module == null )
        {
            return false;
        }

        // TODO: Enable this once PolygeneFacet can be automatically added/removed
//        if( PolygeneFacet.getInstance( module ) == null )
//        {
//            return false;
//        }

        // Are we on IDE View and under project source folder?
        Project project = e.getData( PlatformDataKeys.PROJECT );
        IdeView view = e.getData( LangDataKeys.IDE_VIEW );
        if( view != null && project != null )
        {
            ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance( project ).getFileIndex();
            PsiDirectory[] dirs = view.getDirectories();
            for( PsiDirectory dir : dirs )
            {
                if( projectFileIndex.isInSourceContent( dir.getVirtualFile() ) && JavaDirectoryService.getInstance().getPackage( dir ) != null )
                {
                    return true;
                }
            }
        }

        return false;
    }
}
