package org.dthume.maven.xpom.impl.saxon.pprint;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Collection;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.TransformerFactoryImpl;

import org.codehaus.plexus.util.IOUtil;
import org.custommonkey.xmlunit.XMLUnit;
import org.dthume.jaxp.ClasspathResourceURIResolver;
import org.dthume.jaxp.ClasspathSource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PPrintXmlTest {
    private Transformer transformer;
    private File outputFile;
    
    private Source transformSource;
    private Source inputSource;
    private String expectedSource;

    public PPrintXmlTest(final String test) {
        inputSource =
                new ClasspathSource(test + "-input.xml", getClass());
        expectedSource = test + "-expected.xml";
        
        String xslName = test + "-stylesheet.xsl";
        if (null == getClass().getResourceAsStream(xslName))
            xslName = "pprint-xml.xsl";
        transformSource = new ClasspathSource(xslName, getClass());
    }
    
    @Parameters
    public static Collection<Object[]> parameters() {
        return java.util.Arrays.asList(new Object[][] {
                {"basic-document"},
                {"mixed-content"},
                {"xmlns-ordering"}
        });
    }
    
    @Rule
    public ExternalResource outputFileResource = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            outputFile = File.createTempFile("xpom-pprint-test", "xsl");
            outputFile.deleteOnExit();
        }

        @Override
        protected void after() {
            if (null != outputFile) outputFile.delete();
        }
    };
    
    @BeforeClass
    public static void beforeClassTests() {
        XMLUnit.setIgnoreAttributeOrder(false);
        XMLUnit.setIgnoreWhitespace(false);
    }
    
    @Before
    public void beforeEachTest() throws TransformerException {
        final TransformerFactory factory = new TransformerFactoryImpl();
        factory.setURIResolver(new ClasspathResourceURIResolver(getClass()));
        final Source xsl = transformSource;
        transformer = factory.newTransformer(xsl);
    }
    
    @Test
    public void test() throws Exception {
        transformer.transform(inputSource, new StreamResult(outputFile));

        final String expected =
                IOUtil.toString(getClass().getResourceAsStream(expectedSource));
        final String actual = IOUtil.toString(new FileReader(outputFile));
        
        assertEquals(expected, actual);
    }
}
