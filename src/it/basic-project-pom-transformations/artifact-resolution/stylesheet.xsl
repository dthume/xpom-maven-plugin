<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:pom="http://maven.apache.org/POM/4.0.0"
  xmlns:xpom="urn:xpom:core"
  
  exclude-result-prefixes="#all"
  xpath-default-namespace="http://maven.apache.org/POM/4.0.0"
  version="2.0">
  
  <xsl:import href="urn:xpom:core" />
  
  <xsl:output method="xml" indent="yes" />
  
  <xsl:template match="/ | node() | @*">
    <xsl:apply-templates select="@* | node()" mode="#current" />
  </xsl:template>
  
  <xsl:template match="/project">
    <xsl:variable name="parentCoords" select="
      xpom:extract-artifact-URI(parent)" />
    <xsl:variable name="parentPOM" as="document-node()" select="
      document($parentCoords)" />
    <xsl:variable name="effectiveParent" as="document-node()" select="
      xpom:effective-pom($parentCoords)" />
    <from-parent>
      <test.prop.1>
        <xsl:value-of select="$parentPOM/project/properties/test.prop.1" />
      </test.prop.1>
      <inherited.execution.found>
        <xsl:value-of select="
          exists(
            $effectiveParent/project/descendant::build/plugins/plugin[
              artifactId = 'xpom-maven-plugin'
            ]/executions/execution[
                  id = 'execute-test-case'
              and phase = 'integration-test'
            ]
          )" />
      </inherited.execution.found>
    </from-parent>
  </xsl:template>
</xsl:stylesheet>