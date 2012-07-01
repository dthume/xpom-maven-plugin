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

import javax.xml.transform.Source;

import org.dthume.maven.xpom.api.ArtifactResolver;

/**
 * Transform an arbitrary artifact POM.
 * 
 * @goal artifact
 * @goal requiresProject false
 * 
 * @author dth
 */
public class ArtifactMojo extends AbstractPOMTransformingMojo {
    /**
     * @required
     * @parameter expression="${artifact}"
     */
    private String artifact;
    
    protected Source getSource() {
        final String coords = getCoords();
        final ArtifactResolver resolver = getArtifactResolver();
        
        if (isUseEffectiveModel()) {
            return resolver.resolveEffectivePOM(coords);
        } else {
            return resolver.resolveArtifactPOM(coords);
        }
    }
    
    private String getCoords() {
        final String[] parts = artifact.trim().split(":");
        if (3 == parts.length) {
            return String.format("%s:%s:pom:%s",
                    parts[0], parts[1], parts[2]);
        } else if (4 == parts.length) {
            return String.format("%s:%s:pom:%s:%s",
                    parts[0], parts[1], parts[2], parts[3]);
        } else {
            throw new IllegalArgumentException("Invalid artifact: " + artifact);
        }
    }
}
