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
