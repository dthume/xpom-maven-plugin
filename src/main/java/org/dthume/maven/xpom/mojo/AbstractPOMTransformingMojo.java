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

import static java.util.Collections.emptyList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author dth
 */
public abstract class AbstractPOMTransformingMojo
    extends AbstractTransformingMojo {
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
     * (CLI) Comma separated list of parameters to pass to the execution of
     * the XSL stylesheet.
     *
     * @parameter expression="${params}"
     */
    private List<String> stringParams = emptyList();
    
    /**
     * Attributes to set on the XSL Transformer.
     *
     * @parameter
     */
    private Properties transformationAttributes = new Properties();
    
    /**
     * (CLI) Comma separated list of attributes to set on the execution of the
     * XSL stylesheet.
     *
     * @parameter expression="${attributes}"
     */
    private List<String> stringAttributes = emptyList();
    
    /**
     * Output properties to pass to the execution of the XSL stylesheet.
     *
     * @parameter
     */
    private Properties outputProperties= new Properties();

    /**
     * (CLI) Comma separated list of output properties to pass to the execution
     * of the XSL stylesheet.
     *
     * @parameter expression="${outputProperties}"
     */
    private List<String> stringOutputProperties = emptyList();    
    
    @Override
    protected Map<String, Object> getTransformationParameterMap() {
        return propertiesToMap("parameter",
                transformationParameters,
                super.getTransformationParameterMap(),
                stringParams);
    }
    
    @Override
    protected Map<String, Object> getTransformationAttributeMap() {
        return propertiesToMap("attribute",
                transformationAttributes,
                super.getTransformationAttributeMap(),
                stringAttributes);
    }
    
    @Override
    protected Properties getOutputProperties() {
        final Properties props = new Properties();
        props.putAll(super.getOutputProperties());
        props.putAll(outputProperties);
        
        for (final String kvp : stringOutputProperties) {
            final Map.Entry<String, String> entry =
                    toStringParam("outputProperty", kvp);
            props.put(entry.getKey(), entry.getValue());
        }
        
        return props;
    }
    
    private Map<String, Object> propertiesToMap(final String type,
            final Properties props,
            final Map<String, Object> baseMap,
            final List<String> strings) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.putAll(baseMap);
        
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
        final Map.Entry<String, String> entry = toStringParam(type, kvp);
        params.put(entry.getKey(), entry.getValue());
    }
    
    protected final Map.Entry<String, String> toStringParam(final String type,
            final String kvp) {
        final int lastEq = kvp.lastIndexOf(PARAM_DELIM);
            
        if (-1 == lastEq) {
            final String message = String.format("Invalid %s: %s", type, kvp);
            throw new IllegalArgumentException(message);
        }
            
        final String key = kvp.substring(0, lastEq);
        final String value =
                kvp.endsWith(PARAM_DELIM) ? "" : kvp.substring(lastEq + 1);

        return new Map.Entry<String, String>() {
            public String getKey() { return key; }
            public String getValue() { return value; }

            public String setValue(String value) {
                throw new UnsupportedOperationException();
            }
        };
    }

    protected final boolean isUseModel() {
        return useModel || useEffectiveModel;
    }
    
    protected final boolean isUseEffectiveModel() {
        return useEffectiveModel;
    }
}
