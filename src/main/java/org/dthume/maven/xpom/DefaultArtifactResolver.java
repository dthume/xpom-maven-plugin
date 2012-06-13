package org.dthume.maven.xpom;

import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

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

    public Source resolveArtifactPOM(final String gav) {
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
            throw new RuntimeException("TODO: fixme", e); // FIXME
        }
    }
}
