<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output 
  method="XML"
  encoding="UTF-8"
  indent="yes" 
/>
<xsl:strip-space elements="*"/>
<xsl:template match="/">
		<xsl:value-of select="DocumentationSims/label/contenu"/>
		Sims <xsl:value-of select="DocumentationSims/id"/>
		Uri <xsl:value-of select="DocumentationSims/uri"/>
</xsl:template>
<xsl:template match="DocumentationSims/Rubriques">
<xsl:comment>Début du parcours des rubriques</xsl:comment>
Rubriques:
<xsl:for-each select="//">
Rubrique:
	<!-- <xsl:template match="/label"> </xsl:template> -->
	<xsl:value-of select="/label"/> 
	<xsl:value-of select="." />
</xsl:for-each>
</xsl:template>
</xsl:stylesheet>