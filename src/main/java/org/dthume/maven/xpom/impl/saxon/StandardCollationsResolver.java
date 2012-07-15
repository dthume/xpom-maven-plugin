package org.dthume.maven.xpom.impl.saxon;

import org.apache.maven.artifact.versioning.ComparableVersion;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.CollationURIResolver;
import net.sf.saxon.lib.StandardCollationURIResolver;
import net.sf.saxon.lib.StringCollator;

public class StandardCollationsResolver implements CollationURIResolver {
    private static final long serialVersionUID = 1L;
    
    private final CollationURIResolver defaultResolver =
            new StandardCollationURIResolver();
    
    public StringCollator resolve(final String relativeUri,
            final String baseUri, final Configuration config) {
        StringCollator collator = null;
        
        if ("urn:xpom:collations:maven-version".equals(relativeUri))
            collator = new MavenVersionCollator();
        
        if (null == collator)
            collator = defaultResolver.resolve(relativeUri, baseUri, config);
        
        return collator;
    }
    
    private static class MavenVersionCollator implements StringCollator {
        private static final long serialVersionUID = 1L;

        public Object getCollationKey(final String s) {
            return new ComparableVersion(s).toString();
        }
            
        public boolean comparesEqual(final String s1, final String s2) {
            return 0 == compareStrings(s1, s2);
        }
            
        public int compareStrings(final String s1, final String s2) {
            final ComparableVersion v1 = new ComparableVersion(s1);
            final ComparableVersion v2 = new ComparableVersion(s2);
            return v1.compareTo(v2);
        }
    }
}
