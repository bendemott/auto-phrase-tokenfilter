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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.CharArrayMap;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.arraycopy;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
/**
 * Performs "auto phrasing" on a token stream. Auto phrases refer to sequences of tokens that
 * are meant to describe a single thing and should be searched for as such. When these phrases
 * are detected in the token stream, a single token representing the phrase is emitted rather than
 * the individual tokens that make up the phrase. The filter supports overlapping phrases.
 * 
 * The AutoPhrasing filter can be combined with a synonym filter to handle cases in which prefix or
 * suffix terms in a phrase are synonymous with the phrase, but where other parts of the phrase are
 * not.
 * 
 * The filter will produce single-tokens where no phrase-match was found.  In this case is behaves
 * like a pass-through filter that does not affect tokens or their positions.
 * 
 */

@SuppressWarnings({"unchecked", "PrimitiveArrayArgumentToVariableArgMethod"})
public final class AutoPhrasingTokenFilter extends TokenFilter {

    private CharTermAttribute charTermAttr;
    private PositionIncrementAttribute positionIncrementAttr;
    private OffsetAttribute offsetAttr;
    private PositionLengthAttribute positionLengthAttr;

    // replaceWhitespaceWith stores the value passed into this filter during construction,
    // white-space in the token will be replaced with this character. (space) is recommended.
    private Character replaceWhitespaceWith = null;
    private Version luceneMatchVersion;

    // maps the first word in each auto phrase to all phrases that start with that word
    private final CharArrayMap<CharArraySet> phraseMapFirstWordToPhrases;

    private final ArrayList<char[]> tokenTerms = new ArrayList<>();
    private final ArrayList<Integer> tokenEndPositions = new ArrayList<>();
    private final ArrayList<Integer> tokenStartPositions = new ArrayList<>();
    private final ArrayList<Integer> tokenLengths = new ArrayList<>();
    private final ArrayList<Integer> tokenIncrements = new ArrayList<>();
    
    // currentTokenIdx acts as a pointer to the iterator through the input stream.
    // Because streams are state-machines, we need to keep track where we are in the sourc
    // stream for matching purposes.  We ingest the ENTIRE input stream at once... so we can't use
    // the streams current state or internal iterator.
    private int currentTokenIdx = -1;
    
    // Wildcard tokens can be used to do skip-gram matching.  This token should be present in the
    // phrase definition where you want to support an optional character.
    public static final String WILDCARD_TOKEN = "TOKEN?";
    
    // PHRASE_SEPARATOR defines the string that is used to separate terms that compose phrases to 
    // configure this filter.
    public static final String PHRASE_SEPARATOR = " ";

    /**
     * Constructor
     */
    public AutoPhrasingTokenFilter(TokenStream input, CharArraySet phraseSet) {
        super(input);

        final int estimatedPhraseMapEntries = 100;
        phraseMapFirstWordToPhrases =
                new CharArrayMap<>(estimatedPhraseMapEntries, false);

        this.currentTokenIdx = -1;
        initializePhraseMap(phraseSet);
        initializeAttributes();
    }

    /**
     * Build a data structure that maps the first term in each phrase to the full phrase.
     * You can think of this as a simplistic trie structure (even though its not) to find
     * matches by prefix.
     * 
     * @param phraseSet 
     */
    private void initializePhraseMap(CharArraySet phraseSet) {
        final int EstimatedPhrasesPerFirstWord = 5;

        for (Object aPhrase : phraseSet) {
            char[] phrase = (char[])aPhrase;
            char[] firstWord = CharArrayUtil.getFirstTerm(phrase);
            CharArraySet phrases = phraseMapFirstWordToPhrases.get(firstWord, 0, firstWord.length);
            if (phrases == null) {
                phrases = new CharArraySet(EstimatedPhrasesPerFirstWord, false);
                phraseMapFirstWordToPhrases.put(firstWord, phrases);
            }
            phrases.add(phrase);
        }
    }

    
    /**
     * Get references to the streams attribute classes.
     */
    private void initializeAttributes() {
        // Term attribute tells us what the String contents of the token actually is.
        this.charTermAttr = addAttribute(CharTermAttribute.class);
        // Offset attr tells us where a single token in the stream starts and ends, the positions
        // are a reference to the ORIGINAL string before it was tokenized.  These positions
        // should be preserved through the entire token/filter chain.  If this is not implemented
        // correctly automated tests, and highlighting will not function correctly.
        this.offsetAttr = addAttribute(OffsetAttribute.class);
        // position increment tells us which position in the token stream the token actually
        // occupies, position increments of 0 mean a token is a synonym.
        this.positionIncrementAttr = addAttribute(PositionIncrementAttribute.class);
    }
    
    
    /**
     * Construct data sets that will cache token attributes on the input stream.
     */
    private void initializeInStreamData(TokenStream stream) throws IOException {
        while(input.incrementToken()) {
            int startPos = offsetAttr.startOffset();
            int endPos = offsetAttr.endOffset();
            int increment = positionIncrementAttr.getPositionIncrement();
            tokenIncrements.add(increment);
            tokenStartPositions.add(startPos);
            tokenEndPositions.add(endPos);

            // Char arrays are not immutable, so make a copy so we don't have to worry about
            // changes to the input stream.
            char[] termBuf = charTermAttr.buffer();
            char[] nextTok = new char[charTermAttr.length()];
            arraycopy(termBuf, 0, nextTok, 0, charTermAttr.length());

            tokenTerms.add(nextTok);
        }
    }

    /**
     * Set the character used to replace whitespace in output tokens that are phrases
     * 
     * @param replaceWhitespaceWith 
     */
    public void setReplaceWhitespaceWith(Character replaceWhitespaceWith) {
        this.replaceWhitespaceWith = replaceWhitespaceWith;
    }

    /**
     * Reset token stream attributes, this makes the token stream iterable again.
     * 
     * @throws IOException 
     */
    @Override
    public void reset() throws IOException {
        tokenTerms.clear();
        tokenEndPositions.clear();
        tokenStartPositions.clear();
        tokenIncrements.clear();
        currentTokenIdx = -1;
        super.reset();
    }

    /**
     * This method is part of the TokenStream api for Solr.
     * This method can be called to increment the internal token attributes
     * until the function returns False (there are no more tokens)
     * 
     * @return Return a boolean, indicating if there are more tokens to consume
     * @throws IOException 
     */
    @Override
    public boolean incrementToken() throws IOException {
        // Clear any set attributes to reset their values.
        // Attributes are stateful per-each-token, so they should be reset in order to ensure the
        // clean state of the attributes.
        clearAttributes();
        
        // We want to collect all the upstream (input) tokens only once.
        if(currentTokenIdx < 0) {
            initializeInStreamData(input);
            currentTokenIdx = 0;
        }
        
        if (currentTokenIdx >= tokenTerms.size() || tokenTerms.isEmpty()) {
            // we've read all tokens out of the input stream
            return false;
        }

        // Get the first word in the token stream, and check to see if any phrases start with this
        // word... 
        char[] firstTerm = tokenTerms.get(currentTokenIdx);
        CharArraySet potentialPhraseMatches = phraseMapFirstWordToPhrases.get(firstTerm, 0, firstTerm.length);

        if (potentialPhraseMatches == null) {
            LazyLog.logDebug("No potential phrase matches.");
            emitToken();
            return true;
        }

        // foreach potential phrase match look ahead in the queue and find the first match
        // remove those, make a phrase and emit it
        // Phrases can be exact, or can have "TOKEN" to represent a (potentially not present) generic token
        // so that you can match phrases like pay TOKEN bill on "pay bill," "pay my bill," or "pay your bill."
        char[] phraseMatch = null;
        int phraseWordsUsed = 0;
        for (Object aPotentialPhraseMatch : potentialPhraseMatches) {
            char[] potentialPhraseMatch = (char[])aPotentialPhraseMatch;
            String[] potentialPhraseWords = new String(potentialPhraseMatch).split(PHRASE_SEPARATOR);

            //Figure out how many TOKEN options are present, since these are all optional
            int tokenCount = 0;
            for (String potentialPhraseWord : potentialPhraseWords) {
                if (WILDCARD_TOKEN.equalsIgnoreCase(potentialPhraseWord)) {
                    tokenCount++;
                }
            }

            //If the number of non-optional words left in the phrase is longer than the number of unused tokens left,
            //then it's not possible to match, so go to the next check.
            if (potentialPhraseWords.length - tokenCount > tokenTerms.size() - currentTokenIdx)
                continue;

            int potentialPhraseWordsUsed = matches(new ArrayList<>(Arrays.asList(potentialPhraseWords)), tokenTerms.subList(currentTokenIdx, tokenTerms.size()));
            boolean matches = potentialPhraseWordsUsed > 0;
            if (matches && (phraseMatch == null || potentialPhraseWordsUsed > phraseWordsUsed)) {
                potentialPhraseMatch = String.valueOf(potentialPhraseMatch).replaceAll("[tT][oO][kK][eE][nN]\\? ", "").toCharArray();
                LazyLog.logDebug("Found potential longest phrase match for '%s'.", potentialPhraseMatch);
                phraseMatch = new char[potentialPhraseMatch.length];
                arraycopy(potentialPhraseMatch, 0, phraseMatch, 0, potentialPhraseMatch.length);
                // integer phraseWordsUsed, tells us how many tokens were used from tokenTerms to 
                // match the longest potential phrase possible.
                phraseWordsUsed = potentialPhraseWordsUsed;
            }
        }
        
        // If we found a phrase match, emit the phrase match
        if (phraseMatch != null) {
            LazyLog.logDebug("Found phrase match for '%s'.", phraseMatch);
            
            // phraseMatch is a new token comprising multiple tokens from the input stream.
            emitToken(phraseMatch, phraseWordsUsed);
            return true;
        }

        LazyLog.logDebug("No phrase matches found, emitting single token.");
        emitToken();
        return true;
    }

    //Returns the number of the tokenTerms are consumed.  If -1, then there was no match, so none were used.
    // TODO if we are to use this method, it must use the spanTokens currentTokenIdx...
    // Because multiple tokens can occupy the same 'position' .. it must also return the indexes of the
    // tokens it matched on so length and other attributes can be calculated correctly.
    // ALSO... if this is to calculate matches correctly, it cannot simply assume tokens are sequential
    // tokens can be synonyms and multiple tokens can occupy the same position... this means that 
    // for this to work correctly it must consider the token graph correctly. Any token occupying the
    // current position is a valid consideration.
    private int matches(List<String> potentialPhraseWords, List<char[]> unusedTokens) {
        if (potentialPhraseWords == null || unusedTokens == null)
            return -1;

        //If we've come to the end of the phrase, then it's a match.
        if (potentialPhraseWords.size() < 1)
            return 0;

        if (unusedTokens.size() < 1 && potentialPhraseWords.size() >= 1) {
            for (String potentialPhrase : potentialPhraseWords) {
                if (!WILDCARD_TOKEN.equalsIgnoreCase(potentialPhrase))
                    return -1;
            }
            return 0;
        }

        if (WILDCARD_TOKEN.equalsIgnoreCase(potentialPhraseWords.get(0))) {
            //Option 1 is that the TOKEN? is skipped
            int option1 = matches(potentialPhraseWords.subList(1, potentialPhraseWords.size()), unusedTokens);
            //Option 2 is that the TOKEN? consumes something in the unused Tokens list
            int option2 = matches(potentialPhraseWords.subList(1, potentialPhraseWords.size()), unusedTokens.subList(1, unusedTokens.size()));
            if (option1 < 0 && option2 < 0) {
                return -1;
            } else {
                //Option 1 is that TOKEN? is skipped, so we don't increment the number of tokens "consumed" if we use Option 1.
                return option1 > option2 ? option1 : option2 + 1;
            }

        } else {
            if (CharArrayUtil.equals(potentialPhraseWords.get(0).toCharArray(), unusedTokens.get(0))) {
                int response = matches(potentialPhraseWords.subList(1, potentialPhraseWords.size()), unusedTokens.subList(1, unusedTokens.size()));
                if (response == -1)
                    return -1;
                else
                    return response + 1;

            } else
                return -1;
        }

    }
    
    /**
     * Emit the current token as-is, with its original attributes.
     */
    private void emitToken() {
        charTermAttr.setEmpty();
        char[] token = tokenTerms.get(currentTokenIdx);

        emitToken(token, 1);
    }

    /**
     * Emit token, correctly setting its length, position, and offsets.
     * It's very important the offsets are set correctly, otherwise the test suite will fail.
     * In Solr 5 changes were made to the automated tests that require the last token end position
     * be equal to the original strings length.
     */
    private void emitToken(char[] token, int spanTokens) {
        int lastTokenIdx = currentTokenIdx + spanTokens -1;
        token = CharArrayUtil.replaceWhitespace(token, replaceWhitespaceWith);
        
        int startOffset = tokenStartPositions.get(currentTokenIdx);
        int endOffset = tokenEndPositions.get(lastTokenIdx);
        int increment = tokenIncrements.get(currentTokenIdx);
        
        charTermAttr.append(new String(token));
        offsetAttr.setOffset(startOffset, endOffset);
        positionIncrementAttr.setPositionIncrement(increment);
        
        currentTokenIdx += Math.max(spanTokens, 1);
    }
}
