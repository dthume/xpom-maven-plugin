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
package org.dthume.maven.xpom.impl.saxon;

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
