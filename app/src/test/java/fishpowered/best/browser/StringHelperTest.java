package fishpowered.best.browser;

import android.util.Pair;

import fishpowered.best.browser.utilities.StringHelper;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Locale;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class StringHelperTest {

    @Test
    public void getOverriddenSearchFromSearchString(){
        // quicker to manually test this than mock half of android
        /*String defaultSearchEngine = StringHelper.SEARCH_ENGINE_GOOGLE;
        assertTrue((new Pair<String,String>(null, "bananas")).equals(
                StringHelper.getOverriddenSearchFromSearchString("bananas", defaultSearchEngine))
        );
        assertTrue((new Pair<String,String>(null, "google foo")).equals(StringHelper.getOverriddenSearchFromSearchString("google foo", defaultSearchEngine)));
        assertTrue((new Pair<>(StringHelper.SEARCH_ENGINE_GOOGLE, "bananas")).equals(StringHelper.getOverriddenSearchFromSearchString("g bananas", defaultSearchEngine)));
        assertTrue((new Pair<>(StringHelper.SEARCH_ENGINE_GOOGLE, "bananas")).equals(StringHelper.getOverriddenSearchFromSearchString("ggl bananas", defaultSearchEngine)));
        assertTrue((new Pair<>(StringHelper.SEARCH_ENGINE_GOOGLE, "bananas")).equals(StringHelper.getOverriddenSearchFromSearchString("google bananas", defaultSearchEngine)));
        assertTrue((new Pair<>(StringHelper.SEARCH_ENGINE_GOOGLE, "")).equals(StringHelper.getOverriddenSearchFromSearchString("google", defaultSearchEngine)));
        assertTrue((new Pair<>(StringHelper.SEARCH_ENGINE_GOOGLE, "")).equals(StringHelper.getOverriddenSearchFromSearchString("ggl", defaultSearchEngine)));
        assertTrue((new Pair<>(StringHelper.SEARCH_ENGINE_GOOGLE, "")).equals(StringHelper.getOverriddenSearchFromSearchString("g", defaultSearchEngine)));
        assertTrue((new Pair<>(StringHelper.SEARCH_ENGINE_DUCKDUCKGO, "never gonna give you up lyrics")).equals(
                StringHelper.getOverriddenSearchFromSearchString("d never gonna give you up lyrics", defaultSearchEngine))
        );
        assertTrue((new Pair<>(StringHelper.SEARCH_ENGINE_DUCKDUCKGO, "never gonna give you up lyrics")).equals(
                StringHelper.getOverriddenSearchFromSearchString("ddg never gonna give you up lyrics", defaultSearchEngine))
        );
        assertTrue((new Pair<>(StringHelper.SEARCH_ENGINE_GOOGLE, "duckduckgo never gonna give you up lyrics")).equals(
                StringHelper.getOverriddenSearchFromSearchString("duckduckgo never gonna give you up lyrics", defaultSearchEngine))
        );
        assertTrue((new Pair<>(null, "duck clothes")).equals(
                StringHelper.getOverriddenSearchFromSearchString("duck clothes", defaultSearchEngine))
        );*/
    }

    @Test
    public void cropToLength(){
        assertEquals("", StringHelper.cropToLength(null, 0, "..."));
        assertEquals("", StringHelper.cropToLength("", 0, "..."));
        assertEquals("", StringHelper.cropToLength("A", 0, "..."));
        assertEquals("A", StringHelper.cropToLength("A", 1, "..."));
        assertEquals("A", StringHelper.cropToLength("ABC", 1, "..."));
        assertEquals("A", StringHelper.cropToLength("ABCDE", 1, "..."));
        assertEquals("A", StringHelper.cropToLength("ABCDEFGH", 1, "..."));
        assertEquals("AB", StringHelper.cropToLength("AB", 2, "..."));
        assertEquals("AB", StringHelper.cropToLength("ABC", 2, "..."));
        assertEquals("AB", StringHelper.cropToLength("ABCDE", 2, "..."));
        assertEquals("AB", StringHelper.cropToLength("ABCDEFGH", 2, "..."));
        assertEquals("A", StringHelper.cropToLength("A", 3, "..."));
        assertEquals("AB", StringHelper.cropToLength("AB", 3, "..."));
        assertEquals("ABC", StringHelper.cropToLength("ABC", 3, "..."));
        assertEquals("ABC", StringHelper.cropToLength("ABCDE", 3, "..."));
        assertEquals("ABC", StringHelper.cropToLength("ABCDEFGHI", 3, "..."));
        assertEquals("A", StringHelper.cropToLength("A", 4, "..."));
        assertEquals("AB", StringHelper.cropToLength("AB", 4, "..."));
        assertEquals("ABC", StringHelper.cropToLength("ABC", 4, "..."));
        assertEquals("ABCD", StringHelper.cropToLength("ABCD", 4, "..."));
        assertEquals("ABCD", StringHelper.cropToLength("ABCDE", 4, "..."));
        assertEquals("ABCD", StringHelper.cropToLength("ABCDEF", 4, "..."));
        assertEquals("ABCD", StringHelper.cropToLength("ABCDEFGHI", 4, "..."));
        assertEquals("A", StringHelper.cropToLength("A", 5, "..."));
        assertEquals("AB", StringHelper.cropToLength("AB", 5, "..."));
        assertEquals("ABC", StringHelper.cropToLength("ABC", 5, "..."));
        assertEquals("ABCD", StringHelper.cropToLength("ABCD", 5, "..."));
        assertEquals("ABCDE", StringHelper.cropToLength("ABCDE", 5, "..."));
        assertEquals("ABCDE", StringHelper.cropToLength("ABCDEF", 5, "..."));
        assertEquals("ABCDE", StringHelper.cropToLength("ABCDEFGHI", 5, "..."));
        assertEquals("A", StringHelper.cropToLength("A", 6, "..."));
        assertEquals("AB", StringHelper.cropToLength("AB", 6, "..."));
        assertEquals("ABC", StringHelper.cropToLength("ABC", 6, "..."));
        assertEquals("ABCD", StringHelper.cropToLength("ABCD", 6, "..."));
        assertEquals("ABCDE", StringHelper.cropToLength("ABCDE", 6, "..."));
        assertEquals("ABCDEF", StringHelper.cropToLength("ABCDEF", 6, "..."));
        assertEquals("ABC...", StringHelper.cropToLength("ABCDEFGHI", 6, "..."));
        assertEquals("A", StringHelper.cropToLength("A", 7, "..."));
        assertEquals("AB", StringHelper.cropToLength("AB", 7, "..."));
        assertEquals("ABC", StringHelper.cropToLength("ABC", 7, "..."));
        assertEquals("ABCD", StringHelper.cropToLength("ABCD", 7, "..."));
        assertEquals("ABCDE", StringHelper.cropToLength("ABCDE", 7, "..."));
        assertEquals("ABCDEF", StringHelper.cropToLength("ABCDEF", 7, "..."));
        assertEquals("ABCDEFG", StringHelper.cropToLength("ABCDEFG", 7, "..."));
        assertEquals("ABCD...", StringHelper.cropToLength("ABCDEFGH", 7, "..."));
        assertEquals("ABCD...", StringHelper.cropToLength("ABCDEFGHI", 7, "..."));
        assertEquals("A BB...", StringHelper.cropToLength("A BB C D E F G H I", 7, "..."));
        assertEquals("A B...", StringHelper.cropToLength("A B C D E F G H I", 7, "..."));
    }

    @Test
    public void uppercaseFirst(){
        assertEquals("", StringHelper.uppercaseFirst(""));
        assertEquals("B", StringHelper.uppercaseFirst("b"));
        assertEquals("Bo", StringHelper.uppercaseFirst("bo"));
        assertEquals("Bo Bo", StringHelper.uppercaseFirst("bo Bo"));
        assertEquals(null, StringHelper.uppercaseFirst(null));
    }

    @Test
    public void splitTextToSentences(){
        ArrayList<String> expected0 = new ArrayList<String>();
        expected0.add("Sentence Dr. one. ");
        expected0.add("Sentence 1.2 two. ");
        expected0.add("Sentence three. ");
        expected0.add("Sentence four. . ");
        expected0.add("Sentence five. ");
        final String text = "Sentence Dr. one. Sentence 1.2 two. Sentence three\nSentence four\n\nSentence five";
        assertEquals(expected0, StringHelper.splitTextToSentences(text, 1000, Locale.US));
    }

    @Test
    public void splitToLineLengthWithoutBreakingWords(){
        ArrayList<String> expected0 = new ArrayList<String>();
        expected0.add("");
        assertEquals(expected0, StringHelper.splitToLineLengthWithoutBreakingWords("", 12));

        ArrayList<String> expected = new ArrayList<String>();
        expected.add("aa aa aa aa "); // keep the whitespace as we are breaking sentences
        expected.add("bb bb bb bb ");
        expected.add("cc cc cc cc");
        assertEquals(expected, StringHelper.splitToLineLengthWithoutBreakingWords("aa aa aa aa bb bb bb bb cc cc cc cc", 12));

        ArrayList<String> expected2 = new ArrayList<String>();
        expected2.add("aa aa aa aa ");
        expected2.add("bb bb bb bb ");
        expected2.add("cc cc cc cc");
        assertEquals(expected2, StringHelper.splitToLineLengthWithoutBreakingWords("aa aa aa aa bb bb bb bb cc cc cc cc", 13));

        ArrayList<String> expected3 = new ArrayList<String>();
        expected3.add("aa aa aa aa ");
        expected3.add("bb bb bb bb ");
        expected3.add("cc cc cc cc");
        assertEquals(expected3, StringHelper.splitToLineLengthWithoutBreakingWords("aa aa aa aa bb bb bb bb cc cc cc cc", 14));

        ArrayList<String> expected4 = new ArrayList<String>();
        expected4.add("aa aa aa aa bb ");
        expected4.add("bb bb bb cc cc ");
        expected4.add("cc cc");
        assertEquals(expected4, StringHelper.splitToLineLengthWithoutBreakingWords("aa aa aa aa bb bb bb bb cc cc cc cc", 15));
    }

}