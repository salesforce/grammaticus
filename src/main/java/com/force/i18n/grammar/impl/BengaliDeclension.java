/*
 * Copyright (c) 2025, Salesforce, Inc.
 * SPDX-License-Identifier: Apache-2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.force.i18n.grammar.impl;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.Adjective;
import com.force.i18n.grammar.AdjectiveForm;
import com.force.i18n.grammar.Article;
import com.force.i18n.grammar.ArticleForm;
import com.force.i18n.grammar.ArticledDeclension;
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
 * Bengali is our first Indic language. Spec from Localization:
 *
 * Although this is Indo-Aryan, because of it's use of definite articles, we keep it separate.
 *
 * @author cgrabill
 */
class BengaliDeclension extends ArticledDeclension {

    private final List<BengaliNounForm> entityForms;
    private final List<BengaliNounForm> fieldForms;

    public BengaliDeclension(HumanLanguage language) {
    	super(language);
        // Generate the different forms from subclass methods
        ImmutableList.Builder<BengaliNounForm> entityBuilder = ImmutableList.builder();
        ImmutableList.Builder<BengaliNounForm> fieldBuilder = ImmutableList.builder();
        int ordinal = 0;
        for (LanguageNumber number : getAllowedNumbers()) {
            for (LanguageCase caseType : getRequiredCases()) {
                for (LanguageArticle article : getAllowedArticleTypes()) {
                    BengaliNounForm form = new BengaliNounForm(this, number, caseType, article, ordinal++);
                    entityBuilder.add(form);
                    if (caseType == LanguageCase.NOMINATIVE && article == LanguageArticle.ZERO) {
                        fieldBuilder.add(form);
                    }
                }
            }
        }
        this.entityForms = entityBuilder.build();
        this.fieldForms = fieldBuilder.build();
    }

    static class BengaliNounForm extends ComplexNounForm {
        private static final long serialVersionUID = 1L;

        private final LanguageCase caseType;
        private final LanguageNumber number;
        private final LanguageArticle article;

        BengaliNounForm(LanguageDeclension declension, LanguageNumber number, LanguageCase caseType,
                LanguageArticle article, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.caseType = caseType;
            this.article = article;
        }

        @Override public LanguageNumber getNumber() { return number; }
        @Override public LanguageCase getCase() { return caseType; }
        @Override public LanguageArticle getArticle() { return article; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }

        @Override
        public String getKey() {
            return getNumber().getDbValue() + "-" + getCase().getDbValue() + "-" + getArticle().getDbValue();
        }

        @Override
        public String toString() {
            return "BengaliNF:" + getKey();
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.caseType, this.number, this.article);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;

            if (other instanceof BengaliNounForm) {
                BengaliNounForm otherNf = BengaliNounForm.class.cast(other);
                return super.equals(other) && this.caseType == otherNf.caseType && this.number == otherNf.number
                        && this.article == otherNf.article;
            }
            return false;
        }
    }

    /**
     * Represents a Bengali noun. See BengaliNounForm for more info.
     */
    public static class BengaliNoun extends LegacyArticledNoun {
        private static final long serialVersionUID = 1L;

        //store everything
        private transient Map<BengaliNounForm, String> values = new HashMap<>();

        BengaliNoun(BengaliDeclension declension, String name, String pluralAlias, NounType type,
                String entityName, String access, LanguageGender gender,
                boolean isStandardField, boolean isCopiedFromDefault ) {
            super(declension, name, pluralAlias, type, entityName, LanguageStartsWith.CONSONANT,
                    gender, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        public String getExactString(NounForm form) {
            assert form instanceof BengaliNounForm : "Error: Used non-Bengali noun form to get Bengali noun.";
            return values.get(form);
        }

        @Override
        protected void setString(String value, NounForm nid) {
            values.put((BengaliNounForm) nid, intern(value)); //do I need to intern(value), like Hungarian?
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
         * Need to override so that a cloned BengaliNoun's values map is a HashMap.
         * Else, if you clone() after makeSkinny() has been called, you won't
         * be able to setString() on the cloned Noun.
         */
        @Override
        public Noun clone() {
            BengaliNoun noun = (BengaliNoun) super.clone();
            noun.values = new HashMap<>(noun.values);
            return noun;
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
        return getAllNounForms(); //TODO is this close?
    }

    @Override
    public List<? extends AdjectiveForm> getAdjectiveForms() {
        return SimpleDeclension.ADJECTIVE_FORMS;

    }

    @Override
    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName,
            LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField,
            boolean isCopied) {
        return new BengaliNoun(this, name, pluralAlias, type, entityName, access, gender,
                isStandardField, isCopied);
    }

    @Override
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new SimpleAdjective(this, name);
    }

    @Override
    public boolean hasGender() {
        return false;
    }

    @Override
    public boolean hasStartsWith() {
        return false;
    }

    @Override
    public EnumSet<LanguageCase> getRequiredCases() {
        return EnumSet.of(LanguageCase.NOMINATIVE,
                          LanguageCase.OBJECTIVE,
                          LanguageCase.GENITIVE,
                          LanguageCase.LOCATIVE);
    }

    @Override
    public boolean hasArticleInNounForm() {
        return true;
    }

    @Override
    public Set<LanguageArticle> getAllowedArticleTypes() {
        return EnumSet.of(LanguageArticle.ZERO, LanguageArticle.DEFINITE);
    }

    @Override
    public Article createArticle(String name, LanguageArticle articleType) {
        return new SimpleArticle(this, name, articleType);
    }

    @Override
    public List<? extends ArticleForm> getArticleForms() {
        return Collections.singletonList(SimpleModifierForm.SINGULAR);
    }

    @Override
    protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
        throw new UnsupportedOperationException("Postfixed articles must be defined with the language");
    }
}
