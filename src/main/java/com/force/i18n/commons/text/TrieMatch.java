/*
 * Copyright, 1999-2008, salesforce.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.i18n.commons.text;

/**
 * Struct returned by {@link TrieMatcher#match(CharSequence, int)} to represent a match.
 * 
 * @author koliver
 * @see TrieMatcher
 */
public class TrieMatch {

    private final int position;
    private final String word;
    private final String replacement;

    TrieMatch(int position, String word, String replacement) {
        if (position < 0) throw new IllegalArgumentException(Integer.toString(position));
        if (word == null) throw new NullPointerException();
        if (replacement == null) throw new NullPointerException();
        this.position = position;
        this.word = word;
        this.replacement = replacement;
    }
    
    /**
     * @return position of where the match was in the source.
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
     * @return word in the trie that matched.
     * Eg, <pre>
     *    Trie trie = new Trie(String[]{"x"}, String[]{"Y"});
     *    TrieMatch match = trie.match("abcxdef");
     *    Assert.assertEquals("x", match.getWord());
     * </pre>
     */
    public String getWord() {
        return this.word;
    }
    
    /**
     * @return the replacement for word in the trie that matched.
     * Eg, <pre>
     *    Trie trie = new Trie(String[]{"x"}, String[]{"Y"});
     *    TrieMatch match = trie.match("abcxdef");
     *    Assert.assertEquals("Y", match.getReplacement());
     * </pre>
     */
    public String getReplacement() {
        return this.replacement;
    }
    
}
