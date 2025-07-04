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

import com.google.common.annotations.Beta;

/**
 * This class implements a StringBuilder that is incrementally copied from a source String.  Actual creation
 * the new buffer is deferred until a character differs from a character at the same position in
 * the source String.  This class is useful for reducing garbage creation when doing operations
 * like escaping a String, when most Strings are not expected to contain any escapable characters.  In that
 * case, no additional memory is used (as the original String is not actually copied).
 *
 * Beta class. Classes under com.force.i18n.commons package will be moved into a dedicated project.
 *
 * @author davem
 */
@Beta
public final class DeferredStringBuilder implements Appendable, CharSequence {

    private StringBuilder buf;
    private int pos;
    private final CharSequence source;

    public DeferredStringBuilder(CharSequence source) {
        if (source == null)
            this.buf = new StringBuilder(16);
        this.source = source;
    }

    @Override
    public DeferredStringBuilder append(char c) {
        if (this.buf == null) {
            if (this.pos < this.source.length() && c == this.source.charAt(this.pos)) {
                // characters match - just move ahead
                ++this.pos;
            } else {
                // doh - character mismatch - now we need to allocate a real StringBuilder
                this.buf = new StringBuilder(this.source.length() + 16);
                this.buf.append(this.source.subSequence(0, this.pos));
                this.buf.append(c);
            }
        } else {
            // we've already got the buf - just add this character
            this.buf.append(c);
        }
        return this;
    }

    /**
     * Append the given char, under the condition that the character is directly copied from the
     * parent.  This can only be done for performance reasons,
     * - You must be in a charAt loop for the parent string
     * - You *CAN NEVER SKIP A CHARACTER*.  So you can't use it in StringTypedReader.
     * @param c the character you're trying to add that came from the and your currently skipping
     */
    public void appendQuicklyForEscapingWithoutSkips(char c) {
        if (this.buf == null) {
            assert c == this.source.charAt(this.pos) && this.pos < this.source.length() : "You've violated the guarantee";
            this.pos++;
        } else {
            // we've already got the buf - just add this character
            this.buf.append(c);
        }
    }

    /**
     * Version of appendQuicklyForEscapingWithoutSkips that avoids a downcast in most cases.
     * @param c the character to append
     */
    public void appendQuicklyForEscapingWithoutSkips(int c) {
        if (this.buf == null) {
            // assert c == this.source.charAt(this.pos) && this.pos < this.source.length() : "You've violated the guarantee";
            this.pos++;
        } else {
            // we've already got the buf - just add this character
            this.buf.append((char)c);
        }
    }

    @Override
    public DeferredStringBuilder append(CharSequence csq) {
        if (csq == null)
            return this;
        return append(csq, 0, csq.length());
    }

    /**
     * Call this if the first character of the sequence may be the same as the given sequence.
     * Mostly, this applies to &amp; becoming &amp;amp;
     * @param csq the string to append
     */
    public void appendAsDifferent(CharSequence csq) {
        if (csq == null) return;
        if (this.buf == null) {
            this.buf = new StringBuilder(this.source.length() + 16);
            this.buf.append(this.source.subSequence(0, this.pos));
            this.buf.append(csq);
        } else {
            this.buf.append(csq);
        }
   }

    public void appendAsDifferent(char c) {
        if (this.buf == null) {
            this.buf = new StringBuilder(this.source.length() + 16);
            this.buf.append(this.source.subSequence(0, this.pos));
            this.buf.append(c);
        } else {
            this.buf.append(c);
        }
   }


    @Override
    public DeferredStringBuilder append(CharSequence csq, int start, int end) {
        if (csq != null) {
            if (buf == null) {
                int chars = end - start;
                if (chars < 10 || (this.pos + chars > this.source.length())) {  // For small strings or overflow, do it char by char.
                    for (int i = start; i < end; ++i) {
                        append(csq.charAt(i));
                    }
                } else {
                    CharSequence subSeq = csq.subSequence(start, end);
                    //String.equals seems to get optimized a lot quicker than the chartA + length + loop method.
                    //I don't think this will matter at all, but between this and OptimizedURLEncoder, this made these classes disappear from my profiler
                    if (this.source.subSequence(this.pos, this.pos + chars).equals(subSeq)) {
                        this.pos += chars;
                    } else {
                        this.buf = new StringBuilder(this.source.length() + 16);
                        this.buf.append(this.source.subSequence(0, this.pos));
                        this.buf.append(subSeq);
                    }
                }
            } else {
                // We know it's different, so just append the whole string.
                buf.append(csq, start, end);
            }
        }
        return this;
    }

    @Override
    public char charAt(int index) {
        if (this.buf != null) {
            return this.buf.charAt(index);
        } else if (index < pos) {
            return this.source.charAt(index);
        } else {
            throw new StringIndexOutOfBoundsException(index);
        }
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (this.buf != null) {
            return this.buf.subSequence(start, end);
        } else if (end <= pos) {
            return this.source.subSequence(start, end);
        } else {
            throw new StringIndexOutOfBoundsException(end);
        }
    }

    @Override
    public String toString() {
        return (this.buf != null) ? this.buf.toString() : (this.pos == this.source.length() ? this.source.toString() : this.source
            .subSequence(0, this.pos).toString());
    }

    @Override
    public int length() {
        return this.buf != null ? this.buf.length() : this.pos;
    }
}
