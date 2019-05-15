/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.commons.text;

import java.util.ArrayList;
import java.util.List;

import com.force.i18n.commons.text.GenericTrieMatcher.GenericTrieMatch;
import com.force.i18n.commons.text.GenericTrieMatcher.MatchValidator;
import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

/**
 * @author stamm
 * @since 164
 */
public class GenericTrieMatcherTest extends TestCase {
    public GenericTrieMatcherTest(String name) {
        super(name);
    }

    public void testConstructor() throws Exception {
        try {
            GenericTrieMatcher.compile(null, new ArrayList<List<String>>());
            fail();
        } catch (NullPointerException expected) {
            // expected
        }

        try {
            GenericTrieMatcher.compile(new ArrayList<List<String>>(), null);
            fail();
        } catch (NullPointerException expected) {
            // expected
        }

        try {
            GenericTrieMatcher.compile(new ArrayList<List<String>>(), ImmutableList.of(ImmutableList.of("foo")));
            fail();
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    private static List<String> l(String... strings) {
        return ImmutableList.copyOf(strings);
    }

    public void testMatch_NoOffset() throws Exception {
        GenericTrieMatcher<String> trie = GenericTrieMatcher.compile(ImmutableList.of(l("a","b","c","d"), l("a","b","e"), l("b","a","d")),
                ImmutableList.of(l("x","y","z"), l("1","2","3"), l("u","m","m","m","m","m")));
        GenericTrieMatch<String> match;

        match = trie.match(l("l","a","b","a","d"));
        assertNotNull(match);
        assertEquals(l("b","a","d"), match.getWord());
        assertEquals(2, match.getPosition());
        assertEquals(l("u","m","m","m","m","m"), match.getReplacement());

        match = trie.match(l("a","b","c"));
        assertNull(match);

        match = trie.match(l("a","b","c","e"));
        assertNull(match);

        match = trie.match(l("a","b","c","d","x","x","x"));
        assertNotNull(match);
        assertEquals(l("a","b","c","d"), match.getWord());
        assertEquals(0, match.getPosition());
        assertEquals(l("x","y","z"), match.getReplacement());

        match = trie.match(l("x","x","x","a","b","c","d"));
        assertNotNull(match);
        assertEquals(l("a","b","c","d"), match.getWord());
        assertEquals(3, match.getPosition());
        assertEquals(l("x","y","z"), match.getReplacement());

        match = trie.match(null);
        assertNull(match);

        match = trie.match(new ArrayList<String>());
        assertNull(match);
    }

    public void testMatch_WithOffset() throws Exception {
        GenericTrieMatcher<String> trie = GenericTrieMatcher.compile(ImmutableList.of(l("a","b","c","d"), l("a","b","e"), l("b","a","d")),
                ImmutableList.of(l("x","y","z"), l("1","2","3"), l("u","m","m","m","m","m")));
        GenericTrieMatch<String> match;

        match = trie.match(l("l","a","b","a","d"), 2);
        assertNotNull(match);
        assertEquals(l("b","a","d"), match.getWord());
        assertEquals(2, match.getPosition());
        assertEquals(l("u","m","m","m","m","m"), match.getReplacement());

        match = trie.match(l("l","a","b","a","d"), 3);
        assertNull(match);

        match = trie.match(l("x","x","x","a","b","c","d"), 3);
        assertNotNull(match);
        assertEquals(l("a","b","c","d"), match.getWord());
        assertEquals(3, match.getPosition());
        assertEquals(l("x","y","z"), match.getReplacement());

        match = trie.match(l("x","x","x","a","b","c","d"), 4);
        assertNull(match);

        match = trie.match(null, 1);
        assertNull(match);

        match = trie.match(new ArrayList<String>(), 1);
        assertNull(match);
    }

    public void testMatch_WithOverlapingWords() throws Exception {
        GenericTrieMatcher<String> trie = GenericTrieMatcher.compile(ImmutableList.of(l("a","b","c","d"), l("a","b","e"), l("b","a","d")),
                ImmutableList.of(l("x","y","z"), l("1","2","3"), l("u","m","m","m","m","m")));
        GenericTrieMatch<String> match = trie.match(l("X","X","X","a","b","c","d","X","X","X"));
        assertNotNull(match);
        assertEquals(l("a","b","c","d"), match.getWord());
        assertEquals(3, match.getPosition());
        assertEquals(l("x","y","z"), match.getReplacement());
    }

    public void testContainedIn() throws Exception {
        List<List<String>> searchTerms = ImmutableList.of(l("a","b","c"),l("d","e","f"));
        GenericTrieMatcher<String> trie = GenericTrieMatcher.compile(searchTerms, searchTerms);
        assertFalse(trie.containedIn(l("x","x","x")));
        assertFalse(trie.containedIn(l("a","b"," ","d","e")));
        assertFalse(trie.containedIn(null));
        assertFalse(trie.containedIn(new ArrayList<String>()));
        assertTrue(trie.containedIn(l("a","b","c")));
        assertTrue(trie.containedIn(l("d","e","f")));
        assertTrue(trie.containedIn(l("a","b","c","d","e","f")));
    }

    public void testFindIn() throws Exception {
        List<List<String>> searchTerms = ImmutableList.of(l("a","b","c"),l("d","e","f"));
        GenericTrieMatcher<String> trie = GenericTrieMatcher.compile(searchTerms, searchTerms);
        assertNull(trie.findIn(l("x","x","x"), 0));
        assertNull(trie.findIn(l("x","x","x"), -1));
        assertNull(trie.findIn(l("x","x","x"), 999));
        assertNull(trie.findIn(l("a","b"," ","d","e"), 0));
        assertNull(trie.findIn(null, 0));
        assertNull(trie.findIn(new ArrayList<String>(), 0));

        assertEquals(l("a","b","c"), trie.findIn(l("a","b","c"), 0));
        assertNull(trie.findIn(l("a","b","c"), 1));

        assertEquals(l("d","e","f"), trie.findIn(l("d","e","f"), 0));

        assertEquals(l("a","b","c"), trie.findIn(l("a","b","c","d","e","f"), 0));
        assertEquals(l("d","e","f"), trie.findIn(l("a","b","c","d","e","f"), 1));
    }

    public void testBegins() throws Exception {
        List<List<String>> searchTerms = ImmutableList.of(l("a","b","c"),l("d","e","f"));
        GenericTrieMatcher<String> trie = GenericTrieMatcher.compile(searchTerms, searchTerms);
        assertFalse(trie.begins(l("x","x","x")));
        assertFalse(trie.begins(l("a","b"," ","d","e")));
        assertFalse(trie.begins(null));
        assertFalse(trie.begins(new ArrayList<String>()));

        assertTrue(trie.begins(l("a","b","c")));
        assertFalse(trie.begins(l("b","c")));

        assertTrue(trie.begins(l("d","e","f")));

        assertTrue(trie.begins(l("a","b","c","d","e","f")));
        assertTrue(trie.begins(l("d","e","f","a","b","c")));
        assertFalse(trie.begins(l("e","f","a","b","c")));
    }

    private enum TrieTokens {
        A,B,C,D,E,F,G,H,I,J,K;
    }

    private static List<TrieTokens> e(TrieTokens... strings) {
        return ImmutableList.copyOf(strings);
    }

    public void testEnumReplacement() throws Exception {
        GenericTrieMatcher<TrieTokens> trie = GenericTrieMatcher.compile(ImmutableList.of(e(TrieTokens.A, TrieTokens.B), e(TrieTokens.A, TrieTokens.D), e(TrieTokens.B, TrieTokens.D)),
                ImmutableList.of(e(TrieTokens.I), e(TrieTokens.J), e(TrieTokens.K)));

        assertEquals(e(TrieTokens.F, TrieTokens.J), GenericTrieMatcher.replaceMultiple(e(TrieTokens.F, TrieTokens.J), trie));
        assertEquals(e(TrieTokens.F, TrieTokens.J), GenericTrieMatcher.replaceMultiple(e(TrieTokens.F, TrieTokens.A, TrieTokens.D), trie));
        assertEquals(e(TrieTokens.F, TrieTokens.J, TrieTokens.C), GenericTrieMatcher.replaceMultiple(e(TrieTokens.F, TrieTokens.A, TrieTokens.D, TrieTokens.C), trie));
        assertEquals(e(TrieTokens.F, TrieTokens.A, TrieTokens.C), GenericTrieMatcher.replaceMultiple(e(TrieTokens.F, TrieTokens.A, TrieTokens.C), trie));

    }

    public void testMatcher() throws Exception {
        GenericTrieMatcher<TrieTokens> trie = GenericTrieMatcher.compile(ImmutableList.of(e(TrieTokens.A, TrieTokens.B), e(TrieTokens.A, TrieTokens.D), e(TrieTokens.B, TrieTokens.D), e(TrieTokens.D, TrieTokens.E)),
                ImmutableList.of(e(TrieTokens.I), e(TrieTokens.J), e(TrieTokens.K), e(TrieTokens.F)));

        MatchValidator<TrieTokens> validator = new MatchValidator<TrieTokens>() {
            @Override
            public boolean isValidMatch(GenericTrieMatch<TrieTokens> match, List<TrieTokens> src) {
                return match.getWord().get(0) != TrieTokens.B;  // Only validate A and C
            }
        };

        assertEquals(e(TrieTokens.F, TrieTokens.A, TrieTokens.C), GenericTrieMatcher.replaceMultiple(e(TrieTokens.F, TrieTokens.A, TrieTokens.C), trie, validator));
        assertEquals(e(TrieTokens.F, TrieTokens.B, TrieTokens.D), GenericTrieMatcher.replaceMultiple(e(TrieTokens.F, TrieTokens.B, TrieTokens.D), trie, validator));
        assertEquals(e(TrieTokens.F, TrieTokens.I, TrieTokens.D), GenericTrieMatcher.replaceMultiple(e(TrieTokens.F, TrieTokens.A, TrieTokens.B, TrieTokens.D), trie, validator));
        assertEquals(e(TrieTokens.F, TrieTokens.B, TrieTokens.F), GenericTrieMatcher.replaceMultiple(e(TrieTokens.F, TrieTokens.B, TrieTokens.D, TrieTokens.E), trie, validator));
    }
}

