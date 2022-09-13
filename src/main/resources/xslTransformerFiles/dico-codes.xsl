<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"  xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0" exclude-result-prefixes="#all"
    version="3.0">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

    <!-- the name of the DDI file -->
    <xsl:param name="ddi-file"/>

    <!-- its content -->
    <xsl:param name="ddi" select="document($ddi-file)"/>

    <!-- the full odt file content -->
    <xsl:variable name="odt" select="root(.)"/>

    <!-- addresses of the referenced DDI elements : check-references program may indicate keys are missing -->
    <xsl:key name="LogicalProduct"             match="$ddi/FragmentInstance/Fragment/LogicalProduct"            use="ID"/>
    <xsl:key name="RepresentedVariableScheme"  match="$ddi/FragmentInstance/Fragment/RepresentedVariableScheme" use="ID"/>
    <xsl:key name="RepresentedVariableGroup"   match="$ddi/FragmentInstance/Fragment/RepresentedVariableGroup"  use="ID"/>
    <xsl:key name="RepresentedVariable"        match="$ddi/FragmentInstance/Fragment/RepresentedVariable"       use="ID"/>
    <xsl:key name="CodeList"                   match="$ddi/FragmentInstance/Fragment/CodeList"                  use="ID"/>
    <xsl:key name="Category"                   match="$ddi/FragmentInstance/Fragment/Category"                  use="ID"/>
    <xsl:key name="Universe"                   match="$ddi/FragmentInstance/Fragment/Universe"                  use="ID"/>

    <xd:doc>
        <xd:desc>Root template</xd:desc>
    </xd:doc>
    <xsl:template match="/">
        <xsl:apply-templates select="*">
            <xsl:with-param name="ddi-fragment" select="$ddi/FragmentInstance/Fragment/*[ID = $ddi/FragmentInstance/TopLevelReference/ID]" tunnel="yes"/>
        </xsl:apply-templates>
    </xsl:template>

    <xd:doc>
        <xd:desc>Basic template : copy odt element and ask children</xd:desc>
    </xd:doc>
    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xd:doc>
        <xd:desc>odt templates are defined by their id : no entrance from their parent</xd:desc>
        <xd:desc>group titles are defined with the same structure : copy with specific styles</xd:desc>
        <xd:param name="ddi-fragment"/>
        <xd:param name="group-width"></xd:param>
    </xd:doc>
    <xsl:template match="text:h[@xml:id]">
        <xsl:param name="ddi-fragment" tunnel="yes"/>
        <xsl:param name="group-width" tunnel="yes"/>
        <xsl:if test="starts-with(@xml:id,'groupnumber')">
            <xsl:variable name="style-width">
                <xsl:choose>
                    <xsl:when test="@xml:id='groupnumber-thematique' and $group-width > 3">
                        <xsl:value-of select="4"/>
                    </xsl:when>
                    <xsl:when test="@xml:id='groupnumber-thematique'">
                        <xsl:value-of select="$group-width +1"/>
                    </xsl:when>
                    <xsl:when test="$group-width > 4">
                        <xsl:value-of select="4"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$group-width"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:copy>
                <xsl:attribute name="text:style-name" select="concat('P',$style-width)"/>
                <xsl:attribute name="text:outline-level" select="$style-width"/>
                <xsl:apply-templates select="*">
                    <xsl:with-param name="ddi-fragment" select="$ddi-fragment" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:copy>
        </xsl:if>
    </xsl:template>

    <xd:doc>
        <xd:desc>Call of odt templates for all corresponding ddi fragments</xd:desc>
        <xd:param name="ddi-fragment"/>
        <xd:param name="group-width">manage variableGroup </xd:param>
    </xd:doc>
    <xsl:template match="text:sequence-ref">
        <xsl:param name="ddi-fragment" tunnel="yes"/>
        <xsl:param name="group-width" select="0" tunnel="yes"/>

        <xsl:variable name="referencing-text" select="text()"/>
        <!-- Example with referencing-text =                        CodeRepresentation/CodeList-Thematique
            It calls the children of the odt template named                            CodeList-Thematique
            for all the CodeList fragments with ID in $ddi-fragment/CodeRepresentation/CodeListReference/ID -->
        <xsl:variable name="odt-template-name" select="tokenize($referencing-text,'/')[last()]"/>
        <xsl:variable name="fragment-type" select="tokenize($odt-template-name,'-')[1]"/>
        <xsl:variable name="referencing-fragments" as="node()*">
            <xsl:apply-templates select="$ddi-fragment" mode="evaluate-ddi">
                <xsl:with-param name="xpath-string" select="concat(tokenize($referencing-text,'-')[1],'Reference')"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:for-each select="$referencing-fragments">
            <xsl:variable name="referenced-fragment">
                <xsl:apply-templates select="$ddi" mode="get-referenced">
                    <xsl:with-param name="referencing-fragment" select="."/>
                </xsl:apply-templates>
            </xsl:variable>
            <xsl:apply-templates select="$odt//text:h[@xml:id=$odt-template-name]/*">
                <xsl:with-param name="ddi-fragment" select="$referenced-fragment" tunnel="yes"/>
                <xsl:with-param name="group-width" select="if ($fragment-type='RepresentedVariableGroup') then $group-width +1 else $group-width" tunnel="yes"/>
            </xsl:apply-templates>
        </xsl:for-each>
    </xsl:template>

    <xd:doc>
        <xd:desc>gets the referenced DDI fragment from the referencing one. Needs to be called from the ddi's namespace</xd:desc>
        <xd:param name="referencing-fragment"/>
    </xd:doc>
    <xsl:template match="*" mode="get-referenced">
        <xsl:param name="referencing-fragment"/>
        <xsl:copy-of select="key($referencing-fragment/TypeOfObject,$referencing-fragment/ID)"/>
    </xsl:template>

    <xd:doc>
        <xd:desc>Filters using text:p with id</xd:desc>
        <xd:param name="ddi-fragment"/>
    </xd:doc>
    <xsl:template match="text:p[@xml:id]">
        <xsl:param name="ddi-fragment" tunnel="yes"/>
        <xsl:variable name="current" select="."/>
        <xsl:variable name="id" select="@xml:id"/>
        <!-- in the id, the '/' are replaced by '-'
             it may start with NOT_ or ANY_ -->
        <xsl:variable name="path-content" as="node() *">
            <xsl:apply-templates select="$ddi-fragment" mode="evaluate-ddi">
                <xsl:with-param name="xpath-string" select="replace(
                    replace(
                    replace($id,'-','/'),
                    'NOT_',''),
                    'ANY_','')"/>
            </xsl:apply-templates>
        </xsl:variable>
        <!-- if starting with "ANY_" and ANY element corresponding to the XPath, applies the odt's content with the current XPath -->
        <xsl:if test="starts-with($id,'NOT_') and not($path-content[1] != 'false')">
            <xsl:apply-templates select="* | text()"/>
        </xsl:if>
        <!-- if starting with "NOT_" and NO element corresponding to the XPath, applies the odt's content with the current XPath -->
        <xsl:if test="starts-with($id,'ANY_') and $path-content[1] != 'false'">
            <xsl:apply-templates select="* | text()"/>
        </xsl:if>
        <!-- if not starting with "NOT_" or "ANY_", for each element corresponding to the XPath, applies the odt's content with the corresponding XPath -->
        <xsl:if test="not(starts-with($id,'NOT_')) and not(starts-with($id,'ANY_')) and $path-content[1] != 'false'">
            <xsl:for-each select="$path-content">
                <xsl:apply-templates select="$current/* | $current/text()">
                    <xsl:with-param name="ddi-fragment" select="." tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <xd:doc>
        <xd:desc>text:p elements containing ${referenceToData} are filled with the referenced data</xd:desc>
        <xd:desc>referenced data may contain paragraphs with their own style or simple strings</xd:desc>
        <xd:param name="ddi-fragment"/>
    </xd:doc>
    <xsl:template match="text()[contains(.,'${')]">
        <xsl:param name="ddi-fragment" tunnel="yes"/>
        <xsl:variable name="xpath-string" select="substring-before(substring-after(.,'${'),'}')"/>
        <xsl:variable name="ddi-content">
            <xsl:apply-templates select="$ddi-fragment" mode="evaluate-ddi">
                <xsl:with-param name="xpath-string" select="$xpath-string"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:variable name="after-ddi">
            <xsl:apply-templates select="substring-after(.,'}')"/>
        </xsl:variable>
        <xsl:value-of select="substring-before(.,'${')"/>
        <!-- Replace DDI line-break with odt line-break -->
        <xsl:for-each select="tokenize($ddi-content,'\n')">
            <xsl:if test="position() &gt; 1">
                <text:line-break/>
            </xsl:if>
            <xsl:value-of select="."/>
        </xsl:for-each>
        <xsl:value-of select="$after-ddi"/>
    </xsl:template>

    <xd:doc>
        <xd:desc>text:p elements containing ${referenceToData} are filled with the referenced data</xd:desc>
        <xd:desc>referenced data may contain paragraphs with their own style or simple strings</xd:desc>
        <xd:param name="ddi-fragment"/>
    </xd:doc>
    <xsl:template match="@*[contains(.,'${')]">
        <xsl:param name="ddi-fragment" tunnel="yes"/>
        <xsl:variable name="xpath-string" select="substring-before(substring-after(.,'${'),'}')"/>
        <xsl:variable name="ddi-content">
            <xsl:apply-templates select="$ddi-fragment" mode="evaluate-ddi">
                <xsl:with-param name="xpath-string" select="$xpath-string"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:attribute name="{name()}" select="concat(substring-before(.,'${'),$ddi-content,substring-after(.,'}'))"/>
    </xsl:template>

    <xd:doc>
        <xd:desc>Evaluate xpath in DDI</xd:desc>
        <xd:param name="xpath-string">the string to evaluate</xd:param>
    </xd:doc>
    <xsl:template match="*" mode="evaluate-ddi">
        <xsl:param name="xpath-string"/>
        <xsl:choose>
            <xsl:when test="contains($xpath-string,'/')">
                <xsl:apply-templates select="*[local-name()=substring-before($xpath-string,'/')]" mode="evaluate-ddi">
                    <xsl:with-param name="xpath-string" select="substring-after($xpath-string,'/')"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$xpath-string = '.'">
                <xsl:value-of select="."/>
            </xsl:when>
            <xsl:when test="contains($xpath-string,'_')">
                <xsl:variable name="split-xpath" select="tokenize($xpath-string,'_')"/>
                <xsl:choose>
                    <!-- element_lang__value means element[@lang = value-VALUE]/text() -->
                    <xsl:when test="$split-xpath[4] and $split-xpath[2] = 'lang'">
                        <xsl:value-of select="*[local-name()=$split-xpath[1] and @lang[tokenize(.,'-')[1]=$split-xpath[4]]]"/>
                    </xsl:when>
                    <!-- element_attribute__value means element[@attribute = value]/text() -->
                    <xsl:when test="$split-xpath[4] and $split-xpath[1] != ''">
                        <xsl:value-of select="*[local-name()=$split-xpath[1] and @*[local-name()=$split-xpath[2] and .=$split-xpath[4]]]"/>
                    </xsl:when>
                    <!-- _attribute__value means @attribute = value ? -->
                    <xsl:when test="$split-xpath[4]">
                        <xsl:value-of select="@*[local-name()=$split-xpath[2]]=$split-xpath[4]"/>
                    </xsl:when>
                    <!-- element__value means element = value ? -->
                    <xsl:when test="$split-xpath[3]">
                        <xsl:value-of select="*[local-name()=$split-xpath[1]]=$split-xpath[3]"/>
                    </xsl:when>
                    <!-- element_attribute means element[@attribute]/text() -->
                    <xsl:when test="$split-xpath[1] != '' and $split-xpath[2]">
                        <xsl:choose>
                            <xsl:when test="*[local-name()=$split-xpath[1]]/@*[local-name()=$split-xpath[2]]">
                                <xsl:value-of select="*[local-name()=$split-xpath[1]]/@*[local-name()=$split-xpath[2]]"/>        
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="'false'"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <!-- _attribute means @attribute/text() -->
                    <xsl:when test="$split-xpath[2]">
                        <xsl:choose>
                            <xsl:when test="@*[local-name()=$split-xpath[2]]">
                                <xsl:value-of select="@*[local-name()=$split-xpath[2]]"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="'false'"/>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:value-of select="@*[local-name()=$split-xpath[2]]"/>
                    </xsl:when>
                    <!-- element means element/text() -->
                    <xsl:otherwise>
                        <xsl:value-of select="*[local-name()=$split-xpath[1]]"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="*[local-name()=$xpath-string]"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
