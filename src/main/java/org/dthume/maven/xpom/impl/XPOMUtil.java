package org.dthume.maven.xpom.impl;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.Source;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.springframework.xml.transform.StringSource;

public class XPOMUtil {
    public static Source modelToSource(final Model model) throws IOException {
        final StringWriter writer = new StringWriter();
        new MavenXpp3Writer().write(writer, model);
        return new StringSource(writer.toString());
    }
}
