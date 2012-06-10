<?xml version="1.0" encoding="iso-8859-1"?>
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
  xmlns:prop="urn:xpom:core:java-properties"
  
  exclude-result-prefixes="#all"
  extension-element-prefixes="doc"
  version="2.0">

  <doc:doc>
    <doc:description>
      Utilities for working with Java property files.
    </doc:description>
  </doc:doc>

  <doc:doc>
    <doc:description>
      Serialize a property object into a string in Java property file format.
    </doc:description>
    <doc:param name="input">A property document to serialize</doc:param>
    <doc:return>A string in java property file format</doc:return>
  </doc:doc>
  <xsl:template name="prop:write-properties-string" as="text()*">
    <xsl:param name="input" as="document-node()"/>
    <xsl:text></xsl:text>
    <xsl:for-each select="$input/properties/entry">
      <xsl:value-of select="@key" />
      <xsl:text>=</xsl:text>
      <xsl:value-of select="." />
      <xsl:text>&#xA;</xsl:text>
    </xsl:for-each>
  </xsl:template>

  <doc:doc>
    <doc:description>
      Serialize a property object into a string in Java property file format.
    </doc:description>
    <doc:param name="input">A property document to serialize</doc:param>
    <doc:return>A string in java property file format</doc:return>
  </doc:doc>  
  <xsl:function name="prop:write-properties-string" as="text()*">
    <xsl:param name="input" as="document-node()"/>
    <xsl:call-template name="prop:write-properties-string">
      <xsl:with-param name="input" select="$input" />
    </xsl:call-template>
  </xsl:function>

  <doc:doc>
    <doc:description>
      Parse a property document from a string in Java property file format.
    </doc:description>
    <doc:param name="input">A string to parse</doc:param>
    <doc:return>A document in java property XML file format</doc:return>
  </doc:doc>
  <xsl:template name="prop:parse-properties-string" as="document-node()?">
    <xsl:param name="input" />
    <xsl:sequence select="internal:read-properties-string($input)" />
  </xsl:template>

  <doc:doc>
    <doc:description>
      Parse a property document from a string in Java property file format.
    </doc:description>
    <doc:param name="input">A string to parse</doc:param>
    <doc:return>A document in java property XML file format</doc:return>
  </doc:doc>
  <xsl:function name="prop:parse-properties-string" as="document-node()?">
    <xsl:param name="input" />
    <xsl:call-template name="prop:parse-properties-string">
      <xsl:with-param name="input" select="$input" />
    </xsl:call-template>
  </xsl:function>
</xsl:stylesheet>
