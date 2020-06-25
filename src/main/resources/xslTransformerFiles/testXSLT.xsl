<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" encoding="UTF-8" indent="yes" />
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
			Rubrique:
			<xsl:value-of select="idAttribute" />
			<xsl:value-of select="labelLg1" />
			<xsl:if test="codeList != ''">
				Code list :	<xsl:value-of select="codeList" />
			</xsl:if>
			<xsl:if test="rangeType != ''">
				rangeType :	<xsl:value-of select="rangeType" />
			</xsl:if>
			<xsl:if test="value != ''">
				Valeur :	<xsl:value-of select="value" />
			</xsl:if>
			<xsl:text>&#13;</xsl:text>
			<!-- <xsl:value-of select="." /> -->
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="/Documentation">
		<xsl:text>&#13;</xsl:text>
		<xsl:text>&#13;</xsl:text>
		<xsl:text>&#13;</xsl:text>
		<xsl:text>&#13;</xsl:text>
		<xsl:text>&#13;</xsl:text>
		<xsl:text>TEMPLATE 2</xsl:text>
		<xsl:text>&#13;</xsl:text>
		<xsl:text>&#13;</xsl:text>
		<xsl:value-of select="labelLg1" />
		Sims
		<xsl:value-of select="id" />
		<xsl:if test="idOperation != ''">
			Opération
			<xsl:value-of select="idOperation" />
		</xsl:if>
		<xsl:if test="idSeries != ''">
			Série
			<xsl:value-of select="idSeries" />
		</xsl:if>
		<xsl:if test="idIndicator != ''">
			Indicateur
			<xsl:value-of select="idIndicator" />
		</xsl:if>
		<xsl:for-each select="rubrics/rubrics">
			Rubrique:
			<xsl:value-of select="idAttribute" />
			<xsl:value-of select="labelLg1" />
			<xsl:if test="codeList != ''">
				Code list :	<xsl:value-of select="codeList" />
			</xsl:if>
			<xsl:if test="rangeType != ''">
				rangeType :	<xsl:value-of select="rangeType" />
			</xsl:if>
			<xsl:text>&#13;</xsl:text>
			<!-- <xsl:value-of select="." /> -->
		</xsl:for-each>
	</xsl:template>







	<!-- Ce template n'est pas parcouru pour l'instant -->
	<xsl:template match="Documentation/rubrics">
		<xsl:comment>
			Début du parcours des rubriques
		</xsl:comment>
		Rubriques:
		<xsl:for-each select="rubrics">
			Rubrique:
			<!-- <xsl:template match="/label"> </xsl:template> -->
			<xsl:value-of select="labelLg1" />
			<xsl:value-of select="idAttribute" />
			<xsl:value-of select="." />
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>