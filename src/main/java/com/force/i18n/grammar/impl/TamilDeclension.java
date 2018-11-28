/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.io.*;
import java.util.*;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.ComplexNounForm;
import com.google.common.collect.ImmutableList;

/**
 * Our first Dravidian language. Tamil has cases, plural, gender.
 * Tamil does not have articles, starts with, adj/noun agreement, possession.
 *
 * @author cgrabill
 */
public class TamilDeclension extends LanguageDeclension {

    private final List<TamilNounForm> entityForms;
    private final List<TamilNounForm> fieldForms;

    public TamilDeclension(HumanLanguage language) {
        super(language);
        // Generate the different forms from subclass methods
        ImmutableList.Builder<TamilNounForm> entityBuilder = ImmutableList.builder();
        ImmutableList.Builder<TamilNounForm> fieldBuilder = ImmutableList.builder();
        int ordinal = 0;
        for (LanguageNumber number : EnumSet.of(LanguageNumber.SINGULAR, LanguageNumber.PLURAL)) {
            for (LanguageCase caseType : getRequiredCases()) {
                TamilNounForm form = new TamilNounForm(this, number, caseType, ordinal++);
                entityBuilder.add(form);
                if (caseType == LanguageCase.NOMINATIVE) {
                    fieldBuilder.add(form);
                }
            }
        }
        this.entityForms = entityBuilder.build();
        this.fieldForms = fieldBuilder.build();
    }

    static class TamilNounForm extends ComplexNounForm {
        private static final long serialVersionUID = 1L;
        private final LanguageCase caseType;
        private final LanguageNumber number;

        TamilNounForm(LanguageDeclension declension, LanguageNumber number, LanguageCase caseType, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.caseType = caseType;
        }

        @Override
        public LanguageNumber getNumber() {
            return number;
        }

        @Override
        public LanguageCase getCase() {
            return caseType;
        }

        @Override
        public LanguagePossessive getPossessive() {
            return LanguagePossessive.NONE;
        }

        @Override
        public LanguageArticle getArticle() {
            return LanguageArticle.ZERO;
        }

        @Override
        public String toString() {
            return "TamilNF:" + getKey();
        }

    }

    /**
     * Represents a Tamil noun. See TamilNounForm for more info.
     */
    public static class TamilNoun extends Noun {
        private static final long serialVersionUID = 1L;
        //store everything
        private transient Map<TamilNounForm, String> values = new HashMap<TamilNounForm, String>();

        TamilNoun(TamilDeclension declension, String name, String pluralAlias, NounType type, String entityName,
                  LanguageGender gender, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, LanguageStartsWith.CONSONANT,
                    gender, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        protected void setString(String value, NounForm nid) {
            values.put((TamilNounForm) nid, intern(value));
        }

        @Override
        public Map<? extends NounForm, String> getAllDefinedValues() {
            return values;
        }

        @Override
        protected boolean validateValues(String name, LanguageCase _case) {
            return defaultValidate(name, getDeclension().getFieldForms());
        }

        @Override
        public void makeSkinny() {
            values = makeSkinny(values);
        }

        /**
         * Need to override so that a cloned TamilNoun's values map is a HashMap.
         * Else, if you clone() after makeSkinny() has been called, you won't
         * be able to setString() on the cloned Noun.
         */
        @Override
        public Noun clone() {
            TamilNoun noun = (TamilNoun) super.clone();
            noun.values = new HashMap<TamilNounForm, String>(noun.values);
            return noun;
        }

        @Override
        public String getString(NounForm nid) {
            assert nid instanceof TamilNounForm : "Error: Used non-Tamil noun form to get Tamil noun.";
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
        return Collections.singletonList(SimpleModifierForm.SINGULAR);
    }

    @Override
    protected Noun createNoun(String name, String pluralAlias, NounType type, String entityName,
                              LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField,
                              boolean isCopied) {
        return new TamilNoun(this, name, pluralAlias, type, entityName, gender, access, isStandardField, isCopied);
    }

    @Override
    protected Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
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
    public EnumSet<LanguageCase> getRequiredCases() {
        return EnumSet.of(LanguageCase.NOMINATIVE,
                LanguageCase.GENITIVE,
                LanguageCase.ACCUSATIVE,
                LanguageCase.DATIVE,
                LanguageCase.ABLATIVE,
                LanguageCase.INSTRUMENTAL,
                LanguageCase.LOCATIVE);
    }

    @Override
    public boolean hasArticleInNounForm() {
        return false;
    }

}
