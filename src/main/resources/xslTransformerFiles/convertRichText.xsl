<?xml version="1.1" encoding="UTF-8" ?>

<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
	xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
	xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
	xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
	xmlns:loext="urn:org:documentfoundation:names:experimental:office:xmlns:loext:1.0"
	xmlns:officeooo="http://openoffice.org/2009/office" xmlns:html="http://www.w3.org/TR/html4"
	xmlns:msxsl="urn:schemas-microsoft-com:xslt" exclude-result-prefixes="msxsl">

	<xsl:output method="xml" encoding="UTF-8" indent="yes" />

	<!-- Identity template : copy all text nodes, elements and attributes -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>


	<xsl:template match="RichText">
		<!-- <text:p text:style-name="Text_20_body"> -->
		<!-- <xsl:value-of select="'coucou richtext'" /> -->
		<!-- </text:p> -->
<!-- 		<xsl:value-of select="node()" disable-output-escaping="yes" /> -->
		<xsl:apply-templates select="node()" />
<!-- 		<xsl:value-of select="node()"/> -->
	</xsl:template>

	<xsl:template match="p">
		<text:p>
			<text:p text:style-name="Text_20_body">
				<xsl:value-of select="'coucou richtext'" />
			</text:p>

			<xsl:attribute name="text:style-name">
                <xsl:choose>
                    <xsl:when test="preceding-sibling::p">
                        <xsl:value-of select="Text_20_body" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="P1" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
			<xsl:apply-templates select="node()" />
		</text:p>
	</xsl:template>

	<xsl:template match="strong">
		<text:text>
			<xsl:attribute name="text:b">
            </xsl:attribute>
			<xsl:apply-templates select="node()" />
		</text:text>
	</xsl:template>

	<xsl:template match="em">
		<text:text>
			<xsl:attribute name="text:i">
            </xsl:attribute>
			<xsl:apply-templates select="node()" />
		</text:text>
	</xsl:template>


	<xsl:template match="text()">
		<xsl:value-of select="replace(.,'''','&amp;apos;')" />
	</xsl:template>


</xsl:stylesheet>
