<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="3.0">

    <xsl:template name="format-organization">
        <xsl:param name="simsRubrics" as="node()*"/>
        <xsl:param name="rubric-element" as="xs:string"/>
        <xsl:variable name="original-text" select="$simsRubrics//*[local-name()=$rubric-element]"/>
        <xsl:if test="$original-text != ''">
            <xsl:value-of select="$original-text"/>
            <xsl:if test="$rubric-element = 'labelLg1'">
                <xsl:variable name="stamp" select="$simsRubrics//value/value"/>
                <xsl:variable name="altLabel" select="$organizations//item[id=$stamp]/altLabel"/>
                <xsl:if test="$altLabel != ''">
                    <xsl:value-of select="concat(' - ', $altLabel)"/>
                </xsl:if>
            </xsl:if>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
