<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">  
  <xsl:output method="html" indent="yes"/>

  <xsl:template match="/">
    <html>

    <head>
      <title>Merchant of Venice</title>
    </head>
    
    <body bgcolor="#ffffff">
      <xsl:apply-templates/>
    </body>
  </html>
  </xsl:template>

  <xsl:template match="document">
    <h2>Merchant of Venice Manual</h2>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="emphasis">
    <b><xsl:apply-templates/></b>
  </xsl:template>

  <xsl:template match="highlight">
    <i><xsl:apply-templates/></i>
  </xsl:template>

  <xsl:template match="help">
  </xsl:template>

  <xsl:template match="text">
  </xsl:template>

  <xsl:template match="link">
    <a href="#{@to}"><xsl:apply-templates/></a>
  </xsl:template>

  <xsl:template match="code">
    <code><xsl:apply-templates/></code>
  </xsl:template>

  <xsl:template match="list">
    <ul><xsl:apply-templates/></ul>
  </xsl:template>

  <xsl:template match="item">
    <li><xsl:apply-templates/></li>
  </xsl:template>

  <xsl:template match="para">
    <p><xsl:apply-templates/></p>
  </xsl:template>

  <xsl:template match="chapter">
    <h2>
      <a name="{@name}"/>
      <xsl:number level="multiple" format="1 "/>
      <xsl:value-of select="@name"/>
   </h2>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="section">
    <h3>
      <xsl:number level="multiple" count="chapter|section" format="1 "/>
      <xsl:value-of select="@name"/>
    </h3>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="subsection">
    <h4>
      <xsl:number level="multiple" 
                  count="chapter|section|subsection" 
                  format="1 "/>
      <xsl:value-of select="@name"/>
    </h4>
    <xsl:apply-templates/>
  </xsl:template>

</xsl:stylesheet>