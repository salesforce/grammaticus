/* 
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.grammar.offline;

import static com.force.i18n.LanguageConstants.*;

import java.util.Locale;

/**
 * Relatively Hard-coded implementation of plural rules for the supported declensions.  Only does ordinal.
 *
 * Based on an implementation from an Intl polyfill, but with some updates to handle negative numbers
 * @see <a href="https://github.com/eemeli/make-plural.js">make-plural.js</a>
 * @see <a href="https://www.unicode.org/cldr/charts/46/supplemental/language_plural_rules.html">plural rules from CLDR</a>
 * @author stamm
 * @since 1.1
 */
public class PluralRulesJsImpl {
    private final static String NO_DIFF = "function (n) {return 'other';}";
    private final static String ONE = "function (n) {return n == 1 || n == -1 ? 'one' : 'other';}";
    private final static String ONE_OR_ZERO = "function (n) {return n == 0 || n == 1 || n == -1 ? 'one' : 'other';}";
    private final static String EXACT_ONE = "function (n) {return n == 1 && !String(n).split('.')[1] ? 'one' : 'other';}";
    private final static String EXACT_ONE_OR_NEG_ONE = "function (n) {return (n == 1 || n == -1) && !String(n).split('.')[1] ? 'one' : 'other';}";

    public static String getSelectFunction(Locale locale) {
        String override = getSelectFunctionOverride(locale);
        return override != null ? override : NO_DIFF;
    }

    public static String getSelectFunctionOverride(Locale locale) {

        switch (locale.getLanguage()) {
             case HINDI:
             case ZULU:
             case KANNADA:
             case GUJARATI:
             case PERSIAN:
             case AMHARIC:return "function am(n) {return n >= 0 && n <= 1 ? 'one' : 'other';}";
             case MARATHI: // above function < ICU64
            	 return ONE;
            	 
             case ARABIC:return "function ar(n) {\n"+
               "var s = String(n).split('.'),t0 = Number(s[0]) == n,n100 = t0 && s[0].slice(-2);\n"+
               "return n == 0 ? 'zero' : n == 1 ? 'one' : n == 2 ? 'two' : n100 >= 3 && n100 <= 10 ? 'few' : n100 >= 11 && n100 <= 99 ? 'many' : 'other';}";
             //case "az":return ONE;
             //case "be":return "function be(n) {\n"+
             //  "var s = String(n).split('.'), t0 = Number(s[0]) == n,n10 = t0 && s[0].slice(-1),n100 = t0 && s[0].slice(-2);\n"+
             //    "return n10 == 1 && n100 != 11 ? 'one' : n10 >= 2 && n10 <= 4 && (n100 < 12 || n100 > 14) ? 'few' : t0 && n10 == 0 || n10 >= 5 && n10 <= 9 || n100 >= 11 && n100 <= 14 ? 'many' : 'other';}";
             case BULGARIAN:return ONE;
             //case "bh":return ONE_OR_ZERO;
             //case "bm":return noDiff;
             case BENGALI:return "function bn(n) {return n >= 0 && n <= 1 ? 'one' : 'other';}\n";
             //case "bo":return noDiff;
             //case "br":return "function br(n) {"+
             //  "var s = String(n).split('.'),t0 = Number(s[0]) == n,n10 = t0 && s[0].slice(-1),n100 = t0 && s[0].slice(-2),n1000000 = t0 && s[0].slice(-6);"+
             //  "return n10 == 1 && n100 != 11 && n100 != 71 && n100 != 91 ? 'one' : n10 == 2 && n100 != 12 && n100 != 72 && n100 != 92 ? 'two' : (n10 == 3 || n10 == 4 || n10 == 9) && (n100 < 10 || n100 > 19) && (n100 < 70 || n100 > 79) && (n100 < 90 || n100 > 99) ? 'few' : n != 0 && t0 && n1000000 == 0 ? 'many' : 'other';}";
             case CROATIAN:
             case BOSNIAN:
             case SERBIAN_CYRILLIC:
             case SERBIAN_LATIN:
             return "function bs(n) {"+
            "var s = String(n).split('.'),i = s[0], f = s[1] || '',v0 = !s[1],i10 = i.slice(-1), i100 = i.slice(-2), f10 = f.slice(-1),f100 = f.slice(-2);"+
            "return v0 && i10 == 1 && i100 != 11 || f10 == 1 && f100 != 11 ? 'one' : v0 && (i10 >= 2 && i10 <= 4) && (i100 < 12 || i100 > 14) || f10 >= 2 && f10 <= 4 && (f100 < 12 || f100 > 14) ? 'few' : 'other';}";
             case CATALAN:return EXACT_ONE;
             case CZECH:return "function cs(n) {"+
               " var s = String(n).split('.'), i = s[0], v0 = !s[1];"+
               "return n == 1 && v0 ? 'one' : i >= 2 && i <= 4 && v0 ? 'few' : !v0 ? 'many' : 'other';}";
             case WELSH:return "function cy(n) {return n == 0 ? 'zero' : n == 1 ? 'one' : n == 2 ? 'two' : n == 3 ? 'few' : n == 6 ? 'many' : 'other';}";
             case DANISH:return "function da(n) {\n"+
            "var s = String(n).split('.'),i = s[0],t0 = Number(s[0]) == n;\n"+
            "return n == 1 || n == -1 || !t0 && (i == 0 || i == 1) ? 'one' : 'other';}";
             case GERMAN:return EXACT_ONE;
             //case "dz":return noDiff;
             //case "ee":return ONE;
             case GREEK:return ONE;
             case ENGLISH:return EXACT_ONE_OR_NEG_ONE;
             case ESPERANTO:return ONE;
             case SPANISH:return ONE;
             case ESTONIAN:return EXACT_ONE;
             case BASQUE:return ONE;
             case FINNISH:return EXACT_ONE;
             //case "fo":return ONE;
             case FRENCH:return "function fr(n) { return n > -1 && n < 2 ? 'one' : 'other'; }";
             //case "fy":return EXACT_ONE;
             case IRISH:return "function ga(n) {\n"+
                     "var s = String(n).split('.'),i = s[0],t0 = Number(s[0]) == n;\n"+
                     "return (n == 1) ? 'one' : (n == 2) ? 'two': ((t0 && n >= 3 && n <= 6)) ? 'few' : ((t0 && n >= 7 && n <= 10)) ? 'many' : 'other'}";
             case ITALIAN: return EXACT_ONE;

             case HAWAIIAN: return EXACT_ONE;
             case HEBREW_ISO:
             case HEBREW:return "function he(n) {\n"+
             	"var s = String(n).split('.'), i = s[0], v0 = !s[1], t0 = Number(s[0]) == n, n10 = t0 && s[0].slice(-1);\n"+
             	// "return n == 1 && v0 ? 'one' : i == 2 && v0 ? 'two' : v0 && (n < 0 || n > 10) && t0 && n10 == 0 ? 'many' : 'other';}";
             	"return (i == 1 && v0) || (i == 0 && !v0) ? 'one' : i == 2 && v0 ? 'two' : 'other';}"; // ICU > 70

             case HUNGARIAN:return ONE;
             case ARMENIAN:return "function hy(n) {return n >= 0 && n < 2 ? 'one' : 'other';}";
             case PUNJABI_WESTERN:
             case PUNJABI:return ONE_OR_ZERO;
             //case "id":return noDiff;
             //case "ig":return noDiff;
             case ICELANDIC:return "function is(n) {"+
               "var s = String(n).split('.'),i = s[0],t = Number(s[1]),t0 = Number(s[0]) == n, i10 = i.slice(-1), i100 = i.slice(-2), t10 = t % 10, t100 = t % 100;"+
               //                "return t0 && i10 == 1 && i100 != 11 || !t0 ? 'one' : 'other';}";  // ICU < 70
               "return t0 && i10 == 1 && (i100 != 11 || (!t10 != 1 && t100 != 11)) ? 'one' : 'other';}";
             //case "ja":return noDiff;
             //case "ji":return EXACT_ONE;
             //case "jv":return noDiff;
             //case "jw":return noDiff;
             case GEORGIAN:return ONE;
             case KAZAKH: return ONE;
             //case "km":return noDiff;
             //case "ko":return noDiff;
             //case "ky":return ONE;
             case LUXEMBOURGISH:return ONE;
             //case "lg":return ONE;
             //case "ln":return ONE_OR_ZERO;
             //case "lo":return noDiff;
             case LITHUANIAN:return "function lt(n) {\n"+
            "var s = String(n).split('.'), f = s[1] || '',t0 = Number(s[0]) == n, n10 = t0 && s[0].slice(-1), n100 = t0 && s[0].slice(-2);" +
            "return n10 == 1 && (n100 < 11 || n100 > 19) ? 'one' : n10 >= 2 && n10 <= 9 && (n100 < 11 || n100 > 19) ? 'few' : f != 0 ? 'many' : 'other';}";
             case LATVIAN:return "function lv(n) {\n"+
            "var s = String(n).split('.'), f = s[1] || '',v = f.length, t0 = Number(s[0]) == n, n10 = t0 && s[0].slice(-1), n100 = t0 && s[0].slice(-2), f100 = f.slice(-2), f10 = f.slice(-1);"+
            "return t0 && n10 == 0 || n100 >= 11 && n100 <= 19 || v == 2 && (f100 >= 11 && f100 <= 19) ? 'zero' : n10 == 1 && n100 != 11 || v == 2 && f10 == 1 && f100 != 11 || v != 2 && f10 == 1 ? 'one' : 'other';}";
             //case "mg":return ONE_OR_ZERO;
             case MALAYALAM:return ONE;
             case MACEDONIAN:return "function mk(n) {\n"+
	             "var s = String(n).split('.'), i = s[0], f = s[1] || '', v0 = !s[1], i10 = i.slice(-1), i100 = i.slice(-2), f10 = f.slice(-1), f100 = f.slice(-2);"+
//	             "return (v0 && i10 == 1 || f10 == 1) ? 'one' : 'other';}"; // < ICU 63
	             "return (v0 && i10 == 1 && i100 != 11 || f10 == 1 && f100 != 11 ) ? 'one' : 'other';}";
             //case "mn":return ONE;
             //case "ms":return noDiff;
             case MALTESE:return "function mt(n) {\n"+
	             "var s = String(n).split('.'),t0 = Number(s[0]) == n,n100 = t0 && s[0].slice(-2);\n"+
	             // "return n == 1 ? 'one' : (n == 0 || (n100 >= 2 && n100 <= 10)) ? 'few' : n100 >= 11 && n100 <= 19 ? 'many' : 'other';}"; // ICU < 70
	             "return n == 1 ? 'one' : n == 2 ? 'two' : (n == 0 || (n100 > 2 && n100 <= 10)) ? 'few' : n100 >= 11 && n100 <= 19 ? 'many' : 'other';}";
             //case "nb":return ONE;
             //case "nd":return ONE;
             //case "ne":return ONE;
             case DUTCH:return EXACT_ONE;
             //case "nn":return ONE;
             case NORWEGIAN:return ONE;
             case POLISH:return "function pl(n) {"+
            "var s = String(n).split('.'), i = s[0], v0 = !s[1], i10 = i.slice(-1), i100 = i.slice(-2);"+
            "return n == 1 && v0 ? 'one' : v0 && (i10 >= 2 && i10 <= 4) && (i100 < 12 || i100 > 14) ? 'few' : v0 && i != 1 && (i10 == 0 || i10 == 1) || v0 && (i10 >= 5 && i10 <= 9) || v0 && (i100 >= 12 && i100 <= 14) ? 'many' : 'other';}";
             //case "ps":return ONE;
             case PORTUGUESE:
                 if ("PT".equals(locale.getCountry())) {
                     return EXACT_ONE;
                 }
                 return "function pt(n) {return n >= 0 && n <= 2 && n != 2 ? 'one' : 'other';}";
             case ROMANSH:return ONE;
             case "mo":
             case ROMANIAN:return "function ro(n) {"+
            "var s = String(n).split('.'),v0 = !s[1],t0 = Number(s[0]) == n, n100 = t0 && s[0].slice(-2);"+
            "return n == 1 && v0 ? 'one' : !v0 || n == 0 || n != 1 && (n100 >= 1 && n100 <= 19) ? 'few' : 'other';}";
			       // "return n == 1 && v0 ? 'one' : !v0 || n == 0 || (n100 >= 2 && n100 <= 19) ? 'few' : 'other';}";  // ICU 64-70
             case RUSSIAN:
             case UKRAINIAN:
             return "function ru(n) {"+
             "var s = String(n).split('.'),i = s[0],v0 = !s[1],i10 = i.slice(-1),i100 = i.slice(-2);"+
             "return v0 && i10 == 1 && i100 != 11 ? 'one' : v0 && (i10 >= 2 && i10 <= 4) && (i100 < 12 || i100 > 14) ? 'few' : v0 && i10 == 0 || v0 && (i10 >= 5 && i10 <= 9) || v0 && (i100 >= 11 && i100 <= 14) ? 'many' : 'other';}";
             //case "sg":return noDiff;
             case SLOVAK:return "function sk(n) {"+
            "var s = String(n).split('.'),i = s[0],v0 = !s[1];"+
            "return n == 1 && v0 ? 'one' : i >= 2 && i <= 4 && v0 ? 'few' : !v0 ? 'many' : 'other';}";
             case SLOVENE:return "function sl(n) {"+
            "var s = String(n).split('.'), i = s[0], v0 = !s[1], i100 = i.slice(-2); "+
            "return v0 && i100 == 1 ? 'one' : v0 && i100 == 2 ? 'two' : v0 && (i100 == 3 || i100 == 4) || !v0 ? 'few' : 'other';}";
             case ALBANIAN:return ONE;
             case AFRIKAANS:return ONE;
             case SWEDISH:return EXACT_ONE;
             case SWAHILI:return EXACT_ONE;
             case XHOSA:return ONE;
             case TAMIL:return ONE;
             case TELUGU:return ONE;
             //case "th":return noDiff;
             case TAGALOG:return "function tl(n) {"+
             "var s = String(n).split('.'), i = s[0], f = s[1] || '', v0 = !s[1], i10 = i.slice(-1), f10 = f.slice(-1);"+
             "return v0 && (i == 1 || i == 2 || i == 3) || v0 && i10 != 4 && i10 != 6 && i10 != 9 || !v0 && f10 != 4 && f10 != 6 && f10 != 9 ? 'one' : 'other';}";
             //case "to":return noDiff;
             case TURKISH:return ONE;
             case URDU:return EXACT_ONE;
             //case "vi":return noDiff;
             //case "zh":return noDiff;
             case GREENLANDIC: return EXACT_ONE;
             case YIDDISH_ISO:
             case YIDDISH: return ONE;
        }
        return null;
    }


}
