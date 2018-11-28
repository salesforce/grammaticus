/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableSortedMap;

/**
 * A linguistic noun, containing a set of strings associated with the various noun forms
 * of the given language.
 *
 * @author yoikawa, stamm
 */
public abstract class Noun extends GrammaticalTerm implements Cloneable {
    private static final Logger logger = Logger.getLogger(Noun.class.getName());
    private static final long serialVersionUID = 1L;

    private final NounType nounType;
    private final String entityName;
    private final String pluralAlias;
    private LanguageGender gender;          // TODO: make final
    private LanguageStartsWith startsWith;  // TODO: make final
    private final String access;
    private final boolean isStandardField; // specifies whether this label is a standard field or not. For the Rename
    private final boolean isCopied;        // specifies whether this noun was copied from english (or another fallback language)

    /**
     * Construct a noun for the given declension
     *
     * @param declension          the declension for the language associated with this noun
     * @param name                the name of the noun
     * @param pluralAlias         an alias used in the label files to refer to this noun in plural
     * @param type                the NounType of this noun
     * @param entityName          TODO
     * @param startsWith          what this noun starts with
     * @param gender              the grammatical gender of the noun
     * @param access              a string preresenting the access of this object
     * @param isStandardField     whether this noun represents a standard field (as opposed to a concept).  Determines the section the noun is placed in for rename tabs
     * @param isCopiedFromDefault if true, means that this noun was not defined in the language of the declension, but instead is copied from english or another fallback language
     */
    protected Noun(LanguageDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopiedFromDefault) {
        super(declension, name);
        this.pluralAlias = intern(pluralAlias);
        this.nounType = type;
        this.entityName = intern(entityName);
        this.gender = gender;
        this.startsWith = startsWith;
        this.access = intern(access);
        this.isStandardField = isStandardField;
        this.isCopied = isCopiedFromDefault;
    }

    /**
     * What "type" of noun.  This controls which Noun forms should be displayed in the
     * UI to allow for renaming.
     */
    public enum NounType {
        ENTITY("entity"),
        FIELD("field"),
        OTHER(null),
        ;
        private final String apiValue;

        private NounType(String apiValue) {
            this.apiValue = apiValue;
        }

        public static NounType getByApiValue(String apiValue) {
            for (NounType type : values()) {
                if (type.apiValue != null && type.apiValue.equalsIgnoreCase(apiValue)) {
                    return type;
                }
            }
            return NounType.OTHER;
        }
    }

    @Override
    public boolean isCopiedFromDefault() {
        return isCopied;
    }

    public final LanguageGender getGender() {
        return this.gender;
    }

    @Override
    public final LanguageStartsWith getStartsWith() {
        return this.startsWith;
    }

    public String getPluralAlias() {
        return this.pluralAlias;
    }

    public String getAccess() {
        return this.access;
    }

    /**
     * @return the name of the entity this noun is associated with
     */
    public String getEntityName() {
        return this.entityName;
    }

    // convenient method to get the default value
    public String getDefaultString(boolean isPlural) {
        String result = getString(getDeclension().getNounForm(isPlural ? LanguageNumber.PLURAL : LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE));
        if (result == null && isPlural) {
            logger.fine("Missing plural form for " + getName() + " in " + getDeclension().getLanguage());
            result = getString(getDeclension().getNounForm(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE));
        }
        return result;
    }

    // for label dic parser
    protected abstract void setString(String value, NounForm nid);

    public abstract String getString(NounForm nid);

    /**
     * @return all of the noun forms defined in the sfdcnames.xml file.
     */
    public abstract Map<? extends NounForm, String> getAllDefinedValues();

    public boolean isStandardField() {
        return isStandardField;
    }

    public String getString(NounForm nid, boolean lowerCase) {
        String s = getString(nid);
        if (lowerCase && s != null && s.length() != 0) {
            s = getDeclension().formLowercaseNounForm(s, nid);
        }
        return s;
    }

    private boolean equalsAttribute(Object obj) {
        return this.gender == ((Noun) obj).gender && this.startsWith == ((Noun) obj).startsWith;
    }

    protected boolean equalsValue(Object obj) {
        return ((Noun) obj).getAllDefinedValues().equals(getAllDefinedValues());
    }

    @Override
    public final boolean equals(Object obj) {
        return obj != null && (obj instanceof Noun) && getName().equals(((Noun) obj).getName()) && equalsAttribute(obj) && equalsValue(obj);
    }

    @Override
    public int hashCode() {
        return getName().hashCode()
                + getAllDefinedValues().hashCode()
                + this.gender.hashCode()
                + this.startsWith.hashCode();
    }

    /**
     * @param name the name of the noun
     *             called by validate(String, LanguageDictionary. <br>
     *             Override this method if you need more validation. or fixup gender and startsWith values. For English, we may
     *             be able to figure out Vowel letter, but can't do that for vowel sounds like "Hour".
     */
    protected boolean validateGender(String name) {
        return true;
    }

    protected void setGender(LanguageGender gender) {
        this.gender = gender;
    }

    protected void setStartsWith(LanguageStartsWith startsWith) {
        this.startsWith = startsWith;
    }

    public NounType getNounType() {
        return this.nounType;
    }

    protected boolean validateValues(String name) {
        return validateValues(name, LanguageCase.NOMINATIVE);
    }

    protected abstract boolean validateValues(String name, LanguageCase _case);

    /**
     * validate and fix-up missing data if possible.
     */
    @Override
    public final boolean validate(String name) {
        // because the methods below also provide defaulting, both need
        // to be executed for every evaluation
        boolean genderVal = validateGender(name);
        boolean valuesVal = validateValues(name);
        return genderVal && valuesVal;
    }

    // used only by validation
    protected static final String VALIDATION_ERROR_HEADER = "###\tError:";
    protected static final String VALIDATION_WARNING_HEADER = "###\tWarning:";

    // deep clone
    @Override
    public Noun clone() {
        try {
            Noun n = (Noun) super.clone();
            return n;
        }
///CLOVER:OFF
        catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
///CLOVER:ON
    }

    public Noun clone(LanguageGender genderOverride, LanguageStartsWith startsWithOverride) {
        return clone(genderOverride, startsWithOverride, null);
    }

    /**
     * Make a clone of the given noun, overwriting the given values with the provided overrides
     *
     * @param genderOverride     the gender to override, if not null
     * @param startsWithOverride the starts with to override, if not null
     * @param valueOverrides     the overrides for values (usually from end-user customization)
     * @return a new clone of this noun with the various values overridden
     */
    public Noun clone(LanguageGender genderOverride, LanguageStartsWith startsWithOverride, Map<NounForm, String> valueOverrides) {
        Noun n = clone();
        if (genderOverride != null) n.setGender(genderOverride);
        if (valueOverrides != null) {
            for (Map.Entry<NounForm, String> entry : valueOverrides.entrySet()) {
                n.setString(intern(entry.getValue()), entry.getKey());
            }
        }
        //needs to happen after setString which sets startswith in Greek based on Plosive which breaks testRenameTabPositiveCasesEndUser
        //because it sets startsWith = specialcase without setting the string to begin with a Plosive
        if (startsWithOverride != null) n.setStartsWith(startsWithOverride);
        return n;
    }

    // -------------------------------------------------------------
    // Database Access support functions
    //
    public final void setString(LanguageDeclension declension, ResultSet rs) throws SQLException {
        LanguageNumber number = LanguageNumber.fromIntValue(rs.getInt("PLURAL"));
        LanguageCase _case = LanguageCase.fromDbValue(rs.getString("CASE_TYPE"));
        LanguagePossessive poss = LanguagePossessive.fromDbValue(rs.getString("POSSESSIVE"));
        LanguageArticle art = LanguageArticle.fromDbValue(rs.getString("ARTICLE"));
        NounForm exactForm = declension.getExactNounForm(number, _case, poss, art);
        if (exactForm != null) {
            setString(intern(rs.getString("VALUE")), exactForm);
        } else {
            // 99% of the time this is because of the horribleness that was articles in french and italian.
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("###\tError: The noun " + getName() + " has no exact form in " + declension.getLanguage() + " for " + number + ":" + _case + ":" + poss + ":" + art);
            }
        }
    }

    public boolean defaultValidate(String name, Collection<? extends NounForm> requiredForms) {
        for (NounForm form : getDeclension().getAllNounForms()) {
            if (getString(form) == null) {
                if (getNounType() == NounType.ENTITY) {
                    // Only do the "defaulting" on nouns of type "entity" because
                    // all required forms should be specified or derived
                    String value = getCloseButNoCigarString(form);

                    if (value == null) {
                        logger.info("###\tError: The noun " + name + " has no " + form + " form and no default could be found");
                        return false;
                    }
                    setString(intern(value), form);
                } else if (requiredForms.contains(form)) {
                    // TODO SLT: This logic seems faulty.  Why'd we bother
                    logger.finest("###\tError: The noun " + name + " has no " + form + " form");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param form the form to find
     * @return the noun form that is closest to the form, including an exact match.
     */
    public String getClosestString(NounForm form) {
        String result = getString(form);
        return result != null ? result : getCloseButNoCigarString(form);
    }

    /**
     * @param form the form to find the closest.
     * @return the closest noun form that would match the form, but isn't actually the form
     */
    public String getCloseButNoCigarString(NounForm form) {
        String s = null;
        NounForm baseForm = null;
        // Possessive form is first to drop
        if (form.getPossessive() != getDeclension().getDefaultPossessive()) {
            baseForm = getDeclension().getExactNounForm(form.getNumber(), form.getCase(), getDeclension().getDefaultPossessive(), form.getArticle());
            s = getString(baseForm);
        }

        // Article form is next to drop
        if (s == null && form.getArticle() != getDeclension().getDefaultArticle()) {
            baseForm = getDeclension().getExactNounForm(form.getNumber(), form.getCase(), form.getPossessive(), getDeclension().getDefaultArticle());
            s = getString(baseForm);
        }

        // Now case
        if (s == null && getDeclension().hasAllowedCases() && form.getCase() != getDeclension().getDefaultCase()) {
            baseForm = getDeclension().getExactNounForm(form.getNumber(), getDeclension().getDefaultCase(), form.getPossessive(), form.getArticle());
            s = getString(baseForm);
        }

        // Now number
        if (s == null && getDeclension().hasPlural() && form.getNumber() != LanguageNumber.SINGULAR) {
            baseForm = getDeclension().getExactNounForm(LanguageNumber.SINGULAR, form.getCase(), form.getPossessive(), form.getArticle());
            s = getString(baseForm);
        }

        return s;
    }

    @Override
    protected final TermType getTermType() {
        return TermType.Noun;
    }

    @Override
    public String toString() {
        return "Noun-" + getDeclension().getLanguage().getLocale() + "-'" + getAllDefinedValues().get(getDeclension().getAllNounForms().iterator().next()) + "'";
    }

    /**
     * Provides clients the capability of indicating when members of Noun's can be converted to space efficient data structures.
     */
    public abstract void makeSkinny();

    /**
     * Utility method used to convert static {@link Map}'s concrete type to a {@link ImmutableSortedMap}.
     * {@link ImmutableSortedMap} have a 8 byte overhead per element and are useful for reducing the per element
     * overhead, that is traditionally high on most {@code Map} implementations.
     *
     * @return A {@link ImmutableSortedMap} created from a {@link Map} of {@link NounForm}'s (key) to {@link String}'s
     * (value).
     */
    public <T extends NounForm> Map<T, String> makeSkinny(Map<T, String> map) {
        return ImmutableSortedMap.copyOf(map, new Comparator<NounForm>() {
            @Override
            public int compare(NounForm n1, NounForm n2) {
                return n1.getKey().compareTo(n2.getKey());
            }
        });
    }
}