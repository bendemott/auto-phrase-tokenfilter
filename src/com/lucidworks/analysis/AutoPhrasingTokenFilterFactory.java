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

import org.apache.lucene.analysis.TokenStream;
//import org.apache.lucene.analysis.util.CharArraySet; DEPRECATED
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;
import java.util.Map;

public class AutoPhrasingTokenFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {

    private final AutoPhrasingParameters autoPhrasingParameters;
    private CharArraySet phraseSets;

    public AutoPhrasingTokenFilterFactory(Map<String, String> initArgs) {
        super(initArgs);

        SolrParams params = SolrParams.toSolrParams(new NamedList(initArgs));
        autoPhrasingParameters = new AutoPhrasingParameters(params);
    }

    @Override
    public void inform(ResourceLoader loader) throws IOException {
        String phraseSetFiles = autoPhrasingParameters.getPhraseSetFiles();
        boolean ignoreCase = autoPhrasingParameters.getIgnoreCase();

        if (phraseSetFiles != null)
            phraseSets = getWordSet(loader, phraseSetFiles, ignoreCase);
    }

    @Override
    public TokenStream create(TokenStream input) {
        Character replaceWhitespaceWith = autoPhrasingParameters.getReplaceWhitespaceWith();

        AutoPhrasingTokenFilter autoPhraseFilter = new AutoPhrasingTokenFilter(input, phraseSets);
        autoPhraseFilter.setReplaceWhitespaceWith(replaceWhitespaceWith);
        return autoPhraseFilter;
    }

}
