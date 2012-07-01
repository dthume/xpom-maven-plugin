<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:pom="http://maven.apache.org/POM/4.0.0"
  xmlns:xpom="urn:xpom:core"
  exclude-result-prefixes="#all"
  xpath-default-namespace="http://maven.apache.org/POM/4.0.0"
  version="2.0">
  
  <xsl:import href="urn:xpom:core" />
  
  <xsl:param name="param1" required="yes" />
  <xsl:param name="param2" required="yes" />
  <xsl:param name="cliParam1" required="yes" />
  <xsl:param name="cliParam2" required="yes" />
  
  <xsl:param name="xpom:basedir" required="yes" />

  <xsl:output method="xml" indent="yes" />
  
  <xsl:template match="/ | node() | @*">
    <xsl:apply-templates select="@* | node()" mode="#current" />
  </xsl:template>
  
  <xsl:template match="/project">
    <result>
      <param1><xsl:sequence select="$param1" /></param1>
      <param2><xsl:sequence select="$param2" /></param2>
      <cliParam1><xsl:sequence select="$cliParam1" /></cliParam1>
      <cliParam2><xsl:sequence select="$cliParam2" /></cliParam2>
      <basedir>
        <!--
          We can't specify basedir in our expected result, since it's an
          absolute path, so instead we read a file that we know is in a well
          defined location relative to base dir 
        -->
        <xsl:sequence select="
          unparsed-text(
            resolve-uri('fixtures/simpleValue.txt', $xpom:basedir)
          )" />
      </basedir>
    </result>
  </xsl:template>
</xsl:stylesheet>