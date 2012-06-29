package org.dthume.maven.xpom.impl.saxon;

import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.dthume.maven.xpom.api.ArtifactResolver;

public class ArtifactURIResolver implements URIResolver {

    private final ArtifactResolver resolver;
    
    public ArtifactURIResolver(final ArtifactResolver resolver) {
        this.resolver = resolver;
    }
    
    public Source resolve(final String href, final String base)
            throws TransformerException {
        if (null == resolver) return null;
        
        final String resolved = resolveURI(href, base);
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

    private String resolveURI(final String href, final String base)
            throws TransformerException {
        try {
            return StringUtils.isBlank(base) ? href
                    : new URI(base).resolve(href).toString();
        } catch (final URISyntaxException e) {
            return href;
        }
    }
}