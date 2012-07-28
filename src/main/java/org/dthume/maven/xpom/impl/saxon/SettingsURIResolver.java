package org.dthume.maven.xpom.impl.saxon;/*
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


import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.SettingsWriter;
import org.dthume.maven.xpom.impl.XPOMConstants;
import org.dthume.maven.xpom.impl.XPOMUtil;
import org.springframework.xml.transform.StringSource;

public class SettingsURIResolver implements URIResolver {
    private final Settings settings;
    private final SettingsWriter settingsWriter;
    
    public SettingsURIResolver(final Settings settings,
            final SettingsWriter settingsWriter) {
        this.settings = settings;
        this.settingsWriter = settingsWriter;
    }

    public Source resolve(final String href, final String base)
            throws TransformerException {
        final String uri = XPOMUtil.resolveURI(href, base);
        
        if (!XPOMConstants.SETTINGS_URI.equals(uri))
            return null;
        
        final StringWriter writer = new StringWriter();
        final Map<String, Object> params =
                java.util.Collections.emptyMap();
        try {
            settingsWriter.write(writer, params, settings);
        } catch (final IOException e) {
            throw new TransformerException(e);
        }
        
        return new StringSource(writer.toString());
    }
}
