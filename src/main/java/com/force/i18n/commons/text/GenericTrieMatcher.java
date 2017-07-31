/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.commons.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.Beta;

/**
 * A trie matcher that uses tokens instead of strings.  It's not nearly
 * as efficient, since it uses a regular hashmap instead of a nice IntHashMap
 * to store the trie matches, but it's similar.  If you want to use this
 * for an AST or an enum, you probably want to genericize this to take in
 *
 * The parameter is the token, it can be an enum or an Id.
 * 
 * Beta class. Classes under com.force.i18n.commons package will be moved into a dedicated project.
 *
 * @author stamm
 */
@Beta
public class GenericTrieMatcher<T> {
    private static final int DEFAULT_CAPACITY = 1; // trading initialization time for a small memory footprint

    /**
     * This is not the cheapest of operations.
     *
     * @param searches this is the list of words that make up the Trie.
     *      It is assumed that the lists are not modified once passed into the Trie
     * @param replacements the list of words that can be used to replace those words.
     *      It is assumed that the lists are not modified once passed into the Trie
     */
    public static <TOKEN> GenericTrieMatcher<TOKEN> compile(List<? extends List<TOKEN>> searches, List<? extends List<TOKEN>> replacements) {
        return compile(searches, replacements, null);
    }


    /**
     * This is not the cheapest of operations.
     *
     * @param searches this is the list of words that make up the Trie.
     *      It is assumed that the lists are not modified once passed into the Trie
     * @param replacements the list of words that can be used to replace those words.
     *      It is assumed that the lists are not modified once passed into the Trie
     * @param tokenClass based on the class, a more efficient trie map can be generated
     */
    public static <TOKEN> GenericTrieMatcher<TOKEN> compile(List<? extends List<TOKEN>> searches, List<? extends List<TOKEN>> replacements, Class<TOKEN> tokenClass) {
        return new GenericTrieMatcher<TOKEN>(searches, replacements, tokenClass);
    }

    /**
     * Search and replace multiple strings in <code>s</code> given the the words and replacements given in
     * <code>TrieMatcher</code>.
     * <p>
     * Note, using a Trie for matching multiple strings can be much faster than the using
     * {@link #replace(String, String[], String[])}, however, due to the cost of creating the Trie, this is best used
     * when 1) you will reuse the Trie many times 2) you have a large set of strings your are searching on
     * <p>
     * Note, regexes aren't supported by this, see {@link #replace(String, String[], String[])}.
     *
     * @param s
     *        the text you are searching in
     * @param trieMatcher
     *        the trie representing the words to search and replace for
     * @return the text with the search words swapped by the replacements
     */
    public static final <T> List<T> replaceMultiple(List<T> s, GenericTrieMatcher<T> trieMatcher) {
        return replaceMultiple(s, trieMatcher, null);
    }


    /**
     * Search and replace multiple strings in <code>s</code> given the the words and replacements given in
     * <code>TrieMatcher</code> and a validation strategy
     * <p>
     * @param s
     *        the text you are searching in
     * @param trieMatcher
     *        the trie representing the words to search and replace for
     * @param validator
     *        the optional code that validates whether a match should be accepted or not.
     * @return the text with the search words swapped by the replacements
     */
    public static final <T> List<T> replaceMultiple(List<T> s, GenericTrieMatcher<T> trieMatcher, MatchValidator<T> validator) {
        if (s == null || trieMatcher == null)
            return s;

        // we don't use a DeferredStringBuilder because we don't expect to
        // reuse much of the original string. it's likely all or nothing.
        List<T> dsb = new ArrayList<T>(s.size() + 16);

        int pos = 0;
        int length = s.size();
        boolean foundMatch = false;
        while (pos < length) {
            GenericTrieMatch<T> match = trieMatcher.match(s, pos);
            // Try to find a valid match
            if (match != null && validator != null) {
                int curPos = pos;  // Start from pos and look for the next one
                while (match != null && pos < length && !validator.isValidMatch(match,s)) {
                    match = trieMatcher.match(s, ++curPos);
                }
            }
            if (match == null) {
                if (!foundMatch) {
                    return s;
                } else {
                    // No more matches, so copy the rest and get gone
                    dsb.addAll(s.subList(pos, s.size()));
                    break;
                }
            }
            foundMatch = true;

            // Copy up to the match position
            if (match.getPosition() > pos)
                dsb.addAll(s.subList(pos, match.getPosition()));

            // Append the replacement
            dsb.addAll(match.getReplacement());

            // Advance our current position
            pos = match.getPosition() + match.getWord().size();
        }

        return dsb;
    }

    /**
     * An interface that represents whether a match for a given string is "valid"
     */
    public interface MatchValidator<K> {
        /**
         *
         * @param match the match found
         * @param src the original source string being modified (NOTE: the positions may be different.)
         * @return <tt>true</tt> if the match in the given src string is valid
         */
        public boolean isValidMatch(GenericTrieMatch<K> match, List<K> src);
    }


    /**
     * @param s the term to search for the terms of the trie in
     * @return true if the any of the terms are contained in <code>s</code>
     */
    public boolean containedIn(List<T> s) {
        GenericTrieMatch<T> match = match(s);
        return match != null;
    }

    /**
     * @param s the term to see if it starts with any terms of the trie
     */
    public boolean begins(List<T> s) {
        GenericTrieData<T> match = begins(s, 0);
        return match != null;
    }

    /**
     * Find the next match in <code>s</code>.
     *
     * @param s the term to search for the terms of the trie in
     * @param start the 0-based position to start the search from.
     * @return null if no match found
     */
    public List<T> findIn(List<T> s, int start) {
        GenericTrieMatch<T> match = match(s, start);
        if (match == null) return null;
        return match.getWord();
    }

    private static class GenericTrieData<K> {
        List<K> word;
        List<K> replacement;
        final Map<K,GenericTrieData<K>> nextChars;

        GenericTrieData(Map<K,GenericTrieData<K>> next) {
            this.nextChars = next;
        }
    }

    private final List<List<T>> words;
    private final Map<T,GenericTrieData<T>> root;
    private final int minWordLength;

    /**
     * Use the factory {@link #compile()} instead.
     */
    private GenericTrieMatcher(List<? extends List<T>> strings, List<? extends List<T>> replacements, Class<T> tokenClass) {
        if (strings == null) throw new NullPointerException();
        if (replacements == null) throw new NullPointerException();

        if (strings.size() != replacements.size()) {
            throw new IllegalArgumentException("Replacements must have same size, "+ replacements.size()
                + ", as search strings " + strings.size());
        }

        this.words = Collections.unmodifiableList(strings);
        this.root = makeMap(tokenClass);

        int minWordLen = Integer.MAX_VALUE;
        int wordIndex = 0;
        for (List<T> s : strings) {
            Map<T,GenericTrieData<T>> current = this.root;

            int len = s.size();
            minWordLen = Math.min(minWordLen, len);
            for (int i = 0; i < len; i++) {
                T ch = s.get(i);
                GenericTrieData<T> next = current.get(ch);
                if (next == null) {
                    next = new GenericTrieData<T>(makeMap(tokenClass));
                    current.put(ch, next);
                }
                current = next.nextChars;

                // if we're at the last char, store it and its replacement...
                if (i+1 == len) {
                    next.word = s;
                    next.replacement = replacements.get(wordIndex);
                }
            }
            wordIndex++;
        }

        this.minWordLength = minWordLen;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })  // Conversion to enum can't be done in a way that is safe
    private Map<T,GenericTrieData<T>> makeMap(Class<T> tokenClass) {
        if (tokenClass == null) return new HashMap<T,GenericTrieData<T>>(DEFAULT_CAPACITY);
        if (tokenClass.isEnum()) {
            return new EnumMap(tokenClass);
        }
        return new HashMap<T,GenericTrieData<T>>(DEFAULT_CAPACITY);
    }

    /**
     * See if the given string matches any of the given words in the Trie
     *
     * @return null if none are found.
     */
    GenericTrieMatch<T> match(List<T> s) {
        return match(s, 0);
    }

    /**
     * See if the given string matches any of the given words in the Trie
     *
     * @param offset where to start looking inside of the given String.
     * @return null if none are found.
     */
    public GenericTrieMatch<T> match(List<T> s, int offset) {
        if (s == null || s.size() == 0 || offset < 0) return null;

        int len = s.size();
        for (int i = offset; i < len; i++) {
            // optimize the case when we don't have enough room left to contain any matches
            if (i + this.minWordLength > len) break;

            GenericTrieData<T> data = contains(s, i);
            if (data != null) return new GenericTrieMatch<T>(i, data.word, data.replacement);
        }

        return null;
    }

    private GenericTrieData<T> begins(List<T> s, int offset) {
        if (s == null || s.size() == 0 || offset < 0) return null;
        return contains(s, offset);
    }

    /**
     * @return null if not found
     */
    private GenericTrieData<T> contains(List<T> s, int offset) {
        Map<T,GenericTrieData<T>> current = this.root;
        int len = s.size();
        LinkedList<GenericTrieData<T>> matches = null;

        for (int i = offset; i < len; i++) {
            T ch = s.get(i);
            GenericTrieData<T> nextData = current.get(ch);

            if (nextData == null) break;
            if (nextData.word != null) {
                if (matches == null) matches = new LinkedList<GenericTrieData<T>>();
                matches.add(nextData);
            }

            current = nextData.nextChars;
        }

        if (matches != null) {
            // only 1 match, so we know that's the one
            if (matches.size() == 1) return matches.getFirst();

            // else, we need to find the "highest" priority order word
            // as specified by the input to the trie
            for (List<T> word : this.words) {
                for (GenericTrieData<T> td : matches) {
                    if (word.equals(td.word)) return td;
                }
            }
        }

        return null;
    }


    /**
     * Struct returned by {@link TrieMatcher#match(String)} to represent a match.
     *
     * @author koliver
     * @see TrieMatcher
     */
    public static class GenericTrieMatch<K> {

        private final int position;
        private final List<K> word;
        private final List<K> replacement;

        GenericTrieMatch(int position, List<K> word, List<K> replacement) {
            if (position < 0) throw new IllegalArgumentException(Integer.toString(position));
            if (word == null) throw new NullPointerException();
            if (replacement == null) throw new NullPointerException();
            this.position = position;
            this.word = Collections.unmodifiableList(word);
            this.replacement = Collections.unmodifiableList(replacement);
        }

        /**
         * The position of where the match was in the source.
         * Eg, <pre>
         *    Trie trie = new Trie(String[]{"x"}, String[]{"Y"});
         *    TrieMatch match = trie.match("abcxdef");
         *    Assert.assertEquals(3, match.getPosition());
         * </pre>
         */
        public int getPosition() {
            return this.position;
        }

        /**
         * The word in the trie that matched.
         * Eg, <pre>
         *    Trie trie = new Trie(String[]{"x"}, String[]{"Y"});
         *    TrieMatch match = trie.match("abcxdef");
         *    Assert.assertEquals("x", match.getWord());
         * </pre>
         */
        public List<K> getWord() {
            return this.word;
        }

        /**
         * The replacement for word in the trie that matched.
         * Eg, <pre>
         *    Trie trie = new Trie(String[]{"x"}, String[]{"Y"});
         *    TrieMatch match = trie.match("abcxdef");
         *    Assert.assertEquals("Y", match.getReplacement());
         * </pre>
         */
        public List<K> getReplacement() {
            return this.replacement;
        }

    }



}
