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
package org.dthume.maven.xpom.impl;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;

import org.dthume.maven.xpom.api.ArtifactResolver;
import org.dthume.maven.xpom.api.CollectionResolver;
import org.dthume.maven.xpom.api.ExpressionEvaluator;
import org.dthume.maven.xpom.api.TransformationContext;
import org.dthume.maven.xpom.api.TransformationPipeline;

public final class DefaultTransformationPipeline
    implements TransformationPipeline {

    /**
     * The charset to use when reading and writing source files; defaults
     * to platform encoding.
     */
    private String sourceEncoding = Charset.defaultCharset().name();

    private Source modelSource;

    private Result modelResult;

    private ExpressionEvaluator expressionEvaluator;

    private Properties outputProperties;
    
    private ArtifactResolver artifactResolver;
    
    private CollectionResolver collectionURIResolver;
    
    private URIResolver uriResolver;
    
    private List<TransformationContext> transformations;
    
    private Map<String, Object> transformationAttributes;
    
    public Map<String, Object> getTransformationAttributes() {
        return transformationAttributes;
    }

    public void setTransformationAttributes(final Map<String, Object> attrs) {
        this.transformationAttributes = attrs;
    }
    
    public CollectionResolver getCollectionResolver() {
        return collectionURIResolver;
    }

    public void setCollectionResolver(
            final CollectionResolver collectionResolver) {
        this.collectionURIResolver = collectionResolver;
    }

    public URIResolver getUriResolver() { return uriResolver; }

    public void setUriResolver(final URIResolver uriResolver) {
        this.uriResolver = uriResolver;
    }

    public String getSourceFileEncoding() { return sourceEncoding; }

    public void setSourceFileEncoding(final String encoding) {
        sourceEncoding = encoding;
    }

    public Source getModelSource() { return modelSource; }

    public void setModelSource(final Source source) {
        modelSource = source;
    }

    public Result getModelResult() { return modelResult; }

    public void setModelResult(final Result result) {
        modelResult = result;
    }

    public ExpressionEvaluator getExpressionEvaluator() {
        return expressionEvaluator;
    }

    public void setExpressionEvaluator(final ExpressionEvaluator eval) {
        this.expressionEvaluator = eval;
    }

    public void setTransformationOutputProperties(Properties props) {
        this.outputProperties = props;
    }
    
    public Properties getTransformationOutputProperties() {
        return outputProperties;
    }

    public ArtifactResolver getArtifactResolver() {
        return artifactResolver;
    }
    
    public void setArtifactResolver(final ArtifactResolver resolver) {
        this.artifactResolver = resolver;
    }

    public List<TransformationContext> getTransformations() {
        return transformations;
    }

    public void setTransformations(List<TransformationContext> transformations) {
        this.transformations = transformations;
    }
}
