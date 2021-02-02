/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import static com.force.i18n.LanguageConstants.*;

import java.io.Serializable;
import java.util.Locale;

import com.force.i18n.commons.text.CaseFolder;
import com.force.i18n.commons.text.DeferredStringBuilder;

/**
 * Interface for a Human Spoken Language, into which an application should be localized.
 * The assumption is that an enum (or enum-like object) will be created to support the set
 * of supported languages.
 *
 * Usually there is a mapping from the language field of a locale to one of these languages,
 * but due to regional variation, a company may want to use a full locale to represent the human
 * language, especially for variants in Spanish, Portugese, German, and Chinese.
 *
 * For the override html language, see this page: http://www.w3.org/International/articles/bcp47/
 * @author stamm
 */
public interface HumanLanguage extends Serializable{

	/**
	 * @return the locale associated with this user language
	 */
	Locale getLocale();

	/**
	 * @return the string for the locale associated with this user language (convenience method)
	 */
	String getLocaleString();

	/**
	 * @return the text direction of the language (right now just RightToLeft vs LeftToRight)
	 */
	TextDirection getDirection();

	/**
	 * @return the "override" language for historical reasons.  Generally, this means that the
	 * locale for the language was wrong (dutch), or Java prevents it from being correct (iw vs he)
	 */
	String getOverrideLanguage();

	/**
	 * @return the language code to use for HTTP communication (see RFC 1766)
	 */
	String getHttpLanguageCode();

	/**
	 * @return the path, relative to a "base" directory of labels where the labels
	 * for this particular language will be found.
	 * This handles the special cases for Dutch, English, and Simplified Chinese
	 */
	String getDefaultLabelDirectoryPath();

	/**
	 * @return the default language encoding charset to use for the language
	 */
	String getDefaultFileEncoding();

	/**
	 * Return the user email encoding, which differs from the email encoding for
	 * Thai and Korean for reasons I don't understand.
	 * TODO: Why is this different from alternate?  Really.  What the hell
	 * @return the default user email encoding charset to use for the language
	 */
	String getDefaultUserEmailEncoding();


    /**
     * @return the email encoding to use when sending out emails in the given language
     * Same as file encoding, except it wants to use UTF-8 whenever possible, unlike
     * user emails.
     */
    String getSystemEmailEncoding();

    /**
     * @return the language to use as the fallback language for translations
     * The difference between this and fallback language is what the "fallback"
     * for translations.  So French doesn't fall back to English, because that would be wrong,
     * just the fallback for _XX languages.  This is only used for customer translations.
     *
     * Summary: Use this only for country language variants, not for anything else.
     */
	HumanLanguage getTranslationFallbackLanguage();

	/**
	 * NOTE: You must ensure that the fallback language returned has a lower ordinal than this language in the
	 * enum (i.e. the extension language needs to appear afterwards if it's an enum).  Note, this is usually the same as
	 * translationfallbackLanguage, and only differs for mutually intelligible languages, like Malay and Indonesian
	 * @return the language to use as the fallback language for labels that are not available in this language
	 */
	HumanLanguage getFallbackLanguage();


	/**
	 * @return whether this language is used for linguistic testing.  Esperanto is used in
	 * grammaticus for this.
	 */
	default boolean isTestOnlyLanguage() {
		return false;
	}

	/**
	 * @return whether this language generally has translated values for the applications,
	 * as opposed to being a country/dialect variant of another language.  This doesn't stop
	 * an application from translating, but will let you omit the large number of english and
	 * arabic variants that will fallback to another language.
	 */
	default boolean isTranslatedLanguage() {
		return true;
	}

    /**
     * @return whether use of fallback strings in this language should be considered a problem.
	 * Note: this should only applies to non-fully translated languages that don't have a fallback
     * to a "normal" language.
     */
	boolean shouldLogFallbackStrings();

	/**
	 * @return the key for the label in the LanguageName and TranslatedLabelName label section.
	 * This differs only to handle the en_US and nl_NL historical anomalies
	 */
	String getLabelKey();

	/**
	 * @return whether turkish locale specific case folding should be used to handle
	 * the dotted/dotless i problem.
	 */
	boolean hasTurkicCaseFolding();

	/**
	 * Return the value with the "case folded" using the unicode algorithm
	 * for lowercase based on the current user language
	 * @param input the string to be case folded (i.e. lowercased)
	 * @return the string with converted to lowercase
	 * @see CaseFolder#toFoldedCase(String, boolean)
	 */
	String toFoldedCase(String input);

    // Comparison and hashing

    /**
     * Note: this library assumes that the set of human languages is finite and
     * established at the beginning of the application lifecycle (like an Enum).
     * So equality comparisons in this library may be made using ==.
     *
     * @param o the reference object with which to compare.
     * @return {@code true} if this object is the same as the {@code o} argument; {@code false} otherwise.
     */
    @Override
	boolean equals(Object o);

    /**
     * @return an integer that represents this language in the total set of human values.
     * The ordering of languages should be by ordinal.
     *
     * The "List" returned by the {@link LanguageProvider#getAll()} requires the
     * ordinal is the index in that list.
     *
     * Note: this does assume that an Enum implements HumanLanguage.  I would recommend this.
     * In any case, t
     */
    int ordinal();

    /**
     * Helper methods for implementations of HumanLanguage.  In JDK8, this would be the
     * implementation for the interface, but alas, we once supported JDK7.
     */
    public static class Helper {
    	public static HumanLanguage get(Locale locale) {
    		return LanguageProviderFactory.get().getLanguage(locale);
    	}

    	public static HumanLanguage get(String localeString) {
    		return LanguageProviderFactory.get().getLanguage(localeString);
    	}

    	/**
    	 * @return {@code true} if the language is simplified chinese (which is
    	 * signified by the country, and not the language)
    	 * @param language the language to test
    	 */
    	public static boolean isSimplifiedChinese(HumanLanguage language) {
    		switch (language.getLocaleString()) {
    		case CHINESE_CN:
    		case CHINESE_SG:
    		case CHINESE_MY:
    			return true;
    	    default:
    		}
    		return false;
    	}

    	/**
    	 * @param language the language to test
    	 * @return {@code true} if the language needs dotted i case folding
    	 * (turkish)
    	 */
        public static boolean hasTurkicCaseFolding(HumanLanguage language) {
        	String langStr = language.getLocale().getLanguage();
            return langStr.equals(LanguageConstants.TURKISH)
            		|| langStr.equals(LanguageConstants.AZERBAIJANI);
        }


        /**
    	 * @param language the language to test
         * @return the default language encoding charset to use for the language
         */
        public static String getDefaultFileEncoding(HumanLanguage language) {
            switch (language.getLocale().getLanguage()) {
            // Don't change the Japanese encoding unless you also
            // change PlainFormatter.header() and other weirdness in the code
            // that depends on the Japanese file encoding being "MS932"
            case JAPANESE: return "MS932";
            case KOREAN: return "EUC-KR";
            case CHINESE: return isSimplifiedChinese(language) ? "GB2312" : "Big5";
	        case THAI: return "TIS-620";
            case UKRAINIAN:
            case BULGARIAN:
            case SERBIAN_CYRILLIC:
            case SERBIAN_LATIN:
            case ARMENIAN: case HINDI:
            case SLOVAK:  case HEBREW: case ARABIC: case URDU: case GEORGIAN:
            case BOSNIAN: case MOLDOVAN: case SLOVENE: case MACEDONIAN: case CROATIAN:
            case LATVIAN: case LITHUANIAN: case MALTESE:
            case RUSSIAN:
            case BENGALI:
            case KHMER:
            case TAMIL: return "UTF-8";
            default: return "ISO-8859-1";
            }
        }

        /**
         * Method used to make sure that file names are encoded specially for japanese users on AP0
    	 * @param language the language to test
         * @param forWindows is the file being downloaded on a windows device, where MS932 should be used for japanese
         * @return the encoding used for the file name in the servlet
         */
        public static String getFileNameServletEncoding(HumanLanguage language, boolean forWindows) {
            switch (language.getLocale().getLanguage()) {
            case JAPANESE: return forWindows ? "MS932" : "Shift_JIS";
            default: return "UTF-8";
            }
        }


        /**
         * Return the user email encoding, which differs from the email encoding for
         * Thai and Korean for reasons I don't understand.
         * TODO: Why is this different from alternate?  Really.  What the hell
    	 * @param language the language to test
         * @return the default user email encoding charset to use for the language
         */
        public static String getDefaultUserEmailEncoding(HumanLanguage language) {
            switch (language.getLocale().getLanguage()) {
            case JAPANESE: return "ISO-2022-JP";
            case KOREAN: return "ks_c_5601-1987";
            case CHINESE: return isSimplifiedChinese(language) ? "GB2312" : "Big5";
            case ARMENIAN: case HINDI:
            case UKRAINIAN:
            case BULGARIAN:
            case SERBIAN_CYRILLIC:
            case SERBIAN_LATIN:
            case SLOVAK:
            case HEBREW:
            case ARABIC:
            case URDU:
            case GEORGIAN:
            case BOSNIAN: case MOLDOVAN: case SLOVENE: case MACEDONIAN: case CROATIAN:
            case LATVIAN: case LITHUANIAN: case MALTESE: case MONTENEGRIN:
            case RUSSIAN:
            case BENGALI:
            case TAMIL:
            case KHMER: return "UTF-8";
            default: return "ISO-8859-1";
            }
        }

        /**
    	 * @param language the language to test
         * @return the email encoding to use when sending out emails in the given language
         * Same as file encoding, except it wants to use UTF-8 whenever possible, unlike
         * use emails. This is the old behavior from the old localizer
         */
        public static String getSystemEmailEncoding(HumanLanguage language) {
            switch (language.getLocale().getLanguage()) {
            case JAPANESE: return "ISO-2022-JP";
            case KOREAN: return "EUC-KR";
            case CHINESE: return isSimplifiedChinese(language) ? "GB2312" : "Big5";
            case THAI: return "TIS-620";
            default: return "UTF-8";
            }
        }

        /**
         * Return the value with the "case folded" using the unicode algorithm
         * for lowercase based on the current user language
    	 * @param language the language to test
         * @param input the string to be case folded (i.e. lowercased)
         * @return the string with converted to lowercase
         * @see CaseFolder#toFoldedCase(String, boolean)
         */
        public static String toFoldedCase(HumanLanguage language, String input) {
            if (language.getLocale().getLanguage().equals(GREEK)) {
                // TODO: This should be removed when we move CaseFolder.java into i18n
                // The grammatically correct handling of case folding (to lowercase)
                // Greek sigma differs from the Unicode case folding mapping.
                // There are two valid forms of lowercase sigma, σ and ς, and should be
                // left as is. Uppercase sigma (Σ) maps to either form depending on where
                // in the word the sigma is used, but because sfdcnames.xml and sfdcadjectives.xml
                // only capitalizes the first letter of a word, uppercase sigma should always fold
                // to lowercase sigma
                // http://en.wikipedia.org/wiki/Sigma, http://www.tlg.uci.edu/~opoudjis/dist/sigma.html
                if (input == null)
                    return null;
                DeferredStringBuilder buf = new DeferredStringBuilder(input);

                for (int i = 0; i < input.length(); ++i) {
                    char c = input.charAt(i);
                    char[] remap = null;
                    if (c != '\u03C2' && c != '\u03C3') {  // ς or σ
                        // don't fold lowercase sigma
                        remap = CaseFolder.toFoldedCase(c, false);
                    }

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

            return CaseFolder.toFoldedCase(input, language.hasTurkicCaseFolding());
        }
        
	    /**
	     * Helper method for determining which language to use for "variant" languages with some opinions
	     * when there might be a conflict, as in Simplified vs Traditional Chinese.
	     * 
	     * @return the language to use as the fallback language for translations
	     * The difference between this and fallback language is what the "fallback"
	     * for translations.  So French doesn't fall back to English, because that would be wrong,
	     * just the fallback for _XX languages.  This is only used for customer translations.
	     *
	     * Summary: Use this only for country language variants, not for anything else.
	     */
		public static Locale getTranslationFallbackLanguageLocale(Locale locale) {
			switch (locale.getLanguage()) {
			case LanguageConstants.HAWAIIAN:
				return Locale.US;
			case LanguageConstants.HAITIAN_CREOLE:
				return Locale.FRENCH;
			default:
				String country = locale.getCountry();
				if (country.length() > 0) {
					switch (locale.getLanguage()) {
					case LanguageConstants.CHINESE:
						switch (country) {
						case "TW":
						case "CN":
							return null;
						case "HK":
							return Locale.TRADITIONAL_CHINESE;
						default:
							return Locale.SIMPLIFIED_CHINESE;
						}
					case LanguageConstants.ENGLISH:
						switch (country) {
						case "US":
							return null; // English peculiarity, where en_US is for english. 
						case "GB":
						case "CA":
						case "IL":
							return Locale.US;
						default:
							return Locale.UK;
						}
				    default:
				    	return new Locale(locale.getLanguage());
					}
				}

				return null;
			}
	    }
    }
}