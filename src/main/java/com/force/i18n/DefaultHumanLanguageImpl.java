/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default Human Language Provider that's used if there's no other answer
 *
 * This is an ok default implementation
 * @author stamm
 */
enum DefaultHumanLanguageImpl implements HumanLanguage {
    // standard languages
    ENGLISH(Locale.US),  // It's a peculiarity that we use en_US as the default for English instead of en.  Complain if need be
    GERMAN(Locale.GERMAN),
    SPANISH(new Locale.Builder().setLanguage("es").build()),
    FRENCH(Locale.FRENCH),
    ITALIAN(Locale.ITALIAN),
    JAPANESE(Locale.JAPANESE),
    SWEDISH(new Locale.Builder().setLanguage("sv").build()),
    KOREAN(Locale.KOREAN),
    CHINESE_TRAD(Locale.TRADITIONAL_CHINESE), // Chinese(Traditional)  (this order matters)
    CHINESE_SIMP(Locale.SIMPLIFIED_CHINESE), // Chinese(Simplified)
    PORTUGUESE_BR(new Locale.Builder().setLanguage("pt").setRegion("BR").build()), // Portuguese (Brazilian)
    DUTCH(new Locale.Builder().setLanguage("nl").setRegion("NL").build(), LanguageType.STANDARD, "nl"),  // This is historical, should always have been "nl"
    DANISH(new Locale.Builder().setLanguage("da").build()),
    THAI(new Locale.Builder().setLanguage("th").build()),
    FINNISH(new Locale.Builder().setLanguage("fi").build()),
    RUSSIAN(new Locale.Builder().setLanguage("ru").build()),
    SPANISH_MX(new Locale.Builder().setLanguage("es").setRegion("MX").build()), //Spanish (Mexican), end-user lang prior to 190
    NORWEGIAN(new Locale.Builder().setLanguage("no").build(), LanguageType.STANDARD, 168.0),  // Technically, this should be "nb", but that would be confusing to java and HTML

    // end-user languages
    HUNGARIAN(new Locale.Builder().setLanguage("hu").build(), LanguageType.END_USER),
    POLISH(new Locale.Builder().setLanguage("pl").build(), LanguageType.END_USER),
    CZECH(new Locale.Builder().setLanguage("cs").build(), LanguageType.END_USER),
    TURKISH(new Locale.Builder().setLanguage("tr").build(), LanguageType.END_USER),
    INDONESIAN(new Locale.Builder().setLanguage("in").build(), LanguageType.END_USER, "id"),  // Fix java screwup
    ROMANIAN(new Locale.Builder().setLanguage("ro").build(), LanguageType.END_USER),
    VIETNAMESE(new Locale.Builder().setLanguage("vi").build(), LanguageType.END_USER),
    UKRAINIAN(new Locale.Builder().setLanguage("uk").build(), LanguageType.END_USER),
    HEBREW(new Locale.Builder().setLanguage("iw").build(), LanguageType.END_USER, "he"), // Fix java screwup
    GREEK(new Locale.Builder().setLanguage("el").build(), LanguageType.END_USER),
    BULGARIAN(new Locale.Builder().setLanguage("bg").build(), LanguageType.END_USER),
    ENGLISH_GB(Locale.UK, LanguageType.END_USER, 168.0),
    ARABIC(new Locale.Builder().setLanguage("ar").build(), LanguageType.END_USER, 168.0),
    SLOVAK(new Locale.Builder().setLanguage("sk").build(), LanguageType.END_USER, 168.0),
    PORTUGUESE_PT(new Locale.Builder().setLanguage("pt").setRegion("PT").build(), LanguageType.END_USER, 172.0), // Portuguese (European)
    CROATIAN(new Locale.Builder().setLanguage("hr").build(), LanguageType.END_USER, 170.0),
    SLOVENE(new Locale.Builder().setLanguage("sl").build(), LanguageType.END_USER, 170.0),

    // platform languages
    GEORGIAN(new Locale.Builder().setLanguage("ka").build(), LanguageType.PLATFORM, 168.0),
    SERBIAN_CYRILLIC(new Locale.Builder().setLanguage("sr").build(), LanguageType.PLATFORM, 168.0),  // http://tlt.its.psu.edu/suggestions/international/bylanguage/serbocroatian.html
    SERBIAN_LATIN(new Locale.Builder().setLanguage("sh").build(), LanguageType.PLATFORM, "sr-Latn", 168.0),  // sh is deprecated, but using sr-Latn is only HTML and would confuse Java
    MOLDOVAN(new Locale.Builder().setLanguage("ro").setRegion("MD").build(), LanguageType.PLATFORM, 170.0),  // Note, this is just a variant, but called Moldovan for various reasons.
    BOSNIAN(new Locale.Builder().setLanguage("bs").build(), LanguageType.PLATFORM, 170.0),
    MACEDONIAN(new Locale.Builder().setLanguage("mk").build(), LanguageType.PLATFORM, 170.0),
    LATVIAN(new Locale.Builder().setLanguage("lv").build(), LanguageType.PLATFORM, 172.0),
    LITHUANIAN(new Locale.Builder().setLanguage("lt").build(), LanguageType.PLATFORM, 172.0),
    ESTONIAN(new Locale.Builder().setLanguage("et").build(), LanguageType.PLATFORM, 172.0),
    ALBANIAN(new Locale.Builder().setLanguage("sq").build(), LanguageType.PLATFORM, 172.0),
    MONTENEGRIN(new Locale.Builder().setLanguage("sh").setRegion("ME").build(), LanguageType.PLATFORM, 172.0),  // NOTE: this may end up as a "real" language some day.  Until then, it's staying as a serbo-croatian dialect
    MALTESE(new Locale.Builder().setLanguage("mt").build(), LanguageType.PLATFORM, 172.0),
    IRISH(new Locale.Builder().setLanguage("ga").build(), LanguageType.PLATFORM, 172.0),
    BASQUE(new Locale.Builder().setLanguage("eu").build(), LanguageType.PLATFORM, 172.0),
    WELSH(new Locale.Builder().setLanguage("cy").build(), LanguageType.PLATFORM, 172.0),
    ICELANDIC(new Locale.Builder().setLanguage("is").build(), LanguageType.PLATFORM, 172.0),

    MALAY(new Locale.Builder().setLanguage("ms").build(), LanguageType.PLATFORM, 172.0),
    TAGALOG(new Locale.Builder().setLanguage("tl").build(), LanguageType.PLATFORM, 172.0),

    LUXEMBOURGISH(new Locale.Builder().setLanguage("lb").build(), LanguageType.PLATFORM, 174.0),
    ROMANSH(new Locale.Builder().setLanguage("rm").build(), LanguageType.PLATFORM, 174.0),
    ARMENIAN(new Locale.Builder().setLanguage("hy").build(), LanguageType.PLATFORM, 174.0),
    HINDI(new Locale.Builder().setLanguage("hi").build(), LanguageType.PLATFORM, 174.0),
    URDU(new Locale.Builder().setLanguage("ur").build(), LanguageType.PLATFORM, 174.0),

    BENGALI(new Locale.Builder().setLanguage("bn").build(), LanguageType.PLATFORM, 190.0),
    TAMIL(new Locale.Builder().setLanguage("ta").build(), LanguageType.PLATFORM, 190.0),

    AFRIKAANS(new Locale.Builder().setLanguage("af").build(), LanguageType.PLATFORM, 220.0),
    SWAHILI(new Locale.Builder().setLanguage("sw").build(), LanguageType.PLATFORM, 220.0),
    ZULU(new Locale.Builder().setLanguage("zu").build(), LanguageType.PLATFORM, 220.0),
    XHOSA(new Locale.Builder().setLanguage("xh").build(), LanguageType.PLATFORM, 220.0),

    TELUGU(new Locale.Builder().setLanguage("te").build(), LanguageType.PLATFORM, 220.0),
    MALAYALAM(new Locale.Builder().setLanguage("ml").build(), LanguageType.PLATFORM, 220.0),
    KANNADA(new Locale.Builder().setLanguage("kn").build(), LanguageType.PLATFORM, 220.0),
    MARATHI(new Locale.Builder().setLanguage("mr").build(), LanguageType.PLATFORM, 220.0),
    GUJARATI(new Locale.Builder().setLanguage("gu").build(), LanguageType.PLATFORM, 220.0),
    PUNJABI(new Locale.Builder().setLanguage("pa").build(), LanguageType.PLATFORM, 238.0),

    MAORI(new Locale.Builder().setLanguage("mi").build(), LanguageType.PLATFORM, 220.0),
    BURMESE(new Locale.Builder().setLanguage("my").build(), LanguageType.PLATFORM, 220.0),
    PERSIAN(new Locale.Builder().setLanguage("fa").build(), LanguageType.PLATFORM, 224.0),
    KHMER(new Locale.Builder().setLanguage("km").build(), LanguageType.PLATFORM, 228.0),
    AMHARIC(new Locale.Builder().setLanguage("am").build(), LanguageType.PLATFORM, 230.0),

    KAZAKH(new Locale.Builder().setLanguage("kk").build(), LanguageType.PLATFORM, 232.0),
    HAITIAN_CREOLE(new Locale.Builder().setLanguage("ht").build(), LanguageType.PLATFORM, 232.0),
    SAMOAN(new Locale.Builder().setLanguage("sm").build(), LanguageType.PLATFORM, 232.0),
    HAWAIIAN(new Locale.Builder().setLanguage("haw").build(), LanguageType.PLATFORM, 232.0),

    CATALAN(new Locale.Builder().setLanguage("ca").build(), LanguageType.PLATFORM, 210.0), // Catalan

    GREENLANDIC(new Locale.Builder().setLanguage("kl").build(), LanguageType.PLATFORM, 234.0), //Greenlandic -- no grammar support in 234
    YIDDISH(new Locale.Builder().setLanguage("ji").build(), LanguageType.PLATFORM, "yi", 236.0),  // Java screwup with iso code.
    HMONG(new Locale.Builder().setLanguage("hmn").build(), LanguageType.PLATFORM, 238.0),

    CHUJ(new Locale.Builder().setLanguage("cac").build(), LanguageType.PLATFORM, 248.0), //Chuj 
    KICHE(new Locale.Builder().setLanguage("quc").build(), LanguageType.PLATFORM, 248.0), //Kiche
    KAQCHIKEL(new Locale.Builder().setLanguage("cak").build(), LanguageType.PLATFORM, 248.0), //Kaqchikel

    // Sample use of variants for testing
    ARABIC_DZ(new Locale.Builder().setLanguage("ar").setRegion("DZ").build(), LanguageType.PLATFORM, 194.0), //Arabic Algerian
    ENGLISH_AU(new Locale.Builder().setLanguage("en").setRegion("AU").build(), LanguageType.PLATFORM, 168.0),
    ENGLISH_IN(new Locale.Builder().setLanguage("en").setRegion("IN").build(), LanguageType.PLATFORM, 168.0),
    ENGLISH_PH(new Locale.Builder().setLanguage("en").setRegion("PH").build(), LanguageType.PLATFORM, 168.0),
    ENGLISH_CA(new Locale.Builder().setLanguage("en").setRegion("CA").build(), LanguageType.PLATFORM, 168.0),
    ENGLISH_HK(new Locale.Builder().setLanguage("en").setRegion("HK").build(), LanguageType.PLATFORM, 194.0), //English Hong Kong
    ENGLISH_IE(new Locale.Builder().setLanguage("en").setRegion("IE").build(), LanguageType.PLATFORM, 194.0), //English Ireland
    ENGLISH_SG(new Locale.Builder().setLanguage("en").setRegion("SG").build(), LanguageType.PLATFORM, 194.0), //English Singapore
    ENGLISH_ZA(new Locale.Builder().setLanguage("en").setRegion("ZA").build(), LanguageType.PLATFORM, 194.0), //English South Africa
    FRENCH_CA(Locale.CANADA_FRENCH, LanguageType.PLATFORM, 168.0),
    GERMAN_AT(new Locale.Builder().setLanguage("de").setRegion("AT").build(), LanguageType.PLATFORM, 190.0),
    GERMAN_CH(new Locale.Builder().setLanguage("de").setRegion("CH").build(), LanguageType.PLATFORM, 190.0),
    FRENCH_CH(new Locale.Builder().setLanguage("fr").setRegion("CH").build(), LanguageType.PLATFORM, 194.0),  //French Switzerland
    ITALIAN_CH(new Locale.Builder().setLanguage("it").setRegion("CH").build(), LanguageType.PLATFORM, 194.0), //Italian Switzerland
    SPANISH_AR(new Locale.Builder().setLanguage("es").setRegion("AR").build(), LanguageType.PLATFORM, 194.0), //Spanish Argentina
    RUSSIAN_IL(new Locale.Builder().setLanguage("ru").setRegion("IL").build(), LanguageType.PLATFORM, 232.0), //Russian Israel
    CHINESE_SG(new Locale.Builder().setLanguage("zh").setRegion("SG").build(), LanguageType.PLATFORM, 194.0), //Chinese (Simplified) Singapore
    CHINESE_HK(new Locale.Builder().setLanguage("zh").setRegion("HK").build(), LanguageType.PLATFORM, 194.0), //Chinese (Traditional) Hong Kong 

    ESPERANTO(new Locale.Builder().setLanguage("eo").build(), LanguageType.HIDDEN, 172.0),  // Esperanto is our "fake" language, always leave it last
    ENGLISH_IL(new Locale.Builder().setLanguage("en").setRegion("IL").build(), LanguageType.HIDDEN, 214.0),  // en_IL for testing of right-to-left with latin characters
    ;

    // NOTE: languages are organized according to type and then in chronological order of when they're added within the type. This is so
    // the Language Settings UI and the places where the language picklist is displayed unfiltered (Rename Tabs and Labels, TWB Settings, package edit page)
    // are consistent.

    // Constructor for private enum to handle end-user language
    public enum LanguageType {
        // The order here matters; see supportsType
        STANDARD,
        END_USER,           // language enum including end-user languages like hungarian
        PLATFORM,           // language enum with everything for customers
        HIDDEN;             // language enum including pseudo languages (zxx)

        /**
         * Return the count of languages of the given type.
         */
        public int getCount() {
            return getLangProvider().getTypeCount(this);
        }
    }

    private final Locale locale;
    private final LanguageType type;
    private final TextDirection direction;
    private final String overrideLanguage;  // Override language is needed because the locale constructor munges stuff
    private final String htmlLanguage;  // The language that should be returned for Content-Language
    private final Double minVersion;  // The version when the user language was added

    private DefaultHumanLanguageImpl(Locale locale) {
        this(locale, LanguageType.STANDARD);
    }

    private DefaultHumanLanguageImpl(Locale locale, LanguageType type) {
        this(locale, type, null, null);
    }

    private DefaultHumanLanguageImpl(Locale locale, LanguageType type, Double minVersion) {
        this(locale, type, null, minVersion);
    }

    private DefaultHumanLanguageImpl(Locale locale, LanguageType type, String overrideLanguage) {
        this(locale, type, overrideLanguage, null);
    }

    private DefaultHumanLanguageImpl(Locale locale, LanguageType type, String overrideLanguage, Double minVersion) {
        this.locale = locale;
        this.type = type;
        this.direction = TextDirection.getDirection(locale);
        this.overrideLanguage = handleOverrideLanguage(locale, overrideLanguage);
        this.htmlLanguage = getHtmlLanguage(locale, overrideLanguage);
        this.minVersion = minVersion;
    }

    /**
     * @return the locale associated with this user language
     */
    @Override
	public Locale getLocale() {
        return this.locale;
    }

    /**
     * @return the string for the locale associated with this user language (convenience method)
     */
    @Override
	public String getLocaleString() {
        return this.locale.toString();
    }

    /**
     * @return the string for the language that should be used to store in the Database
     */
    public String getDbValue() {
        return this.locale.toString();
    }

    /**
     * @return the text direction of the language (right now just RightToLeft vs LeftToRight)
     */
	@Override
    @SuppressWarnings("deprecation")
	public TextDirection getDirection() {
        // invertIfNotNormalDirection checks if we have set a debugging "reverse" flag - to allow
        // a language like English to be shown as an RTL language.
        return TextDirection.invertIfNotNormalDirection(this.direction);
    }

    /**
     * @return the "override" language for historical reasons.  Generally, this means that the
     * locale for the language was wrong (dutch), or Java prevents it from being correct because
     * it used the 1988 ISO 639 spec in 1995.
     */
    @Override
	public String getOverrideLanguage() {
        return this.overrideLanguage;
    }

    /**
     * @return the language code to use for HTTP communication (see RFC 1766)
     */
    @Override
	public String getHttpLanguageCode() {
        return this.htmlLanguage;
    }

    /**
     * @return the version where this language was added
     * Currently unused: will be included at the same time as locales are made version aware.
     */
    public Double getMinVersion() {
        return this.minVersion;
    }

    /**
     * @return whether this language is supported for end users only and doesn't have full translation files
     */
    public final LanguageType getLanguageType() {return this.type;}

    /**
     * @param type the language type to compare
     * @return whether this language should be displayed as part of the language type given.
     * e.g. English will always return true, but Hungarian will return only if type is END_USER or RIGHT_TO_LEFT
     */
    public boolean supportsType(LanguageType type) {
        return getLanguageType().ordinal() <= type.ordinal();
    }

    /**
     * @return the path, relative to a "base" directory of labels where the labels
     * for this particular language will be found.
     * This handles the special cases for Dutch, English, and Simplified Chinese
     */
    @Override
	public String getDefaultLabelDirectoryPath() {
        switch (this) {
        case ENGLISH: return "";
        case DUTCH: return "nl";
        case CHINESE_SIMP: return "zh";
        
        // By default, use the old incorrect directories.
        case HEBREW: return LanguageConstants.HEBREW;
        case INDONESIAN: return LanguageConstants.INDONESIAN;
        case YIDDISH: return LanguageConstants.YIDDISH;

        default:
        	return getLocale().toString().replace('_', '/');
        }
    }

    /**
     * @return the default language encoding charset to use for the language
     */
    @Override
	public String getDefaultFileEncoding() {
    	return Helper.getDefaultFileEncoding(this);
    }

    /**
     * Return the user email encoding, which differs from the email encoding for
     * Thai and Korean for reasons I don't understand.
     * TODO: Why is this different from alternate?
     * @return the default user email encoding charset to use for the language
     */
    @Override
	public String getDefaultUserEmailEncoding() {
    	return Helper.getDefaultUserEmailEncoding(this);
    }

    /**
     * @return the email encoding to use when sending out emails in the given language
     * Same as file encoding, except it wants to use UTF-8 whenever possible, unlike
     * use emails. This is the old behavior from the old localizer
     */
    @Override
	public String getSystemEmailEncoding() {
    	return Helper.getSystemEmailEncoding(this);
    }

    /**
     * NOTE: You must ensure that the fallback language returned appears before this language in the
     * enum (i.e. the extension language needs to appear afterwards).  Note, this is usually the same as
     * translationfallbackLanguage, and only differs for mutually intelligible languages, like Malay and Indonesian
     * @return the language to use as the fallback language for labels that are not available in this language
     */
    @Override
	public DefaultHumanLanguageImpl getFallbackLanguage() {
        switch (this) {
        case ENGLISH:      return null;  // English has no fallback
        case MALAY:        return INDONESIAN;  // Indonesian is a dialect of Malay, but shouldn't be used for translations?
        default:
            DefaultHumanLanguageImpl translationFallback = getTranslationFallbackLanguage();
            return translationFallback != null ? translationFallback : DefaultHumanLanguageImpl.ENGLISH;
        }
    }

    /**
     * @return the language to use as the fallback language for translations
     * The difference between this and fallback language is what the "fallback"
     * for translations.  So French doesn't fall back to English, because that would be wrong,
     * just the fallback for _XX languages.  This is only used for customer translations.
     *
     * Summary: Use this only for country language variants, not for anything else.
     */
    @Override
	public DefaultHumanLanguageImpl getTranslationFallbackLanguage() {
    	return getLangProvider().getTranslationFallbackLanguage(this);
    }

    /**
     * @return the key for the label in the LanguageName and TranslatedLabelName label section.
     * This differs only to handle the en_US and nl_NL historical anomalies
     */
    @Override
	public final String getLabelKey() {
        switch (this) {
        case ENGLISH:
            return "en";
        case DUTCH:
            return "nl";
        default:
            return getLocaleString();
        }
    }

    /**
     * @return whether use of fallback strings in this language should be considered a problem.  This only applies to end user and R2L languages that don't have a fallback
     * to a "normal" language.  This means fallbacks in Mexican Spanish and British English will not be logged, since they're not a huge deal.
     */
    @Override
    public final boolean shouldLogFallbackStrings() {
        // Only log
        return getLanguageType() != DefaultHumanLanguageImpl.LanguageType.STANDARD && getLanguageType() != DefaultHumanLanguageImpl.LanguageType.PLATFORM && getTranslationFallbackLanguage() == null;
    }

    /**
     * @return whether turkish locale specific case folding should be used to handle
     * the dotted/dotless i problem.
     */
    @Override
	public final boolean hasTurkicCaseFolding() {
        return this == TURKISH;
    }

    /**
     * Return the value with the "case folded" using the unicode algorithm
     * for lowercase based on the current user language
     * @param input the string to be case folded (i.e. lowercased)
     * @return the string with converted to lowercase
     * @see com.force.i18n.commons.text.CaseFolder#toFoldedCase(String, boolean)
     */
    @Override
	public String toFoldedCase(String input) {
    	return Helper.toFoldedCase(this, input);
    }


	@Override
	public boolean isTestOnlyLanguage() {
		return getLanguageType() == LanguageType.HIDDEN;
	}

	@Override
	public boolean isTranslatedLanguage() {
		switch (getLanguageType()) {
			case STANDARD:
			case END_USER:
				return true;
		    default:
		}
		return false;
	}

	/**
     * Helper functions for avoiding Java generics garbage while creating an enum map.
     */
    public static <V> EnumMap<DefaultHumanLanguageImpl,V> newEnumMap() {
        return new EnumMap<>(DefaultHumanLanguageImpl.class);
    }

    private static final AtomicReference<DefaultHumanLanguageImpl> DEFAULT_LANGUAGE = new AtomicReference<>();

    public static DefaultHumanLanguageImpl getDefaultLanguage() {
    	DefaultHumanLanguageImpl stored = DEFAULT_LANGUAGE.get();
        if (stored != null) return stored;
        return DefaultHumanLanguageImpl.ENGLISH;  // Default to english; sorry
    }

    public void setDefaultLanguage(DefaultHumanLanguageImpl defaultLanguage) {
    	DEFAULT_LANGUAGE.set(defaultLanguage);
    }
    
    static DefaultHumanLanguageImplProvider getLangProvider() {
    	return (DefaultHumanLanguageImplProvider)LanguageProviderFactory.get().getProvider();
    }

    /**
     * Return a list of DefaultHumanLanguageImpls that are of the specified LanguageType
     * NOTE: Only standard, end user, and platform only languages are supported
     * Other language types will cause null to be returned
     */
    public static List<DefaultHumanLanguageImpl> getLanguageList(LanguageType type) {
    	return getLangProvider().getLanguageList(type);
    }

    /**
     * Returns a list of all DefaultHumanLanguageImpls that a user would have access to
     * given the specified language type
     */
    public static List<DefaultHumanLanguageImpl> getAllLanguagesList(LanguageType type) {
    	return getLangProvider().getAllLanguagesList(type);
    }

    /**
     * @return string value of enum to be stored in search index.
     * Note that changing this will require either changes on query side or re-indexing
     * since this value is stored in lucene index
     */
    public String searchValue() {
        return name().toLowerCase();
    }

    static String getHtmlLanguage(Locale locale, String overrideLanguage) {
        if (overrideLanguage != null) return overrideLanguage;
        // zh_CN -> zh-CN, which is ok, but not really what's intended
        if ("zh".equals(locale.getLanguage())) {
            String country = locale.getCountry();
            switch (country) {
            case "TW":
            case "HK":
                return "zh-Hant-"+country;
            case "SG":
            case "CN":
                return "zh-Hans-"+country;
            default:
            }
        }
        return locale.toLanguageTag();
    }
    
    /** 
     * In JDK 17, the language locale for Yiddish, Hebrew, and Indonesian were corrected to be
     * the valid ISO Code, but not everything has adopted 17 yet, so support the old names.
     * @param locale the JDK provided locale
     * @param overrideLanguage the override language
     * @return the override language that should be used, usually the one provided, unless it is the
     * same as the locale language.
     */
    static String handleOverrideLanguage(Locale locale, String overrideLanguage) {
    	if (overrideLanguage == null) {
    		return null;
    	}
    	if (overrideLanguage.equals(locale.toString())) {
    		switch (overrideLanguage) {
    		case LanguageConstants.HEBREW_ISO: return LanguageConstants.HEBREW;
    		case LanguageConstants.YIDDISH_ISO: return LanguageConstants.YIDDISH;
    		case LanguageConstants.INDONESIAN_ISO: return LanguageConstants.INDONESIAN;
    		default:
    			assert false : "An override language isn't required";
    		}
    	}
    	return overrideLanguage;
    }
    
 	
    /**
     * Helper method for determining which language to use for "variant" languages with some opinions
     * when there might be a conflict, as in Simplified vs Traditional Chines.
     * 
     * @return the language to use as the fallback language for translations
     * The difference between this and fallback language is what the "fallback"
     * for translations.  So French doesn't fall back to English, because that would be wrong,
     * just the fallback for _XX languages.  This is only used for customer translations.
     *
     * Summary: Use this only for country language variants, not for anything else.
     */
	static Locale getTranslationFallbackLanguageLocale(Locale locale) {
		String country = locale.getCountry();
		if (country.length() > 0) {
			switch (locale.getLanguage()) {
			case "pt":
				switch (country) {
				case "BR":
					return null;
				default:
					return new Locale.Builder().setLanguage("pt").setRegion("BR").build();  // No "pt" language
				}
			case "nl":
				switch (country) {
				case "NL":
					return null;
				default:
					return new Locale.Builder().setLanguage("nl").setRegion("NL").build();  // No "nl" language
				}
			}
		}
		return HumanLanguage.Helper.getTranslationFallbackLanguageLocale(locale);
    }


    /**
     * Default HumanLanguageProvider with some helpful features for categorizing languages and 
     * determining fallback behavior.
     * 
     * @author stamm
     */
    public static final class DefaultHumanLanguageImplProvider extends LanguageProvider.Helper {
        private final List<DefaultHumanLanguageImpl> ALL_STANDARD;
        private final List<DefaultHumanLanguageImpl> ALL_END_USER;
        private final List<DefaultHumanLanguageImpl> ALL_PLATFORM_ONLY;
        private final Map<DefaultHumanLanguageImpl,DefaultHumanLanguageImpl> TRANS_FALLBACK;
        private final Map<LanguageType,Integer> TYPE_COUNT;
    	
    	public DefaultHumanLanguageImplProvider() {
    		super(DefaultHumanLanguageImpl.ENGLISH, Arrays.asList(DefaultHumanLanguageImpl.values())); 
    		
            EnumMap<LanguageType,Integer> languageCount = new EnumMap<>(LanguageType.class);
            List<DefaultHumanLanguageImpl> allStandard = new ArrayList<>(32);
            List<DefaultHumanLanguageImpl> allEndUser = new ArrayList<>(64);
            List<DefaultHumanLanguageImpl> allPlatformOnly = new ArrayList<>(64);
            EnumMap<DefaultHumanLanguageImpl,DefaultHumanLanguageImpl> fallback = new EnumMap<>(DefaultHumanLanguageImpl.class);
            for (LanguageType type : LanguageType.values()) languageCount.put(type, 0);  // Initialize to 0

            for (DefaultHumanLanguageImpl language : values()) {
                LanguageType type = language.getLanguageType();
                languageCount.put(type, languageCount.get(type) + 1);

                switch(type) {
                case STANDARD:
                    allStandard.add(language); break;
                case END_USER:
                    allEndUser.add(language); break;
                case PLATFORM:
                    allPlatformOnly.add(language); break;
                case HIDDEN:
                    // Do nothing
                }
                
                Locale fallbackLocale = getTranslationFallbackLanguageLocale(language.getLocale());
                DefaultHumanLanguageImpl fallbackLanguage = (DefaultHumanLanguageImpl) getLanguage(fallbackLocale);
                fallback.put(language, fallbackLanguage);
            }
            
            
            TYPE_COUNT = Collections.unmodifiableMap(languageCount);
            ALL_STANDARD = Collections.unmodifiableList(allStandard);
            ALL_END_USER = Collections.unmodifiableList(allEndUser);
            ALL_PLATFORM_ONLY = Collections.unmodifiableList(allPlatformOnly);
            TRANS_FALLBACK = Collections.unmodifiableMap(fallback);
    	
    	}

        /**
         * Return a list of DefaultHumanLanguageImpls that are of the specified LanguageType
         * NOTE: Only standard, end user, and platform only languages are supported
         * Other language types will cause null to be returned
         */
        List<DefaultHumanLanguageImpl> getLanguageList(LanguageType type) {
            switch(type) {
            case STANDARD:
                return ALL_STANDARD;
            case END_USER:
                return ALL_END_USER;
            case PLATFORM:
                return ALL_PLATFORM_ONLY;
            case HIDDEN:
                // Do nothing
            }
            return null;
        }

        /**
         * Returns a list of all DefaultHumanLanguageImpls that a user would have access to
         * given the specified language type
         */
        List<DefaultHumanLanguageImpl> getAllLanguagesList(LanguageType type) {
            List<DefaultHumanLanguageImpl> langs = new ArrayList<>(ALL_STANDARD);
            // Note this case statement is ordered by LanguageType dependency
            // The returned language list should also be ordered so that the most
            // pref-dependent language appears last
            switch(type) {
            case HIDDEN:
                // All languages should be visible with Hidden
                langs.clear();
                langs.addAll(Arrays.asList(DefaultHumanLanguageImpl.values()));
                break;
            case PLATFORM:
                langs.addAll(ALL_END_USER);
                langs.addAll(ALL_PLATFORM_ONLY);
                break;
            case END_USER:
                langs.addAll(ALL_END_USER);
                break;
            case STANDARD:
                // Do nothing
            }
            return langs;
        }

        
        int getTypeCount(LanguageType type) {
        	return TYPE_COUNT.get(type);
        }
        
		DefaultHumanLanguageImpl getTranslationFallbackLanguage(DefaultHumanLanguageImpl lang) {
			return TRANS_FALLBACK.get(lang);
		}
    }
}
