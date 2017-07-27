package com.lucidworks.analysis;

import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.CharArraySet;

import java.util.Arrays;
import org.apache.lucene.analysis.TokenStream;

/**
 * This class is a JUnit test case.  The test class will automatically be discovered by mavens
 * build process.
 * 
 * Any method beginning with 'test' is a test case.  
 * 
 * See documentation for BaseTokenStreamTestCase here:
 *  https://lucene.apache.org/core/6_5_0/test-framework/org/apache/lucene/analysis/BaseTokenStreamTestCase.html
 * 
 * The method 'assertAnalyzesTo' is a method of the parent class BaseTokenStreamTestCase
 * and is used for the vast majority of test cases.  Note that this method has many signatures
 * for testing different aspects of the analyzer.
 * 
 * Example debug string:
 
          original:  hello my name is donald duck I lived in places
         increment:    1        1           2         1        1
            tokens:  hello ** name ** donald duck * live  ** place
         positions:  -----    ----    -----------   ====    ------
           lengths:    1       1           2         1        1
                     01234567890123456789012345678901234567890123456
                              10        20        30        40     
 */
public class TestAutoPhrasingTokenFilter extends BaseTokenStreamTestCase {

    /**
     * Generate phrases to be used for each test from a list of phrases.
     * 
     * @param phrases
     * @return 
     */
    private static CharArraySet getPhraseSets(String... phrases) {
        return new CharArraySet(Arrays.asList(phrases), false);
    }
    
    public static void assertAnalyzesTo(Analyzer a, String input, String[] output, int[] startOffsets, int[] endOffsets, int[] posIncrements) throws IOException {
        assertAnalyzesTo(a, input, output, startOffsets, endOffsets, null, posIncrements, null);
    }
    
    
    public static void assertAnalyzesTo(Analyzer a, String input, String[] output, int startOffsets[], int endOffsets[], String types[], int posIncrements[], int posLengths[]) throws IOException {
        
        try {
            BaseTokenStreamTestCase.assertAnalyzesTo(a, input, output, startOffsets, endOffsets, types, posIncrements, posLengths);
        } catch (AssertionError e) {
            a.close();
            // Create a second instance of the Analyzer, analyzers tokenStreams cannot be reused.
            AutoPhrasingAnalyzer ap = (AutoPhrasingAnalyzer) a;
            CharArraySet phrases = ap.getPhraseSets();
            AutoPhrasingAnalyzer a2 = new AutoPhrasingAnalyzer(phrases, ' ');
            
            if(a2==null) {
                // if we weren't able to reconstruct the Analyzer, just continue throwing the
                // original error.
                throw e;
            }
            
            TokenStream stream = a2.tokenStream("", new StringReader(input));

            String debugging = TokenStreamDebug.debugTokenStream(stream, input);
 
            throw new AssertionError(debugging + "\n" + e.getMessage(), e); 
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new AssertionError(e.getMessage(), e);
        }
    }

    public void testNoPhrasesNoReplaceNoInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {});
    }

    public void testNoPhrasesNoReplaceOneCharInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "A";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input, // input - (THE INPUT STRING TO TOKENIZE)
                new String[] {"A"},       // output - (THE EXPECTED OUTPUT TOKENS)
                new int[] {0},            // startOffsets - (the position of the first character corresponding to this token in the source text)
                new int[] {1},            // endOffsets - (one greater than the position of the last character corresponding to this token in the source text)
                new int[] {1});           // positionIncrements - (The token increment position between each token... 
                                          //                      0 = Occupy the same position as the previous token
                                          //                      1 = (normal) occupy the position next to the previous token
                                          //                     1+ = occupy a position some tokens after the previous token... Used when you want to prevent phrase matching.)
        
    }

    public void testNoPhrasesNoReplaceOneWordInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "word";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"word"},
                new int[] {0},
                new int[] {4},
                new int[] {1});
    }

    public void testNoPhrasesNoReplaceTwoCharsInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "A B";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"A", "B"},
                new int[] {0, 2},
                new int[] {1, 3},
                new int[] {1, 1});
    }

    public void testNoPhrasesNoReplaceTwoWordsInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "two words";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"two", "words"},
                new int[] {0, 4},
                new int[] {3, 9},
                new int[] {1, 1});
    }

    public void testNoPhrasesWithReplaceNoInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {});
    }

    public void testNoPhrasesWithReplaceOneCharInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "A";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"A"},
                new int[] {0},
                new int[] {1},
                new int[] {1});
    }

    public void testNoPhrasesWithReplaceOneWordInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "word";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"word"},
                new int[] {0},
                new int[] {4},
                new int[] {1});
    }

    public void testNoPhrasesWithReplaceTwoCharsInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "A B";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"A", "B"},
                new int[] {0, 2},
                new int[] {1, 3},
                new int[] {1, 1});
    }

    public void testNoPhrasesWithReplaceTwoWordsInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "two words";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"two", "words"},
                new int[] {0, 4},
                new int[] {3, 9},
                new int[] {1, 1});
    }

    public void testOnePhraseNoReplaceNoInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {});
    }

    public void testOnePhraseNoReplaceOneCharInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "A";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"A"},
                new int[] {0},
                new int[] {1},
                new int[] {1});
    }

    public void testOnePhraseNoReplaceOneWordInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "word";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"word"},
                new int[] {0},
                new int[] {4},
                new int[] {1});
    }

    public void testOnePhraseNoReplaceTwoCharsInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "A B";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        
        assertAnalyzesTo(analyzer, input,
                new String[] {"A", "B"},
                new int[] {0, 2},
                new int[] {1, 3},
                new int[] {1, 1});
    }

    public void testOnePhraseNoReplaceTwoWordsInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "two words";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"two", "words"},
                new int[] {0, 4},
                new int[] {3, 9},
                new int[] {1, 1});
    }

    public void testOnePhraseWithReplaceNoInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {});
    }

    public void testOnePhraseWithReplaceOneCharInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "A";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"A"},
                new int[] {0},
                new int[] {1},
                new int[] {1});
    }

    public void testOnePhraseWithReplaceOneWordInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "word";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"word"},
                new int[] {0},
                new int[] {4},
                new int[] {1});
    }

    public void testOnePhraseWithReplaceTwoCharsInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "A B";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"A", "B"},
                new int[] {0, 2},
                new int[] {1, 3},
                new int[] {1, 1});
    }

    public void testOnePhraseWithReplaceTwoWordsInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "two words";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"two", "words"},
                new int[] {0, 4},
                new int[] {3, 9},
                new int[] {1, 1});
    }

    public void testOnePhraseNoReplacePartialPhraseInputStart() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "wheel";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheel"},
                new int[] {0},
                new int[] {5},
                new int[] {1});
    }

    public void testOnePhraseNoReplacePartialPhraseInputEnd() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "chair";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"chair"},
                new int[] {0},
                new int[] {5},
                new int[] {1});
    }

    public void testOnePhraseNoReplacePhraseMatch() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "wheel chair";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheel chair"},
                new int[] {0},
                new int[] {11},
                new int[] {1});
    }

    public void testOnePhraseWithReplacePartialPhraseInputStart() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "wheel";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheel"},
                new int[] {0},
                new int[] {5},
                new int[] {1});
    }

    public void testOnePhraseWithReplacePartialPhraseInputEnd() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "chair";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"chair"},
                new int[] {0},
                new int[] {5},
                new int[] {1});
    }

    public void testPhraseWithPrecedingNonPhraseWord() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "some wheel chair";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"some", "wheel chair"},
                new int[] {0, 5},
                new int[] {4, 16},
                new int[] {1, 1});
        /*
            original: some wheel chair
           increment:   1       1     
              tokens: some wheel chair
           positions: ---- -----------
             lengths:   1       1     
            sequence:   1       2     
                      0123456789012345
                               10   
        */
    }

    public void testPhraseWithFollowingNonPhraseWord() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "wheel chair sauce";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheel chair", "sauce"},
                new int[] {0, 12},
                new int[] {11, 17},
                new int[] {1, 1});
        
        /*
            original: wheel chair sauce
           increment:      1        1  
              tokens: wheel chair sauce
           positions: ----------- -----
             lengths:      1        1  
            sequence:      1        2  
                      01234567890123456
                               10      
        */
    }

    public void testTwoPhrases() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair", "foo bar");
        final String input = "wheel chair foo bar";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheel chair", "foo bar"},
                new int[] {0, 12},
                new int[] {11, 19},
                new int[] {1, 1});
        /*
            original: wheel chair foo bar
           increment:      1         1   
              tokens: wheel chair foo bar
           positions: ----------- -------
             lengths:      1         1   
                      0123456789012345678
                               10      
         */
    }

    public void testTwoPhrasesSomethingInBetween() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair", "foo bar");
        final String input = "wheel chair hello foo bar";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheel chair", "hello", "foo bar"},
                new int[] {0, 12, 18},
                new int[] {11, 17, 25},
                new int[] {1, 1, 1});
        /*
            original: wheel chair hello foo bar
           increment:      1        1      1   
              tokens: wheel chair hello foo bar
           positions: ----------- ----- -------
             lengths:      1        1      1   
                      0123456789012345678901234
                               10        20   
        */
    }

    public void testTwoPhrasesTwoWordsInBetween() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair", "foo bar");
        final String input = "wheel chair hello there foo bar";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheel chair", "hello", "there", "foo bar"},
                new int[] {0, 12, 18, 24},
                new int[] {11, 17, 23, 31},
                new int[] {1, 1, 1, 1});
        /*
            original: wheel chair hello there foo bar
           increment:      1        1     1      1   
              tokens: wheel chair hello there foo bar
           positions: ----------- ----- ----- -------
             lengths:      2        1     1      2   
                      0123456789012345678901234567890
                               10        20        30
        */
    }

    public void testTwoPhrasesPrecedingWord() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair", "foo bar");
        final String input = "hello wheel chair foo bar";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"hello", "wheel chair", "foo bar"},
                new int[] {0, 6, 18}, // startOffsets
                new int[] {5, 17, 25},// endOffsets
                new int[] {1, 1, 1}); // posIncrements
         
        /*
            original: hello wheel chair foo bar
           increment:   1        1         1   
              tokens: hello wheel chair foo bar
           positions: ----- ----------- -------
             lengths:   1        2         2   
                      0123456789012345678901234
                               10        20   
        */
    }

    public void testTwoCompetingPhrases() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair", "chair alarm");
        final String input = "wheel chair alarm";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheel chair", "alarm"},
                new int[] {0, 12},
                new int[] {11, 17},
                new int[] {1, 1});
            /*
                original: wheel chair alarm
               increment:      1        1  
                  tokens: wheel chair alarm
               positions: ----------- -----
                 lengths:      2        1  
                          01234567890123456
                                   10    
            */
    }

    public void testTwoCompetingPhrasesBothPhrases() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair", "chair alarm");
        final String input = "wheel chair chair alarm";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheel chair", "chair alarm"},
                new int[] {0, 12},
                new int[] {11, 23},
                new int[] {1, 1});
            /*
                original: wheel chair chair alarm
               increment:      1           1     
                  tokens: wheel chair chair alarm
               positions: ----------- -----------
                 lengths:      2           2     
                          01234567890123456789012
                                   10        20  
            */
        
    }

    public void testMultiplePhraseMatchUsesLongest() throws Exception {
        final CharArraySet phrases = getPhraseSets("corn bread", "corn bread dressing");
        final String input = "corn bread dressing";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"corn bread dressing"},
                new int[] {0},
                new int[] {19},
                new int[] {1});
        /*
            original: corn bread dressing
           increment:          1         
              tokens: corn bread dressing
           positions: -------------------
             lengths:          3         
            sequence:          1         
                      0123456789012345678
                               10  
        */
    }

    public void testFuzzyMatchingMultipleTokens() throws Exception {
        final CharArraySet phrases = getPhraseSets("corn TOKEN? TOKEN? bread");
        final String input = "corn on my bread";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"corn bread"},
                new int[] {0},
                new int[] {16},
                new int[] {1});
    }

    public void testFuzzyMatchingSingleToken() throws Exception {
        final CharArraySet phrases = getPhraseSets("corn TOKEN? bread");
        final String input = "corn or bread";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"corn bread"},
                new int[] {0},
                new int[] {13},
                new int[] {1});
    }

    public void testFuzzyMatchingTokenOptional() throws Exception {
        final CharArraySet phrases = getPhraseSets("corn TOKEN? bread");
        final String input = "corn bread";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"corn bread"},
                new int[] {0},
                new int[] {10},
                new int[] {1});
        /*
            original: corn bread
           increment:      1    
              tokens: corn bread
           positions: ----------
             lengths:      2    
                      0123456789
                               10
        */
    }

    public void testFuzzyMatchingTokenWithMultipleWordsAfter() throws Exception {
        final CharArraySet phrases = getPhraseSets("first TOKEN? second third");
        final String input = "first or second third";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"first second third"},
                new int[] {0},
                new int[] {21},
                new int[] {1});
        /*
            original: first or second third
           increment:          1           
              tokens: first second third   
           positions: ---------------------
             lengths:          4           
                      012345678901234567890
                               10        20
        */
    }

    public void testFuzzyMatchingOptionalTokenWithMultipleWordsAfter() throws Exception {
        final CharArraySet phrases = getPhraseSets("first TOKEN? second third");
        final String input = "first second third";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"first second third"},
                new int[] {0},
                new int[] {18},
                new int[] {1});
        /*
           original: first second third
          increment:          1        
             tokens: first second third
          positions: ------------------
            lengths:          1        
           sequence:          1        
                     012345678901234567
                              10  
        */
    }

    public void testFuzzyMatchingDoesNotPrematurelyEnd() throws Exception {
        final String WILDCARD = AutoPhrasingTokenFilter.WILDCARD_TOKEN;
        final String phrase = String.format("first %s %s %s second", WILDCARD, WILDCARD, WILDCARD);
        final CharArraySet phrases = getPhraseSets(phrase);
        final String input = "first third fourth fifth"; 
        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"first", "third", "fourth", "fifth"},
                new int[] {0, 6, 12, 19},
                new int[] {5, 11, 18, 24},
                new int[] {1, 1, 1, 1});

    }

    public void testEarlyMatchesDoNotOverrideChanges() throws Exception {
        final CharArraySet phrases = getPhraseSets("one two three");
        final String input = "one two four";
        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, ' ');
        assertAnalyzesTo(analyzer, input,
                new String[] {"one", "two", "four"},
                new int[] {0, 4, 8},
                new int[] {3, 7, 12},
                new int[] {1, 1, 1});

    }
    
    public void testTrigramExtraPhrasePickLongest() throws Exception {
        final CharArraySet phrases = getPhraseSets("business intelligence developer", "business intelligence");
        final String input = "business intelligence developer";
        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"business_intelligence_developer"},
                new int[] {0},
                new int[] {31},
                new int[] {1});
        
        /* original: business intelligence developer
          increment:                1               
             tokens: business intelligence developer
          positions: -------------------------------
            lengths:                1               
           sequence:                1               
                     0123456789012345678901234567890
                              10        20        30
        */

    }
    
    public void testAnalyzerReuse() throws Exception {
        final CharArraySet phrases = getPhraseSets("business administrator", "devops engineer");
        final String input1 = "business administrator";
        final String input2 = "devops engineer";
        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input1,
                new String[] {"business_administrator"},
                new int[] {0},
                new int[] {22},
                new int[] {1});
        
        assertAnalyzesTo(analyzer, input2,
                new String[] {"devops_engineer"},
                new int[] {0},
                new int[] {15},
                new int[] {1});
    }

}
