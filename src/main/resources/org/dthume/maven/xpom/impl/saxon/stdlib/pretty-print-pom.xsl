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
  xmlns:xpom="urn:xpom:core"
  
  exclude-result-prefixes="#all"
  xpath-default-namespace="http://maven.apache.org/POM/4.0.0" 
  version="2.0">

  <xsl:output method="xml" indent="yes"/>

  <xsl:strip-space elements="*" />

  <xsl:template match="@* | element() | comment() | processing-instruction()">
    <xsl:copy>
      <xsl:apply-templates mode="#current" select="@* | node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="text()">
    <xsl:value-of select="normalize-space()" /> 
  </xsl:template>
  
  <xsl:template match="/project">
    <xsl:copy>
      <xsl:apply-templates mode="#current" select="
        @*,
        modelVersion,
        parent,
        groupId, artifactId, version, packaging,
        name, description, url, inceptionYear,
        licenses, organization, developers, contributors,
        issueManagement, ciManagement, mailingLists, scm,
        distributionManagement,
        properties,
        dependencyManagement, dependencies,
        repositories, pluginRepositories,
        modules, build, reporting,
        profiles" />
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
