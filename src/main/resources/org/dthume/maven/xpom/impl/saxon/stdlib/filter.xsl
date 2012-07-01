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
  
  extension-element-prefixes="doc" 
  version="2.0">

  <doc:doc type="stylesheet">
    <doc:description>
      Base stylesheet for variations on the filtering transform.
    </doc:description>
  </doc:doc>

  <doc:doc>
    <doc:description>
      Simply applies templates to all children of the current node, dropping
      the current node itself.
    </doc:description>
  </doc:doc>
  <xsl:template match="/ | @* | node()">
    <xsl:apply-templates select="@* | node()" />
  </xsl:template>
  
</xsl:stylesheet>
