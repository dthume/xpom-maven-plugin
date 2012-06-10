package org.dthume.maven.xpom.xsl;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;

public interface TraxHelper {
    Node toNode(Source source) throws TransformerException;
}
