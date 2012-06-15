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
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.CollectionURIResolver;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.dthume.maven.util.LogWriter;
import org.dthume.maven.xpom.api.ArtifactResolver;
import org.dthume.maven.xpom.api.CollectionResolver;
import org.dthume.maven.xpom.api.POMTransformer;
import org.dthume.maven.xpom.api.TransformationContext;
import org.dthume.maven.xpom.api.XPOMException;
import org.dthume.maven.xpom.impl.DefaultArtifactResolver;
import org.dthume.maven.xpom.impl.DefaultTransformationContext;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Node;

/**
 * Transform project POM
 * 
 * @goal project
 * 
 * @author dth
 */
public class ProjectMojo extends AbstractXPOMMojo {
    /**
     * Whether or not to use the original, or effective (after inheritance
     * and interpolation) project model as the input to the transformation.
     *
     * @parameter expression="${useEffectiveModel}" default-value="false"
     */
    private boolean useEffectiveModel = false;

    /**
     * Whether to use the model instead of the original POM file itself.
     * Ignored if {@link #useEffectiveModel} is {@code true}.
     *
     * @parameter expression="${useModel}" default-value="false"
     */
    private boolean useModel = false;
    
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
    private boolean dryRun = false;
    
    /**
     * The output file to write to instead of the project {@code pom.xml}.
     * Setting this will disable the backup stategy automatically.
     *
     * @parameter expression="${outputFile}"
     */
    private File outputFile = null;
    
    /**
     * Parameters to pass to the execution of the XSL stylesheet. 
     *
     * @parameter
     */
    private Properties transformationParameters = new Properties();

    /**
     * (CLI) Comma separated list of parameters to pass to the execution of
     * the XSL stylesheet.
     *
     * @readonly
     * @parameter expression="${xpom.params}"
     */
    private List<String> stringParams = java.util.Collections.emptyList();

    /**
     * Attributes to set on the XSL Transformer.
     *
     * @parameter
     */
    private Properties transformationAttributes = new Properties();
    
    /**
     * (CLI) Comma separated list of attributes to set on the execution of the
     * XSL stylesheet.
     *
     * @readonly
     * @parameter expression="${xpom.attributes}"
     */
    private List<String> stringAttributes = java.util.Collections.emptyList();
    
    /**
     * The entry point to Aether
     *
     * @component
     */
    private RepositorySystem repoSystem;
    
    /**
     * The current repository/network configuration of Maven.
     *
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    private RepositorySystemSession repoSession;
    
    /**
     * Remote repositories to use for the resolution of project dependencies.
     *
     * @parameter default-value="${project.remoteProjectRepositories}"
     * @readonly
     */
    private List<RemoteRepository> projectRepos;
    
    /**
     * Remote repositories to use for the resolution of plugins and their
     * dependencies.
     *
     * @parameter default-value="${project.remotePluginRepositories}"
     * @readonly
     */
    private List<RemoteRepository> pluginRepos;

    /**
     * @component roleHint="xsl"
     */
    private POMTransformer transformer;

    @Override
    protected void executeInternal()
            throws MojoExecutionException, MojoFailureException {
        if (!dryRun && isOverwriteProjectPOM())
            prepareProjectPOMForTransformation();
        else getLog().debug("Skipping backup of project POM file");
        
        final TransformationContext context = createTransformationContext();
        
        transformer.transform(context);
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
    
    private TransformationContext createTransformationContext()
        throws MojoExecutionException, MojoFailureException {
        final DefaultTransformationContext context =
                new DefaultTransformationContext();

        context.setArtifactResolver(getArtifactResolver());
        context.setSourceFileEncoding(getSourceFileEncoding());
        context.setModelSource(getModelSource());
        context.setModelResult(getModelResult());
        context.setStylesheetSource(getStylesheetSource());
        context.setExpressionEvaluator(getExpressionEvaluator());
        context.setTransformationAttributes(getTransformationAttributeMap());
        context.setTransformationParameters(getTransformationParameterMap());
        context.setCollectionResolver(getCollectionResolver());

        return context;
    }
    
    private ArtifactResolver getArtifactResolver() {
        return new DefaultArtifactResolver(repoSystem, repoSession,
                projectRepos, pluginRepos);
    }
    
    private Map<String, Object> getTransformationParameterMap() {
        return propertiesToMap("parameter", transformationParameters,
                stringParams);
    }
    
    private Map<String, Object> getTransformationAttributeMap() {
        return propertiesToMap("attribute", transformationAttributes,
                stringAttributes);
    }
    
    private Map<String, Object> propertiesToMap(final String type,
            final Properties props,
            final List<String> strings) {
        final Map<String, Object> params = new HashMap<String, Object>();
        
        for (final Map.Entry<Object, Object> pair : props.entrySet())
            params.put(pair.getKey().toString(), pair.getValue());
        
        for (final String kvp : strings)
            addStringParameter(type, params, kvp);
        
        return params;
    }
    
    private final static String PARAM_DELIM = "="; 
    
    private void addStringParameter(final String type,
            final Map<String, Object> params,
            final String kvp) {
        final int lastEq = kvp.lastIndexOf(PARAM_DELIM);
            
        if (-1 == lastEq) {
            final String message = String.format("Invalid %s: %s", type, kvp);
            throw new IllegalArgumentException(message);
        }
            
        final String key = kvp.substring(0, lastEq);
        final String value =
                kvp.endsWith(PARAM_DELIM) ? "" : kvp.substring(lastEq + 1);

        params.put(key, value);
    }

    private Source getModelSource() {
       return isUseModel() ? modelToSource(getModel()) : getProjectPOMSource();
    }
    
    private boolean isUseModel() { return useModel || useEffectiveModel; }
    
    private Result getModelResult() {
        Result result = null;
        
        if (dryRun) {
            result = new StreamResult(new LogWriter(getLog()));
        } else if (null == outputFile) {
            result = new StreamResult(getProjectPOMFile());
        } else {
            result = new StreamResult(outputFile);
        }
        
        return result;
    }

    private Source getStylesheetSource() {
        return new StreamSource(stylesheetFile);
    }
    
    private Source getProjectPOMSource() {
        try {        
            return new StreamSource(new XmlStreamReader(getProjectPOMFile()));
        } catch (final IOException e) {
            throw new XPOMException(e);
        }
    }
    
    private Source modelToSource(final Model model) {
        final StringWriter writer = new StringWriter();
        try {
            new MavenXpp3Writer().write(writer, model);
        } catch (final IOException e) {
            getLog().error("error writing model");
        }
        return new StringSource(writer.toString());
    }

    private Model getModel() {
        final Log log = getLog();

        Model model = null;
        if (useEffectiveModel) {
            log.debug("Using effective project model");
            model = getProject().getModel();
        } else {
            log.debug("Using original project Model");
            model = getProject().getOriginalModel();
        }

        if (log.isDebugEnabled()) log.debug("Model=" + model);

        return model;
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
        public Iterator<Source> iterator() {
            return new ReactorIterator();
        }
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
