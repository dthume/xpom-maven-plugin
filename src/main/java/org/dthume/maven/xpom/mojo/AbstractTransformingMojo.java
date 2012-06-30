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

import static org.dthume.maven.xpom.trax.ChainingURIResolver.chainResolvers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.io.SettingsWriter;
import org.dthume.maven.util.LogWriter;
import org.dthume.maven.xpom.api.ArtifactResolver;
import org.dthume.maven.xpom.api.CollectionResolver;
import org.dthume.maven.xpom.api.POMTransformer;
import org.dthume.maven.xpom.api.TransformationContext;
import org.dthume.maven.xpom.api.XPOMException;
import org.dthume.maven.xpom.impl.DefaultArtifactResolver;
import org.dthume.maven.xpom.impl.DefaultTransformationContext;
import org.dthume.maven.xpom.impl.XPOMUtil;
import org.dthume.maven.xpom.impl.saxon.SettingsURIResolver;
import org.dthume.maven.xpom.trax.ClasspathURIResolver;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.RemoteRepositoryManager;
import org.sonatype.aether.repository.RemoteRepository;

public abstract class AbstractTransformingMojo extends AbstractSCMAwareMojo {
    /**
     * The XSL stylesheet file to apply.
     *
     * @parameter expression="${xsl}"
     */
    private File stylesheetFile = null;
    
    /**
     * The URI of an XSL stylesheet to apply.
     * 
     * @parameter expression="${xslUri}"
     */
    private URI stylesheetURI = null;
    
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
    
    /**
     * @component
     */
    private SettingsWriter settingsWriter;
    
    private URIResolver uriResolver = null;
    
    protected final Source getStylesheetSource() {
        return null != stylesheetFile ?
                new StreamSource(stylesheetFile) : getStylesheetSourceFromURI();
    }
    
    private Source getStylesheetSourceFromURI()
    {
        try {
            final String href = stylesheetURI.toString();
            final String base = getBaseDir().toURI().toString(); 
            final String uri = XPOMUtil.resolveURI(href, base);
            return new StreamSource(uri);
        } catch (final TransformerException e) {
            throw new XPOMException(e);
        }
    }

    @Override
    protected void executeInternal()
            throws MojoExecutionException, MojoFailureException {
        prepareForTransformationInternal();
        final TransformationContext context = prepareContext();
        transformer.transform(context);
    }
    
    private void prepareForTransformationInternal()
            throws MojoExecutionException, MojoFailureException {
        uriResolver = chainResolvers(
                new SettingsURIResolver(getSettings(), settingsWriter),
                new ClasspathURIResolver(getClass()));
        
        prepareForTransformation();
    }
    
    protected void prepareForTransformation()
        throws MojoExecutionException, MojoFailureException {}
    
    private TransformationContext prepareContext() {
        final DefaultTransformationContext context =
                new DefaultTransformationContext();
        
        context.setArtifactResolver(getArtifactResolver());
        context.setCollectionResolver(getCollectionResolver());
        context.setUriResolver(getURIResolver());
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
    
    private final static String REACTOR_PROJECTS =
            "urn:xpom:reactor-projects";
    
    private final static String EFFECTIVE_REACTOR_PROJECTS =
            REACTOR_PROJECTS + "?effective=true";
    
    private CollectionResolver getCollectionResolver() {
        return new BuiltinCollectionResolver();
    }
    
    private URIResolver getURIResolver() { return uriResolver; }
    
    private final class BuiltinCollectionResolver
        implements CollectionResolver {
        
        public Iterable<Source> resolve(final String href,
                final String base) throws TransformerException {
            if (REACTOR_PROJECTS.equals(href)) {
                return new Iterable<Source>() {
                    public Iterator<Source> iterator() {
                        return new ReactorIterator();
                    }
                };
            } else if (EFFECTIVE_REACTOR_PROJECTS.equals(href)) {
                return new Iterable<Source>() {
                    public Iterator<Source> iterator() {
                        return new EffectiveReactorIterator();
                    }
                };
            }
            return null;
        }
    };
    
    private abstract class AbstractReactorIterator implements Iterator<Source> {
        private final Iterator<MavenProject> iter;
        
        AbstractReactorIterator() {
            iter = getReactorProjects().iterator();
        }
        
        public boolean hasNext() { return iter.hasNext(); }
        public Source next() { return toSource(iter.next()); }
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        private Source toSource(final MavenProject project) {
            try {
                return toSourceInternal(project);
            } catch (final IOException e) {
                throw new XPOMException(e);
            }
        }
        
        protected abstract Source toSourceInternal(MavenProject project)
            throws IOException;
    }
    
    private class ReactorIterator extends AbstractReactorIterator {
        protected final Source toSourceInternal(final MavenProject project) {
            return new StreamSource(project.getFile());
        }
    }
    
    private class EffectiveReactorIterator extends AbstractReactorIterator {
        protected final Source toSourceInternal(final MavenProject project)
            throws IOException {
            return XPOMUtil.modelToSource(project.getModel());
        }
    }
}
