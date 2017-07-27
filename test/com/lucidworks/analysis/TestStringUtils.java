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
import static org.junit.Assert.*;
import org.junit.*;

/**
 * Tests for our String Utilities
 * 
 * @author ben.demott
 */
public class TestStringUtils {
    
    /* TEST FILLING */
    @Test
    public void testFill() {
        assertEquals(" ", StringUtils.fill(1));
        assertEquals("  ", StringUtils.fill(2));
        assertEquals("   ", StringUtils.fill(3));
        assertEquals("    ", StringUtils.fill(4));
    }
    
    @Test
    public void testFillZero() {
        assertEquals("", StringUtils.fill(0));
    }
    
    @Test
    public void testFillChar() {
        assertEquals("!", StringUtils.fill(1, '!'));
        assertEquals("__", StringUtils.fill(2, '_'));
        assertEquals( "---", StringUtils.fill(3, '-'));
        assertEquals("****", StringUtils.fill(4, '*'));
    }
    
    /* TEST PADDING */
    @Test
    public void testPadNoPad() {
        assertEquals("abc", StringUtils.pad("abc", 3, ' '));
    }
    
    @Test
    public void testPadOne() {
        assertEquals("abc ", StringUtils.pad("abc", 4, ' '));
    }
    
    @Test
    public void testPadChar() {
        assertEquals("abc1", StringUtils.pad("abc", 4, '1'));
    }
    
    @Test
    public void testPadNeg() {
        assertEquals("abc", StringUtils.pad("abc", 1, '1'));
    }
    
    @Test
    public void testPadZero() {
        assertEquals("abc", StringUtils.pad("abc", 0, ' '));
    }
    
    @Test
    public void testPadLeft() {
        assertEquals(" abc", StringUtils.pad("abc", 1, 0, ' '));
    }
    
    @Test
    public void testPadRight() {
        assertEquals("abc ", StringUtils.pad("abc", 0, 1, ' '));
    }
    
    /* TEST JUSTIFY */
    @Test
    public void testJustifyLeft() {
        assertEquals("abc  ", StringUtils.ljust("abc", 5, ' '));
    }
    
    @Test
    public void testJustifyRight() {
        assertEquals("  abc", StringUtils.rjust("abc", 5, ' '));
    }
    
    /* TEST INSERT */
    @Test
    public void testInsert() {
        assertEquals("1234 5678 abcd", StringUtils.insert("1234 abcd", 5, "5678 "));
    }
    
    @Test
    public void testInsertIndexZero() {
        assertEquals("5678 1234 abcd", StringUtils.insert("1234 abcd", 0, "5678 "));
    }
    
    @Test
    public void testInsertAtEnd() {
        assertEquals("AB", StringUtils.insert("A", 1, "B"));
    }
    
    @Test
    public void testInsertOutOfBoundsSingle() {
        assertEquals("A B", StringUtils.insert("A", 2, "B"));
    }
    
    @Test
    public void testInsertIndexOutOfBounds() {
        assertEquals("1234 abcd efgh", StringUtils.insert("1234 abcd", 10, "efgh"));
    }

    @Test
    public void testInsertOutOfBoundsChar() {
        assertEquals("1234 abcd**efgh", StringUtils.insert("1234 abcd", 11, "efgh", '*'));
    }
    
    @Test
    public void testInsertOneChar() {
        assertEquals("1!234 abcd", StringUtils.insert("1234 abcd", 1, "!"));
    }
    
    
    /* TEST INSERT REPLACEMENT */
    @Test
    public void testInsertReplace() {
        assertEquals("1234 5678", StringUtils.insertReplace("1234 abcd", 5, "5678"));
    }
    
    @Test
    public void testInsertReplaceIndexZero() {
        assertEquals("5678 abcd", StringUtils.insertReplace("1234 abcd", 0, "5678"));
    }
    
    @Test
    public void testInsertReplaceIndexOutOfBounds() {
        assertEquals("1234 abcd efgh", StringUtils.insertReplace("1234 abcd", 10, "efgh"));
    }
    
    @Test
    public void testInsertReplaceEnd() {
        assertEquals("1234 !", StringUtils.insertReplace("1234", 4, " !"));
    }
    
    @Test
    public void testInsertReplaceEnd2() {
        assertEquals("1234 !", StringUtils.insertReplace("1234", 5, "!"));
    }

    @Test
    public void testInsertReplaceOutOfBoundsChar() {
        assertEquals("1234 abcd**efgh", StringUtils.insertReplace("1234 abcd", 11, "efgh", '*'));
    }
    
    @Test
    public void testInsertReplaceOneChar() {
        assertEquals("1!34 abcd", StringUtils.insertReplace("1234 abcd", 1, "!"));
    }
    
    @Test
    public void testInsertReplaceCharRange() {
        int start = 3;
        int end = 7;
        String replaced = StringUtils.insertReplaceCharRange("oh darn it", '*', start, end);
        assertEquals("oh **** it", replaced);
    }
    
    @Test
    public void testInsertReplaceCharRangeUnderflow() {
        // In the event stop is less than start, the string is returned unmodified.
        int start = 6;
        int stop = 5;
        String replaced = StringUtils.insertReplaceCharRange("1234 5678", ' ', start, stop);
        assertEquals("1234 5678", replaced);
    }
    
    @Test
    public void testInsertReplaceCharLengthNegative() {
        // In the event length is negative, the string is returned unmodified.
        int start = 6;
        int length = -2;
        String replaced = StringUtils.insertReplaceCharRange("1234 5678", ' ', start, length);
        assertEquals("1234 5678", replaced);
    }
    
    @Test
    public void testInsertReplaceCharLength() {
        int start = 3;
        int length = 4;
        String replaced = StringUtils.insertReplaceCharLength("oh darn it", '!', start, length);
        assertEquals("oh !!!! it", replaced);
    }
    
    @Test
    public void testInsertReplaceCharLengthEnd1() {
        int start = 5;
        int length = 4;
        String replaced = StringUtils.insertReplaceCharLength("1234 ...", '!', start, length);
        assertEquals("1234 !!!!", replaced);
    }
    
    @Test
    public void testInsertReplaceCharLengthEnd2() {
        int start = 4;
        int length = 1;
        String replaced = StringUtils.insertReplaceCharLength("1234!", '.', start, length);
        assertEquals("1234.", replaced);
    }
    
    @Test
    public void testInsertReplaceCharLengthOverflow() {
        int start = 6;
        int length = 4;
        String replaced = StringUtils.insertReplaceCharLength("1234", '_', start, length);
        assertEquals("1234  ____", replaced);
    }
    
    
    /* TEST charsPresentArray */
    @Test
    public void testCharsPresentArray() {
        Boolean[] result = StringUtils.charsPresentArray("1 3 5", new Character[]{' '});
        Assert.assertArrayEquals(new Boolean[]{false, true, false, true, false}, result);
    }
    
    @Test
    public void testCharsPresentArray2Chars() {
        Boolean[] result = StringUtils.charsPresentArray("1.3!5", new Character[]{'!', '.'});
        Assert.assertArrayEquals(new Boolean[]{false, true, false, true, false}, result);
    }
    
    @Test
    public void testCharsNotPresent() {
        Boolean[] result = StringUtils.charsNotPresentArray("1.3!5", new Character[]{'!', '.'});
        Assert.assertArrayEquals(new Boolean[]{true, false, true, false, true}, result);
    }
    
    /* Test punctuationPresentArray */
    @Test
    public void testPunctuationPresentArray() {
        Boolean[] result = StringUtils.punctuationPresentArray("1.&!5 ");
        Assert.assertArrayEquals(new Boolean[]{false, true, true, true, false, false}, result);
    }
    
    @Test
    public void testPunctuationPresentArrayOthers() {
        Boolean[] result = StringUtils.punctuationPresentArray(" 1.3!5 B ", new Character[]{' ', 'B'});
        Assert.assertArrayEquals(new Boolean[]{true, false, true, false, true, false, true, true, true}, result);
    }
}
