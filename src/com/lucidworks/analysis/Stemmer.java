package com.lucidworks.analysis;

import org.tartarus.snowball.ext.PorterStemmer;

/**
 * Uses the porterStemmer to stem phrases
 */
public class Stemmer {
    private static PorterStemmer porterStemmer = new PorterStemmer();

    public static char[] stem(char[] term) {
        if (term == null) return null;

        porterStemmer.setCurrent(term, term.length);
        porterStemmer.stem();

        char[] currentBuffer = porterStemmer.getCurrentBuffer();
        char[] output = new char[porterStemmer.getCurrentBufferLength()];
        for (int i = 0 ; i < output.length ; ++i) {
            output[i] = currentBuffer[i];
        }

        return output;
    }
}
