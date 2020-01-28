/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.io.*;

import org.xml.sax.Attributes;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.impl.LanguageDeclensionFactory;

/**
 * Represents the attributes of a term
 *
 * helper class for LabelfileHandler to keep attributes of
 * <CODE>&lt;value%gt;</CODE> element.
 *
 * @author yoikawa,stamm
 */
public final class TermAttributes implements Serializable {
    private static final long serialVersionUID = 7466902680249231026L;


    public static final String PLURAL = "plural";
    public static final String CASE = "case";
    public static final String ARTICLE = "article";
    public static final String POSSESSIVE = "poss";
    public static final String POSITION = "position";
    public static final String GENDER = "gender";
    public static final String STARTS = "startsWith";
    public static final String ENDS = "endsWith";

    public static final String YES = "y";
    public static final String NO = "n";

    private transient LanguageDeclension declension;  // not final because of serialization
    private final LanguageStartsWith startsWith;
    private final LanguageGender gender;
    private final LanguageCase caseType;
    private final LanguageArticle article;
    private final LanguagePossessive possessive;
    private final LanguageNumber number;
    private final LanguagePosition position;

    public TermAttributes(LanguageDeclension declension, Attributes atts) {
         this(declension, atts, true);
    }


    /**
     * Initialize the TermAttributes with the given for the given declension and the given attributes.
     * @param declension the declension being parsed
     * @param atts the attributes being parsed
     * @param useDefaults if attributes are missing, should this object contain null, or contain the default values
     */
    public TermAttributes(LanguageDeclension declension, Attributes atts, boolean useDefaults) {
        this.declension = declension;

        LanguageStartsWith _startsWith = null;
        LanguageGender _gender = null;
        LanguageCase _caseType = null;
        LanguageArticle _article = null;
        LanguagePossessive _possessive = null;
        LanguageNumber _number = null;
        LanguagePosition _position = null;

        if (useDefaults) {
            _startsWith = declension.getDefaultStartsWith();
            _gender = declension.getDefaultGender();
            _caseType = declension.getDefaultCase();
            _article = declension.getDefaultArticle();
            _possessive = declension.getDefaultPossessive();
            _number = LanguageNumber.SINGULAR;
        }

        String plural = atts.getValue(PLURAL);
        if (plural != null) _number = LanguageNumber.fromLabelValue(plural);
        LanguageStartsWith st = LanguageStartsWith.fromDbValue(atts.getValue(ENDS));
        if (st != null) {
        	_startsWith = st;
        } else {
	        st = LanguageStartsWith.fromDbValue(atts.getValue(STARTS));
	        if (st != null) _startsWith = st;
        }
        LanguageGender g = LanguageGender.fromLabelValue(atts.getValue(GENDER));
        if (g != null) _gender = g;
        LanguageCase ct = LanguageCase.fromDbValue(atts.getValue(CASE));
        if (ct != null) _caseType = ct;
        LanguagePossessive p = LanguagePossessive.fromLabelValue(atts.getValue(POSSESSIVE));
        if (p != null) _possessive = p;
        LanguageArticle a = LanguageArticle.fromLabelValue(atts.getValue(ARTICLE));
        if (a != null) _article = a;
        LanguagePosition loc = LanguagePosition.fromLabelValue(atts.getValue(POSITION));
        if (loc != null) _position = loc;

        this.startsWith = _startsWith;
        this.gender = _gender;
        this.caseType = _caseType;
        this.article = _article;
        this.possessive = _possessive;
        this.number = _number;
        this.position = _position;
    }

    public boolean isPlural() {
        return this.number.isPlural();
    }
    public LanguageNumber getNumber() {
        return number;
    }
    public LanguageStartsWith getStartsWith() {
        return startsWith;
    }
    public LanguageGender getGender() {
        return gender;
    }
    public LanguageCase getCase() {
        return caseType;
    }
    public LanguagePossessive getPossessive() {
        return possessive;
    }
    public LanguageArticle getArticle() {
        return article;
    }
    public LanguagePosition getPosition() {
        return position;
    }

    // Used when parsing dictionaries
    public NounForm getExactNounForm() {
        return declension.getExactNounForm(getNumber(), getCase(), getPossessive(), getArticle());
    }
    public AdjectiveForm getAdjectiveForm() {
        return declension.getAdjectiveForm(getStartsWith(), getGender(), getNumber(), getCase(), getArticle(), getPossessive());
    }
    public ArticleForm getArticleForm() {
        return declension.getArticleForm(getStartsWith(), getGender(), getNumber(), getCase());
    }


    // Used when parsing labels.  The term attributes can "override" if necessary
    public NounForm getApproximateNounForm() {
        return declension.getApproximateNounForm(getNumber(), getCase(), getPossessive(), getArticle());
    }


    /**
     * @return the adjective form for the given noun and next lexical element using the current values as "overrides" if necessary
     * @param decl the declension for the language
     * @param n the noun associated with the given adjective
     * @param nounForm the form of the noun associated with this adjective
     * @param nextLexicalElement the next lexical element that should be used to determine starts with
     */
    public AdjectiveForm getAdjectiveForm(LanguageDeclension decl, Noun n, NounForm nounForm, GrammaticalTerm nextLexicalElement) {
        LanguageStartsWith actualStartsWith = nextLexicalElement != null && nextLexicalElement.getStartsWith() != null ? nextLexicalElement.getStartsWith() : (n != null ? n.getStartsWith() : decl.getDefaultStartsWith());
        return decl.getApproximateAdjectiveForm(actualStartsWith,
                getGender() != null ? getGender() : (n != null ? n.getGender() : decl.getDefaultGender()),
                getNumber() != null ? getNumber() : nounForm.getNumber(),
                getCase() != null ? getCase() : nounForm.getCase(),
                getArticle() != null ? getArticle() : nounForm.getArticle(),
                getPossessive() != null ? getPossessive() : nounForm.getPossessive());
    }

    /**
     * @return the adjective form for the given noun and next lexical element using the current values as "overrides" if necessary
     * @param decl the declension for the language
     * @param n the noun associated with the given article
     * @param nounForm the form of the noun associated with this article
     * @param nextLexicalElement the next lexical element that should be used to determine starts with
     */
    public ArticleForm getArticleForm(LanguageDeclension decl, Noun n, NounForm nounForm, GrammaticalTerm nextLexicalElement) {
        // n and nextLexicalElement may be null, so default appropriately
        // Note: this logic be similar to getAdjectiveForm()
        LanguageGender defaultGender;
        LanguageStartsWith actualStartsWith;

        if (n != null) {
            defaultGender = n.getGender();
        } else {
            defaultGender = decl.getDefaultGender() != null ? decl.getDefaultGender() : LanguageGender.NEUTER;
        }

        if (nextLexicalElement != null && nextLexicalElement.getStartsWith() != null) {
            actualStartsWith = nextLexicalElement.getStartsWith();
        } else if (n != null && n.getStartsWith() != null){
            actualStartsWith = n.getStartsWith();
        } else {
            actualStartsWith = decl.getDefaultStartsWith() != null ? decl.getDefaultStartsWith() : LanguageStartsWith.CONSONANT;
        }

        return decl.getApproximateArticleForm(actualStartsWith, getGender() != null ? getGender() : defaultGender,
                getNumber() != null ? getNumber() : nounForm.getNumber(),
                getCase() != null ? getCase() : nounForm.getCase());
    }

    TermAttributes overrideArticle(LanguageArticle articleOverride) {
        if (articleOverride != null) {
            return new TermAttributes(this.declension, this.startsWith, this.gender, this.caseType, articleOverride, this.possessive, this.number, this.position);
        } else {
            return this;
        }
    }

    TermAttributes overrideFromNounForm(NounForm form) {
        return new TermAttributes(this.declension, this.startsWith, this.gender, form.getCase(), form.getArticle(), form.getPossessive(), this.number, this.position);
    }

    private TermAttributes(LanguageDeclension declension, LanguageStartsWith startsWith, LanguageGender gender,
            LanguageCase caseType, LanguageArticle article, LanguagePossessive possessive, LanguageNumber number, LanguagePosition position) {
        this.declension = declension;
        this.startsWith = startsWith;
        this.gender = gender;
        this.caseType = caseType;
        this.article = article;
        this.possessive = possessive;
        this.number = number;
        this.position = position;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = ((article == null) ? 0 : article.hashCode());
        result = prime * result + ((declension == null) ? 0 : declension.hashCode());
        result = prime * result + ((caseType == null) ? 0 : caseType.hashCode());
        result = prime * result + ((article == null) ? 0 : article.hashCode());
        result = prime * result + ((gender == null) ? 0 : gender.hashCode());
        result = prime * result + ((number == null) ? 0 : number.hashCode());
        result = prime * result + ((possessive == null) ? 0 : possessive.hashCode());
        result = prime * result + ((startsWith == null) ? 0 : startsWith.hashCode());
        result = prime * result + ((position == null) ? 0 : position.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TermAttributes other = (TermAttributes)obj;
        if (declension != other.declension) return false;
        if (caseType != other.caseType) return false;
        if (article != other.article) return false;
        if (gender != other.gender) return false;
        if (number != other.number) return false;
        if (possessive != other.possessive) return false;
        if (startsWith != other.startsWith) return false;
        if (position != other.position) return false;
        return true;
    }

    @Override
    public String toString() {
        return "TermAttrs:"+toNullStr(getNumber())+":"+toNullStr(this.getGender())+":"+toNullStr(this.getCase())+":"+toNullStr(this.getStartsWith())+":"+toNullStr(this.getArticle())+":"+toNullStr(this.getPossessive());
    }
    
    public String toJson() {
    	StringBuilder sw = new StringBuilder();
    	sw.append("{");
    	if (this.caseType != null) sw.append("\""+LanguageCase.JSON_ATTR_NAME+"\":\"").append(this.caseType.getDbValue()).append("\",");
    	if (this.article != null) sw.append("\""+LanguageArticle.JSON_ATTR_NAME+"\":\"").append(this.article.getDbValue()).append("\",");
    	if (this.gender != null) sw.append("\""+LanguageGender.JSON_ATTR_NAME+"\":\"").append(this.gender.getDbValue()).append("\",");
    	if (this.number != null) sw.append("\""+LanguageNumber.JSON_ATTR_NAME+"\":\"").append(this.number.getDbValue()).append("\",");
    	if (this.possessive != null) sw.append("\""+LanguagePossessive.JSON_ATTR_NAME+"\":\"").append(this.possessive.getDbValue()).append("\",");
    	if (this.startsWith != null) sw.append("\""+LanguageStartsWith.JSON_ATTR_NAME+"\":\"").append(this.startsWith.getDbValue()).append("\",");
    	if (this.position != null) sw.append("\""+LanguagePosition.JSON_ATTR_NAME+"\":\"").append(this.position.getDbValue()).append("\",");
    	if (sw.length()>1) sw.setLength(sw.length() - 1); // Get rid of the last comma
    	sw.append("}");
    	return sw.toString();
    }

    private static String toNullStr(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeShort(this.declension.getLanguage().ordinal());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        HumanLanguage ul = LanguageProviderFactory.get().getAll().get(in.readShort());
        this.declension = LanguageDeclensionFactory.get().getDeclension(ul);
    }
}
