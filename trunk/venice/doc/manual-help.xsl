<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:redirect2="org.apache.xalan.xslt.extensions.Redirect"
		xmlns:redirect="http://xml.apache.org/xalan/redirect"
                extension-element-prefixes="redirect"
                version="1.0">  
  <xsl:output method="html" indent="yes"/>

  <xsl:param name='destdir' select='"."'/>

  <xsl:template match="/">
    <html>
      <body bgcolor="#FFFFFF">
      <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="document">
    <xsl:variable name='filename' select='concat($destdir,"/",@name,".html")'/>
    <xsl:result-document href='{$filename}'>
      <h2>
	<xsl:apply-templates select='@name'/>
      </h2>
      <xsl:apply-templates/>
    </xsl:result-document>    

    <!--
    <redirect:open file="{@name}.html"/>
    <redirect:write file="{@name}.html">
      <h2><xsl:value-of select="@name"/></h2>
      <xsl:apply-templates/>
    </redirect:write>
    <redirect:close file="{@name}.html"/>
    -->    
  </xsl:template>

  <xsl:template match="emphasis">
    <b><xsl:apply-templates/></b>
  </xsl:template>

  <xsl:template match="highlight">
    <i><xsl:apply-templates/></i>
  </xsl:template>

  <xsl:template match="help">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="text">
  </xsl:template>

  <xsl:template match="link">
    <a href="{@to}.html"><xsl:apply-templates/></a>
  </xsl:template>

  <xsl:template match="code">
    <code><xsl:apply-templates/></code>
  </xsl:template>

  <xsl:template match="codeblock">
    <pre><xsl:apply-templates/></pre>
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
    <xsl:variable name='filename' select='concat($destdir,"/",@name,".html")'/>
    <xsl:result-document href='{$filename}'>
      <html>
	<body>
	  <h2>
	    <xsl:apply-templates select='@name'/>
	  </h2>
	  <xsl:apply-templates/>
	</body>
      </html>
    </xsl:result-document>
    
    <!--
    <redirect:open file="{@name}.html"/>
    <redirect:write file="{@name}.html">
      <html><body>
      <h2><xsl:value-of select="@name"/></h2>
      <xsl:apply-templates/>
      </body></html>
    </redirect:write>
    <redirect:close file="{@name}.html"/>
    -->
  </xsl:template>

  <xsl:template match="section">
    <h3><xsl:value-of select="@name"/></h3>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="subsection">
    <h3><xsl:value-of select="@name"/></h3>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match='text()'>
    <xsl:value-of select='.'/>
  </xsl:template>

</xsl:stylesheet>
