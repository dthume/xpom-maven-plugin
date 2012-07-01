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

import static java.util.Collections.singletonList;
import static org.apache.maven.repository.internal.ArtifactDescriptorUtils.toRemoteRepository;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.RemoteRepositoryManager;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

class XPOMModelResolver implements ModelResolver {
    private final Set<String> repositoryIds = new HashSet<String>();
    
    private final RepositorySystem repoSystem;
    
    private final RemoteRepositoryManager repoManager;
    
    private final RepositorySystemSession session;
    
    private final Map<String, MavenProject> reactor =
            new HashMap<String, MavenProject>();
    
    private final Map<String, ModelSource> modelCache =
            new HashMap<String, ModelSource>();
    
    private List<RemoteRepository> repositories;
    
    private final static String toCacheId(final String groupId,
            final String artifactId, final String version) {
        return new StringBuilder(groupId)
                .append(":").append(artifactId)
                .append(":").append(version)
                .toString();
    }
    
    XPOMModelResolver(final RepositorySystem repoSystem,
            final RemoteRepositoryManager repoManager,
            final RepositorySystemSession session,
            final List<RemoteRepository> repositories,
            final List<MavenProject> reactorProjects) {
        this.repoSystem = repoSystem;
        this.repoManager = repoManager;
        this.session = session;
        this.repositories = new LinkedList<RemoteRepository>(repositories);
        for (final MavenProject project : reactorProjects) {
            final String key =
                    toCacheId(project.getGroupId(), project.getArtifactId(),
                            project.getVersion());
            reactor.put(key, project);
        }
    }
    
    public XPOMModelResolver(final XPOMModelResolver base) {
        repoSystem = base.repoSystem;
        repoManager = base.repoManager;
        session = base.session;
        repositories = new LinkedList<RemoteRepository>(base.repositories);
        reactor.putAll(base.reactor);
    }
    
    public ModelResolver newCopy() {
        return new XPOMModelResolver(this);
    }
    
    public void addRepository(final Repository repository)
            throws InvalidRepositoryException {
        if (!repositoryIds.add(repository.getId()))
            return;
        
        final List<RemoteRepository> newRepos =
                singletonList(toRemoteRepository(repository));
        
        repositories = repoManager.aggregateRepositories(session, repositories,
                        newRepos, true);
    }
    
    public ModelSource resolveModel(final String groupId,
            final String artifactId,
            final String version) throws UnresolvableModelException {
        final String key = toCacheId(groupId, artifactId, version);

        ModelSource source = modelCache.get(key);
        if (null == source) {
            final File resolved = reactor.containsKey(key) ?
                  reactor.get(key).getFile()
                : resolvePOMFile(groupId, artifactId, version);
            source = new FileModelSource(resolved);
            modelCache.put(key, source);
        }
        return source;
    }
        
    private File resolvePOMFile(final String groupId,
            final String artifactId,
            final String version) throws UnresolvableModelException {
        final Artifact artifact =
                new DefaultArtifact(groupId, artifactId, "", "pom", version);
        try {
            final ArtifactRequest request =
                    new ArtifactRequest(artifact, repositories, "project");
            final Artifact resolved =
                    repoSystem.resolveArtifact(session, request).getArtifact();
            return resolved.getFile();
        } catch (final ArtifactResolutionException e) {
            throw new UnresolvableModelException(e.getMessage(), groupId,
                    artifactId, version);
        }
    }
}
