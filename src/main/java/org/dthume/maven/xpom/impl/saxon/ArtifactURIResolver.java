package org.dthume.maven.xpom.impl.saxon;/*
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


import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.dthume.maven.xpom.api.ArtifactResolver;
import org.dthume.maven.xpom.impl.XPOMUtil;

public class ArtifactURIResolver implements URIResolver {

    private final ArtifactResolver resolver;
    
    public ArtifactURIResolver(final ArtifactResolver resolver) {
        this.resolver = resolver;
    }
    
    public Source resolve(final String href, final String base)
            throws TransformerException {
        if (null == resolver) return null;
        
        final String resolved = XPOMUtil.resolveURI(href, base);
        final XPomUri uri = XPomUri.parseURIOrNull(resolved);
        
        if (null == uri) return null;
        
        Source source;
        if (uri.isResourceURI()) {
            final Reader reader =
                resolver.resolveResource(uri.getCoords(), uri.getResource());
            source = new StreamSource(reader, resolved);
        } else if ("true".equalsIgnoreCase(uri.getParams().get("effective"))) {
            source = resolver.resolveEffectivePOM(uri.getCoords());
        } else {
            source = resolver.resolveArtifactPOM(uri.getCoords());
        }
        return source;
    }
}