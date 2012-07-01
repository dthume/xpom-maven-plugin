<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:pom="http://maven.apache.org/POM/4.0.0"
  xmlns:xpom="urn:xpom:core"
  
  exclude-result-prefixes="#all"
  xpath-default-namespace="http://maven.apache.org/POM/4.0.0"
  version="2.0">
  
  <xsl:import href="urn:xpom:core" />
  
  <xsl:param name="xpom:basedir" />
  
  <xsl:output method="xml" indent="yes" />
  
  <xsl:template match="/ | node() | @*">
    <xsl:apply-templates select="@* | node()" mode="#current" />
  </xsl:template>
  
  <xsl:template match="/ | node() | @*" mode="relativize-paths">
    <xsl:copy>
      <xsl:apply-templates mode="#current" select="@* | node()" />
    </xsl:copy>
  </xsl:template>
  
  <xsl:template mode="relativize-paths" match="
    resources/resource/directory | outputDirectory">
    <xsl:copy>
      <xsl:sequence select="
        xpom:relativize-uri(xpom:filepath-to-uri(.), $xpom:basedir)" />
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="/project">
    <project>
      <evaluation-result>
        <xsl:sequence select="xpom:evaluate('${testExpr}')" />
      </evaluation-result>
      <evaluation-result>
        <xsl:sequence select="xpom:evaluate('foo/bar')" />
      </evaluation-result>
      
      <resolved>
        <xsl:apply-templates mode="relativize-paths" select="
          xpom:resolve-placeholders(
            build/pluginManagement/plugins/plugin[
              artifactId = 'maven-resources-plugin'
            ]/executions/execution[
              id = 'copy-expected-test-results'
            ]/configuration 
          )" />
      </resolved>
    </project>
  </xsl:template>
</xsl:stylesheet>