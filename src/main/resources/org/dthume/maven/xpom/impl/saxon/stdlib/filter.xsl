<?xml version="1.0" encoding="utf-8"?>
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
