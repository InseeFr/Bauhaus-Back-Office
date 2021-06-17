<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
    xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0" xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
    xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0" xmlns:officeooo="http://openoffice.org/2009/office"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="xs xd"
    version="3.0">

    <xsl:output method="xml" encoding="UTF-8" indent="yes" />

    <!-- the params with the addresses of the files -->
    <xsl:param name="seriesFile"/>
    <xsl:param name="operationFile"/>
    <xsl:param name="indicatorFile"/>
    <xsl:param name="simsFile"/>
    <xsl:param name="organizationsFile"/>
    <xsl:param name="codeListsFile"/>
    <xsl:param name="msdFile"/>
    <xsl:param name="conceptsFile"/>
    <xsl:param name="collectionsFile"/>
    <xsl:param name="parametersFile"/>

    <!-- the params with the content of the files -->
    <xsl:param name="parameters">
        <xsl:choose>
            <xsl:when test="doc-available($parametersFile)">
                <xsl:copy-of select="document($parametersFile)"/>
            </xsl:when>
            <xsl:otherwise>
                <parameters/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:param>
    <xsl:param name="series" as="node()">
        <xsl:choose>
            <xsl:when test="doc-available($seriesFile)">
                <xsl:copy-of select="document($seriesFile)"/>
            </xsl:when>
            <xsl:otherwise>
                <Series/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:param>
    <xsl:param name="operation" as="node()">
        <xsl:choose>
            <xsl:when test="doc-available($operationFile)">
                <xsl:copy-of select="document($operationFile)"/>
            </xsl:when>
            <xsl:otherwise>
                <Operation/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:param>
    <xsl:param name="indicator" as="node()">
        <xsl:choose>
            <xsl:when test="doc-available($indicatorFile)">
                <xsl:copy-of select="document($indicatorFile)"/>
            </xsl:when>
            <xsl:otherwise>
                <Indicator/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:param>
    <xsl:param name="sims" as="node()">
        <xsl:choose>
            <xsl:when test="doc-available($simsFile)">
                <xsl:copy-of select="document($simsFile)"/>
            </xsl:when>
            <xsl:otherwise>
                <Sims/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:param>
    <xsl:param name="organizations" as="node()">
        <xsl:choose>
            <xsl:when test="doc-available($organizationsFile)">
                <xsl:copy-of select="document($organizationsFile)"/>
            </xsl:when>
            <xsl:otherwise>
                <Organizations/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:param>
    <xsl:param name="codeLists" as="node()">
        <xsl:choose>
            <xsl:when test="doc-available($codeListsFile)">
                <xsl:copy-of select="document($codeListsFile)"/>
            </xsl:when>
            <xsl:otherwise>
                <CodeLists/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:param>
    <xsl:param name="msd" as="node()">
        <xsl:choose>
            <xsl:when test="doc-available($msdFile)">
                <xsl:copy-of select="document($msdFile)"/>
            </xsl:when>
            <xsl:otherwise>
                <Msd/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:param>
    <xsl:param name="concepts" as="node()">
        <xsl:choose>
            <xsl:when test="doc-available($conceptsFile)">
                <xsl:copy-of select="document($conceptsFile)"/>
            </xsl:when>
            <xsl:otherwise>
                <Concepts/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:param>
    <xsl:param name="collections" as="node()">
        <xsl:choose>
            <xsl:when test="doc-available($collectionsFile)">
                <xsl:copy-of select="document($collectionsFile)"/>
            </xsl:when>
            <xsl:otherwise>
                <Collections/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:param>

    <!-- keep the minimum spaces from the data -->
    <xsl:strip-space elements="*" />

    <xd:doc>
        <xd:desc>Root template</xd:desc>
    </xd:doc>
    <xsl:template match="/">
        <xsl:apply-templates select="*">
            <xsl:with-param name="context" as="node()" tunnel="yes">
                <Context/>
            </xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xd:doc>
        <xd:desc>default template : copy the fodt file, element by element</xd:desc>
    </xd:doc>
    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*[not(name()='xml:id')]"/>
        </xsl:copy>
    </xsl:template>

    <xd:doc>
        <xd:desc>loop on the mas</xd:desc>
    </xd:doc>
    <xsl:template match="text:section[@text:name='mas-loop']">
        <xsl:variable name="section-content" select="*" as="node() *"/>
        <xsl:for-each select="$msd//mas">
            <xsl:sort data-type="number" select="substring-before(concat(substring-after(idMas,'.'),'.0'),'.')"/>
            <xsl:sort data-type="number" select="substring-after(substring-after(idMas,'.'),'.')"/>
            <!-- ever mas or only the mas corresponding to a rubrics from the sims or its parent or its grand-parent -->
            <xsl:if test="$parameters//includeEmptyFields = 'true'
                or idMas = $sims//rubrics/idAttribute
                or idMas = $msd//mas[idMas = $sims//rubrics/idAttribute]/idParent
                or idMas = $msd//mas[idMas = $msd//mas[idMas = $sims//rubrics/idAttribute]/idParent]/idParent">
                <xsl:apply-templates select="$section-content">
                    <xsl:with-param name="context" as="node()" tunnel="yes">
                        <Context>
                            <mas><xsl:value-of select="idMas"/></mas>
                        </Context>
                    </xsl:with-param>
                </xsl:apply-templates>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xd:doc>
        <xd:desc>elements with @id are used to filter the pattern, depending on parameters or mas/isPresentational </xd:desc>
    </xd:doc>
    <xsl:template match="*[@xml:id]" priority="1">
        <xsl:param name="context" tunnel="yes"/>
        <xsl:variable name="parameter-name" select="substring-before(@xml:id,'.')"/>
        <xsl:variable name="parameter-value" select="substring-before(substring-after(concat(@xml:id,'.'),'.'),'.')"/>

        <xsl:choose>
            <xsl:when test="$parameter-name = 'targetType' or $parameter-name = 'lang' or $parameter-name = 'mas'">
                <xsl:variable name="show-children" as="xs:boolean">
                    <xsl:choose>
                        <xsl:when test="$parameter-name = 'targetType'">
                            <xsl:value-of select="$parameters//targetType !='' and contains(upper-case($parameter-value),upper-case($parameters//targetType))"/>
                        </xsl:when>
                        <xsl:when test="$parameter-name = 'lang'">
                            <xsl:value-of select="$parameter-value = $parameters//language/@id
                                              or ($parameter-value='2' and count($parameters//language)&gt;=2)"/>
                        </xsl:when>
                        <xsl:when test="$parameter-name = 'mas' and $parameter-value = 'parent'">
                            <xsl:value-of select="$msd//mas[idMas = $context//mas]/isPresentational = 'true'"/>
                        </xsl:when>
                        <xsl:when test="$parameter-name = 'mas' and $parameter-value = 'child'">
                            <xsl:value-of select="not($msd//mas[idMas = $context//mas]/isPresentational = 'true')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="true()"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:if test="$show-children">
                    <xsl:next-match/>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat('identifiant inconnu : ',@xml:id)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xd:doc>
        <xd:desc>filter rows when $parameters//includeEmptyFields is false</xd:desc>
    </xd:doc>
    <xsl:template match="table:table-row">
        <xsl:variable name="show-children" as="xs:boolean">
            <xsl:choose>
                <xsl:when test="$parameters//includeEmptyFields = 'false' and descendant::text:p[contains(text(),'${')]">
                    <xsl:variable name="first-personalized-variable">
                        <xsl:call-template name="get-variable-nodes">
                            <xsl:with-param name="variable-address" select="substring-before(substring-after(descendant::text:p[contains(text(),'${')][1],'${'),'}')"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:value-of select="$first-personalized-variable != ''"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="true()"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:if test="$show-children">
            <xsl:copy>
                <xsl:apply-templates select="node() | @*[not(name()='xml:id')]"/>
            </xsl:copy>
        </xsl:if>
    </xsl:template>

    <xd:doc>
        <xd:desc>text:p elements containing ${referenceToData} are filled with the referenced data</xd:desc>
        <xd:desc>referenced data may contain paragraphs with their own style or simple strings</xd:desc>
    </xd:doc>
    <xsl:template match="text:p[contains(text(),'${')]">
        <xsl:variable name="new-text">
            <xsl:call-template name="personalize-text">
                <xsl:with-param name="text-to-personalize" select="text()"/>
                <xsl:with-param name="style" select="@text:style-name" tunnel="yes"/>
                <xsl:with-param name="title-style" select="preceding::text:p[1]/@text:style-name" tunnel="yes"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$new-text//*">
                <xsl:copy-of select="$new-text"/>
            </xsl:when>
            <xsl:otherwise>
                <text:p text:style-name="{@text:style-name}">
                    <xsl:value-of select="$new-text"/>
                </text:p>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Recognize grammar structures :</xd:p>
            <xd:p>${dataReference}</xd:p>
            <xd:p>#if (${dataReference}) content #endif</xd:p>
            <xd:p>#list(${dataReference},'list style')</xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template name="personalize-text">
        <xsl:param name="text-to-personalize"/>
        <xsl:param name="style" tunnel="yes"/>
        <xsl:param name="context" as="node()" tunnel="yes"/>

        <!-- $1 #if ($2) $3 #if ($4) $5 #endif $6 #endif $7-->
        <xsl:analyze-string select="$text-to-personalize" regex="^(.*)#if \((.*)\)(.*)#if \((.*)\)(.*)#endif(.*)#endif(.*)$">
            <xsl:matching-substring>
                <xsl:variable name="test1-text">
                    <xsl:call-template name="get-variable-nodes">
                        <xsl:with-param name="variable-address" select="substring-before(substring-after(regex-group(2),'${'),'}')"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="test2-text">
                    <xsl:call-template name="get-variable-nodes">
                        <xsl:with-param name="variable-address" select="substring-before(substring-after(regex-group(4),'${'),'}')"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:call-template name="personalize-text">
                    <xsl:with-param name="text-to-personalize" select="regex-group(1)"/>
                </xsl:call-template>
                <xsl:if test="$test1-text != ''">
                    <xsl:call-template name="personalize-text">
                        <xsl:with-param name="text-to-personalize" select="regex-group(3)"/>
                    </xsl:call-template>
                    <xsl:if test="$test2-text != ''">
                        <xsl:call-template name="personalize-text">
                            <xsl:with-param name="text-to-personalize" select="regex-group(5)"/>
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:call-template name="personalize-text">
                        <xsl:with-param name="text-to-personalize" select="regex-group(6)"/>
                    </xsl:call-template>
                </xsl:if>
                <xsl:call-template name="personalize-text">
                    <xsl:with-param name="text-to-personalize" select="regex-group(7)"/>
                </xsl:call-template>
            </xsl:matching-substring>
            <xsl:non-matching-substring>
                <!-- $1 #if ($2) $3 #endif $4 -->
                <xsl:analyze-string select="$text-to-personalize" regex="^(.*)#if \((.*)\)(.*)#endif(.*)$">
                    <xsl:matching-substring>
                        <xsl:variable name="test1-text">
                            <xsl:call-template name="get-variable-nodes">
                                <xsl:with-param name="variable-address" select="substring-before(substring-after(regex-group(2),'${'),'}')"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:call-template name="personalize-text">
                            <xsl:with-param name="text-to-personalize" select="regex-group(1)"/>
                        </xsl:call-template>
                        <xsl:if test="$test1-text != ''">
                            <xsl:call-template name="personalize-text">
                                <xsl:with-param name="text-to-personalize" select="regex-group(3)"/>
                            </xsl:call-template>
                        </xsl:if>
                        <xsl:call-template name="personalize-text">
                            <xsl:with-param name="text-to-personalize" select="regex-group(4)"/>
                        </xsl:call-template>
                    </xsl:matching-substring>
                    <xsl:non-matching-substring>
                        <!-- $1 #list($2,'$3') $4 -->
                        <xsl:analyze-string select="$text-to-personalize" regex="^(.*)#list\((.*),'(.*)'\)(.*)$">
                            <xsl:matching-substring>
                                <xsl:call-template name="personalize-text">
                                    <xsl:with-param name="text-to-personalize" select="regex-group(1)"/>
                                </xsl:call-template>
                                <xsl:variable name="listed-variables" as="xs:string *">
                                    <xsl:call-template name="get-variable-nodes">
                                        <xsl:with-param name="variable-address" select="substring-before(substring-after(regex-group(2),'${'),'}')"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:if test="$listed-variables != ''">
                                    <xsl:choose>
                                        <xsl:when test="count($listed-variables) != 1">
                                            <text:list text:style-name="{regex-group(3)}">
                                                <xsl:for-each select="$listed-variables">
                                                    <text:list-item>
                                                        <text:p text:style-name="{$style}">
                                                            <xsl:value-of select="."/>
                                                        </text:p>
                                                    </text:list-item>
                                                </xsl:for-each>
                                            </text:list>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <text:p text:style-name="{$style}">
                                                <xsl:value-of select="$listed-variables"/>
                                            </text:p>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:if>
                                <xsl:call-template name="personalize-text">
                                    <xsl:with-param name="text-to-personalize" select="regex-group(4)"/>
                                </xsl:call-template>
                            </xsl:matching-substring>
                            <xsl:non-matching-substring>
                                <xsl:choose>
                                    <!-- $before ${ $var } $after -->
                                    <xsl:when test="contains(.,'${')">
                                        <xsl:variable name="before-var" select="substring-before($text-to-personalize,'${')"/>
                                        <xsl:variable name="after-var">
                                            <xsl:call-template name="personalize-text">
                                                <xsl:with-param name="text-to-personalize" select="substring-after(substring-after($text-to-personalize,'${'),'}')"/>
                                            </xsl:call-template>
                                        </xsl:variable>
                                        <xsl:if test="$before-var !=''">
                                            <xsl:value-of select="$before-var"/>
                                        </xsl:if>
                                        <xsl:call-template name="get-variable-nodes">
                                            <xsl:with-param name="variable-address" select="substring-before(substring-after($text-to-personalize,'${'),'}')"/>
                                        </xsl:call-template>
                                        <xsl:if test="$after-var !=''">
                                            <xsl:value-of select="$after-var"/>
                                        </xsl:if>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$text-to-personalize"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:non-matching-substring>
                        </xsl:analyze-string>
                    </xsl:non-matching-substring>
                </xsl:analyze-string>
            </xsl:non-matching-substring>
        </xsl:analyze-string>
    </xsl:template>

    <xd:doc>
        <xd:desc>Transform variable-name into nodes</xd:desc>
    </xd:doc>
    <xsl:template name="get-variable-nodes">
        <xsl:param name="variable-address"/>
        <xsl:param name="style" tunnel="yes"/>
        <xsl:param name="title-style" tunnel="yes"/>
        <xsl:param name="context" as="node()" tunnel="yes"/>

        <xsl:variable name="source" select="substring-before($variable-address,'/')"/>
        <xsl:variable name="address-complement" select="substring-after($variable-address,'/')"/>

        <xsl:choose>
            <xsl:when test="$source = 'concept' and not(contains($address-complement,'/'))">
                <xsl:copy-of select="$concepts//*[local-name()=$address-complement]//text()"/>
            </xsl:when>
            <xsl:when test="$source = 'concept' and starts-with($address-complement,'richContent/')">
                <xsl:apply-templates select="$concepts//*[local-name()=substring-after($address-complement,'richContent/')]/*" mode="rich-content"/>
            </xsl:when>
            <xsl:when test="$source = 'concept'">
                <xsl:copy-of select="$concepts//*[local-name()=substring-before($address-complement,'/')]//*[local-name()=substring-after($address-complement,'/')]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'collection' and not(contains($address-complement,'/'))">
                <xsl:copy-of select="$collections//*[local-name()=$address-complement]//text()"/>
            </xsl:when>
            <xsl:when test="$source = 'series' and not(contains($address-complement,'/'))">
                <xsl:copy-of select="$series//*[local-name()=$address-complement]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'series' and starts-with($address-complement,'richContent/')">
                <xsl:apply-templates select="$series//*[local-name()=substring-after($address-complement,'richContent/')]/*" mode="rich-content"/>
            </xsl:when>
            <xsl:when test="$source = 'series' and starts-with($address-complement,'seeAlso-')">
                <xsl:copy-of select="$series//seeAlso[type=substring-before(substring-after($address-complement,'-'),'/')]//*[local-name()=substring-after($address-complement,'/')]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'series'">
                <xsl:copy-of select="$series//*[local-name()=substring-before($address-complement,'/')]//*[local-name()=substring-after($address-complement,'/')]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'operation' and not(contains($address-complement,'/'))">
                <xsl:copy-of select="$operation//*[local-name()=$address-complement]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'operation'">
                <xsl:copy-of select="$operation//*[local-name()=substring-before($address-complement,'/')]//*[local-name()=substring-after($address-complement,'/')]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'indicator' and not(contains($address-complement,'/'))">
                <xsl:copy-of select="$indicator//*[local-name()=$address-complement]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'indicator' and starts-with($address-complement,'richContent/')">
                <xsl:apply-templates select="$indicator//*[local-name()=substring-after($address-complement,'richContent/')]/*" mode="rich-content"/>
            </xsl:when>
            <xsl:when test="$source = 'indicator' and starts-with($address-complement,'seeAlso-')">
                <xsl:copy-of select="$indicator//seeAlso[type=substring-before(substring-after($address-complement,'-'),'/')]//*[local-name()=substring-after($address-complement,'/')]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'indicator'">
                <xsl:copy-of select="$indicator//*[local-name()=substring-before($address-complement,'/')]//*[local-name()=substring-after($address-complement,'/')]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'seriesCode'">
                <xsl:variable name="series-variable" select="substring-before($address-complement,'/')"/>
                <xsl:variable name="codeList-variable" select="substring-after($address-complement,'/')"/>
                <xsl:variable name="codeList-name" select="$series//*[local-name() = concat($series-variable,'List')]"/>
                <xsl:variable name="code-value" select="$series//*[local-name() = concat($series-variable,'Code')]"/>
                <xsl:copy-of select="$codeLists//CodeList[notation=$codeList-name]
                                                //codes[code=$code-value]
                                                /*[local-name()=$codeList-variable]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'indicatorCode'">
                <xsl:variable name="indicator-variable" select="substring-before($address-complement,'/')"/>
                <xsl:variable name="codeList-variable" select="substring-after($address-complement,'/')"/>
                <xsl:variable name="codeList-name" select="$indicator//*[local-name() = concat($indicator-variable,'List')]"/>
                <xsl:variable name="code-value" select="$indicator//*[local-name() = concat($indicator-variable,'Code')]"/>
                <xsl:copy-of select="$codeLists//CodeList[notation=$codeList-name]
                                               //codes[code=$code-value]
                                               /*[local-name()=$codeList-variable]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'sims'">
                <xsl:copy-of select="$sims//*[local-name()=$address-complement]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'mas'">
                <xsl:copy-of select="$msd//mas[idMas = $context//mas]//*[local-name()=$address-complement]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'simsRubrics'">
                <xsl:call-template name="get-variable-nodes">
                    <xsl:with-param name="variable-address" select="concat('identifiedSims/',$context//mas,'/',$address-complement)"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$source = 'identifiedSims'">
                <xsl:variable name="simsRubrics" select="$sims//rubrics[idAttribute = substring-before($address-complement,'/')]" as="node()*"/>
                <xsl:variable name="rubric-element" select="substring-after($address-complement,'/')"/>
                <xsl:choose>
                    <xsl:when test="not($simsRubrics//*)"/>
                    <xsl:when test="$simsRubrics//rangeType='RICH_TEXT'">
                        <xsl:apply-templates select="$simsRubrics//*[local-name()=$rubric-element]/*" mode="rich-content"/>
                        <xsl:variable name="count-links" select="count($simsRubrics//*[local-name()=replace($rubric-element,'label','documents')]/url[not(contains(tokenize(text(),'/')[last()],'.'))])"/>
                        <xsl:variable name="count-documents" select="count($simsRubrics//*[local-name()=replace($rubric-element,'label','documents')]/url[contains(tokenize(text(),'/')[last()],'.')])"/>
                        <xsl:choose>
                            <xsl:when test="$count-links = 0"/>
                            <xsl:when test="$count-links = 1 and ends-with($rubric-element,'Lg1')">
                                <text:p text:style-name="{$title-style}">Lien :</text:p>
                            </xsl:when>
                            <xsl:when test="$count-links = 1">
                                <text:p text:style-name="{$title-style}">Link:</text:p>
                            </xsl:when>
                            <xsl:when test="ends-with($rubric-element,'Lg1')">
                                <text:p text:style-name="{$title-style}">Liens :</text:p>
                            </xsl:when>
                            <xsl:otherwise>
                                <text:p text:style-name="{$title-style}">Links:</text:p>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:for-each select="$simsRubrics//*[local-name()=replace($rubric-element,'label','documents')][url[not(contains(tokenize(text(),'/')[last()],'.'))]]">
                            <xsl:element name="text:p">
                                <xsl:attribute name="text:style-name" select="$style"/>
                                <xsl:element name="text:a">
                                    <xsl:attribute name="xlink:type" select="'simple'"/>
                                    <xsl:attribute name="xlink:href" select="url"/>
                                    <xsl:choose>
                                        <xsl:when test="ends-with($rubric-element,'1')">
                                            <xsl:value-of select="labelLg1"/>
                                        </xsl:when>
                                        <xsl:when test="not(labelLg2/text()!='')">
                                            <xsl:value-of select="labelLg1"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="labelLg2"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:element>
                                <xsl:value-of select="concat(' (',lang,')')"/>
                            </xsl:element>
                            <xsl:element name="text:p">
                                <xsl:attribute name="text:style-name" select="$style"/>
                                <xsl:choose>
                                    <xsl:when test="ends-with($rubric-element,'1')">
                                        <xsl:value-of select="descriptionLg1"/>
                                    </xsl:when>
                                    <xsl:when test="not(descriptionLg2/text()!='')">
                                        <xsl:value-of select="descriptionLg1"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="descriptionLg2"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:element>
                        </xsl:for-each>
                        <xsl:choose>
                            <xsl:when test="$count-documents = 0"/>
                            <xsl:when test="$count-documents = 1 and ends-with($rubric-element,'Lg1')">
                                <text:p text:style-name="{$title-style}">Document :</text:p>
                            </xsl:when>
                            <xsl:when test="$count-documents = 1">
                                <text:p text:style-name="{$title-style}">Document:</text:p>
                            </xsl:when>
                            <xsl:when test="ends-with($rubric-element,'Lg1')">
                                <text:p text:style-name="{$title-style}">Documents :</text:p>
                            </xsl:when>
                            <xsl:otherwise>
                                <text:p text:style-name="{$title-style}">Document:</text:p>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:for-each select="$simsRubrics//*[local-name()=replace($rubric-element,'label','documents')][url[contains(tokenize(text(),'/')[last()],'.')]]">
                            <xsl:element name="text:p">
                                <xsl:attribute name="text:style-name" select="$style"/>
                                <xsl:variable name="document-language" select="lang"/>
                                <xsl:variable name="document-date" select="updatedDate"/>
                                <xsl:choose>
                                    <xsl:when test="ends-with($rubric-element,'1')">
                                        <xsl:value-of select="labelLg1"/>
                                    </xsl:when>
                                    <xsl:when test="not(labelLg2/text()!='')">
                                        <xsl:value-of select="labelLg1"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="labelLg2"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <xsl:if test="$document-language !='' or $document-date !=''">
                                    <text:span text:style-name="Italic">
                                        <xsl:value-of select="concat(' (',$document-language)"/>
                                        <xsl:if test="$document-language !='' and $document-date !=''">
                                            <xsl:value-of select="' - '"/>
                                        </xsl:if>
                                        <xsl:if test="$document-date != ''">
                                            <xsl:choose>
                                                <xsl:when test="ends-with($rubric-element,'1')">
                                                    <xsl:value-of select="concat(substring($document-date,9,2),'/',substring($document-date,6,2),'/',substring($document-date,1,4))"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="$document-date"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:if>
                                        <xsl:value-of select="')'"/>
                                    </text:span>
                                </xsl:if>
                            </xsl:element>
                            <xsl:element name="text:p">
                                <xsl:attribute name="text:style-name" select="$style"/>
                                <xsl:choose>
                                    <xsl:when test="ends-with($rubric-element,'1')">
                                        <xsl:value-of select="descriptionLg1"/>
                                    </xsl:when>
                                    <xsl:when test="not(descriptionLg2/text()!='')">
                                        <xsl:value-of select="descriptionLg1"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="descriptionLg2"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:element>
                            <text:p text:style-name="{$style}"><xsl:value-of select="tokenize(url/text(),'/')[last()]"/></text:p>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:when test="$simsRubrics//rangeType='TEXT' or $simsRubrics//rangeType='GEOGRAPHY'">
                        <xsl:variable name="original-text" select="$simsRubrics//*[local-name()=$rubric-element]"/>
                        <xsl:choose>
                            <xsl:when test="starts-with($original-text,'&lt;p&gt;')">
                                <xsl:value-of select="substring-before(substring-after($original-text,'&lt;p&gt;'),'&lt;/p&gt;')"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$original-text"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:when test="$simsRubrics//rangeType='DATE'">
                        <xsl:variable name="date" select="$simsRubrics//value/value"/>
                        <xsl:choose>
                            <xsl:when test="$rubric-element = 'labelLg1'">
                                <xsl:value-of select="concat(substring($date,9,2),'/',substring($date,6,2),'/',substring($date,1,4))"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$date"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:when test="$simsRubrics//rangeType='ORGANIZATION'">
                        <xsl:variable name="organisation" select="$simsRubrics//value/value"/>
                        <xsl:variable name="original-text" select="$organizations//item[id=$organisation]/*[local-name()=$rubric-element]"/>
                        <xsl:choose>
                            <xsl:when test="$original-text != '' and $rubric-element = 'labelLg1'">
                                <xsl:variable name="altLabel" select="$organizations//item[id=$organisation]/altLabel"/>
                                <xsl:choose>
                                    <xsl:when test="$altLabel != ''">
                                        <xsl:value-of select="concat($organisation,' : ',$original-text,' - ',$altLabel)"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="concat($organisation,' : ',$original-text)"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:when test="$original-text != ''">
                                <xsl:value-of select="concat($organisation,' : ',$original-text)"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$organisation"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:when test="$simsRubrics//rangeType='CODE_LIST'">
                        <xsl:variable name="codeList-name" select="$simsRubrics//codeList"/>
                        <xsl:variable name="code-value" select="$simsRubrics//value[not(value)]"/>
                        <xsl:choose>
                            <xsl:when test="count($codeLists//CodeList[notation=$codeList-name]
                                                            //codes[code=$code-value]
                                                             /*[local-name()=$rubric-element]) &gt; 1">
                                <text:list text:style-name="L1">
                                    <xsl:for-each select="$codeLists//CodeList[notation=$codeList-name]
                                                                    //codes[code=$code-value]
                                                                     /*[local-name()=$rubric-element]">
                                        <text:list-item>
                                            <text:p text:style-name="{$style}">
                                                <xsl:value-of select="."/>
                                            </text:p>
                                        </text:list-item>
                                    </xsl:for-each>
                                </text:list>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$codeLists//CodeList[notation=$codeList-name]
                                                                //codes[code=$code-value]
                                                                 /*[local-name()=$rubric-element]"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="'type de rubrique à définir'"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <TODO>TODO</TODO>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="text()" mode="rich-content">
        <xsl:value-of select="."/>
    </xsl:template>

    <!-- div is just a container and is not kept in odt ; all other ones have their corresponding odt tag -->
    <xsl:template match="xhtml:div" mode="rich-content">
        <xsl:apply-templates select="node()" mode="rich-content"/>
    </xsl:template>

    <xsl:template match="xhtml:p" mode="rich-content">
        <xsl:param name="style" tunnel="yes"/>
        <text:p text:style-name="{$style}">
            <xsl:apply-templates select="node()" mode="rich-content"/>
        </text:p>
    </xsl:template>

    <xsl:template match="xhtml:br" mode="rich-content">
        <text:line-break/>
    </xsl:template>

    <xsl:template match="xhtml:strong" mode="rich-content">
        <text:span text:style-name="Bold">
            <xsl:apply-templates select="node()" mode="rich-content"/>
        </text:span>
    </xsl:template>

    <xsl:template match="xhtml:em" mode="rich-content">
        <text:span text:style-name="Italic">
            <xsl:apply-templates select="node()" mode="rich-content"/>
        </text:span>
    </xsl:template>

    <xsl:template match="xhtml:ul" mode="rich-content">
        <text:list text:style-name="L1">
            <xsl:apply-templates select="node()" mode="rich-content"/>
        </text:list>
    </xsl:template>

    <xsl:template match="xhtml:ol" mode="rich-content">
        <text:list text:style-name="L2">
            <xsl:apply-templates select="node()" mode="rich-content"/>
        </text:list>
    </xsl:template>

    <xsl:template match="xhtml:li" mode="rich-content">
        <xsl:param name="style" tunnel="yes"/>
        <text:list-item>
            <text:p text:style-name="{$style}">
                <xsl:apply-templates select="node()" mode="rich-content"/>
            </text:p>
        </text:list-item>
    </xsl:template>

    <xsl:template match="xhtml:a" mode="rich-content">
        <text:a xlink:type="simple" xlink:href="{@href}">
            <xsl:apply-templates select="node()" mode="rich-content"/>
        </text:a>
    </xsl:template>

    <xsl:template match="*" mode="rich-content">
        <xsl:value-of select="concat('BeginUnknownTag ',local-name())"/>
        <xsl:apply-templates select="node()" mode="rich-content"/>
        <xsl:value-of select="concat('EndUnknownTag ',local-name())"/>
    </xsl:template>

</xsl:stylesheet>