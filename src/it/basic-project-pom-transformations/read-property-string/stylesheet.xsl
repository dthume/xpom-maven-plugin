<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:pom="http://maven.apache.org/POM/4.0.0"
  xmlns:test="urn:xpom:it:local"
  xmlns:prop="urn:xpom:core:java-properties"
  
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="urn:xpom:core:java-properties" />
  
  <xsl:output method="xml" indent="yes" />
  
  <xsl:template match="/ | node() | @*">
    <xsl:apply-templates select="@* | node()" mode="#current" />
  </xsl:template>
  
  <xsl:template match="/ | node() | @*" mode="test:sort-properties">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="#current" />
    </xsl:copy>
  </xsl:template>
  
  <xsl:template mode="test:sort-properties" match="properties">
    <xsl:copy>
      <xsl:apply-templates select="@*" mode="#current" />
      <xsl:apply-templates select="entry" mode="#current">
        <xsl:sort select="@key" />
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:function name="test:sort-properties">
    <xsl:param name="props" />
    <xsl:apply-templates mode="test:sort-properties" select="$props" /> 
  </xsl:function>
  
  <!--============================= Test Cases =============================-->
  
  <xsl:template match="/pom:project">
    <result>
      <inline-props>
        <xsl:sequence select="
          test:sort-properties(
            prop:parse-properties-string('foo=bar&#xA;me=you')
          )" />
      </inline-props>
      <props-from-unparsed-text>
        <xsl:sequence select="
          test:sort-properties(
            prop:parse-properties-string(
              unparsed-text('simple-props.properties')
            )
          )" />
      </props-from-unparsed-text>
      <reserialized-props>
        <xsl:sequence select="
          prop:write-properties-string(
            test:sort-properties(
              prop:parse-properties-string('foo=bar&#xA;me=you')
            )
          )" />
      </reserialized-props>
    </result>
  </xsl:template>
</xsl:stylesheet>