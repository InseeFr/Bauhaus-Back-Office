<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dereferencing="dereferencing" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    exclude-result-prefixes="#all" version="3.0">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

    <!-- the full odt file content -->
    <xsl:variable name="ddi" select="root(.)"/>

    <!-- List of the ...Reference elements of DDI that reference a needed fragment -->
    <xsl:variable name="follow-references">
        <dereferencing:reference-name name="LogicalProductReference"/>
        <dereferencing:reference-name name="RepresentedVariableSchemeReference"/>
        <dereferencing:reference-name name="RepresentedVariableGroupReference"/>
        <dereferencing:reference-name name="RepresentedVariableReference"/>
        <dereferencing:reference-name name="CategoryReference"/>
        <dereferencing:reference-name name="CodeListReference"/>
        <dereferencing:reference-name name="UniverseReference"/>
    </xsl:variable>

    <!-- List of the ...Reference elements of DDI that are not usable to follow -->
    <xsl:variable name="ignore-references">
        <dereferencing:reference-name name="BasedOnReference"/>
    </xsl:variable>

    <!-- addresses of the referenced DDI elements -->
    <xsl:key name="LogicalProductReference-_-LogicalProduct"                        match="$ddi/FragmentInstance/Fragment/LogicalProduct"            use="ID"/>
    <xsl:key name="RepresentedVariableSchemeReference-_-RepresentedVariableScheme"  match="$ddi/FragmentInstance/Fragment/RepresentedVariableScheme" use="ID"/>
    <xsl:key name="RepresentedVariableReference-_-RepresentedVariable"              match="$ddi/FragmentInstance/Fragment/RepresentedVariable"       use="ID"/>
    <xsl:key name="RepresentedVariableGroupReference-_-RepresentedVariableGroup"    match="$ddi/FragmentInstance/Fragment/RepresentedVariableGroup"  use="ID"/>
    <xsl:key name="CategoryReference-_-Category"                                    match="$ddi/FragmentInstance/Fragment/Category"                  use="ID"/>
    <xsl:key name="CodeListReference-_-CodeList"                                    match="$ddi/FragmentInstance/Fragment/CodeList"                  use="ID"/>
    <xsl:key name="UniverseReference-_-Universe"                                    match="$ddi/FragmentInstance/Fragment/Universe"                  use="ID"/>

    <!-- logical links between the DDI referencing and referenced elements -->
    <xsl:variable name="key-names">
        <dereferencing:key-name name='LogicalProductReference-_-LogicalProduct' name1='LogicalProductReference' name2='LogicalProduct'/>
        <dereferencing:key-name name='RepresentedVariableSchemeReference-_-RepresentedVariableScheme' name1='RepresentedVariableSchemeReference' name2='RepresentedVariableScheme'/>
        <dereferencing:key-name name='RepresentedVariableReference-_-RepresentedVariable' name1='RepresentedVariableReference' name2='RepresentedVariable'/>
        <dereferencing:key-name name='RepresentedVariableGroupReference-_-RepresentedVariableGroup' name1='RepresentedVariableGroupReference' name2='RepresentedVariableGroup'/>
        <dereferencing:key-name name='CategoryReference-_-Category' name1='CategoryReference' name2='Category'/>
        <dereferencing:key-name name='CodeListReference-_-CodeList'  name1='CodeListReference' name2='CodeList'/>
        <dereferencing:key-name name='UniverseReference-_-Universe'  name1='UniverseReference' name2='Universe'/>
    </xsl:variable>

    <!-- Typology of the error messages for incorrect DDI -->
    <xsl:variable name="message-label">
        <dereferencing:message type-number="1" message-order="1">
            <dereferencing:label>Unknown Reference tag</dereferencing:label>
            <dereferencing:modifyXSLcode>Modify "follow-references" or "ignore references" variable, by adding the unknown reference tag. One or more key is also needed, see other messages.</dereferencing:modifyXSLcode>
        </dereferencing:message>
        <dereferencing:message type-number="2" message-order="2">
            <dereferencing:label>Missing ID node</dereferencing:label>
            <dereferencing:modifyDDIcode>Add non empty ID tag to the tag described in the where section</dereferencing:modifyDDIcode>
        </dereferencing:message>
        <dereferencing:message type-number="3" message-order="3">
            <dereferencing:label>Non-unique ID found with xsl:key</dereferencing:label>
            <dereferencing:modifyDDIcode>Find the incorrect ID and modify it in the DDI code.</dereferencing:modifyDDIcode>
        </dereferencing:message>
        <dereferencing:message type-number="4" message-order="4">
            <dereferencing:label>ID not found with xsl:key but found with global search</dereferencing:label>
            <dereferencing:modifyDDIcode>There is either a problem in the reference node or in the target node (or in both) : the type of object or the reference tag is incorrect in one of the node</dereferencing:modifyDDIcode>
        </dereferencing:message>
        <dereferencing:message type-number="5" message-order="5">
            <dereferencing:label>xsl:key creation requested</dereferencing:label>
            <dereferencing:modifyXSLcode>There is no key for this combination of reference and type of object.
                It is therefore necessary to create a xsl:key node with the proposed name, match and use (in value node).
                Do not forget to update the key-names variable using name, name1 and name2 in value node : it will not work if you do not do it.</dereferencing:modifyXSLcode>
        </dereferencing:message>
        <dereferencing:message type-number="6" message-order="6">
            <dereferencing:label>Type mismatch</dereferencing:label>
            <dereferencing:modifyDDIcode>The type of object in the reference node and the tag of the target node does node match.</dereferencing:modifyDDIcode>
        </dereferencing:message>
        <dereferencing:message type-number="7" message-order="7">
            <dereferencing:label>Non-unique ID found with global search</dereferencing:label>
            <dereferencing:modifyDDIcode>A key search of the ID was not possible, and a global search of the ID in the document brought more than one match back.</dereferencing:modifyDDIcode>
        </dereferencing:message>
        <dereferencing:message type-number="8" message-order="8">
            <dereferencing:label>ID not found with xsl:key and global search</dereferencing:label>
            <dereferencing:modifyDDIcode>A key search then a global search in the document brought no match back.</dereferencing:modifyDDIcode>
        </dereferencing:message>
        <dereferencing:message type-number="9" message-order="9">
            <dereferencing:label>ID not found with global search</dereferencing:label>
            <dereferencing:modifyDDIcode>A key search of the ID was not possible, and a global search in the document brought no match back.</dereferencing:modifyDDIcode>
        </dereferencing:message>
    </xsl:variable>

    <!-- list of the error messages for layout -->
    <xsl:variable name="messages-all">
        <xsl:apply-templates select="$ddi/FragmentInstance/Fragment/*[ID = $ddi/FragmentInstance/TopLevelReference/ID]" mode="error-message"/>
    </xsl:variable>

    <xd:doc>
        <xd:desc>Root template</xd:desc>
    </xd:doc>
    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="$messages-all/*">
                <KO>
                    <xsl:call-template name="error-layout"/>
                </KO>
            </xsl:when>
            <xsl:otherwise>
                <OK/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xd:doc>
        <xd:desc>Default output template : call of the children</xd:desc>
    </xd:doc>
    <xsl:template match="*" mode="error-message">
        <xsl:apply-templates select="*" mode="error-message"/>
    </xsl:template>

    <xd:doc>
        <xd:desc>Template for Reference tags</xd:desc>
    </xd:doc>
    <xsl:template match="node()[ends-with(name(.), 'Reference')]" mode="error-message">
        <xsl:variable name="copy-node-name" select="name(.)"/>
        <xsl:choose>
            <xsl:when test="count($follow-references/dereferencing:reference-name[@name=$copy-node-name])!=0">
                <xsl:call-template name="xxx-Reference-ID"/>
                <xsl:apply-templates select="./*[name() != 'ID' and name() != 'Agency']" mode="error-message"/>
            </xsl:when>
            <xsl:when test="count($ignore-references/dereferencing:reference-name[@name=$copy-node-name])!=0">
                <xsl:apply-templates select="*" mode="error-message"/>
            </xsl:when>
            <xsl:otherwise>
                <dereferencing:warning>
                    <dereferencing:type-number>1</dereferencing:type-number>
                    <dereferencing:value><xsl:value-of select="$copy-node-name"/></dereferencing:value>
                    <xsl:call-template name="where-message"/>
                </dereferencing:warning>
                <xsl:call-template name="xxx-Reference-ID"/>
                <xsl:apply-templates select="./*[name() != 'ID' and name() != 'Agency']" mode="error-message"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xd:doc>
        <xd:desc>Base template for searched References</xd:desc>
    </xd:doc>
    <xsl:template name="xxx-Reference-ID">
        <xsl:choose>
            <xsl:when test="count(ID)=0">
                <dereferencing:warning>
                    <dereferencing:type-number>2</dereferencing:type-number>
                    <xsl:call-template name="where-message"/>
                </dereferencing:warning>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="$key-names/dereferencing:key-name[@name1=name(current()) and @name2=current()/TypeOfObject]">
                        <xsl:variable name="result-key">
                            <xsl:copy-of select="key(concat(name(.),'-_-',TypeOfObject),ID)"/>
                        </xsl:variable>
                        <xsl:choose>
                            <xsl:when test="count($result-key/*)=1">
                                <xsl:apply-templates select="key(concat(name(.),'-_-',TypeOfObject),ID)" mode="error-message"/>
                            </xsl:when>
                            <xsl:when test="count($result-key/*)>1">
                                <dereferencing:warning>
                                    <dereferencing:type-number>3</dereferencing:type-number>
                                    <xsl:call-template name="where-message"/>
                                </dereferencing:warning>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:call-template name="find-id-without-key">
                                    <xsl:with-param name="key-existence" select="true()"/>
                                </xsl:call-template>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="find-id-without-key">
                            <xsl:with-param name="key-existence" select="false()"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xd:doc>
        <xd:desc>Template to find an id with a global search.</xd:desc>
        <xd:param name="key-existence"/>
    </xd:doc>
    <xsl:template name="find-id-without-key">
        <xsl:param name="key-existence"/>
        <xsl:variable name="current-id" select="ID"/>
        <xsl:variable name="current-name" select="name(.)"/>
        <xsl:variable name="current-type" select="TypeOfObject"/>
        <xsl:variable name="reference-targets"><xsl:copy-of select="//*[ID=$current-id and name(.) != $current-name]"/></xsl:variable>

        <xsl:choose>
            <xsl:when test="count($reference-targets/*) = 1">
                <xsl:variable name="reference-targets-parent-name" select="name((//*[ID=$current-id and name(.) != $current-name])[1]/..)"/>
                <xsl:variable name="reference-targets-name" select="name((//*[ID=$current-id and name(.) != $current-name])[1])"/>
                <xsl:variable name="reference-targets-local-name" select="local-name((//*[ID=$current-id and name(.) != $current-name])[1])"/>
                <dereferencing:warning>
                    <xsl:choose>
                        <xsl:when test="$key-existence=true()">
                            <dereferencing:type-number>4</dereferencing:type-number>
                            <dereferencing:reference-target-comparison>
                                <dereferencing:reference><xsl:value-of select="$current-name"/></dereferencing:reference>
                                <dereferencing:target-scheme><xsl:value-of select="$reference-targets-parent-name"/></dereferencing:target-scheme>
                                <dereferencing:type-of-object><xsl:value-of select="$current-type"/></dereferencing:type-of-object>
                                <dereferencing:target-type><xsl:value-of select="$reference-targets-name"/></dereferencing:target-type>
                                <dereferencing:ID><xsl:value-of select="$current-id"/></dereferencing:ID>
                            </dereferencing:reference-target-comparison>
                            <xsl:call-template name="where-message"/>
                        </xsl:when>
                        <xsl:when test="$key-existence=false()">
                            <xsl:choose>
                                <xsl:when test="$reference-targets-local-name = $current-type">
                                    <dereferencing:type-number>5</dereferencing:type-number>
                                    <dereferencing:value>
                                        <dereferencing:name><xsl:value-of select="concat($current-name,'-_-',$current-type)"/></dereferencing:name>
                                        <dereferencing:match><xsl:value-of select="concat('/DDIInstance/ResourcePackage/',$reference-targets-parent-name,'/',$reference-targets-name)"/></dereferencing:match>
                                        <dereferencing:use><xsl:value-of select="'ID'"/></dereferencing:use>
                                        <dereferencing:name1><xsl:value-of select="$current-name"/></dereferencing:name1>
                                        <dereferencing:name2><xsl:value-of select="$current-type"/></dereferencing:name2>
                                    </dereferencing:value>
                                    <xsl:call-template name="where-message"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <dereferencing:type-number>6</dereferencing:type-number>
                                    <dereferencing:reference-target-comparison>
                                        <dereferencing:reference><xsl:value-of select="$current-name"/></dereferencing:reference>
                                        <dereferencing:target-scheme><xsl:value-of select="$reference-targets-parent-name"/></dereferencing:target-scheme>
                                        <dereferencing:type-of-object><xsl:value-of select="$current-type"/></dereferencing:type-of-object>
                                        <dereferencing:target-type><xsl:value-of select="$reference-targets-name"/></dereferencing:target-type>
                                        <dereferencing:ID><xsl:value-of select="$current-id"/></dereferencing:ID>
                                    </dereferencing:reference-target-comparison>
                                    <xsl:call-template name="where-message"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise/>
                    </xsl:choose>
                </dereferencing:warning>
                <xsl:apply-templates select="//*[ID=$current-id and name(.) != $current-name]" mode="error-message"/>
            </xsl:when>
            <xsl:otherwise>
                <dereferencing:warning>
                    <xsl:choose>
                        <xsl:when test="count($reference-targets/*) > 1">
                            <dereferencing:type-number>7</dereferencing:type-number>
                            <xsl:call-template name="where-message"/>
                        </xsl:when>
                        <xsl:when test="$key-existence=true()">
                            <dereferencing:type-number>8</dereferencing:type-number>
                            <xsl:call-template name="where-message"/>
                        </xsl:when>
                        <xsl:when test="$key-existence=false()">
                            <dereferencing:type-number>9</dereferencing:type-number>
                            <xsl:call-template name="where-message"/>
                        </xsl:when>
                    </xsl:choose>
                </dereferencing:warning>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xd:doc>
        <xd:desc>Writing where the message comes from.</xd:desc>
    </xd:doc>
    <xsl:template name="where-message">
        <dereferencing:where>
            <dereferencing:current-id><xsl:value-of select="./ID"/></dereferencing:current-id>
            <dereferencing:current-name><xsl:value-of select="name(.)"/></dereferencing:current-name>
            <dereferencing:current-type><xsl:value-of select="./TypeOfObject"/></dereferencing:current-type>
            <dereferencing:parent-id><xsl:value-of select="../ID"/></dereferencing:parent-id>
            <dereferencing:parent-name><xsl:value-of select="name(..)"/></dereferencing:parent-name>
            <dereferencing:parent-type><xsl:value-of select="../TypeOfObject"/></dereferencing:parent-type>
        </dereferencing:where>
    </xsl:template>

    <xd:doc>
        <xd:desc>Layout of error messages</xd:desc>
    </xd:doc>
    <xsl:template name="error-layout">
        <xsl:for-each select="$message-label/*" >
            <xsl:sort select="number(dereferencing:message-order)"/>
            <xsl:if test="$messages-all/*[dereferencing:type-number=current()/@type-number]">
                <xsl:copy>
                    <xsl:copy-of select="./@* | ./node() |text()"/>
                    <xsl:choose>
                        <xsl:when test="dereferencing:modifyXSLcode">
                            <xsl:for-each-group select="$messages-all/*[./dereferencing:type-number=current()/@type-number]" group-by="./dereferencing:value">
                                <xsl:sort select="./dereferencing:value"/>
                                <dereferencing:value>
                                    <xsl:copy-of select="./dereferencing:value/*"/>
                                    <dereferencing:where-list>
                                        <xsl:for-each-group select="current-group()" group-by="dereferencing:where">
                                            <xsl:sort select="dereferencing:where"/>
                                            <xsl:copy-of select="./dereferencing:where"/>
                                        </xsl:for-each-group>
                                    </dereferencing:where-list>
                                </dereferencing:value>
                            </xsl:for-each-group>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:for-each-group select="$messages-all/*[./dereferencing:type-number=current()/@type-number]" group-by="./dereferencing:where">
                                <xsl:sort select="./dereferencing:where"/>
                                <xsl:copy-of select="./*[name() != 'dereferencing:type-number']"/>
                            </xsl:for-each-group>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:copy>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
