package org.dthume.maven.xpom;

import javax.xml.transform.Source;

public interface ArtifactResolver {
    Source resolveArtifactPOM(final String coords);
}
