<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>

    <xd:doc>
        <xd:desc>Root template : for each possible missing content, a variable calculates the list of the elements with that missing content.
            If the list is not empty, one line explains what kind of content was expected, 
            then for each element with missing content, one line describes that element (ID, name)</xd:desc>
    </xd:doc>
    <xsl:template match="/">
        <xsl:variable name="StudyUnitWithoutTitle" select="/FragmentInstance/Fragment/StudyUnit[not(Citation/Title/String[@lang='fr-FR']) or not(normalize-space(Citation/Title/String[@lang='fr-FR']) != '')]" as="node() *"/>
        <xsl:if test="$StudyUnitWithoutTitle[1]">
            <xsl:text>StudyUnit sans titre :</xsl:text>
            <xsl:for-each select="$StudyUnitWithoutTitle">
                <xsl:text>&#xa;Identifiant : </xsl:text>
                <xsl:value-of select="ID"/>
            </xsl:for-each>
            <xsl:text>&#xa;&#xa;</xsl:text>
        </xsl:if>
        
        <xsl:variable name="StudyUnitWithoutLogicalProduct" select="/FragmentInstance/Fragment/StudyUnit[not(LogicalProductReference)]" as="node() *"/>
        <xsl:if test="$StudyUnitWithoutLogicalProduct[1]">
            <xsl:text>StudyUnit sans LogicalProduct :</xsl:text>
            <xsl:for-each select="$StudyUnitWithoutLogicalProduct">
                <xsl:text>&#xa;Identifiant : </xsl:text>
                <xsl:value-of select="ID"/>
                <xsl:text> ; Nom : </xsl:text>
                <xsl:value-of select="Citation/Title/String[@lang='fr-FR']"/>
            </xsl:for-each>
            <xsl:text>&#xa;&#xa;</xsl:text>
        </xsl:if>
        
        <xsl:variable name="LogicalProductWithoutRepresentedVariableScheme" select="/FragmentInstance/Fragment/LogicalProduct[not(RepresentedVariableSchemeReference)]" as="node() *"/>
        <xsl:if test="$LogicalProductWithoutRepresentedVariableScheme[1]">
            <xsl:text>LogicalProduct sans RepresentedVariableScheme :</xsl:text>
            <xsl:for-each select="$LogicalProductWithoutRepresentedVariableScheme">
                <xsl:text>&#xa;Identifiant : </xsl:text>
                <xsl:value-of select="ID"/>
                <xsl:text> ; Nom : </xsl:text>
                <xsl:value-of select="LogicalProductName/String[@lang='fr-FR']"/>
            </xsl:for-each>
            <xsl:text>&#xa;&#xa;</xsl:text>
        </xsl:if>
        
        <xsl:variable name="RepresentedVariableGroupWithoutLabel" select="/FragmentInstance/Fragment/RepresentedVariableGroup[not(Label/Content[@lang='fr-FR']) or not(normalize-space(Label/Content[@lang='fr-FR']) != '')]" as="node() *"/>
        <xsl:if test="$RepresentedVariableGroupWithoutLabel[1]">
            <xsl:text>RepresentedVariableGroup sans Label :</xsl:text>
            <xsl:for-each select="$RepresentedVariableGroupWithoutLabel">
                <xsl:text>&#xa;Identifiant : </xsl:text>
                <xsl:value-of select="ID"/>
                <xsl:text> ; Nom : </xsl:text>
                <xsl:value-of select="RepresentedVariableGroupName/String[@lang='fr-FR']"/>
            </xsl:for-each>
            <xsl:text>&#xa;&#xa;</xsl:text>
        </xsl:if>

        <xsl:variable name="RepresentedVariableGroupWithoutRepresentedVariable" select="/FragmentInstance/Fragment/RepresentedVariableGroup[not(RepresentedVariableReference)]" as="node() *"/>
        <xsl:if test="$RepresentedVariableGroupWithoutRepresentedVariable[1]">
            <xsl:text>RepresentedVariableGroup sans RepresentedVariable :</xsl:text>
            <xsl:for-each select="$RepresentedVariableGroupWithoutRepresentedVariable">
                <xsl:text>&#xa;Identifiant : </xsl:text>
                <xsl:value-of select="ID"/>
                <xsl:text> ; Nom : </xsl:text>
                <xsl:value-of select="RepresentedVariableGroupName/String[@lang='fr-FR']"/>
            </xsl:for-each>
            <xsl:text>&#xa;&#xa;</xsl:text>
        </xsl:if>
        
        <xsl:variable name="RepresentedVariableWithoutName" select="/FragmentInstance/Fragment/RepresentedVariable[not(RepresentedVariableName/String[@lang='fr-FR']) or not(normalize-space(RepresentedVariableName/String[@lang='fr-FR']) != '')]" as="node() *"/>
        <xsl:if test="$RepresentedVariableWithoutName[1]">
            <xsl:text>RepresentedVariable sans Name :</xsl:text>
            <xsl:for-each select="$RepresentedVariableWithoutName">
                <xsl:text>&#xa;Identifiant : </xsl:text>
                <xsl:value-of select="ID"/>
            </xsl:for-each>
            <xsl:text>&#xa;&#xa;</xsl:text>
        </xsl:if>
        
        <xsl:variable name="RepresentedVariableWithoutLabel" select="/FragmentInstance/Fragment/RepresentedVariable[not(Label/Content[@lang='fr-FR']) or not(normalize-space(Label/Content[@lang='fr-FR']) != '')]" as="node() *"/>
        <xsl:if test="$RepresentedVariableWithoutLabel[1]">
            <xsl:text>RepresentedVariable sans Label :</xsl:text>
            <xsl:for-each select="$RepresentedVariableWithoutLabel">
                <xsl:text>&#xa;Identifiant : </xsl:text>
                <xsl:value-of select="ID"/>
                <xsl:text> ; Nom : </xsl:text>
                <xsl:value-of select="RepresentedVariableName/String[@lang='fr-FR']"/>
            </xsl:for-each>
            <xsl:text>&#xa;&#xa;</xsl:text>
        </xsl:if>
        
        <xsl:variable name="RepresentedVariableWithoutRepresentation" select="/FragmentInstance/Fragment/RepresentedVariable[not(TextRepresentation) and not(NumericRepresentation) and not(DateTimeRepresentation) and not(CodeRepresentation)]" as="node() *"/>
        <xsl:if test="$RepresentedVariableWithoutRepresentation[1]">
            <xsl:text>RepresentedVariable sans Représentation :</xsl:text>
            <xsl:for-each select="$RepresentedVariableWithoutRepresentation">
                <xsl:text>&#xa;Identifiant : </xsl:text>
                <xsl:value-of select="ID"/>
                <xsl:text> ; Nom : </xsl:text>
                <xsl:value-of select="RepresentedVariableName/String[@lang='fr-FR']"/>
            </xsl:for-each>
            <xsl:text>&#xa;&#xa;</xsl:text>
        </xsl:if>
        
        <xsl:variable name="CodeListWithoutName" select="/FragmentInstance/Fragment/CodeList[not(CodeListName/String[@lang='fr-FR']) or not(normalize-space(CodeListName/String[@lang='fr-FR']) != '')]" as="node() *"/>
        <xsl:if test="$CodeListWithoutName[1]">
            <xsl:text>CodeList sans Name :</xsl:text>
            <xsl:for-each select="$CodeListWithoutName">
                <xsl:text>&#xa;Identifiant : </xsl:text>
                <xsl:value-of select="ID"/>
            </xsl:for-each>
            <xsl:text>&#xa;&#xa;</xsl:text>
        </xsl:if>
        
        <xsl:variable name="CategoryWithoutLabel" select="/FragmentInstance/Fragment/Category[not(Label/Content[@lang='fr-FR']) or not(normalize-space(Label/Content[@lang='fr-FR']) != '')]" as="node() *"/>
        <xsl:if test="$CategoryWithoutLabel[1]">
            <xsl:text>Category sans Label :</xsl:text>
            <xsl:for-each select="$CategoryWithoutLabel">
                <xsl:variable name="categoryId" select="ID"/>
                <xsl:variable name="codelistWithoutCategoryLabel" select="/FragmentInstance/Fragment/CodeList[Code/CategoryReference/ID = $categoryId]" as="node() *"/>
                <xsl:text>&#xa;Identifiant : </xsl:text>
                <xsl:value-of select="$categoryId"/>
                <xsl:text>&#xa;Liste de codes concernée : </xsl:text>
                <xsl:for-each select="$codelistWithoutCategoryLabel">
                    <xsl:text>&#xa;Identifiant : </xsl:text>
                    <xsl:value-of select="ID"/>
                    <xsl:text> ; Nom : </xsl:text>
                    <xsl:value-of select="CodeListName/String[@lang='fr-FR']"/>
                    <xsl:text> ; Valeur : </xsl:text>
                    <xsl:value-of select="Code[CategoryReference/ID = $categoryId]/Value"/>                    
                </xsl:for-each>
            </xsl:for-each>
            <xsl:text>&#xa;&#xa;</xsl:text>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>