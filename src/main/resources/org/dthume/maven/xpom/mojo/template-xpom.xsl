<?xml version="1.0" encoding="utf-8"?>
<!--
  #%L
  XPOM Maven Plugin
  %%
  Copyright (C) 2012 David Thomas Hume
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:exe="urn:xpom:alias:xsl"
  xmlns:doc="urn:xpom:doc"
  xmlns:pom="http://maven.apache.org/POM/4.0.0"
  xmlns:template="urn:xpom:template"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
    
  extension-element-prefixes="doc"  
  version="2.0">

  <xsl:param name="baseTransformation" as="xs:string" select="''" />

  <xsl:param name="indentSize" select="4" as="xs:integer" />

  <xsl:param name="pomIsDefaultOutputNamespace" as="xs:boolean" />
  <xsl:param name="pomIsDefaultXPathNamespace" as="xs:boolean" />

  <xsl:namespace-alias stylesheet-prefix="exe" result-prefix="xsl" />

  <xsl:output method="xml" indent="yes" />

  <xsl:function name="template:indent" as="xs:string">
    <xsl:param name="times" as="xs:integer" />
    <xsl:sequence select="
      string-join(
        (
          '&#xA;',
          for $indent in (1 to $times),
              $space in (1 to $indentSize)
            return ' '
        ),
        ''
      )" />
  </xsl:function>

  <xsl:template match="/">
    <xsl:variable name="pomNS" select="'http://maven.apache.org/POM/4.0.0'" />
    <xsl:variable name="xpomNS" select="'urn:xpom:core'" />
    <exe:stylesheet
      exclude-result-prefixes="#all"
      version="2.0">
      <xsl:namespace name="pom" select="$pomNS" />
      <xsl:namespace name="xpom" select="$xpomNS" />
      <xsl:if test="$pomIsDefaultOutputNamespace">
        <xsl:namespace name="" select="$pomNS" />
      </xsl:if>
      <xsl:if test="$pomIsDefaultXPathNamespace">
        <xsl:attribute name="xpath-default-namespace" select="$pomNS" />
      </xsl:if>
      <!-- Always import xpom core -->
      <xsl:element name="xsl:import">
        <xsl:attribute name="href" select="$xpomNS" />
      </xsl:element>
      <!-- Base transformation type import(s) -->
      <xsl:variable name="baseTransformStylesheet" select="
        if ('identity' = $baseTransformation)
          then concat($xpomNS, ':identity')
        else if ('filter' = $baseTransformation)
          then concat($xpomNS, ':filter')
        else ''" />
      <xsl:if test="0 lt string-length($baseTransformStylesheet)">
        <xsl:element name="xsl:import">
          <xsl:attribute name="href" select="$baseTransformStylesheet" />
        </xsl:element>
      </xsl:if>
      
      <xsl:text>&#xA;</xsl:text>
      <xsl:sequence select="template:indent(1)" />
      <xsl:comment>
        <xsl:sequence select="template:indent(2)" />
        <xsl:text>Simple template which plays well with all</xsl:text>
        <xsl:text> the available base transforms.</xsl:text>
        <xsl:sequence select="template:indent(2)" />
        <xsl:text>Modify or replace as appropriate.</xsl:text>
        <xsl:sequence select="template:indent(1)" />
      </xsl:comment>
      <xsl:sequence select="template:indent(1)" />
      <xsl:element name="xsl:template">
        <xsl:attribute name="match" select="'/'" />
        <xsl:element name="xsl:apply-templates">
          <xsl:attribute name="mode" select="'#current'" />
          <xsl:attribute name="select" select="'@* | node()'" />
        </xsl:element>
      </xsl:element>
    </exe:stylesheet>
  </xsl:template>
  
</xsl:stylesheet>
