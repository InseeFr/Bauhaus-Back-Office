<?xml version="1.1" encoding="UTF-8" ?>

<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" encoding="UTF-8" indent="yes" />

	<xsl:template match="/">
		<xsl:value-of select="'xsl:version: '" />
		<xsl:value-of select="system-property('xsl:version')" />
	</xsl:template>

</xsl:stylesheet>