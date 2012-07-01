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

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.CollectionURIResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

import org.dthume.maven.xpom.api.CollectionResolver;
import org.dthume.maven.xpom.trax.TraxHelper;
import org.w3c.dom.Node;

public class CollectionURIResolverAdaptor implements CollectionURIResolver {
    private static final long serialVersionUID = 1L;
    
    private final CollectionResolver resolver;
    private final TraxHelper trax;

    public CollectionURIResolverAdaptor(final CollectionResolver resolver,
            final TraxHelper trax) {
        this.resolver = resolver;
        this.trax = trax;
    }
    
    public SequenceIterator<?> resolve(final String href, final String base,
            final XPathContext ctxt) throws XPathException {
        final Iterable<Source> sources = resolveInternal(href, base, ctxt);
        return new SeqIterator(sources, ctxt.getConfiguration());
    }
    
    private Iterable<Source> resolveInternal(final String href,
            final String base, final XPathContext ctxt)
        throws XPathException {
        Iterable<Source> sources = null;
        
        try {
            sources = resolver.resolve(href, base);
        } catch (final TransformerException e) {}
        
        if (null == sources) sources = java.util.Collections.emptyList();
        
        return sources;
    }

    private class SeqIterator implements SequenceIterator<NodeInfo> {
        private Configuration config;
        private Iterable<Source> iterable;
        private Iterator<Source> iterator;
        private int position = -1;
        private NodeInfo current = null;
        
        SeqIterator(final Iterable<Source> iterable,
                final Configuration config) {
            this.config = config;
            this.iterable = iterable;
            this.iterator = iterable.iterator();
        }
        
        public void close() {
            if (iterator instanceof Closeable) {
                try {
                    ((Closeable) iterator).close();
                } catch (IOException e) {};
            }
            iterator = null;
            current = null;
            iterable = null;
            config = null;
        }

        public NodeInfo current() { return current; }

        public SequenceIterator<NodeInfo> getAnother() throws XPathException {
            return new SeqIterator(iterable, config);
        }

        public int getProperties() { return 0; }

        public int position() { return position; }

        public NodeInfo next() throws XPathException {
            if (iterator.hasNext()) {
                final Source next = iterator.next();
                final Node node = toNode(next);
                current = new DocumentWrapper(node, next.getSystemId(), config);
                position++;
                return current;
            } else return null;
        }
        
        private Node toNode(final Source source) throws XPathException {
            try {
                return trax.toNode(source);
            } catch (final TransformerException e) {
                throw new XPathException(e);
            }
        }
    }
}
