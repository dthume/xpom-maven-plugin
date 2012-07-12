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
package org.dthume.maven.xpom.api;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;

public interface TransformationPipeline {
    String getSourceFileEncoding();
    
    Source getModelSource();
    
    Result getModelResult();
    
    ExpressionEvaluator getExpressionEvaluator();
    
    ArtifactResolver getArtifactResolver();
    
    CollectionResolver getCollectionResolver();
    
    URIResolver getUriResolver();
    
    List<TransformationContext> getTransformations();
    
    Properties getTransformationOutputProperties();
    
    Map<String, Object> getTransformationAttributes();
}
