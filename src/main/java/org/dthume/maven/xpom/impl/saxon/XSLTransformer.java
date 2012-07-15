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

import static net.sf.saxon.lib.FeatureKeys.COLLECTION_URI_RESOLVER;
import static net.sf.saxon.lib.FeatureKeys.COLLATION_URI_RESOLVER;
import static net.sf.saxon.lib.FeatureKeys.OUTPUT_URI_RESOLVER;
import static org.dthume.maven.xpom.impl.XPOMConstants.xpomName;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.event.TransformerReceiver;
import net.sf.saxon.lib.StandardOutputResolver;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.dthume.jaxp.ChainingURIResolver;
import org.dthume.maven.xpom.api.CollectionResolver;
import org.dthume.maven.xpom.api.ExpressionEvaluator;
import org.dthume.maven.xpom.api.POMTransformer;
import org.dthume.maven.xpom.api.TransformationContext;
import org.dthume.maven.xpom.api.TransformationPipeline;
import org.dthume.maven.xpom.api.XPOMException;
import org.dthume.maven.xpom.impl.saxon.stdlib.StandardLibraryURIResolver;
import org.dthume.maven.xpom.trax.TraxHelper;

@Component(role=POMTransformer.class, hint="xsl")
public class XSLTransformer implements POMTransformer {
    @Requirement
    private TraxHelper trax;
    
    @Requirement(role=ExtensionFunctionRegistrar.class, optional=true)
    private List<ExtensionFunctionRegistrar> extensionFunctions =
        java.util.Collections.emptyList();
    
    public void transform(final TransformationPipeline pipeline) {
        try {
            new Handler(pipeline).handle();
        } catch (final TransformerException e) {
            throw new XPOMException(e);
        }
    }

    private final class Handler {
        private final TransformationPipeline pipeline;
        private TransformerFactoryImpl factory = new TransformerFactoryImpl();

        Handler(final TransformationPipeline pipeline)
                throws TransformerException {
            this.pipeline = pipeline;
            initializeFactory();
        }

        void handle() throws TransformerException {
            final Source inputModel = pipeline.getModelSource();
            final Transformer transformer = factory.newTransformer();
            transformer.transform(inputModel, constructResultPipeline());
        }
        
        Result constructResultPipeline() throws TransformerException {
            Result current = pipeline.getModelResult();
            final List<TransformationContext> transformations =
                    new ArrayList<TransformationContext>(
                            pipeline.getTransformations());
            java.util.Collections.reverse(transformations);
            for (final TransformationContext transformation : transformations) {
                final TransformerReceiver prev = createResult(transformation);
                prev.setResult(current);
                current = prev;
            }
            return current;
        }
        
        TransformerReceiver createResult(final TransformationContext context)
            throws TransformerException {
            final Transformer transformer =
                    factory.newTransformer(context.getStylesheetSource());
            setTransformationParams(transformer, context);
            transformer.setOutputProperties(
                    pipeline.getTransformationOutputProperties());
            return new TransformerReceiver((Controller) transformer);
        }
        
        private void setTransformationParams(final Transformer transformer,
                final TransformationContext context) {
            final ExpressionEvaluator eval = pipeline.getExpressionEvaluator(); 
            // built in
            transformer.setParameter(xpomName("basedir"),
                    ((File) eval.evaluate("${project.basedir}")).toURI());
            // user specified
            final Map<String, Object> params =
                    context.getTransformationParameters();
            for (final Map.Entry<String, Object> param : params.entrySet())
                transformer.setParameter(param.getKey(), param.getValue());
        }

        private void initializeFactory() {
            factory = new TransformerFactoryImpl();
            setCollationResolver();
            setCollectionResolver();
            setFactoryAttributes();
            setURIResolver();
            setOutputURIResolver();
            bindExtensionFunctions();
        }
        
        private void setURIResolver() {
            factory.setURIResolver(new ChainingURIResolver(
                    new StandardLibraryURIResolver(),
                    new ArtifactURIResolver(pipeline.getArtifactResolver()),
                    pipeline.getUriResolver()));
        }

        private void setCollationResolver() {
            factory.setAttribute(COLLATION_URI_RESOLVER,
                    new StandardCollationsResolver());
        }
        
        private void setCollectionResolver() {
            final CollectionResolver resolver =
                    pipeline.getCollectionResolver();
            factory.setAttribute(COLLECTION_URI_RESOLVER,
                    new CollectionURIResolverAdaptor(resolver, trax));
        }

        private void setOutputURIResolver() {
            factory.setAttribute(OUTPUT_URI_RESOLVER,
                    new StandardOutputResolver());
        }
        
        private void bindExtensionFunctions() {
            final Configuration config = factory.getConfiguration();
            for (final ExtensionFunctionRegistrar r : extensionFunctions)
                r.registerExtensionFunctions(pipeline, config);
        }
        
        private void setFactoryAttributes() {
            final Map<String, Object> attrs =
                    pipeline.getTransformationAttributes();
            for (final Map.Entry<String, Object> attr : attrs.entrySet())
                factory.setAttribute(attr.getKey(), attr.getValue());
        }
    }
}
