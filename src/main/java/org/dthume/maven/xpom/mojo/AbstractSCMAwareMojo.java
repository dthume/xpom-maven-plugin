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

import static org.codehaus.plexus.util.IOUtil.copy;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.XmlStreamWriter;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

/**
 * @author dth
 */
public abstract class AbstractSCMAwareMojo extends AbstractXPOMMojo {
    /**
     * @parameter default-value="1.1"
     */
    private String scmPluginVersion = "1.1";
    
    /**
     * @required
     * @parameter
     *  expression="${backupPomFile}"
     *  default-value="${project.basedir}/pom.xml.xpom-backup"
     */
    private File backupPomFile;
    
    /**
     * Whether or not to disable the "poor mans SCM" facilities provided by this
     * plugin.
     * 
     * @parameter expression="${noBackups}" default-value="false"
     */
    private boolean disableBackups = false;

    /**
     * Whether or not to perform an {@code scm:edit} on the project
     * {@code pom.xml} before transforming it.
     * 
     * @parameter expression="${useSCM}" default-value="false"
     */
    private boolean editPOMInSCM = false;
    
    protected final File getBackupPOMFile() { return backupPomFile; }
    
    protected final boolean isLocalSCM() { return !disableBackups; }
    
    protected final void prepareProjectPOMForTransformation()
            throws MojoExecutionException, MojoFailureException {
        if (editPOMInSCM)
            executeSCMMojo("edit", element("includes", "pom.xml"));
            
        if (isLocalSCM())
            prepareBackupPOMFile();
    }

    private void prepareBackupPOMFile()
            throws MojoExecutionException, MojoFailureException{
        if (getBackupPOMFile().exists()) {
            throw new MojoFailureException("Backup file already exists: " +
                    getBackupPOMFile());
        }
        
        try {
            copy(new XmlStreamReader(getProjectPOMFile()),
                 new XmlStreamWriter(getBackupPOMFile()));
        } catch (final IOException e) {
            throw new MojoFailureException("Failed to backup POM file");
        }
    }
    
    protected final void executeSCMMojo(String goal, Element...config)
            throws MojoExecutionException, MojoFailureException {
        executeMojo(
                plugin(
                        "org.apache.maven.plugins",
                        "maven-scm-plugin",
                        scmPluginVersion
                ),
                goal,
                configuration(config),
                executionEnvironment(
                        getProject(),
                        getSession(),
                        getPluginManager()
                )
        );
    }
    
    protected final Element el(String name, String value) {
        return element(name, value);
    }
    
    protected final Element el(String name, Element...els) {
        return element(name, els);
    }
}
