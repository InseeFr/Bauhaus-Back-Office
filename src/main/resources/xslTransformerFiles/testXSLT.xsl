<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes"/>
	<xsl:strip-space elements="*" />
	<xsl:template match="/">
		<xsl:text>&#13;</xsl:text>
		<xsl:value-of select="Documentation/labelLg1" />
		Sims
		<xsl:value-of select="Documentation/id" />
		<xsl:if test="Documentation/idOperation != ''">
			Opération 	<xsl:value-of select="Documentation/idOperation" />
		</xsl:if>
		<xsl:if test="Documentation/idSeries != ''">
			Série	<xsl:value-of select="Documentation/idSeries" />
		</xsl:if>
		<xsl:if test="Documentation/idIndicator != ''">
			Indicateur	<xsl:value-of select="Documentation/idIndicator" />
		</xsl:if>
		<xsl:for-each select="Documentation/rubrics/rubrics">
			 <xsl:sort data-type="number" select="substring-before(concat(substring-after(idAttribute,'.'),'.'),'.')" order="ascending"/> 
			 <xsl:sort data-type="number" select="substring-after(substring-after(idAttribute,'.'),'.')" order="ascending"/>
			 Rubrique:
			<xsl:value-of select="idAttribute" />
			<xsl:value-of select="labelLg1" />
			<xsl:if test="codeList != ''">
				Code list :	<xsl:value-of select="codeList" />
			</xsl:if>
			<xsl:if test="rangeType != ''">
				RangeType :	<xsl:value-of select="rangeType" />
			</xsl:if>
			<xsl:if test="value != ''">
				Valeur :	<xsl:value-of select="value" />
			</xsl:if>
			<xsl:text>&#13;</xsl:text>
			<!-- <xsl:value-of select="." /> -->
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>