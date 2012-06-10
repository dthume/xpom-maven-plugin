package org.dthume.maven.xpom.xsl;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.codehaus.plexus.component.annotations.Component;
import org.w3c.dom.Node;

@Component(role=TraxHelper.class)
public class DefaultTraxHelper implements TraxHelper {
    private TransformerFactory newFactory() {
        return new net.sf.saxon.TransformerFactoryImpl();
    }
    
    public Node toNode(final Source source) throws TransformerException {
        final Transformer transformer = newFactory().newTransformer();
        final DOMResult result = new DOMResult();
        transformer.transform(source, result);
        return result.getNode();
    }
}
