package org.dthume.maven.xpom.api;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

public interface CollectionResolver {
    Iterable<Source> resolve(String href, String base)
        throws TransformerException;
}
