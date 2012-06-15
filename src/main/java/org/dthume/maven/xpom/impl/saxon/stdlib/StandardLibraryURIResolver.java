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
package org.dthume.maven.xpom.impl.saxon.stdlib;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.dthume.maven.xpom.impl.XPOMConstants;

public class StandardLibraryURIResolver implements URIResolver {

    private final static String CORE_URN = XPOMConstants.CORE_NS;
    
    private final static String BASE_URN = CORE_URN + ":";
    
    private final static String CORE_XSL = "xpom-core";
    
    private final static String MASKED_URN = BASE_URN + CORE_XSL;
    
    public Source resolve(final String href, final String base)
            throws TransformerException {
        if (CORE_URN.equals(href))
            return readResource(CORE_XSL, href);
        
        if (isBlank(href)
                || MASKED_URN.equals(href)
                || !href.startsWith(BASE_URN))
            return null;
        
        return readResource(href.substring(BASE_URN.length()), href);
    }
    
    private Source readResource(String name, String href) {
        final InputStream in =
                getClass().getResourceAsStream(name + ".xsl");
        return new StreamSource(in, href);
    }
}
