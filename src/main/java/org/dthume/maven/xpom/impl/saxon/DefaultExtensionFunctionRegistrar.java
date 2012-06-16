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

import static net.sf.saxon.expr.StaticProperty.EXACTLY_ONE;
import static net.sf.saxon.type.BuiltInAtomicType.ANY_URI;
import static net.sf.saxon.value.SequenceType.OPTIONAL_DOCUMENT_NODE;
import static net.sf.saxon.value.SequenceType.SINGLE_STRING;
import static net.sf.saxon.value.SequenceType.makeSequenceType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import org.apache.maven.model.building.ModelBuilder;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.dthume.maven.xpom.api.ArtifactResolver;
import org.dthume.maven.xpom.api.ExpressionEvaluator;
import org.dthume.maven.xpom.api.TransformationContext;
import org.dthume.maven.xpom.impl.XPOMConstants;
import org.w3c.dom.Node;

@SuppressWarnings({"serial", "rawtypes"})
@Component(role=ExtensionFunctionRegistrar.class)
public class DefaultExtensionFunctionRegistrar
    implements ExtensionFunctionRegistrar {
    
    private final static SequenceType SINGLE_URI =
            makeSequenceType(ANY_URI, EXACTLY_ONE);

    @Requirement
    private TraxHelper trax;
    
    public void registerExtensionFunctions(final TransformationContext context,
            final Configuration config) {
        final List<ExtensionFunctionDefinition> functions =
                new LinkedList<ExtensionFunctionDefinition>();
        
        functions.add(new FilePathToURI());
        functions.add(new ReadPropertiesString(config));
        functions.add(new RelativizeURI());
        
        final ExpressionEvaluator evaluator = context.getExpressionEvaluator();
        if (null != evaluator)
            functions.add(new EvaluateExpression(evaluator));
        
        final ArtifactResolver resolver = context.getArtifactResolver();
        if (null != resolver) {
            functions.add(new ResolveArtifactPOM(resolver, config));
            functions.add(new EffectivePOM(resolver, config));
        }
        
        for (final ExtensionFunctionDefinition function : functions)
            config.registerExtensionFunction(function);
    }

    private final static String FN_PREFIX = "internal";

    private final static String FN_NS = XPOMConstants.INTERNAL_NS;

    private abstract class SimpleFunctionDef
        extends ExtensionFunctionDefinition {

        private final String prefix;
        private final String uri;
        private final String name;
        private final SequenceType[] argumentTypes;
        private final SequenceType resultType;
        
        protected SimpleFunctionDef(
                final String prefix, final String uri, final String name,
                final SequenceType resultType,
                final SequenceType...argumentTypes) {
            this.prefix = prefix;
            this.uri = uri;
            this.name = name;
            this.resultType = resultType;
            this.argumentTypes = argumentTypes;
        }

        @Override
        public SequenceType[] getArgumentTypes() {
            return argumentTypes;
        }

        @Override
        public final StructuredQName getFunctionQName() {
            return new StructuredQName(prefix, uri, name);
        }

        @Override
        public SequenceType getResultType(final SequenceType[] args) {
            return resultType;
        }

        @Override
        public boolean hasSideEffects() { return false; }
    }
    
    private class EvaluateExpression extends SimpleFunctionDef {
        private final ExpressionEvaluator evaluator;
        
        EvaluateExpression(final ExpressionEvaluator evaluator) {
            super(FN_PREFIX, FN_NS, "evaluate",
                    SINGLE_STRING, SINGLE_STRING);
            this.evaluator = evaluator;
        }
        
        public ExtensionFunctionCall makeCallExpression() {
            return new ExtensionFunctionCall() {
                @Override
                public SequenceIterator<? extends Item> call(
                        final SequenceIterator<? extends Item>[] args,
                        final XPathContext context) throws XPathException {
                    final String expr = args[0].next().getStringValue();
                    final Object result = evaluator.evaluate(expr);
                    final String value = resultToString(result);
                    final StringValue stringValue = new StringValue(value);

                    return SingletonIterator.makeIterator(stringValue);
                }
                
                private String resultToString(final Object result) {
                    if (null == result)
                        return "";
                    if (result instanceof File)
                        return ((File) result).toURI().toString();
                    
                    return result.toString();
                }
            };
        }
    }

    private class FilePathToURI extends SimpleFunctionDef {
        FilePathToURI() {
            super(FN_PREFIX, FN_NS, "filepath-to-uri",
                    SINGLE_URI, SINGLE_STRING);
        }
        
        public ExtensionFunctionCall makeCallExpression() {
            return new ExtensionFunctionCall() {
                @Override
                public SequenceIterator<? extends Item> call(
                        final SequenceIterator<? extends Item>[] args,
                        final XPathContext context) throws XPathException {
                    final String path = args[0].next().getStringValue().trim();
                    final String uri = new File(path).toURI().toString();
                    final AnyURIValue value = new AnyURIValue(uri);

                    return SingletonIterator.makeIterator(value);
                }                
            };
        }
    }

    private class RelativizeURI extends SimpleFunctionDef {
        RelativizeURI() {
            super(FN_PREFIX, FN_NS, "relativize-uri",
                    SINGLE_URI, SINGLE_STRING, SINGLE_STRING);
        }
        
        public ExtensionFunctionCall makeCallExpression() {
            return new ExtensionFunctionCall() {
                @Override
                public SequenceIterator<? extends Item> call(
                        final SequenceIterator<? extends Item>[] args,
                        final XPathContext context) throws XPathException {
                    final URI uri = parseURI(args[0]);
                    final URI base = parseURI(args[1]);
                    final AnyURIValue value =
                            new AnyURIValue(base.relativize(uri).toString());

                    return SingletonIterator.makeIterator(value);
                }
                
                private URI parseURI(
                        final SequenceIterator<? extends Item> seq)
                        throws XPathException {
                    try {
                        final String uri = seq.next().getStringValue().trim();
                        return new URI(uri);
                    } catch (URISyntaxException e) {
                        throw new XPathException(e);
                    }
                }
            };
        }
    }
    
    private class ReadPropertiesString extends SimpleFunctionDef {
        private final Configuration configuration;
        
        ReadPropertiesString(Configuration configuration) {
            super(FN_PREFIX, FN_NS, "read-properties-string",
                    OPTIONAL_DOCUMENT_NODE, SINGLE_STRING);
            this.configuration = configuration;
        }
        
        public ExtensionFunctionCall makeCallExpression() {
            return new ExtensionFunctionCall() {
                @Override
                public SequenceIterator<? extends Item> call(
                        SequenceIterator<? extends Item>[] args,
                        XPathContext context) throws XPathException {
                    try {
                        final String input = args[0].next().getStringValue();
                        final Node node = propertiesStringToSource(input);
                        final NodeInfo doc =
                                new DocumentWrapper(node, null, configuration);
                        return SingletonIterator.makeIterator(doc);
                    } catch (final IOException e) {
                        throw new XPathException(e);
                    } catch (final TransformerException e) {
                        throw new XPathException(e);
                    }
                }
                
                private Node propertiesStringToSource(final String input)
                    throws IOException, TransformerException {
                    final Properties props = new Properties();
                    props.load(new StringReader(input));
                    
                    final ByteArrayOutputStream out =
                            new ByteArrayOutputStream();
                    props.storeToXML(out, null);
                    
                    final ByteArrayInputStream in =
                            new ByteArrayInputStream(out.toByteArray());
                    
                    return trax.toNode(new StreamSource(in));
                }
            };
        }
    }
    
    private class ResolveArtifactPOM extends SimpleFunctionDef {
        private final ArtifactResolver resolver;
        private final Configuration configuration;
        
        ResolveArtifactPOM(final ArtifactResolver resolver,
                final Configuration configuration) {
            super(FN_PREFIX, FN_NS, "resolve-artifact-pom",
                    OPTIONAL_DOCUMENT_NODE, SINGLE_STRING);
            this.configuration = configuration;
            this.resolver = resolver;
        }
        
        public ExtensionFunctionCall makeCallExpression() {
            return new ExtensionFunctionCall() {
                @Override
                public SequenceIterator<? extends Item> call(
                        final SequenceIterator<? extends Item>[] args,
                        final XPathContext xpathContext) throws XPathException {
                    final String coords = args[0].next().getStringValue();
                    
                    Node node;
                    try {
                        node = trax.toNode(resolver.resolveArtifactPOM(coords));
                    } catch (final TransformerException e) {
                        throw new XPathException(e);
                    }
                    
                    final NodeInfo item =
                            new DocumentWrapper(node, null, configuration);
                    
                    return SingletonIterator.makeIterator(item);
                }
            };
        }
    }

    private class EffectivePOM extends SimpleFunctionDef {
        private final ArtifactResolver resolver;
        private final Configuration configuration;
        
        EffectivePOM(final ArtifactResolver resolver,
                final Configuration configuration) {
            super(FN_PREFIX, FN_NS, "effective-pom",
                    OPTIONAL_DOCUMENT_NODE, SINGLE_STRING);
            this.configuration = configuration;
            this.resolver = resolver;
        }
        
        public ExtensionFunctionCall makeCallExpression() {
            return new ExtensionFunctionCall() {
                @Override
                public SequenceIterator<? extends Item> call(
                        final SequenceIterator<? extends Item>[] args,
                        final XPathContext xpathContext) throws XPathException {
                    final String coords = args[0].next().getStringValue();
                    
                    Node node;
                    try {
                        node = trax.toNode(resolver.resolveEffectivePOM(coords));
                    } catch (final TransformerException e) {
                        throw new XPathException(e);
                    }
                    
                    final NodeInfo item =
                            new DocumentWrapper(node, null, configuration);
                    
                    return SingletonIterator.makeIterator(item);
                }
            };
        }
    }
}
