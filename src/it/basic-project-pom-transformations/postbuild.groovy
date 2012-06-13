import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

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
    assertXMLEqual("Failed to validate: $test", expected.text, actual.text);
};

return true;
