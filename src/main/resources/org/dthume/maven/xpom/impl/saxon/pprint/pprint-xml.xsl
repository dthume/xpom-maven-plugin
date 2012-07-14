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
  
  exclude-result-prefixes="#all"
  xpath-default-namespace="http://maven.apache.org/POM/4.0.0" 
  version="2.0">

  <!--======================= Stylesheet Parameters  =======================-->

  <xsl:param name="defaultEncoding" as="xs:string" select="'UTF-8'" />
  <xsl:param name="defaultIndent" as="xs:string" select="'  '" />
  <xsl:param name="defaultOutputTextAsCData" as="xs:boolean" select="false()" />
  <xsl:param name="defaultMaxColumns" as="xs:integer" select="80" />

  <xsl:output method="text" />

  <xsl:strip-space elements="*" />
  
  <!--====================== Newlines and Indentation ======================-->
  
  <xsl:variable name="pp:emptyElSeq" as="element()*" select="()" />
  
  <xsl:variable name="pp:newline" as="element()">
    <pp:newline />
  </xsl:variable>
  
  <xsl:variable name="pp:indent" as="element()">
    <pp:indent />
  </xsl:variable>
  
  <xsl:variable name="pp:newlineIndent" as="element()">
    <pp:newline-indent />
  </xsl:variable>
  
  <xsl:function name="pp:as-simple-block" as="element()*">
    <xsl:param name="els" as="element()*" />
    <xsl:sequence select="
      if (exists($els)) then ($els, $pp:newline) else $els" />
  </xsl:function>

  <xsl:template name="pp:print-newline" as="xs:string">
    <xsl:sequence select="'&#xA;'" />
  </xsl:template>
  
  <xsl:template name="pp:print-indent" as="xs:string">
    <xsl:param name="pp:currentIndent" tunnel="yes" as="xs:string" />
    <xsl:sequence select="$pp:currentIndent" />
  </xsl:template>
  
  <xsl:template name="pp:print-newline-indent" as="xs:string">
    <xsl:param name="pp:currentIndent" tunnel="yes" as="xs:string" />
    <xsl:param name="extraIndentLevel" as="xs:integer" select="0" />
    <xsl:variable name="indent" as="xs:string">
      <xsl:call-template name="pp:push-indent">
        <xsl:with-param name="level" select="$extraIndentLevel" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:sequence select="concat('&#xA;', $indent)" />
  </xsl:template>
  
  <xsl:template name="pp:push-indent" as="xs:string">
    <xsl:param name="pp:indentString" tunnel="yes" as="xs:string" />
    <xsl:param name="pp:currentIndent" tunnel="yes" as="xs:string" />
    <xsl:param name="level" as="xs:integer" select="1" />
    <xsl:sequence select="
      if (0 ge $level)
      then $pp:currentIndent
      else string-join(
        (
          $pp:currentIndent,
          for $i in 1 to $level return $pp:indentString
        ),
        ''
      )" />
  </xsl:template>
  
  <!--=========================== Text Formatting ===========================-->
  
  <xsl:template name="pp:escape-text-data" as="xs:string">
    <xsl:param name="text" />
    <xsl:variable name="charMap" as="element()*">
      <entry key="&amp;" value="&amp;amp;" />
      <entry key="&lt;" value="&amp;lt;" />
      <entry key="&gt;" value="&amp;gt;" />
      <entry key="&apos;" value="&amp;apos;" />
      <entry key="&quot;" value="&amp;quot;" />
    </xsl:variable>
    <xsl:variable name="escaped" as="xs:string*">
      <xsl:analyze-string regex="(&amp;|&gt;|&lt;|&apos;|&quot;)" select="
        $text">
        <xsl:matching-substring>
          <xsl:value-of select="$charMap[@key = current()]/@value" />
        </xsl:matching-substring>
        <xsl:non-matching-substring>
          <xsl:sequence select="." />
        </xsl:non-matching-substring>
      </xsl:analyze-string>
    </xsl:variable>
    <xsl:sequence select="string-join($escaped, '')" />
  </xsl:template>
  
  <xsl:template name="pp:print-inline-element-content" as="xs:string">
    <xsl:param name="pp:currentIndent" tunnel="yes" as="xs:string" />
    <xsl:param name="pp:maxColumns" tunnel="yes" as="xs:integer" />
    <xsl:param name="tokens" as="xs:string*" />
    <xsl:param name="currentLine" as="xs:string" select="''" />
    <xsl:param name="result" as="xs:string*" select="()" />
    
    <xsl:variable name="freshLine" as="xs:boolean" select="
      0 = string-length($currentLine)" />
    <xsl:choose>
      <xsl:when test="exists($tokens)">
        <xsl:variable name="nextLine" as="xs:string" select="
          concat(
            $currentLine,
            if ($freshLine) then $pp:currentIndent else ' ',
            $tokens[1]
          )" />
        <xsl:variable name="moreRoomOnLine" as="xs:boolean" select="
          $pp:maxColumns ge string-length($nextLine)" />
        <xsl:choose>
          <xsl:when test="$moreRoomOnLine">
            <xsl:call-template name="pp:print-inline-element-content">
              <xsl:with-param name="tokens" select="subsequence($tokens, 2)" />
              <xsl:with-param name="currentLine" select="$nextLine" />
              <xsl:with-param name="result" select="$result" />
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="$freshLine">
            <xsl:call-template name="pp:print-inline-element-content">
              <xsl:with-param name="tokens" select="subsequence($tokens, 2)" />
              <xsl:with-param name="result" select="($result, $nextLine)" />
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="pp:print-inline-element-content">
              <xsl:with-param name="tokens" select="$tokens" />
              <xsl:with-param name="result" select="($result, $currentLine)" />
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select=
          "string-join(
            if ($freshLine) then $result else ($result, $currentLine),
            '&#xA;'
           )" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!--========================= Attribute Handling =========================-->

  <xsl:template mode="pp:print-attribute-value" match="@*" as="xs:string">
    <xsl:call-template name="pp:escape-text-data">
      <xsl:with-param name="text" select="." />
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template mode="pp:pretty-print-xml-attributes" match="node()">
    <xsl:apply-templates mode="#current" select="@* | node()" />
  </xsl:template>
  
  <xsl:template mode="pp:pretty-print-xml-attributes" match="@*">
    <xsl:variable name="body" as="xs:string">
      <xsl:apply-templates mode="pp:print-attribute-value" select="." />
    </xsl:variable>
    <xsl:sequence select="
      string-join(
        (name(), '=&quot;', $body, '&quot;'),
        ''
      )" />
  </xsl:template>
  
  <xsl:template mode="pp:pretty-print-xml-attributes" match="*[exists(@*)]">
    <xsl:for-each select="@*">
      <xsl:apply-templates mode="#current" select="."/>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template mode="pp:pretty-print-xml-attributes" match="*[empty(@*)]"
  ></xsl:template>
  
  <!--========================= Namespace Handling =========================-->
  
  <xsl:template name="pp:namespaces-for-element" as="element()*">
    <xsl:param name="el" as="element()?" />
    <xsl:if test="exists($el)">
      <xsl:for-each select="in-scope-prefixes($el)">
        <xsl:variable name="uri" as="xs:anyURI" select="
          namespace-uri-for-prefix(., $el)" />
        <xsl:if test="'http://www.w3.org/XML/1998/namespace' != $uri">
          <ns prefix="{.}" uri="{$uri}" />
        </xsl:if>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template name="pp:declared-namespaces-for-element" as="element()*">
    <xsl:param name="el" as="element()" />
    <xsl:variable name="parentNS" as="element()*">
      <xsl:call-template name="pp:namespaces-for-element">
        <xsl:with-param name="el" select="$el/parent::*" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="thisNS" as="element()*">
      <xsl:call-template name="pp:namespaces-for-element">
        <xsl:with-param name="el" select="$el" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:for-each select="$thisNS">
      <xsl:if test="
        empty(
          $parentNS[
                @prefix = current()/@prefix
            and @uri = current()/@uri
          ]
        )">
        <xsl:sequence select="." />
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="pp:pretty-print-xml-namespaces" match="
    @* | text() | comment() | processing-instruction()"></xsl:template>
  
  <xsl:template mode="pp:pretty-print-xml-namespaces" as="xs:string*" match="
    element()">
    <xsl:variable name="declaredNS" as="element()*">
      <xsl:call-template name="pp:declared-namespaces-for-element">
        <xsl:with-param name="el" select="." />
      </xsl:call-template>
    </xsl:variable>
    <xsl:sequence select="
      for $ns in $declaredNS return string-join(
        (
          'xmlns',
          if ('' = $ns/@prefix) then '' else ':',
          $ns/@prefix,
          '=&quot;',
          $ns/@uri,
          '&quot;'
        ),
        ''
      )" />
  </xsl:template>
  
  <!--========================== Element Handling ==========================-->

  <xsl:template mode="pp:pretty-print-xml" as="xs:string*" match="
    *[not(self::pp:*) and exists(child::node())]">
    <xsl:param name="pp:maxColumns" as="xs:integer" tunnel="yes" />
    <xsl:param name="pp:printAsInlineBlock" as="xs:boolean" tunnel="yes" />
    
    <xsl:variable name="name" select="name()" />
    
    <xsl:variable name="contentIndent" as="xs:string">
      <xsl:call-template name="pp:push-indent" />
    </xsl:variable>
    
    <xsl:variable name="openElementHead">
      <xsl:if test="not($pp:printAsInlineBlock)">
        <xsl:call-template name="pp:print-newline-indent" />
      </xsl:if>
      <xsl:text>&lt;</xsl:text>
      <xsl:value-of select="$name" />
    </xsl:variable>
    
    <xsl:variable name="namespaces" as="xs:string*">
      <xsl:apply-templates mode="pp:pretty-print-xml-namespaces" select=".">
        <xsl:with-param name="pp:currentIndent" tunnel="yes" select="
          $contentIndent" />
      </xsl:apply-templates>
    </xsl:variable>
    
    <xsl:variable name="attributes" as="xs:string*">
      <xsl:apply-templates mode="pp:pretty-print-xml-attributes" select=".">
        <xsl:with-param name="pp:currentIndent" tunnel="yes" select="
          $contentIndent" />
      </xsl:apply-templates>
    </xsl:variable>
    
    <xsl:variable name="nsAndAttrs" as="xs:string*" select="
      ($namespaces, $attributes)" />
    
    <xsl:variable name="openElementTail" as="xs:string" select="
      '&gt;'" />
    
    <xsl:variable name="hasChildElements" as="xs:boolean" select="
      exists(child::*)" />
      
    <xsl:variable name="isMixedContent" as="xs:boolean" select="
      exists(child::text())" />
    
    <xsl:variable name="openElement" as="xs:string*">
      <xsl:call-template name="pp:construct-open-block-element">
        <xsl:with-param name="pp:currentIndent" tunnel="yes" select="
          $contentIndent" />
        <xsl:with-param name="input" select="$nsAndAttrs" />
        <xsl:with-param name="firstLine" select="$openElementHead" />
        <xsl:with-param name="lastLine" select="$openElementTail" />
      </xsl:call-template>
    </xsl:variable>
    
    <xsl:variable name="closeElement" as="xs:string*">
      <xsl:if test="
        $hasChildElements and not($pp:printAsInlineBlock or $isMixedContent)">
        <xsl:call-template name="pp:print-newline-indent" />
      </xsl:if>
      <xsl:text>&lt;/</xsl:text>
      <xsl:value-of select="$name" />
      <xsl:text>&gt;</xsl:text>
    </xsl:variable>

    <xsl:variable name="body" as="xs:string*">
      <xsl:apply-templates mode="pp:pretty-print-element-content" select=".">
        <xsl:with-param name="pp:currentIndent" tunnel="yes" select="
          $contentIndent" />
        <xsl:with-param name="pp:isMixedContent" tunnel="yes" select="
          $isMixedContent" />
        <xsl:with-param name="pp:printAsInlineBlock" tunnel="yes" select="
          $isMixedContent" />
      </xsl:apply-templates>
    </xsl:variable>
    
    <xsl:variable name="elementOnOneLine" as="xs:string" select="
      string-join(
        (
          $openElement,
          string-join($body, ' '),
          $closeElement
        ),
        ''
      )" />

    <xsl:choose>
      <xsl:when test="$pp:printAsInlineBlock">
        <xsl:sequence select="
          (
            string-join(($openElement, $body[1]), ''),
            subsequence($body, 2, count($body) - 2),
            string-join(($body[count($body)], $closeElement), '')
          )" />
      </xsl:when>
      <xsl:when test="
        $isMixedContent and $pp:maxColumns lt string-length($elementOnOneLine)">
        <xsl:variable name="result" as="xs:string*">
          <xsl:sequence select="$openElement" />
          <xsl:call-template name="pp:print-newline" />
          <xsl:call-template name="pp:print-inline-element-content">
            <xsl:with-param name="tokens" select="$body" />
            <xsl:with-param name="pp:currentIndent" tunnel="yes" select="
              $contentIndent" />
          </xsl:call-template>
          <xsl:call-template name="pp:print-newline-indent" />
          <xsl:sequence select="$closeElement" />
        </xsl:variable>
        <xsl:sequence select="string-join($result, '')" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$elementOnOneLine" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="pp:pretty-print-element-content" match="
    element()">
    <xsl:apply-templates mode="pp:pretty-print-xml" select="node()" />
  </xsl:template>

  <xsl:template mode="pp:pretty-print-xml" as="xs:string*" match="
    *[not(self::pp:*) and empty(child::node())]">
    <xsl:param name="pp:printAsInlineBlock" as="xs:boolean" tunnel="yes" />
    <xsl:variable name="name" select="name()" />
    <xsl:variable name="startElement">
      <xsl:if test="not($pp:printAsInlineBlock)">
        <xsl:call-template name="pp:print-newline-indent" />
      </xsl:if>
      <xsl:text>&lt;</xsl:text>
      <xsl:value-of select="$name" />
    </xsl:variable>
    <xsl:variable name="namespaces" as="xs:string*">
      <xsl:apply-templates mode="pp:pretty-print-xml-namespaces" select="." />
    </xsl:variable>
    <xsl:variable name="attributes" as="xs:string*">
      <xsl:apply-templates mode="pp:pretty-print-xml-attributes" select="." />
    </xsl:variable>
    <xsl:variable name="elementTail">
      <xsl:text>/&gt;</xsl:text>
    </xsl:variable>
    <xsl:variable name="tokens" as="xs:string*" select="
      ($startElement, $namespaces, $attributes, $elementTail)" />
    <xsl:sequence select="
      if ($pp:printAsInlineBlock)
      then $tokens
      else string-join($tokens, ' ')" />
  </xsl:template>

  <xsl:template mode="pp:pretty-print-xml" match="pp:newline">
    <xsl:call-template name="pp:print-newline" />
  </xsl:template>
  
  <xsl:template mode="pp:pretty-print-xml" match="pp:indent">
    <xsl:call-template name="pp:print-indent" />
  </xsl:template>
  
  <xsl:template mode="pp:pretty-print-xml" match="pp:newline-indent">
    <xsl:call-template name="pp:print-newline-indent" />
  </xsl:template>
  
  <xsl:template mode="pp:pretty-print-xml" match="text()" as="xs:string*">
    <xsl:param name="pp:outputTextAsCData" as="xs:boolean" tunnel="yes" />
    <xsl:param name="pp:maxColumns" as="xs:integer" tunnel="yes" />
    <xsl:param name="pp:printAsInlineBlock" as="xs:boolean" tunnel="yes" />
    <xsl:variable name="rawText" as="xs:string">
      <xsl:choose>
        <xsl:when test="$pp:outputTextAsCData">
          <xsl:text>&lt;![CDATA[</xsl:text>
          <xsl:sequence select="normalize-space()" />
          <xsl:text>]]&gt;</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="pp:escape-text-data">
            <xsl:with-param name="text" select="normalize-space()" />
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:sequence select="
      if ($pp:printAsInlineBlock)
      then tokenize($rawText, '\s+')
      else $rawText" />
  </xsl:template>
  
  <xsl:template name="pp:construct-open-block-element" as="xs:string">
    <xsl:param name="input" as="xs:string*" />
    <xsl:param name="firstLine" as="xs:string" />
    <xsl:param name="lastLine" as="xs:string" />
    <xsl:param name="pp:maxColumns" tunnel="yes" as="xs:integer" />

    <xsl:variable name="nextFirstLine" as="xs:string" select="
      concat(
        $firstLine,
        if (0 = string-length($input[1])) then '' else concat(' ', $input[1])
      )" />

    <xsl:variable name="isMoreRoomOnLine" as="xs:boolean" select="
      $pp:maxColumns gt string-length($nextFirstLine)" />

    <xsl:choose>
      <xsl:when test="$isMoreRoomOnLine and exists($input)">
        <xsl:call-template name="pp:construct-open-block-element">
          <xsl:with-param name="input" select="subsequence($input, 2)" />
          <xsl:with-param name="firstLine" select="$nextFirstLine" />
          <xsl:with-param name="lastLine" select="$lastLine" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="indent" as="xs:string">
          <xsl:call-template name="pp:print-newline-indent" />
        </xsl:variable>
        <xsl:variable name="finalFirstLine" as="xs:string" select="
          if ($isMoreRoomOnLine) then $nextFirstLine else $firstLine" />
        <xsl:variable name="finalContent" as="xs:string*" select="
          if ($isMoreRoomOnLine) then subsequence($input, 2) else $input" />
        <xsl:sequence select="
          concat(
            $finalFirstLine,
            if (empty($finalContent))
              then ''
              else string-join(('', $finalContent), $indent),
            $lastLine
          )" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="pp:pretty-print-xml" as="xs:string">
    <xsl:param name="root" />
    <xsl:param name="encoding" as="xs:string" select="$defaultEncoding" />
    <xsl:param name="indentString" as="xs:string" select="$defaultIndent" />
    <xsl:param name="maxColumns" as="xs:integer" select="$defaultMaxColumns" />
    <xsl:param name="outputTextAsCData" as="xs:boolean" select="
      $defaultOutputTextAsCData" />
    <xsl:param name="startIndent" as="xs:string" select="''" />
    <xsl:variable name="xmlDecl">
      <xsl:text>&lt;?xml version=&quot;1.0&quot; encoding=&quot;</xsl:text>
      <xsl:value-of select="$encoding" />
      <xsl:text>&quot;?&gt;</xsl:text>
    </xsl:variable>
    <xsl:variable name="docBody">
      <xsl:apply-templates mode="pp:pretty-print-xml" select="$root">
        <xsl:with-param name="pp:currentIndent" tunnel="yes" select="
          $startIndent" />
        <xsl:with-param name="pp:indentString" tunnel="yes" select="
          $indentString" />
        <xsl:with-param name="pp:printAsInlineBlock" tunnel="yes"
          select="false()" />
        <xsl:with-param name="pp:maxColumns" tunnel="yes" select="
          $maxColumns" />
        <xsl:with-param name="pp:outputTextAsCData" tunnel="yes" select="
          $outputTextAsCData" />
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:sequence select="concat($xmlDecl, $docBody)" />
  </xsl:template>

  <!--========================== Main Entry Point ==========================-->
  
  <xsl:template match="/">
    <xsl:call-template name="pp:pretty-print-xml">
      <xsl:with-param name="root" select="." />
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>
