/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.*;

import com.force.i18n.commons.text.TextUtil;
import com.force.i18n.grammar.RenamingProvider;
import com.force.i18n.grammar.RenamingProviderFactory;
import com.google.common.base.Preconditions;

/**
 * Performs locale-sensitive formatting of name fields.
 *
 * @author jared.pearson
 */
public class LocaleNameFormatter {
    private final LocaleAttributesUtil localeConfig;

    /**
     * Creates a new formatter with the locale information from {@link DefaultLocaleAttributesUtil}.
     */
    public LocaleNameFormatter() {
        this(DefaultLocaleAttributesUtil.get());
    }

    /**
     * Creates a new formatter with the given locale information.
     *
     * @param localeConfig the extra information associated to a locale
     */
    public LocaleNameFormatter(LocaleAttributesUtil localeConfig) {
        Preconditions.checkArgument(localeConfig != null, "localeConfig should not be null");
        this.localeConfig = localeConfig;
    }

    /**
     * Creates a new formatter with the given locale information.
     *
     * @param localeInfos the collection of extra information associated to a locale
     * @deprecated Use {@link #LocaleNameFormatter(LocaleAttributesUtil)} instead
     */
    @Deprecated
    public LocaleNameFormatter(Iterable<? extends LocaleInfo> localeInfos) {
        Preconditions.checkArgument(localeInfos != null, "localeInfos should not be null");
        this.localeConfig = new SimpleLocaleAttributesUtil(localeInfos);
    }

    /**
     * @return true when the specified locale should be considered "formal", in that you should always use the full
     * name when addressing a user.
     * @deprecated Use {@link LocaleAttributesUtil#isFormalLocale(Locale)} instead
     */
    @Deprecated
    public boolean isFormalLocale(Locale locale) {
        return localeConfig.isFormalLocale(locale);
    }

    /**
     * @return true when the specified locale uses an eastern name order.
     * @deprecated Use {@link LocaleAttributesUtil#useEasternNameOrder(Locale)} instead
     */
    @Deprecated
    public boolean useEasternNameOrder(Locale locale) {
        return localeConfig.useEasternNameOrder(locale);
    }

    /**
     * Performs locale-sensitive formatting of a name using all fields (if they apply in the locale).
     * <p>
     * Might return a <CODE>String</CODE> with a trailing space -- deal with it.
     *
     * @param locale    the locale that the name is displayed in
     * @param firstName the first name of the person
     * @param lastName  the last name of the person
     * @return String representing the locale-formatted name
     */
    public String format(Locale locale, String firstName, String lastName) {
        return format(locale, null, firstName, lastName);
    }

    /**
     * Performs locale-sensitive formatting of a name using all fields (if they apply in the locale).
     * <p>
     * Might return a <CODE>String</CODE> with a trailing space -- deal with it.
     *
     * @param locale     the locale that the name is displayed in
     * @param salutation the salutation of the person or null if not specified
     * @param firstName  the first name of the person
     * @param lastName   the last name of the person
     * @return String representing the locale-formatted name
     */
    public String format(Locale locale, String salutation, String firstName, String lastName) {
        return format(locale, salutation, firstName, lastName, false);
    }

    /**
     * Performs locale-sensitive formatting of a name using all fields (if they apply in the locale).
     * <p>
     * Might return a <CODE>String</CODE> with a trailing space -- deal with it.
     *
     * @param locale     the locale that the name is displayed in
     * @param salutation the salutation of the person or null if not specified
     * @param firstName  the first name of the person
     * @param lastName   the last name of the person
     * @param casual     if true, use first name only, if the locale allows it
     * @return String representing the locale-formatted name
     */
    public String format(Locale locale, String salutation, String firstName, String lastName, boolean casual) {
        return format(locale, salutation, firstName, lastName, null, null, null, casual);
    }

    /**
     * Performs locale-sensitive formatting of a name using all fields (if they apply in the locale).
     * <p>
     * Might return a <CODE>String</CODE> with a trailing space -- deal with it.
     *
     * @param locale       the locale that the name is displayed in
     * @param salutation   the salutation of the person or null if not specified
     * @param firstName    the first name of the person
     * @param lastName     the last name of the person
     * @param middleName   the middle name of the person or null if not specified
     * @param informalName the informal name of the person or null if not specified
     * @param suffix       the suffix or null if not specified
     * @return String representing the locale-formatted name
     */
    public String format(Locale locale, String salutation, String firstName, String lastName, String middleName, String informalName, String suffix) {
        return format(locale, salutation, firstName, lastName, middleName, informalName, suffix, false);
    }

    /**
     * Performs locale-sensitive formatting of a name using all fields (if they apply in the locale).
     * <p>
     * Might return a <CODE>String</CODE> with a trailing space -- deal with it.
     *
     * @param locale       the locale that the name is displayed in
     * @param salutation   the salutation of the person or null if not specified
     * @param firstName    the first name of the person
     * @param lastName     the last name of the person
     * @param middleName   the middle name of the person or null if not specified
     * @param informalName the informal name of the person or null if not specified
     * @param suffix       the suffix or null if not specified
     * @param casual       if true, use first name only, if the locale allows it
     * @return String representing the locale-formatted name
     */
    public String format(Locale locale, String salutation, String firstName, String lastName, String middleName, String informalName, String suffix, boolean casual) {
        final boolean isInformalNameSpecified = !TextUtil.isNullEmptyOrWhitespace(informalName);
        if (casual && (!TextUtil.isNullEmptyOrWhitespace(firstName) || isInformalNameSpecified) && !localeConfig.isFormalLocale(locale)) {
            return isInformalNameSpecified ? TextUtil.trim(informalName) : TextUtil.trim(firstName);
        }

        final RenamingProvider rp = RenamingProviderFactory.get().getProvider();
        final boolean canShowMiddleName = !TextUtil.isNullEmptyOrWhitespace(middleName) && rp.displayMiddleNameInCalculatedPersonName();
        final boolean canShowSuffix = !TextUtil.isNullEmptyOrWhitespace(suffix) && rp.displaySuffixInCalculatedPersonName();

        // the better the initial allocation, the less likely a re-allocation
        StringBuilder buf = new StringBuilder(
                (null == salutation ? 0 : salutation.length())
                        + (null == firstName ? 0 : firstName.length())
                        + (null == lastName ? 0 : lastName.length())
                        + (!canShowMiddleName ? 0 : middleName.length())
                        + (!canShowSuffix ? 0 : suffix.length()) + 10
        );
        if (localeConfig.useEasternNameOrder(locale)) { // format = "last first[ middle][ suffix]"
            appendNamePart(buf, lastName);
            appendNamePart(buf, firstName);
            if (canShowMiddleName) {
                appendNamePart(buf, middleName);
            }
        } else { // default format = "salutation first middle last"
            appendNamePart(buf, salutation);
            appendNamePart(buf, firstName);
            if (canShowMiddleName) {
                appendNamePart(buf, middleName);
            }
            appendNamePart(buf, lastName);
        }
        if (canShowSuffix) {
            appendNamePart(buf, suffix);
        }

        return buf.toString();
    }

    /**
     * Appends the name part specified to the buffer. If the buffer already contains characters (length > 0)
     * then a space is appended before the part. The part is not appended if it is null. The value is also trimmed
     * of any leading or trailing white spaces.
     */
    private void appendNamePart(StringBuilder buf, String part) {
        if (!TextUtil.isNullEmptyOrWhitespace(part)) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append(TextUtil.trim(part));
        }
    }
}