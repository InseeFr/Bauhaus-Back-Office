<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    xmlns:saxon="http://saxon.sf.net/"
    xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0" xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
    xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0" xmlns:officeooo="http://openoffice.org/2009/office"
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
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="office:automatic-styles">
        <xsl:copy>
            <parameters>
                <xsl:copy-of select="$parameters"/>
            </parameters>
            <seriesFile>
                <xsl:copy-of select="$seriesFile"/>
            </seriesFile>
            <operationFile>
                <xsl:copy-of select="$operationFile"/>
            </operationFile>
            <indicatorFile>
                <xsl:copy-of select="$indicatorFile"/>
            </indicatorFile>
            <simsFile>
                <xsl:copy-of select="$simsFile"/>
            </simsFile>
            <organizationsFile>
                <xsl:copy-of select="$organizationsFile"/>
            </organizationsFile>
            <codeListsFile>
                <xsl:copy-of select="$codeListsFile"/>
            </codeListsFile>
            <msdFile>
                <xsl:copy-of select="$msdFile"/>
            </msdFile>
            <series>
                <xsl:copy-of select="$series"/>
            </series>
            <operation>
                <xsl:copy-of select="$operation"/>
            </operation>
            <indicator>
                <xsl:copy-of select="$indicator"/>
            </indicator>
            <sims>
                <xsl:copy-of select="$sims"/>
            </sims>
            <organizations>
                <xsl:copy-of select="$organizations"/>
            </organizations>
            <codeLists>
                <xsl:copy-of select="$codeLists"/>
            </codeLists>
            <msd>
                <xsl:copy-of select="$msd"/>
            </msd>
            <xsl:apply-templates select="node() | @*"/>
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
            <xsl:if test="$parameters//includeEmptyMas = 'true'
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
        <xd:desc>elements with @id are used to filter the pattern, depending on parameters or </xd:desc>
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
                            <xsl:value-of select="upper-case($parameters//targetType) = upper-case($parameter-value)"/>
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
                    <xsl:choose>
                        <xsl:when test="self::text:p">
                            <xsl:variable name="new-text">
                                <xsl:call-template name="personalize-text">
                                    <xsl:with-param name="text-to-personalize" select="text()"/>
                                    <xsl:with-param name="style" select="@text:style-name" tunnel="yes"/>
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
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:copy>
                                <xsl:apply-templates select="@*[not(name()='xml:id')]|node()"/>
                            </xsl:copy>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat('identifiant inconnu : ',@xml:id)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="text:p[contains(text(),'${')]">
        <xsl:variable name="new-text">
            <xsl:call-template name="personalize-text">
                <xsl:with-param name="text-to-personalize" select="text()"/>
                <xsl:with-param name="style" select="@text:style-name" tunnel="yes"/>
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

    <xsl:template name="personalize-text">
        <xsl:param name="text-to-personalize"/>
        <xsl:param name="style" tunnel="yes"/>
        <xsl:param name="context" as="node()" tunnel="yes"/>

        <!-- $1 #if ($2 ='$4') $5 #if ($6='$8') $9 #endif #endif $10 ... or != instead of = -->
        <xsl:analyze-string select="$text-to-personalize" regex="^(.*)#if \((.*)\)(.*)#if \((.*)\)(.)*#endif(.)*#endif(.*)$">
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
                <xsl:choose>
                    <xsl:when test="not($test1-text != '')">
                        <xsl:call-template name="personalize-text">
                            <xsl:with-param name="text-to-personalize" select="regex-group(1)"/>
                        </xsl:call-template>
                        <xsl:call-template name="personalize-text">
                            <xsl:with-param name="text-to-personalize" select="regex-group(7)"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="not($test2-text != '')">
                        <xsl:call-template name="personalize-text">
                            <xsl:with-param name="text-to-personalize" select="regex-group(1)"/>
                        </xsl:call-template>
                        <xsl:call-template name="personalize-text">
                            <xsl:with-param name="text-to-personalize" select="regex-group(3)"/>
                        </xsl:call-template>
                        <xsl:call-template name="personalize-text">
                            <xsl:with-param name="text-to-personalize" select="regex-group(6)"/>
                        </xsl:call-template>
                        <xsl:call-template name="personalize-text">
                            <xsl:with-param name="text-to-personalize" select="regex-group(7)"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="personalize-text">
                            <xsl:with-param name="text-to-personalize" select="regex-group(1)"/>
                        </xsl:call-template>
                        <xsl:call-template name="personalize-text">
                            <xsl:with-param name="text-to-personalize" select="regex-group(3)"/>
                        </xsl:call-template>
                        <xsl:call-template name="personalize-text">
                            <xsl:with-param name="text-to-personalize" select="regex-group(5)"/>
                        </xsl:call-template>
                        <xsl:call-template name="personalize-text">
                            <xsl:with-param name="text-to-personalize" select="regex-group(6)"/>
                        </xsl:call-template>
                        <xsl:call-template name="personalize-text">
                            <xsl:with-param name="text-to-personalize" select="regex-group(7)"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:matching-substring>
            <xsl:non-matching-substring>
                <!-- $1 #if ($2 ='$4') $5 #endif $6 ... or "!=" instead of "=" -->
                <xsl:analyze-string select="$text-to-personalize" regex="^(.*)#if \((.*)\)(.*)#endif(.*)$">
                    <xsl:matching-substring>
                        <xsl:variable name="test1-text">
                            <xsl:call-template name="get-variable-nodes">
                                <xsl:with-param name="variable-address" select="substring-before(substring-after(regex-group(2),'${'),'}')"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:choose>
                            <xsl:when test="not($test1-text != '')">
                                <xsl:call-template name="personalize-text">
                                    <xsl:with-param name="text-to-personalize" select="regex-group(1)"/>
                                </xsl:call-template>
                                <xsl:call-template name="personalize-text">
                                    <xsl:with-param name="text-to-personalize" select="regex-group(4)"/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:call-template name="personalize-text">
                                    <xsl:with-param name="text-to-personalize" select="regex-group(1)"/>
                                </xsl:call-template>
                                <xsl:call-template name="personalize-text">
                                    <xsl:with-param name="text-to-personalize" select="regex-group(3)"/>
                                </xsl:call-template>
                                <xsl:call-template name="personalize-text">
                                    <xsl:with-param name="text-to-personalize" select="regex-group(4)"/>
                                </xsl:call-template>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:matching-substring>
                    <xsl:non-matching-substring>
                        <!-- $1 #list ($2,'$3') $4 -->
                        <xsl:analyze-string select="$text-to-personalize" regex="^(.*)#list\((.*),'(.*)'\)(.*)$">
                            <xsl:matching-substring>
                                <xsl:variable name="list-content">
                                    <xsl:call-template name="personalize-text">
                                        <xsl:with-param name="text-to-personalize" select="regex-group(1)"/>
                                    </xsl:call-template>
                                    <xsl:variable name="listed-variables" as="xs:string *">
                                        <xsl:call-template name="get-variable-nodes">
                                            <xsl:with-param name="variable-address" select="substring-before(substring-after(regex-group(2),'${'),'}')"/>
                                        </xsl:call-template>
                                    </xsl:variable>
                                    <xsl:if test="$listed-variables != ''">
                                        <text:list text:style-name="{regex-group(3)}">
                                            <xsl:for-each select="$listed-variables">
                                                <text:list-item>
                                                    <text:p>
                                                        <xsl:value-of select="."/>
                                                    </text:p>
                                                </text:list-item>
                                            </xsl:for-each>
                                        </text:list>
                                    </xsl:if>
                                    <xsl:call-template name="personalize-text">
                                        <xsl:with-param name="text-to-personalize" select="regex-group(4)"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:copy-of select="$list-content"/>
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
        <xsl:param name="context" as="node()" tunnel="yes"/>

        <xsl:variable name="source" select="substring-before($variable-address,'.')"/>
        <xsl:variable name="address-complement" select="substring-after($variable-address,'.')"/>

        <xsl:choose>
            <xsl:when test="$source = 'series' and not(contains($address-complement,'.'))">
                <xsl:copy-of select="$series//*[local-name()=$address-complement]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'series'">
                <xsl:copy-of select="$series//*[local-name()=substring-before($address-complement,'.')]//*[local-name()=substring-after($address-complement,'.')]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'operation' and not(contains($address-complement,'.'))">
                <xsl:copy-of select="$operation//*[local-name()=$address-complement]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'operation'">
                <xsl:copy-of select="$operation//*[local-name()=substring-before($address-complement,'.')]//*[local-name()=substring-after($address-complement,'.')]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'indicator' and not(contains($address-complement,'.'))">
                <xsl:copy-of select="$indicator//*[local-name()=$address-complement]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'indicator'">
                <xsl:copy-of select="$indicator//*[local-name()=substring-before($address-complement,'.')]//*[local-name()=substring-after($address-complement,'.')]/text()"/>
            </xsl:when>
            <xsl:when test="$source = 'seriesCode'">
                <xsl:variable name="series-variable" select="substring-before($address-complement,'.')"/>
                <xsl:variable name="codeList-variable" select="substring-after($address-complement,'.')"/>
                <xsl:variable name="codeList-name" select="$series//*[local-name() = concat($series-variable,'List')]"/>
                <xsl:variable name="code-value" select="$series//*[local-name() = concat($series-variable,'Code')]"/>
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
                <xsl:variable name="simsRubrics" select="$sims//rubrics[idAttribute = $context//mas]" as="node()*"/>
                <xsl:choose>
                    <xsl:when test="not($simsRubrics//*)"/>
                    <xsl:when test="$simsRubrics//rangeType='RICH_TEXT'">
                        <xsl:apply-templates select="$simsRubrics//*[local-name()=$address-complement]/*" mode="rich-text"/>
                    </xsl:when>
                    <xsl:when test="$simsRubrics//rangeType='TEXT' or $simsRubrics//rangeType='GEOGRAPHY'">
                        <xsl:variable name="original-text" select="$simsRubrics//*[local-name()=$address-complement]"/>
                        <xsl:choose>
                            <xsl:when test="starts-with($original-text,'&lt;p&gt;')">
                                <xsl:value-of select="substring-before(substring-after($original-text,'&lt;p&gt;'),'&lt;/p&gt;')"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$original-text"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:when test="$simsRubrics//rangeType='ORGANIZATION'">
                        <xsl:variable name="organisation" select="$simsRubrics//value/value"/>
                        <xsl:variable name="original-text" select="$organizations//item[id=$organisation]/*[local-name()=$address-complement]"/>
                        <xsl:choose>
                            <xsl:when test="$original-text != '' and $address-complement = 'labelLg1'">
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
                        <xsl:variable name="code-value" select="$simsRubrics//value"/>
                        <xsl:value-of select="$codeLists//CodeList[notation=$codeList-name]
                            //codes[code=$code-value]
                            /*[local-name()=$address-complement]"/>
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

    <xsl:template match="p" mode="rich-text">
        <xsl:param name="style" tunnel="yes"/>
        <text:p text:style-name="{$style}">
            <xsl:apply-templates select="node()|text()" mode="rich-text"/>
        </text:p>
    </xsl:template>
    <xsl:template match="ol" mode="rich-text">
        <text:list text:style-name="L2">
            <xsl:apply-templates select="*|text()" mode="rich-text"/>
        </text:list>
    </xsl:template>
    <xsl:template match="ul" mode="rich-text">
        <text:list text:style-name="L1">
            <xsl:apply-templates select="*|text()" mode="rich-text"/>
        </text:list>
    </xsl:template>
    <xsl:template match="li" mode="rich-text">
        <xsl:param name="style"/>
        <text:list-item>
            <text:p text:style-name="{$style}">
                <xsl:apply-templates select="*|text()" mode="rich-text"/>    
            </text:p>
        </text:list-item>
    </xsl:template>
    <xsl:template match="strong" mode="rich-text">
        <text:span text:style-name="Bold">
            <xsl:apply-templates select="*|text()" mode="rich-text"/>
        </text:span>
    </xsl:template>
    <xsl:template match="em" mode="rich-text">
        <text:span text:style-name="Italic">
            <xsl:apply-templates select="*|text()" mode="rich-text"/>
        </text:span>
    </xsl:template>
    <xsl:template match="br" mode="rich-text" priority="1">
        <text:line-break/>
    </xsl:template>
    <xsl:template match="node()" mode="rich-text" priority="-1">
        <xsl:value-of select="concat('début balise ',name())"/>
        <xsl:apply-templates select="*|text()" mode="rich-text"/>
        <xsl:value-of select="concat('fin balise ',name())"/>
    </xsl:template>

    <xsl:template match="text()" mode="rich-text">
        <xsl:value-of select="."/>
    </xsl:template>
</xsl:stylesheet>