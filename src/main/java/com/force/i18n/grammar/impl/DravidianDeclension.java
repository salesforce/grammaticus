/*
 * Copyright (c) 2019, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.grammar.impl;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.AbstractLanguageDeclension;
import com.force.i18n.grammar.Adjective;
import com.force.i18n.grammar.AdjectiveForm;
import com.force.i18n.grammar.LanguageArticle;
import com.force.i18n.grammar.LanguageCase;
import com.force.i18n.grammar.LanguageDeclension;
import com.force.i18n.grammar.LanguageGender;
import com.force.i18n.grammar.LanguageNumber;
import com.force.i18n.grammar.LanguagePosition;
import com.force.i18n.grammar.LanguagePossessive;
import com.force.i18n.grammar.LanguageStartsWith;
import com.force.i18n.grammar.Noun;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.NounForm;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.ComplexNounForm;
import com.google.common.collect.ImmutableList;

/**
 * Dravidian languages generally have cases, plural, gender.
 * but do not have articles, starts with, adj/noun agreement, possession.
 *
 * This is generalized from Tamil
 *
 * @author cgrabill, stamm
 * @since 1.1
 */
abstract class DravidianDeclension extends AbstractLanguageDeclension {

    private final List<DravidianNounForm> entityForms;
    private final List<DravidianNounForm> fieldForms;

    protected DravidianDeclension(HumanLanguage language) {
        super(language);
        // Generate the different forms from subclass methods
        ImmutableList.Builder<DravidianNounForm> entityBuilder = ImmutableList.builder();
        ImmutableList.Builder<DravidianNounForm> fieldBuilder = ImmutableList.builder();
        int ordinal = 0;
        for (LanguageNumber number : getAllowedNumbers()) {
            for (LanguageCase caseType : getRequiredCases()) {
                DravidianNounForm form = new DravidianNounForm(this, number, caseType, ordinal++);
                entityBuilder.add(form);
                if (caseType == LanguageCase.NOMINATIVE) {
                    fieldBuilder.add(form);
                }
            }
        }
        this.entityForms = entityBuilder.build();
        this.fieldForms = fieldBuilder.build();
    }

    static class DravidianNounForm extends ComplexNounForm {
        private static final long serialVersionUID = 1L;

        private final LanguageCase caseType;
        private final LanguageNumber number;

        DravidianNounForm(LanguageDeclension declension, LanguageNumber number, LanguageCase caseType, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.caseType = caseType;
        }

        @Override public LanguageNumber getNumber() { return number; }
        @Override public LanguageCase getCase() { return caseType; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
        @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO; }

        @Override
        public String getKey() {
            return getNumber().getDbValue() + "-" + getCase().getDbValue();
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.caseType, this.number);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other instanceof DravidianNounForm) {
                DravidianNounForm o = this.getClass().cast(other);
                return super.equals(other) && this.caseType == o.caseType && this.number == o.number;
            }
            return false;
        }

        @Override
        public String toString() {
            return "DravNF:" + getKey();
        }
    }

    /**
     * Represents a Dravidian noun. See DravidianNounForm for more info.
     */
    public static class DravidianNoun extends Noun {
        private static final long serialVersionUID = 1L;

        //store everything
        private transient Map<DravidianNounForm, String> values = new HashMap<>();

        DravidianNoun(DravidianDeclension declension, String name, String pluralAlias, NounType type, String entityName,
                LanguageGender gender, String access, boolean isStandardField, boolean isCopiedFromDefault ) {
            super(declension, name, pluralAlias, type, entityName, LanguageStartsWith.CONSONANT,
                    gender, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        protected void setString(String value, NounForm nid) {
            values.put((DravidianNounForm) nid, intern(value));
        }

        @Override
        public Map<? extends NounForm, String> getAllDefinedValues() {
            return values;
        }

        @Override
        protected boolean validateValues(String name, LanguageCase _case) {
            return defaultValidate(name, getDeclension().getFieldForms());
        }

        /**
         * Need to override so that a cloned DravidianNoun's values map is a HashMap.
         * Else, if you clone() after makeSkinny() has been called, you won't
         * be able to setString() on the cloned Noun.
         */
        @Override
        public Noun clone() {
            DravidianNoun noun = (DravidianNoun) super.clone();
            noun.values = new HashMap<DravidianNounForm,String>(noun.values);
            return noun;
        }

        @Override
        public String getString(NounForm nid) {
            assert nid instanceof DravidianNounForm : "Error: Used non-Dravidian noun form to get Dravidian noun.";
            return values.get(nid);
        }

        // Override read and write, or else you'll get mysterious exception
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();

            ComplexGrammaticalForm.serializeFormMap(out, values);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();

            this.values = ComplexGrammaticalForm.deserializeFormMap(in, getDeclension(), TermType.Noun);
        }
    }

    @Override
    public List<? extends NounForm> getAllNounForms() {
        return entityForms;
    }

    @Override
    public Collection<? extends NounForm> getEntityForms() {
        return entityForms;
    }

    @Override
    public Collection<? extends NounForm> getFieldForms() {
        return fieldForms;
    }

    @Override
    public Collection<? extends NounForm> getOtherForms() {
        return entityForms;
    }

    @Override
    public List<? extends AdjectiveForm> getAdjectiveForms() {
        return SimpleDeclension.ADJECTIVE_FORMS;
    }

    @Override
    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName,
            LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField,
            boolean isCopied) {
        return new DravidianNoun(this, name, pluralAlias, type, entityName, gender, access, isStandardField, isCopied);
    }

    @Override
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new SimpleAdjective(this, name);
    }

    @Override
    public boolean hasGender() {
        return true;
    }

    @Override
    public EnumSet<LanguageGender> getRequiredGenders() {
        return EnumSet.of(LanguageGender.NEUTER,
                          LanguageGender.MASCULINE,
                          LanguageGender.FEMININE);
    }


    @Override
    public boolean hasStartsWith() {
        return false;
    }


    @Override
    public boolean hasArticleInNounForm() {
        return false;
    }

    static final class TamilDeclension extends DravidianDeclension {
        public TamilDeclension(HumanLanguage language) {
            super(language);
        }

        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return EnumSet.of(LanguageCase.NOMINATIVE,
                              LanguageCase.GENITIVE,
                              LanguageCase.ACCUSATIVE,
                              LanguageCase.DATIVE,
                              LanguageCase.ABLATIVE,
                              LanguageCase.INSTRUMENTAL,
                              LanguageCase.LOCATIVE);
        }
    }

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Telugu_language#Grammar">Wikipedia: Telugu grammar</a>
     */
    static final class TeluguDeclension extends DravidianDeclension {
        public TeluguDeclension(HumanLanguage language) {
            super(language);
        }

        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return EnumSet.of(LanguageCase.NOMINATIVE,
                              LanguageCase.GENITIVE,
                              LanguageCase.ACCUSATIVE,
                              LanguageCase.DATIVE,
                              LanguageCase.ABLATIVE,  // Ablative & Instrumental merger
                              LanguageCase.LOCATIVE);
        }
    }

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Kannada_grammar">Wikipedia: Kannada grammar</a>
     */
    static final class KannadaDeclension extends DravidianDeclension {
        public KannadaDeclension(HumanLanguage language) {
            super(language);
        }

        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return EnumSet.of(LanguageCase.NOMINATIVE,
                              LanguageCase.GENITIVE,
                              LanguageCase.ACCUSATIVE,
                              LanguageCase.DATIVE,
                              LanguageCase.LOCATIVE);
        }

    }

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Malayalam_grammar">Wikipedia: Malayalam grammar</a>
     */
    static final class MalayalamDeclension extends DravidianDeclension {
        public MalayalamDeclension(HumanLanguage language) {
            super(language);
        }

        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return EnumSet.of(LanguageCase.NOMINATIVE,
                              LanguageCase.GENITIVE,
                              LanguageCase.ACCUSATIVE,
                              LanguageCase.DATIVE,
                              LanguageCase.INSTRUMENTAL,  // Ablative & Instrumental merger
                              LanguageCase.LOCATIVE);
            // TODO FIXME: Malayalam has a "Sociative" case that expresses 'together with'.  It's mostly obsolete in Hungarian, so we don't have it anywhere else.
            // Check with linguists?
        }
    }
}
