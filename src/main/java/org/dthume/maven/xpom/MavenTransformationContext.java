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

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.springframework.xml.transform.StringSource;

public final class MavenTransformationContext implements TransformationContext {

    /**
     * The charset to use when reading and writing source files; defaults
     * to platform encoding.
     */
    private String sourceEncoding = Charset.defaultCharset().name();
    
    /**
     * The entry point to Aether
     */
    private RepositorySystem repoSystem;
    
    /**
     * The current repository/network configuration of Maven.
     */
    private RepositorySystemSession repoSession;
    
    /**
     * The project's remote repositories to use for the resolution of project
     * dependencies.
     */
    private List<RemoteRepository> projectRepos;
    
    /**
     * The project's remote repositories to use for the resolution of
     * plugins and their dependencies.
     */
    private List<RemoteRepository> pluginRepos;
    
    private Source stylesheetSource;
    
    private Source modelSource;
    
    private Result modelResult;
    
    private ExpressionEvaluator expressionEvaluator;
    
    private Map<String, Object> transformationParameters;
    
    private Map<String, Object> transformationAttributes;
    
    public String getSourceFileEncoding() { return sourceEncoding; }
    
    public void setSourceFileEncoding(final String encoding) {
        sourceEncoding = encoding;
    }

    public Source getModelSource() { return modelSource; }

    public void setModelSource(final Source source) {
        modelSource = source;
    }
    
    public Result getModelResult() { return modelResult; }

    public void setModelResult(final Result result) {
        modelResult = result;
    }
    
    public Source getStylesheetSource() { return stylesheetSource; }

    public void setStylesheetSource(final Source source) {
        stylesheetSource = source;
    }

    public ExpressionEvaluator getExpressionEvaluator() {
        return expressionEvaluator;
    }

    public void setExpressionEvaluator(final ExpressionEvaluator eval) {
        this.expressionEvaluator = eval;
    }

    public Map<String, Object> getTransformationParameters() {
        return transformationParameters;
    }

    public void setTransformationParameters(final Map<String, Object> params) {
        this.transformationParameters = params;
    }

    public Map<String, Object> getTransformationAttributes() {
        return transformationAttributes;
    }

    public void setTransformationAttributes(final Map<String, Object> attrs) {
        this.transformationAttributes = attrs;
    }
    
    public Source resolveArtifactPOM(final String gav) {
        final ArtifactResult result = resolveArtifact(gav);
        return new StreamSource(result.getArtifact().getFile());
    }
    
    public Source resolveArtifactFile(final String gav, final String file) {
        final ArtifactResult result = resolveArtifact(gav);
        throw new UnsupportedOperationException("TODO - implement");
    }
    
    @SuppressWarnings("unchecked")
    private ArtifactResult resolveArtifact(final String artifactId) {
        final ArtifactRequest req = new ArtifactRequest();
        
        final List<List<RemoteRepository>> repoSets =
                asList(projectRepos, pluginRepos);
        for (final List<RemoteRepository> repos : repoSets)
            for (final RemoteRepository repo : repos)
                req.addRepository(repo);

        final Artifact artifact = new DefaultArtifact(artifactId);
        req.setArtifact(artifact);
        
        try {
            return repoSystem.resolveArtifact(repoSession, req);
        } catch (final ArtifactResolutionException e) {
            throw new RuntimeException("TODO: fixme"); // FIXME
        }
    }
}
