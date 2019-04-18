/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.*;
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
    SPANISH(new Locale("es")),
    FRENCH(Locale.FRENCH),
    ITALIAN(Locale.ITALIAN),
    JAPANESE(Locale.JAPANESE),
    SWEDISH(new Locale("sv")),
    KOREAN(Locale.KOREAN),
    CHINESE_TRAD(Locale.TRADITIONAL_CHINESE), // Chinese(Traditional)  (this order matters)
    CHINESE_SIMP(Locale.SIMPLIFIED_CHINESE), // Chinese(Simplified)
    PORTUGUESE_BR(new Locale("pt", "BR")), // Portuguese (Brazilian)
    DUTCH(new Locale("nl", "NL"), LanguageType.STANDARD, "nl"),  // This is historical, should always have been "nl"
    DANISH(new Locale("da")),
    THAI(new Locale("th")),
    FINNISH(new Locale("fi")),
    RUSSIAN(new Locale("ru")),
    SPANISH_MX(new Locale("es", "MX")), //Spanish (Mexican), end-user lang prior to 190
    NORWEGIAN(new Locale("no"), LanguageType.STANDARD, 168.0),  // Technically, this should be "nb", but that would be confusing to java and HTML

    // end-user languages
    HUNGARIAN(new Locale("hu"), LanguageType.END_USER),
    POLISH(new Locale("pl"), LanguageType.END_USER),
    CZECH(new Locale("cs"), LanguageType.END_USER),
    TURKISH(new Locale("tr"), LanguageType.END_USER),
    INDONESIAN(new Locale("in"), LanguageType.END_USER, "id"),  // Fix java screwup
    ROMANIAN(new Locale("ro"), LanguageType.END_USER),
    VIETNAMESE(new Locale("vi"), LanguageType.END_USER),
    UKRAINIAN(new Locale("uk"), LanguageType.END_USER),
    HEBREW(new Locale("iw"), LanguageType.END_USER, "he"), // Fix java screwup
    GREEK(new Locale("el"), LanguageType.END_USER),
    BULGARIAN(new Locale("bg"), LanguageType.END_USER),
    ENGLISH_GB(Locale.UK, LanguageType.END_USER, 168.0),
    ARABIC(new Locale("ar"), LanguageType.END_USER, 168.0),
    SLOVAK(new Locale("sk"), LanguageType.END_USER, 168.0),
    PORTUGUESE_PT(new Locale("pt", "PT"), LanguageType.END_USER, 172.0), // Portuguese (European)
    CROATIAN(new Locale("hr"), LanguageType.END_USER, 170.0),
    SLOVENE(new Locale("sl"), LanguageType.END_USER, 170.0),
    
    // platform languages
    FRENCH_CA(Locale.CANADA_FRENCH, LanguageType.PLATFORM, 168.0),
    GEORGIAN(new Locale("ka"), LanguageType.PLATFORM, 168.0),
    SERBIAN_CYRILLIC(new Locale("sr"), LanguageType.PLATFORM, 168.0),  // http://tlt.its.psu.edu/suggestions/international/bylanguage/serbocroatian.html
    SERBIAN_LATIN(new Locale("sh"), LanguageType.PLATFORM, "sr-Latn", 168.0),  // sh is deprecated, but using sr-Latn is only HTML and would confuse Java
    ENGLISH_AU(new Locale("en", "AU"), LanguageType.PLATFORM, 168.0),
    ENGLISH_MY(new Locale("en", "MY"), LanguageType.PLATFORM, 168.0),
    ENGLISH_IN(new Locale("en", "IN"), LanguageType.PLATFORM, 168.0),
    ENGLISH_PH(new Locale("en", "PH"), LanguageType.PLATFORM, 168.0),
    ENGLISH_CA(new Locale("en", "CA"), LanguageType.PLATFORM, 168.0),
    MOLDOVAN(new Locale("ro", "MD"), LanguageType.PLATFORM, 170.0),  // Note, this is just a variant, but called Moldovan for various reasons.
    BOSNIAN(new Locale("bs"), LanguageType.PLATFORM, 170.0),
    MACEDONIAN(new Locale("mk"), LanguageType.PLATFORM, 170.0),
    LATVIAN(new Locale("lv"), LanguageType.PLATFORM, 172.0),
    LITHUANIAN(new Locale("lt"), LanguageType.PLATFORM, 172.0),
    ESTONIAN(new Locale("et"), LanguageType.PLATFORM, 172.0),
    ALBANIAN(new Locale("sq"), LanguageType.PLATFORM, 172.0),
    MONTENEGRIN(new Locale("sh", "ME"), LanguageType.PLATFORM, 172.0),  // NOTE: this may end up as a "real" language some day.  Until then, it's staying as a serbo-croatian dialect
    MALTESE(new Locale("mt"), LanguageType.PLATFORM, 172.0),
    IRISH(new Locale("ga"), LanguageType.PLATFORM, 172.0),
    BASQUE(new Locale("eu"), LanguageType.PLATFORM, 172.0),
    WELSH(new Locale("cy"), LanguageType.PLATFORM, 172.0),
    ICELANDIC(new Locale("is"), LanguageType.PLATFORM, 172.0),

    MALAY(new Locale("ms"), LanguageType.PLATFORM, 172.0),
    TAGALOG(new Locale("tl"), LanguageType.PLATFORM, 172.0),

    LUXEMBOURGISH(new Locale("lb"), LanguageType.PLATFORM, 174.0),
    ROMANSH(new Locale("rm"), LanguageType.PLATFORM, 174.0),
    ARMENIAN(new Locale("hy"), LanguageType.PLATFORM, 174.0),
    HINDI(new Locale("hi"), LanguageType.PLATFORM, 174.0),
    URDU(new Locale("ur"), LanguageType.PLATFORM, 174.0),
    
    BENGALI(new Locale("bn"), LanguageType.PLATFORM, 190.0),
    GERMAN_AT(new Locale("de", "AT"), LanguageType.PLATFORM, 190.0),
    GERMAN_CH(new Locale("de", "CH"), LanguageType.PLATFORM, 190.0),
    TAMIL(new Locale("ta"), LanguageType.PLATFORM, 190.0),
    
    AFRIKAANS(new Locale("af"), LanguageType.PLATFORM, 220.0),
    SWAHILI(new Locale("sw"), LanguageType.PLATFORM, 220.0),
    ZULU(new Locale("zu"), LanguageType.PLATFORM, 220.0),
    XHOSA(new Locale("xh"), LanguageType.PLATFORM, 220.0),
    
    TELUGU(new Locale("te"), LanguageType.PLATFORM, 220.0),
    MALAYALAM(new Locale("ml"), LanguageType.PLATFORM, 220.0),
    KANNADA(new Locale("kn"), LanguageType.PLATFORM, 220.0),
    MARATHI(new Locale("mr"), LanguageType.PLATFORM, 220.0),
    GUJARATI(new Locale("gu"), LanguageType.PLATFORM, 220.0),
    
    MAORI(new Locale("mi"), LanguageType.PLATFORM, 220.0),
    BURMESE(new Locale("my"), LanguageType.PLATFORM, 220.0),
       
    ARABIC_DZ(new Locale("ar", "DZ"), LanguageType.PLATFORM, 194.0), //Arabic Algerian
    ARABIC_BH(new Locale("ar", "BH"), LanguageType.PLATFORM, 194.0), //Arabic Bahrain
    ARABIC_EG(new Locale("ar", "EG"), LanguageType.PLATFORM, 194.0), //Arabic Egypt
    ARABIC_IQ(new Locale("ar", "IQ"), LanguageType.PLATFORM, 194.0), //Arabic Iraq
    ARABIC_JO(new Locale("ar", "JO"), LanguageType.PLATFORM, 194.0), //Arabic Jordan
    ARABIC_KW(new Locale("ar", "KW"), LanguageType.PLATFORM, 194.0), //Arabic Kuwait
    ARABIC_LB(new Locale("ar", "LB"), LanguageType.PLATFORM, 194.0), //Arabic Lebanon
    ARABIC_LY(new Locale("ar", "LY"), LanguageType.PLATFORM, 194.0), //Arabic Libya
    ARABIC_MA(new Locale("ar", "MA"), LanguageType.PLATFORM, 194.0), //Arabic Morocco
    ARABIC_OM(new Locale("ar", "OM"), LanguageType.PLATFORM, 194.0), //Arabic Oman
    ARABIC_QA(new Locale("ar", "QA"), LanguageType.PLATFORM, 194.0), //Arabic Quatar
    ARABIC_SA(new Locale("ar", "SA"), LanguageType.PLATFORM, 194.0), //Arabic Saudi Arabia
    ARABIC_SD(new Locale("ar", "SD"), LanguageType.PLATFORM, 194.0), //Arabic Sudan
    ARABIC_SY(new Locale("ar", "SY"), LanguageType.PLATFORM, 194.0), //Arabic Syria
    ARABIC_TN(new Locale("ar", "TN"), LanguageType.PLATFORM, 194.0), //Arabic Tunisia
    ARABIC_AE(new Locale("ar", "AE"), LanguageType.PLATFORM, 194.0), //Arabic United Arab Emirates
    ARABIC_YE(new Locale("ar", "YE"), LanguageType.PLATFORM, 194.0), //Arabic Yemen

    CHINESE_SG(new Locale("zh", "SG"), LanguageType.PLATFORM, 194.0), //Chinese (Simplified) Singapore
    CHINESE_HK(new Locale("zh", "HK"), LanguageType.PLATFORM, 194.0), //Chinese (Traditional) Hong Kong

    ENGLISH_HK(new Locale("en", "HK"), LanguageType.PLATFORM, 194.0), //English Hong Kong
    ENGLISH_IE(new Locale("en", "IE"), LanguageType.PLATFORM, 194.0), //English Ireland
    ENGLISH_SG(new Locale("en", "SG"), LanguageType.PLATFORM, 194.0), //English Singapore
    ENGLISH_ZA(new Locale("en", "ZA"), LanguageType.PLATFORM, 194.0), //English South Africa
    
    FRENCH_BE(new Locale("fr", "BE"), LanguageType.PLATFORM, 194.0),  //French Belgium
    FRENCH_LU(new Locale("fr", "LU"), LanguageType.PLATFORM, 194.0),  //French Luxembourg
    FRENCH_CH(new Locale("fr", "CH"), LanguageType.PLATFORM, 194.0),  //French Switzerland
    
    GERMAN_LU(new Locale("de", "LU"), LanguageType.PLATFORM, 194.0),  //German Luxembourg
    
    ITALIAN_CH(new Locale("it", "CH"), LanguageType.PLATFORM, 194.0), //Italian Switzerland
    
    SPANISH_AR(new Locale("es", "AR"), LanguageType.PLATFORM, 194.0), //Spanish Argentina
    SPANISH_BO(new Locale("es", "BO"), LanguageType.PLATFORM, 194.0), //Spanish Bolivia
    SPANISH_CL(new Locale("es", "CL"), LanguageType.PLATFORM, 194.0), //Spanish Chile
    SPANISH_CO(new Locale("es", "CO"), LanguageType.PLATFORM, 194.0), //Spanish Colombia
    SPANISH_CR(new Locale("es", "CR"), LanguageType.PLATFORM, 194.0), //Spanish Costa Rica
    SPANISH_DO(new Locale("es", "DO"), LanguageType.PLATFORM, 194.0), //Spanish Dominican Republic
    SPANISH_EC(new Locale("es", "EC"), LanguageType.PLATFORM, 194.0), //Spanish Ecuador
    SPANISH_SV(new Locale("es", "SV"), LanguageType.PLATFORM, 194.0), //Spanish El Salvador
    SPANISH_GT(new Locale("es", "GT"), LanguageType.PLATFORM, 194.0), //Spanish Guatemala
    SPANISH_HN(new Locale("es", "HN"), LanguageType.PLATFORM, 194.0), //Spanish Honduras
    SPANISH_NI(new Locale("es", "NI"), LanguageType.PLATFORM, 194.0), //Spanish Nicaragua
    SPANISH_PA(new Locale("es", "PA"), LanguageType.PLATFORM, 194.0), //Spanish Panama
    SPANISH_PY(new Locale("es", "PY"), LanguageType.PLATFORM, 194.0), //Spanish Paraguay
    SPANISH_PE(new Locale("es", "PE"), LanguageType.PLATFORM, 194.0), //Spanish Peru
    SPANISH_PR(new Locale("es", "PR"), LanguageType.PLATFORM, 194.0), //Spanish Puerto Rico
    SPANISH_US(new Locale("es", "US"), LanguageType.PLATFORM, 194.0), //Spanish United States
    SPANISH_UY(new Locale("es", "UY"), LanguageType.PLATFORM, 194.0), //Spanish Uruguay
    SPANISH_VE(new Locale("es", "VE"), LanguageType.PLATFORM, 194.0), //Spanish Venezuela

    CATALAN(new Locale("ca"), LanguageType.PLATFORM, 210.0), // Catalan
    
    ESPERANTO(new Locale("eo"), LanguageType.HIDDEN, 172.0),  // Esperanto is our "fake" language, always leave it last
    ENGLISH_IL(new Locale("en", "IL"), LanguageType.HIDDEN, 214.0),  // en_IL for testing of right-to-left with latin characters
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
            return TYPE_COUNT.get(this);
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
        this.overrideLanguage = overrideLanguage;
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
	public TextDirection getDirection() {
        // invertIfNotNormalDirection checks if we have set a debugging "reverse" flag - to allow
        // a language like English to be shown as an RTL language.
        return TextDirection.invertIfNotNormalDirection(this.direction);
    }

    /**
     * @return the "override" language for historical reasons.  Generally, this means that the
     * locale for the language was wrong (dutch), or Java prevents it from being correct
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
        switch (this) {
            case ARABIC_DZ:
            case ARABIC_BH:
            case ARABIC_EG:
            case ARABIC_IQ:
            case ARABIC_JO:
            case ARABIC_KW:
            case ARABIC_LB:
            case ARABIC_LY:
            case ARABIC_MA:
            case ARABIC_OM:
            case ARABIC_QA:
            case ARABIC_SA:
            case ARABIC_SD:
            case ARABIC_SY:
            case ARABIC_TN:
            case ARABIC_AE:
            case ARABIC_YE:
                return ARABIC;
            case FRENCH_CA:
            case FRENCH_BE:
            case FRENCH_LU:
            case FRENCH_CH:
                return FRENCH;
            case SPANISH_MX:
            case SPANISH_AR:
            case SPANISH_BO:
            case SPANISH_CL:
            case SPANISH_CO: 
            case SPANISH_CR:
            case SPANISH_DO: 
            case SPANISH_EC: 
            case SPANISH_SV:  
            case SPANISH_GT:  
            case SPANISH_HN:  
            case SPANISH_NI:  
            case SPANISH_PA:   
            case SPANISH_PY:   
            case SPANISH_PE:    
            case SPANISH_PR:    
            case SPANISH_US:    
            case SPANISH_UY:    
            case SPANISH_VE:      
                return SPANISH;
            case CHINESE_HK:
                return CHINESE_TRAD;
            case CHINESE_SG:
                return CHINESE_SIMP ;
            case PORTUGUESE_PT:
                return PORTUGUESE_BR;
            case ENGLISH_AU:
            case ENGLISH_MY:
            case ENGLISH_PH:
            case ENGLISH_IN:
            case ENGLISH_HK:
            case ENGLISH_IE:
            case ENGLISH_SG:
            case ENGLISH_ZA:
                return ENGLISH_GB;
            case ENGLISH_GB:
            case ENGLISH_CA:
            case ENGLISH_IL:
                return ENGLISH;
            case ITALIAN_CH:
                return ITALIAN;
            case MONTENEGRIN:
                return SERBIAN_LATIN;  // Montenegrin is the same as serbian latin.
            case MOLDOVAN:
                return ROMANIAN;  // They are the same language
            case GERMAN_AT:
            case GERMAN_LU:
            case GERMAN_CH:
                return GERMAN;
            default:
                return null;
        }
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
     * @return whether use of fallback strings in this language should be considered a problem.  This only applies to end user & R2L languages that don't have a fallback
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

    /**
     * Return a list of DefaultHumanLanguageImpls that are of the specified LanguageType
     * NOTE: Only standard, end user, and platform only languages are supported
     * Other language types will cause null to be returned
     */
    public static List<DefaultHumanLanguageImpl> getLanguageList(LanguageType type) {
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
    public static List<DefaultHumanLanguageImpl> getAllLanguagesList(LanguageType type) {
        List<DefaultHumanLanguageImpl> langs = new ArrayList<>(DefaultHumanLanguageImpl.ALL_STANDARD);
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
            langs.addAll(DefaultHumanLanguageImpl.ALL_END_USER);
            langs.addAll(DefaultHumanLanguageImpl.ALL_PLATFORM_ONLY);
            break;
        case END_USER:
            langs.addAll(DefaultHumanLanguageImpl.ALL_END_USER);
            break;
        case STANDARD:
            // Do nothing
        }

        return langs;
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

    private static final List<DefaultHumanLanguageImpl> ALL_STANDARD;
    private static final List<DefaultHumanLanguageImpl> ALL_END_USER;
    private static final List<DefaultHumanLanguageImpl> ALL_PLATFORM_ONLY;

    // STATIC INITIALIZERS FOR MAGIC MAPS
    private static final Map<LanguageType,Integer> TYPE_COUNT;
    static {
        EnumMap<LanguageType,Integer> languageCount = new EnumMap<>(LanguageType.class);
        List<DefaultHumanLanguageImpl> allStandard = new ArrayList<>(32);
        List<DefaultHumanLanguageImpl> allEndUser = new ArrayList<>(64);
        List<DefaultHumanLanguageImpl> allPlatformOnly = new ArrayList<>(64);
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
        }
        TYPE_COUNT = Collections.unmodifiableMap(languageCount);
        ALL_STANDARD = Collections.unmodifiableList(allStandard);
        ALL_END_USER = Collections.unmodifiableList(allEndUser);
        ALL_PLATFORM_ONLY = Collections.unmodifiableList(allPlatformOnly);
    }
    
    
    public static final class DefaultHumanLanguageImplProvider extends LanguageProvider.Helper {
    	public DefaultHumanLanguageImplProvider() {
    		super(DefaultHumanLanguageImpl.ENGLISH, Arrays.asList(DefaultHumanLanguageImpl.values()));
    	}
    	
    }
}
