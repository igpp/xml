<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/doc">
   <html>
   <head>
      <style>
	     .indent { margin-left: 16px; }
	  </style>
   </head>
   <body>
      <h2><xsl:value-of select="name"/>'s Family</h2>
         <xsl:for-each select="sibling">
          Sibling: <xsl:value-of select="name"/>
	 	 <div class="indent">
            <xsl:for-each select="child">
               Child: <xsl:value-of select="name"/>
            </xsl:for-each>
		  </div>
      </xsl:for-each>
   </body>
   </html>
</xsl:template>

</xsl:stylesheet> 