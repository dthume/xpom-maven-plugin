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

import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.dthume.maven.xpom.api.ArtifactResolver;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

public class DefaultArtifactResolver implements ArtifactResolver {

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

    public DefaultArtifactResolver(final RepositorySystem repoSystem,
            final RepositorySystemSession repoSession,
            final List<RemoteRepository> projectRepos,
            final List<RemoteRepository> pluginRepos) {
        this.repoSystem = repoSystem;
        this.repoSession = repoSession;
        this.projectRepos = projectRepos;
        this.pluginRepos = pluginRepos;
    }

    public synchronized Source resolveArtifactPOM(final String gav) {
        final ArtifactResult result = resolveArtifact(gav);
        return new StreamSource(result.getArtifact().getFile());
    }
    
    private ArtifactResult resolveArtifact(final String artifactId) {
        final Artifact artifact = new DefaultArtifact(artifactId);
        final ArtifactRequest req =
                new ArtifactRequest(artifact, projectRepos, null);            
        try {
            return repoSystem.resolveArtifact(repoSession, req);
        } catch (final ArtifactResolutionException e) {
            throw new org.dthume.maven.xpom.api.ArtifactResolutionException(e);
        }
    }
    
    public synchronized Source resolveDependencies(final String coords) {
        throw new IllegalArgumentException("TODO: implement");
    }
}
