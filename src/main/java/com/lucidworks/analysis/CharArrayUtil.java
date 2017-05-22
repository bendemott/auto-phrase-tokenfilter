/*
 * The MIT License
 *
 * Copyright 2017 ben.demott.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.lucidworks.analysis;

import java.util.ArrayList;

import static java.lang.System.arraycopy;

/**
 * Utility functions for working with character arrays.
 */
public class CharArrayUtil {
    public static boolean equals(char[] buffer, char[] phrase) {
        if (buffer == null || phrase == null) return false;

        if (phrase.length != buffer.length) return false;
        for (int i = 0; i < phrase.length; i++) {
            if (buffer[i] != phrase[i]) return false;
        }
        return true;
    }

    public static boolean startsWith(char[] buffer, char[] phrase) {
        if (buffer == null || phrase == null) return false;

        if (phrase.length > buffer.length) return false;
        for (int i = 0; i < phrase.length; i++) {
            if (buffer[i] != phrase[i]) return false;
        }
        return true;
    }

    public static boolean endsWith(char[] buffer, char[] phrase) {
        if (buffer == null || phrase == null) return false;

        if (phrase.length > buffer.length) return false;
        for (int i = 1; i < phrase.length + 1; ++i) {
            if (buffer[buffer.length - i] != phrase[phrase.length - i]) return false;
        }
        return true;
    }

    public static char[] replaceWhitespace(char[] token, Character replaceWhitespaceWith) {
        char[] replaced = new char[token.length];
        int sourcePosition, destinationPosition;
        for (sourcePosition = 0, destinationPosition = 0; sourcePosition < token.length; sourcePosition++) {
            if (token[sourcePosition] == ' ') {
                if (replaceWhitespaceWith == null) {
                    continue;
                }
                replaced[destinationPosition++] = replaceWhitespaceWith;
            } else {
                replaced[destinationPosition++] = token[sourcePosition];
            }
        }
        if (sourcePosition != destinationPosition){
            char[] shorterReplaced = new char[destinationPosition];
            System.arraycopy(replaced, 0, shorterReplaced, 0, destinationPosition);
            return shorterReplaced;
        }
        return replaced;
    }

    public static boolean isSpaceChar(char ch) {
        return " \t\n\r".indexOf(ch) >= 0;
    }

    public static char[] getFirstTerm(char[] phrase) {
        if (phrase.length == 0)
            return new char[] {};

        int index = 0;
        while (index < phrase.length) {
            if (CharArrayUtil.isSpaceChar(phrase[index++])) {
                break;
            }
        }

       if (index == phrase.length)
           return phrase;

        char[] firstTerm = new char[index - 1];
        arraycopy(phrase, 0, firstTerm, 0, index - 1);
        return firstTerm;
    }
}
