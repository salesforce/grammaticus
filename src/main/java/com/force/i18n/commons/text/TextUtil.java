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

package com.force.i18n.commons.text;

import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterators;


/**
 * A package of generic text utility functions.
 *
 * Beta class. Classes under com.force.i18n.commons package will be moved into a dedicated project.
 *
 * @author davem,pnakada,jjordano,et. al.
 */
@Beta
public final class TextUtil {

    private static final Logger logger = Logger.getLogger(TextUtil.class.getName());

    //Various statics for logging long strings undergoing escaping
    private static final int MIN_LOG_LENGTH = 1000000; //1,000,000 character strings are suspicious :P
    public static final int MIN_GACK_LENGTH = 23000000; //23,000,000 character strings are insane
    private static final int MIN_REJECT_LENGTH = 30000000; //30,000,000 character strings are unacceptable
    /* package */ static final String LOG_REJECT_MESSAGE = " attempt to process a string of length ";
    private static final String ESCAPE_TO_XML = "escapeToXml";
    private static final String ESCAPE_TO_HTML = "escapeToHtml";

    private static final int defaultMaxWordLength = 30;
    private static final Map<Integer, Pattern> longWords = new HashMap<Integer, Pattern>();
    private static final String wbr = "<wbr/><span class=\"wbr\"></span>";

    private static SuspiciousValueLogger SUSPICIOUS_LOGGER = null;

    /**
     * Interface you can override for logging suspicious values (very long strings).
     */
    public static interface SuspiciousValueLogger {
        /**
         * Provide the ability for the caller to log/monitor/vomit over a long string.
         * 1M chars+
         * @param logger the logger for the TextUtil class
         * @param source the source specified by the caller
         * @param length the length of the long string
         * @param first1000Chars the first 1000 chars of the string
         */
        void logLongString(Logger logger, String source, int length, String first1000Chars);
        /**
         * Provide the ability for the caller to log/monitor/vomit over a gigantic string.
         * 23M chars.  It will call logLongString first.
         * @param logger the logger for the TextUtil class
         * @param source the source specified by the caller
         * @param length the length of the huge string
         * @param ex an exception in the logValue method
         */
        void logHugeString(Logger logger, String source, int length, Exception ex);
        /**
         * Provide the ability for the caller to log/monitor/vomit over a gigantic string.
         * 23M chars.  It will call logLongString first.
         * @param logger the logger for the TextUtil class
         * @param source the source specified by the caller
         * @param ex an exception in the logValue method
         */
        void logExceptionWhenLogging(Logger logger, String source,  Exception ex);
    }

    protected static void logValue(String source, CharSequence value) {
        if(value == null) {
            return;
        }
        try {
            int length = value.length();
            if(length > MIN_LOG_LENGTH) {
                //Only log the first 1000 characters so we don't make logging sad. We just want to know
                //whether the string is legitimate or if it should be blocked
                if (SUSPICIOUS_LOGGER != null) {
                    SUSPICIOUS_LOGGER.logLongString(logger, source, length, value.subSequence(0, 1000).toString());
                }
            }
            if(length > MIN_GACK_LENGTH) {
                if (SUSPICIOUS_LOGGER != null) {
                    SUSPICIOUS_LOGGER.logHugeString(logger, source, length, new Exception());
                }
            }
            if(length > MIN_REJECT_LENGTH) {
                throw new IllegalArgumentException(source + LOG_REJECT_MESSAGE + length);
            }
        }
        catch(IllegalArgumentException e) {
            //So this doesn't fall into the exception case below
            throw e;
        }
        catch(Exception e) {
            //Catch everything because we don't want any exceptions to bubble up and fail when
            //we're just logging
            if (SUSPICIOUS_LOGGER != null) {
                SUSPICIOUS_LOGGER.logExceptionWhenLogging(logger, source, new Exception());
            }
        }
    }

    /**
     * Escapes <code>String</code>s into valid xml. Similar to <code>escapeInput</code> except that it will also
     * replace control characters with spaces.
     * <br><br>
     * Unless you are writing an element class or writing something that doesn't use elements,
     * <b>you probably shouldn't call this method</b>.
     * <br><br>
     * Calling this method in conjunction with an element is an error and will result in double-escaping.
     * <br><br>
     * The convention in the app is that all escaping is done at output time by Elements
     * and output should go through elements when possible. If you are using this method, you should think
     * carefully about what you are doing and decide if it's truly necessary to bypass elements.
     * @param input the text to escape
     * @return the string after escaping
     */
    public static String escapeToXml(CharSequence input) {
        return escapeToXml(input, false, false);
    }

    public static String escapeToXml(CharSequence input, boolean allowNewLines, boolean convertNulls) {
        return escapeToXml(input, allowNewLines, convertNulls, false);
    }

    public static String escapeToXml(CharSequence input, boolean allowNewLines, boolean convertNulls,
            boolean escapeApos) {
        return escapeToXml(input, allowNewLines, convertNulls, escapeApos, false);
    }

    /**
     * Escapes <code>String</code>s into valid xml. Similar to <code>escapeInput</code> except that it will also
     * replace control characters with spaces.
     * <br><br>
     * Unless you are writing an element class or writing something that doesn't use elements,
     * <b>you probably shouldn't call this method</b>.
     * <br><br>
     * Calling this method in conjunction with an element is an error and will result in double-escaping.
     * <br><br>
     * The convention in the app is that all escaping is done at output time by Elements
     * and output should go through elements when possible. If you are using this method, you should think
     * carefully about what you are doing and decide if it's truly necessary to bypass elements.
     * @return the string after escaping
     * @param input
     *        the text to escape
     * @param allowNewLines
     *        if false, newlines (\r or \n) are converted to spaces instead
     * @param convertNulls
     *        convert nulls to the empty string if treu
     * @param escapeApos
     *        Add a backslash in front of apostrophes to deal with MSXML's nonsense
     * @param preserveWhitespace
     *        if true, whitespace chars (as defined by {@link Character#isWhitespace(char)}) are not converted to
     *        spaces. If false, they may be converted to spaces if they are control characters. This argument is weaker
     *        than {@code allowNewLines}, so if {@code allowNewLines} is true but this argument is false, newlines will
     *        be preserved anyway. Since newlines are also whitespace, if {@code allowNewLines} is false but this
     *        argument is true, then newlines will still be preserved.
     */
    public static String escapeToXml(CharSequence input, boolean allowNewLines, boolean convertNulls,
            boolean escapeApos, boolean preserveWhitespace) {
        if (input == null || input.length() == 0) {
            return convertNulls ? "" : input == null ? null : "";
        }

        logValue(ESCAPE_TO_XML, input);

        int limit = input.length();
        DeferredStringBuilder buf = new DeferredStringBuilder(input);
        for (int i = 0; i < limit; i++) {
            char c = input.charAt(i);
            switch (c) {
                case '\n':
                    buf.append(allowNewLines ? '\n' : ' ');
                    break;
                case '\r':
                    buf.append(allowNewLines ? '\r' : ' ');
                    break;
                case '<':
                    buf.append("&lt;");
                    break;
                case '>':
                    buf.append("&gt;");
                    break;
                case '&':
                    buf.append("&amp;");
                    break;
                case '"':
                    buf.append("&quot;");
                    break;
                case '\'':
                    buf.append(escapeApos ? "&apos;" : "\'");
                    break;
                default:
                    if (!(preserveWhitespace && Character.isWhitespace(c)) && isIsoControlOrOddUnicode(c)) {
                        buf.append(' ');
                    } else {
                        buf.append(c);
                    }
                    break;
            }
        }
        return buf.toString();
    }

    /**
     * @return if the given input char is an iso-control character, undefined, or in an unusable Unicode block.
     * @param c the character to test
     */
    public static boolean isIsoControlOrOddUnicode(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (Character.isISOControl(c) || !Character.isDefined(c) || block == Character.UnicodeBlock.HIGH_SURROGATES
                || block == Character.UnicodeBlock.HIGH_PRIVATE_USE_SURROGATES || block == Character.UnicodeBlock.LOW_SURROGATES);
    }

    /**
     * Escape output being sent to the user to be safe in HTML. Replaces &lt; &gt; &amp; &quot; etc. with their HTML escape
     * sequences. Does not translate \n's.
     * <br><br>
     * Unless you are writing an element class or writing something that doesn't use elements,
     * <b>you probably shouldn't call this method</b>.
     * <br><br>
     * Calling this method in conjunction with an element is an error and will result in double-escaping.
     * <br><br>
     * The convention in the app is that all escaping is done at output time by Elements
     * and output should go through elements when possible. If you are using this method, you should think
     * carefully about what you are doing and decide if it's truly necessary to bypass elements.
     * @param value the string to escape to HTML
     * @return the escaped string
     */
    public static String escapeToHtml(String value) {
        return TextUtil.escapeToHtml(value, false);
    }

    /**
     * Escape output being sent to the user to be safe in HTML. Replaces &lt; &gt; &amp; &quot; etc. with their HTML escape
     * sequences. Also translates '\n' to &lt;br&gt; if <code>escapeNewline</code> is <code>true</code>.
     * <br><br>
     * Unless you are writing an element class or writing something that doesn't use elements,
     * <b>you probably shouldn't call this method</b>.
     * <br><br>
     * Calling this method in conjunction with an element is an error and will result in double-escaping.
     * <br><br>
     * The convention in the app is that all escaping is done at output time by Elements
     * and output should go through elements when possible. If you are using this method, you should think
     * carefully about what you are doing and decide if it's truly necessary to bypass elements.
     * @param value the string to escape to HTML
     * @param escapeNewline if the new lines should be converted to &lt;br&gt;
     * @return the escaped string
     */
    public static String escapeToHtml(String value, boolean escapeNewline) {
        if (value == null || value.length() == 0) {
            return value;
        }
        logValue(ESCAPE_TO_HTML, value);
        DeferredStringBuilder buf = new DeferredStringBuilder(value);
        // Optimized version of appendEscapedOutput where we can use appendQuicklyForEscapingWithoutSkips
        final int length = value.length();
        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            // TODO: Is this switch statement faster than an IntHashMap?  I'm guessing it is.
            switch (c) {
            case '\n':  if (escapeNewline) {
                buf.append("<br>");
            } else {
                buf.appendQuicklyForEscapingWithoutSkips(c);
            } break;
            case '<':   buf.append("&lt;"); break;
            case '>':   buf.append("&gt;"); break;
            case '&':   buf.appendAsDifferent("&amp;"); break;
            case '"':   buf.append("&quot;"); break;
            case '\'':   buf.append("&#39;"); break;
            case '\u2028':   buf.append("<br>"); break;
            case '\u2029':   buf.append("<p>"); break;
            case '\u00a9':   buf.append("&copy;"); break;  // ©
            default: buf.appendQuicklyForEscapingWithoutSkips(c);
            }
        }
        return buf.toString();
    }

    /**
     * @return a copy of the string, with all leading and trailing whitespace characters omitted. This is different from
     *         <code>java.lang.String.trim()</code>, which only trims characters before <code>'&#92;u0020'</code>
     *         but not characters like the wide space character in Japanese (<code>'&#92;u3000'</code>). will return
     *         an empty String if the input String is all whitespace
     * @param str the string to trim
     */
    public static String trim(String str) {
        return TextUtil.trim(str, false);
    }

    public static boolean isNullEmptyOrWhitespace(CharSequence str) {
        if (str == null) {
            return true;
        }

        return isEmptyOrWhitespace(str);
    }

    public static boolean isEmptyOrWhitespace(CharSequence str) {
        int end = str.length();
        char c;
        for (int i = 0; i < end; i++) {
            if (!((c = str.charAt(i)) <= ' ' || Character.isWhitespace(c))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param str
     *        String to be trimmed
     * @param returnNullIfEmptyString
     *        returnNullIfEmptyString
     * @return String if input String is null, return null if input String is non-empty after trimming, return the
     *         trimmed String if input String is empty after trimming, check boolean to determine return value
     */
    private static String trim(String str, boolean returnNullIfEmptyString) {
        if (str == null) {
            return null;
        }

        int start = 0;
        int end = str.length();
        char c;
        while ((start < end) && ((c = str.charAt(start)) <= ' ' || Character.isWhitespace(c))) {
            start++;
        }
        while ((start < end) && ((c = str.charAt(end - 1)) <= ' ' || Character.isWhitespace(c))) {
            end--;
        }

        if ((start == end) && returnNullIfEmptyString) {
            return null;
        }
        return ((start > 0) || (end < str.length())) ? str.substring(start, end) : str;
    }

    /**
     * Splits the given string str using the given delimiter and returns the result as a string list. If str is null, then
     * null is returned.<br>
     * <br>
     * The returned string list is an ArrayList that is constructed using the 4 as the ArrayList's initial size. If you
     * expect to have more than four elements more than just on the rare occasion, then please consider using another
     * splitSimple overload that lets you pass in the expected size.<br>
     * <br>
     * This is more efficient than String.split or TextUtil.split because it does not use a regular expression.<br>
     * <br>
     * <b>CAUTION:</b> The str and delimiter parameters are in an order that differs from other string splitting
     * methods. Be absolutely sure that you get the str and delimiter parameter arguments correct. This may eventually
     * be fixed with a refactoring.
     *
     * @param delimiter
     *            The delimiter to split the string using
     * @param str
     *            The string to split
     * @return String list or, if str was null, then null
     */
    public static List<String> splitSimple(String delimiter, String str) {
        return splitSimple(delimiter, str, 4);
    }

    /**
     * Splits the given string str using the given delimiter and returns the result as a string list. If str is null, then
     * null is returned.<br>
     * <br>
     * The returned string list is an ArrayList that is constructed using the given expected size as the ArrayList's
     * initial size. If you are not aware of the expected size, then use 0, which will cause this method to use a
     * LinkedList instead of an ArrayList.<br>
     * <br>
     * This is more efficient than String.split or TextUtil.split because it does not use a regular expression.<br>
     * <br>
     * <b>CAUTION:</b> The str and delimiter parameters are in an order that differs from other string splitting
     * methods. Be absolutely sure that you get the str and delimiter parameter arguments correct. This may eventually
     * be fixed with a refactoring.
     *
     * @param delimiter
     *            The delimiter to split the string using
     * @param str
     *            The string to split
     * @param expectedSize
     *            The expected number of elements in the output list. If you don't know, or if it could be arbitrarily
     *            large, and if you will only access the returned list sequentially with an iterator, then use 0 to tell
     *            this method to use a LinkedList
     * @return String list or, if str was null, then null
     */
    public static List<String> splitSimple(String delimiter, String str, int expectedSize) {
        return splitSimple(str, delimiter, expectedSize, false);
    }

    /**
     * Splits the given string str using the given delimiter, trims each element, and returns the result as a string
     * list. If str is null, then null is returned.<br>
     * <br>
     * The returned string list is an ArrayList that is constructed using the given expected size as the ArrayList's
     * initial size. If you are not aware of the expected size, then use 0, which will cause this method to use a
     * LinkedList instead of an ArrayList.<br>
     * <br>
     * This is more efficient than String.split or TextUtil.split because it does not use a regular expression.
     *
     * @param str
     *            The string to split
     * @param delimiter
     *            The delimiter to split the string using
     * @param expectedSize
     *            The expected number of elements in the output list. If you don't know, or if it could be arbitrarily
     *            large, and if you will only access the returned list sequentially with an iterator, then use 0 to tell
     *            this method to use a LinkedList
     * @return String list or, if str was null, then null
     */
    public static List<String> splitSimpleAndTrim(String str, String delimiter, int expectedSize) {
        return splitSimple(str, delimiter, expectedSize, true);
    }

    private static List<String> splitSimple(String s, String split, int expectedSize, boolean shouldTrim) {
        return splitSimple(s, split, expectedSize, shouldTrim, false);
    }

    private static List<String> splitSimple(String str, String delimiter, int expectedSize, boolean shouldTrim, boolean ignoreTrailingEmpty) {
        if (str == null) {
            return null;
        }
        List<String> result = (expectedSize == 0)? new LinkedList<String>(): new ArrayList<String>(expectedSize);

        if(delimiter.length() == 0) {
            if(!ignoreTrailingEmpty) {
                throw new IllegalArgumentException();
            }

            //Special case to match java's behavior
            char[] chars = new char[str.length()];
            str.getChars(0, str.length(), chars, 0);
            result.add("");
            for(char c : chars) {
                result.add(Character.toString(c));
            }
            return result;
        }

        //Special case to match java's behavior
        if(ignoreTrailingEmpty && "".equals(str)) {
            result.add("");
            return result;
        }

        int start = 0;
        int indexof;
        while ((indexof = str.indexOf(delimiter, start)) != -1) {
            String substring = str.substring(start, indexof);
            if (shouldTrim) {
                substring = substring.trim();
            }
            result.add(substring);
            start = indexof + delimiter.length();
            if (start >= str.length()) {
                break;
            }
        }
        if (start == str.length()) {
            result.add("");
        } else if (start < str.length()) {
            String substring = str.substring(start);
            if (shouldTrim) {
                substring = substring.trim();
            }
            result.add(substring);
        }
        if(ignoreTrailingEmpty && result.size() > 0) {
            //Discard empty substrings at the end
            for(int i=result.size()-1; i>=0; i--) {
                if(result.get(i).equals("")) {
                    result.remove(i);
                }
                else {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Makes the first letter of the input string upper case.
     * @param in the string to initialized
     * @return the string with the first character capitalized
     */
    public static String initCap(String in) {
        if (in == null || in.length() == 0 || Character.isUpperCase(in.charAt(0))) {
            return in;
        }
        if (in.length() == 1) {
            return in.toUpperCase();
        }
        StringBuilder result = new StringBuilder(in.length()).append(in);
        result.setCharAt(0, Character.toUpperCase(in.charAt(0)));
        return result.toString();
    }

    public static String escapeForMessageFormat(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        return escapeForMessageFormat(str, new StringBuilder(str.length()), true).toString();
    }

    /**
     * Single-quotes and curly brackets are special characters used by Java's MessageFormat class.
     * Specify check = true to only escape if a numbered param (eg. {0}) is specified
     * The escaping rules are:
     * 1) All single quotes =&gt; ''
     * 2) All left curly brackets that are not part of a numbered param need
     * to be wrapped with single quotes =&gt; '{'
     * NOTE: There's only support for wrapping left curly brackets with single-quotes; any
     * other use of single-quotes will be escaped
     * @param src the source string
     * @param sb the string buider to append to
     * @param check should we check first for whether there's a curly brace.  Performance improvement if you know there is one.
     * @return sb
     */
    public static StringBuilder escapeForMessageFormat(String src, StringBuilder sb, boolean check) {
        if (src == null || src.length() == 0) {
            return sb;
        }
        if (check && src.indexOf('{') < 0) {
            // Label doesn't specify any substitutions, ie. {0}
            return sb.append(src);
        }

        for (int idx = 0; idx < src.length(); idx++) {
            char ch = src.charAt(idx);
            if (ch == '{') {
                if (idx + 1 == src.length()) {
                    // Always escape if it's the last char
                    sb.append("'{'");
                } else {
                    // Wrap the left curly bracket if it's not used for numbered params
                    // Numbered params can be of the following format:
                    // {arguementIndex} => {0}
                    // {argumentIndex, formatType} => {0, number}
                    // {argumentIndex, formatType, formatStyle} => {0, number, integer}
                    boolean escape = false;
                    for (int i = idx + 1; i < src.length(); i++) {
                        char nextCh = src.charAt(i);
                        if (nextCh == '}') {
                            // We'll need to escape if the substring is {}
                            escape = i == idx + 1;
                            break;
                        } else if (nextCh > '9' || nextCh < '0' || i == src.length() - 1) {
                            // We want to escape the { if it's not part of a numbered param.
                            // or if we never saw a closing }
                            // Because there are 3 types of params supported, we also don't
                            // escape if we see a comma after a number (we know we've only
                            // seen numbers if i > idx + 1)
                            escape = nextCh != ',' || i == idx + 1;
                            break;
                        }
                    }

                    if (escape) {
                        // Wrap left curly bracket with single-quotes
                        sb.append("'{'");
                    } else {
                        // This curly bracket is for a numbered param,
                        // so leave it alone
                        sb.append("{");
                    }
                }
            } else if (ch == '\'') {
                if (idx + 1 == src.length()) {
                    // Always escape if it's the last char
                    sb.append("''");
                } else {
                    char nextCh = src.charAt(idx + 1);
                    if (nextCh == '\'') {
                        // Single-quote is already escaped
                        sb.append("''");
                        idx++;
                    } else if (nextCh == '{' && src.charAt(idx + 2) == '\'') {
                        // Found an escaped curly left bracket, so leave it alone
                        sb.append("'{'");
                        idx = idx + 2;
                    } else {
                        // Escape this single quotes
                        sb.append("''");
                    }
                }
            } else {
                sb.append(ch);
            }
        }
        return sb;
    }

    /**
     * Break long words and escape markup to HTML.  This method does not activate links.
     * @param text the text to break and escape
     * @param preserveNewLinesInHtml whether to preserve new line characters (\n) as &lt;br&gt; tags
     * @return the escaped and broken string
     */
    public static String breakLongWordsAndEscapeToHTML(String text, boolean preserveNewLinesInHtml) {
        return breakLongWordsAndEscapeToHTML(text, preserveNewLinesInHtml, defaultMaxWordLength);
    }

    /**
     * Break long words and escape markup to HTML.  This method does not activate links.
     * @param text the text to break and escape
     * @param preserveNewLinesInHtml whether to preserve new line characters (\n) as &lt;br&gt; tags
     * @param maxWordLength words longer than this will be broken with &lt;br&gt; tags (defaults to 30)
     * @return the escaped and broken string
     */
    public static String breakLongWordsAndEscapeToHTML(String text, boolean preserveNewLinesInHtml, int maxWordLength) {
        if (text == null){
            return null;
        }

        Pattern pattern = longWords.get(maxWordLength);
        if (pattern == null) {
            pattern = Pattern.compile("\\S{" + maxWordLength + "}");
            longWords.put(maxWordLength, pattern);
        }

        Matcher m = pattern.matcher(text);
        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (m.find()) {
            final int current = m.end();
            final String escaped = escapeToHtml(text.substring(index, current), preserveNewLinesInHtml);

            sb.append(escaped).append(wbr);
            index = current;
        }

        String escaped = escapeToHtml(text.substring(index), preserveNewLinesInHtml);
        sb.append(escaped);
        return sb.toString();
    }

    /**
     * Concatenate the string values of zero or more strings.
     *
     * @return the concatenated strings
     * @param separator - string that will delimit the result.
     * @param strings   - the strings whose values will be concatenated
     */
    public static String join(String separator, String ... strings) {
        return join(separator, (Object[])strings);
    }

    /**
     * Concatenate the string values of zero or more objects.
     *
     * @return the concatenated objects
     * @param separator - string that will delimit the result.
     * @param objects   - the objects whose string values will be concatenated
     */
    public static String join(String separator, Object ... objects) {
        switch (objects.length) {
        case 0: return "";
        case 1: return objects[0].toString();
        default: return join(separator, Iterators.forArray(objects));
        }
    }

    /**
     * @return a new fast comparator for strings for the given collator.
     * @param size the number of elements to compare (default is 16).
     * @param collator the collator to use for comparison
     */
    public static Comparator<String> getComparator(Collator collator, int size) {
        return new CollatingComparator(collator, size);
    }

    /**
     * A String comparator that uses the current collation, but
     * @author stamm
     */
    static class CollatingComparator implements Comparator<String> {
        private final Collator collator;
        private final Map<String, CollationKey> cKeyMap;

        CollatingComparator(Collator collator) {
            this(collator, 16);
        }

        CollatingComparator(Collator collator, int defaultSize) {
            this.collator = collator;
            cKeyMap = new HashMap<String, CollationKey>(defaultSize);
        }

        @Override
        public int compare(String o1, String o2) {
            if (o1 == o2) {
                return 0;
            } else if (o2 == null) {
                return 1;
            } else if (o1 == null) {
                return -1;
            }

            return getCollationKey(o1).compareTo(getCollationKey(o2));
        }

        private CollationKey getCollationKey(String comp) {
            CollationKey key = cKeyMap.get(comp);
            if (key == null) {
                key = collator.getCollationKey(comp);
                cKeyMap.put(comp, key);
            }
            return key;
        }
    }

    private static final String[] JSON_IN = new String[] { "\\",  "\b", "\f", "\n", "\r", "\t", "\"" };
    private static final String[] JSON_OUT = new String[] { "\\\\", "\\b", "\\f", "\\n", "\\r", "\\t", "\\\""};
    private static final TrieMatcher JSON_SEARCH_REPLACE = TrieMatcher.compile(JSON_IN, JSON_OUT);

    /**
     * Properly escapes strings to be displayed in Json Strings. This means that backslashes and double quotes are
     * escaped. <br>
     * <b>NOTE:</b> refer RFC8259 / ECMA 404. this method does not escape solidus (\x2f) as it seems to be both
     * acceptable in either slash, or escaped.
     * @see <a href="https://tools.ietf.org/html/rfc8259#section-7">RFC 8259  #7 Strings</a>
     * @param in the string to escape
     * @return the escaped string for json
     */
    public static String escapeForJsonString(String in) {
        return TrieMatcher.replaceMultiple(in, JSON_SEARCH_REPLACE);
    }
}
