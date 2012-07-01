package org.dthume.maven.xpom.impl.saxon;

import static org.junit.Assert.*;

import org.junit.Test;

public class XPomUriTest {
    @Test
    public void shouldMatchArtifactWithNoClassifier() throws Exception {
        final String expected = "org.dthume:artifact:pom:1.0-SNAPSHOT";
        final XPomUri uri = new XPomUri(
                "xpom://org.dthume/artifact/pom/no;classifier/1.0-SNAPSHOT");
        
        assertFalse("Should not be a resource URI", uri.isResourceURI());
        assertEquals(expected, uri.getCoords());
    }
    
    @Test
    public void shouldMatchArtifactWithClassifier() throws Exception {
        final String expected = "org.dthume:artifact:pom:src:1.0-SNAPSHOT";
        final XPomUri uri =
                new XPomUri("xpom://" + expected.replace(":", "/"));
        
        assertFalse("Should not be a resource URI", uri.isResourceURI());
        assertEquals(expected, uri.getCoords());
    }
    
    @Test
    public void shouldMatchResourceWithNoClassifier() throws Exception {
        final String expectedGAV = "org.dthume:artifact:pom:1.0-SNAPSHOT";
        final String expectedResource = "some/resource.xml";
        final XPomUri uri = new XPomUri(new StringBuilder("xpom://")
            .append("org.dthume/artifact/pom/no;classifier/1.0-SNAPSHOT/")
            .append(expectedResource)
            .toString());
        
        assertTrue("Should be a resource URI", uri.isResourceURI());
        assertEquals(expectedGAV, uri.getCoords());
        assertEquals(expectedResource, uri.getResource());
    }
    
    @Test
    public void shouldMatchResourceWithClassifier() throws Exception {
        final String expectedGAV = "org.dthume:artifact:jar:src:1.0-SNAPSHOT";
        final String expectedResource = "some/resource.xml";
        final XPomUri uri = new XPomUri(new StringBuilder("xpom://")
            .append(expectedGAV.replace(":", "/"))
            .append("/")
            .append(expectedResource)
            .toString());
        
        assertTrue("Should be a resource URI", uri.isResourceURI());
        assertEquals(expectedGAV, uri.getCoords());
        assertEquals(expectedResource, uri.getResource());
    }
    
    @Test
    public void shouldMatchArtifactWithNoClassifierAndParams()
            throws Exception {
        final String expected = "org.dthume:artifact:pom:1.0-SNAPSHOT";
        final XPomUri uri = new XPomUri(
                "xpom://org.dthume/artifact/pom/no;classifier/1.0-SNAPSHOT?effective=true");
        
        assertFalse("Should not be a resource URI", uri.isResourceURI());
        assertEquals(expected, uri.getCoords());
        assertEquals(java.util.Collections.singletonMap("effective", "true"),
                uri.getParams());
    }
}
