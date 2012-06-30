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

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.dthume.maven.xpom.api.ExpressionEvaluator;
import org.springframework.xml.transform.StringSource;

public class XPOMUtil {
    public static Source modelToSource(final Model model) throws IOException {
        final StringWriter writer = new StringWriter();
        new MavenXpp3Writer().write(writer, model);
        return new StringSource(writer.toString());
    }
    
    public static ExpressionEvaluator expressionEvaluatorForSession(
            final MavenSession session) {
        final MojoExecution execution = new MojoExecution(new MojoDescriptor());
        final PluginParameterExpressionEvaluator evaluator =
                new PluginParameterExpressionEvaluator(session, execution);
        return new DefaultExpressionEvaluator(evaluator);
    }
    
    public static String resolveURI(final String href, final String base)
            throws TransformerException {
        try {
            return StringUtils.isBlank(base) ? href
                    : new URI(base).resolve(href).toString();
        } catch (final URISyntaxException e) {
            return href;
        }
    }
}
