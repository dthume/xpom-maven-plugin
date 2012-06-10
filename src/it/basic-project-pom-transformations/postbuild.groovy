import java.util.Properties;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;

/* ========================= Test Case Location ========================= */

def projectFile = { name -> return new File(basedir, name); }

def loadProjectProperties = { fileName ->
    def props = new Properties();
    projectFile(fileName).withInputStream { stream ->
        props.load(stream);
    }
    return props;
}

def getSystemPropertiesFiles = { props ->
    int ii = 1;
    def sysPropFiles = [];
    String propName = null;
    
    while (null != (propName = props["invoker.systemPropertiesFile." + ii++]))
        sysPropFiles.add(propName);
    
    return sysPropFiles;
}

def getTestCases = { ->
    def invokerProps = loadProjectProperties("invoker.properties");
    def sysPropFiles = getSystemPropertiesFiles(invokerProps);
    return sysPropFiles.collect(loadProjectProperties).collect { sysProps ->
        sysProps["testcase"];
    }
}

/* ======================== Test Case Validation ======================== */

def TEST_CASE_RESULTS = projectFile("target/xpom-test-results")

def assertXMLFilesEqual = { msg, expected, actual ->
    XMLAssert.assertXMLEqual(msg, expected.text, actual.text);
};

def assertSimpleTestCasePass = { testcase ->
    println "Validating testcase: $testcase";
    def testCaseDir = new File(TEST_CASE_RESULTS, testcase);
    def expected = new File(testCaseDir, "expected/pom.xml");
    def actual = new File(testCaseDir, "actual/pom.xml");
    assertXMLFilesEqual("Failed to validate: $testcase", expected, actual);
};

/* ============================== Execution ============================== */

XMLUnit.setIgnoreWhitespace(true);

getTestCases().each(assertSimpleTestCasePass);

return true;
