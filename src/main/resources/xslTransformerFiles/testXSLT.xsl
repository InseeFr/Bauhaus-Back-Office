<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- <xsl:include href="../xslTransformerFiles/office-styles.xsl" /> -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes" />
	<!-- omit-xml-declaration="yes" -->
	<xsl:param name="tempFile" />
	<xsl:param name="fileTarget" select="document($tempFile)" />
	<xsl:param name="msd" />
	<xsl:param name="fileMsd" select="document($msd)" />
	<xsl:param name="targetType" />

	<xsl:strip-space elements="*" />
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
						style:justify-single-word="false" fo:background-color="blue" />
					<style:text-properties fo:font-size="24pt"
						fo:font-weight="bold" fo:color="white" />
				</style:style>

				<style:style style:name="HeaderEn" style:family="paragraph"
					style:class="chapter">
					<style:paragraph-properties
						fo:text-align="center" fo:margin-top="1cm"
						style:justify-single-word="false" fo:background-color="blue" />
					<style:text-properties fo:font-size="20pt"
						fo:font-weight="bold" fo:color="white" />
				</style:style>

				<style:style style:name="TitleFr" style:family="paragraph"
					style:class="chapter">
					<style:paragraph-properties
						fo:text-align="center" fo:margin-top="1cm"
						style:justify-single-word="false" />
					<style:text-properties fo:font-size="20pt"
						fo:font-weight="bold" fo:color="#7b7c7c" />
				</style:style>

				<style:style style:name="TitleEn" style:family="paragraph"
					style:class="chapter">
					<style:paragraph-properties
						fo:text-align="center" fo:margin-top="1cm"
						style:justify-single-word="false" />
					<style:text-properties fo:font-size="16pt"
						fo:font-weight="bold" fo:color="#7b7c7c" />
				</style:style>

				<style:style style:name="Rubric" style:family="paragraph"
					style:class="chapter">
					<style:paragraph-properties
						fo:text-align="left" fo:margin-top="1cm"
						style:justify-single-word="false" />
					<style:text-properties fo:font-size="12pt"
						fo:font-weight="normal" fo:color="black" />
				</style:style>

				<style:style style:name="RubricHead" style:family="paragraph"
					style:class="chapter">
					<style:paragraph-properties
						fo:text-align="left" fo:margin-top="1cm"
						style:justify-single-word="false" />
					<style:text-properties fo:font-size="14pt"
						fo:font-weight="bold" fo:color="blue" />
				</style:style>

				<style:style style:name="RubricItem" style:family="paragraph"
					style:class="chapter">
					<style:paragraph-properties
						fo:text-align="left" style:justify-single-word="false" />
					<style:text-properties fo:font-size="12pt"
						fo:font-weight="normal" fo:color="black" />
				</style:style>

			</office:automatic-styles>

			<office:body>
				<office:text>
					<!-- Header -->
					<text:p text:style-name="HeaderFr">
						<xsl:value-of select="$fileTarget//prefLabelLg1" />
					</text:p>

					<text:p text:style-name="HeaderEn">
						<xsl:value-of select="$fileTarget//prefLabelLg2" />
					</text:p>
					<!-- Title -->
					<text:p text:style-name="TitleFr">
						<xsl:choose>
							<xsl:when test="$targetType='SERIES'">
								Informations sur la série:
							</xsl:when>
							<xsl:when test="$targetType='OPERATION'">
								Informations sur l'opération:
							</xsl:when>
							<xsl:otherwise>
								Informations sur l'indicateur:
							</xsl:otherwise>
						</xsl:choose>
						<xsl:value-of select="$fileTarget//prefLabelLg1" />
					</text:p>
					<text:p text:style-name="TitleEn">
						<xsl:choose>
							<xsl:when test="$targetType='SERIES'">
								Informations about the series:
							</xsl:when>
							<xsl:when test="$targetType='OPERATION'">
								Informations about the operation:
							</xsl:when>
							<xsl:otherwise>
								Informations about the indicator:
							</xsl:otherwise>
						</xsl:choose>
						<xsl:value-of select="$fileTarget//prefLabelLg2" />
					</text:p>


					<!-- Sims -->

					<text:p text:style-name="TitleFr">
						Informations sur le Sims
						<xsl:value-of select="Documentation/labelLg1" />
					</text:p>
					<!-- <table> -->
					<!-- <text:p text:style-name="P1"> -->
					<!-- Sims -->
					<!-- <xsl:value-of select="Documentation/id" /> -->
					<!-- </text:p> -->
					<!-- <text:p text:style-name="P1"> -->
					<!-- <xsl:text>&#13;</xsl:text> -->
					<!-- <xsl:if test="Documentation/idOperation != ''"> -->
					<!-- Opération -->
					<!-- <xsl:value-of select="Documentation/idOperation" /> -->
					<!-- </xsl:if> -->
					<!-- <xsl:if test="Documentation/idSeries != ''"> -->
					<!-- Série -->
					<!-- <xsl:value-of select="Documentation/idSeries" /> -->
					<!-- </xsl:if> -->
					<!-- <xsl:if test="Documentation/idIndicator != ''"> -->
					<!-- Indicateur -->
					<!-- <xsl:value-of select="Documentation/idIndicator" /> -->
					<!-- </xsl:if> -->
					<!-- </text:p> -->
					<!-- </table> -->



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

<!-- 					<text:p text:style-name="RubricItem"> -->
<!-- 						<xsl:value-of select="$fileMsd//mas" /> -->
<!-- 					</text:p> -->

					<xsl:for-each select="$fileMsd//mas">
						<text:p text:style-name="RubricHead">
							<xsl:value-of select="masLabelLg1" />
							<xsl:text> </xsl:text>
						</text:p>
					</xsl:for-each>



				</office:text>
			</office:body>
		</office:document>
	</xsl:template>

	<!-- <xsl:template match="$fileMsd"> -->
	<!-- <xsl:copy> -->
	<!-- <xsl:apply-templates select="$fileMsd//mas" /> -->
	<!-- </xsl:copy> -->
	<!-- </xsl:template> -->

</xsl:stylesheet>