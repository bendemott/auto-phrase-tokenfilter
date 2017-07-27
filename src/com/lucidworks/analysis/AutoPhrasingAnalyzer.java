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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.CharArraySet;
import java.io.StringReader;

/**
 * Implements an Analyzer for the AutoPhrasingTokenFilter to assist in unit testing it
 */
public class AutoPhrasingAnalyzer extends Analyzer {

    private CharArraySet phraseSets;
    private Character replaceWhitespaceWith = null;

    public AutoPhrasingAnalyzer(CharArraySet phraseSets) {
        this(phraseSets, null);
    }

    /**
     * Construct a new analyzer that uses WhitespaceTokenizer, with a single filter 'AutoPhrasingTokenFilter'
     * 
     * @param phraseSets 
     * @param replaceWhitespaceWith 
     */
    public AutoPhrasingAnalyzer(CharArraySet phraseSets, Character replaceWhitespaceWith) {
        this.phraseSets = phraseSets;
        this.replaceWhitespaceWith = replaceWhitespaceWith;
    }
    
    /**
     * Return the phrase set this analyzer was constructed with.
     * 
     * @return 
     */
    public CharArraySet getPhraseSets() {
        return this.phraseSets;
    }
    
    /**
     * 
     * 
     * @return 
     */
    public Character getReplaceWhitespaceWith() {
        return this.replaceWhitespaceWith;
    }

    @Override
    protected TokenStreamComponents createComponents(String s) {
        Tokenizer tokenizer = new WhitespaceTokenizer();
        tokenizer.setReader(new StringReader(s));
        AutoPhrasingTokenFilter tokenFilter =
                new AutoPhrasingTokenFilter(tokenizer, phraseSets);
        tokenFilter.setReplaceWhitespaceWith(replaceWhitespaceWith);
        return new TokenStreamComponents(tokenizer, tokenFilter);
    }
}
