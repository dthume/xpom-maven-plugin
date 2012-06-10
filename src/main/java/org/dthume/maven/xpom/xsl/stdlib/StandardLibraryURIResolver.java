package org.dthume.maven.xpom.xsl.stdlib;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.dthume.maven.xpom.XPOMConstants;

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
