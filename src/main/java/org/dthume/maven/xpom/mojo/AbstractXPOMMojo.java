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
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

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
import org.dthume.maven.xpom.api.ExpressionEvaluator;
import org.dthume.maven.xpom.api.XPOMException;
import org.dthume.maven.xpom.impl.DefaultExpressionEvaluator;
import org.dthume.maven.xpom.impl.saxon.TraxHelper;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

/**
 * @author dth
 */
public abstract class AbstractXPOMMojo extends AbstractMojo {
    /**
     * Base directory of the project.
     *
     * @required
     * @readonly
     * @parameter default-value="${basedir}"
     */
    private File basedir;
    
    /**
     * The charset to use when reading and writing source files; defaults
     * to platform encoding.
     * 
     * @readonly
     * @parameter expression="${project.build.sourceEncoding}"
     */
    private String sourceEncoding = Charset.defaultCharset().name();
    
    /**
     * Execute the mojo once for a single reactor build if {@code true},
     * otherwise once per project (the default).
     *
     * @parameter expression="${aggregate}" default-value="false"
     */
    private boolean aggregate = false;
    
    /**
     * @readonly
     * @parameter expression="${reactorProjects}"
     */
    private List<MavenProject> reactorProjects = Collections.emptyList();
    
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
    
    /**
     * @component
     */
    private TraxHelper trax;
    
    protected final File getBaseDir() { return basedir; }
    
    protected final boolean isRunningInExecutionRoot() {
        final String rootDir = session.getExecutionRootDirectory();
        return rootDir.equalsIgnoreCase(basedir.toString());
    }
    
    protected final TraxHelper getTrax() { return trax; }
    
    protected final String getSourceFileEncoding() { return sourceEncoding; }
    
    protected final MavenProject getProject() { return project; }
    
    protected final MavenSession getSession() { return session; }
    
    protected final BuildPluginManager getPluginManager() {
        return pluginManager;
    }
    
    protected final List<MavenProject> getReactorProjects() {
        return Collections.unmodifiableList(reactorProjects);
    }
    
    protected final File getProjectPOMFile() { return project.getFile(); }
    
    protected final ExpressionEvaluator getExpressionEvaluator() {
        final MojoExecution execution = new MojoExecution(new MojoDescriptor());
        final PluginParameterExpressionEvaluator evaluator =
                new PluginParameterExpressionEvaluator(session, execution);
        return new DefaultExpressionEvaluator(evaluator);
    }
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (aggregate && !isRunningInExecutionRoot()) {
            getLog().info("Skipping execution due to request to only run once");
            return;
        }
        
        try {
            executeInternal();
        } catch (final XPOMException e) {
            throw new MojoExecutionException("Caught XPOM Exception", e);
        }
    }
    
    protected abstract void executeInternal()
            throws MojoExecutionException, MojoFailureException;
}
