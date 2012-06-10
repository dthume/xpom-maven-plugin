package org.dthume.maven.xpom.xsl;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

public class ChainingURIResolver implements URIResolver {

    private final Iterable<URIResolver> resolvers;

    public ChainingURIResolver(final Iterable<URIResolver> resolvers) {
        this.resolvers = null != resolvers ? resolvers
                :java.util.Collections.<URIResolver>emptyList();
    }

    public Source resolve(final String href, final String base)
            throws TransformerException {
        Source result = null;
        for (final URIResolver resolver : resolvers)
            if (null != (result = resolve(resolver, href, base)))
                break;
        return result;
    }

    private Source resolve(final URIResolver resolver,
            final String href, final String base) {
        try {
            return resolver.resolve(href, base);
        } catch (Exception e) {
            return null; // as per api spec
        }
    }
}
