package org.dthume.maven.xpom.api;

import javax.xml.transform.Source;

public interface ArtifactResolver {
    Source resolveArtifactPOM(final String coords);
}
