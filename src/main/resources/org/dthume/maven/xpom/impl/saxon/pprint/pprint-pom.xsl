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
    
  xpath-default-namespace="http://maven.apache.org/POM/4.0.0"
  exclude-result-prefixes="#all" 
  version="2.0">

  <xsl:import href="pprint-xml.xsl" />
  <xsl:import href="pprint-xsi-schemaLocation.xsl" />

  <xsl:template mode="pp:pretty-print-element-content" match="project">
    <xsl:apply-templates mode="pp:pretty-print-xml" select="
      pp:as-simple-block((modelVersion, parent)),
      pp:as-simple-block((groupId, artifactId, version, packaging)),
      pp:as-simple-block((name, description, url, inceptionYear)),
      pp:as-simple-block((licenses, organization, developers, contributors)),
      pp:as-simple-block((issueManagement, ciManagement, mailingLists, scm)),
      pp:as-simple-block((distributionManagement)),
      pp:as-simple-block((properties)),
      pp:as-simple-block((dependencyManagement, dependencies)),
      pp:as-simple-block((repositories, pluginRepositories)),
      pp:as-simple-block((modules, build, reporting)),
      profiles" />
  </xsl:template>
  
  <xsl:template mode="pp:pretty-print-element-content" match="dependency">
    <xsl:apply-templates mode="pp:pretty-print-xml" select="
      groupId, artifactId, version, type,
      scope, optional, exclusions" />
  </xsl:template>
  
  <xsl:template mode="pp:pretty-print-element-content" match="extension">
    <xsl:apply-templates mode="pp:pretty-print-xml" select="
      groupId, artifactId, version" />
  </xsl:template>
  
  <xsl:template mode="pp:pretty-print-element-content" match="plugin">
    <xsl:apply-templates mode="pp:pretty-print-xml" select="
      groupId, artifactId, version,
      extensions, inherited,
      configuration, executions, dependencies" />
  </xsl:template>
</xsl:stylesheet>