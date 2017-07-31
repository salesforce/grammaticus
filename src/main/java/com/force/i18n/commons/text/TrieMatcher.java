/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.commons.text;

import java.util.*;

import com.force.i18n.commons.util.collection.IntHashMap;
import com.google.common.annotations.Beta;

/**
 * An immutable trie used for fast multiple string search and replace.
 *
 * It's set of words and replacements are populated at initialization,
 * and the data structure creation is not the cheapest of operations,
 * so it is best used when the object will be used multiple times.
 *
 * Beta class. Classes under com.force.i18n.commons package will be moved into a dedicated project.
 *
 * @author koliver
 * @see TrieMatcher#replaceMultiple(String, TrieMatcher)
 */
@Beta
public class TrieMatcher {

    private static final int DEFAULT_CAPACITY = 1; // trading initialization time for a small memory footprint

    /**
     * This is not the cheapest of operations.
     *
     * @param strings this is the list of words that make up the Trie.
     *      It is assumed that the lists are not modified once passed into the Trie
     * @param replacements the list of words that can be used to replace those words.
     *      It is assumed that the lists are not modified once passed into the Trie
     */
    public static TrieMatcher compile(String[] strings, String[] replacements) {
        return TrieMatcher.compile(Arrays.asList(strings), Arrays.asList(replacements));
    }

    /**
     * This is not the cheapest of operations.
     *
     * @param strings this is the list of words that make up the Trie.
     *      It is assumed that the lists are not modified once passed into the Trie
     * @param replacements the list of words that can be used to replace those words.
     *      It is assumed that the lists are not modified once passed into the Trie
     */
    public static TrieMatcher compile(List<String> strings, List<String> replacements) {
        return new TrieMatcher(strings, replacements);
    }
    
    /**
     * @param s the term to see if it starts with any terms of the trie
     */
    public boolean begins(CharSequence s) {
        TrieData match = begins(s, 0);
        return match != null;
    }

    private static class TrieData {
        String word;
        String replacement;
        final IntHashMap<TrieData> nextChars;

        TrieData(IntHashMap<TrieData> next) {
            this.nextChars = next;
        }
    }

    private final IntHashMap<TrieData> root;
    private final List<String> words;
    private final int minWordLength;

    /**
     * Use the factory {@link #compile()} instead.
     */
    private TrieMatcher(List<String> strings, List<String> replacements) {
        if (strings == null) throw new NullPointerException();
        if (replacements == null) throw new NullPointerException();

        if (strings.size() != replacements.size()) {
            throw new IllegalArgumentException("Replacements must have same size, "+ replacements.size()
                + ", as search strings " + strings.size());
        }

        this.words = Collections.unmodifiableList(strings);
        this.root = new IntHashMap<TrieData>(DEFAULT_CAPACITY);

        int minWordLen = Integer.MAX_VALUE;
        int wordIndex = 0;
        for (String s : strings) {
            IntHashMap<TrieData> current = this.root;

            int len = s.length();
            minWordLen = Math.min(minWordLen, len);
            for (int i = 0; i < len; i++) {
                int ch = s.charAt(i);
                TrieData next = current.get(ch);
                if (next == null) {
                    next = new TrieData(new IntHashMap<TrieData>(DEFAULT_CAPACITY));
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

    private TrieData begins(CharSequence s, int offset) {
        if (s == null || s.length() == 0 || offset < 0) return null;
        return contains(s, offset);
    }

    /**
     * @return null if not found
     */
    private TrieData contains(CharSequence s, int offset) {
        IntHashMap<TrieData> current = this.root;
        int len = s.length();
        LinkedList<TrieData> matches = null;
        TrieData firstMatch = null;

        for (int i = offset; i < len; i++) {
            int ch = s.charAt(i);
            TrieData nextData = current.get(ch);

            if (nextData == null) break;
            if (nextData.word != null) {
                if (firstMatch == null){
                    firstMatch = nextData;
                } else {
                    if (matches == null){
                        matches = new LinkedList<TrieData>();
                        matches.add(firstMatch);
                    }
                    matches.add(nextData);
                }
            }

            current = nextData.nextChars;
        }

        if (firstMatch != null) {
            // only 1 match, so we know that's the one
            if (matches == null) return firstMatch;

            // else, we need to find the "highest" priority order word
            // as specified by the input to the trie
            for (String word : this.words) {
                for (TrieData td : matches) {
                    if (word.equals(td.word)) return td;
                }
            }
        }

        return null;
    }

}
