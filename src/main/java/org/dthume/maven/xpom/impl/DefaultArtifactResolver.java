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
package org.dthume.maven.xpom.impl;

import java.io.IOException;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.project.MavenProject;
import org.dthume.maven.xpom.api.ArtifactResolver;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.RemoteRepositoryManager;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

public class DefaultArtifactResolver implements ArtifactResolver {

    private Object lock = new Object();
    
    /**
     * The entry point to Aether
     */
    private final RepositorySystem repoSystem;
    
    /**
     * The current repository/network configuration of Maven.
     */
    private final RepositorySystemSession repoSession;
    
    /**
     * The project's remote repositories to use for the resolution of project
     * dependencies.
     */
    private final List<RemoteRepository> projectRepos;
    
    /**
     * The project's remote repositories to use for the resolution of
     * plugins and their dependencies.
     */
    private final List<RemoteRepository> pluginRepos;
    
    private ModelBuilder modelBuilder;
    
    private RemoteRepositoryManager repoManager;
    
    private List<MavenProject> reactorProjects;
    
    public DefaultArtifactResolver(final RepositorySystem repoSystem,
            final RemoteRepositoryManager repoManager,
            final RepositorySystemSession repoSession,
            final List<RemoteRepository> projectRepos,
            final List<RemoteRepository> pluginRepos,
            final ModelBuilder modelBuilder,
            final List<MavenProject> reactor) {
        this.repoSystem = repoSystem;
        this.repoManager = repoManager;
        this.repoSession = repoSession;
        this.projectRepos = projectRepos;
        this.pluginRepos = pluginRepos;
        this.modelBuilder = modelBuilder;
        this.reactorProjects = reactor;
    }

    public Source resolveArtifactPOM(final String gav) {
        synchronized (lock) {
            final ArtifactResult result = resolveArtifact(gav);
            return new StreamSource(result.getArtifact().getFile());
        }
    }
    
    private ArtifactResult resolveArtifact(final String artifactId) {
        final Artifact artifact = new DefaultArtifact(artifactId);
        final ArtifactRequest req =
                new ArtifactRequest(artifact, projectRepos, "project");
        
        try {
            return repoSystem.resolveArtifact(repoSession, req);
        } catch (final ArtifactResolutionException e) {
            throw ARE("Exception while resolving artifact: " + artifactId, e);
        }
    }
    
    public synchronized Source resolveDependencies(final String coords) {
        throw new UnsupportedOperationException("TODO: implement");
    }
    
    public synchronized Source resolveEffectivePOM(final String coords) {
        synchronized (lock) {
            return resolveEffectivePOMInternal(coords);
        }
    }

    private Source resolveEffectivePOMInternal(final String coords) {
        final ModelBuildingRequest request = new DefaultModelBuildingRequest()
            .setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL)
            .setModelResolver(getModelResolver())
            .setPomFile(resolveArtifact(coords).getArtifact().getFile());

        final ModelBuildingResult result = buildModel(request);

        try {
            return XPOMUtil.modelToSource(result.getEffectiveModel());
        } catch (final IOException e) {
            throw ARE("Failed to serialize model", e);
        }
    }
    
    private ModelBuildingResult buildModel(final ModelBuildingRequest request) {
        try {
            return modelBuilder.build(request);
        } catch (final ModelBuildingException e) {
            throw ARE("Exception while building model", e);
        }
    }
    
    private ModelResolver getModelResolver() {
        return new XPOMModelResolver(repoSystem, repoManager, repoSession,
                projectRepos, reactorProjects);
    }
    
    private org.dthume.maven.xpom.api.ArtifactResolutionException ARE(
            final String m, final Exception e) {
        return new org.dthume.maven.xpom.api.ArtifactResolutionException(m, e);
    }
}
