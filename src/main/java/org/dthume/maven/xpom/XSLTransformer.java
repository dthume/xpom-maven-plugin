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
package org.dthume.maven.xpom;

import static net.sf.saxon.lib.FeatureKeys.OUTPUT_URI_RESOLVER;
import static org.dthume.maven.xpom.XPOMConstants.xpomName;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.StandardOutputResolver;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.dthume.maven.xpom.xsl.ChainingURIResolver;
import org.dthume.maven.xpom.xsl.ExtensionFunctionRegistrar;
import org.dthume.maven.xpom.xsl.stdlib.StandardLibraryURIResolver;

@Component(role=POMTransformer.class, hint="xsl")
public class XSLTransformer implements POMTransformer {

    @Requirement(role=ExtensionFunctionRegistrar.class, optional=true)
    private List<ExtensionFunctionRegistrar> extensionFunctions =
        java.util.Collections.emptyList();

    public void transform(final TransformationContext context) {
        try {
            new Handler(context).handle();
        } catch (final TransformerException e) {
            throw new RuntimeException(e); // FIXME
        }
    }

    private final class Handler {
        private final TransformationContext context; 

        Handler(final TransformationContext context) {
            this.context = context;
        }

        void handle() throws TransformerException {
            final Source inputModel = context.getModelSource();
            final Transformer transformer = getConfiguredTransformer();
            transformer.transform(inputModel, context.getModelResult());
        }

        private Transformer getConfiguredTransformer()
                throws TransformerException {
            final Source stylesheet = context.getStylesheetSource();
            final TransformerFactory factory = getConfiguredFactory();
            final Transformer transformer = factory.newTransformer(stylesheet);
            setTransformationParams(transformer);
            return transformer;
        }
        
        private void setTransformationParams(final Transformer transformer) {
            final ExpressionEvaluator eval = context.getExpressionEvaluator(); 
            // built in
            transformer.setParameter(xpomName("basedir"),
                    ((File) eval.evaluate("${project.basedir}")).toURI());
            // user specified
            final Map<String, Object> params =
                    context.getTransformationParameters();
            for (final Map.Entry<String, Object> param : params.entrySet())
                transformer.setParameter(param.getKey(), param.getValue());
        }

        private TransformerFactory getConfiguredFactory() {
            final TransformerFactoryImpl factory = new TransformerFactoryImpl();
            final Configuration config = factory.getConfiguration();

            setFactoryAttributes(factory);
            setURIResolver(factory);
            setOutputURIResolver(factory);
            bindExtensionFunctions(config);

            return factory;
        }
        
        private void setURIResolver(final TransformerFactory factory) {
            final List<URIResolver> resolvers = java.util.Arrays.asList(
                    (URIResolver)new StandardLibraryURIResolver()
            );
            factory.setURIResolver(new ChainingURIResolver(resolvers));
        }

        private void setOutputURIResolver(final TransformerFactory factory) {
            factory.setAttribute(OUTPUT_URI_RESOLVER,
                    new StandardOutputResolver());
        }
        
        private void bindExtensionFunctions(final Configuration config) {
            for (final ExtensionFunctionRegistrar r : extensionFunctions)
                r.registerExtensionFunctions(context, config);
        }
        
        private void setFactoryAttributes(final TransformerFactory factory) {
            final Map<String, Object> attrs =
                    context.getTransformationAttributes();
            for (final Map.Entry<String, Object> attr : attrs.entrySet())
                factory.setAttribute(attr.getKey(), attr.getValue());
        }
    }
}
