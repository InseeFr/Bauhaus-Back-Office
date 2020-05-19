<?xml version="1.0" encoding="UTF-8"?>
<!--Nous devons définir plusieurs espaces de noms car nous utilisons -->
<!--le modèle de document du fichier OpenOffice.org en mémoire       -->
<!--Si nous souhaitons accéder à plus de propriétés du document,     -->
<!--nous devons ajouter les espaces de noms ici                      -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
    xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
    xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
    exclude-result-prefixes="office table text">
    
    <xsl:output method = "xml" indent = "yes" encoding = "UTF-8" omit-xml-declaration = "no"/>
    
    <!--En spécifiant la PropertyValue 'URL' dans les propriétés destoreToURL(),         -->
    <!--nous pouvons passer un unique paramètre à cette feuille de style.                -->
    <!--Inconvénient : si nous utilisons la propriété 'URL' dans la feuille de style     -->
    <!--et que nous l'appelons depuis OOo par le menu« Fichiers » > « Exporter ... »,    -->
    <!--OOodéfini une URL de destination. Ce n'est pas forcément ce que nous souhaitons. -->
    <xsl:param name="targetURL"/>
    
    <xsl:variable name="exportDate">
        <xsl:choose>
            <xsl:when test="string-length(substring-before($targetURL,';'))=10">
                <xsl:value-of select="substring-before($targetURL,';')"/>
            </xsl:when>
            <xsl:when test="string-length($targetURL)=10">
                <xsl:value-of select="$targetURL"/>
            </xsl:when>
        </xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="exportUser">
        <xsl:if test="string-length(substring-before($targetURL,';'))>0">
            <xsl:value-of select="substring-after($targetURL,';')"/>
        </xsl:if>
    </xsl:variable>
    
    <!--Traitement du document OOo -->
    <xsl:template match="/"> 
        <payments>  
            <xsl:attribute name="export-date">
                <xsl:value-of select="$exportDate"/>
            </xsl:attribute>  
            <xsl:attribute name="export-user">
                <xsl:value-of select="$exportUser"/></xsl:attribute>  <!--Traitementde tout le tableau-->  
            <xsl:apply-templates select="//table:table"/> 
        </payments>
    </xsl:template>
</xsl:stylesheet>