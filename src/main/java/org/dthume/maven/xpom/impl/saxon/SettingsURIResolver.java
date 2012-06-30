package org.dthume.maven.xpom.impl.saxon;

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
