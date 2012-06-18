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

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.dthume.maven.util.LogWriter;
import org.dthume.maven.xpom.api.ArtifactResolver;
import org.dthume.maven.xpom.api.CollectionResolver;
import org.dthume.maven.xpom.api.POMTransformer;
import org.dthume.maven.xpom.api.TransformationContext;
import org.dthume.maven.xpom.impl.DefaultArtifactResolver;
import org.dthume.maven.xpom.impl.DefaultTransformationContext;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.RemoteRepositoryManager;
import org.sonatype.aether.repository.RemoteRepository;

public abstract class AbstractTransformingMojo extends AbstractSCMAwareMojo {
    /**
     * The XSL stylesheet to apply.
     *
     * @required
     * @parameter expression="${xsl}"
     */
    private File stylesheetFile;
    
    /**
     * If {@code true} then only output transformation result to console, do
     * not actually write file.
     *
     * @parameter expression="${dryRun}" default-value="false"
     */
    protected boolean dryRun = false;
    
    /**
     * The output file to write to instead of the project {@code pom.xml}.
     * Setting this will disable the backup stategy automatically.
     *
     * @parameter expression="${outputFile}"
     */
    protected File outputFile = null;
    
    /**
     * The entry point to Aether
     *
     * @component
     */
    protected RepositorySystem repoSystem;
    
    /**
     * The current repository/network configuration of Maven.
     *
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    protected RepositorySystemSession repoSession;
    
    /**
     * @component
     */
    protected RemoteRepositoryManager repoManager;
    
    /**
     * Remote repositories to use for the resolution of project dependencies.
     *
     * @parameter default-value="${project.remoteProjectRepositories}"
     * @readonly
     */
    protected List<RemoteRepository> projectRepos;
    
    /**
     * Remote repositories to use for the resolution of plugins and their
     * dependencies.
     *
     * @parameter default-value="${project.remotePluginRepositories}"
     * @readonly
     */
    protected List<RemoteRepository> pluginRepos;

    /**
     * @component
     */
    private ModelBuilder modelBuilder;
    
    /**
     * @component roleHint="xsl"
     */
    private POMTransformer transformer;
    
    protected Source getStylesheetSource() {
        return new StreamSource(stylesheetFile);
    }

    @Override
    protected void executeInternal()
            throws MojoExecutionException, MojoFailureException {
        prepareForTransformation();
        
        final TransformationContext context = prepareContext();
        
        transformer.transform(context);
    }
    
    protected void prepareForTransformation()
        throws MojoExecutionException, MojoFailureException {}
    
    private TransformationContext prepareContext() {
        final DefaultTransformationContext context =
                new DefaultTransformationContext();
        
        context.setArtifactResolver(getArtifactResolver());
        context.setCollectionResolver(getCollectionResolver());
        context.setExpressionEvaluator(getExpressionEvaluator());
        context.setModelResult(getResult());
        context.setModelSource(getSource());
        context.setSourceFileEncoding(getSourceFileEncoding());
        context.setStylesheetSource(getStylesheetSource());
        context.setTransformationAttributes(getTransformationAttributeMap());
        context.setTransformationParameters(getTransformationParameterMap());
        context.setTransformationOutputProperties(getOutputProperties());
        
        return context;
    }
    
    protected abstract Source getSource();
    
    protected final Result getResult() {
        if (dryRun)
            return new StreamResult(new LogWriter(getLog()));
        
        if (null == outputFile)
            return new StreamResult(getProjectPOMFile());

        return new StreamResult(outputFile);
    }
    
    protected final File getOutputFile() { return outputFile; }
    
    protected final ArtifactResolver getArtifactResolver() {
        return new DefaultArtifactResolver(repoSystem, repoManager, repoSession,
                projectRepos, pluginRepos, modelBuilder, getReactorProjects());
    }
    
    protected Map<String, Object> getTransformationParameterMap() {
        return java.util.Collections.emptyMap();
    }
    
    protected Map<String, Object> getTransformationAttributeMap() {
        return java.util.Collections.emptyMap();
    }
    
    protected Properties getOutputProperties() {
        return new Properties();
    }
    
    private CollectionResolver getCollectionResolver() {
        return new CollectionResolver() {
            public Iterable<Source> resolve(final String href,
                    final String base) throws TransformerException {
                if ("urn:xpom:reactor-projects".equals(href)) {
                    return new ReactorResolver();
                }
                return null;
            }
        };
    }
    
    private class ReactorResolver implements Iterable<Source> {
        public Iterator<Source> iterator() { return new ReactorIterator(); }
    }
    
    private class ReactorIterator implements Iterator<Source> {
        private final Iterator<MavenProject> iter;
        
        ReactorIterator() {
            iter = getReactorProjects().iterator();
        }
        
        public boolean hasNext() { return iter.hasNext(); }

        public Source next() {
            final MavenProject project = iter.next();
            return new StreamSource(project.getFile());
        }

        public void remove() {}
    }
}
