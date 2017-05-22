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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Only calls logDebug is debug logging is enabled to avoid char[] to String casts and String.formats.
 * It exposes overloads instead of a single method with Object... args because char[].toString()
 * doesn't get the string. A new string must be created.
 */
public class LazyLog {
    private static final Logger Log = LoggerFactory.getLogger(AutoPhrasingTokenFilter.class);

    public static void logDebug (String format) {
        Log.debug(format);
    }

    public static void logDebug (String format, char[] arg) {
        if (Log.isDebugEnabled()) {
            Log.debug(String.format(format, arg == null ? "NULL" : new String(arg)));
        }
    }

    public static void logDebug (String format, int arg) {
        if (Log.isDebugEnabled()) {
            Log.debug(String.format(format, arg));
        }
    }

    public static void logDebug (String format, char[] arg0, int arg1, int arg2) {
        if (Log.isDebugEnabled()) {
            Log.debug(String.format(format, new String(arg0), arg1, arg2));
        }
    }

    public static void logDebug (String format, StringBuffer arg) {
        if (Log.isDebugEnabled()) {
            Log.debug(String.format(format, arg));
        }
    }
}
