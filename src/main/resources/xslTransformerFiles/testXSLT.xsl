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
	<!-- <xsl:include href="../xslTransformerFiles/office-styles.xsl" /> -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes" />
	<!-- omit-xml-declaration="yes" -->
	<!--Sims target -->
	<xsl:param name="tempFile" />
	<xsl:param name="fileTarget" select="document($tempFile)" />
	<!--Sims target's series -->
	<xsl:param name="accessoryTempFile" />
	<xsl:param name="fileSeries" select="document($accessoryTempFile)" />
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
				<!-- Mise en forme Tableaux -->

				<style:style style:name="Tableau3.A" style:family="table-column">
					<style:table-column-properties
						style:column-width="8.0cm" />
				</style:style>
				<style:style style:name="Tableau3.B" style:family="table-column">
					<style:table-column-properties
						style:column-width="0.5cm" />
				</style:style>
				<style:style style:name="Tableau3.C" style:family="table-column">
					<style:table-column-properties
						style:column-width="8.0cm" />
				</style:style>
				<!-- <style:style style:name="TableauMainColumn" -->
				<!-- style:family="table-column"> -->
				<!-- <style:table-column-properties -->
				<!-- style:rel-column-width="21845*" style:column-width="6.5cm" /> -->
				<!-- </style:style> -->
				<!-- <style:style style:name="TableauSepColumn" style:family="table-column"> -->
				<!-- <style:table-column-properties -->
				<!-- style:rel-column-width="21845*" style:column-width="1cm" /> -->
				<!-- </style:style> -->
				<!-- <style:style style:name="separatingColumn" style:family="table-column"> -->
				<!-- <style:table-column-properties -->
				<!-- style:column-width="0.381cm" /> -->
				<!-- </style:style> -->
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
						style:justify-single-word="false" fo:background-color="#012a83"
						font-family="Arial" />
					<style:text-properties fo:font-size="24pt"
						fo:font-weight="bold" fo:color="#ffffff" />
				</style:style>
				<style:style style:name="HeaderEn" style:family="paragraph"
					style:class="chapter">
					<style:paragraph-properties
						fo:text-align="center" style:justify-single-word="false"
						fo:background-color="#234ca5" />
					<style:text-properties fo:font-size="20pt"
						fo:font-weight="bold" fo:color="#ffffff" />
				</style:style>
				<style:style style:name="attribute" style:family="paragraph">
					<style:paragraph-properties
						style:vertical-align="top" style:justify-single-word="false"
						fo:text-align="start" style:auto-text-indent="false"
						fo:text-indent="0cm" fo:line-height="100%"
						loext:contextual-spacing="false" fo:margin-bottom="0.33cm"
						fo:margin-top="0.33cm" fo:margin-right="0.33cm" fo:margin-left="0.33cm" />
					<style:text-properties
						officeooo:paragraph-rsid="0004e715" style:text-rotation-scale="line-height"
						style:text-rotation-angle="0" style:font-size-complex="10pt"
						style:font-size-asian="10pt" fo:font-size="10pt" style:font-name="Arial"
						style:text-underline-color="font-color"
						style:text-underline-width="auto" style:text-underline-style="solid" />
				</style:style>

				<!-- <style:style style:name="HeaderEn" style:family="paragraph"> -->
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

				<!-- <style:style style:name="HeaderFr" style:family="paragraph"> -->
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
						style:width="20.98cm" fo:border-bottom-style="2pt solid #234ca5" />
				</style:style>
				<style:style style:name="T2" style:family="paragraph">
					<style:paragraph-properties
						fo:text-align="center" fo:margin-top="1cm" />
					<style:text-properties
						style:text-rotation-scale="line-height" style:text-rotation-angle="0"
						style:font-size-complex="14pt" style:font-size-asian="14pt"
						fo:font-weight="bold" fo:font-size="14pt" style:font-name="Arial"
						fo:color="#234ca5" />
				</style:style>
				<style:style style:name="T2En" style:family="paragraph">
					<style:paragraph-properties
						fo:text-align="center" />
					<style:text-properties
						style:text-rotation-scale="line-height" style:text-rotation-angle="0"
						style:font-size-complex="12pt" style:font-size-asian="12pt"
						fo:font-weight="bold" fo:font-size="12pt" style:font-name="Arial"
						fo:color="#234ca5" />
				</style:style>
				<style:style style:name="T3" style:family="paragraph">
					<style:paragraph-properties
						fo:text-align="center" fo:margin-top="1cm"
						style:justify-single-word="false" fo:background-color="#012a83"
						font-family="Arial" />
					<style:text-properties fo:font-size="14pt"
						fo:font-weight="bold" fo:color="#ffffff" />
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
				<style:style style:name="RubricHead" style:family="paragraph">
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
				<style:style style:name="RubricItem" style:family="paragraph">
					<style:paragraph-properties
						fo:text-align="left" style:justify-single-word="false"
						fo:margin-bottom="0.33cm" fo:margin-top="0.33cm" fo:margin-right="0.15cm"
						fo:margin-left="0.15cm" />
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

					<!-- Essais -->
					<!-- <text:p> -->
					<!-- <xsl:value-of select="' Series: '" /> -->
					<!-- <xsl:value-of select="$fileSeries/Series/id" /> -->
					<!-- <xsl:value-of select="$fileSeries/Series/prefLabelLg1" /> -->
					<!-- <xsl:value-of select="$accessoryTempFile" /> -->
					<!-- </text:p> -->
					<!-- <text:p> -->
					<!-- <xsl:value-of select="' Operation: '" /> -->
					<!-- <xsl:value-of select="$fileTarget/Operation/id" /> -->
					<!-- <xsl:value-of select="$fileTarget/Operation/prefLabelLg1" /> -->
					<!-- <xsl:value-of select="$tempFile" /> -->
					<!-- </text:p> -->

					<text:p>
						<xsl:value-of select="'xsl:version: '" />
						<xsl:value-of select="system-property('xsl:version')" />
					</text:p>


					<!-- Header -->
					<xsl:call-template name="header"></xsl:call-template>

					<!-- Title -->
					<xsl:choose>
						<xsl:when test="$targetType='SERIES'">
							<xsl:call-template name="series"></xsl:call-template>
						</xsl:when>
						<xsl:when test="$targetType='OPERATION'">
							<xsl:call-template name="series"></xsl:call-template>
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

					<text:p text:style-name="T3">
						<xsl:value-of select="'Informations sur le Sims : '" />
						<xsl:value-of select="$fileTarget//prefLabelLg1" />
					</text:p>


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
												<xsl:param name="mas" select="idMas" />
<!-- 												<xsl:apply-templates select="$rootVar/Documentation/rubrics/rubrics[@idAttribute = $mas]" /> -->
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

						<xsl:choose>
							<xsl:when test="rangeType = 'GEOGRAPHY'"></xsl:when>
							<xsl:when test="rangeType = 'RICH_TEXT'"></xsl:when>
							<xsl:when test="rangeType = 'CODE_LIST'"></xsl:when>
							<xsl:when test="rangeType = 'ORGANIZATION'"></xsl:when>
							<xsl:when test="rangeType = 'TEXT'"></xsl:when>
						</xsl:choose>
						<xsl:text> </xsl:text>
					</xsl:for-each>

				</office:text>
			</office:body>
		</office:document>
	</xsl:template>

	<xsl:template name="header">
		<table:table table:name="Tableau1" table:style-name="Tableau1">
			<table:table-row>
				<table:table-cell>
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
	</xsl:template>

	<xsl:template match="rubrics">
		<text:p>
			<xsl:value-of select="'coucou sims'" />
			<xsl:apply-templates select="*" />
			<xsl:value-of select="labelLg1"></xsl:value-of>
		</text:p>
	</xsl:template>

	<xsl:template name="series">
		<table:table table:name="TableauSeriesHead"
			table:style-name="Tableau1">
			<table:table-row>
				<table:table-cell>
					<text:p text:style-name="T2">
						Informations sur la série:
						<xsl:value-of select="$fileSeries/Series/prefLabelLg1" />
					</text:p>
					<text:p text:style-name="T2En">
						Informations about the series:
						<xsl:value-of select="$fileSeries/Series/prefLabelLg2" />
					</text:p>
				</table:table-cell>
			</table:table-row>
		</table:table>
		<table:table table:name="TableauSeries" table:style-name="Tableau1">
			<table:table-column table:style-name="Tableau3.A" />
			<table:table-column table:style-name="Tableau3.B" />
			<table:table-column table:style-name="Tableau3.C" />
			<table:table-row>
				<!-- français -->
				<table:table-cell table:style-name="framedCell">
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Nom court'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileSeries/Series/altLabelLg1"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Résumé'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileSeries/Series/abstractLg1"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Historique'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileSeries/Series/historyNoteLg1"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Type d'"></xsl:value-of>
						&apos;
						<xsl:value-of select="'opération'" />
					</text:p>
					<xsl:if test="$fileSeries/Series/typeCode!=''">
						<text:p text:style-name="RubricItem">
							Modalité
							<xsl:value-of select="$fileSeries/Series/typeCode"></xsl:value-of>
							de la liste de codes:
							<xsl:value-of select="$fileSeries/Series/typeList"></xsl:value-of>
						</text:p>
					</xsl:if>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Fréquence de collecte des données'"></xsl:value-of>
					</text:p>
					<xsl:if test="$fileSeries/Series/accrualPeriodicityCode!=''">
						<text:p text:style-name="RubricItem">
							Modalité
							<xsl:value-of select="$fileSeries/Series/accrualPeriodicityCode"></xsl:value-of>
							de la liste de codes:
							<xsl:value-of select="$fileSeries/Series/accrualPeriodicityList"></xsl:value-of>
						</text:p>
					</xsl:if>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Organismes responsables'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:for-each select="$fileSeries/Series/publishers/publishers">
							<xsl:value-of select="labelLg1"></xsl:value-of>
							<xsl:if test="position() != last()">
								-
							</xsl:if>
						</xsl:for-each>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Partenaires'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:for-each select="$fileSeries/Series/contributors/contributors">
							<xsl:value-of select="labelLg1"></xsl:value-of>
							<xsl:if test="position() != last()">
								-
							</xsl:if>
						</xsl:for-each>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Services collecteurs'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:for-each select="$fileSeries/Series/dataCollectors/dataCollectors">
							<xsl:value-of select="labelLg1"></xsl:value-of>
							<xsl:if test="position() != last()">
								-
							</xsl:if>
						</xsl:for-each>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Propriétaire'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileSeries/Series/creators/creators"></xsl:value-of>
					</text:p>
					<!-- <text:p text:style-name="RubricItem"> -->
					<!-- <xsl:for-each select="$fileSeries/Series/proprietaires/proprietaires"> -->
					<!-- <xsl:value-of select="labelLg1"></xsl:value-of> -->
					<!-- </xsl:for-each> -->
					<!-- </text:p> -->
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Succède à'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<!-- Afficher toute la liste! -->
						<xsl:value-of select="$fileSeries/Series/replaces//labelLg1"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Remplacée par'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<!-- Afficher toute la liste! -->
						<xsl:value-of select="$fileSeries/Series/isReplacedBy//labelLg1"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<!-- Afficher toute la liste! -->
						<xsl:value-of select="'Indicateurs produits'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<!-- Afficher toute la liste! -->
						<xsl:value-of select="$fileSeries/Series/generates//labelLg1"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Séries ou Indicateurs liés'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="'Séries:'"/>
						<xsl:for-each select="$fileSeries/Series/seeAlso/seeAlso">
							<xsl:if test="type = 'series'">
								<xsl:value-of select="labelLg1"></xsl:value-of>
								<xsl:if test="position() != last()">
									-
								</xsl:if>
							</xsl:if>
						</xsl:for-each>
						<xsl:value-of select="'Indicateurs:'"/>
						<xsl:for-each select="$fileSeries/Series/seeAlso/seeAlso">
							<xsl:if test="type = 'indicator'">
								<xsl:value-of select="labelLg1"></xsl:value-of>
								<xsl:if test="position() != last()">
									-
								</xsl:if>
							</xsl:if>
						</xsl:for-each>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Famille parente'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileSeries/Series/family/labelLg1"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Opérations filles'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:for-each select="$fileSeries/Series/operations/operations">
							<xsl:value-of select="labelLg1"></xsl:value-of>
							<xsl:if test="position() != last()">
								-
							</xsl:if>
						</xsl:for-each>
					</text:p>
				</table:table-cell>
				<!-- séparation -->
				<table:table-cell table:style-name="separatingColumn" />
				<!-- english -->
				<table:table-cell table:style-name="framedCell">
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Short name'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileSeries/Series/altLabelLg2"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Summary'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileSeries/Series/abstractLg2"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'History'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileSeries/Series/historyNoteLg2"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Operation type'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Data collection frequency'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Replaces'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Replaced by'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Indicators produced'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Parent Family'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileSeries/Series/family/labelLg2"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Daughter operations'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileSeries//operation/labelLg2"></xsl:value-of>
					</text:p>
				</table:table-cell>
			</table:table-row>
		</table:table>
	</xsl:template>

	<!-- Deprecated -->
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
			<!-- <table:table-column /> -->
			<!-- <table:table-column /> -->
			<!-- <table:table-column /> -->

			<table:table-row>
				<!-- <table:table-cell /> -->
				<table:table-cell>
					<text:p text:style-name="T2">
						Informations sur l'opération:
						<xsl:value-of select="$fileTarget/Operation/prefLabelLg1" />
					</text:p>
					<text:p text:style-name="T2En">
						Informations about the operation:
						<xsl:value-of select="$fileTarget/Operation/prefLabelLg2" />
					</text:p>
				</table:table-cell>
				<!-- <table:table-cell /> -->
			</table:table-row>
		</table:table>
		<table:table>
			<table:table-column table:style-name="Tableau3.A" />
			<table:table-column table:style-name="Tableau3.B" />
			<table:table-column table:style-name="Tableau3.C" />
			<table:table-row>
				<table:table-cell table:style-name="framedCell">
					<text:p text:style-name="attribute">
						Nom court
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileTarget/Operation/altLabelLg1"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						Liens
					</text:p>
					<text:p text:style-name="RubricItem">
					</text:p>
					<text:p text:style-name="attribute">
						Série parente
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileTarget/Operation/series/labelLg1"></xsl:value-of>
					</text:p>
				</table:table-cell>
				<table:table-cell />
				<table:table-cell table:style-name="framedCell">
					<text:p text:style-name="attribute">
						Short name
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileTarget/Operation/altLabelLg2"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						Links
					</text:p>
					<text:p text:style-name="RubricItem">
					</text:p>
					<text:p text:style-name="attribute">
						Parent series
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileTarget/Operation/series/labelLg2"></xsl:value-of>
					</text:p>
				</table:table-cell>
			</table:table-row>
		</table:table>
	</xsl:template>

	<xsl:template name="indicator">
		<table:table table:name="TableauIndicatorHead"
			table:style-name="Tableau1">
			<table:table-row>
				<table:table-cell>
					<text:p text:style-name="T2">
						Informations sur l'indicateur:
						<xsl:value-of select="$fileTarget/Indicator/prefLabelLg1" />
					</text:p>
					<text:p text:style-name="T2En">
						Informations about the indicator:
						<xsl:value-of select="$fileTarget/Indicator/prefLabelLg2" />
					</text:p>
				</table:table-cell>
			</table:table-row>
		</table:table>

		<table:table table:name="TableauIndicator"
			table:style-name="Tableau1">
			<table:table-column table:style-name="Tableau3.A" />
			<table:table-column table:style-name="Tableau3.B" />
			<table:table-column table:style-name="Tableau3.C" />
			<table:table-row>
				<!-- français -->
				<table:table-cell table:style-name="framedCell">
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Nom court'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileTarget/Indicator/altLabelLg1"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Résumé'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileTarget/Indicator/abstractLg1"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Historique'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileTarget/Indicator/historyNoteLg1"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Fréquence de diffusion'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Partenaires'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:for-each select="$fileTarget/Indicator/contributors/contributors">
							<xsl:value-of select="labelLg1"></xsl:value-of>
						</xsl:for-each>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Organisme responsable'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileTarget/Indicator/publisher"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Propriétaire'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Succède à'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Remplacée par'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Produit de'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Séries ou Indicateurs liés'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileTarget/Indicator/operation/labelLg1"></xsl:value-of>
					</text:p>
				</table:table-cell>
				<!-- séparation -->
				<table:table-cell table:style-name="separatingColumn" />
				<!-- english -->
				<table:table-cell table:style-name="framedCell">
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Short name'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileTarget/Indicator/altLabelLg1"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Summary'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileTarget/Indicator/abstractLg1"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'History'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileTarget/Indicator/historyNoteLg1"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Frequency of dissemination'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Organisation in charge'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Owner'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Partners'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Replaces'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Remplaced by'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Produced from'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="attribute">
						<xsl:value-of select="'Related series or indicators'"></xsl:value-of>
					</text:p>
					<text:p text:style-name="RubricItem">
						<xsl:value-of select="$fileTarget/Indicator/operation/labelLg1"></xsl:value-of>
					</text:p>
				</table:table-cell>
			</table:table-row>
		</table:table>
	</xsl:template>


</xsl:stylesheet>