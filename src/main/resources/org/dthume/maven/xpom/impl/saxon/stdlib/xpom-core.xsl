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
  xmlns:doc="urn:xpom:doc"
  xmlns:internal="urn:xpom:internal"
  xmlns:pom="http://maven.apache.org/POM/4.0.0"
  xmlns:xpom="urn:xpom:core"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  
  exclude-result-prefixes="#all"
  version="2.0">

  <doc:doc type="stylesheet">
    <doc:description>
      Core XPOM functions
    </doc:description>
  </doc:doc>
  
  <doc:doc>
    <doc:description>
      Evaluate the supplied string expression in the context of the current
      POM.
    </doc:description>
    <doc:param name="expr">The expression to evaluate.</doc:param>
  </doc:doc>
  <xsl:function name="xpom:evaluate" as="xs:string">
    <xsl:param name="expr" as="xs:string" />
    <xsl:sequence select="internal:evaluate($expr)" />
  </xsl:function>

  <doc:doc>
    <doc:description>
      Convert a system dependent file path to a URI
    </doc:description>
    <doc:param name="path">The path to convert.</doc:param>
  </doc:doc>
  <xsl:function name="xpom:filepath-to-uri" as="xs:anyURI">
    <xsl:param name="path" as="xs:string" />
    <xsl:sequence select="internal:filepath-to-uri($path)" />
  </xsl:function>

  <doc:doc>
    <doc:description>
      Relativize the given URI against a base URI.
    </doc:description>
    <doc:param name="uri">The URI to relativize</doc:param>
    <doc:param name="base">
      The base URI against which uri should be relativized
    </doc:param>
  </doc:doc>
  <xsl:function name="xpom:relativize-uri" as="xs:anyURI">
    <xsl:param name="uri" as="xs:string" />
    <xsl:param name="base" as="xs:string" />
    <xsl:sequence select="internal:relativize-uri($uri, $base)" />
  </xsl:function>
  
  <doc:doc>
    <doc:description>
      Resolve and return the POM for the given coordinates. 
    </doc:description>
    <doc:param name="coordinates">The coordinates to resolve </doc:param>
  </doc:doc>
  <xsl:function name="xpom:resolve-artifact-pom" as="document-node()?">
    <xsl:param name="coordinates" as="xs:string" />
    <xsl:sequence select="internal:resolve-artifact-pom($coordinates)" />
  </xsl:function>

  <doc:doc>
    <doc:description>
      Resolve and return the POM for the given coordinates. 
    </doc:description>
    <doc:param name="coordinates">The coordinates to resolve </doc:param>
  </doc:doc>
  <xsl:function name="xpom:effective-pom" as="document-node()?">
    <xsl:param name="coordinates" as="xs:string" />
    <xsl:sequence select="internal:effective-pom($coordinates)" />
  </xsl:function>

  <!--======================= Resolve Placeholders =======================-->

  <doc:doc type="mode" for="xpom:resolve-placeholders">
    <doc:description>
      Resolve placeholder expressions using the context of the current POM.
    </doc:description>
  </doc:doc>
  
  <doc:doc>
    <doc:description>
      General template for text items; evaluates the text in the context as
      an expression in the context of the current POM.
    </doc:description>
  </doc:doc>
  <xsl:template mode="xpom:resolve-placeholders" match="text()">
    <xsl:sequence select="internal:evaluate(.)" />
  </xsl:template>

  <doc:doc>
    <doc:description>
      General template for all non text items; simply copies and recurses.
    </doc:description>
  </doc:doc>
  <xsl:template mode="xpom:resolve-placeholders" match="
    / | @* | element() | comment() | processing-instruction()">
    <xsl:copy>
      <xsl:apply-templates mode="#current" select="@* | node()" />
    </xsl:copy>
  </xsl:template>

  <doc:doc>
    <doc:description>
      Returns a copy of the supplied fragment, with all element text nodes
      evaluated in the context of the current POM.
    </doc:description>
    <doc:param name="fragment">The fragment to resolve</doc:param>
  </doc:doc>
  <xsl:template name="xpom:resolve-placeholders">
    <xsl:param name="fragment" />
    <xsl:param name="pom" as="element()" select="
      $fragment/ancestor-or-self::pom:project" />
    <xsl:apply-templates mode="xpom:resolve-placeholders" select="$fragment">
      <xsl:with-param name="xpom:current-pom" select="$pom" tunnel="yes" />
    </xsl:apply-templates>
  </xsl:template>

  <doc:doc>
    <doc:description>
      Returns a copy of the supplied fragment, with all element text nodes
      evaluated in the context of the current POM.
    </doc:description>
    <doc:param name="fragment">The fragment to resolve</doc:param>
  </doc:doc>
  <xsl:function name="xpom:resolve-placeholders">
    <xsl:param name="fragment" />
    <xsl:call-template name="xpom:resolve-placeholders">
      <xsl:with-param name="fragment" select="$fragment" />
    </xsl:call-template>
  </xsl:function>

  <doc:doc>
    <doc:description>
      Returns a copy of the supplied fragment, with all element text nodes
      evaluated in the context of the current POM.
    </doc:description>
    <doc:param name="fragment">The fragment to resolve</doc:param>
    <doc:param name="pom" as="element()" />
  </doc:doc>
  <xsl:function name="xpom:resolve-placeholders">
    <xsl:param name="fragment" />
    <xsl:param name="pom" as="element()" />
    <xsl:call-template name="xpom:resolve-placeholders">
      <xsl:with-param name="fragment" select="$fragment" />
      <xsl:with-param name="pom" select="$pom" />
    </xsl:call-template>
  </xsl:function>
  
</xsl:stylesheet>
