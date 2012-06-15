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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.dthume.maven.xpom.api.XPOMException;
import org.springframework.xml.transform.StringSource;

/**
 * @goal project
 * 
 * @author dth
 */
public class ProjectMojo extends AbstractTransformingMojo {
    /**
     * Whether or not to use the original, or effective (after inheritance
     * and interpolation) project model as the input to the transformation.
     *
     * @parameter expression="${useEffectiveModel}" default-value="false"
     */
    private boolean useEffectiveModel = false;

    /**
     * Whether to use the model instead of the original POM file itself.
     * Ignored if {@link #useEffectiveModel} is {@code true}.
     *
     * @parameter expression="${useModel}" default-value="false"
     */
    private boolean useModel = false;
    
    /**
     * Parameters to pass to the execution of the XSL stylesheet.
     *
     * @parameter
     */
    private Properties transformationParameters = new Properties();

    /**
     * Attributes to set on the XSL Transformer.
     *
     * @parameter
     */
    private Properties transformationAttributes = new Properties();
    
    /**
     * (CLI) Comma separated list of parameters to pass to the execution of
     * the XSL stylesheet.
     *
     * @parameter expression="${params}"
     */
    private List<String> stringParams = java.util.Collections.emptyList();

    /**
     * (CLI) Comma separated list of attributes to set on the execution of the
     * XSL stylesheet.
     *
     * @parameter expression="${attributes}"
     */
    private List<String> stringAttributes = java.util.Collections.emptyList();
    
    @Override
    protected void prepareForTransformation()
            throws MojoExecutionException, MojoFailureException {
        if (!dryRun && isOverwriteProjectPOM())
            prepareProjectPOMForTransformation();
        else getLog().debug("Skipping backup of project POM file");
        
        super.prepareForTransformation();
    }

    private boolean isOverwriteProjectPOM() throws MojoExecutionException {
        if (null == outputFile) return true;
        
        final File projectPOM =
                getCanonicalFile(getProjectPOMFile());
        final File outputPOM = getCanonicalFile(outputFile);
        
        return projectPOM.equals(outputPOM);
    }
    
    private File getCanonicalFile(final File f) throws MojoExecutionException {
        try {
            return f.getCanonicalFile();
        } catch (final IOException e) {
            throw new MojoExecutionException("Caught IOException", e);
        }
    }
    
    @Override
    protected Map<String, Object> getTransformationParameterMap() {
        return propertiesToMap("parameter",
                transformationParameters, stringParams);
    }
    
    @Override
    protected Map<String, Object> getTransformationAttributeMap() {
        return propertiesToMap("attribute",
                transformationAttributes, stringAttributes);
    }
    
    private Map<String, Object> propertiesToMap(final String type,
            final Properties props, final List<String> strings) {
        final Map<String, Object> params = new HashMap<String, Object>();
        
        for (final Map.Entry<Object, Object> pair : props.entrySet())
            params.put(pair.getKey().toString(), pair.getValue());
        
        for (final String kvp : strings)
            addStringParameter(type, params, kvp);
        
        return params;
    }
    
    private final static String PARAM_DELIM = "="; 
    
    protected final void addStringParameter(final String type,
            final Map<String, Object> params,
            final String kvp) {
        final int lastEq = kvp.lastIndexOf(PARAM_DELIM);
            
        if (-1 == lastEq) {
            final String message = String.format("Invalid %s: %s", type, kvp);
            throw new IllegalArgumentException(message);
        }
            
        final String key = kvp.substring(0, lastEq);
        final String value =
                kvp.endsWith(PARAM_DELIM) ? "" : kvp.substring(lastEq + 1);

        params.put(key, value);
    }

    protected Source getSource() {
       return isUseModel() ? modelToSource(getModel()) : getProjectPOMSource();
    }
    
    private boolean isUseModel() { return useModel || useEffectiveModel; }
    
    private Source getProjectPOMSource() {
        try {        
            return new StreamSource(new XmlStreamReader(getProjectPOMFile()));
        } catch (final IOException e) {
            throw new XPOMException(e);
        }
    }
    
    private Source modelToSource(final Model model) {
        final StringWriter writer = new StringWriter();
        try {
            new MavenXpp3Writer().write(writer, model);
        } catch (final IOException e) {
            getLog().error("error writing model");
        }
        return new StringSource(writer.toString());
    }

    private Model getModel() {
        final Log log = getLog();

        Model model = null;
        if (useEffectiveModel) {
            log.debug("Using effective project model");
            model = getProject().getModel();
        } else {
            log.debug("Using original project Model");
            model = getProject().getOriginalModel();
        }

        if (log.isDebugEnabled()) log.debug("Model=" + model);

        return model;
    }    
}
