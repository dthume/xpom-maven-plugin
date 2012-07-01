/*
 * #%L
 * XPOM Maven Plugin
 * %%
 * Copyright (C) 2012 David Thomas Hume
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.dthume.maven.xpom.mojo;

import static org.dthume.maven.xpom.impl.XPOMUtil.modelToSource;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.dthume.maven.xpom.api.XPOMException;

/**
 * Transform the current project POM.
 * 
 * @requiresProject true
 * @goal project
 * 
 * @author dth
 */
public class ProjectMojo extends AbstractPOMTransformingMojo {
    @Override
    protected void prepareForTransformation()
            throws MojoExecutionException, MojoFailureException {
        if (!dryRun && isOverwriteProjectPOM())
            prepareProjectPOMForTransformation();
        else getLog().debug("Skipping backup of project POM file");
        
        super.prepareForTransformation();
    }

    private boolean isOverwriteProjectPOM() throws MojoExecutionException {
        if (null == outputFile) return true;
        
        final File projectPOM =
                getCanonicalFile(getProjectPOMFile());
        final File outputPOM = getCanonicalFile(outputFile);
        
        return projectPOM.equals(outputPOM);
    }
    
    private File getCanonicalFile(final File f) throws MojoExecutionException {
        try {
            return f.getCanonicalFile();
        } catch (final IOException e) {
            throw new MojoExecutionException("Caught IOException", e);
        }
    }    

    protected Source getSource() {
        try {
            return isUseModel() ? modelToSource(getModel())
                    : getProjectPOMSource();
        } catch (final IOException e) {
            throw new XPOMException(e);
        }
    }
    
    private Source getProjectPOMSource() throws IOException{
        return new StreamSource(new XmlStreamReader(getProjectPOMFile()));
    }

    private Model getModel() {
        final Log log = getLog();

        Model model = null;
        if (isUseEffectiveModel()) {
            log.debug("Using effective project model");
            model = getProject().getModel();
        } else {
            log.debug("Using original project Model");
            model = getProject().getOriginalModel();
        }

        if (log.isDebugEnabled()) log.debug("Model=" + model);

        return model;
    }    
}
