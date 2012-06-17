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
package org.dthume.maven.xpom.mojo;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.serialize.XMLEmitter;
import net.sf.saxon.serialize.XMLIndenter;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.springframework.xml.transform.StringSource;

/**
 * Create a new XPOM XSL file
 * 
 * @goal new
 * 
 * @author dhume
 */
public class NewMojo extends AbstractXPOMMojo {
    /**
     * Whether or not to set the POM namespace to be the default namespace
     * for XPath expressions within the stylesheet.
     *
     * @parameter
     *  expression="${defaultXPathNS}"
     *  default-value="false"
     */
    private boolean defaultXPathNamespace = false;
    
    /**
     * Whether or not to set the POM namespace to be the default namespace
     * for elements within the stylesheet.
     *
     * @parameter
     *  expression="${defaultOutputNS}"
     *  default-value="false"
     */
    private boolean defaultOutputNamespace = false;
    
    /**
     * The base transformation to import into the newly generated stylesheet;
     * if specified, must be one of the values {@code identity} or
     * {@code filter}.
     * 
     * @parameter expression="${base}"
     */
    private String baseTransformation = null;
    
    /**
     * The name of the file to generate
     * 
     * @required
     * @parameter expression="${outputFile}"
     */
    private File outputFile;
    
    /**
     * The number of spaces to use for each level of indentation
     * 
     * @parameter expression="${indentSize}" default-value="4"
     */
    private int indentSize = 4;
    
    /**
     * Whether or not to overwrite the {@code outputFile}; if this is
     * {@code false} then the mojo will fail rather than overwrite the target.
     * 
     * @parameter expression="${overwrite}" default-value="false"
     */
    private boolean overwrite = false;
    
    @Override
    protected void executeInternal()
            throws MojoExecutionException, MojoFailureException {
        if (!overwrite && outputFile.exists()) {
            final String msg =
                    "overwrite is not true, and the outputFile: "
                    + outputFile.getAbsolutePath()
                    + " already exists";
            throw new MojoFailureException(msg);
        }
        
        try {
            final Transformer transformer = getConfiguredTransformer();
            final Source source = new StringSource("<null />");
            final Result result = new StreamResult(outputFile);
            transformer.transform(source, result);
        } catch (final TransformerException e) {
            final String msg = "Exception during generation of new XPOM XSL";
            getLog().warn(msg);
            throw new MojoExecutionException(msg, e);
        }
    }
    
    private Transformer getConfiguredTransformer() throws TransformerException {
        final TransformerFactoryImpl factory = new TransformerFactoryImpl();
        final Configuration config = factory.getConfiguration();
        final InputStream in =
                getClass().getResourceAsStream("template-xpom.xsl");
        final Source xsl = new StreamSource(in);
        
        final Transformer tf = factory.newTransformer(xsl);
        
        tf.setParameter("indentSize", indentSize);
        tf.setParameter("pomIsDefaultOutputNamespace", defaultOutputNamespace);
        tf.setParameter("pomIsDefaultXPathNamespace", defaultXPathNamespace);
        if (!StringUtils.isBlank(baseTransformation))
            tf.setParameter("baseTransformation", baseTransformation);
        
        config.setSerializerFactory(getSerializerFactory(config));
        
        return tf;
    }
    
    private final static String XSL_NS =
            "{http://www.w3.org/1999/XSL/Transform}";
    
    private final static Set<String> DOUBLE_SPACED_NODES =
            unmodifiableSet(new HashSet<String>(asList(
                    XSL_NS + "import",
                    XSL_NS + "include",
                    XSL_NS + "output",
                    XSL_NS + "template"
            )));
    
    private SerializerFactory getSerializerFactory(final Configuration config) {
        return new XPOMTemplateSerializerFactory(config);
    }
    
    private class XPOMTemplateSerializerFactory extends SerializerFactory {
        private static final long serialVersionUID = 1L;

        XPOMTemplateSerializerFactory(final Configuration config) {
            super(config);
        }

        @Override
        protected ProxyReceiver newXMLIndenter(final XMLEmitter next,
                final Properties outputProperties) {
            return new XMLIndenter(next) {
                @Override
                protected int getIndentation() { return indentSize; }

                @Override
                protected boolean isDoubleSpaced(final NodeName name) {
                    final String qn =
                            name.getStructuredQName().getClarkName();
                    return DOUBLE_SPACED_NODES.contains(qn);
                }
            };        
        }
    }
}
