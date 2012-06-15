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
    <xsl:variable name="parentPOM" as="document-node()" select="
      xpom:resolve-artifact-pom(
        string-join(
          (parent/groupId, parent/artifactId, 'pom', parent/version),
          ':'
        )
      )" />
    <from-parent>
      <test.prop.1>
        <xsl:value-of select="$parentPOM/project/properties/test.prop.1" />
      </test.prop.1>
    </from-parent>
  </xsl:template>
</xsl:stylesheet>