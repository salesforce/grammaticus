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

package com.force.i18n;

import java.util.Locale;


/**
 * Represents the text direction for a language or set of characters.
 *
 * Similar to ComponentOrientation, or Bidi, but an enum, which is always better.
 *
 * @author stamm
 */
public enum TextDirection {
    LTR,
    RTL;

    public String getToken(String token) {
        if ("left".equals(token)) {
            return getLeft();
        } else if ("right".equals(token)) {
            return getRight();
        } else if ("Left".equals(token)) {
            return getCapLeft();
        } else if ("Right".equals(token)) {
            return getCapRight();
        } else {
            return null;
        }
    }


    /**
     * @return the value for css for "canonical" left.  So that in a right to left language, it would be "right"
     */
    public String getLeft() { return this == LTR ? "left" : "right"; }
    public String getCapLeft() { return this == LTR ? "Left" : "Right"; }
    /**
     * @return the value for css for "canonical" right.  So that in a right to left language, it would be "left"
     */
    public String getRight() { return this == LTR ? "right" : "left"; }
    public String getCapRight() { return this == LTR ? "Right" : "Left"; }

    char getEmbeddingChar() {
        return this == LTR ? '\u202A' : '\u202B';
    }

    /**
     * Surround the string with the appropriate LTR or RTL mark
     * @param string the string to surround
     * @return the given string surrounded by the unicode embedding char marks
     */
    public String addEmbeddingMarks(String string) {
        if (string == null) return string;
        StringBuilder sb = new StringBuilder(string.length() + 2);
        sb.append(getEmbeddingChar());
        sb.append(string);
        sb.append('\u202C');  // Pop Directional Format character
        return sb.toString();
    }

    /**
     * Make the string display as left to right, if necessary, based on whether this direction needs it (i.e. the code is right to left.)
     * @param string the string to embed as left to right
     * @return the string with the appropriate embedding marks added.
     */
    public String makeStringLeftToRight(String string) {
        switch (this) {
        case RTL: return LTR.addEmbeddingMarks(string);
        default:
        case LTR: return string;
        }
    }

    /**
     * Get the text direction for the given locale.
     * @param locale the locale to test
     * @return the text direction for that locale's language
     */
    public static TextDirection getDirection(Locale locale) {
        if (locale == null) return invertIfNotNormalDirection(LTR);
        // Special case for "en_IL" to have an english pseudo-RTL language
        if ("IL".equals(locale.getCountry()) && "en".equals(locale.getLanguage())) return invertIfNotNormalDirection(RTL);
        String script = locale.getScript();
        switch (script) {
        case "Arab":
        case "Adlm":
        	return invertIfNotNormalDirection(RTL);
        case "Deva":
        	return invertIfNotNormalDirection(LTR);
        }
        return getDirection(locale.getLanguage());
    }

    /**
     * Get the text direction for the given language iso code (2 characters please)
     * @param lang the language to test
     * @return the text direction for that locale's language
     */
    public static TextDirection getDirection(String lang) {
        if (lang == null) return invertIfNotNormalDirection(LTR);
        // Hebrew, Arabic, Farsi, and Urdu.
        switch (lang) {
        case "he":
        case "ar":
        case "iw":
        case "fa":
        case "ur":
        case "ji":
        case "yi":
        case "dv":
        case "ks":
        case "sd":
        case "ug":
        case "ps":
        case "ckb":
        case "lrc":
        case "mzn":
        case "pnb":
        case "nqo":
        case "syr":
            return invertIfNotNormalDirection(RTL);
        default:
        }
        return invertIfNotNormalDirection(LTR);
    }

    /**
     * Append this marker next to "weak" characters (numbers and punctuation chars)
     * to provide direction to that and all other neighboring "weak" characters
     * See Sec 7.3, http://www.w3.org/TR/i18n-html-tech-bidi/#ri20030218.135304584
     * @return the Right-to-Left Marker (RLM) or Left-To-Right Marker (LRM) character
     */
    public char getBaseDirectionChar() {
        return this == LTR ? '\u200E' : '\u200F';
    }

    private static boolean normalTextDirection = true;

    /**
     * To aid debugging RTL issues, setNormalTextDirection(false) which
     * allows a page to be shown with its text direction inverted.
     * @param direction the direction to invert
     * @return the reversed direction if setNormalTextDirection has been called with false
     * @deprecated use en_IL for pseudotranslation
     */
    @Deprecated
    public static TextDirection invertIfNotNormalDirection(TextDirection direction) {
        if (normalTextDirection) {
            return direction;
        } else {
            return direction==LTR ? RTL : LTR;
        }
    }

    /**
     * Set to false to allow LTR languages to be shown as RTL, or RTL
     * as LTR.  Especially helpful for debugging RTL styling issues
     * and staying sane while doing so.  Don't even try to call this
     * on a production configuration.
     * @param normal what the normal text direction should be
     */
    @Deprecated
    public static void setNormalTextDirection(boolean normal) {
        // obviously, this method should never be called in production.
        normalTextDirection = normal;
    }

    /**
     * @return true if the text direction is normal
     */
    @Deprecated
    public static boolean getNormalTextDirection() {
        return normalTextDirection;
    }

}
