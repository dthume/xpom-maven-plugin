import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;

XMLUnit.setIgnoreWhitespace(true);

def modules = new XmlSlurper()
    .parseText(new File(basedir, "pom.xml").text)
    .modules.module.collect { // 
        new File(basedir, it.text());
    };

modules.each { testcase ->
    final File testCaseDir = new File(testcase, "target/xpom-test-results");
    final File expected = new File(testCaseDir, "expected/pom.xml");
    final File actual = new File(testCaseDir, "actual/pom.xml");
    XMLAssert.assertXMLEqual("Failed to validate: $testcase",
        expected.text, actual.text);
};

return true;
