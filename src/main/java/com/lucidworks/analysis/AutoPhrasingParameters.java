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

import org.apache.solr.common.params.SolrParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstracts the parameters (and the parsing of those parameters) used by the auto phrasing parser
 */
@SuppressWarnings("FieldCanBeLocal")
public class AutoPhrasingParameters {
    private final String DefaultDownstreamParser = "lucene";
    private final Character DefaultReplaceWhitespaceWith = null;
    private final boolean DefaultIgnoreCase = true;
    public AutoPhrasingParameters(SolrParams solrParams) {
        if (solrParams == null) {
            downstreamParser = DefaultDownstreamParser;
            replaceWhitespaceWith = DefaultReplaceWhitespaceWith;
            ignoreCase = DefaultIgnoreCase;
            phraseSetFiles = null;
        } else {
            setDownstreamParser(solrParams.get("defType", DefaultDownstreamParser));
            setReplaceWhitespaceWith(solrParams.get("replaceWhitespaceWith", null));
            setIgnoreCase(solrParams.getBool("ignoreCase", DefaultIgnoreCase));
            setPhraseSetFiles(solrParams.get("phrases"));
        }
    }

    /**
     * Getter for a comma separated string of file names containing auto phrases
     * @return a list of the parsed individual file names
     */
    public List<String> getIndividualPhraseSetFiles() {
        if (phraseSetFiles == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();
        for (String file : phraseSetFiles.split("(?<!\\\\),")) {
            result.add(file.replaceAll("\\\\(?=,)", ""));
        }

        return result;
    }

    public String getPhraseSetFiles() {
        return this.phraseSetFiles;
    }

    /**
     * Setter for a comma separated string of files containing autophrase entries
     * @param phraseSetFiles The comma separated string of files containing autophrase entries
     */
    public void setPhraseSetFiles(String phraseSetFiles) {
        this.phraseSetFiles = phraseSetFiles;
    }

    public Character getReplaceWhitespaceWith() {
        return replaceWhitespaceWith;
    }

    public void setReplaceWhitespaceWith(String replaceWhitespaceWith) {
        if (replaceWhitespaceWith != null && replaceWhitespaceWith.length() > 0)
            this.replaceWhitespaceWith = replaceWhitespaceWith.charAt(0);
        else
            this.replaceWhitespaceWith = DefaultReplaceWhitespaceWith;
    }

    public boolean getIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public String getDownstreamParser() {
        return downstreamParser;
    }

    public void setDownstreamParser(String downstreamParser) {
        this.downstreamParser = downstreamParser;
    }

    private String downstreamParser;
    private Character replaceWhitespaceWith;
    private boolean ignoreCase;
    private String phraseSetFiles;
}
