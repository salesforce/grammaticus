/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import java.util.*;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.google.common.collect.ImmutableList;

/**
 * Indonesian is a generally non-inflected language, except for plurals
 * Technically, this should be MalayDeclension, as Indonesian is a form of malay...
 *
 * @author stamm
 */
class IndonesianDeclension extends LanguageDeclension {
    // All the forms you can request
    static final List<? extends NounForm> ALL_FORMS = ImmutableList.copyOf(EnumSet.allOf(PluralNounForm.class));
    // All the forms you can set for "other" forms
    static final Set<? extends NounForm> OTHER_FORMS = EnumSet.of(PluralNounForm.SINGULAR);
    // All the forms you can set for "other" forms
    static final List<? extends AdjectiveForm> ADJECTIVE_FORMS = Collections.singletonList(SimpleModifierForm.SINGULAR);

    public IndonesianDeclension(HumanLanguage language) {
        super(language);
    }

    @Override
    public List<? extends NounForm> getAllNounForms() {
        return ALL_FORMS;
    }

    @Override
    public Collection<? extends NounForm> getEntityForms() {
        return getAllNounForms();
    }

    @Override
    public Collection<? extends NounForm> getFieldForms() {
        return getAllNounForms();
    }

    @Override
    public Collection<? extends NounForm> getOtherForms() {
        return OTHER_FORMS;
    }

    @Override
    public List<? extends AdjectiveForm> getAdjectiveForms() {
        return ADJECTIVE_FORMS;
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
    protected Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new SimpleAdjective(this, name);
    }

    @Override
    protected Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new SimplePluralNoun(this, name, pluralAlias, type, entityName, access, isStandardField, isCopied);
    }
}
