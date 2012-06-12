import java.util.Properties;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;

/* ========================= Test Case Location ========================= */

def projectFile(name) {
    return new File(basedir, name);
}

def findModuleNames(File pomFile) {
    return new XmlSlurper()
        .parseText(pomFile.text)
        .modules.module.collect { it.text(); };
}

def findModules(File pomFile) {
    final File parent = pomFile.getParentFile();
    return findModuleNames(pomFile).collect{ new File(parent, it); };
}

/* ======================== Test Case Validation ======================== */

def testCaseResultDirFor(project) {
    return new File(project, "target/xpom-test-results");
}

def assertXMLFilesEqual(msg, expected, actual) {
    XMLAssert.assertXMLEqual(msg, expected.text, actual.text);
}

def assertSimpleTestCasePass = {testcase ->
    def testCaseDir = testCaseResultDirFor(testcase);
    def expected = new File(testCaseDir, "expected/pom.xml");
    def actual = new File(testCaseDir, "actual/pom.xml");
    assertXMLFilesEqual("Failed to validate: $testcase", expected, actual);
}

/* ============================== Execution ============================== */

XMLUnit.setIgnoreWhitespace(true);

findModules(projectFile("pom.xml")).each(assertSimpleTestCasePass);

return true;
