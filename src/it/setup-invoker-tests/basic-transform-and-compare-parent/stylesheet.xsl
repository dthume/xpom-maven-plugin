<?xml version="1.0" encoding="iso-8859-1"?>
<!--
  This stylesheet is purely a template to help us minimize configuration in
  test case POMs (i.e. so we can avoid them having to declare the xpom or
  resources plugins).  This means we have to bind both executions in the
  parent plugins section, which means they run at the parent level too.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
  <xsl:template match="/"><empty /></xsl:template>
</xsl:stylesheet>
