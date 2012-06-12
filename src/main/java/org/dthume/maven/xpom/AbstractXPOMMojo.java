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
package org.dthume.maven.xpom;

import static org.codehaus.plexus.util.IOUtil.copy;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.XmlStreamWriter;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

/**
 * @author dth
 */
public abstract class AbstractXPOMMojo extends AbstractMojo {
    /**
     * The charset to use when reading and writing source files; defaults
     * to platform encoding.
     * 
     * @parameter expression="${project.build.sourceEncoding}"
     */
    private String sourceEncoding = Charset.defaultCharset().name();

    /**
     * @parameter
     *  expression="${scmPluginVersion}"
     *  default-value="1.1"
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
     * @parameter expression="${editPOMInSCM}" default-value="false"
     */
    private boolean editPOMInSCM = false;
    
    /**
     * @required
     * @readonly
     * @parameter expression="${project}"
     */
    private MavenProject project;
    
    /**
     * @required
     * @readonly
     * @parameter expression="${session}"
     */
    private MavenSession session;
        
    /**
     * @component
     * @required
     */
    private BuildPluginManager pluginManager;
    
    protected final String getSourceFileEncoding() { return sourceEncoding; }
    
    protected final MavenProject getProject() { return project; }
    
    protected final File getProjectPOMFile() { return project.getFile(); }
    
    protected final File getBackupPOMFile() { return backupPomFile; }
    
    protected final boolean isLocalSCM() { return !disableBackups; }
    
    protected final ExpressionEvaluator getExpressionEvaluator() {
        final MojoExecution execution = new MojoExecution(new MojoDescriptor());
        final PluginParameterExpressionEvaluator evaluator =
                new PluginParameterExpressionEvaluator(session, execution);
        return new DefaultExpressionEvaluator(evaluator);
    }
    
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
            throw new RuntimeException(e); // FIXME
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
                executionEnvironment(project, session, pluginManager)
        );
    }
    
    protected final Element el(String name, String value) {
        return element(name, value);
    }
    
    protected final Element el(String name, Element...els) {
        return element(name, els);
    }
}
