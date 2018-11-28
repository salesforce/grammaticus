/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.commons.text;

import com.google.common.annotations.Beta;

/**
 * This class implements an efficient algorithm for case folding, per
 * Unicode spec UTR #21 Case Mappings, at http://www.unicode.org/unicode/reports/tr21/
 * <p>
 * Beta class. Classes under com.force.i18n.commons package will be moved into a dedicated project.
 *
 * @author davem
 */
@Beta
public class CaseFolder {

    /* This array was built by tools.CaseFolderBuilder.java */
    /* The outermost dimension of this array is indexed by the high byte
     * of the character (i.e., \u0041 is in bucket 00).  Within each
     * bucket is mapping of character from -> to
     */
    private static final char[][][][] FOLDINGMAP = new char[256][][][];
    private static final char[][][][] TURKICFOLDINGMAP = new char[256][][][];

    static {
        FOLDINGMAP[0] = new char[][][]{{{'\u0041'}, {'\u0061'}}, // LATIN CAPITAL LETTER A
                {{'\u0042'}, {'\u0062'}}, // LATIN CAPITAL LETTER B
                {{'\u0043'}, {'\u0063'}}, // LATIN CAPITAL LETTER C
                {{'\u0044'}, {'\u0064'}}, // LATIN CAPITAL LETTER D
                {{'\u0045'}, {'\u0065'}}, // LATIN CAPITAL LETTER E
                {{'\u0046'}, {'\u0066'}}, // LATIN CAPITAL LETTER F
                {{'\u0047'}, {'\u0067'}}, // LATIN CAPITAL LETTER G
                {{'\u0048'}, {'\u0068'}}, // LATIN CAPITAL LETTER H
                {{'\u0049'}, {'\u0069'}}, // LATIN CAPITAL LETTER I
                {{'\u004A'}, {'\u006A'}}, // LATIN CAPITAL LETTER J
                {{'\u004B'}, {'\u006B'}}, // LATIN CAPITAL LETTER K
                {{'\u004C'}, {'\u006C'}}, // LATIN CAPITAL LETTER L
                {{'\u004D'}, {'\u006D'}}, // LATIN CAPITAL LETTER M
                {{'\u004E'}, {'\u006E'}}, // LATIN CAPITAL LETTER N
                {{'\u004F'}, {'\u006F'}}, // LATIN CAPITAL LETTER O
                {{'\u0050'}, {'\u0070'}}, // LATIN CAPITAL LETTER P
                {{'\u0051'}, {'\u0071'}}, // LATIN CAPITAL LETTER Q
                {{'\u0052'}, {'\u0072'}}, // LATIN CAPITAL LETTER R
                {{'\u0053'}, {'\u0073'}}, // LATIN CAPITAL LETTER S
                {{'\u0054'}, {'\u0074'}}, // LATIN CAPITAL LETTER T
                {{'\u0055'}, {'\u0075'}}, // LATIN CAPITAL LETTER U
                {{'\u0056'}, {'\u0076'}}, // LATIN CAPITAL LETTER V
                {{'\u0057'}, {'\u0077'}}, // LATIN CAPITAL LETTER W
                {{'\u0058'}, {'\u0078'}}, // LATIN CAPITAL LETTER X
                {{'\u0059'}, {'\u0079'}}, // LATIN CAPITAL LETTER Y
                {{'\u005A'}, {'\u007A'}}, // LATIN CAPITAL LETTER Z
                {{'\u00B5'}, {'\u03BC'}}, // MICRO SIGN
                {{'\u00C0'}, {'\u00E0'}}, // LATIN CAPITAL LETTER A WITH GRAVE
                {{'\u00C1'}, {'\u00E1'}}, // LATIN CAPITAL LETTER A WITH ACUTE
                {{'\u00C2'}, {'\u00E2'}}, // LATIN CAPITAL LETTER A WITH CIRCUMFLEX
                {{'\u00C3'}, {'\u00E3'}}, // LATIN CAPITAL LETTER A WITH TILDE
                {{'\u00C4'}, {'\u00E4'}}, // LATIN CAPITAL LETTER A WITH DIAERESIS
                {{'\u00C5'}, {'\u00E5'}}, // LATIN CAPITAL LETTER A WITH RING ABOVE
                {{'\u00C6'}, {'\u00E6'}}, // LATIN CAPITAL LETTER AE
                {{'\u00C7'}, {'\u00E7'}}, // LATIN CAPITAL LETTER C WITH CEDILLA
                {{'\u00C8'}, {'\u00E8'}}, // LATIN CAPITAL LETTER E WITH GRAVE
                {{'\u00C9'}, {'\u00E9'}}, // LATIN CAPITAL LETTER E WITH ACUTE
                {{'\u00CA'}, {'\u00EA'}}, // LATIN CAPITAL LETTER E WITH CIRCUMFLEX
                {{'\u00CB'}, {'\u00EB'}}, // LATIN CAPITAL LETTER E WITH DIAERESIS
                {{'\u00CC'}, {'\u00EC'}}, // LATIN CAPITAL LETTER I WITH GRAVE
                {{'\u00CD'}, {'\u00ED'}}, // LATIN CAPITAL LETTER I WITH ACUTE
                {{'\u00CE'}, {'\u00EE'}}, // LATIN CAPITAL LETTER I WITH CIRCUMFLEX
                {{'\u00CF'}, {'\u00EF'}}, // LATIN CAPITAL LETTER I WITH DIAERESIS
                {{'\u00D0'}, {'\u00F0'}}, // LATIN CAPITAL LETTER ETH
                {{'\u00D1'}, {'\u00F1'}}, // LATIN CAPITAL LETTER N WITH TILDE
                {{'\u00D2'}, {'\u00F2'}}, // LATIN CAPITAL LETTER O WITH GRAVE
                {{'\u00D3'}, {'\u00F3'}}, // LATIN CAPITAL LETTER O WITH ACUTE
                {{'\u00D4'}, {'\u00F4'}}, // LATIN CAPITAL LETTER O WITH CIRCUMFLEX
                {{'\u00D5'}, {'\u00F5'}}, // LATIN CAPITAL LETTER O WITH TILDE
                {{'\u00D6'}, {'\u00F6'}}, // LATIN CAPITAL LETTER O WITH DIAERESIS
                {{'\u00D8'}, {'\u00F8'}}, // LATIN CAPITAL LETTER O WITH STROKE
                {{'\u00D9'}, {'\u00F9'}}, // LATIN CAPITAL LETTER U WITH GRAVE
                {{'\u00DA'}, {'\u00FA'}}, // LATIN CAPITAL LETTER U WITH ACUTE
                {{'\u00DB'}, {'\u00FB'}}, // LATIN CAPITAL LETTER U WITH CIRCUMFLEX
                {{'\u00DC'}, {'\u00FC'}}, // LATIN CAPITAL LETTER U WITH DIAERESIS
                {{'\u00DD'}, {'\u00FD'}}, // LATIN CAPITAL LETTER Y WITH ACUTE
                {{'\u00DE'}, {'\u00FE'}}, // LATIN CAPITAL LETTER THORN
                {{'\u00DF'}, {'\u0073', '\u0073',}}, // LATIN SMALL LETTER SHARP S
        };
        FOLDINGMAP[1] = new char[][][]{{{'\u0100'}, {'\u0101'}}, // LATIN CAPITAL LETTER A WITH MACRON
                {{'\u0102'}, {'\u0103'}}, // LATIN CAPITAL LETTER A WITH BREVE
                {{'\u0104'}, {'\u0105'}}, // LATIN CAPITAL LETTER A WITH OGONEK
                {{'\u0106'}, {'\u0107'}}, // LATIN CAPITAL LETTER C WITH ACUTE
                {{'\u0108'}, {'\u0109'}}, // LATIN CAPITAL LETTER C WITH CIRCUMFLEX
                {{'\u010A'}, {'\u010B'}}, // LATIN CAPITAL LETTER C WITH DOT ABOVE
                {{'\u010C'}, {'\u010D'}}, // LATIN CAPITAL LETTER C WITH CARON
                {{'\u010E'}, {'\u010F'}}, // LATIN CAPITAL LETTER D WITH CARON
                {{'\u0110'}, {'\u0111'}}, // LATIN CAPITAL LETTER D WITH STROKE
                {{'\u0112'}, {'\u0113'}}, // LATIN CAPITAL LETTER E WITH MACRON
                {{'\u0114'}, {'\u0115'}}, // LATIN CAPITAL LETTER E WITH BREVE
                {{'\u0116'}, {'\u0117'}}, // LATIN CAPITAL LETTER E WITH DOT ABOVE
                {{'\u0118'}, {'\u0119'}}, // LATIN CAPITAL LETTER E WITH OGONEK
                {{'\u011A'}, {'\u011B'}}, // LATIN CAPITAL LETTER E WITH CARON
                {{'\u011C'}, {'\u011D'}}, // LATIN CAPITAL LETTER G WITH CIRCUMFLEX
                {{'\u011E'}, {'\u011F'}}, // LATIN CAPITAL LETTER G WITH BREVE
                {{'\u0120'}, {'\u0121'}}, // LATIN CAPITAL LETTER G WITH DOT ABOVE
                {{'\u0122'}, {'\u0123'}}, // LATIN CAPITAL LETTER G WITH CEDILLA
                {{'\u0124'}, {'\u0125'}}, // LATIN CAPITAL LETTER H WITH CIRCUMFLEX
                {{'\u0126'}, {'\u0127'}}, // LATIN CAPITAL LETTER H WITH STROKE
                {{'\u0128'}, {'\u0129'}}, // LATIN CAPITAL LETTER I WITH TILDE
                {{'\u012A'}, {'\u012B'}}, // LATIN CAPITAL LETTER I WITH MACRON
                {{'\u012C'}, {'\u012D'}}, // LATIN CAPITAL LETTER I WITH BREVE
                {{'\u012E'}, {'\u012F'}}, // LATIN CAPITAL LETTER I WITH OGONEK
                {{'\u0130'}, {'\u0069', '\u0307',}}, // LATIN CAPITAL LETTER I WITH DOT ABOVE
                {{'\u0132'}, {'\u0133'}}, // LATIN CAPITAL LIGATURE IJ
                {{'\u0134'}, {'\u0135'}}, // LATIN CAPITAL LETTER J WITH CIRCUMFLEX
                {{'\u0136'}, {'\u0137'}}, // LATIN CAPITAL LETTER K WITH CEDILLA
                {{'\u0139'}, {'\u013A'}}, // LATIN CAPITAL LETTER L WITH ACUTE
                {{'\u013B'}, {'\u013C'}}, // LATIN CAPITAL LETTER L WITH CEDILLA
                {{'\u013D'}, {'\u013E'}}, // LATIN CAPITAL LETTER L WITH CARON
                {{'\u013F'}, {'\u0140'}}, // LATIN CAPITAL LETTER L WITH MIDDLE DOT
                {{'\u0141'}, {'\u0142'}}, // LATIN CAPITAL LETTER L WITH STROKE
                {{'\u0143'}, {'\u0144'}}, // LATIN CAPITAL LETTER N WITH ACUTE
                {{'\u0145'}, {'\u0146'}}, // LATIN CAPITAL LETTER N WITH CEDILLA
                {{'\u0147'}, {'\u0148'}}, // LATIN CAPITAL LETTER N WITH CARON
                {{'\u0149'}, {'\u02BC', '\u006E',}}, // LATIN SMALL LETTER N PRECEDED BY APOSTROPHE
                {{'\u014A'}, {'\u014B'}}, // LATIN CAPITAL LETTER ENG
                {{'\u014C'}, {'\u014D'}}, // LATIN CAPITAL LETTER O WITH MACRON
                {{'\u014E'}, {'\u014F'}}, // LATIN CAPITAL LETTER O WITH BREVE
                {{'\u0150'}, {'\u0151'}}, // LATIN CAPITAL LETTER O WITH DOUBLE ACUTE
                {{'\u0152'}, {'\u0153'}}, // LATIN CAPITAL LIGATURE OE
                {{'\u0154'}, {'\u0155'}}, // LATIN CAPITAL LETTER R WITH ACUTE
                {{'\u0156'}, {'\u0157'}}, // LATIN CAPITAL LETTER R WITH CEDILLA
                {{'\u0158'}, {'\u0159'}}, // LATIN CAPITAL LETTER R WITH CARON
                {{'\u015A'}, {'\u015B'}}, // LATIN CAPITAL LETTER S WITH ACUTE
                {{'\u015C'}, {'\u015D'}}, // LATIN CAPITAL LETTER S WITH CIRCUMFLEX
                {{'\u015E'}, {'\u015F'}}, // LATIN CAPITAL LETTER S WITH CEDILLA
                {{'\u0160'}, {'\u0161'}}, // LATIN CAPITAL LETTER S WITH CARON
                {{'\u0162'}, {'\u0163'}}, // LATIN CAPITAL LETTER T WITH CEDILLA
                {{'\u0164'}, {'\u0165'}}, // LATIN CAPITAL LETTER T WITH CARON
                {{'\u0166'}, {'\u0167'}}, // LATIN CAPITAL LETTER T WITH STROKE
                {{'\u0168'}, {'\u0169'}}, // LATIN CAPITAL LETTER U WITH TILDE
                {{'\u016A'}, {'\u016B'}}, // LATIN CAPITAL LETTER U WITH MACRON
                {{'\u016C'}, {'\u016D'}}, // LATIN CAPITAL LETTER U WITH BREVE
                {{'\u016E'}, {'\u016F'}}, // LATIN CAPITAL LETTER U WITH RING ABOVE
                {{'\u0170'}, {'\u0171'}}, // LATIN CAPITAL LETTER U WITH DOUBLE ACUTE
                {{'\u0172'}, {'\u0173'}}, // LATIN CAPITAL LETTER U WITH OGONEK
                {{'\u0174'}, {'\u0175'}}, // LATIN CAPITAL LETTER W WITH CIRCUMFLEX
                {{'\u0176'}, {'\u0177'}}, // LATIN CAPITAL LETTER Y WITH CIRCUMFLEX
                {{'\u0178'}, {'\u00FF'}}, // LATIN CAPITAL LETTER Y WITH DIAERESIS
                {{'\u0179'}, {'\u017A'}}, // LATIN CAPITAL LETTER Z WITH ACUTE
                {{'\u017B'}, {'\u017C'}}, // LATIN CAPITAL LETTER Z WITH DOT ABOVE
                {{'\u017D'}, {'\u017E'}}, // LATIN CAPITAL LETTER Z WITH CARON
                {{'\u017F'}, {'\u0073'}}, // LATIN SMALL LETTER LONG S
                {{'\u0181'}, {'\u0253'}}, // LATIN CAPITAL LETTER B WITH HOOK
                {{'\u0182'}, {'\u0183'}}, // LATIN CAPITAL LETTER B WITH TOPBAR
                {{'\u0184'}, {'\u0185'}}, // LATIN CAPITAL LETTER TONE SIX
                {{'\u0186'}, {'\u0254'}}, // LATIN CAPITAL LETTER OPEN O
                {{'\u0187'}, {'\u0188'}}, // LATIN CAPITAL LETTER C WITH HOOK
                {{'\u0189'}, {'\u0256'}}, // LATIN CAPITAL LETTER AFRICAN D
                {{'\u018A'}, {'\u0257'}}, // LATIN CAPITAL LETTER D WITH HOOK
                {{'\u018B'}, {'\u018C'}}, // LATIN CAPITAL LETTER D WITH TOPBAR
                {{'\u018E'}, {'\u01DD'}}, // LATIN CAPITAL LETTER REVERSED E
                {{'\u018F'}, {'\u0259'}}, // LATIN CAPITAL LETTER SCHWA
                {{'\u0190'}, {'\u025B'}}, // LATIN CAPITAL LETTER OPEN E
                {{'\u0191'}, {'\u0192'}}, // LATIN CAPITAL LETTER F WITH HOOK
                {{'\u0193'}, {'\u0260'}}, // LATIN CAPITAL LETTER G WITH HOOK
                {{'\u0194'}, {'\u0263'}}, // LATIN CAPITAL LETTER GAMMA
                {{'\u0196'}, {'\u0269'}}, // LATIN CAPITAL LETTER IOTA
                {{'\u0197'}, {'\u0268'}}, // LATIN CAPITAL LETTER I WITH STROKE
                {{'\u0198'}, {'\u0199'}}, // LATIN CAPITAL LETTER K WITH HOOK
                {{'\u019C'}, {'\u026F'}}, // LATIN CAPITAL LETTER TURNED M
                {{'\u019D'}, {'\u0272'}}, // LATIN CAPITAL LETTER N WITH LEFT HOOK
                {{'\u019F'}, {'\u0275'}}, // LATIN CAPITAL LETTER O WITH MIDDLE TILDE
                {{'\u01A0'}, {'\u01A1'}}, // LATIN CAPITAL LETTER O WITH HORN
                {{'\u01A2'}, {'\u01A3'}}, // LATIN CAPITAL LETTER OI
                {{'\u01A4'}, {'\u01A5'}}, // LATIN CAPITAL LETTER P WITH HOOK
                {{'\u01A6'}, {'\u0280'}}, // LATIN LETTER YR
                {{'\u01A7'}, {'\u01A8'}}, // LATIN CAPITAL LETTER TONE TWO
                {{'\u01A9'}, {'\u0283'}}, // LATIN CAPITAL LETTER ESH
                {{'\u01AC'}, {'\u01AD'}}, // LATIN CAPITAL LETTER T WITH HOOK
                {{'\u01AE'}, {'\u0288'}}, // LATIN CAPITAL LETTER T WITH RETROFLEX HOOK
                {{'\u01AF'}, {'\u01B0'}}, // LATIN CAPITAL LETTER U WITH HORN
                {{'\u01B1'}, {'\u028A'}}, // LATIN CAPITAL LETTER UPSILON
                {{'\u01B2'}, {'\u028B'}}, // LATIN CAPITAL LETTER V WITH HOOK
                {{'\u01B3'}, {'\u01B4'}}, // LATIN CAPITAL LETTER Y WITH HOOK
                {{'\u01B5'}, {'\u01B6'}}, // LATIN CAPITAL LETTER Z WITH STROKE
                {{'\u01B7'}, {'\u0292'}}, // LATIN CAPITAL LETTER EZH
                {{'\u01B8'}, {'\u01B9'}}, // LATIN CAPITAL LETTER EZH REVERSED
                {{'\u01BC'}, {'\u01BD'}}, // LATIN CAPITAL LETTER TONE FIVE
                {{'\u01C4'}, {'\u01C6'}}, // LATIN CAPITAL LETTER DZ WITH CARON
                {{'\u01C5'}, {'\u01C6'}}, // LATIN CAPITAL LETTER D WITH SMALL LETTER Z WITH CARON
                {{'\u01C7'}, {'\u01C9'}}, // LATIN CAPITAL LETTER LJ
                {{'\u01C8'}, {'\u01C9'}}, // LATIN CAPITAL LETTER L WITH SMALL LETTER J
                {{'\u01CA'}, {'\u01CC'}}, // LATIN CAPITAL LETTER NJ
                {{'\u01CB'}, {'\u01CC'}}, // LATIN CAPITAL LETTER N WITH SMALL LETTER J
                {{'\u01CD'}, {'\u01CE'}}, // LATIN CAPITAL LETTER A WITH CARON
                {{'\u01CF'}, {'\u01D0'}}, // LATIN CAPITAL LETTER I WITH CARON
                {{'\u01D1'}, {'\u01D2'}}, // LATIN CAPITAL LETTER O WITH CARON
                {{'\u01D3'}, {'\u01D4'}}, // LATIN CAPITAL LETTER U WITH CARON
                {{'\u01D5'}, {'\u01D6'}}, // LATIN CAPITAL LETTER U WITH DIAERESIS AND MACRON
                {{'\u01D7'}, {'\u01D8'}}, // LATIN CAPITAL LETTER U WITH DIAERESIS AND ACUTE
                {{'\u01D9'}, {'\u01DA'}}, // LATIN CAPITAL LETTER U WITH DIAERESIS AND CARON
                {{'\u01DB'}, {'\u01DC'}}, // LATIN CAPITAL LETTER U WITH DIAERESIS AND GRAVE
                {{'\u01DE'}, {'\u01DF'}}, // LATIN CAPITAL LETTER A WITH DIAERESIS AND MACRON
                {{'\u01E0'}, {'\u01E1'}}, // LATIN CAPITAL LETTER A WITH DOT ABOVE AND MACRON
                {{'\u01E2'}, {'\u01E3'}}, // LATIN CAPITAL LETTER AE WITH MACRON
                {{'\u01E4'}, {'\u01E5'}}, // LATIN CAPITAL LETTER G WITH STROKE
                {{'\u01E6'}, {'\u01E7'}}, // LATIN CAPITAL LETTER G WITH CARON
                {{'\u01E8'}, {'\u01E9'}}, // LATIN CAPITAL LETTER K WITH CARON
                {{'\u01EA'}, {'\u01EB'}}, // LATIN CAPITAL LETTER O WITH OGONEK
                {{'\u01EC'}, {'\u01ED'}}, // LATIN CAPITAL LETTER O WITH OGONEK AND MACRON
                {{'\u01EE'}, {'\u01EF'}}, // LATIN CAPITAL LETTER EZH WITH CARON
                {{'\u01F0'}, {'\u006A', '\u030C',}}, // LATIN SMALL LETTER J WITH CARON
                {{'\u01F1'}, {'\u01F3'}}, // LATIN CAPITAL LETTER DZ
                {{'\u01F2'}, {'\u01F3'}}, // LATIN CAPITAL LETTER D WITH SMALL LETTER Z
                {{'\u01F4'}, {'\u01F5'}}, // LATIN CAPITAL LETTER G WITH ACUTE
                {{'\u01F6'}, {'\u0195'}}, // LATIN CAPITAL LETTER HWAIR
                {{'\u01F7'}, {'\u01BF'}}, // LATIN CAPITAL LETTER WYNN
                {{'\u01F8'}, {'\u01F9'}}, // LATIN CAPITAL LETTER N WITH GRAVE
                {{'\u01FA'}, {'\u01FB'}}, // LATIN CAPITAL LETTER A WITH RING ABOVE AND ACUTE
                {{'\u01FC'}, {'\u01FD'}}, // LATIN CAPITAL LETTER AE WITH ACUTE
                {{'\u01FE'}, {'\u01FF'}}, // LATIN CAPITAL LETTER O WITH STROKE AND ACUTE
        };
        FOLDINGMAP[2] = new char[][][]{{{'\u0200'}, {'\u0201'}}, // LATIN CAPITAL LETTER A WITH DOUBLE GRAVE
                {{'\u0202'}, {'\u0203'}}, // LATIN CAPITAL LETTER A WITH INVERTED BREVE
                {{'\u0204'}, {'\u0205'}}, // LATIN CAPITAL LETTER E WITH DOUBLE GRAVE
                {{'\u0206'}, {'\u0207'}}, // LATIN CAPITAL LETTER E WITH INVERTED BREVE
                {{'\u0208'}, {'\u0209'}}, // LATIN CAPITAL LETTER I WITH DOUBLE GRAVE
                {{'\u020A'}, {'\u020B'}}, // LATIN CAPITAL LETTER I WITH INVERTED BREVE
                {{'\u020C'}, {'\u020D'}}, // LATIN CAPITAL LETTER O WITH DOUBLE GRAVE
                {{'\u020E'}, {'\u020F'}}, // LATIN CAPITAL LETTER O WITH INVERTED BREVE
                {{'\u0210'}, {'\u0211'}}, // LATIN CAPITAL LETTER R WITH DOUBLE GRAVE
                {{'\u0212'}, {'\u0213'}}, // LATIN CAPITAL LETTER R WITH INVERTED BREVE
                {{'\u0214'}, {'\u0215'}}, // LATIN CAPITAL LETTER U WITH DOUBLE GRAVE
                {{'\u0216'}, {'\u0217'}}, // LATIN CAPITAL LETTER U WITH INVERTED BREVE
                {{'\u0218'}, {'\u0219'}}, // LATIN CAPITAL LETTER S WITH COMMA BELOW
                {{'\u021A'}, {'\u021B'}}, // LATIN CAPITAL LETTER T WITH COMMA BELOW
                {{'\u021C'}, {'\u021D'}}, // LATIN CAPITAL LETTER YOGH
                {{'\u021E'}, {'\u021F'}}, // LATIN CAPITAL LETTER H WITH CARON
                {{'\u0220'}, {'\u019E'}}, // LATIN CAPITAL LETTER N WITH LONG RIGHT LEG
                {{'\u0222'}, {'\u0223'}}, // LATIN CAPITAL LETTER OU
                {{'\u0224'}, {'\u0225'}}, // LATIN CAPITAL LETTER Z WITH HOOK
                {{'\u0226'}, {'\u0227'}}, // LATIN CAPITAL LETTER A WITH DOT ABOVE
                {{'\u0228'}, {'\u0229'}}, // LATIN CAPITAL LETTER E WITH CEDILLA
                {{'\u022A'}, {'\u022B'}}, // LATIN CAPITAL LETTER O WITH DIAERESIS AND MACRON
                {{'\u022C'}, {'\u022D'}}, // LATIN CAPITAL LETTER O WITH TILDE AND MACRON
                {{'\u022E'}, {'\u022F'}}, // LATIN CAPITAL LETTER O WITH DOT ABOVE
                {{'\u0230'}, {'\u0231'}}, // LATIN CAPITAL LETTER O WITH DOT ABOVE AND MACRON
                {{'\u0232'}, {'\u0233'}}, // LATIN CAPITAL LETTER Y WITH MACRON
        };
        FOLDINGMAP[3] = new char[][][]{{{'\u0345'}, {'\u03B9'}}, // COMBINING GREEK YPOGEGRAMMENI
                {{'\u0386'}, {'\u03AC'}}, // GREEK CAPITAL LETTER ALPHA WITH TONOS
                {{'\u0388'}, {'\u03AD'}}, // GREEK CAPITAL LETTER EPSILON WITH TONOS
                {{'\u0389'}, {'\u03AE'}}, // GREEK CAPITAL LETTER ETA WITH TONOS
                {{'\u038A'}, {'\u03AF'}}, // GREEK CAPITAL LETTER IOTA WITH TONOS
                {{'\u038C'}, {'\u03CC'}}, // GREEK CAPITAL LETTER OMICRON WITH TONOS
                {{'\u038E'}, {'\u03CD'}}, // GREEK CAPITAL LETTER UPSILON WITH TONOS
                {{'\u038F'}, {'\u03CE'}}, // GREEK CAPITAL LETTER OMEGA WITH TONOS
                {{'\u0390'}, {'\u03B9', '\u0308', '\u0301',}}, // GREEK SMALL LETTER IOTA WITH DIALYTIKA AND TONOS
                {{'\u0391'}, {'\u03B1'}}, // GREEK CAPITAL LETTER ALPHA
                {{'\u0392'}, {'\u03B2'}}, // GREEK CAPITAL LETTER BETA
                {{'\u0393'}, {'\u03B3'}}, // GREEK CAPITAL LETTER GAMMA
                {{'\u0394'}, {'\u03B4'}}, // GREEK CAPITAL LETTER DELTA
                {{'\u0395'}, {'\u03B5'}}, // GREEK CAPITAL LETTER EPSILON
                {{'\u0396'}, {'\u03B6'}}, // GREEK CAPITAL LETTER ZETA
                {{'\u0397'}, {'\u03B7'}}, // GREEK CAPITAL LETTER ETA
                {{'\u0398'}, {'\u03B8'}}, // GREEK CAPITAL LETTER THETA
                {{'\u0399'}, {'\u03B9'}}, // GREEK CAPITAL LETTER IOTA
                {{'\u039A'}, {'\u03BA'}}, // GREEK CAPITAL LETTER KAPPA
                {{'\u039B'}, {'\u03BB'}}, // GREEK CAPITAL LETTER LAMDA
                {{'\u039C'}, {'\u03BC'}}, // GREEK CAPITAL LETTER MU
                {{'\u039D'}, {'\u03BD'}}, // GREEK CAPITAL LETTER NU
                {{'\u039E'}, {'\u03BE'}}, // GREEK CAPITAL LETTER XI
                {{'\u039F'}, {'\u03BF'}}, // GREEK CAPITAL LETTER OMICRON
                {{'\u03A0'}, {'\u03C0'}}, // GREEK CAPITAL LETTER PI
                {{'\u03A1'}, {'\u03C1'}}, // GREEK CAPITAL LETTER RHO
                {{'\u03A3'}, {'\u03C3'}}, // GREEK CAPITAL LETTER SIGMA
                {{'\u03A4'}, {'\u03C4'}}, // GREEK CAPITAL LETTER TAU
                {{'\u03A5'}, {'\u03C5'}}, // GREEK CAPITAL LETTER UPSILON
                {{'\u03A6'}, {'\u03C6'}}, // GREEK CAPITAL LETTER PHI
                {{'\u03A7'}, {'\u03C7'}}, // GREEK CAPITAL LETTER CHI
                {{'\u03A8'}, {'\u03C8'}}, // GREEK CAPITAL LETTER PSI
                {{'\u03A9'}, {'\u03C9'}}, // GREEK CAPITAL LETTER OMEGA
                {{'\u03AA'}, {'\u03CA'}}, // GREEK CAPITAL LETTER IOTA WITH DIALYTIKA
                {{'\u03AB'}, {'\u03CB'}}, // GREEK CAPITAL LETTER UPSILON WITH DIALYTIKA
                {{'\u03B0'}, {'\u03C5', '\u0308', '\u0301',}}, // GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND TONOS
                {{'\u03C2'}, {'\u03C3'}}, // GREEK SMALL LETTER FINAL SIGMA
                {{'\u03D0'}, {'\u03B2'}}, // GREEK BETA SYMBOL
                {{'\u03D1'}, {'\u03B8'}}, // GREEK THETA SYMBOL
                {{'\u03D5'}, {'\u03C6'}}, // GREEK PHI SYMBOL
                {{'\u03D6'}, {'\u03C0'}}, // GREEK PI SYMBOL
                {{'\u03D8'}, {'\u03D9'}}, // GREEK LETTER ARCHAIC KOPPA
                {{'\u03DA'}, {'\u03DB'}}, // GREEK LETTER STIGMA
                {{'\u03DC'}, {'\u03DD'}}, // GREEK LETTER DIGAMMA
                {{'\u03DE'}, {'\u03DF'}}, // GREEK LETTER KOPPA
                {{'\u03E0'}, {'\u03E1'}}, // GREEK LETTER SAMPI
                {{'\u03E2'}, {'\u03E3'}}, // COPTIC CAPITAL LETTER SHEI
                {{'\u03E4'}, {'\u03E5'}}, // COPTIC CAPITAL LETTER FEI
                {{'\u03E6'}, {'\u03E7'}}, // COPTIC CAPITAL LETTER KHEI
                {{'\u03E8'}, {'\u03E9'}}, // COPTIC CAPITAL LETTER HORI
                {{'\u03EA'}, {'\u03EB'}}, // COPTIC CAPITAL LETTER GANGIA
                {{'\u03EC'}, {'\u03ED'}}, // COPTIC CAPITAL LETTER SHIMA
                {{'\u03EE'}, {'\u03EF'}}, // COPTIC CAPITAL LETTER DEI
                {{'\u03F0'}, {'\u03BA'}}, // GREEK KAPPA SYMBOL
                {{'\u03F1'}, {'\u03C1'}}, // GREEK RHO SYMBOL
                {{'\u03F2'}, {'\u03C3'}}, // GREEK LUNATE SIGMA SYMBOL
                {{'\u03F4'}, {'\u03B8'}}, // GREEK CAPITAL THETA SYMBOL
                {{'\u03F5'}, {'\u03B5'}}, // GREEK LUNATE EPSILON SYMBOL
        };
        FOLDINGMAP[4] = new char[][][]{{{'\u0400'}, {'\u0450'}}, // CYRILLIC CAPITAL LETTER IE WITH GRAVE
                {{'\u0401'}, {'\u0451'}}, // CYRILLIC CAPITAL LETTER IO
                {{'\u0402'}, {'\u0452'}}, // CYRILLIC CAPITAL LETTER DJE
                {{'\u0403'}, {'\u0453'}}, // CYRILLIC CAPITAL LETTER GJE
                {{'\u0404'}, {'\u0454'}}, // CYRILLIC CAPITAL LETTER UKRAINIAN IE
                {{'\u0405'}, {'\u0455'}}, // CYRILLIC CAPITAL LETTER DZE
                {{'\u0406'}, {'\u0456'}}, // CYRILLIC CAPITAL LETTER BYELORUSSIAN-UKRAINIAN I
                {{'\u0407'}, {'\u0457'}}, // CYRILLIC CAPITAL LETTER YI
                {{'\u0408'}, {'\u0458'}}, // CYRILLIC CAPITAL LETTER JE
                {{'\u0409'}, {'\u0459'}}, // CYRILLIC CAPITAL LETTER LJE
                {{'\u040A'}, {'\u045A'}}, // CYRILLIC CAPITAL LETTER NJE
                {{'\u040B'}, {'\u045B'}}, // CYRILLIC CAPITAL LETTER TSHE
                {{'\u040C'}, {'\u045C'}}, // CYRILLIC CAPITAL LETTER KJE
                {{'\u040D'}, {'\u045D'}}, // CYRILLIC CAPITAL LETTER I WITH GRAVE
                {{'\u040E'}, {'\u045E'}}, // CYRILLIC CAPITAL LETTER SHORT U
                {{'\u040F'}, {'\u045F'}}, // CYRILLIC CAPITAL LETTER DZHE
                {{'\u0410'}, {'\u0430'}}, // CYRILLIC CAPITAL LETTER A
                {{'\u0411'}, {'\u0431'}}, // CYRILLIC CAPITAL LETTER BE
                {{'\u0412'}, {'\u0432'}}, // CYRILLIC CAPITAL LETTER VE
                {{'\u0413'}, {'\u0433'}}, // CYRILLIC CAPITAL LETTER GHE
                {{'\u0414'}, {'\u0434'}}, // CYRILLIC CAPITAL LETTER DE
                {{'\u0415'}, {'\u0435'}}, // CYRILLIC CAPITAL LETTER IE
                {{'\u0416'}, {'\u0436'}}, // CYRILLIC CAPITAL LETTER ZHE
                {{'\u0417'}, {'\u0437'}}, // CYRILLIC CAPITAL LETTER ZE
                {{'\u0418'}, {'\u0438'}}, // CYRILLIC CAPITAL LETTER I
                {{'\u0419'}, {'\u0439'}}, // CYRILLIC CAPITAL LETTER SHORT I
                {{'\u041A'}, {'\u043A'}}, // CYRILLIC CAPITAL LETTER KA
                {{'\u041B'}, {'\u043B'}}, // CYRILLIC CAPITAL LETTER EL
                {{'\u041C'}, {'\u043C'}}, // CYRILLIC CAPITAL LETTER EM
                {{'\u041D'}, {'\u043D'}}, // CYRILLIC CAPITAL LETTER EN
                {{'\u041E'}, {'\u043E'}}, // CYRILLIC CAPITAL LETTER O
                {{'\u041F'}, {'\u043F'}}, // CYRILLIC CAPITAL LETTER PE
                {{'\u0420'}, {'\u0440'}}, // CYRILLIC CAPITAL LETTER ER
                {{'\u0421'}, {'\u0441'}}, // CYRILLIC CAPITAL LETTER ES
                {{'\u0422'}, {'\u0442'}}, // CYRILLIC CAPITAL LETTER TE
                {{'\u0423'}, {'\u0443'}}, // CYRILLIC CAPITAL LETTER U
                {{'\u0424'}, {'\u0444'}}, // CYRILLIC CAPITAL LETTER EF
                {{'\u0425'}, {'\u0445'}}, // CYRILLIC CAPITAL LETTER HA
                {{'\u0426'}, {'\u0446'}}, // CYRILLIC CAPITAL LETTER TSE
                {{'\u0427'}, {'\u0447'}}, // CYRILLIC CAPITAL LETTER CHE
                {{'\u0428'}, {'\u0448'}}, // CYRILLIC CAPITAL LETTER SHA
                {{'\u0429'}, {'\u0449'}}, // CYRILLIC CAPITAL LETTER SHCHA
                {{'\u042A'}, {'\u044A'}}, // CYRILLIC CAPITAL LETTER HARD SIGN
                {{'\u042B'}, {'\u044B'}}, // CYRILLIC CAPITAL LETTER YERU
                {{'\u042C'}, {'\u044C'}}, // CYRILLIC CAPITAL LETTER SOFT SIGN
                {{'\u042D'}, {'\u044D'}}, // CYRILLIC CAPITAL LETTER E
                {{'\u042E'}, {'\u044E'}}, // CYRILLIC CAPITAL LETTER YU
                {{'\u042F'}, {'\u044F'}}, // CYRILLIC CAPITAL LETTER YA
                {{'\u0460'}, {'\u0461'}}, // CYRILLIC CAPITAL LETTER OMEGA
                {{'\u0462'}, {'\u0463'}}, // CYRILLIC CAPITAL LETTER YAT
                {{'\u0464'}, {'\u0465'}}, // CYRILLIC CAPITAL LETTER IOTIFIED E
                {{'\u0466'}, {'\u0467'}}, // CYRILLIC CAPITAL LETTER LITTLE YUS
                {{'\u0468'}, {'\u0469'}}, // CYRILLIC CAPITAL LETTER IOTIFIED LITTLE YUS
                {{'\u046A'}, {'\u046B'}}, // CYRILLIC CAPITAL LETTER BIG YUS
                {{'\u046C'}, {'\u046D'}}, // CYRILLIC CAPITAL LETTER IOTIFIED BIG YUS
                {{'\u046E'}, {'\u046F'}}, // CYRILLIC CAPITAL LETTER KSI
                {{'\u0470'}, {'\u0471'}}, // CYRILLIC CAPITAL LETTER PSI
                {{'\u0472'}, {'\u0473'}}, // CYRILLIC CAPITAL LETTER FITA
                {{'\u0474'}, {'\u0475'}}, // CYRILLIC CAPITAL LETTER IZHITSA
                {{'\u0476'}, {'\u0477'}}, // CYRILLIC CAPITAL LETTER IZHITSA WITH DOUBLE GRAVE ACCENT
                {{'\u0478'}, {'\u0479'}}, // CYRILLIC CAPITAL LETTER UK
                {{'\u047A'}, {'\u047B'}}, // CYRILLIC CAPITAL LETTER ROUND OMEGA
                {{'\u047C'}, {'\u047D'}}, // CYRILLIC CAPITAL LETTER OMEGA WITH TITLO
                {{'\u047E'}, {'\u047F'}}, // CYRILLIC CAPITAL LETTER OT
                {{'\u0480'}, {'\u0481'}}, // CYRILLIC CAPITAL LETTER KOPPA
                {{'\u048A'}, {'\u048B'}}, // CYRILLIC CAPITAL LETTER SHORT I WITH TAIL
                {{'\u048C'}, {'\u048D'}}, // CYRILLIC CAPITAL LETTER SEMISOFT SIGN
                {{'\u048E'}, {'\u048F'}}, // CYRILLIC CAPITAL LETTER ER WITH TICK
                {{'\u0490'}, {'\u0491'}}, // CYRILLIC CAPITAL LETTER GHE WITH UPTURN
                {{'\u0492'}, {'\u0493'}}, // CYRILLIC CAPITAL LETTER GHE WITH STROKE
                {{'\u0494'}, {'\u0495'}}, // CYRILLIC CAPITAL LETTER GHE WITH MIDDLE HOOK
                {{'\u0496'}, {'\u0497'}}, // CYRILLIC CAPITAL LETTER ZHE WITH DESCENDER
                {{'\u0498'}, {'\u0499'}}, // CYRILLIC CAPITAL LETTER ZE WITH DESCENDER
                {{'\u049A'}, {'\u049B'}}, // CYRILLIC CAPITAL LETTER KA WITH DESCENDER
                {{'\u049C'}, {'\u049D'}}, // CYRILLIC CAPITAL LETTER KA WITH VERTICAL STROKE
                {{'\u049E'}, {'\u049F'}}, // CYRILLIC CAPITAL LETTER KA WITH STROKE
                {{'\u04A0'}, {'\u04A1'}}, // CYRILLIC CAPITAL LETTER BASHKIR KA
                {{'\u04A2'}, {'\u04A3'}}, // CYRILLIC CAPITAL LETTER EN WITH DESCENDER
                {{'\u04A4'}, {'\u04A5'}}, // CYRILLIC CAPITAL LIGATURE EN GHE
                {{'\u04A6'}, {'\u04A7'}}, // CYRILLIC CAPITAL LETTER PE WITH MIDDLE HOOK
                {{'\u04A8'}, {'\u04A9'}}, // CYRILLIC CAPITAL LETTER ABKHASIAN HA
                {{'\u04AA'}, {'\u04AB'}}, // CYRILLIC CAPITAL LETTER ES WITH DESCENDER
                {{'\u04AC'}, {'\u04AD'}}, // CYRILLIC CAPITAL LETTER TE WITH DESCENDER
                {{'\u04AE'}, {'\u04AF'}}, // CYRILLIC CAPITAL LETTER STRAIGHT U
                {{'\u04B0'}, {'\u04B1'}}, // CYRILLIC CAPITAL LETTER STRAIGHT U WITH STROKE
                {{'\u04B2'}, {'\u04B3'}}, // CYRILLIC CAPITAL LETTER HA WITH DESCENDER
                {{'\u04B4'}, {'\u04B5'}}, // CYRILLIC CAPITAL LIGATURE TE TSE
                {{'\u04B6'}, {'\u04B7'}}, // CYRILLIC CAPITAL LETTER CHE WITH DESCENDER
                {{'\u04B8'}, {'\u04B9'}}, // CYRILLIC CAPITAL LETTER CHE WITH VERTICAL STROKE
                {{'\u04BA'}, {'\u04BB'}}, // CYRILLIC CAPITAL LETTER SHHA
                {{'\u04BC'}, {'\u04BD'}}, // CYRILLIC CAPITAL LETTER ABKHASIAN CHE
                {{'\u04BE'}, {'\u04BF'}}, // CYRILLIC CAPITAL LETTER ABKHASIAN CHE WITH DESCENDER
                {{'\u04C1'}, {'\u04C2'}}, // CYRILLIC CAPITAL LETTER ZHE WITH BREVE
                {{'\u04C3'}, {'\u04C4'}}, // CYRILLIC CAPITAL LETTER KA WITH HOOK
                {{'\u04C5'}, {'\u04C6'}}, // CYRILLIC CAPITAL LETTER EL WITH TAIL
                {{'\u04C7'}, {'\u04C8'}}, // CYRILLIC CAPITAL LETTER EN WITH HOOK
                {{'\u04C9'}, {'\u04CA'}}, // CYRILLIC CAPITAL LETTER EN WITH TAIL
                {{'\u04CB'}, {'\u04CC'}}, // CYRILLIC CAPITAL LETTER KHAKASSIAN CHE
                {{'\u04CD'}, {'\u04CE'}}, // CYRILLIC CAPITAL LETTER EM WITH TAIL
                {{'\u04D0'}, {'\u04D1'}}, // CYRILLIC CAPITAL LETTER A WITH BREVE
                {{'\u04D2'}, {'\u04D3'}}, // CYRILLIC CAPITAL LETTER A WITH DIAERESIS
                {{'\u04D4'}, {'\u04D5'}}, // CYRILLIC CAPITAL LIGATURE A IE
                {{'\u04D6'}, {'\u04D7'}}, // CYRILLIC CAPITAL LETTER IE WITH BREVE
                {{'\u04D8'}, {'\u04D9'}}, // CYRILLIC CAPITAL LETTER SCHWA
                {{'\u04DA'}, {'\u04DB'}}, // CYRILLIC CAPITAL LETTER SCHWA WITH DIAERESIS
                {{'\u04DC'}, {'\u04DD'}}, // CYRILLIC CAPITAL LETTER ZHE WITH DIAERESIS
                {{'\u04DE'}, {'\u04DF'}}, // CYRILLIC CAPITAL LETTER ZE WITH DIAERESIS
                {{'\u04E0'}, {'\u04E1'}}, // CYRILLIC CAPITAL LETTER ABKHASIAN DZE
                {{'\u04E2'}, {'\u04E3'}}, // CYRILLIC CAPITAL LETTER I WITH MACRON
                {{'\u04E4'}, {'\u04E5'}}, // CYRILLIC CAPITAL LETTER I WITH DIAERESIS
                {{'\u04E6'}, {'\u04E7'}}, // CYRILLIC CAPITAL LETTER O WITH DIAERESIS
                {{'\u04E8'}, {'\u04E9'}}, // CYRILLIC CAPITAL LETTER BARRED O
                {{'\u04EA'}, {'\u04EB'}}, // CYRILLIC CAPITAL LETTER BARRED O WITH DIAERESIS
                {{'\u04EC'}, {'\u04ED'}}, // CYRILLIC CAPITAL LETTER E WITH DIAERESIS
                {{'\u04EE'}, {'\u04EF'}}, // CYRILLIC CAPITAL LETTER U WITH MACRON
                {{'\u04F0'}, {'\u04F1'}}, // CYRILLIC CAPITAL LETTER U WITH DIAERESIS
                {{'\u04F2'}, {'\u04F3'}}, // CYRILLIC CAPITAL LETTER U WITH DOUBLE ACUTE
                {{'\u04F4'}, {'\u04F5'}}, // CYRILLIC CAPITAL LETTER CHE WITH DIAERESIS
                {{'\u04F8'}, {'\u04F9'}}, // CYRILLIC CAPITAL LETTER YERU WITH DIAERESIS
        };
        FOLDINGMAP[5] = new char[][][]{{{'\u0500'}, {'\u0501'}}, // CYRILLIC CAPITAL LETTER KOMI DE
                {{'\u0502'}, {'\u0503'}}, // CYRILLIC CAPITAL LETTER KOMI DJE
                {{'\u0504'}, {'\u0505'}}, // CYRILLIC CAPITAL LETTER KOMI ZJE
                {{'\u0506'}, {'\u0507'}}, // CYRILLIC CAPITAL LETTER KOMI DZJE
                {{'\u0508'}, {'\u0509'}}, // CYRILLIC CAPITAL LETTER KOMI LJE
                {{'\u050A'}, {'\u050B'}}, // CYRILLIC CAPITAL LETTER KOMI NJE
                {{'\u050C'}, {'\u050D'}}, // CYRILLIC CAPITAL LETTER KOMI SJE
                {{'\u050E'}, {'\u050F'}}, // CYRILLIC CAPITAL LETTER KOMI TJE
                {{'\u0531'}, {'\u0561'}}, // ARMENIAN CAPITAL LETTER AYB
                {{'\u0532'}, {'\u0562'}}, // ARMENIAN CAPITAL LETTER BEN
                {{'\u0533'}, {'\u0563'}}, // ARMENIAN CAPITAL LETTER GIM
                {{'\u0534'}, {'\u0564'}}, // ARMENIAN CAPITAL LETTER DA
                {{'\u0535'}, {'\u0565'}}, // ARMENIAN CAPITAL LETTER ECH
                {{'\u0536'}, {'\u0566'}}, // ARMENIAN CAPITAL LETTER ZA
                {{'\u0537'}, {'\u0567'}}, // ARMENIAN CAPITAL LETTER EH
                {{'\u0538'}, {'\u0568'}}, // ARMENIAN CAPITAL LETTER ET
                {{'\u0539'}, {'\u0569'}}, // ARMENIAN CAPITAL LETTER TO
                {{'\u053A'}, {'\u056A'}}, // ARMENIAN CAPITAL LETTER ZHE
                {{'\u053B'}, {'\u056B'}}, // ARMENIAN CAPITAL LETTER INI
                {{'\u053C'}, {'\u056C'}}, // ARMENIAN CAPITAL LETTER LIWN
                {{'\u053D'}, {'\u056D'}}, // ARMENIAN CAPITAL LETTER XEH
                {{'\u053E'}, {'\u056E'}}, // ARMENIAN CAPITAL LETTER CA
                {{'\u053F'}, {'\u056F'}}, // ARMENIAN CAPITAL LETTER KEN
                {{'\u0540'}, {'\u0570'}}, // ARMENIAN CAPITAL LETTER HO
                {{'\u0541'}, {'\u0571'}}, // ARMENIAN CAPITAL LETTER JA
                {{'\u0542'}, {'\u0572'}}, // ARMENIAN CAPITAL LETTER GHAD
                {{'\u0543'}, {'\u0573'}}, // ARMENIAN CAPITAL LETTER CHEH
                {{'\u0544'}, {'\u0574'}}, // ARMENIAN CAPITAL LETTER MEN
                {{'\u0545'}, {'\u0575'}}, // ARMENIAN CAPITAL LETTER YI
                {{'\u0546'}, {'\u0576'}}, // ARMENIAN CAPITAL LETTER NOW
                {{'\u0547'}, {'\u0577'}}, // ARMENIAN CAPITAL LETTER SHA
                {{'\u0548'}, {'\u0578'}}, // ARMENIAN CAPITAL LETTER VO
                {{'\u0549'}, {'\u0579'}}, // ARMENIAN CAPITAL LETTER CHA
                {{'\u054A'}, {'\u057A'}}, // ARMENIAN CAPITAL LETTER PEH
                {{'\u054B'}, {'\u057B'}}, // ARMENIAN CAPITAL LETTER JHEH
                {{'\u054C'}, {'\u057C'}}, // ARMENIAN CAPITAL LETTER RA
                {{'\u054D'}, {'\u057D'}}, // ARMENIAN CAPITAL LETTER SEH
                {{'\u054E'}, {'\u057E'}}, // ARMENIAN CAPITAL LETTER VEW
                {{'\u054F'}, {'\u057F'}}, // ARMENIAN CAPITAL LETTER TIWN
                {{'\u0550'}, {'\u0580'}}, // ARMENIAN CAPITAL LETTER REH
                {{'\u0551'}, {'\u0581'}}, // ARMENIAN CAPITAL LETTER CO
                {{'\u0552'}, {'\u0582'}}, // ARMENIAN CAPITAL LETTER YIWN
                {{'\u0553'}, {'\u0583'}}, // ARMENIAN CAPITAL LETTER PIWR
                {{'\u0554'}, {'\u0584'}}, // ARMENIAN CAPITAL LETTER KEH
                {{'\u0555'}, {'\u0585'}}, // ARMENIAN CAPITAL LETTER OH
                {{'\u0556'}, {'\u0586'}}, // ARMENIAN CAPITAL LETTER FEH
                {{'\u0587'}, {'\u0565', '\u0582',}}, // ARMENIAN SMALL LIGATURE ECH YIWN
        };
        FOLDINGMAP[30] = new char[][][]{{{'\u1E00'}, {'\u1E01'}}, // LATIN CAPITAL LETTER A WITH RING BELOW
                {{'\u1E02'}, {'\u1E03'}}, // LATIN CAPITAL LETTER B WITH DOT ABOVE
                {{'\u1E04'}, {'\u1E05'}}, // LATIN CAPITAL LETTER B WITH DOT BELOW
                {{'\u1E06'}, {'\u1E07'}}, // LATIN CAPITAL LETTER B WITH LINE BELOW
                {{'\u1E08'}, {'\u1E09'}}, // LATIN CAPITAL LETTER C WITH CEDILLA AND ACUTE
                {{'\u1E0A'}, {'\u1E0B'}}, // LATIN CAPITAL LETTER D WITH DOT ABOVE
                {{'\u1E0C'}, {'\u1E0D'}}, // LATIN CAPITAL LETTER D WITH DOT BELOW
                {{'\u1E0E'}, {'\u1E0F'}}, // LATIN CAPITAL LETTER D WITH LINE BELOW
                {{'\u1E10'}, {'\u1E11'}}, // LATIN CAPITAL LETTER D WITH CEDILLA
                {{'\u1E12'}, {'\u1E13'}}, // LATIN CAPITAL LETTER D WITH CIRCUMFLEX BELOW
                {{'\u1E14'}, {'\u1E15'}}, // LATIN CAPITAL LETTER E WITH MACRON AND GRAVE
                {{'\u1E16'}, {'\u1E17'}}, // LATIN CAPITAL LETTER E WITH MACRON AND ACUTE
                {{'\u1E18'}, {'\u1E19'}}, // LATIN CAPITAL LETTER E WITH CIRCUMFLEX BELOW
                {{'\u1E1A'}, {'\u1E1B'}}, // LATIN CAPITAL LETTER E WITH TILDE BELOW
                {{'\u1E1C'}, {'\u1E1D'}}, // LATIN CAPITAL LETTER E WITH CEDILLA AND BREVE
                {{'\u1E1E'}, {'\u1E1F'}}, // LATIN CAPITAL LETTER F WITH DOT ABOVE
                {{'\u1E20'}, {'\u1E21'}}, // LATIN CAPITAL LETTER G WITH MACRON
                {{'\u1E22'}, {'\u1E23'}}, // LATIN CAPITAL LETTER H WITH DOT ABOVE
                {{'\u1E24'}, {'\u1E25'}}, // LATIN CAPITAL LETTER H WITH DOT BELOW
                {{'\u1E26'}, {'\u1E27'}}, // LATIN CAPITAL LETTER H WITH DIAERESIS
                {{'\u1E28'}, {'\u1E29'}}, // LATIN CAPITAL LETTER H WITH CEDILLA
                {{'\u1E2A'}, {'\u1E2B'}}, // LATIN CAPITAL LETTER H WITH BREVE BELOW
                {{'\u1E2C'}, {'\u1E2D'}}, // LATIN CAPITAL LETTER I WITH TILDE BELOW
                {{'\u1E2E'}, {'\u1E2F'}}, // LATIN CAPITAL LETTER I WITH DIAERESIS AND ACUTE
                {{'\u1E30'}, {'\u1E31'}}, // LATIN CAPITAL LETTER K WITH ACUTE
                {{'\u1E32'}, {'\u1E33'}}, // LATIN CAPITAL LETTER K WITH DOT BELOW
                {{'\u1E34'}, {'\u1E35'}}, // LATIN CAPITAL LETTER K WITH LINE BELOW
                {{'\u1E36'}, {'\u1E37'}}, // LATIN CAPITAL LETTER L WITH DOT BELOW
                {{'\u1E38'}, {'\u1E39'}}, // LATIN CAPITAL LETTER L WITH DOT BELOW AND MACRON
                {{'\u1E3A'}, {'\u1E3B'}}, // LATIN CAPITAL LETTER L WITH LINE BELOW
                {{'\u1E3C'}, {'\u1E3D'}}, // LATIN CAPITAL LETTER L WITH CIRCUMFLEX BELOW
                {{'\u1E3E'}, {'\u1E3F'}}, // LATIN CAPITAL LETTER M WITH ACUTE
                {{'\u1E40'}, {'\u1E41'}}, // LATIN CAPITAL LETTER M WITH DOT ABOVE
                {{'\u1E42'}, {'\u1E43'}}, // LATIN CAPITAL LETTER M WITH DOT BELOW
                {{'\u1E44'}, {'\u1E45'}}, // LATIN CAPITAL LETTER N WITH DOT ABOVE
                {{'\u1E46'}, {'\u1E47'}}, // LATIN CAPITAL LETTER N WITH DOT BELOW
                {{'\u1E48'}, {'\u1E49'}}, // LATIN CAPITAL LETTER N WITH LINE BELOW
                {{'\u1E4A'}, {'\u1E4B'}}, // LATIN CAPITAL LETTER N WITH CIRCUMFLEX BELOW
                {{'\u1E4C'}, {'\u1E4D'}}, // LATIN CAPITAL LETTER O WITH TILDE AND ACUTE
                {{'\u1E4E'}, {'\u1E4F'}}, // LATIN CAPITAL LETTER O WITH TILDE AND DIAERESIS
                {{'\u1E50'}, {'\u1E51'}}, // LATIN CAPITAL LETTER O WITH MACRON AND GRAVE
                {{'\u1E52'}, {'\u1E53'}}, // LATIN CAPITAL LETTER O WITH MACRON AND ACUTE
                {{'\u1E54'}, {'\u1E55'}}, // LATIN CAPITAL LETTER P WITH ACUTE
                {{'\u1E56'}, {'\u1E57'}}, // LATIN CAPITAL LETTER P WITH DOT ABOVE
                {{'\u1E58'}, {'\u1E59'}}, // LATIN CAPITAL LETTER R WITH DOT ABOVE
                {{'\u1E5A'}, {'\u1E5B'}}, // LATIN CAPITAL LETTER R WITH DOT BELOW
                {{'\u1E5C'}, {'\u1E5D'}}, // LATIN CAPITAL LETTER R WITH DOT BELOW AND MACRON
                {{'\u1E5E'}, {'\u1E5F'}}, // LATIN CAPITAL LETTER R WITH LINE BELOW
                {{'\u1E60'}, {'\u1E61'}}, // LATIN CAPITAL LETTER S WITH DOT ABOVE
                {{'\u1E62'}, {'\u1E63'}}, // LATIN CAPITAL LETTER S WITH DOT BELOW
                {{'\u1E64'}, {'\u1E65'}}, // LATIN CAPITAL LETTER S WITH ACUTE AND DOT ABOVE
                {{'\u1E66'}, {'\u1E67'}}, // LATIN CAPITAL LETTER S WITH CARON AND DOT ABOVE
                {{'\u1E68'}, {'\u1E69'}}, // LATIN CAPITAL LETTER S WITH DOT BELOW AND DOT ABOVE
                {{'\u1E6A'}, {'\u1E6B'}}, // LATIN CAPITAL LETTER T WITH DOT ABOVE
                {{'\u1E6C'}, {'\u1E6D'}}, // LATIN CAPITAL LETTER T WITH DOT BELOW
                {{'\u1E6E'}, {'\u1E6F'}}, // LATIN CAPITAL LETTER T WITH LINE BELOW
                {{'\u1E70'}, {'\u1E71'}}, // LATIN CAPITAL LETTER T WITH CIRCUMFLEX BELOW
                {{'\u1E72'}, {'\u1E73'}}, // LATIN CAPITAL LETTER U WITH DIAERESIS BELOW
                {{'\u1E74'}, {'\u1E75'}}, // LATIN CAPITAL LETTER U WITH TILDE BELOW
                {{'\u1E76'}, {'\u1E77'}}, // LATIN CAPITAL LETTER U WITH CIRCUMFLEX BELOW
                {{'\u1E78'}, {'\u1E79'}}, // LATIN CAPITAL LETTER U WITH TILDE AND ACUTE
                {{'\u1E7A'}, {'\u1E7B'}}, // LATIN CAPITAL LETTER U WITH MACRON AND DIAERESIS
                {{'\u1E7C'}, {'\u1E7D'}}, // LATIN CAPITAL LETTER V WITH TILDE
                {{'\u1E7E'}, {'\u1E7F'}}, // LATIN CAPITAL LETTER V WITH DOT BELOW
                {{'\u1E80'}, {'\u1E81'}}, // LATIN CAPITAL LETTER W WITH GRAVE
                {{'\u1E82'}, {'\u1E83'}}, // LATIN CAPITAL LETTER W WITH ACUTE
                {{'\u1E84'}, {'\u1E85'}}, // LATIN CAPITAL LETTER W WITH DIAERESIS
                {{'\u1E86'}, {'\u1E87'}}, // LATIN CAPITAL LETTER W WITH DOT ABOVE
                {{'\u1E88'}, {'\u1E89'}}, // LATIN CAPITAL LETTER W WITH DOT BELOW
                {{'\u1E8A'}, {'\u1E8B'}}, // LATIN CAPITAL LETTER X WITH DOT ABOVE
                {{'\u1E8C'}, {'\u1E8D'}}, // LATIN CAPITAL LETTER X WITH DIAERESIS
                {{'\u1E8E'}, {'\u1E8F'}}, // LATIN CAPITAL LETTER Y WITH DOT ABOVE
                {{'\u1E90'}, {'\u1E91'}}, // LATIN CAPITAL LETTER Z WITH CIRCUMFLEX
                {{'\u1E92'}, {'\u1E93'}}, // LATIN CAPITAL LETTER Z WITH DOT BELOW
                {{'\u1E94'}, {'\u1E95'}}, // LATIN CAPITAL LETTER Z WITH LINE BELOW
                {{'\u1E96'}, {'\u0068', '\u0331',}}, // LATIN SMALL LETTER H WITH LINE BELOW
                {{'\u1E97'}, {'\u0074', '\u0308',}}, // LATIN SMALL LETTER T WITH DIAERESIS
                {{'\u1E98'}, {'\u0077', '\u030A',}}, // LATIN SMALL LETTER W WITH RING ABOVE
                {{'\u1E99'}, {'\u0079', '\u030A',}}, // LATIN SMALL LETTER Y WITH RING ABOVE
                {{'\u1E9A'}, {'\u0061', '\u02BE',}}, // LATIN SMALL LETTER A WITH RIGHT HALF RING
                {{'\u1E9B'}, {'\u1E61'}}, // LATIN SMALL LETTER LONG S WITH DOT ABOVE
                {{'\u1EA0'}, {'\u1EA1'}}, // LATIN CAPITAL LETTER A WITH DOT BELOW
                {{'\u1EA2'}, {'\u1EA3'}}, // LATIN CAPITAL LETTER A WITH HOOK ABOVE
                {{'\u1EA4'}, {'\u1EA5'}}, // LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND ACUTE
                {{'\u1EA6'}, {'\u1EA7'}}, // LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND GRAVE
                {{'\u1EA8'}, {'\u1EA9'}}, // LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND HOOK ABOVE
                {{'\u1EAA'}, {'\u1EAB'}}, // LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND TILDE
                {{'\u1EAC'}, {'\u1EAD'}}, // LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND DOT BELOW
                {{'\u1EAE'}, {'\u1EAF'}}, // LATIN CAPITAL LETTER A WITH BREVE AND ACUTE
                {{'\u1EB0'}, {'\u1EB1'}}, // LATIN CAPITAL LETTER A WITH BREVE AND GRAVE
                {{'\u1EB2'}, {'\u1EB3'}}, // LATIN CAPITAL LETTER A WITH BREVE AND HOOK ABOVE
                {{'\u1EB4'}, {'\u1EB5'}}, // LATIN CAPITAL LETTER A WITH BREVE AND TILDE
                {{'\u1EB6'}, {'\u1EB7'}}, // LATIN CAPITAL LETTER A WITH BREVE AND DOT BELOW
                {{'\u1EB8'}, {'\u1EB9'}}, // LATIN CAPITAL LETTER E WITH DOT BELOW
                {{'\u1EBA'}, {'\u1EBB'}}, // LATIN CAPITAL LETTER E WITH HOOK ABOVE
                {{'\u1EBC'}, {'\u1EBD'}}, // LATIN CAPITAL LETTER E WITH TILDE
                {{'\u1EBE'}, {'\u1EBF'}}, // LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND ACUTE
                {{'\u1EC0'}, {'\u1EC1'}}, // LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND GRAVE
                {{'\u1EC2'}, {'\u1EC3'}}, // LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND HOOK ABOVE
                {{'\u1EC4'}, {'\u1EC5'}}, // LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND TILDE
                {{'\u1EC6'}, {'\u1EC7'}}, // LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND DOT BELOW
                {{'\u1EC8'}, {'\u1EC9'}}, // LATIN CAPITAL LETTER I WITH HOOK ABOVE
                {{'\u1ECA'}, {'\u1ECB'}}, // LATIN CAPITAL LETTER I WITH DOT BELOW
                {{'\u1ECC'}, {'\u1ECD'}}, // LATIN CAPITAL LETTER O WITH DOT BELOW
                {{'\u1ECE'}, {'\u1ECF'}}, // LATIN CAPITAL LETTER O WITH HOOK ABOVE
                {{'\u1ED0'}, {'\u1ED1'}}, // LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND ACUTE
                {{'\u1ED2'}, {'\u1ED3'}}, // LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND GRAVE
                {{'\u1ED4'}, {'\u1ED5'}}, // LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND HOOK ABOVE
                {{'\u1ED6'}, {'\u1ED7'}}, // LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND TILDE
                {{'\u1ED8'}, {'\u1ED9'}}, // LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND DOT BELOW
                {{'\u1EDA'}, {'\u1EDB'}}, // LATIN CAPITAL LETTER O WITH HORN AND ACUTE
                {{'\u1EDC'}, {'\u1EDD'}}, // LATIN CAPITAL LETTER O WITH HORN AND GRAVE
                {{'\u1EDE'}, {'\u1EDF'}}, // LATIN CAPITAL LETTER O WITH HORN AND HOOK ABOVE
                {{'\u1EE0'}, {'\u1EE1'}}, // LATIN CAPITAL LETTER O WITH HORN AND TILDE
                {{'\u1EE2'}, {'\u1EE3'}}, // LATIN CAPITAL LETTER O WITH HORN AND DOT BELOW
                {{'\u1EE4'}, {'\u1EE5'}}, // LATIN CAPITAL LETTER U WITH DOT BELOW
                {{'\u1EE6'}, {'\u1EE7'}}, // LATIN CAPITAL LETTER U WITH HOOK ABOVE
                {{'\u1EE8'}, {'\u1EE9'}}, // LATIN CAPITAL LETTER U WITH HORN AND ACUTE
                {{'\u1EEA'}, {'\u1EEB'}}, // LATIN CAPITAL LETTER U WITH HORN AND GRAVE
                {{'\u1EEC'}, {'\u1EED'}}, // LATIN CAPITAL LETTER U WITH HORN AND HOOK ABOVE
                {{'\u1EEE'}, {'\u1EEF'}}, // LATIN CAPITAL LETTER U WITH HORN AND TILDE
                {{'\u1EF0'}, {'\u1EF1'}}, // LATIN CAPITAL LETTER U WITH HORN AND DOT BELOW
                {{'\u1EF2'}, {'\u1EF3'}}, // LATIN CAPITAL LETTER Y WITH GRAVE
                {{'\u1EF4'}, {'\u1EF5'}}, // LATIN CAPITAL LETTER Y WITH DOT BELOW
                {{'\u1EF6'}, {'\u1EF7'}}, // LATIN CAPITAL LETTER Y WITH HOOK ABOVE
                {{'\u1EF8'}, {'\u1EF9'}}, // LATIN CAPITAL LETTER Y WITH TILDE
        };
        FOLDINGMAP[31] = new char[][][]{{{'\u1F08'}, {'\u1F00'}}, // GREEK CAPITAL LETTER ALPHA WITH PSILI
                {{'\u1F09'}, {'\u1F01'}}, // GREEK CAPITAL LETTER ALPHA WITH DASIA
                {{'\u1F0A'}, {'\u1F02'}}, // GREEK CAPITAL LETTER ALPHA WITH PSILI AND VARIA
                {{'\u1F0B'}, {'\u1F03'}}, // GREEK CAPITAL LETTER ALPHA WITH DASIA AND VARIA
                {{'\u1F0C'}, {'\u1F04'}}, // GREEK CAPITAL LETTER ALPHA WITH PSILI AND OXIA
                {{'\u1F0D'}, {'\u1F05'}}, // GREEK CAPITAL LETTER ALPHA WITH DASIA AND OXIA
                {{'\u1F0E'}, {'\u1F06'}}, // GREEK CAPITAL LETTER ALPHA WITH PSILI AND PERISPOMENI
                {{'\u1F0F'}, {'\u1F07'}}, // GREEK CAPITAL LETTER ALPHA WITH DASIA AND PERISPOMENI
                {{'\u1F18'}, {'\u1F10'}}, // GREEK CAPITAL LETTER EPSILON WITH PSILI
                {{'\u1F19'}, {'\u1F11'}}, // GREEK CAPITAL LETTER EPSILON WITH DASIA
                {{'\u1F1A'}, {'\u1F12'}}, // GREEK CAPITAL LETTER EPSILON WITH PSILI AND VARIA
                {{'\u1F1B'}, {'\u1F13'}}, // GREEK CAPITAL LETTER EPSILON WITH DASIA AND VARIA
                {{'\u1F1C'}, {'\u1F14'}}, // GREEK CAPITAL LETTER EPSILON WITH PSILI AND OXIA
                {{'\u1F1D'}, {'\u1F15'}}, // GREEK CAPITAL LETTER EPSILON WITH DASIA AND OXIA
                {{'\u1F28'}, {'\u1F20'}}, // GREEK CAPITAL LETTER ETA WITH PSILI
                {{'\u1F29'}, {'\u1F21'}}, // GREEK CAPITAL LETTER ETA WITH DASIA
                {{'\u1F2A'}, {'\u1F22'}}, // GREEK CAPITAL LETTER ETA WITH PSILI AND VARIA
                {{'\u1F2B'}, {'\u1F23'}}, // GREEK CAPITAL LETTER ETA WITH DASIA AND VARIA
                {{'\u1F2C'}, {'\u1F24'}}, // GREEK CAPITAL LETTER ETA WITH PSILI AND OXIA
                {{'\u1F2D'}, {'\u1F25'}}, // GREEK CAPITAL LETTER ETA WITH DASIA AND OXIA
                {{'\u1F2E'}, {'\u1F26'}}, // GREEK CAPITAL LETTER ETA WITH PSILI AND PERISPOMENI
                {{'\u1F2F'}, {'\u1F27'}}, // GREEK CAPITAL LETTER ETA WITH DASIA AND PERISPOMENI
                {{'\u1F38'}, {'\u1F30'}}, // GREEK CAPITAL LETTER IOTA WITH PSILI
                {{'\u1F39'}, {'\u1F31'}}, // GREEK CAPITAL LETTER IOTA WITH DASIA
                {{'\u1F3A'}, {'\u1F32'}}, // GREEK CAPITAL LETTER IOTA WITH PSILI AND VARIA
                {{'\u1F3B'}, {'\u1F33'}}, // GREEK CAPITAL LETTER IOTA WITH DASIA AND VARIA
                {{'\u1F3C'}, {'\u1F34'}}, // GREEK CAPITAL LETTER IOTA WITH PSILI AND OXIA
                {{'\u1F3D'}, {'\u1F35'}}, // GREEK CAPITAL LETTER IOTA WITH DASIA AND OXIA
                {{'\u1F3E'}, {'\u1F36'}}, // GREEK CAPITAL LETTER IOTA WITH PSILI AND PERISPOMENI
                {{'\u1F3F'}, {'\u1F37'}}, // GREEK CAPITAL LETTER IOTA WITH DASIA AND PERISPOMENI
                {{'\u1F48'}, {'\u1F40'}}, // GREEK CAPITAL LETTER OMICRON WITH PSILI
                {{'\u1F49'}, {'\u1F41'}}, // GREEK CAPITAL LETTER OMICRON WITH DASIA
                {{'\u1F4A'}, {'\u1F42'}}, // GREEK CAPITAL LETTER OMICRON WITH PSILI AND VARIA
                {{'\u1F4B'}, {'\u1F43'}}, // GREEK CAPITAL LETTER OMICRON WITH DASIA AND VARIA
                {{'\u1F4C'}, {'\u1F44'}}, // GREEK CAPITAL LETTER OMICRON WITH PSILI AND OXIA
                {{'\u1F4D'}, {'\u1F45'}}, // GREEK CAPITAL LETTER OMICRON WITH DASIA AND OXIA
                {{'\u1F50'}, {'\u03C5', '\u0313',}}, // GREEK SMALL LETTER UPSILON WITH PSILI
                {{'\u1F52'}, {'\u03C5', '\u0313', '\u0300',}}, // GREEK SMALL LETTER UPSILON WITH PSILI AND VARIA
                {{'\u1F54'}, {'\u03C5', '\u0313', '\u0301',}}, // GREEK SMALL LETTER UPSILON WITH PSILI AND OXIA
                {{'\u1F56'}, {'\u03C5', '\u0313', '\u0342',}}, // GREEK SMALL LETTER UPSILON WITH PSILI AND PERISPOMENI
                {{'\u1F59'}, {'\u1F51'}}, // GREEK CAPITAL LETTER UPSILON WITH DASIA
                {{'\u1F5B'}, {'\u1F53'}}, // GREEK CAPITAL LETTER UPSILON WITH DASIA AND VARIA
                {{'\u1F5D'}, {'\u1F55'}}, // GREEK CAPITAL LETTER UPSILON WITH DASIA AND OXIA
                {{'\u1F5F'}, {'\u1F57'}}, // GREEK CAPITAL LETTER UPSILON WITH DASIA AND PERISPOMENI
                {{'\u1F68'}, {'\u1F60'}}, // GREEK CAPITAL LETTER OMEGA WITH PSILI
                {{'\u1F69'}, {'\u1F61'}}, // GREEK CAPITAL LETTER OMEGA WITH DASIA
                {{'\u1F6A'}, {'\u1F62'}}, // GREEK CAPITAL LETTER OMEGA WITH PSILI AND VARIA
                {{'\u1F6B'}, {'\u1F63'}}, // GREEK CAPITAL LETTER OMEGA WITH DASIA AND VARIA
                {{'\u1F6C'}, {'\u1F64'}}, // GREEK CAPITAL LETTER OMEGA WITH PSILI AND OXIA
                {{'\u1F6D'}, {'\u1F65'}}, // GREEK CAPITAL LETTER OMEGA WITH DASIA AND OXIA
                {{'\u1F6E'}, {'\u1F66'}}, // GREEK CAPITAL LETTER OMEGA WITH PSILI AND PERISPOMENI
                {{'\u1F6F'}, {'\u1F67'}}, // GREEK CAPITAL LETTER OMEGA WITH DASIA AND PERISPOMENI
                {{'\u1F80'}, {'\u1F00', '\u03B9',}}, // GREEK SMALL LETTER ALPHA WITH PSILI AND YPOGEGRAMMENI
                {{'\u1F81'}, {'\u1F01', '\u03B9',}}, // GREEK SMALL LETTER ALPHA WITH DASIA AND YPOGEGRAMMENI
                {{'\u1F82'}, {'\u1F02', '\u03B9',}}, // GREEK SMALL LETTER ALPHA WITH PSILI AND VARIA AND YPOGEGRAMMENI
                {{'\u1F83'}, {'\u1F03', '\u03B9',}}, // GREEK SMALL LETTER ALPHA WITH DASIA AND VARIA AND YPOGEGRAMMENI
                {{'\u1F84'}, {'\u1F04', '\u03B9',}}, // GREEK SMALL LETTER ALPHA WITH PSILI AND OXIA AND YPOGEGRAMMENI
                {{'\u1F85'}, {'\u1F05', '\u03B9',}}, // GREEK SMALL LETTER ALPHA WITH DASIA AND OXIA AND YPOGEGRAMMENI
                {{'\u1F86'}, {'\u1F06', '\u03B9',}}, // GREEK SMALL LETTER ALPHA WITH PSILI AND PERISPOMENI AND YPOGEGRAMMENI
                {{'\u1F87'}, {'\u1F07', '\u03B9',}}, // GREEK SMALL LETTER ALPHA WITH DASIA AND PERISPOMENI AND YPOGEGRAMMENI
                {{'\u1F88'}, {'\u1F00', '\u03B9',}}, // GREEK CAPITAL LETTER ALPHA WITH PSILI AND PROSGEGRAMMENI
                {{'\u1F89'}, {'\u1F01', '\u03B9',}}, // GREEK CAPITAL LETTER ALPHA WITH DASIA AND PROSGEGRAMMENI
                {{'\u1F8A'}, {'\u1F02', '\u03B9',}}, // GREEK CAPITAL LETTER ALPHA WITH PSILI AND VARIA AND PROSGEGRAMMENI
                {{'\u1F8B'}, {'\u1F03', '\u03B9',}}, // GREEK CAPITAL LETTER ALPHA WITH DASIA AND VARIA AND PROSGEGRAMMENI
                {{'\u1F8C'}, {'\u1F04', '\u03B9',}}, // GREEK CAPITAL LETTER ALPHA WITH PSILI AND OXIA AND PROSGEGRAMMENI
                {{'\u1F8D'}, {'\u1F05', '\u03B9',}}, // GREEK CAPITAL LETTER ALPHA WITH DASIA AND OXIA AND PROSGEGRAMMENI
                {{'\u1F8E'}, {'\u1F06', '\u03B9',}}, // GREEK CAPITAL LETTER ALPHA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
                {{'\u1F8F'}, {'\u1F07', '\u03B9',}}, // GREEK CAPITAL LETTER ALPHA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
                {{'\u1F90'}, {'\u1F20', '\u03B9',}}, // GREEK SMALL LETTER ETA WITH PSILI AND YPOGEGRAMMENI
                {{'\u1F91'}, {'\u1F21', '\u03B9',}}, // GREEK SMALL LETTER ETA WITH DASIA AND YPOGEGRAMMENI
                {{'\u1F92'}, {'\u1F22', '\u03B9',}}, // GREEK SMALL LETTER ETA WITH PSILI AND VARIA AND YPOGEGRAMMENI
                {{'\u1F93'}, {'\u1F23', '\u03B9',}}, // GREEK SMALL LETTER ETA WITH DASIA AND VARIA AND YPOGEGRAMMENI
                {{'\u1F94'}, {'\u1F24', '\u03B9',}}, // GREEK SMALL LETTER ETA WITH PSILI AND OXIA AND YPOGEGRAMMENI
                {{'\u1F95'}, {'\u1F25', '\u03B9',}}, // GREEK SMALL LETTER ETA WITH DASIA AND OXIA AND YPOGEGRAMMENI
                {{'\u1F96'}, {'\u1F26', '\u03B9',}}, // GREEK SMALL LETTER ETA WITH PSILI AND PERISPOMENI AND YPOGEGRAMMENI
                {{'\u1F97'}, {'\u1F27', '\u03B9',}}, // GREEK SMALL LETTER ETA WITH DASIA AND PERISPOMENI AND YPOGEGRAMMENI
                {{'\u1F98'}, {'\u1F20', '\u03B9',}}, // GREEK CAPITAL LETTER ETA WITH PSILI AND PROSGEGRAMMENI
                {{'\u1F99'}, {'\u1F21', '\u03B9',}}, // GREEK CAPITAL LETTER ETA WITH DASIA AND PROSGEGRAMMENI
                {{'\u1F9A'}, {'\u1F22', '\u03B9',}}, // GREEK CAPITAL LETTER ETA WITH PSILI AND VARIA AND PROSGEGRAMMENI
                {{'\u1F9B'}, {'\u1F23', '\u03B9',}}, // GREEK CAPITAL LETTER ETA WITH DASIA AND VARIA AND PROSGEGRAMMENI
                {{'\u1F9C'}, {'\u1F24', '\u03B9',}}, // GREEK CAPITAL LETTER ETA WITH PSILI AND OXIA AND PROSGEGRAMMENI
                {{'\u1F9D'}, {'\u1F25', '\u03B9',}}, // GREEK CAPITAL LETTER ETA WITH DASIA AND OXIA AND PROSGEGRAMMENI
                {{'\u1F9E'}, {'\u1F26', '\u03B9',}}, // GREEK CAPITAL LETTER ETA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
                {{'\u1F9F'}, {'\u1F27', '\u03B9',}}, // GREEK CAPITAL LETTER ETA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
                {{'\u1FA0'}, {'\u1F60', '\u03B9',}}, // GREEK SMALL LETTER OMEGA WITH PSILI AND YPOGEGRAMMENI
                {{'\u1FA1'}, {'\u1F61', '\u03B9',}}, // GREEK SMALL LETTER OMEGA WITH DASIA AND YPOGEGRAMMENI
                {{'\u1FA2'}, {'\u1F62', '\u03B9',}}, // GREEK SMALL LETTER OMEGA WITH PSILI AND VARIA AND YPOGEGRAMMENI
                {{'\u1FA3'}, {'\u1F63', '\u03B9',}}, // GREEK SMALL LETTER OMEGA WITH DASIA AND VARIA AND YPOGEGRAMMENI
                {{'\u1FA4'}, {'\u1F64', '\u03B9',}}, // GREEK SMALL LETTER OMEGA WITH PSILI AND OXIA AND YPOGEGRAMMENI
                {{'\u1FA5'}, {'\u1F65', '\u03B9',}}, // GREEK SMALL LETTER OMEGA WITH DASIA AND OXIA AND YPOGEGRAMMENI
                {{'\u1FA6'}, {'\u1F66', '\u03B9',}}, // GREEK SMALL LETTER OMEGA WITH PSILI AND PERISPOMENI AND YPOGEGRAMMENI
                {{'\u1FA7'}, {'\u1F67', '\u03B9',}}, // GREEK SMALL LETTER OMEGA WITH DASIA AND PERISPOMENI AND YPOGEGRAMMENI
                {{'\u1FA8'}, {'\u1F60', '\u03B9',}}, // GREEK CAPITAL LETTER OMEGA WITH PSILI AND PROSGEGRAMMENI
                {{'\u1FA9'}, {'\u1F61', '\u03B9',}}, // GREEK CAPITAL LETTER OMEGA WITH DASIA AND PROSGEGRAMMENI
                {{'\u1FAA'}, {'\u1F62', '\u03B9',}}, // GREEK CAPITAL LETTER OMEGA WITH PSILI AND VARIA AND PROSGEGRAMMENI
                {{'\u1FAB'}, {'\u1F63', '\u03B9',}}, // GREEK CAPITAL LETTER OMEGA WITH DASIA AND VARIA AND PROSGEGRAMMENI
                {{'\u1FAC'}, {'\u1F64', '\u03B9',}}, // GREEK CAPITAL LETTER OMEGA WITH PSILI AND OXIA AND PROSGEGRAMMENI
                {{'\u1FAD'}, {'\u1F65', '\u03B9',}}, // GREEK CAPITAL LETTER OMEGA WITH DASIA AND OXIA AND PROSGEGRAMMENI
                {{'\u1FAE'}, {'\u1F66', '\u03B9',}}, // GREEK CAPITAL LETTER OMEGA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
                {{'\u1FAF'}, {'\u1F67', '\u03B9',}}, // GREEK CAPITAL LETTER OMEGA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
                {{'\u1FB2'}, {'\u1F70', '\u03B9',}}, // GREEK SMALL LETTER ALPHA WITH VARIA AND YPOGEGRAMMENI
                {{'\u1FB3'}, {'\u03B1', '\u03B9',}}, // GREEK SMALL LETTER ALPHA WITH YPOGEGRAMMENI
                {{'\u1FB4'}, {'\u03AC', '\u03B9',}}, // GREEK SMALL LETTER ALPHA WITH OXIA AND YPOGEGRAMMENI
                {{'\u1FB6'}, {'\u03B1', '\u0342',}}, // GREEK SMALL LETTER ALPHA WITH PERISPOMENI
                {{'\u1FB7'}, {'\u03B1', '\u0342', '\u03B9',}}, // GREEK SMALL LETTER ALPHA WITH PERISPOMENI AND YPOGEGRAMMENI
                {{'\u1FB8'}, {'\u1FB0'}}, // GREEK CAPITAL LETTER ALPHA WITH VRACHY
                {{'\u1FB9'}, {'\u1FB1'}}, // GREEK CAPITAL LETTER ALPHA WITH MACRON
                {{'\u1FBA'}, {'\u1F70'}}, // GREEK CAPITAL LETTER ALPHA WITH VARIA
                {{'\u1FBB'}, {'\u1F71'}}, // GREEK CAPITAL LETTER ALPHA WITH OXIA
                {{'\u1FBC'}, {'\u03B1', '\u03B9',}}, // GREEK CAPITAL LETTER ALPHA WITH PROSGEGRAMMENI
                {{'\u1FBE'}, {'\u03B9'}}, // GREEK PROSGEGRAMMENI
                {{'\u1FC2'}, {'\u1F74', '\u03B9',}}, // GREEK SMALL LETTER ETA WITH VARIA AND YPOGEGRAMMENI
                {{'\u1FC3'}, {'\u03B7', '\u03B9',}}, // GREEK SMALL LETTER ETA WITH YPOGEGRAMMENI
                {{'\u1FC4'}, {'\u03AE', '\u03B9',}}, // GREEK SMALL LETTER ETA WITH OXIA AND YPOGEGRAMMENI
                {{'\u1FC6'}, {'\u03B7', '\u0342',}}, // GREEK SMALL LETTER ETA WITH PERISPOMENI
                {{'\u1FC7'}, {'\u03B7', '\u0342', '\u03B9',}}, // GREEK SMALL LETTER ETA WITH PERISPOMENI AND YPOGEGRAMMENI
                {{'\u1FC8'}, {'\u1F72'}}, // GREEK CAPITAL LETTER EPSILON WITH VARIA
                {{'\u1FC9'}, {'\u1F73'}}, // GREEK CAPITAL LETTER EPSILON WITH OXIA
                {{'\u1FCA'}, {'\u1F74'}}, // GREEK CAPITAL LETTER ETA WITH VARIA
                {{'\u1FCB'}, {'\u1F75'}}, // GREEK CAPITAL LETTER ETA WITH OXIA
                {{'\u1FCC'}, {'\u03B7', '\u03B9',}}, // GREEK CAPITAL LETTER ETA WITH PROSGEGRAMMENI
                {{'\u1FD2'}, {'\u03B9', '\u0308', '\u0300',}}, // GREEK SMALL LETTER IOTA WITH DIALYTIKA AND VARIA
                {{'\u1FD3'}, {'\u03B9', '\u0308', '\u0301',}}, // GREEK SMALL LETTER IOTA WITH DIALYTIKA AND OXIA
                {{'\u1FD6'}, {'\u03B9', '\u0342',}}, // GREEK SMALL LETTER IOTA WITH PERISPOMENI
                {{'\u1FD7'}, {'\u03B9', '\u0308', '\u0342',}}, // GREEK SMALL LETTER IOTA WITH DIALYTIKA AND PERISPOMENI
                {{'\u1FD8'}, {'\u1FD0'}}, // GREEK CAPITAL LETTER IOTA WITH VRACHY
                {{'\u1FD9'}, {'\u1FD1'}}, // GREEK CAPITAL LETTER IOTA WITH MACRON
                {{'\u1FDA'}, {'\u1F76'}}, // GREEK CAPITAL LETTER IOTA WITH VARIA
                {{'\u1FDB'}, {'\u1F77'}}, // GREEK CAPITAL LETTER IOTA WITH OXIA
                {{'\u1FE2'}, {'\u03C5', '\u0308', '\u0300',}}, // GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND VARIA
                {{'\u1FE3'}, {'\u03C5', '\u0308', '\u0301',}}, // GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND OXIA
                {{'\u1FE4'}, {'\u03C1', '\u0313',}}, // GREEK SMALL LETTER RHO WITH PSILI
                {{'\u1FE6'}, {'\u03C5', '\u0342',}}, // GREEK SMALL LETTER UPSILON WITH PERISPOMENI
                {{'\u1FE7'}, {'\u03C5', '\u0308', '\u0342',}}, // GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND PERISPOMENI
                {{'\u1FE8'}, {'\u1FE0'}}, // GREEK CAPITAL LETTER UPSILON WITH VRACHY
                {{'\u1FE9'}, {'\u1FE1'}}, // GREEK CAPITAL LETTER UPSILON WITH MACRON
                {{'\u1FEA'}, {'\u1F7A'}}, // GREEK CAPITAL LETTER UPSILON WITH VARIA
                {{'\u1FEB'}, {'\u1F7B'}}, // GREEK CAPITAL LETTER UPSILON WITH OXIA
                {{'\u1FEC'}, {'\u1FE5'}}, // GREEK CAPITAL LETTER RHO WITH DASIA
                {{'\u1FF2'}, {'\u1F7C', '\u03B9',}}, // GREEK SMALL LETTER OMEGA WITH VARIA AND YPOGEGRAMMENI
                {{'\u1FF3'}, {'\u03C9', '\u03B9',}}, // GREEK SMALL LETTER OMEGA WITH YPOGEGRAMMENI
                {{'\u1FF4'}, {'\u03CE', '\u03B9',}}, // GREEK SMALL LETTER OMEGA WITH OXIA AND YPOGEGRAMMENI
                {{'\u1FF6'}, {'\u03C9', '\u0342',}}, // GREEK SMALL LETTER OMEGA WITH PERISPOMENI
                {{'\u1FF7'}, {'\u03C9', '\u0342', '\u03B9',}}, // GREEK SMALL LETTER OMEGA WITH PERISPOMENI AND YPOGEGRAMMENI
                {{'\u1FF8'}, {'\u1F78'}}, // GREEK CAPITAL LETTER OMICRON WITH VARIA
                {{'\u1FF9'}, {'\u1F79'}}, // GREEK CAPITAL LETTER OMICRON WITH OXIA
                {{'\u1FFA'}, {'\u1F7C'}}, // GREEK CAPITAL LETTER OMEGA WITH VARIA
                {{'\u1FFB'}, {'\u1F7D'}}, // GREEK CAPITAL LETTER OMEGA WITH OXIA
                {{'\u1FFC'}, {'\u03C9', '\u03B9',}}, // GREEK CAPITAL LETTER OMEGA WITH PROSGEGRAMMENI
        };
        FOLDINGMAP[33] = new char[][][]{{{'\u2126'}, {'\u03C9'}}, // OHM SIGN
                {{'\u212A'}, {'\u006B'}}, // KELVIN SIGN
                {{'\u212B'}, {'\u00E5'}}, // ANGSTROM SIGN
                {{'\u2160'}, {'\u2170'}}, // ROMAN NUMERAL ONE
                {{'\u2161'}, {'\u2171'}}, // ROMAN NUMERAL TWO
                {{'\u2162'}, {'\u2172'}}, // ROMAN NUMERAL THREE
                {{'\u2163'}, {'\u2173'}}, // ROMAN NUMERAL FOUR
                {{'\u2164'}, {'\u2174'}}, // ROMAN NUMERAL FIVE
                {{'\u2165'}, {'\u2175'}}, // ROMAN NUMERAL SIX
                {{'\u2166'}, {'\u2176'}}, // ROMAN NUMERAL SEVEN
                {{'\u2167'}, {'\u2177'}}, // ROMAN NUMERAL EIGHT
                {{'\u2168'}, {'\u2178'}}, // ROMAN NUMERAL NINE
                {{'\u2169'}, {'\u2179'}}, // ROMAN NUMERAL TEN
                {{'\u216A'}, {'\u217A'}}, // ROMAN NUMERAL ELEVEN
                {{'\u216B'}, {'\u217B'}}, // ROMAN NUMERAL TWELVE
                {{'\u216C'}, {'\u217C'}}, // ROMAN NUMERAL FIFTY
                {{'\u216D'}, {'\u217D'}}, // ROMAN NUMERAL ONE HUNDRED
                {{'\u216E'}, {'\u217E'}}, // ROMAN NUMERAL FIVE HUNDRED
                {{'\u216F'}, {'\u217F'}}, // ROMAN NUMERAL ONE THOUSAND
        };
        FOLDINGMAP[36] = new char[][][]{{{'\u24B6'}, {'\u24D0'}}, // CIRCLED LATIN CAPITAL LETTER A
                {{'\u24B7'}, {'\u24D1'}}, // CIRCLED LATIN CAPITAL LETTER B
                {{'\u24B8'}, {'\u24D2'}}, // CIRCLED LATIN CAPITAL LETTER C
                {{'\u24B9'}, {'\u24D3'}}, // CIRCLED LATIN CAPITAL LETTER D
                {{'\u24BA'}, {'\u24D4'}}, // CIRCLED LATIN CAPITAL LETTER E
                {{'\u24BB'}, {'\u24D5'}}, // CIRCLED LATIN CAPITAL LETTER F
                {{'\u24BC'}, {'\u24D6'}}, // CIRCLED LATIN CAPITAL LETTER G
                {{'\u24BD'}, {'\u24D7'}}, // CIRCLED LATIN CAPITAL LETTER H
                {{'\u24BE'}, {'\u24D8'}}, // CIRCLED LATIN CAPITAL LETTER I
                {{'\u24BF'}, {'\u24D9'}}, // CIRCLED LATIN CAPITAL LETTER J
                {{'\u24C0'}, {'\u24DA'}}, // CIRCLED LATIN CAPITAL LETTER K
                {{'\u24C1'}, {'\u24DB'}}, // CIRCLED LATIN CAPITAL LETTER L
                {{'\u24C2'}, {'\u24DC'}}, // CIRCLED LATIN CAPITAL LETTER M
                {{'\u24C3'}, {'\u24DD'}}, // CIRCLED LATIN CAPITAL LETTER N
                {{'\u24C4'}, {'\u24DE'}}, // CIRCLED LATIN CAPITAL LETTER O
                {{'\u24C5'}, {'\u24DF'}}, // CIRCLED LATIN CAPITAL LETTER P
                {{'\u24C6'}, {'\u24E0'}}, // CIRCLED LATIN CAPITAL LETTER Q
                {{'\u24C7'}, {'\u24E1'}}, // CIRCLED LATIN CAPITAL LETTER R
                {{'\u24C8'}, {'\u24E2'}}, // CIRCLED LATIN CAPITAL LETTER S
                {{'\u24C9'}, {'\u24E3'}}, // CIRCLED LATIN CAPITAL LETTER T
                {{'\u24CA'}, {'\u24E4'}}, // CIRCLED LATIN CAPITAL LETTER U
                {{'\u24CB'}, {'\u24E5'}}, // CIRCLED LATIN CAPITAL LETTER V
                {{'\u24CC'}, {'\u24E6'}}, // CIRCLED LATIN CAPITAL LETTER W
                {{'\u24CD'}, {'\u24E7'}}, // CIRCLED LATIN CAPITAL LETTER X
                {{'\u24CE'}, {'\u24E8'}}, // CIRCLED LATIN CAPITAL LETTER Y
                {{'\u24CF'}, {'\u24E9'}}, // CIRCLED LATIN CAPITAL LETTER Z
        };
        FOLDINGMAP[251] = new char[][][]{{{'\uFB00'}, {'\u0066', '\u0066',}}, // LATIN SMALL LIGATURE FF
                {{'\uFB01'}, {'\u0066', '\u0069',}}, // LATIN SMALL LIGATURE FI
                {{'\uFB02'}, {'\u0066', '\u006C',}}, // LATIN SMALL LIGATURE FL
                {{'\uFB03'}, {'\u0066', '\u0066', '\u0069',}}, // LATIN SMALL LIGATURE FFI
                {{'\uFB04'}, {'\u0066', '\u0066', '\u006C',}}, // LATIN SMALL LIGATURE FFL
                {{'\uFB05'}, {'\u0073', '\u0074',}}, // LATIN SMALL LIGATURE LONG S T
                {{'\uFB06'}, {'\u0073', '\u0074',}}, // LATIN SMALL LIGATURE ST
                {{'\uFB13'}, {'\u0574', '\u0576',}}, // ARMENIAN SMALL LIGATURE MEN NOW
                {{'\uFB14'}, {'\u0574', '\u0565',}}, // ARMENIAN SMALL LIGATURE MEN ECH
                {{'\uFB15'}, {'\u0574', '\u056B',}}, // ARMENIAN SMALL LIGATURE MEN INI
                {{'\uFB16'}, {'\u057E', '\u0576',}}, // ARMENIAN SMALL LIGATURE VEW NOW
                {{'\uFB17'}, {'\u0574', '\u056D',}}, // ARMENIAN SMALL LIGATURE MEN XEH
        };
        FOLDINGMAP[255] = new char[][][]{{{'\uFF21'}, {'\uFF41'}}, // FULLWIDTH LATIN CAPITAL LETTER A
                {{'\uFF22'}, {'\uFF42'}}, // FULLWIDTH LATIN CAPITAL LETTER B
                {{'\uFF23'}, {'\uFF43'}}, // FULLWIDTH LATIN CAPITAL LETTER C
                {{'\uFF24'}, {'\uFF44'}}, // FULLWIDTH LATIN CAPITAL LETTER D
                {{'\uFF25'}, {'\uFF45'}}, // FULLWIDTH LATIN CAPITAL LETTER E
                {{'\uFF26'}, {'\uFF46'}}, // FULLWIDTH LATIN CAPITAL LETTER F
                {{'\uFF27'}, {'\uFF47'}}, // FULLWIDTH LATIN CAPITAL LETTER G
                {{'\uFF28'}, {'\uFF48'}}, // FULLWIDTH LATIN CAPITAL LETTER H
                {{'\uFF29'}, {'\uFF49'}}, // FULLWIDTH LATIN CAPITAL LETTER I
                {{'\uFF2A'}, {'\uFF4A'}}, // FULLWIDTH LATIN CAPITAL LETTER J
                {{'\uFF2B'}, {'\uFF4B'}}, // FULLWIDTH LATIN CAPITAL LETTER K
                {{'\uFF2C'}, {'\uFF4C'}}, // FULLWIDTH LATIN CAPITAL LETTER L
                {{'\uFF2D'}, {'\uFF4D'}}, // FULLWIDTH LATIN CAPITAL LETTER M
                {{'\uFF2E'}, {'\uFF4E'}}, // FULLWIDTH LATIN CAPITAL LETTER N
                {{'\uFF2F'}, {'\uFF4F'}}, // FULLWIDTH LATIN CAPITAL LETTER O
                {{'\uFF30'}, {'\uFF50'}}, // FULLWIDTH LATIN CAPITAL LETTER P
                {{'\uFF31'}, {'\uFF51'}}, // FULLWIDTH LATIN CAPITAL LETTER Q
                {{'\uFF32'}, {'\uFF52'}}, // FULLWIDTH LATIN CAPITAL LETTER R
                {{'\uFF33'}, {'\uFF53'}}, // FULLWIDTH LATIN CAPITAL LETTER S
                {{'\uFF34'}, {'\uFF54'}}, // FULLWIDTH LATIN CAPITAL LETTER T
                {{'\uFF35'}, {'\uFF55'}}, // FULLWIDTH LATIN CAPITAL LETTER U
                {{'\uFF36'}, {'\uFF56'}}, // FULLWIDTH LATIN CAPITAL LETTER V
                {{'\uFF37'}, {'\uFF57'}}, // FULLWIDTH LATIN CAPITAL LETTER W
                {{'\uFF38'}, {'\uFF58'}}, // FULLWIDTH LATIN CAPITAL LETTER X
                {{'\uFF39'}, {'\uFF59'}}, // FULLWIDTH LATIN CAPITAL LETTER Y
                {{'\uFF3A'}, {'\uFF5A'}}, // FULLWIDTH LATIN CAPITAL LETTER Z
        };
        TURKICFOLDINGMAP[0] = new char[][][]{{{'\u0049'}, {'\u0131'}}, // LATIN CAPITAL LETTER I
        };
        TURKICFOLDINGMAP[1] = new char[][][]{{{'\u0130'}, {'\u0069'}}, // LATIN CAPITAL LETTER I WITH DOT ABOVE
        };
    }

    public static String toFoldedCase(String input) {
        return toFoldedCase(input, false);
    }

    public static String toFoldedCase(String input, boolean turkic) {
        if (input == null)
            return null;
        DeferredStringBuilder buf = new DeferredStringBuilder(input);

        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            char[] remap = toFoldedCase(c, turkic);
            if (remap == null) {
                buf.append(c);
            } else {
                // found a match! remap the character
                for (int j = 0; j < remap.length; ++j) {
                    buf.append(remap[j]);
                }
            }
        }
        return buf.toString();
    }

    /**
     * Case fold a single character, returning null if no
     * folding isn't necessary and the char array of the
     * replacement chars if it is necessary.  Currently
     * used by Vampire to in-place case folding to prevent
     * allocating new strings.
     */
    public static char[] toFoldedCase(char c, boolean turkic) {
        int mapBlockIdx = c >> 8;
        char[][][] mapBlock = FOLDINGMAP[mapBlockIdx];
        if (mapBlock == null) {
            // character block isn't mapped - move on
            return null;
        }

        char[] remap = null;
        if (turkic) {
            // The turkic folding map should "win" if we're using turkish
            char[][][] turkishMapBlock = TURKICFOLDINGMAP[mapBlockIdx];
            if (turkishMapBlock != null) {
                remap = findCharInBlock(c, turkishMapBlock);
            }
        }
        if (remap == null) {
            remap = findCharInBlock(c, mapBlock);
        }

        if (remap != null) {
            return remap;
        }
        return null;
    }

    private static char[] findCharInBlock(char c, char[][][] mapBlock) {
        int bottom = 0;
        int top = mapBlock.length;
        int current = top / 2;
        // invariant: top > current >= bottom && ch >= mapBlock[bottom][0]
        while (top - bottom > 1) {
            if (c >= mapBlock[current][0][0]) {
                bottom = current;
            } else {
                top = current;
            }
            current = (top + bottom) / 2;
        }
        if (c == mapBlock[current][0][0]) {
            // found a match! remap the character
            return mapBlock[current][1];
        } else {
            return null;
        }
    }
}
