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
    <xsl:sequence select="document($coordinates)" />
  </xsl:function>

  <doc:doc>
    <doc:description>
      Resolve and return the POM for the given coordinates. 
    </doc:description>
    <doc:param name="coordinates">The coordinates to resolve </doc:param>
  </doc:doc>
  <xsl:function name="xpom:effective-pom" as="document-node()?">
    <xsl:param name="coordinates" as="xs:string" />
    <xsl:sequence select="document(concat($coordinates, '?effective=true'))" />
  </xsl:function>

  <!--====================== Coordinate Extraction =======================-->

  <doc:doc type="mode" for="xpom:extract-coordinates">
    <doc:description>
      Extract coordinate details for various types of artifacts defined in the
      POM.
    </doc:description>
  </doc:doc>

  <doc:doc>
    <doc:description>
      Extract project coordinates, taking into account defaults, and parent
      POM information
    </doc:description>
  </doc:doc>
  <xsl:template mode="xpom:extract-coordinates" as="element()" match="
    pom:project">
    <artifact>
      <groupId>
        <xsl:value-of select="(pom:groupId, pom:parent/pom:groupId)[1]" />
      </groupId>
      <artifactId>
        <xsl:value-of select="pom:artifactId" />
      </artifactId>
      <type>
        <xsl:value-of select="(pom:packaging, 'jar')[1]" />
      </type>
      <version>
        <xsl:value-of select="(pom:version, pom:parent/pom:version)[1]" />
      </version>
    </artifact>
  </xsl:template>

  <doc:doc>
    <doc:description>
      Extract project parent coordinates.
    </doc:description>
  </doc:doc>  
  <xsl:template mode="xpom:extract-coordinates" as="element()" match="
    pom:parent">
    <artifact>
      <groupId>
        <xsl:value-of select="pom:groupId" />
      </groupId>
      <artifactId><xsl:value-of select="pom:artifactId" /></artifactId>
      <type>pom</type>
      <version>
        <xsl:value-of select="pom:version" />
      </version>
    </artifact>    
  </xsl:template>

  <doc:doc>
    <doc:description>
      Extract project plugin coordinates.
    </doc:description>
  </doc:doc>  
  <xsl:template mode="xpom:extract-coordinates" as="element()" match="
    pom:plugin">
    <artifact>
      <groupId><xsl:value-of select="pom:groupId" /></groupId>
      <artifactId><xsl:value-of select="pom:artifactId" /></artifactId>
      <type>jar</type>
      <version><xsl:value-of select="pom:version" /></version>
    </artifact>    
  </xsl:template>

  <doc:doc>
    <doc:description>
      Extract project extension coordinates.
    </doc:description>
  </doc:doc>
  <xsl:template mode="xpom:extract-coordinates" as="element()" match="
    pom:extension">
    <artifact>
      <groupId><xsl:value-of select="pom:groupId" /></groupId>
      <artifactId><xsl:value-of select="pom:artifactId" /></artifactId>
      <type>jar</type>
      <xsl:if test="pom:classifier">
        <classifier><xsl:value-of select="pom:classifier" /></classifier>
      </xsl:if>
      <version><xsl:value-of select="pom:version" /></version>
    </artifact>    
  </xsl:template>
  
  <doc:doc>
    <doc:description>
      Extract project dependency coordinates.
    </doc:description>
  </doc:doc>
  <xsl:template mode="xpom:extract-coordinates" as="element()" match="
    pom:dependency">
    <artifact>
      <groupId><xsl:value-of select="pom:groupId" /></groupId>
      <artifactId><xsl:value-of select="pom:artifactId" /></artifactId>
      <type>jar</type>
      <xsl:if test="pom:classifier">
        <classifier><xsl:value-of select="pom:classifier" /></classifier>
      </xsl:if>
      <version><xsl:value-of select="pom:version" /></version>
    </artifact>
  </xsl:template>

  <doc:doc>
    <doc:description>
      Extract coordinate details for various types of artifacts defined in the
      POM.
    </doc:description>
    <doc:param name="el">The item to extract coordinates from</doc:param>
  </doc:doc>
  <xsl:template name="xpom:extract-coordinates" as="element()">
    <xsl:param name="el" />
    <xsl:apply-templates mode="xpom:extract-coordinates" select="$el" />
  </xsl:template>

  <doc:doc>
    <doc:description>
      Extract coordinate details for various types of artifacts defined in the
      POM.
    </doc:description>
    <doc:param name="el">The item to extract coordinates from</doc:param>
  </doc:doc>
  <xsl:function name="xpom:extract-coordinates" as="element()">
    <xsl:param name="el" />
    <xsl:call-template name="xpom:extract-coordinates">
      <xsl:with-param name="el" select="$el" />
    </xsl:call-template>
  </xsl:function>

  <doc:doc>
    <doc:description>
      Convert a fully specified set of coordinate details (as returned by
      xpom:extract-coordinates) into an xpom URI suitable for passing to the
      document function.
    </doc:description>
    <doc:param name="el">The coordinates to parse</doc:param>
  </doc:doc>
  <xsl:template name="xpom:to-artifact-URI" as="xs:anyURI">
    <xsl:param name="el" as="element()" />
    <xsl:value-of select="
      xs:anyURI(
        concat(
          'xpom://',
          string-join(
            (
              $el/groupId,
              $el/artifactId,
              $el/type,
              ($el/classifier, 'no;classifier')[1],
              $el/version
            ),
            '/'
          )
        )
      )" />
  </xsl:template>
  
  <doc:doc>
    <doc:description>
      Convert a fully specified set of coordinate details (as returned by
      xpom:extract-coordinates) into an xpom URI suitable for passing to the
      document function.
    </doc:description>
    <doc:param name="el">The coordinates to parse</doc:param>
  </doc:doc>
  <xsl:function name="xpom:to-artifact-URI" as="xs:anyURI">
    <xsl:param name="el" as="element()" />
    <xsl:call-template name="xpom:to-artifact-URI">
      <xsl:with-param name="el" select="$el" />
    </xsl:call-template>
  </xsl:function>
  
  <doc:doc>
    <doc:description>
      Extract the coordinates of various types of items in the POM, and
      convert them into string form; equivalent to
      xpom:to-artifact-URI(xpom:extract-coordinates($el)).
    </doc:description>
    <doc:param name="el">The item to extract a coordinate URI from</doc:param>
  </doc:doc>
  <xsl:template name="xpom:extract-artifact-URI" as="xs:anyURI">
    <xsl:param name="el" />
    <xsl:call-template name="xpom:to-artifact-URI">
      <xsl:with-param name="el" as="element()">
        <xsl:call-template name="xpom:extract-coordinates">
          <xsl:with-param name="el" select="$el" />
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <doc:doc>
    <doc:description>
      Extract the coordinates of various types of items in the POM, and
      convert them into string form; equivalent to
      xpom:to-artifact-URI(xpom:extract-coordinates($el)).
    </doc:description>
    <doc:param name="el">The item to extract a coordinate URI from</doc:param>
  </doc:doc>
  <xsl:function name="xpom:extract-artifact-URI" as="xs:anyURI">
    <xsl:param name="el" />
    <xsl:call-template name="xpom:extract-artifact-URI">
      <xsl:with-param name="el" select="$el" />
    </xsl:call-template>
  </xsl:function>

  <doc:doc>
    <doc:description>
      Convert a fully specified set of coordinate details (as returned by
      xpom:extract-coordinates) into string format, suitable for passing to
      xpom:resolve-pom.
    </doc:description>
    <doc:param name="el">The coordinates to stringify</doc:param>
  </doc:doc>
  <xsl:template name="xpom:to-coordinate-string" as="xs:string">
    <xsl:param name="el" />
    <xsl:value-of select="
      string-join(
        (
          $el/groupId,
          $el/artifactId,
          $el/type,
          ($el/classifier, 'no;classifier')[1],
          $el/version
        ),
        ':'
      )" />
  </xsl:template>

  <doc:doc>
    <doc:description>
      Convert a fully specified set of coordinate details (as returned by
      xpom:extract-coordinates) into string format, suitable for passing to
      xpom:resolve-pom.
    </doc:description>
    <doc:param name="el">The coordinates to stringify</doc:param>
  </doc:doc>
  <xsl:function name="xpom:to-coordinate-string" as="xs:string">
    <xsl:param name="el" />
    <xsl:call-template name="xpom:to-coordinate-string">
      <xsl:with-param name="el" select="$el" />
    </xsl:call-template>
  </xsl:function>

  <doc:doc>
    <doc:description>
      Extract the coordinates of various types of items in the POM, and
      convert them into string form; equivalent to
      xpom:to-coordinate-string(xpom:extract-coordinates($el)).
    </doc:description>
    <doc:param name="el">The item to extract string coordinates from</doc:param>
  </doc:doc>
  <xsl:template name="xpom:extract-coordinate-string" as="xs:string">
    <xsl:param name="el" />
    <xsl:call-template name="xpom:to-coordinate-string">
      <xsl:with-param name="el">
        <xsl:call-template name="xpom:extract-coordinates">
          <xsl:with-param name="el" select="$el" />
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <doc:doc>
    <doc:description>
      Extract the coordinates of various types of items in the POM, and
      convert them into string form; equivalent to
      xpom:to-coordinate-string(xpom:extract-coordinates($el)).
    </doc:description>
    <doc:param name="el">The item to extract string coordinates from</doc:param>
  </doc:doc>
  <xsl:function name="xpom:extract-coordinate-string" as="xs:string">
    <xsl:param name="el" />
    <xsl:call-template name="xpom:extract-coordinate-string">
      <xsl:with-param name="el" select="$el" />
    </xsl:call-template>
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
