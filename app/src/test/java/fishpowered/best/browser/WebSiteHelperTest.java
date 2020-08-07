package fishpowered.best.browser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class WebSiteHelperTest {

    @Test
    public void testGetVideoThumbnailFromWebPage(){
        // https://gist.github.com/Glurt/ea11b690ba4b1278e049
        assertEquals("https://img.youtube.com/vi/0EcJcbniOC0/hqdefault.jpg", WebSiteHelper.getVideoThumbnailUrlFromWebPage("https://m.youtube.com/watch?v=0EcJcbniOC0", null));
        assertEquals("https://img.youtube.com/vi/0EcJcbniOC0/hqdefault.jpg", WebSiteHelper.getVideoThumbnailUrlFromWebPage("https://m.youtube.com/watch?v=0EcJcbniOC0&foo", null));
        assertEquals("https://img.youtube.com/vi/0EcJcbniOC0/hqdefault.jpg", WebSiteHelper.getVideoThumbnailUrlFromWebPage("https://m.youtube.com/watch?v=0EcJcbniOC0#foo", null));
        assertEquals("https://img.youtube.com/vi/0EcJcbniOC0/hqdefault.jpg", WebSiteHelper.getVideoThumbnailUrlFromWebPage("https://youtube.com/watch?v=0EcJcbniOC0", null));
        assertEquals("https://img.youtube.com/vi/w9R42md2tqM/hqdefault.jpg", WebSiteHelper.getVideoThumbnailUrlFromWebPage("https://www.youtube.com/watch?v=w9R42md2tqM", null));
        assertEquals("https://img.youtube.com/vi/-wtIMTCHWuI/hqdefault.jpg", WebSiteHelper.getVideoThumbnailUrlFromWebPage("http://m.youtube.com/watch?v=-wtIMTCHWuI", null));
        assertEquals("https://img.youtube.com/vi/-wtIMTCHWuI/hqdefault.jpg", WebSiteHelper.getVideoThumbnailUrlFromWebPage("http://m.youtube.com/watch?v=-wtIMTCHWuI", null));
        // TODO..
        //assertEquals("https://img.youtube.com/vi/6X3zUh8RqbY/hqdefault.jpg", WebSiteHelper.getVideoThumbnailUrlFromWebPage("http://www.youtube.com/embed/6X3zUh8RqbY", null));

        //http://youtu.be/-wtIMTCHWuI this redirects, not sure I need to worry

        // Facebook
        // Vimeo
        // Dailymotion
        // Gihpy
        // Gfycat
        // Liveleak etc
        // Pwnhub etc
    }

    @Test
    public void testIsWordSelected(){
        assertTrue(WebSiteHelper.isWordSelected("apple"));
        assertTrue(WebSiteHelper.isWordSelected("apple "));
        assertTrue(WebSiteHelper.isWordSelected("apple's "));
        assertTrue(WebSiteHelper.isWordSelected(" apple "));
        assertTrue(WebSiteHelper.isWordSelected(" mega-apple ")); //hyphenated words we should treat as one for dictionary

        assertFalse(WebSiteHelper.isWordSelected(""));
        assertFalse(WebSiteHelper.isWordSelected("banana apple"));
        assertFalse(WebSiteHelper.isWordSelected("banana.apple"));
        assertFalse(WebSiteHelper.isWordSelected("123"));
    }


    @Test
    public void testIsNumberSelected(){
        assertTrue(WebSiteHelper.isNumberSelected("123"));
        assertTrue(WebSiteHelper.isNumberSelected("12,33"));
        assertTrue(WebSiteHelper.isNumberSelected(" 12,33 "));
        assertTrue(WebSiteHelper.isNumberSelected(" £1,234.56 "));
        assertTrue(WebSiteHelper.isNumberSelected(" $1,234.56 "));
        assertTrue(WebSiteHelper.isNumberSelected(" €1,234.56 "));
        assertTrue(WebSiteHelper.isNumberSelected("1234\"56"));
        assertTrue(WebSiteHelper.isNumberSelected("1234'56"));
        assertFalse(WebSiteHelper.isNumberSelected("apple"));
        assertFalse(WebSiteHelper.isNumberSelected(""));
        assertFalse(WebSiteHelper.isNumberSelected("banana apple"));
        assertFalse(WebSiteHelper.isNumberSelected("banana.apple"));
    }
    @Test
    public void testGetNumberFromPossibleStringNumber(){
        // Decimal point "."
        assertEquals(new Double(123), WebSiteHelper.getNumberFromPossibleStringNumber("123"));
        assertEquals(new Double(123.5), WebSiteHelper.getNumberFromPossibleStringNumber("123.5"));
        assertEquals(new Double(123.5), WebSiteHelper.getNumberFromPossibleStringNumber(" 123.5 "));
        assertEquals(new Double(123.5), WebSiteHelper.getNumberFromPossibleStringNumber("£123.5"));
        assertEquals(new Double(123.51), WebSiteHelper.getNumberFromPossibleStringNumber("£123.51"));
        assertEquals(new Double(1234567.89), WebSiteHelper.getNumberFromPossibleStringNumber("£1234567.89"));
        assertEquals(new Double(1234567.51), WebSiteHelper.getNumberFromPossibleStringNumber("£1,234,567.51"));
        assertEquals(new Double(1234567.51), WebSiteHelper.getNumberFromPossibleStringNumber("£1 234 567.51"));
        assertEquals(new Double(1234567.51), WebSiteHelper.getNumberFromPossibleStringNumber("Some text £1 234 567.51"));

        // Comma decimal point
        assertEquals(new Double(123), WebSiteHelper.getNumberFromPossibleStringNumber("123"));
        assertEquals(new Double(123.5), WebSiteHelper.getNumberFromPossibleStringNumber("123,5"));
        assertEquals(new Double(123.5), WebSiteHelper.getNumberFromPossibleStringNumber(" 123,5 "));
        assertEquals(new Double(123.5), WebSiteHelper.getNumberFromPossibleStringNumber("$123,5"));
        assertEquals(new Double(123.5), WebSiteHelper.getNumberFromPossibleStringNumber("€123,5"));
        assertEquals(new Double(1234567.51), WebSiteHelper.getNumberFromPossibleStringNumber("£1.234.567,51"));
        assertEquals(new Double(1234567.51), WebSiteHelper.getNumberFromPossibleStringNumber("£1 234 567,51"));
        assertEquals(new Double(1234567.51), WebSiteHelper.getNumberFromPossibleStringNumber("Some text £1 234 567,51"));
        assertNull(WebSiteHelper.getNumberFromPossibleStringNumber("Some text £1 234 567,51 some text"));
        assertNull(WebSiteHelper.getNumberFromPossibleStringNumber(null));
        assertNull(WebSiteHelper.getNumberFromPossibleStringNumber(""));
        assertNull(WebSiteHelper.getNumberFromPossibleStringNumber("aAKdjnadjkna kjanw dkjanwdkajnwd"));
        assertNull(WebSiteHelper.getNumberFromPossibleStringNumber("Some text £1 234 567.51 some text")); // too much text selected, ignore the number
        assertNull(WebSiteHelper.getNumberFromPossibleStringNumber("End of a sentence £123. Cont.."));
        assertNull(WebSiteHelper.getNumberFromPossibleStringNumber("End of a sentence £123.51. Cont.."));
        assertNull(WebSiteHelper.getNumberFromPossibleStringNumber("End of a sentence £123,51. Cont.."));
        assertNull(WebSiteHelper.getNumberFromPossibleStringNumber("End of a sentence £1,234.51. Cont.."));
        assertNull(WebSiteHelper.getNumberFromPossibleStringNumber("End of a sentence £1.234,51. Cont.."));
        assertNull(WebSiteHelper.getNumberFromPossibleStringNumber("End of a sentence £1,234,567. Cont.."));
        assertNull(WebSiteHelper.getNumberFromPossibleStringNumber("End of a sentence £1 234 567. Cont.."));
    }
}