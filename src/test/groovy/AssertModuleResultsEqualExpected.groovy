import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.fail;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;

XMLUnit.setIgnoreWhitespace(true);

new XmlSlurper().parseText(
    new File(basedir, "pom.xml").text
).modules.module.collect { moduleName ->
    new File(basedir, moduleName.text());
}.each { test ->
    final File testDir = new File(test, "target/xpom-test-results");
    final File expected = new File(testDir, "expected/pom.xml");
    final File actual = new File(testDir, "actual/pom.xml");
    if (!XMLUnit.compareXML(expected.text, actual.text).similar()) {
        fail("Failed to validate testcase: $test");
    }
};

return true;
