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

package com.force.i18n.grammar.tools;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.impl.LanguageDeclensionFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;


/**
 * Autogenerate the names.dtd that go with each language based on the
 * LanguageDeclension defined in code.  This allows the translators to perform
 * a rudimentary validation of the names.xml files.
 *
 * @author stamm
 * @since 0.6.0
 */
public class NamesDtdGenerator {

    private List<String> getGenderAttributesFromLanguage(LanguageDeclension decl) {
        List<String> result = new ArrayList<String>();
        EnumSet<LanguageGender> genders = decl.getRequiredGenders() != null ? decl.getRequiredGenders() : EnumSet.of(decl.getDefaultGender());
        for (LanguageGender gender : genders) {
            result.add(gender.getDbValue());
            for (String alias : gender.getAliases()) {
                result.add(alias);  // TODO: Make it so that "c" and "e" are only for Dutch bzw Swedish somehow.
            }
        }
        return result;
    }

    private List<String> getStartsWithAttributesFromLanguage(LanguageDeclension decl) {
        List<String> result = new ArrayList<String>();
        // Compromise with PM: Allow DTD to specify "random values" for starts with if no one cares
        EnumSet<LanguageStartsWith> startsWiths = decl.hasStartsWith() ? decl.getRequiredStartsWith() : EnumSet.of(LanguageStartsWith.CONSONANT, LanguageStartsWith.VOWEL);
        for (LanguageStartsWith startsWith : startsWiths) {
            result.add(startsWith.getDbValue());
        }
        return result;
    }

    private List<String> getPossessiveAttributesFromLanguage(LanguageDeclension decl) {
        List<String> result = new ArrayList<String>();
        for (LanguagePossessive possessive : decl.getRequiredPossessive()) {
            result.add(possessive.getDbValue());
        }
        return result;
    }

    private List<String> getCaseAttributesFromLanguage(LanguageDeclension decl) {
        List<String> result = new ArrayList<String>();
        for (LanguageCase caseType : decl.getAllowedCases()) {
            result.add(caseType.getDbValue());
        }
        return result;
    }

    public void generate(String prefix, String directory) throws Exception {
        // Now generate the two files
        Configuration config = new Configuration();
        config.setClassForTemplateLoading(getClass(), "/com/force/i18n/grammar");

        Template template = config.getTemplate("names.dtd.ftl");

        IllegalStateException savedE = null;
        for (HumanLanguage language : LanguageProviderFactory.get().getAll()) {
            boolean isBase = language == LanguageProviderFactory.get().getBaseLanguage();

            Map<String,Object> context = new HashMap<String,Object>();

            LanguageDeclension decl = LanguageDeclensionFactory.get().getDeclension(language);
            EnumSet<LanguageGender> genders = decl.getRequiredGenders();
            context.put("prefix", "");
            context.put("decl", decl);
            context.put("isEnglish", isBase);
            context.put("genderOrs", getGenderAttributesFromLanguage(decl).stream().collect(Collectors.joining(" | ")));
            context.put("startsWithOrs", getStartsWithAttributesFromLanguage(decl).stream().collect(Collectors.joining(" | ")));
            context.put("hasNounClasses", genders != null && genders.contains(LanguageGender.CLASS_I));
            context.put("adjectives", false);
            if (decl.hasAllowedCases()) context.put("caseOrs", getCaseAttributesFromLanguage(decl).stream().collect(Collectors.joining(" | ")));
            if (decl.hasPossessive()) context.put("possessiveOrs", getPossessiveAttributesFromLanguage(decl).stream().collect(Collectors.joining(" | ")));


            File outDir = new File(directory + "/" + language.getDefaultLabelDirectoryPath());
            outDir.mkdirs();
            try (FileWriter out = new FileWriter(new File(outDir, "names.dtd"))){
            	template.process(context, out);
            } catch (IllegalStateException e) {
                if (savedE == null) savedE = e;
            }

            // Create the ajectives.dtd
            context.put("adjectives", true);
            try (FileWriter out = new FileWriter(new File(outDir, "adjective.dtd"))){
            	template.process(context, out);
            } catch (IllegalStateException e) {
                if (savedE == null) savedE = e;
            }
        }
        if (savedE != null) throw savedE;
    }

}
