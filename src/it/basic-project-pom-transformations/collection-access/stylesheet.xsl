<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:pom="http://maven.apache.org/POM/4.0.0"

  exclude-result-prefixes="#all"
  xpath-default-namespace="http://maven.apache.org/POM/4.0.0"
  version="2.0">
  
  <xsl:import href="urn:xpom:core:filter" />
  
  <xsl:output method="xml" indent="yes" />
  
  <xsl:template match="/project">
    <xsl:variable name="reactor" as="document-node()*" select="
      collection('urn:xpom:reactor-projects')" />
    <xsl:variable name="artifactResolution" as="element()" select="
      $reactor/project[artifactId = 'artifact-resolution']" />
    <result>
      <extract-project-coordinates-found>
        <xsl:sequence select="exists($artifactResolution)" />
      </extract-project-coordinates-found>
    </result>
  </xsl:template>
</xsl:stylesheet>