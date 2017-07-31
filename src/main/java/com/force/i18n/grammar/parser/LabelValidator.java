/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.force.i18n.*;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.GrammaticalTerm.TermType;
import com.force.i18n.grammar.impl.LanguageDeclensionFactory;
import com.force.i18n.grammar.parser.GrammaticalLabelFileParser.AliasParam;
import com.force.i18n.settings.MapPropertyFileData;

/**
 * A class you can use to validate that labels are correctly formatted, have the right aliases,
 * and manage runtime test label management
 */
public class LabelValidator {
    // Make it concurrent to allow reuse from a service
    private Map<HumanLanguage, LanguageDictionary> dictionaryMap = new ConcurrentHashMap<HumanLanguage, LanguageDictionary>(64, 0.75f, 1);
    private final GrammaticalLabelSetDescriptor baseDesc;
    private final GrammaticalLabelSetProvider parentSet;

    public LabelValidator(GrammaticalLabelSetDescriptor baseDesc, GrammaticalLabelSetProvider parentSet) {
        this.baseDesc = baseDesc;
        this.parentSet = parentSet;
    }


    // A simple comparator
    Comparator<AliasParam> ALIAS_COMPARATOR = new Comparator<AliasParam>() {
        @Override
        public int compare(AliasParam o1, AliasParam o2) {
            int fileCompare =  o1.file.getPath().compareTo(o2.file.getPath());
            if (fileCompare == 0) {
               if (o1.lineNumber<o2.lineNumber) {
                return -1;
            } else if (o1.lineNumber==o2.lineNumber) {
                return 0;
            } else {
                return 1;
            }
            } else {
                return fileCompare;
            }
        }
    };

    public List<String> getInvalidAliases(HumanLanguage language) throws IOException {
        List<String> result = new ArrayList<>();
        GrammaticalLabelFileParser parser = getParser(language, false);
        if (parser.getIllegalAliases() != null) {
            Collections.sort(parser.getIllegalAliases(), ALIAS_COMPARATOR);
            for (AliasParam param : parser.getIllegalAliases()) {
                result.add(param.toString());
            }
        }
        return result;
    }

    public List<String> getDuplicates(HumanLanguage language) throws IOException {
        GrammaticalLabelFileParser parser = getParser(language, true);
        return parser.writeDuplicateLabelsFile(null);
    }

    public List<String> getInvalidLabels(HumanLanguage language) throws Exception {
        List<String> result = new ArrayList<>();
        GrammaticalLabelFileParser parser = getParser(language, false);
        if (parser.getInvalidLabels() != null) {
            for (LabelReference ref : parser.getInvalidLabels()) {
                result.add(ref.toString());
            }
        }
        return result;
    }

    /**
     * Validate that the referenced case forms exist for the nouns in the label sets
     * @param sets the label sets to validate
     * @return a set of error messages; if empty, it's all good.
     */
    public List<String> validateReferencedCaseFormsExistTest(List<GrammaticalLabelSet> sets) {
        List<String> errMsgs = new ArrayList<String>();

        for (GrammaticalLabelSet ls : sets) {
            LanguageDeclension declension = LanguageDeclensionFactory.get().getDeclension(ls.getDictionary().getLanguage());
            if (!declension.isInflected()) continue;  // Don't bother with simple declensions

            Set<String> inheritedNouns = ls.getDictionary().getAllInheritedTermNames(TermType.Noun);

            // Unwrap the layers of falling back: NOTE, this only works one level deep which is fine for now.
            GrammaticalLabelSet main = ls;
            if (main instanceof GrammaticalLabelSetFallbackImpl) {
                main = ((GrammaticalLabelSetFallbackImpl)main).getOverlay();
            }

            // Iterate through the label set
            for (String section : ls.sectionNames()) {
                Map<String, Object> sectionMap = ls.getSection(section);
                if (sectionMap == null) continue;

                for (Map.Entry<String,Object> entry : sectionMap.entrySet()) {
                    if (!(entry.getValue() instanceof List)) continue;  // Only care if there's an adjective
                    for (Object o : (List<?>)entry.getValue()) {
                        if (o instanceof TermRefTag && ((TermRefTag)o).isNoun()) {
                            TermRefTag nrt = (TermRefTag) o;
                            String name = nrt.getName();
                            if (inheritedNouns.contains(name)) continue;  // We only case about actual values.
                            // TODO SLT: Reenable
                            NounForm form = (NounForm) nrt.getForm(ls.getDictionary(), true);
                            if (form instanceof LegacyArticledNounForm) form = ((LegacyArticledNounForm)form).getBaseNounForm();  // Unwrap the base form
                            if (!declension.getEntityForms().contains(form)) continue;  // Ignore autoderived forms
                            Noun noun = ls.getDictionary().getNoun(name, false);
                            if (!noun.getAllDefinedValues().keySet().contains(form)) {
                                errMsgs.add(ls.getDictionary().getLanguage().getLocaleString() + ":" + section + "." + entry.getKey() + ":" + ls.getLabelSectionToFilename().get(section)
                                    + " form " + LabelUtils.get().getFormDescriptionInEnglish(declension, form) + " for noun " + name );
                            }
                        }
                    }
                }
            }
        }
        return errMsgs;
    }


    /**
     * @param language the language to load for the parser
     * @throws IOException if there was a problem while parsing
     * @return A fully formed label file parser *after* it has been parser
     */
    public GrammaticalLabelFileParser getParser(HumanLanguage language, boolean trackDupes) throws IOException {
        GrammaticalLabelSetDescriptor desc = language == baseDesc.getLanguage() ? baseDesc : baseDesc.getForOtherLanguage(language);
        LanguageDictionary dictionary = loadDictionary(desc);
        GrammaticalLabelFileParser parser = new GrammaticalLabelFileParser(dictionary, desc, parentSet, trackDupes);

        // This constructor loads the parser; calling just parser.load() required both data and meta-data.
        new GrammaticalLabelSetImpl(dictionary, parser, new MapPropertyFileData(language.getLocale()));

        return parser;
    }

    public String renderLabel(HumanLanguage language, String text) throws IOException {
        return renderLabel(language, text, null);
    }


    /**
     * Render the label for the given language with the appropriate override
     */
    public String renderLabel(HumanLanguage language, String text, String grammarOverride) throws IOException {
        return getTestLabelSet(language, text, grammarOverride).getString("Test", "Test");
    }

    public String renderDynamicLabel(HumanLanguage language, String text, Renameable... entities) throws IOException {
        return getTestLabelSet(language, text, null).getString("Test", entities, "Test");
    }

    public GrammaticalLabelSet getTestLabelSet(HumanLanguage language, String text, String grammarOverride) throws IOException {
        TestLanguageLabelSetDescriptor testDesc = new TestLanguageLabelSetDescriptor(baseDesc.getForOtherLanguage(language), LabelUtils.getSampleLabelFile(text), LabelUtils.getSampleGrammarFile(grammarOverride));

        return GrammaticalLabelSetImpl.getTestLabelSet(testDesc);
    }


    public LanguageDictionary loadDictionary(GrammaticalLabelSetDescriptor dictDesc) throws IOException {
        if (!dictionaryMap.containsKey(dictDesc.getLanguage())) {
            LanguageDictionaryParser parser = new LanguageDictionaryParser(dictDesc, dictDesc.getLanguage(), parentSet);
            dictionaryMap.put(dictDesc.getLanguage(), parser.getDictionary());
        }
        return dictionaryMap.get(dictDesc.getLanguage());
    }


    LanguageDictionary loadDictionaryNoThrow(GrammaticalLabelSetDescriptor dictDesc) {
        try {
            return loadDictionary(dictDesc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
