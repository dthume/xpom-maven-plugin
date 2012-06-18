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

import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

public interface TransformationContext {
    String getSourceFileEncoding();
    
    Source getModelSource();
    
    Result getModelResult();
    
    Source getStylesheetSource();
    
    ExpressionEvaluator getExpressionEvaluator();
    
    Map<String, Object> getTransformationParameters();
    
    Map<String, Object> getTransformationAttributes();

    Properties getTransformationOutputProperties();
    
    ArtifactResolver getArtifactResolver();
    
    CollectionResolver getCollectionResolver();
}
