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

package com.force.i18n.grammar.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.force.i18n.HumanLanguage;
import com.force.i18n.Renameable;
import com.force.i18n.grammar.GrammaticalLabelSet;
import com.force.i18n.LanguageProviderFactory;

/**
 * End-to-end tests for Basque suffix rendering across cases and stem endings using attributes on terms. Converted to
 * parameterized tests to reduce repetition.
 */
public class BasqueCaseRenderingTest {

    private static final String NOUNS_GRAMMAR = """
            <noun name="House"><value plural="n">Etxe</value></noun>
            <noun name="Girl"><value plural="n">Neska</value></noun>
            <noun name="Valley"><value plural="n">Ibar</value></noun>
            <noun name="Man"><value plural="n">Gizon</value></noun>

            <noun name=\"Account\" type=\"entity\" entity=\"Account\" alias=\"Accounts\"><value plural=\"n\">Kontu</value></noun>
            <noun name=\"Contact\" type=\"entity\" entity=\"Contact\" alias=\"Contacts\"><value plural=\"n\">Kontaktu</value></noun>
            <noun name=\"User\" type=\"entity\" entity=\"User\" alias=\"Users\"><value plural=\"n\">Erabiltzaile</value></noun>
            <noun name=\"Activity\" type=\"entity\" entity=\"Activity\" alias=\"Activities\" gender=\"n\" endsWith=\"a\"><value plural=\"n\">Jarduera</value></noun>
            <noun name=\"Task\" type=\"entity\" entity=\"Task\" alias=\"Tasks\" gender=\"f\" endsWith=\"c\"><value plural=\"n\">Zeregin</value></noun>
            <noun name=\"Folder\" type=\"entity\" entity=\"Folder\" alias=\"Folders\" gender=\"f\" endsWith=\"c\"><value plural=\"n\">Karpeta</value></noun>
            <noun name=\"Leader\" type=\"entity\" entity=\"Leader\" alias=\"Leaders\" gender=\"f\" endsWith=\"c\"><value plural=\"n\">Lider</value></noun>
            <noun name=\"Enterprise\" type=\"entity\" entity=\"Enterprise\" alias=\"Enterprises\" gender=\"n\" endsWith=\"v\"><value plural=\"n\">Enpresa</value></noun>
            <noun name=\"Parent_Account\" type=\"entity\" entity=\"Account\" alias=\"Accounts\" gender=\"n\" endsWith=\"v\"><value plural=\"n\">Kontu nagusi</value></noun>
            """;

    // irregular adjectives such as demonstratives, personal pronouns, and others has extra values for each case to
    // override the default suffix-appending behavior provided by `BasqueDeclension.java`.
    private static final String ADJECTIVES_GRAMMAR = """
            <adjective name=\"New\"><value>Berri</value></adjective>
            <adjective name=\"private\"><value plural=\"n\">pribatu</value></adjective>"
            <adjective name=\"selected\"><value plural=\"n\">Hautatutako</value></adjective>
            <adjective name=\"parent\"><value plural=\"n\">nagusi</value></adjective>
            <adjective name=\"any\"><value plural=\"n\">edozein</value></adjective>
            <adjective name=\"main\"><value plural=\"n\">nagusi</value></adjective>
            <adjective name=\"public\"><value plural=\"n\">publiko</value></adjective>
            <adjective name="Big"><value plural=\"n\">handi</value></adjective>

            <adjective name=\"this\" position=\"b\">
                    <value plural=\"n\">hau</value> <!-- article="n" -->
                    <!-- Absolutive (Nominative) -->
                    <value plural=\"n\" article=\"d\">hau</value>
                    <value plural=\"y\" article=\"d\">hauek</value> <!-- these -->
                    <!-- Ergative -->
                    <value plural=\"n\" article=\"d\" case=\"er\">honek</value>
                    <!-- Dative -->
                    <value plural=\"n\" article=\"d\" case=\"d\">honi</value>
                    <!-- Genitive (Possessive) -->
                    <value plural=\"n\" article=\"d\" case=\"g\">honen</value>
                    <!-- Benefactive -->
                    <value plural=\"n\" article=\"d\" case=\"be\">honentzat</value>
                    <!-- Comitative -->
                    <value plural=\"n\" article=\"d\" case=\"com\">honekin</value>
                    <!-- Instrumental -->
                    <value plural=\"n\" article=\"d\" case=\"in\">honetaz</value>
                    <value plural=\"y\" article=\"d\" case=\"in\">hauetaz</value>
                    <!-- Inessive -->
                    <value plural=\"n\" article=\"d\" case=\"ines\">honetan</value>
                    <!-- Allative -->
                    <value plural=\"n\" article=\"d\" case=\"al\">honetara</value>
                    <!-- Ablative -->
                    <value plural=\"n\" article=\"d\" case=\"abl\">honetatik</value>
                    <!-- Local-Genitive (Locative) -->
                    <value plural=\"n\" article=\"d\" case=\"l\">honetako</value>
            </adjective>
            <adjective name=\"that\" position=\"b\">
                    <value plural=\"n\">hori</value> <!-- article="n" -->
                    <!-- Absolutive (Nominative) -->
                    <value plural=\"n\" article=\"d\">hori</value>
                    <value plural=\"y\" article=\"d\">horiek</value> <!-- those -->
                    <!-- Ergative -->
                    <value plural=\"n\" article=\"d\" case=\"er\">horrek</value>
                    <!-- Dative -->
                    <value plural=\"n\" article=\"d\" case=\"d\">horri</value>
                    <!-- Genitive (Possessive) -->
                    <value plural=\"n\" article=\"d\" case=\"g\">horren</value>
                    <!-- Benefactive -->
                    <value plural=\"n\" article=\"d\" case=\"be\">horrentzat</value>
                    <!-- Comitative -->
                    <value plural=\"n\" article=\"d\" case=\"com\">horrekin</value>
                    <!-- Instrumental -->
                    <value plural=\"n\" article=\"d\" case=\"in\">horretaz</value>
                    <value plural=\"y\" article=\"d\" case=\"in\">horietaz</value>
                    <!-- Inessive -->
                    <value plural=\"n\" article=\"d\" case=\"ines\">horretan</value>
                    <!-- Allative -->
                    <value plural=\"n\" article=\"d\" case=\"al\">horretara</value>
                    <!-- Ablative -->
                    <value plural=\"n\" article=\"d\" case=\"abl\">horretatik</value>
                    <!-- Local-Genitive (Locative) -->
                    <value plural=\"n\" article=\"d\" case=\"l\">horretako</value>
            </adjective>
            <adjective name=\"you\"  position=\"b\">
                <value plural=\"n\">zu</value>
                <!-- Absolutive (Nominative) -->
                <value plural=\"n\" article=\"d\">zu</value><!-- not "zu-a" -->
                <value plural=\"y\" article=\"d\">zuek</value><!-- not "zu-ak" -->
                <!-- Ergative -->
                <value plural=\"n\" article=\"d\" case=\"er\">zuek</value><!-- not "zu-ak" -->
                <!-- Dative -->
                <value plural=\"n\" article=\"d\" case=\"d\">zuri</value><!-- not "zu-ari" -->
                <!-- Genitive (Possessive) -->
                <value plural=\"n\" article=\"d\" case=\"g\">zuren</value><!-- not "zu-aren" -->
                <!-- Benefactive -->
                <value plural=\"n\" article=\"d\" case=\"be\">zuretzat</value><!-- not "zu-arentzat" -->
                <!-- Comitative -->
                <value plural=\"n\" article=\"d\" case=\"com\">zurekin</value><!-- not "zu-arekin" -->
                <!-- Instrumental -->
                <value plural=\"n\" article=\"d\" case=\"in\">zutaz</value><!-- not "zu-az" -->
                <value plural=\"y\" article=\"d\" case=\"in\">zuetaz</value><!-- not "zu-ez" -->
            </adjective>
            <adjective name=\"your\" position=\"b\">
                <value plural=\"n\">zure</value>
            </adjective>
            """;

    public abstract static class Base {
        protected HumanLanguage eu;
        // Use composition instead of inheritance because BaseGrammaticalLabelTest extends
        // junit.framework.TestCase (JUnit 3/4), which conflicts with JUnit 5's model and
        // can force legacy assertion resolution. Composition lets us keep this test pure JUnit 5.
        protected final BaseGrammaticalLabelTest helper = new BaseGrammaticalLabelTest("BasqueCaseRenderingTest") {
        };

        @BeforeEach
        public void setUp() throws Exception {
            helper.setUp();
            eu = LanguageProviderFactory.get().getLanguage("eu");
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class NounCases extends Base {
        Stream<Arguments> data() {
            String nouns = NOUNS_GRAMMAR;
            Object[][] arr = new Object[][] {
                    // Basic concatenation with dictionary noun
                    { "ABS sg def (dict Test)", "Kontua", "<Test article=\"d\"/>", "<noun name=\"Test\"><value plural=\"n\">Kontu</value></noun>" },
                    { "ERG sg def (dict Test)", "Kontuak", "<Test case=\"er\" article=\"d\"/>", "<noun name=\"Test\"><value plural=\"n\">Kontu</value></noun>" },
                    { "DAT sg def (dict Test)", "Kontuari", "<Test case=\"d\" article=\"d\"/>", "<noun name=\"Test\"><value plural=\"n\">Kontu</value></noun>" },

                    // Absolutive
                    { "ABS sg def account", "kontua", "<account article=\"d\"/>", nouns },
                    { "ABS sg def the user", "erabiltzailea", "<user case=\"n\" plural=\"n\" article=\"d\"/>", nouns },  // ends with vowel
                    { "ABS sg def the task", "zeregina", "<task article=\"d\"/>", nouns },  // ends with consonant
                    { "ABS sg def the folder", "karpeta", "<folder article=\"d\"/>", nouns }, // ends with 'a'
                    { "ABS sg def the leader", "liderra", "<leader article=\"d\"/>", nouns }, // ends with 'r'

                    { "ABS sg def house", "etxea", "<house article=\"d\"/>", nouns },   // ends with vowel
                    { "ABS sg def girl", "neska", "<girl article=\"d\"/>", nouns },   // ends with 'a'
                    { "ABS sg def man", "gizona", "<man article=\"d\"/>", nouns },   // ends with consonant
                    { "ABS sg def valley", "ibarra", "<valley article=\"d\"/>", nouns }, // ends with 'r'

                    { "ABS pl def the accounts", "kontuak", "<account plural=\"y\" article=\"d\"/>", nouns },
                    { "ABS pl def house", "etxeak", "<house plural=\"y\" article=\"d\"/>", nouns },
                    { "ABS pl def girl", "neskak", "<girl plural=\"y\" article=\"d\"/>", nouns },
                    { "ABS pl def valley", "ibarrak", "<valley plural=\"y\" article=\"d\"/>", nouns },
                    { "ABS pl def man", "gizonak", "<man plural=\"y\" article=\"d\"/>", nouns },

                    { "ABS sg indef house", "etxe", "<house article=\"i\"/>", nouns },
                    { "ABS sg indef girl", "neska", "<girl article=\"i\"/>", nouns },
                    { "ABS sg indef valley", "ibar", "<valley article=\"i\"/>", nouns },
                    { "ABS sg indef man", "gizon", "<man article=\"i\"/>", nouns },

                    // Ergative
                    { "ERG sg def account", "kontuak", "<account case=\"er\" article=\"d\"/>", nouns },
                    { "ERG sg def house", "etxeak", "<house case=\"er\" article=\"d\"/>", nouns },
                    { "ERG sg def girl", "neskak", "<girl case=\"er\" article=\"d\"/>", nouns },
                    { "ERG sg def valley", "ibarrak", "<valley case=\"er\" article=\"d\"/>", nouns },
                    { "ERG sg def man", "gizonak", "<man case=\"er\" article=\"d\"/>", nouns },

                    { "ERG pl def account", "kontuek", "<account case=\"er\" plural=\"y\" article=\"d\"/>", nouns },
                    { "ERG pl def house", "etxeek", "<house case=\"er\" plural=\"y\" article=\"d\"/>", nouns },
                    { "ERG pl def girl", "neskek", "<girl plural=\"y\" case=\"er\" article=\"d\"/>", nouns },
                    { "ERG pl def valley", "ibarrek", "<valley plural=\"y\" case=\"er\" article=\"d\"/>", nouns },
                    { "ERG pl def man", "gizonek", "<man plural=\"y\" case=\"er\" article=\"d\"/>", nouns },

                    { "ERG sg indef house", "etxek", "<house case=\"er\" article=\"i\"/>", nouns },
                    { "ERG sg indef girl", "neskak", "<girl case=\"er\" article=\"i\"/>", nouns },
                    { "ERG sg indef valley", "ibarrek", "<valley case=\"er\" article=\"i\"/>", nouns },
                    { "ERG sg indef man", "gizonek", "<man case=\"er\" article=\"i\"/>", nouns },

                    // Dative
                    { "DAT sg def account", "kontuari", "<account case=\"d\" article=\"d\"/>", nouns },
                    { "DAT sg def house", "etxeari", "<house case=\"d\" article=\"d\"/>", nouns },
                    { "DAT sg def girl", "neskari", "<girl case=\"d\" article=\"d\"/>", nouns },
                    { "DAT sg def valley", "ibarrari", "<valley case=\"d\" article=\"d\"/>", nouns },
                    { "DAT sg def man", "gizonari", "<man case=\"d\" article=\"d\"/>", nouns },

                    { "DAT pl def account", "kontuei", "<account plural=\"y\" case=\"d\" article=\"d\"/>", nouns },
                    { "DAT pl def house", "etxeei", "<house plural=\"y\" case=\"d\"/>", nouns },
                    { "DAT pl def girl", "neskei", "<girl plural=\"y\" case=\"d\"/>", nouns },
                    { "DAT pl def valley", "ibarrei", "<valley plural=\"y\" case=\"d\"/>", nouns },
                    { "DAT pl def man", "gizonei", "<man plural=\"y\" case=\"d\"/>", nouns },

                    { "DAT sg indef house", "etxeri", "<house case=\"d\" article=\"i\"/>", nouns },
                    { "DAT sg indef girl", "neskari", "<girl case=\"d\" article=\"i\"/>", nouns },
                    { "DAT sg indef valley", "ibarri", "<valley case=\"d\" article=\"i\"/>", nouns },
                    { "DAT sg indef man", "gizoni", "<man case=\"d\" article=\"i\"/>", nouns },

                    // Genitive
                    { "GEN sg def house", "etxearen", "<house case=\"g\" article=\"d\"/>", nouns },
                    { "GEN sg def girl", "neskaren", "<girl case=\"g\" article=\"d\"/>", nouns },
                    { "GEN sg def valley", "ibarraren", "<valley case=\"g\" article=\"d\"/>", nouns },
                    { "GEN sg def man", "gizonaren", "<man case=\"g\" article=\"d\"/>", nouns },

                    { "GEN pl def house", "etxeen", "<house plural=\"y\" case=\"g\"/>", nouns },
                    { "GEN pl def girl", "nesken", "<girl plural=\"y\" case=\"g\"/>", nouns },
                    { "GEN pl def valley", "ibarren", "<valley plural=\"y\" case=\"g\"/>", nouns },
                    { "GEN pl def man", "gizonen", "<man plural=\"y\" case=\"g\"/>", nouns },

                    { "GEN sg indef house", "etxeren", "<house case=\"g\" article=\"i\"/>", nouns },
                    { "GEN sg indef girl", "neskaren", "<girl case=\"g\" article=\"i\"/>", nouns },
                    { "GEN sg indef valley", "ibarren", "<valley case=\"g\" article=\"i\"/>", nouns },
                    { "GEN sg indef man", "gizonen", "<man case=\"g\" article=\"i\"/>", nouns },

                    // Local Genitive
                    { "LOC sg def house", "etxeko", "<house case=\"l\" article=\"d\"/>", nouns },
                    { "LOC sg def girl", "neskako", "<girl case=\"l\" article=\"d\"/>", nouns },
                    { "LOC sg def valley", "ibarreko", "<valley case=\"l\" article=\"d\"/>", nouns },
                    { "LOC sg def man", "gizoneko", "<man case=\"l\" article=\"d\"/>", nouns },

                    { "LOC pl def house", "etxeetako", "<house plural=\"y\" case=\"l\"/>", nouns },

                    { "LOC sg indef house", "etxeko", "<house case=\"l\" article=\"i\"/>", nouns },
                    { "LOC sg indef valley", "ibarreko", "<valley case=\"l\" article=\"i\"/>", nouns },

                    // Inessive
                    { "INE sg def account", "kontuan", "<account case=\"ines\" article=\"d\"/>", nouns },
                    { "INE sg def house", "etxean", "<house case=\"ines\" article=\"d\"/>", nouns },
                    { "INE sg def girl", "neskan", "<girl case=\"ines\" article=\"d\"/>", nouns },
                    { "INE sg def valley", "ibarrean", "<valley case=\"ines\" article=\"d\"/>", nouns },
                    { "INE sg def man", "gizonean", "<man case=\"ines\" article=\"d\"/>", nouns },

                    { "INE pl def account", "kontuetan", "<account plural=\"y\" case=\"ines\" article=\"d\"/>", nouns },
                    { "INE pl def house", "etxeetan", "<house plural=\"y\" case=\"ines\"/>", nouns },
                    { "INE pl def girl", "nesketan", "<girl plural=\"y\" case=\"ines\"/>", nouns },
                    { "INE pl def valley", "ibarretan", "<valley plural=\"y\" case=\"ines\"/>", nouns },
                    { "INE pl def man", "gizonetan", "<man plural=\"y\" case=\"ines\"/>", nouns },

                    { "INE sg indef house", "etxetan", "<house case=\"ines\" article=\"i\"/>", nouns },
                    { "INE sg indef girl", "neskatan", "<girl case=\"ines\" article=\"i\"/>", nouns },
                    { "INE sg indef valley", "ibarretan", "<valley case=\"ines\" article=\"i\"/>", nouns },
                    { "INE sg indef man", "gizonetan", "<man case=\"ines\" article=\"i\"/>", nouns },

                    // Allative
                    { "ALL sg def account", "kontura", "<account case=\"al\" article=\"d\"/>", nouns },
                    { "ALL sg def house", "etxera", "<house case=\"al\" article=\"d\"/>", nouns },
                    { "ALL sg def girl", "neskara", "<girl case=\"al\" article=\"d\"/>", nouns },
                    { "ALL sg def valley", "ibarrera", "<valley case=\"al\" article=\"d\"/>", nouns },
                    { "ALL sg def man", "gizonera", "<man case=\"al\" article=\"d\"/>", nouns },

                    { "ALL pl def account", "kontuetara", "<account plural=\"y\" case=\"al\" article=\"d\"/>", nouns },
                    { "ALL pl def house", "etxeetara", "<house plural=\"y\" case=\"al\"/>", nouns },
                    { "ALL pl def girl", "nesketara", "<girl plural=\"y\" case=\"al\"/>", nouns },
                    { "ALL pl def valley", "ibarretara", "<valley plural=\"y\" case=\"al\"/>", nouns },
                    { "ALL pl def man", "gizonetara", "<man plural=\"y\" case=\"al\"/>", nouns },

                    { "ALL sg indef house", "etxetara", "<house case=\"al\" article=\"i\"/>", nouns },
                    { "ALL sg indef girl", "neskatara", "<girl case=\"al\" article=\"i\"/>", nouns },
                    { "ALL sg indef valley", "ibarretara", "<valley case=\"al\" article=\"i\"/>", nouns },
                    { "ALL sg indef man", "gizonetara", "<man case=\"al\" article=\"i\"/>", nouns },

                    // Ablative
                    { "ABL sg def account", "kontutik", "<account case=\"abl\" article=\"d\"/>", nouns },
                    { "ABL sg def house", "etxetik", "<house case=\"abl\" article=\"d\"/>", nouns },
                    { "ABL sg def girl", "neskatik", "<girl case=\"abl\" article=\"d\"/>", nouns },
                    { "ABL sg def valley", "ibarretik", "<valley case=\"abl\" article=\"d\"/>", nouns },
                    { "ABL sg def man", "gizonetik", "<man case=\"abl\" article=\"d\"/>", nouns },

                    { "ABL pl def account", "kontuetatik", "<account plural=\"y\" case=\"abl\" article=\"d\"/>", nouns },
                    { "ABL pl def house", "etxeetatik", "<house plural=\"y\" case=\"abl\"/>", nouns },
                    { "ABL pl def girl", "nesketatik", "<girl plural=\"y\" case=\"abl\"/>", nouns },
                    { "ABL pl def valley", "ibarretatik", "<valley plural=\"y\" case=\"abl\"/>", nouns },
                    { "ABL pl def man", "gizonetatik", "<man plural=\"y\" case=\"abl\"/>", nouns },
                    { "ABL pl def valley (dup)", "ibarretatik", "<valley plural=\"y\" case=\"abl\"/>", nouns },

                    { "ABL sg indef house", "etxetatik", "<house case=\"abl\" article=\"i\"/>", nouns },
                    { "ABL sg indef girl", "neskatatik", "<girl case=\"abl\" article=\"i\"/>", nouns },
                    { "ABL sg indef valley", "ibarretatik", "<valley case=\"abl\" article=\"i\"/>", nouns },
                    { "ABL sg indef man", "gizonetatik", "<man case=\"abl\" article=\"i\"/>", nouns },

                    // Instrumental
                    { "INS sg def account", "kontuaz", "<account case=\"in\" article=\"d\"/>", nouns },
                    { "INS sg def house", "etxeaz", "<house case=\"in\" article=\"d\"/>", nouns },
                    { "INS sg def girl", "neskaz", "<girl case=\"in\" article=\"d\"/>", nouns },
                    { "INS sg def valley", "ibarraz", "<valley case=\"in\" article=\"d\"/>", nouns },
                    { "INS sg def man", "gizonaz", "<man case=\"in\" article=\"d\"/>", nouns },
                    { "INS sg indef house", "etxez", "<house case=\"in\" article=\"i\"/>", nouns },

                    { "INS pl def account", "kontuez", "<account plural=\"y\" case=\"in\" article=\"d\"/>", nouns },
                    { "INS pl def house", "etxeez", "<house plural=\"y\" case=\"in\"/>", nouns },
                    { "INS pl def girl", "neskez", "<girl plural=\"y\" case=\"in\"/>", nouns },
                    { "INS pl def valley", "ibarrez", "<valley plural=\"y\" case=\"in\"/>", nouns },
                    { "INS pl def man", "gizonez", "<man plural=\"y\" case=\"in\"/>", nouns },

                    // Comitative
                    { "COM sg def account", "kontuarekin", "<account case=\"com\" article=\"d\"/>", nouns },
                    { "COM sg def house", "etxearekin", "<house case=\"com\" article=\"d\"/>", nouns },
                    { "COM sg def girl", "neskarekin", "<girl case=\"com\" article=\"d\"/>", nouns },
                    { "COM sg def valley", "ibarrarekin", "<valley case=\"com\" article=\"d\"/>", nouns },
                    { "COM sg def man", "gizonarekin", "<man case=\"com\" article=\"d\"/>", nouns },
                    { "COM sg indef house", "etxerekin", "<house case=\"com\" article=\"i\"/>", nouns },

                    { "COM pl def account", "kontuekin", "<account plural=\"y\" case=\"com\" article=\"d\"/>", nouns },
                    { "COM pl def house", "etxeekin", "<house plural=\"y\" case=\"com\"/>", nouns },
                    { "COM pl def girl", "neskekin", "<girl plural=\"y\" case=\"com\"/>", nouns },
                    { "COM pl def valley", "ibarrekin", "<valley plural=\"y\" case=\"com\"/>", nouns },
                    { "COM pl def man", "gizonekin", "<man plural=\"y\" case=\"com\"/>", nouns },

                    // Benefactive
                    { "BEN sg def account", "kontuarentzat", "<account case=\"be\" article=\"d\"/>", nouns },
                    { "BEN sg def house", "etxearentzat", "<house case=\"be\" article=\"d\"/>", nouns },
                    { "BEN sg def girl", "neskarentzat", "<girl case=\"be\" article=\"d\"/>", nouns },
                    { "BEN sg def valley", "ibarrarentzat", "<valley case=\"be\" article=\"d\"/>", nouns },
                    { "BEN sg def man", "gizonarentzat", "<man case=\"be\" article=\"d\"/>", nouns },

                    { "BEN pl def account", "kontuentzat", "<account plural=\"y\" case=\"be\" article=\"d\"/>", nouns },
                    { "BEN pl def house", "etxeentzat", "<house plural=\"y\" case=\"be\"/>", nouns },

                    { "BEN sg indef house", "etxerentzat", "<house case=\"be\" article=\"i\"/>", nouns },

                    // Partitive
                    { "PAR sg indef house", "etxerik", "<house case=\"par\" article=\"i\"/>", nouns },
                    { "PAR sg indef girl", "neskarik", "<girl case=\"par\" article=\"i\"/>", nouns },
                    { "PAR sg indef valley", "ibarrik", "<valley case=\"par\" article=\"i\"/>", nouns },
                    { "PAR sg indef man", "gizonik", "<man case=\"par\" article=\"i\"/>", nouns },

                    // Compound nouns
                    { "ABS sg def parent_account", "kontu nagusia", "<parent_account article=\"d\"/>", nouns },
                    { "Three element compound word: in the enterprise account folder", "enpresaren kontuaren karpetan",
                            "<enterprise case=\"g\"/> <account case=\"g\" article=\"d\"/> <folder case=\"ines\" article=\"d\"/>", nouns },
                    { "accounts with parent accounts", "kontuak kontu nagusiekin", "<account plural=\"y\" article=\"d\"/> <parent_account plural=\"y\" article=\"d\" case=\"com\"/>", nouns },

                    // Special cases
                    { "R-double exception UR ABS", "Ura", "<Water article=\"d\"/>",
                            "<noun name=\"Water\"><value plural=\"n\">Ur</value></noun>" },
                    { "R-double exception UR GEN", "Uraren", "<Water case=\"g\" article=\"d\"/>",
                            "<noun name=\"Water\"><value plural=\"n\">Ur</value></noun>" },
                    { "R-double exception UR INE", "Urean", "<Water case=\"ines\" article=\"d\"/>",
                            "<noun name=\"Water\"><value plural=\"n\">Ur</value></noun>" },
                    { "h-final treated as vowel-final LOC", "Zahko", "<VowelH case=\"l\" article=\"i\"/>",
                            "<noun name=\"VowelH\"><value plural=\"n\">Zah</value></noun>" },
                    { "ABS pl with article=i falls back to def", "etxeak", "<house plural=\"y\" article=\"i\"/>",
                            nouns },
                };
            return Arrays.stream(arr).map(Arguments::of);
        }

        @ParameterizedTest(name = "{index}: {0}")
        @MethodSource("data")
        void rendersAsExpected(String description, String expected, String label, String grammar) throws IOException {
            assertEquals(expected, helper.renderLabel(eu, label, grammar), description);
        }

        @org.junit.jupiter.api.Test
        void staticFallbackGenerateFromTermIfMissingReturnsNull_throws() {
            String grammar = "<noun name=\"NoVal\"></noun>";
            String label = "<NoVal case=\"d\" article=\"d\"/>";
            assertThrows(AssertionError.class, () -> helper.renderLabel(eu, label, grammar));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class AdjectiveCases extends Base {
        Stream<Arguments> data() {
            Object[][] arr = new Object[][] {
                    // Absolutive and agreement
                    { "ADJ ABS sg def", "Etxe Berria", "<House/> <New article=\"d\"/>" },
                    { "ADJ ABS pl def", "etxe berriak", "<house/> <new plural=\"y\"/>" },
                    { "ADJ ABS sg indef", "etxe berri", "<house/> <new article=\"i\"/>" },

                    // Ergative
                    { "ADJ ERG sg def", "Etxe berriak", "<House/> <new case=\"er\" article=\"d\"/>" },
                    { "ADJ ERG pl def", "etxe berriek", "<house/> <new case=\"er\" plural=\"y\"/>" },
                    { "ADJ ERG sg indef", "Etxe berrik", "<House/> <new case=\"er\" article=\"i\"/>" },

                    // Dative
                    { "ADJ DAT sg def", "Etxe berriari", "<House/> <new case=\"d\" article=\"d\"/>" },
                    { "ADJ DAT pl def", "Etxe berriei", "<House/> <new case=\"d\" plural=\"y\"/>" },
                    { "ADJ DAT sg indef", "Etxe berriri", "<House/> <new case=\"d\" article=\"i\"/>" },

                    // Genitive
                    { "ADJ GEN sg def", "Etxe Berriaren", "<House/> <New case=\"g\" article=\"d\"/>" },
                    { "ADJ GEN pl def", "Etxe Berrien", "<House/> <New case=\"g\" plural=\"y\"/>" },
                    { "ADJ GEN sg indef", "Etxe Berriren", "<House/> <New case=\"g\" article=\"i\"/>" },

                    // Local Genitive
                    { "ADJ LOC sg def", "Etxe Berriko", "<House/> <New case=\"l\" article=\"d\"/>" },
                    { "ADJ LOC pl def", "Etxe Berrietako", "<House/> <New case=\"l\" plural=\"y\"/>" },
                    { "ADJ LOC sg indef", "Etxe Berriko", "<House/> <New case=\"l\" article=\"i\"/>" },

                    // Inessive
                    { "ADJ INE sg def", "Etxe Berrian", "<House/> <New case=\"ines\" article=\"d\"/>" },
                    { "ADJ INE pl def", "Etxe Berrietan", "<House/> <New case=\"ines\" plural=\"y\"/>" },
                    { "ADJ INE sg indef", "Etxe Berritan", "<House/> <New case=\"ines\" article=\"i\"/>" },

                    // Allative
                    { "ADJ ALL sg def", "Etxe Berrira", "<House/> <New case=\"al\" article=\"d\"/>" },
                    { "ADJ ALL pl def", "Etxe Berrietara", "<House/> <New case=\"al\" plural=\"y\"/>" },
                    { "ADJ ALL sg indef", "Etxe Berritara", "<House/> <New case=\"al\" article=\"i\"/>" },

                    // Ablative
                    { "ADJ ABL sg def", "Etxe Berritik", "<House/> <New case=\"abl\" article=\"d\"/>" },
                    { "ADJ ABL pl def", "Etxe Berrietatik", "<House/> <New case=\"abl\" plural=\"y\"/>" },
                    { "ADJ ABL sg indef", "Etxe Berritatik", "<House/> <New case=\"abl\" article=\"i\"/>" },

                    // Instrumental
                    { "ADJ INS sg def", "Etxe berriaz", "<House/> <new case=\"in\" article=\"d\"/>" },
                    { "ADJ INS pl def", "Etxe berriez", "<House/> <new case=\"in\" plural=\"y\"/>" },
                    { "ADJ INS sg indef", "Etxe berriz", "<House/> <new case=\"in\" article=\"i\"/>" },

                    // Comitative
                    { "ADJ COM sg def", "Etxe Berriarekin", "<House/> <New case=\"com\" article=\"d\"/>" },
                    { "ADJ COM pl def", "Etxe Berriekin", "<House/> <New case=\"com\" plural=\"y\"/>" },
                    { "ADJ COM sg indef", "Etxe Berrirekin", "<House/> <New case=\"com\" article=\"i\"/>" },

                    // Benefactive
                    { "ADJ BEN sg def", "Etxe Berriarentzat", "<House/> <New case=\"be\" article=\"d\"/>" },
                    { "ADJ BEN pl def", "Etxe Berrientzat", "<House/> <New case=\"be\" plural=\"y\"/>" },
                    { "ADJ BEN sg indef", "Etxe Berrirentzat", "<House/> <New case=\"be\" article=\"i\"/>" },

                    // Partitive
                    { "ADJ PAR sg indef", "Etxe Berririk", "<House/> <New case=\"par\" article=\"i\"/>" },

                    // Absolutive (Nominative)
                    { "ADJ this account", "kontu hau", "<account/> <this/>" },
                    { "ADJ this activity", "jarduera hau", "<activity/> <this/>" },
                    { "ADJ this task", "zeregin hau", "<task/> <this/>" },
                    { "ADJ these accounts", "kontu hauek", "<account/> <this plural=\"y\"/>" },
                    { "ABS def the selected contact", "hautatutako kontaktua", "<selected/> <contact plural=\"n\" article=\"d\"/>" },

                    // Other test cases
                    { "def + adj + vowel-ending:  the main user", "erabiltzaile nagusia", "<user/> <main article=\"d\"/>" },
                    { "def + adj + consonant-ending: the main task", "zeregin nagusia","<task/> <main article=\"d\"/>" },
                    { "def + adj + a-ending: the main folder", "karpeta nagusia","<folder/> <main article=\"d\"/>" },
                    { "def + adj + r-ending: the main leader", "lider nagusia", "<leader/> <main article=\"d\"/>" },

                    { "def + possessive noun + noun as adjective + vowel-ending: your main user", "zure erabiltzaile nagusia", "<your/> <user/> <main article=\"d\"/>" },
                    { "def + possessive noun + noun as adjective + consonant-ending: your main task", "zure zeregin nagusia", "<your/> <task/> <main article=\"d\"/>" },
                    { "def + possessive noun + noun as adjective + a-ending: your main folder", "zure karpeta nagusia", "<your/> <folder/> <main article=\"d\"/>" },
                    { "def + possessive noun + noun as adjective + r-ending: your main folder", "zure lider nagusia", "<your/> <leader/> <main article=\"d\"/>" },

                    { "demonstrative + adjective + noun: those big folders", "karpeta handi horiek", "<folder/> <big/> <that plural=\"y\"/>" },
                    { "noun + noun clause (Declined form): the enterprise account", "enpresaren kontua", "<enterprise case=\"g\"/> <account article=\"d\"/>" },
                    { "noun + noun clause (Compound form): in the enterprise account", "enpresa-kontuan", "<enterprise/>-<account case=\"ines\" article=\"d\"/>" },
                    { "noun + noun clause (Declined form): in the enterprise account", "enpresaren kontuan", "<enterprise case=\"g\"/> <account case=\"ines\" article=\"d\"/>" },

                    { "ABS noun + adj + adj: the main public account", "kontu publiko nagusia", "<account/> <public/> <main article=\"d\"/>" },
                    { "INES noun + adj + adj: in the main public account", "kontu publiko nagusian", "<account/> <public/> <main case=\"ines\" article=\"d\"/>" },

                    { "ADJ the contact's parent account", "kontaktuaren kontu nagusia", "<contact article=\"d\" case=\"g\"/> <account/> <parent article=\"d\"/>" },
                    { "ABS sg indef any account", "edozein kontu",  "<any/> <account/>" },
                    { "PAR sg indef any accounts", "edozein konturik",  "<any/> <account plural=\"n\" case=\"par\" article=\"n\"/>" },
                };
            return Arrays.stream(arr).map(Arguments::of);
        }

        @ParameterizedTest(name = "{index}: {0}")
        @MethodSource("data")
        void rendersAsExpected(String description, String expected, String label) throws IOException {
            assertEquals(expected, helper.renderLabel(eu, label, NOUNS_GRAMMAR + ADJECTIVES_GRAMMAR), description);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class DynamicEntityCases extends Base {
        Stream<Arguments> data() {
            String nouns = """
                    <noun name=\"Account\" type=\"entity\" entity=\"Account\" alias=\"accounts\"><value plural=\"n\">Kontu</value></noun>
                    <noun name=\"Contact\" type=\"entity\" entity=\"Contact\" alias=\"contacts\"><value plural=\"n\">Kontaktu</value></noun>
                    """;
            String nounsAndAdjectives = nouns + ADJECTIVES_GRAMMAR;
            Object[][] arr = new Object[][] { { "ENTITY ABS sg indef", "Kontu", "<Entity entity=\"0\"/>", nouns },
                    { "ENTITY ABS sg indef", "Kontaktu", "<Entity entity=\"1\"/>", nouns },
                    { "ENTITY DAT sg def", "Kontuari", "<Entity entity=\"0\" case=\"d\" article=\"d\"/>", nouns },
                    { "ENTITY GEN sg def", "Kontuaren", "<Entity entity=\"0\" case=\"g\" article=\"d\"/>", nouns },
                    { "ENTITY LOC sg def", "Kontuko", "<Entity entity=\"0\" case=\"l\" article=\"d\"/>", nouns },
                    { "ENTITY + ADJ sg def", "Kontu berria", "<Entity entity=\"0\"/> <new article=\"d\"/>", nounsAndAdjectives },
                    { "ENTITY + ADJ pl def", "Kontu berriak", "<Entity entity=\"0\"/> <new plural=\"y\"/>", nounsAndAdjectives },
                    // Force dynamic render to return null (out-of-range index), then exercise fallback
                    // generateFromTermIfMissing
                    { "ENTITY FALLBACK via generateFromTermIfMissing (DAT sg def)", "Kontuari",
                            "<Entity entity=\"2\" case=\"d\" article=\"d\"/>", nouns }, };
            return Arrays.stream(arr).map(Arguments::of);
        }

        @ParameterizedTest(name = "{index}: {0}")
        @MethodSource("data")
        void rendersAsExpected(String description, String expected, String label, String grammar) throws IOException {
            GrammaticalLabelSet set = helper.getTestLabelSet(eu, label, grammar);
            Renameable account = new BaseGrammaticalLabelTest.MockExistingRenameable("Account", set.getDictionary());
            Renameable contact = new BaseGrammaticalLabelTest.MockExistingRenameable("Contact", set.getDictionary());

            assertEquals(expected, helper.getValue(eu, label, new Renameable[] { account, contact }, grammar),
                    description);
        }
    }
}
