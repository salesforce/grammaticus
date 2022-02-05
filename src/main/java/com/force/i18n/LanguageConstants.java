/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

/**
 * Useful constants for Java 7-style string switch statements.
 * @author stamm
 */
public final class LanguageConstants {
    public static final String ENGLISH = "en";  // It's a peculiarity that we use en_US as the default for English instead of en.  Complain if need be
    public static final String GERMAN = "de";
    public static final String SPANISH = "es";
    public static final String PORTUGUESE = "pt"; // Portuguese (Brazilian)
    public static final String FRENCH = "fr";
    public static final String ITALIAN = "it";
    public static final String JAPANESE = "ja";
    public static final String SWEDISH = "sv";
    public static final String KOREAN = "ko";
    public static final String DUTCH = "nl";  // This is historical, should always have been "nl"
    public static final String DANISH = "da";
    public static final String THAI = "th";
    public static final String FINNISH = "fi";
    public static final String CHINESE = "zh";
    public static final String RUSSIAN = "ru";
    public static final String NORWEGIAN = "no";  // Technically, this should be "nb", but that would be confusing to java and HTML
    public static final String HUNGARIAN = "hu";
    public static final String POLISH = "pl";
    public static final String CZECH = "cs";
    public static final String TURKISH = "tr";
    public static final String INDONESIAN = "in";  // Fix java screwup
    public static final String ROMANIAN = "ro";
    public static final String VIETNAMESE = "vi";
    public static final String UKRAINIAN = "uk";
    public static final String HEBREW = "iw"; // Fix java screwup
    public static final String GREEK = "el";
    public static final String BULGARIAN = "bg";
    public static final String ARABIC = "ar";
    public static final String SLOVAK = "sk";
    public static final String CROATIAN = "hr";
    public static final String SLOVENE = "sl";
    public static final String BOSNIAN = "bs";
    public static final String MACEDONIAN = "mk";
    public static final String LATVIAN = "lv";
    public static final String LITHUANIAN = "lt";
    public static final String ESTONIAN = "et";
    public static final String ALBANIAN = "sq";
    public static final String MALTESE = "mt";
    public static final String IRISH = "ga";
    public static final String BASQUE = "eu";
    public static final String WELSH = "cy";
    public static final String ICELANDIC = "is";
    public static final String MALAY = "ms";
    public static final String TAGALOG = "tl";
    public static final String LUXEMBOURGISH = "lb";
    public static final String ROMANSH = "rm";
    public static final String ARMENIAN = "hy";
    public static final String HINDI = "hi";
    public static final String URDU = "ur";
    public static final String BENGALI = "bn";
    public static final String TAMIL = "ta";
    public static final String GEORGIAN = "ka";
    public static final String ESPERANTO = "eo";  // Esperanto is our "fake" language, always leave it last
    public static final String SERBIAN_CYRILLIC = "sr";  // http://tlt.its.psu.edu/suggestions/international/bylanguage/serbocroatian.html
    public static final String SERBIAN_LATIN ="sh";  // sh is deprecated, but using sr-Latn is only HTML and would confuse Java
    public static final String CATALAN = "ca";
    public static final String AFRIKAANS = "af";
    public static final String SWAHILI = "sw";
    public static final String ZULU = "zu";
    public static final String XHOSA = "xh";
    public static final String TELUGU = "te";
    public static final String KANNADA = "kn";
    public static final String MALAYALAM = "ml";
    public static final String MARATHI = "mr";
    public static final String GUJARATI = "gu";
    public static final String PUNJABI = "pa";
    public static final String PUNJABI_WESTERN = "pnb";  // aka pa_Arab
    public static final String MAORI = "mi";
    public static final String BURMESE = "my";
    public static final String PERSIAN = "fa";
    public static final String KHMER = "km";
    public static final String AMHARIC = "am";
    public static final String KAZAKH = "kk";
    public static final String SAMOAN = "sm";
    public static final String HAWAIIAN = "haw";
    public static final String HAITIAN_CREOLE = "ht";
    public static final String AZERBAIJANI = "az";
    public static final String GREENLANDIC = "kl";
    public static final String YIDDISH = "ji"; // Fix java screwup
    public static final String HMONG = "hmn";
    
    // platform languages
    public static final String SPANISH_MX = "es_MX"; //Spanish (Mexican), end-user lang prior to 190
    public static final String FRENCH_CA = "fr_CA";
    public static final String PORTUGUESE_BR = "pt_BR"; // Portuguese (Brazilian)
    public static final String PORTUGUESE_PT = "pt_PT"; // Portuguese (European)
    public static final String ENGLISH_US = "en_US";
    public static final String ENGLISH_GB = "en_GB";
    public static final String ENGLISH_AU = "en_AU";
    public static final String ENGLISH_MY = "en_MY";
    public static final String ENGLISH_IN = "en_IN";
    public static final String ENGLISH_PH = "en_PH";
    public static final String ENGLISH_CA = "en_CA";
    public static final String MOLDOVAN = "ro_MD";  // Note, this is just a variant, but called Moldovan for various reasons.
    public static final String MONTENEGRIN = "sh_ME";  // NOTE: this may end up as a "real" language some day.  Until then, it's staying as a serbo-croatian dialect
    public static final String GERMAN_AT = "de_AT";
    public static final String GERMAN_CH = "de_CH";
    public static final String ARABIC_DZ = "ar_DZ"; //Arabic Algerian
    public static final String ARABIC_BH = "ar_BH"; //Arabic Bahrain
    public static final String ARABIC_EG = "ar_EG"; //Arabic Egypt
    public static final String ARABIC_IQ = "ar_IQ"; //Arabic Iraq
    public static final String ARABIC_JO = "ar_JO"; //Arabic Jordan
    public static final String ARABIC_KW = "ar_KW"; //Arabic Kuwait
    public static final String ARABIC_LB = "ar_LB"; //Arabic Lebanon
    public static final String ARABIC_LY = "ar_LY"; //Arabic Libya
    public static final String ARABIC_MA = "ar_MA"; //Arabic Morocco
    public static final String ARABIC_OM = "ar_OM"; //Arabic Oman
    public static final String ARABIC_QA = "ar_QA"; //Arabic Quatar
    public static final String ARABIC_SA = "ar_SA"; //Arabic Saudi Arabia
    public static final String ARABIC_SD = "ar_SD"; //Arabic Sudan
    public static final String ARABIC_SY = "ar_SY"; //Arabic Syria
    public static final String ARABIC_TN = "ar_TN"; //Arabic Tunisia
    public static final String ARABIC_AE = "ar_AE"; //Arabic United Arab Emirates
    public static final String ARABIC_YE = "ar_YE"; //Arabic Yemen
    public static final String CHINESE_CN = "zh_CN"; //Chinese (Simplified) China
    public static final String CHINESE_TW = "zh_TW"; //Chinese (Traditional) Taiwan
    public static final String CHINESE_SG = "zh_SG"; //Chinese (Simplified) Singapore
    public static final String CHINESE_MY = "zh_MY"; //Chinese (Simplified) Malaysia
    public static final String CHINESE_HK = "zh_HK"; //Chinese (Traditional) Hong Kong
    public static final String ENGLISH_HK = "en_HK"; //English Hong Kong
    public static final String ENGLISH_IE = "en_IE"; //English Ireland
    public static final String ENGLISH_SG = "en_SG"; //English Singapore
    public static final String ENGLISH_ZA = "en_ZA"; //English South Africa
    public static final String FRENCH_BE = "fr_BE";  //French Belgium
    public static final String FRENCH_LU = "fr_LU";  //French Luxembourg
    public static final String FRENCH_CH = "fr_CH";  //French Switzerland
    public static final String GERMAN_LU = "de_LU";  //German Luxembourg
    public static final String ITALIAN_CH = "it_CH"; //Italian Switzerland
    public static final String SPANISH_AR = "es_AR"; //Spanish Argentina
    public static final String SPANISH_BO = "es_BO"; //Spanish Bolivia
    public static final String SPANISH_CL = "es_CL"; //Spanish Chile
    public static final String SPANISH_CO = "es_CO"; //Spanish Colombia
    public static final String SPANISH_CR = "es_CR"; //Spanish Costa Rica
    public static final String SPANISH_DO = "es_DO"; //Spanish Dominican Republic
    public static final String SPANISH_EC = "es_EC"; //Spanish Ecuador
    public static final String SPANISH_SV = "es_SV"; //Spanish El Salvador
    public static final String SPANISH_GT = "es_GT"; //Spanish Guatemala
    public static final String SPANISH_HN = "es_HN"; //Spanish Honduras
    public static final String SPANISH_NI = "es_NI"; //Spanish Nicaragua
    public static final String SPANISH_PA = "es_PA"; //Spanish Panama
    public static final String SPANISH_PY = "es_PY"; //Spanish Paraguay
    public static final String SPANISH_PE = "es_PE"; //Spanish Peru
    public static final String SPANISH_PR = "es_PR"; //Spanish Puerto Rico
    public static final String SPANISH_US = "es_US"; //Spanish United States
    public static final String SPANISH_UY = "es_UY"; //Spanish Uruguay
    public static final String SPANISH_VE = "es_VE"; //Spanish Venezuela
}