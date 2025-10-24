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
 * End-to-end tests for Basque suffix rendering across cases and stem endings using attributes on terms.
 * Converted to parameterized tests to reduce repetition.
 */
public class BasqueCaseRenderingTest {

    private static final String NOUNS_GRAMMAR = """
        <noun name="House"><value plural="n">Etxe</value></noun>
        <noun name="Girl"><value plural="n">Neska</value></noun>
        <noun name="Valley"><value plural="n">Ibar</value></noun>
        <noun name="Man"><value plural="n">Gizon</value></noun>
        """;
    private static final String ADJECTIVES_GRAMMAR = "<adjective name=\"New\"><value>Berri</value></adjective>";

    public abstract static class Base {
        protected HumanLanguage eu;
        // Use composition instead of inheritance because BaseGrammaticalLabelTest extends
        // junit.framework.TestCase (JUnit 3/4), which conflicts with JUnit 5's model and
        // can force legacy assertion resolution. Composition lets us keep this test pure JUnit 5.
        protected final BaseGrammaticalLabelTest helper = new BaseGrammaticalLabelTest("BasqueCaseRenderingTest") {};

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
            Object[][] arr = new Object[][]{
                // Basic concatenation with dictionary noun
                {"ABS sg def (dict Test)", "Kontua", "<Test article=\"d\"/>", "<noun name=\"Test\"><value plural=\"n\">Kontu</value></noun>"},
                {"ERG sg def (dict Test)", "Kontuak", "<Test case=\"er\" article=\"d\"/>", "<noun name=\"Test\"><value plural=\"n\">Kontu</value></noun>"},
                {"DAT sg def (dict Test)", "Kontuari", "<Test case=\"d\" article=\"d\"/>", "<noun name=\"Test\"><value plural=\"n\">Kontu</value></noun>"},

                // Absolutive
                {"ABS sg def house", "etxea", "<house article=\"d\"/>", nouns},
                {"ABS sg def girl", "neska", "<girl article=\"d\"/>", nouns},
                {"ABS sg def valley", "ibarra", "<valley article=\"d\"/>", nouns},
                {"ABS sg def man", "gizona", "<man article=\"d\"/>", nouns},
                {"ABS pl def house", "etxeak", "<house plural=\"y\" article=\"d\"/>", nouns},
                {"ABS pl def girl", "neskak", "<girl plural=\"y\" article=\"d\"/>", nouns},
                {"ABS pl def valley", "ibarrak", "<valley plural=\"y\" article=\"d\"/>", nouns},
                {"ABS pl def man", "gizonak", "<man plural=\"y\" article=\"d\"/>", nouns},
                {"ABS sg indef house", "etxe", "<house article=\"i\"/>", nouns},
                {"ABS sg indef girl", "neska", "<girl article=\"i\"/>", nouns},
                {"ABS sg indef valley", "ibar", "<valley article=\"i\"/>", nouns},
                {"ABS sg indef man", "gizon", "<man article=\"i\"/>", nouns},

                // Ergative
                {"ERG sg def house", "etxeak", "<house case=\"er\" article=\"d\"/>", nouns},
                {"ERG sg def girl", "neskak", "<girl case=\"er\" article=\"d\"/>", nouns},
                {"ERG sg def valley", "ibarrak", "<valley case=\"er\" article=\"d\"/>", nouns},
                {"ERG sg def man", "gizonak", "<man case=\"er\" article=\"d\"/>", nouns},
                {"ERG pl def house", "etxeek", "<house case=\"er\" plural=\"y\" article=\"d\"/>", nouns},
                {"ERG pl def girl", "neskek", "<girl plural=\"y\" case=\"er\" article=\"d\"/>", nouns},
                {"ERG pl def valley", "ibarrek", "<valley plural=\"y\" case=\"er\" article=\"d\"/>", nouns},
                {"ERG pl def man", "gizonek", "<man plural=\"y\" case=\"er\" article=\"d\"/>", nouns},
                {"ERG sg indef house", "etxek", "<house case=\"er\" article=\"i\"/>", nouns},
                {"ERG sg indef girl", "neskak", "<girl case=\"er\" article=\"i\"/>", nouns},
                {"ERG sg indef valley", "ibarrek", "<valley case=\"er\" article=\"i\"/>", nouns},
                {"ERG sg indef man", "gizonek", "<man case=\"er\" article=\"i\"/>", nouns},

                // Dative
                {"DAT sg def house", "etxeari", "<house case=\"d\" article=\"d\"/>", nouns},
                {"DAT sg def girl", "neskari", "<girl case=\"d\" article=\"d\"/>", nouns},
                {"DAT sg def valley", "ibarrari", "<valley case=\"d\" article=\"d\"/>", nouns},
                {"DAT sg def man", "gizonari", "<man case=\"d\" article=\"d\"/>", nouns},
                {"DAT pl def house", "etxeei", "<house plural=\"y\" case=\"d\"/>", nouns},
                {"DAT pl def girl", "neskei", "<girl plural=\"y\" case=\"d\"/>", nouns},
                {"DAT pl def valley", "ibarrei", "<valley plural=\"y\" case=\"d\"/>", nouns},
                {"DAT pl def man", "gizonei", "<man plural=\"y\" case=\"d\"/>", nouns},
                {"DAT sg indef house", "etxeri", "<house case=\"d\" article=\"i\"/>", nouns},
                {"DAT sg indef girl", "neskari", "<girl case=\"d\" article=\"i\"/>", nouns},
                {"DAT sg indef valley", "ibarri", "<valley case=\"d\" article=\"i\"/>", nouns},
                {"DAT sg indef man", "gizoni", "<man case=\"d\" article=\"i\"/>", nouns},

                // Genitive
                {"GEN sg def house", "etxearen", "<house case=\"g\" article=\"d\"/>", nouns},
                {"GEN sg def girl", "neskaren", "<girl case=\"g\" article=\"d\"/>", nouns},
                {"GEN sg def valley", "ibarraren", "<valley case=\"g\" article=\"d\"/>", nouns},
                {"GEN sg def man", "gizonaren", "<man case=\"g\" article=\"d\"/>", nouns},
                {"GEN pl def house", "etxeen", "<house plural=\"y\" case=\"g\"/>", nouns},
                {"GEN pl def girl", "nesken", "<girl plural=\"y\" case=\"g\"/>", nouns},
                {"GEN pl def valley", "ibarren", "<valley plural=\"y\" case=\"g\"/>", nouns},
                {"GEN pl def man", "gizonen", "<man plural=\"y\" case=\"g\"/>", nouns},
                {"GEN sg indef house", "etxeren", "<house case=\"g\" article=\"i\"/>", nouns},
                {"GEN sg indef girl", "neskaren", "<girl case=\"g\" article=\"i\"/>", nouns},
                {"GEN sg indef valley", "ibarren", "<valley case=\"g\" article=\"i\"/>", nouns},
                {"GEN sg indef man", "gizonen", "<man case=\"g\" article=\"i\"/>", nouns},

                // Local Genitive
                {"LOC sg def house", "etxeko", "<house case=\"l\" article=\"d\"/>", nouns},
                {"LOC sg def girl", "neskako", "<girl case=\"l\" article=\"d\"/>", nouns},
                {"LOC sg def valley", "ibarreko", "<valley case=\"l\" article=\"d\"/>", nouns},
                {"LOC sg def man", "gizoneko", "<man case=\"l\" article=\"d\"/>", nouns},
                {"LOC pl def house", "etxeetako", "<house plural=\"y\" case=\"l\"/>", nouns},
                {"LOC sg indef house", "etxeko", "<house case=\"l\" article=\"i\"/>", nouns},
                {"LOC sg indef valley", "ibarreko", "<valley case=\"l\" article=\"i\"/>", nouns},

                // Inessive
                {"INE sg def house", "etxean", "<house case=\"ines\" article=\"d\"/>", nouns},
                {"INE sg def girl", "neskan", "<girl case=\"ines\" article=\"d\"/>", nouns},
                {"INE sg def valley", "ibarrean", "<valley case=\"ines\" article=\"d\"/>", nouns},
                {"INE sg def man", "gizonean", "<man case=\"ines\" article=\"d\"/>", nouns},
                {"INE pl def house", "etxeetan", "<house plural=\"y\" case=\"ines\"/>", nouns},
                {"INE pl def girl", "nesketan", "<girl plural=\"y\" case=\"ines\"/>", nouns},
                {"INE pl def valley", "ibarretan", "<valley plural=\"y\" case=\"ines\"/>", nouns},
                {"INE pl def man", "gizonetan", "<man plural=\"y\" case=\"ines\"/>", nouns},
                {"INE sg indef house", "etxetan", "<house case=\"ines\" article=\"i\"/>", nouns},
                {"INE sg indef girl", "neskatan", "<girl case=\"ines\" article=\"i\"/>", nouns},
                {"INE sg indef valley", "ibarretan", "<valley case=\"ines\" article=\"i\"/>", nouns},
                {"INE sg indef man", "gizonetan", "<man case=\"ines\" article=\"i\"/>", nouns},

                // Allative
                {"ALL sg def house", "etxera", "<house case=\"al\" article=\"d\"/>", nouns},
                {"ALL sg def girl", "neskara", "<girl case=\"al\" article=\"d\"/>", nouns},
                {"ALL sg def valley", "ibarrera", "<valley case=\"al\" article=\"d\"/>", nouns},
                {"ALL sg def man", "gizonera", "<man case=\"al\" article=\"d\"/>", nouns},
                {"ALL pl def house", "etxeetara", "<house plural=\"y\" case=\"al\"/>", nouns},
                {"ALL pl def girl", "nesketara", "<girl plural=\"y\" case=\"al\"/>", nouns},
                {"ALL pl def valley", "ibarretara", "<valley plural=\"y\" case=\"al\"/>", nouns},
                {"ALL pl def man", "gizonetara", "<man plural=\"y\" case=\"al\"/>", nouns},
                {"ALL sg indef house", "etxetara", "<house case=\"al\" article=\"i\"/>", nouns},
                {"ALL sg indef girl", "neskatara", "<girl case=\"al\" article=\"i\"/>", nouns},
                {"ALL sg indef valley", "ibarretara", "<valley case=\"al\" article=\"i\"/>", nouns},
                {"ALL sg indef man", "gizonetara", "<man case=\"al\" article=\"i\"/>", nouns},

                // Ablative
                {"ABL sg def house", "etxetik", "<house case=\"abl\" article=\"d\"/>", nouns},
                {"ABL sg def girl", "neskatik", "<girl case=\"abl\" article=\"d\"/>", nouns},
                {"ABL sg def valley", "ibarretik", "<valley case=\"abl\" article=\"d\"/>", nouns},
                {"ABL sg def man", "gizonetik", "<man case=\"abl\" article=\"d\"/>", nouns},
                {"ABL pl def house", "etxeetatik", "<house plural=\"y\" case=\"abl\"/>", nouns},
                {"ABL pl def girl", "nesketatik", "<girl plural=\"y\" case=\"abl\"/>", nouns},
                {"ABL pl def valley", "ibarretatik", "<valley plural=\"y\" case=\"abl\"/>", nouns},
                {"ABL pl def man", "gizonetatik", "<man plural=\"y\" case=\"abl\"/>", nouns},
                {"ABL pl def valley (dup)", "ibarretatik", "<valley plural=\"y\" case=\"abl\"/>", nouns},
                {"ABL sg indef house", "etxetatik", "<house case=\"abl\" article=\"i\"/>", nouns},
                {"ABL sg indef girl", "neskatatik", "<girl case=\"abl\" article=\"i\"/>", nouns},
                {"ABL sg indef valley", "ibarretatik", "<valley case=\"abl\" article=\"i\"/>", nouns},
                {"ABL sg indef man", "gizonetatik", "<man case=\"abl\" article=\"i\"/>", nouns},

                // Instrumental
                {"INS sg def house", "etxeaz", "<house case=\"in\" article=\"d\"/>", nouns},
                {"INS sg def girl", "neskaz", "<girl case=\"in\" article=\"d\"/>", nouns},
                {"INS sg def valley", "ibarraz", "<valley case=\"in\" article=\"d\"/>", nouns},
                {"INS sg def man", "gizonaz", "<man case=\"in\" article=\"d\"/>", nouns},
                {"INS sg indef house", "etxez", "<house case=\"in\" article=\"i\"/>", nouns},
                {"INS pl def house", "etxeez", "<house plural=\"y\" case=\"in\"/>", nouns},
                {"INS pl def girl", "neskez", "<girl plural=\"y\" case=\"in\"/>", nouns},
                {"INS pl def valley", "ibarrez", "<valley plural=\"y\" case=\"in\"/>", nouns},
                {"INS pl def man", "gizonez", "<man plural=\"y\" case=\"in\"/>", nouns},

                // Comitative
                {"COM sg def house", "etxearekin", "<house case=\"com\" article=\"d\"/>", nouns},
                {"COM sg def girl", "neskarekin", "<girl case=\"com\" article=\"d\"/>", nouns},
                {"COM sg def valley", "ibarrarekin", "<valley case=\"com\" article=\"d\"/>", nouns},
                {"COM sg def man", "gizonarekin", "<man case=\"com\" article=\"d\"/>", nouns},
                {"COM sg indef house", "etxerekin", "<house case=\"com\" article=\"i\"/>", nouns},
                {"COM pl def house", "etxeekin", "<house plural=\"y\" case=\"com\"/>", nouns},
                {"COM pl def girl", "neskekin", "<girl plural=\"y\" case=\"com\"/>", nouns},
                {"COM pl def valley", "ibarrekin", "<valley plural=\"y\" case=\"com\"/>", nouns},
                {"COM pl def man", "gizonekin", "<man plural=\"y\" case=\"com\"/>", nouns},

                // Benefactive
                {"BEN sg def house", "etxearentzat", "<house case=\"be\" article=\"d\"/>", nouns},
                {"BEN sg def girl", "neskarentzat", "<girl case=\"be\" article=\"d\"/>", nouns},
                {"BEN sg def valley", "ibarrarentzat", "<valley case=\"be\" article=\"d\"/>", nouns},
                {"BEN sg def man", "gizonarentzat", "<man case=\"be\" article=\"d\"/>", nouns},
                {"BEN pl def house", "etxeentzat", "<house plural=\"y\" case=\"be\"/>", nouns},
                {"BEN sg indef house", "etxerentzat", "<house case=\"be\" article=\"i\"/>", nouns},

                // Partitive
                {"PAR sg indef house", "etxerik", "<house case=\"par\" article=\"i\"/>", nouns},
                {"PAR sg indef girl", "neskarik", "<girl case=\"par\" article=\"i\"/>", nouns},
                {"PAR sg indef valley", "ibarrik", "<valley case=\"par\" article=\"i\"/>", nouns},
                {"PAR sg indef man", "gizonik", "<man case=\"par\" article=\"i\"/>", nouns},

                // Special cases
                {"R-double exception UR ABS", "Ura", "<Water article=\"d\"/>", "<noun name=\"Water\"><value plural=\"n\">Ur</value></noun>"},
                {"R-double exception UR GEN", "Uraren", "<Water case=\"g\" article=\"d\"/>", "<noun name=\"Water\"><value plural=\"n\">Ur</value></noun>"},
                {"R-double exception UR INE", "Urean", "<Water case=\"ines\" article=\"d\"/>", "<noun name=\"Water\"><value plural=\"n\">Ur</value></noun>"},
                {"h-final treated as vowel-final LOC", "Zahko", "<VowelH case=\"l\" article=\"i\"/>", "<noun name=\"VowelH\"><value plural=\"n\">Zah</value></noun>"},
                {"ABS pl with article=i falls back to def", "etxeak", "<house plural=\"y\" article=\"i\"/>", nouns},
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
            String nounsAndAdjectives = NOUNS_GRAMMAR + ADJECTIVES_GRAMMAR;
            Object[][] arr = new Object[][]{
                // Absolutive and agreement
                {"ADJ ABS sg def", "Etxe Berria", "<House/> <New article=\"d\"/>", nounsAndAdjectives},
                {"ADJ ABS pl def", "etxe berriak", "<house/> <new plural=\"y\"/>", nounsAndAdjectives},
                {"ADJ ABS sg indef", "etxe berri", "<house/> <new article=\"i\"/>", nounsAndAdjectives},

                // Ergative
                {"ADJ ERG sg def", "Etxe berriak", "<House/> <new case=\"er\" article=\"d\"/>", nounsAndAdjectives},
                {"ADJ ERG pl def", "etxe berriek", "<house/> <new case=\"er\" plural=\"y\"/>", nounsAndAdjectives},
                {"ADJ ERG sg indef", "Etxe berrik", "<House/> <new case=\"er\" article=\"i\"/>", nounsAndAdjectives},

                // Dative
                {"ADJ DAT sg def", "Etxe berriari", "<House/> <new case=\"d\" article=\"d\"/>", nounsAndAdjectives},
                {"ADJ DAT pl def", "Etxe berriei", "<House/> <new case=\"d\" plural=\"y\"/>", nounsAndAdjectives},
                {"ADJ DAT sg indef", "Etxe berriri", "<House/> <new case=\"d\" article=\"i\"/>", nounsAndAdjectives},

                // Genitive
                {"ADJ GEN sg def", "Etxe Berriaren", "<House/> <New case=\"g\" article=\"d\"/>", nounsAndAdjectives},
                {"ADJ GEN pl def", "Etxe Berrien", "<House/> <New case=\"g\" plural=\"y\"/>", nounsAndAdjectives},
                {"ADJ GEN sg indef", "Etxe Berriren", "<House/> <New case=\"g\" article=\"i\"/>", nounsAndAdjectives},

                // Local Genitive
                {"ADJ LOC sg def", "Etxe Berriko", "<House/> <New case=\"l\" article=\"d\"/>", nounsAndAdjectives},
                {"ADJ LOC pl def", "Etxe Berrietako", "<House/> <New case=\"l\" plural=\"y\"/>", nounsAndAdjectives},
                {"ADJ LOC sg indef", "Etxe Berriko", "<House/> <New case=\"l\" article=\"i\"/>", nounsAndAdjectives},

                // Inessive
                {"ADJ INE sg def", "Etxe Berrian", "<House/> <New case=\"ines\" article=\"d\"/>", nounsAndAdjectives},
                {"ADJ INE pl def", "Etxe Berrietan", "<House/> <New case=\"ines\" plural=\"y\"/>", nounsAndAdjectives},
                {"ADJ INE sg indef", "Etxe Berritan", "<House/> <New case=\"ines\" article=\"i\"/>", nounsAndAdjectives},

                // Allative
                {"ADJ ALL sg def", "Etxe Berrira", "<House/> <New case=\"al\" article=\"d\"/>", nounsAndAdjectives},
                {"ADJ ALL pl def", "Etxe Berrietara", "<House/> <New case=\"al\" plural=\"y\"/>", nounsAndAdjectives},
                {"ADJ ALL sg indef", "Etxe Berritara", "<House/> <New case=\"al\" article=\"i\"/>", nounsAndAdjectives},

                // Ablative
                {"ADJ ABL sg def", "Etxe Berritik", "<House/> <New case=\"abl\" article=\"d\"/>", nounsAndAdjectives},
                {"ADJ ABL pl def", "Etxe Berrietatik", "<House/> <New case=\"abl\" plural=\"y\"/>", nounsAndAdjectives},
                {"ADJ ABL sg indef", "Etxe Berritatik", "<House/> <New case=\"abl\" article=\"i\"/>", nounsAndAdjectives},

                // Instrumental
                {"ADJ INS sg def", "Etxe berriaz", "<House/> <new case=\"in\" article=\"d\"/>", nounsAndAdjectives},
                {"ADJ INS pl def", "Etxe berriez", "<House/> <new case=\"in\" plural=\"y\"/>", nounsAndAdjectives},
                {"ADJ INS sg indef", "Etxe berriz", "<House/> <new case=\"in\" article=\"i\"/>", nounsAndAdjectives},

                // Comitative
                {"ADJ COM sg def", "Etxe Berriarekin", "<House/> <New case=\"com\" article=\"d\"/>", nounsAndAdjectives},
                {"ADJ COM pl def", "Etxe Berriekin", "<House/> <New case=\"com\" plural=\"y\"/>", nounsAndAdjectives},
                {"ADJ COM sg indef", "Etxe Berrirekin", "<House/> <New case=\"com\" article=\"i\"/>", nounsAndAdjectives},

                // Benefactive
                {"ADJ BEN sg def", "Etxe Berriarentzat", "<House/> <New case=\"be\" article=\"d\"/>", nounsAndAdjectives},
                {"ADJ BEN pl def", "Etxe Berrientzat", "<House/> <New case=\"be\" plural=\"y\"/>", nounsAndAdjectives},
                {"ADJ BEN sg indef", "Etxe Berrirentzat", "<House/> <New case=\"be\" article=\"i\"/>", nounsAndAdjectives},

                // Partitive
                {"ADJ PAR sg indef", "Etxe Berririk", "<House/> <New case=\"par\" article=\"i\"/>", nounsAndAdjectives},
            };
            return Arrays.stream(arr).map(Arguments::of);
        }

        @ParameterizedTest(name = "{index}: {0}")
        @MethodSource("data")
        void rendersAsExpected(String description, String expected, String label, String grammar) throws IOException {
            assertEquals(expected, helper.renderLabel(eu, label, grammar), description);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class DynamicEntityCases extends Base {
        Stream<Arguments> data() {
            String nouns = """
                <noun name=\"account\"><value plural=\"n\">Kontu</value></noun>
                <noun name=\"entity\"><value plural=\"n\">Kontu</value></noun>
                """;
            String nounsAndAdjectives = nouns + ADJECTIVES_GRAMMAR;
            Object[][] arr = new Object[][]{
                {"ENTITY ABS sg indef", "Kontu", "<Entity entity=\"0\"/>", nouns},
                {"ENTITY DAT sg def", "Kontuari", "<Entity entity=\"0\" case=\"d\" article=\"d\"/>", nouns},
                {"ENTITY GEN sg def", "Kontuaren", "<Entity entity=\"0\" case=\"g\" article=\"d\"/>", nouns},
                {"ENTITY LOC sg def", "Kontuko", "<Entity entity=\"0\" case=\"l\" article=\"d\"/>", nouns},
                {"ENTITY + ADJ sg def", "Kontu berria", "<Entity entity=\"0\"/> <new article=\"d\"/>", nounsAndAdjectives},
                {"ENTITY + ADJ pl def", "Kontu berriak", "<Entity entity=\"0\"/> <new plural=\"y\"/>", nounsAndAdjectives},
                // Force dynamic render to return null (out-of-range index), then exercise fallback generateFromTermIfMissing
                {"ENTITY FALLBACK via generateFromTermIfMissing (DAT sg def)", "Kontuari", "<Entity entity=\"1\" case=\"d\" article=\"d\"/>", nouns},
            };
            return Arrays.stream(arr).map(Arguments::of);
        }

        @ParameterizedTest(name = "{index}: {0}")
        @MethodSource("data")
        void rendersAsExpected(String description, String expected, String label, String grammar) throws IOException {
            GrammaticalLabelSet set = helper.getTestLabelSet(eu, label, grammar);
            Renameable account = new BaseGrammaticalLabelTest.MockExistingRenameable("Account", set.getDictionary());

            assertEquals(expected, helper.getValue(eu, label, new Renameable[] { account }, grammar), description);
        }
    }
}


