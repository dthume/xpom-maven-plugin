package org.dthume.maven.xpom.trax;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.dthume.maven.xpom.impl.XPOMUtil;

public class ClasspathURIResolver implements URIResolver {
    private final ClassLoader classLoader;
    
    public ClasspathURIResolver() {
        this(ClasspathURIResolver.class);
    }
    
    public ClasspathURIResolver(final Class<?> clazz) {
        this(clazz.getClassLoader());
    }
    
    public ClasspathURIResolver(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    public Source resolve(final String href, final String base)
            throws TransformerException {
        try {
            return resolveInternal(href, base);
        } catch (final Exception e) {
            return null;
        }
    }

    private Source resolveInternal(final String href, final String base)
        throws TransformerException, URISyntaxException {
        final URI uri = new URI(XPOMUtil.resolveURI(href, base));
        
        if (!"classpath".equals(uri.getScheme()))
            return null;
        
        final String path = uri.getSchemeSpecificPart();
        final InputStream in = classLoader.getResourceAsStream(path);
        
        return new StreamSource(in, uri.toString());
    }
}
