<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    version="3.0">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
    
    <xd:doc>
        <xd:desc>Template to remove namespaces from the DDI file : copies the elements with their local-name instead of their name wit namespace</xd:desc>
    </xd:doc>
    <xsl:template match="*">
        <xsl:element name="{local-name()}">
            <xsl:for-each select="@*">
                <xsl:attribute name="{local-name()}">
                    <xsl:value-of select="."/>
                </xsl:attribute>
            </xsl:for-each>
            <xsl:apply-templates />
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>