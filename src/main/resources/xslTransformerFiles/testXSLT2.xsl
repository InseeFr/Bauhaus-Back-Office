<?xml version="1.1" encoding="UTF-8" ?>

<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
	xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
	xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
	xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
	xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
	xmlns:loext="urn:org:documentfoundation:names:experimental:office:xmlns:loext:1.0"
	xmlns:officeooo="http://openoffice.org/2009/office">

	<xsl:output method="xml" encoding="UTF-8" indent="yes" />



	<!--Sims target -->
	<xsl:param name="tempFile" />
	<xsl:param name="fileTarget" select="document($tempFile)" />
	<xsl:param name="targetType" />
	<!--Sims target's series -->
	<xsl:param name="accessoryTempFile" />
	<xsl:param name="fileSeries" select="document($accessoryTempFile)" />
	<!--MSD -->
	<xsl:param name="msd" />
	<xsl:param name="fileMsd" select="document($msd)" />

	<xsl:strip-space elements="*" />

	<xsl:variable name="rootVar" select="fn:root(.)" as="node()" />

	<!-- <xsl:variable name="targetVar" as="node()"> -->
	<!-- <xsl:choose> -->
	<!-- <xsl:when test="$fileTarget!=''"> -->
	<!-- <xsl:copy-of select="$fileTarget"/> -->
	<!-- </xsl:when> -->
	<!-- <xsl:otherwise> -->
	<!-- <xsl:copy-of select="document(replace($tempFile,'C:\Users\MMAJR1\AppData\Local\',''))"/> -->
	<!-- </xsl:otherwise> -->
	<!-- </xsl:choose> -->
	<!-- </xsl:variable> -->

	<xsl:template match="/">
		<office:document office:version="1.2"
			office:mimetype="application/vnd.oasis.opendocument.text"
			xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
			xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
			xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
			xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
			xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0">
			<office:body>
				<office:text text:use-soft-page-breaks="true">
					<text:sequence-decls>
						<text:sequence-decl text:name="Illustration"
							text:display-outline-level="0" />
						<text:sequence-decl text:name="Table"
							text:display-outline-level="0" />
						<text:sequence-decl text:name="Text"
							text:display-outline-level="0" />
						<text:sequence-decl text:name="Drawing"
							text:display-outline-level="0" />
					</text:sequence-decls>


					<text:p>
						<xsl:value-of select="'xsl:version: '" />
						<xsl:value-of select="system-property('xsl:version')" />
					</text:p>

					<!-- Header -->
					<xsl:call-template name="header"></xsl:call-template>

					<text:p>
						Sims
						<xsl:value-of select="Documentation/id" />
					</text:p>
					<text:p text:style-name="P1">
						<xsl:text>&#13;</xsl:text>
						<xsl:if test="Documentation/idOperation != ''">
							Opération
							<xsl:value-of select="Documentation/idOperation" />
						</xsl:if>
						<xsl:if test="Documentation/idSeries != ''">
							Série
							<xsl:value-of select="Documentation/idSeries" />
						</xsl:if>
						<xsl:if test="Documentation/idIndicator != ''">
							Indicateur
							<xsl:value-of select="Documentation/idIndicator" />
						</xsl:if>
					</text:p>


				</office:text>
			</office:body>
		</office:document>
	</xsl:template>

	<xsl:template name="header">
		<text:p>
			<xsl:value-of select="'in Template Header '" />
					</text:p>
					<text:p>
			<xsl:value-of select="'MSD Path : '" />
			<xsl:value-of select="$msd" />
					</text:p>
					<text:p>
			<xsl:value-of select="'MSD: '" />
			<xsl:value-of select="$fileMsd/MSD" />
					</text:p>
					<text:p>
			<xsl:value-of select="'Target : '" />
			<xsl:value-of select="$tempFile" />
			<xsl:value-of select="$fileTarget" />
					</text:p>
					<text:p>
			<xsl:value-of select="$targetType" />
			<xsl:value-of select="document($tempFile)/Operation/prefLabelLg1" />
		</text:p>
	</xsl:template>

</xsl:stylesheet>