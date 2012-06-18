<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:pom="http://maven.apache.org/POM/4.0.0"
  
  exclude-result-prefixes="#all"
  xpath-default-namespace="http://maven.apache.org/POM/4.0.0"
  version="2.0">
  
  <xsl:output method="xml" indent="yes" />
  
  <xsl:template match="/ | node() | @*">
    <xsl:apply-templates select="@* | node()" mode="#current" />
  </xsl:template>
  
  <xsl:template match="/project">
    <project>
      <groupId><xsl:value-of select="parent/groupId" /></groupId>
      <artifactId><xsl:value-of select="artifactId" /></artifactId>
      <version><xsl:value-of select="parent/version" /></version>
    </project>
  </xsl:template>
</xsl:stylesheet>