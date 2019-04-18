<!--
   Grammaticus dictionary DTD for ${decl.language}

   see com.force.i18n.grammar.LanguageDeclension for more information
   
   DO NOT MODIFY BY HAND! This file is generated from names.dtd.ftl
-->
<!-- simple boolean type -->
<!ENTITY % BOOLEAN    "(true|false)">
<!ENTITY % YN         "(y|n)">

<!--=================== dictionary elements ==============================-->

<!-- "entity" if this is standard object(tab) name -->
<!ENTITY % noun.type  "type     (entity | field | other)">
<#if hasNounClasses>
<#if decl.language.locale.language == "sw"><!--  "M-wa" or "I" for M-Wa class I/II
    "M-mi" or "III" for M-mi class III/IV
    "Ma" or "V" for Ma class V/VI
    "Ki-vi" or "VII" for Ki-vi class VII/VIII
    "N" or "IX" for N class IX/X
    "U" or "XI" for U class XI/XII/XIV
    "Pa" or "XVI" for Pa class XVI
    "Ku" or "XVII" for Ku class XVII
    "Mu" or "XVIII" for Mu class XVIII -->
<#else><!--  "I" for class I/II and Ia/IIa (um-/aba-, u-/o-)
    "III" for class III/IV  (um-/imi-)
    "V" for class V/VI (u-/i-,ama-)
    "VII" for class VII/VIII (isi-,izi-)
    "IX" for class IX/X (in-, izin-)
    "XI" for class XI (u-,ulu-)
    "XIV" for class XIV (ubu-)
    "XV" for class XV (uku-)
    "XVII" for class XVII (uku-)
</#if>
<#else><!-- "m" for Masculine, "f" for Feminine, "n" for Neuter,
     "a" for Animate Masculine (Slavic languages)
     "e" for Euter (Swedish) and "c" for Common (Dutch) -->
</#if>
<!ENTITY % gender     "gender   (${genderOrs})">

<!--
Specifing the type of first letter(or soft sound) of noun
    c:  consonant    (default)
    v:  starts with vowel.
    s:  S(consonant) or Z (for Italian) or ㄹ (for Korean)
    -->
<#if decl.hasEndsWith()>
<!ENTITY % ends     "endsWith  (${startsWithOrs})">
<#else>    
<!ENTITY % starts     "startsWith  (${startsWithOrs})">
</#if>

<!--"y" for plural form, "n" for singular form -->
<!ENTITY % plural     "plural   %YN;">

<#if decl.hasAllowedCases()><!--
case attribute is to determine:  (nominative is default)
<#foreach case in decl.allowedCases>
    ${case.dbValue}    ${case.apiValue} case
</#foreach>    -->
<!ENTITY % case       "case     (${caseOrs})">

</#if>
<#if decl.hasPossessive()><!--
Possesive
    n:  none
    f:  first person possessive
    s:  second person possessive
    -->
<!ENTITY % poss       "poss     (${possessiveOrs})">

</#if>
<#if decl.hasArticle() || decl.hasArticleInNounForm()><!--
article type
    n     no article (default)
    the|d definite article "the" in English
    a|i   indefinite article "a" in English
    -->
<!ENTITY % article    "article (n|a|the|i|d)">

</#if>
<!--
dnt (Do Not Translate) attribute defines whether the following section or label
should be translated. Set to "true" to NOT translate.
-->
<!ENTITY % dnt      "dnt %BOOLEAN;">

<!--
standard field
		default is "y" (yes)
	y	is standard field (yes)
	n	is not a standard field (no)
	-->
<!ENTITY % standardField "standardField %YN;">
<!--=================== Root element =====================================-->
<#if adjectives>
<!ELEMENT ${prefix}adjectives ((adjective)*, (article)*, (import)*)>
<#else>
<!ELEMENT ${prefix}cnames ((noun)+, (adjective)*, (article)*, (import)*)>
</#if>

<#if !adjectives>
<!--
noun element is used to specify translation of nouns
    name:   specify either  %entity; or %compoundNouns;
    alias:   The alias used in label files to refer to the object in the plural
    entity:  The object type associated with this label
    type:   "entity" if this is for standard object, where all form are required
            "field" if this requires both singular/plural form,
              other forms are not required, but can be specified and renamed
<#if decl.hasGender()>    gender:  The gender of the noun as defined here
</#if>
<#if decl.hasStartsWith()>    endsWith: What kind of sound does this noun ends with
<#elseif decl.hasStartsWith()>    startsWith: What kind of sound does this noun start with
</#if>    -->
<!ELEMENT noun (value)+>
<!ATTLIST noun
  name          ID          #REQUIRED
  %dnt;                 #IMPLIED
  alias		CDATA       #IMPLIED
  entity	CDATA	    #IMPLIED
  access        CDATA       #IMPLIED
  %noun.type;               #IMPLIED
  %gender;                  <#if decl.hasGender()> #REQUIRED
<#else> "${decl.defaultGender.dbValue}"
</#if>  %starts;                 <#if decl.hasStartsWith() && !decl.hasAutoDerivedStartsWith()> #REQUIRED
<#else> "${decl.defaultStartsWith.dbValue}"
</#if>  %standardField;	    #IMPLIED
  >

</#if>
<!--
adjective element is used to specify translation of adjectives
    position: whether this adjective comes before or after the noun, usually.
    name:   specify name of this adjective
    -->
<!ELEMENT adjective (value)+>
<!ATTLIST adjective
  name          ID          #REQUIRED
  %dnt;                     #IMPLIED
  position      (b|a)       #IMPLIED
<#if decl.hasStartsWith()>  %starts;                  "${decl.defaultStartsWith.dbValue}"
</#if>
  >

<!--
article element is used to specify translation of articles and other determiners
    name:   specify name of this adjective
    -->
<!ELEMENT article (value)+>
<!ATTLIST article
  name          ID          #REQUIRED
  %dnt;                     #IMPLIED
  type          (n|a|the|i|d)     #REQUIRED
  >

<!--
import element specifies file to be included as part of the document
    -->
<!ELEMENT import EMPTY>
<!ATTLIST import
  file          CDATA       #REQUIRED
  >

<!--
value element is shared by both noun/adjective.
    -->
<!ELEMENT value (#PCDATA)>
<!ATTLIST value
  %plural;                  <#if decl.hasPlural()>#REQUIRED<#else> "n"</#if>
<#if decl.hasGender()>  %gender;                  "${decl.defaultGender.dbValue}"
</#if>
<#if decl.hasStartsWith()>  %starts;                  #IMPLIED
</#if>
<#if decl.hasEndsWith()>  %ends;                  #IMPLIED
</#if>
<#if decl.hasAllowedCases()>  %case;                    "${decl.defaultCase.dbValue}"
</#if>
<#if decl.hasPossessive()>  %poss;                    "${decl.defaultPossessive.dbValue}"
</#if>
<#if decl.hasArticle() || decl.hasArticleInNounForm()>  %article;                 #IMPLIED
</#if>  >

