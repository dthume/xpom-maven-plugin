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
  xmlns:pp="urn:xpom:core:pretty-print-xml"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    
  exclude-result-prefixes="#all" 
  version="2.0">

  <xsl:template mode="pp:print-attribute-value" as="xs:string" match="
    @xsi:schemaLocation" >
    <xsl:variable name="indent" as="xs:string">
      <xsl:call-template name="pp:print-newline-indent">
        <xsl:with-param name="extraIndentLevel" select="1" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:value-of select="string-join(tokenize(., '\s+'), $indent)" />
  </xsl:template>
</xsl:stylesheet>