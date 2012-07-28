/*
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
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertTrue

import org.custommonkey.xmlunit.XMLUnit;

XMLUnit.setIgnoreComments(true);
XMLUnit.setIgnoreWhitespace(true);

new XmlSlurper().parseText(
    new File(basedir, "pom.xml").text
).modules.module.collect { moduleName ->
    new File(basedir, moduleName.text());
}.each { test ->
    final File testDir = new File(test, "target/xpom-test-results");
    final File expected = new File(testDir, "expected/$target");
    final File actual = new File(testDir, "actual/$target");
    
    def diff = XMLUnit.compareXML(expected.text, actual.text);
    
    assertTrue(
        "Failed to validate testcase: $test, result of XML comparison:\n$diff",
        diff.similar()
    );
};

return true;
