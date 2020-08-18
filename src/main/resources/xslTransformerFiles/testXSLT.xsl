<?xml version="1.1" encoding="UTF-8" ?>

<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
	xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
	xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
	xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
	xmlns:fn="http://www.w3.org/2005/xpath-functions" 
	xmlns:officeooo="http://openoffice.org/2009/office">
	<!-- <xsl:include href="../xslTransformerFiles/office-styles.xsl" /> -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes" />
	<!-- omit-xml-declaration="yes" -->
	<xsl:param name="tempFile" />
	<xsl:param name="fileTarget" select="document($tempFile)" />
	<xsl:param name="msd" />
	<xsl:param name="fileMsd" select="document($msd)" />
	<xsl:param name="targetType" />

	<xsl:strip-space elements="*" />

	<xsl:variable name="rootVar" select="fn:root(.)" as="node()" />

	<xsl:template match="/">
		<office:document office:version="1.2"
			office:mimetype="application/vnd.oasis.opendocument.text"
			xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
			xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
			xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
			xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
			xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0">

			<!-- <office:automatic-styles> -->
			<!-- <xsl:copy-of select="bauhaus:Office-styles()" /> -->
			<!-- <xsl:function name="bauhaus:Office-styles"> -->
			<!-- <style:style style:name="Standard" style:family="paragraph" -->
			<!-- style:class="text" /> -->
			<!-- </xsl:function> -->
			<!-- </office:automatic-styles> -->

			<office:automatic-styles>

				<style:style style:name="framedCell" style:family="table-cell">
					<style:table-cell-properties
						fo:wrap-option="wrap" style:shrink-to-fit="false"
						style:vertical-align="top" fo:border="2pt solid #234ca5"
						fo:padding="0cm" />
				</style:style>
				<style:style style:name="P1" style:parent-style-name="Standard"
					style:family="paragraph">
					<style:paragraph-properties
						fo:text-align="left" fo:margin-top="1cm"
						style:justify-single-word="false" />
					<style:text-properties fo:font-size="12pt"
						fo:font-weight="normal" fo:color="black" />
				</style:style>
				<style:style style:name="HeaderFr" style:family="paragraph"
					style:class="chapter">
					<style:paragraph-properties
						fo:text-align="center" fo:margin-top="1cm"
						style:justify-single-word="false" fo:background-color="#234ca5"
						font-family="Arial" />
					<style:text-properties fo:font-size="24pt"
						fo:font-weight="bold" fo:color="#ffffff" />
				</style:style>
				<style:style style:name="HeaderEn" style:family="paragraph"
					style:class="chapter">
					<style:paragraph-properties
						fo:text-align="center" fo:margin-top="1cm"
						style:justify-single-word="false" fo:background-color="#234ca5" />
					<style:text-properties fo:font-size="20pt"
						fo:font-weight="bold" fo:color="#ffffff" />
				</style:style>

				<!-- <style:style style:name="HeaderEn" style:family="paragraph" -->
				<!-- style:parent-style-name="Heading_20_1"> -->
				<!-- <style:paragraph-properties -->
				<!-- style:vertical-align="middle" style:justify-single-word="false" -->
				<!-- fo:text-align="center" style:auto-text-indent="false" -->
				<!-- fo:text-indent="0cm" fo:margin-bottom="0cm" fo:margin-top="0cm" -->
				<!-- fo:margin-right="0cm" fo:margin-left="0cm" -->
				<!-- style:line-height-at-least="0.025cm" /> -->
				<!-- <style:text-properties fo:background-color="#234ca5" -->
				<!-- style:text-rotation-scale="line-height" style:text-rotation-angle="0" -->
				<!-- style:font-size-complex="20pt" style:font-size-asian="20pt" -->
				<!-- fo:font-weight="bold" fo:font-size="20pt" style:font-name="Arial" -->
				<!-- fo:color="#ffffff" /> -->
				<!-- </style:style> -->

				<!-- <style:style style:name="HeaderFr" style:family="paragraph" -->
				<!-- style:parent-style-name="Heading_20_1"> -->
				<!-- <style:paragraph-properties -->
				<!-- style:vertical-align="middle" style:justify-single-word="false" -->
				<!-- fo:text-align="center" style:auto-text-indent="false" -->
				<!-- fo:text-indent="0cm" fo:margin-bottom="0cm" fo:margin-top="0cm" -->
				<!-- fo:margin-right="0cm" fo:margin-left="0cm" -->
				<!-- style:line-height-at-least="0.025cm" /> -->
				<!-- <style:text-properties fo:background-color="#234ca5" -->
				<!-- style:text-rotation-scale="line-height" style:text-rotation-angle="0" -->
				<!-- style:font-size-complex="20pt" style:font-size-asian="20pt" -->
				<!-- fo:font-size="20pt" style:font-name="Arial" fo:color="#ffffff" /> -->
				<!-- </style:style> -->

				<style:style style:name="Tableau1"
					style:master-page-name="master_5f_0" style:family="table">
					<style:table-properties style:page-number="auto"
						style:width="20.98cm" fo:border-bottom-style="2pt solid #234ca5"/>
				</style:style>
				<style:style style:name="T2" style:family="text">
					<style:text-properties
						style:text-rotation-scale="line-height" style:text-rotation-angle="0"
						style:font-size-complex="14pt" style:font-size-asian="14pt"
						fo:font-weight="bold" fo:font-size="14pt" style:font-name="Arial"
						fo:color="#234ca5" />
				</style:style>
				<style:style style:name="T2En" style:family="text">
					<style:text-properties
						style:text-rotation-scale="line-height" style:text-rotation-angle="0"
						style:font-size-complex="12pt" style:font-size-asian="12pt"
						fo:font-weight="bold" fo:font-size="12pt" style:font-name="Arial"
						fo:color="#234ca5" />
				</style:style>
				<style:style style:name="TitleFr" style:family="paragraph"
					style:class="chapter">
					<style:paragraph-properties
						fo:text-align="center" fo:margin-top="1cm"
						style:justify-single-word="false" />
					<style:text-properties fo:font-size="20pt"
						fo:font-weight="bold" fo:color="blue" />
				</style:style>
				<style:style style:name="TitleEn" style:family="paragraph"
					style:class="chapter">
					<style:paragraph-properties
						fo:text-align="center" fo:margin-top="1cm"
						style:justify-single-word="false" />
					<style:text-properties fo:font-size="16pt"
						fo:font-weight="bold" fo:color="blue" />
				</style:style>
				<style:style style:name="Rubric" style:family="paragraph"
					style:class="chapter">
					<style:paragraph-properties
						fo:text-align="left" fo:margin-top="1cm"
						style:justify-single-word="false" />
					<style:text-properties fo:font-size="12pt"
						fo:font-weight="normal" fo:color="black" />
				</style:style>
				<style:style style:name="RubricHead" style:family="text">
					<style:paragraph-properties
						fo:text-align="left" fo:margin-top="1cm"
						style:justify-single-word="false" />
					<!-- <style:text-properties fo:font-size="14pt" -->
					<!-- fo:font-weight="bold" fo:color="blue" /> -->
					<style:text-properties officeooo:rsid="0015b432"
						style:font-weight-complex="bold" style:font-size-complex="10pt"
						style:font-weight-asian="bold" style:font-size-asian="10pt"
						fo:font-weight="bold" fo:font-size="10pt" style:font-name="Arial1"
						style:text-underline-color="font-color"
						style:text-underline-width="auto" style:text-underline-style="solid"
						text-decoration="underline" />
				</style:style>
				<style:style style:name="RubricItem" style:family="text">
					<style:paragraph-properties
						fo:text-align="left" style:justify-single-word="false" />
					<style:text-properties fo:font-size="10pt"
						fo:font-weight="normal" fo:color="black" />
				</style:style>
				<style:style style:name="Field" style:family="paragraph"
					style:class="chapter">
					<style:paragraph-properties
						fo:text-align="left" style:justify-single-word="false" />
					<style:text-properties fo:font-size="12pt"
						fo:font-weight="bold" fo:color="black" text-decoration="underline" />
				</style:style>

			</office:automatic-styles>

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

					<table:table table:name="Tableau1" table:style-name="Tableau1">
						<table:table-row>
							<!-- <table:table-cell table:style-name="Tableau1" -->
							<!-- office:value-type="string"> -->
							<!-- <text:p text:style-name="Standard" /> -->
							<!-- </table:table-cell> -->
							<table:table-cell>
								<!-- Header -->
								<text:p text:style-name="HeaderFr">
									<xsl:value-of select="$fileTarget//prefLabelLg1" />
								</text:p>
							</table:table-cell>
						</table:table-row>
						<table:table-row>
							<table:table-cell>
								<text:p text:style-name="HeaderEn">
									<xsl:value-of select="$fileTarget//prefLabelLg2" />
								</text:p>
							</table:table-cell>
						</table:table-row>

					</table:table>

					<!-- Title -->
					<xsl:choose>
						<xsl:when test="$targetType='SERIES'">
							<text:p text:style-name="T2">
								Informations sur la série:
								<xsl:value-of select="$fileTarget//prefLabelLg1" />
							</text:p>
							<text:p text:style-name="T2En">
								Informations about the series:
								<xsl:value-of select="$fileTarget//prefLabelLg2" />
							</text:p>
							<xsl:call-template name="series"></xsl:call-template>
						</xsl:when>
						<xsl:when test="$targetType='OPERATION'">
							<xsl:call-template name="seriesForOperation"></xsl:call-template>
							<text:p text:style-name="T2">
								Informations sur l'opération:
								<xsl:value-of select="$fileTarget//prefLabelLg1" />
							</text:p>
							<text:p text:style-name="T2En">
								Informations about the operation:
								<xsl:value-of select="$fileTarget//prefLabelLg2" />
							</text:p>
							<xsl:call-template name="operation"></xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<text:p text:style-name="T2">
								Informations sur l'indicateur:
								<xsl:value-of select="$fileTarget//prefLabelLg1" />
							</text:p>
							<text:p text:style-name="T2En">
								Informations about the indicator:
								<xsl:value-of select="$fileTarget//prefLabelLg2" />
							</text:p>
							<xsl:call-template name="indicator"></xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>

					<!-- Sims -->

					<text:p text:style-name="T2">
						Informations sur le Sims
						<xsl:value-of select="Documentation/labelLg1" />
					</text:p>

					<text:p text:style-name="P1">
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




					<xsl:for-each select="Documentation/rubrics/rubrics">
						<xsl:sort data-type="number"
							select="substring-before(concat(substring-after(idAttribute,'.'),'.'),'.')"
							order="ascending" />
						<xsl:sort data-type="number"
							select="substring-after(substring-after(idAttribute,'.'),'.')"
							order="ascending" />

						<text:p text:style-name="RubricHead">
							Rubrique:
							<xsl:value-of select="idAttribute" />
							<xsl:text> </xsl:text>
						</text:p>

						<text:p text:style-name="Rubric">
							<xsl:value-of select="labelLg1" />
						</text:p>

						<xsl:if test="codeList != ''">
							<text:p text:style-name="RubricItem">
								Code list :
								<xsl:value-of select="codeList" />
							</text:p>
						</xsl:if>

						<xsl:if test="rangeType != ''">
							<text:p text:style-name="RubricItem">
								RangeType :
								<xsl:value-of select="rangeType" />
							</text:p>
						</xsl:if>

						<xsl:if test="value != ''">
							<text:p text:style-name="RubricItem">
								Valeur :
								<xsl:value-of select="value" />
							</text:p>
						</xsl:if>
						<xsl:text> </xsl:text>
					</xsl:for-each>


					<!-- On parcourt la MSD -->
					<xsl:for-each select="$fileMsd//mas">
						<xsl:sort data-type="number"
							select="substring-before(concat(substring-after(idMas,'.'),'.'),'.')"
							order="ascending" />
						<xsl:sort data-type="number"
							select="substring-after(substring-after(idMas,'.'),'.')" order="ascending" />
						<xsl:choose>
							<xsl:when test="idParent != ''">
								<table:table>
									<table:table-column />
									<table:table-column />
									<table:table-row>
										<table:table-cell table:style-name="framedCell">
											<text:p text:style-name="RubricItem">
												<xsl:variable name="mas" select="idMas" />
												<xsl:value-of select="$mas" />
												-
												<xsl:value-of select="masLabelLg1" />
												<!-- <xsl:param name="mas" select="idMas" /> -->
<!-- 												<xsl:apply-templates select="$rootVar//rubrics[@idAttribute = $mas]" /> -->
												<xsl:text> </xsl:text>
											</text:p>
										</table:table-cell>
										<table:table-cell table:style-name="framedCell">
											<text:p text:style-name="RubricItem">
												<xsl:value-of select="idMas" />
												-
												<xsl:value-of select="masLabelLg2" />
												<xsl:text> </xsl:text>
											</text:p>
										</table:table-cell>
									</table:table-row>
								</table:table>
							</xsl:when>
							<xsl:otherwise>
								<table:table>
									<table:table-column />
									<table:table-column />
									<table:table-row>
										<table:table-cell>
											<text:p text:style-name="RubricHead">
												<xsl:value-of select="idMas" />
												-
												<xsl:value-of select="masLabelLg1" />
												<xsl:text> </xsl:text>
											</text:p>
										</table:table-cell>
									</table:table-row>
								</table:table>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</office:text>
			</office:body>
		</office:document>
	</xsl:template>

	<xsl:template match="rubrics">
		<xsl:value-of select="'coucou sims'" />
		<xsl:apply-templates select="*" />
		<xsl:value-of select="labelLg1"></xsl:value-of>
	</xsl:template>

	<xsl:template name="series">
		<xsl:value-of select="'coucou series'" />
		<xsl:value-of select="labelLg1"></xsl:value-of>
		<xsl:value-of select="labelLg2"></xsl:value-of>

		<xsl:value-of select="prefLabelLg1"></xsl:value-of>
		<xsl:value-of select="prefLabelLg2"></xsl:value-of>
		<xsl:value-of select="altLabelLg1"></xsl:value-of>
		<xsl:value-of select="altLabelLg2"></xsl:value-of>
		<xsl:value-of select="abstractLg1"></xsl:value-of>
		<xsl:value-of select="abstractLg2"></xsl:value-of>
		<xsl:value-of select="family/labelLg1"></xsl:value-of>
		<xsl:value-of select="family/labelLg2"></xsl:value-of>
		<xsl:value-of select="accrualPeriodicityCode"></xsl:value-of>
		<xsl:value-of select="creator"></xsl:value-of>
		<xsl:value-of select="contributor"></xsl:value-of>

	</xsl:template>


	<xsl:template name="seriesForOperation">
		<text:p text:style-name="T2">
			Informations sur la série:
			<xsl:value-of select="$fileTarget//labelLg1" />
		</text:p>
		<text:p text:style-name="T2En">
			Informations about the series:
			<xsl:value-of select="$fileTarget//labelLg2" />
		</text:p>
		<table:table>
			<table:table-column />
			<table:table-column />
			<table:table-row>
				<table:table-cell table:style-name="framedCell">
					<text:p text:style-name="RubricHead">
						Label
					</text:p>
					<xsl:value-of select="$fileTarget/series/labelLg1"></xsl:value-of>
				</table:table-cell>
				<table:table-cell table:style-name="framedCell">
					<text:p text:style-name="RubricHead">
						Name
					</text:p>
					<xsl:value-of select="$fileTarget/series/labelLg2"></xsl:value-of>
				</table:table-cell>
			</table:table-row>
		</table:table>
	</xsl:template>

	<xsl:template name="operation">
		<table:table>
			<table:table-column />
			<table:table-column />
			<table:table-row>
				<table:table-cell table:style-name="framedCell">
					<text:p text:style-name="RubricHead">
						Nom court
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileTarget//altLabelLg1"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricHead">
						Liens
					</text:p>
					<text:p text:style-name="RubricItem">
					</text:p>
					<text:p text:style-name="RubricHead">
						Série parente
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileTarget/series/labelLg1"></xsl:value-of>
					</text:p>
				</table:table-cell>
				<table:table-cell table:style-name="framedCell">
					<text:p text:style-name="RubricHead">
						Short name
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileTarget//altLabelLg2"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricHead">
						Links
					</text:p>
					<text:p text:style-name="RubricItem">
					</text:p>
					<text:p text:style-name="RubricHead">
						Parent series
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileTarget/series/labelLg2"></xsl:value-of>
					</text:p>
				</table:table-cell>
			</table:table-row>
		</table:table>
	</xsl:template>

	<xsl:template name="indicator">
		<xsl:value-of select="'coucou indicator'" />
		<xsl:value-of select="prefLabelLg1"></xsl:value-of>
		<xsl:value-of select="prefLabelLg2"></xsl:value-of>
		<xsl:value-of select="altLabelLg1"></xsl:value-of>
		<xsl:value-of select="altLabelLg2"></xsl:value-of>
		<xsl:value-of select="abstractLg1"></xsl:value-of>
		<xsl:value-of select="abstractLg2"></xsl:value-of>
	</xsl:template>


</xsl:stylesheet>