package com.lucidworks.analysis;

import junit.framework.TestCase;

public class TestStemmer extends TestCase {

    public void testStemEmptyInput() throws Exception {
        char[] input = "".toCharArray();
        char[] expected = "".toCharArray();

        char[] actual = Stemmer.stem(input);
        assertEquals(new String(expected), new String(actual));
    }

    public void testStemNullInput() throws Exception {
        char[] actual = Stemmer.stem(null);
        assertNull(actual);
    }

    public void testStemOneWordInputNoStem() throws Exception {
        char[] input = "wheel".toCharArray();
        char[] expected = "wheel".toCharArray();

        char[] actual = Stemmer.stem(input);
        assertEquals(new String(expected), new String(actual));
    }

    public void testStemOneWordInputWithStem() throws Exception {
        char[] input = "wheels".toCharArray();
        char[] expected = "wheel".toCharArray();

        char[] actual = Stemmer.stem(input);
        assertEquals(new String(expected), new String(actual));
    }

    public void testStemTwoWordInputNoStem() throws Exception {
        char[] input = "wheel chair".toCharArray();
        char[] expected = "wheel chair".toCharArray();

        char[] actual = Stemmer.stem(input);
        assertEquals(new String(expected), new String(actual));
    }

    public void testStemTwoWordInputWithStem() throws Exception {
        char[] input = "wheel chairs".toCharArray();
        char[] expected = "wheel chair".toCharArray();

        char[] actual = Stemmer.stem(input);
        assertEquals(new String(expected), new String(actual));
    }

    public void testStemOneWordInputWithStemNoLengthChange() throws Exception {
        char[] input = "happy".toCharArray();
        char[] expected = "happi".toCharArray();

        char[] actual = Stemmer.stem(input);
        assertEquals(new String(expected), new String(actual));
    }
}