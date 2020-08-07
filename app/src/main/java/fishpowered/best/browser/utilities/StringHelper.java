package fishpowered.best.browser.utilities;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {

    public static final String SEARCH_ENGINE_GOOGLE = "google";
    public static final String SEARCH_ENGINE_DUCKDUCKGO = "duckduckgo";
    public static final String SEARCH_ENGINE_BING = "bing";
    public static final String SEARCH_ENGINE_YANDEX = "yandex";
    public static final String SEARCH_ENGINE_BAIDU = "baidu";
    public static final String SEARCH_ENGINE_ECOSIA = "ecosia";
    public static final String SEARCH_ENGINE_ASK = "ask";
    public static final String SEARCH_ENGINE_WOLFRAMALPHA = "wolframalpha";

    public static ArrayList<String> splitToLineLengthWithoutBreakingWords(String msg, int lineSize){
        ArrayList<String> res = new ArrayList<>();
        if(msg.length() <= lineSize){
            res.add(msg);
            return res;
        }

        Pattern p = Pattern.compile("\\b.{1," + (lineSize-1) + "}\\b\\W?");
        Matcher m = p.matcher(msg);

        while(m.find()) {
            res.add(m.group());
        }
        return res;
    }

    public static ArrayList<String> splitTextToSentences(String text, int maxLength, Locale textLanguage) {
        text = text.trim().replace("\n", ". ");
        BreakIterator iterator = BreakIterator.getSentenceInstance(textLanguage);
        iterator.setText(text);
        ArrayList<String> strings = new ArrayList<>();
        int  start= 0;
        for (int end = iterator.next(); end!=BreakIterator.DONE; start = end, end = iterator.next()) {
            String sentence = text.substring(start, end);
            if(end==text.length() && !sentence.endsWith(".")){
                sentence += ".";
            }
            for(String brokenSentence : splitToLineLengthWithoutBreakingWords(sentence, maxLength)) {
                strings.add(brokenSentence.trim()+" "); // white space at end is important for timing when joining sentences back together
            }
        }
        return strings;
    }

    public static String replaceAbbreviations(String text, Locale locale){
        switch(locale.getLanguage()){
            case "en":
                // important to remove full stops that could appear mid-sentence as this may break the sentence splitting
                text = text.replace(" Mr. ", " Mister ");
                text = text.replace(" Mrs. ", " Misses ");
                text = text.replace(" Ms. ", " Mis ");
                text = text.replace(" Dr. ", " Doctor "); // doctor or drive?
                text = text.replace(" Prof. ", " Professor ");
                text = text.replace(" approx. ", " approximately ");
                text = text.replace(" appt. ", " appointment ");
                text = text.replace(" apt. ", " apartment ");
                text = text.replace(" A.S.A.P. ", " as soon as possible ");
                text = text.replace(" ASAP ", " as soon as possible ");
                text = text.replace(" dept. ", " department ");
                text = text.replace(" est. ", " established ");
                text = text.replace(" ETA ", " estimated time of arrival ");
                text = text.replace(" E.T.A. ", " estimated time of arrival ");
                text = text.replace(" R.S.V.P. ", " R S V P ");
                text = text.replace(" tel. ", " telephone ");
                // Locations
                text = text.replace(" St. ", " street ");
                text = text.replace(" Ave. ", " avenue ");
                text = text.replace(" Blvd. ", " boulevard ");
                text = text.replace(" rd. ", " road ");
                text = text.replace(" ln. ", " lane ");
                // Cooking
                text = text.replace(" tsp. ", " teaspoons ");
                text = text.replace(" tsp ", " teaspoons ");
                text = text.replace(" tbs ", " tablespoons ");
                text = text.replace(" tbs. ", " tablespoons ");
                text = text.replace(" tbsp ", " tablespoons ");
                text = text.replace(" tbsp. ", " tablespoons ");
                text = text.replace(" lb ", " pounds ");
                text = text.replace(" pt ", " p t "); // pint or point?
                text = text.replace(" qt ", " quarts ");
                // Social media
                text = text.replace(" AFAIK ", " as far as I know ");
                text = text.replace(" IIRC ", " if I recall correctly ");
                text = text.replace(" TY ", " thank you ");

                break;
        }
        return text;
    }

    public static String repeat(String string, int times){
        return new String(new char[times]).replace("\0", string);
    }

    /**
     * @param searchTerm
     * @param prefix
     * @param allowExactMatch should "g" trigger Google should it require "g ". Exact match can be distracting when typing words that begin with the shortcut e.g. "googles"
     * @return Null if not a search engine shortcut, String of search term with removed shortcut if it is
     */
    private static String removeSearchOverridePrefix(@NonNull String searchTerm, @NonNull String prefix, final boolean allowExactMatch){
        if (allowExactMatch && searchTerm.toLowerCase().trim().equals(prefix.toLowerCase())) {
            // exact match e.g. "g"
            return "";
        }

        if (searchTerm.toLowerCase().startsWith(prefix.toLowerCase()+" ")) { // DON'T TRIM
            // e.g. "g foo"
            return searchTerm.substring(prefix.length()+1);
        }
        return null;
    }

    /**
     * Same as String.valueOf except null values will be returned as "" instead of "null"
     * @param object
     * @return
     */
    public static String toString(Object object){
        if(object==null){
            return "";
        }
        return String.valueOf(object);
    }

    public static String uppercaseFirst(String str)
    {
        if(str == null) return str;
        if(str.length()>1) {
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
        return str.toUpperCase();
    }

    /**
     * @param searchTerm
     * @return @return <String|null searchEngine (see StringHelper.SEARCH_ENGINE_xxx), String newSearchTerm e.g. with search shortcuts removed>
     */
    @NonNull
    public static Pair<String, String> getOverriddenSearchFromSearchString(String searchTerm, @NonNull String defaultSearchEngine, final boolean allowExactMatch) {
        if(searchTerm==null){
            return new Pair<>(null, null);
        }
        String searchTermWithoutPrefix;

        HashMap<String,String> mappings = new HashMap<>();
        // mappings.add(new Pair<>("google ", SEARCH_ENGINE_GOOGLE)); maybe these are a bad idea, if you wanted to search about the company without changing engine
        mappings.put("ggl", SEARCH_ENGINE_GOOGLE);
        mappings.put("g", SEARCH_ENGINE_GOOGLE);
        mappings.put("ddg", SEARCH_ENGINE_DUCKDUCKGO);
        mappings.put("d", SEARCH_ENGINE_DUCKDUCKGO);
        mappings.put("e", SEARCH_ENGINE_ECOSIA);
        mappings.put("ec", SEARCH_ENGINE_ECOSIA);
        mappings.put("bdu", SEARCH_ENGINE_BAIDU);
        mappings.put("b", SEARCH_ENGINE_BING);
        mappings.put("bng", SEARCH_ENGINE_BING);
        mappings.put("w", SEARCH_ENGINE_WOLFRAMALPHA);
        mappings.put("wf", SEARCH_ENGINE_WOLFRAMALPHA);
        mappings.put("wfa", SEARCH_ENGINE_WOLFRAMALPHA);
        mappings.put("ydx", SEARCH_ENGINE_YANDEX);
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            final String searchPrefix = entry.getKey();
            searchTermWithoutPrefix = removeSearchOverridePrefix(searchTerm, searchPrefix, allowExactMatch);
            if(searchTermWithoutPrefix!=null){
                final String searchEngineToUse = entry.getValue();
                final String searchTermToReturn = defaultSearchEngine.equals(entry.getValue()) ? searchTerm : searchTermWithoutPrefix; // e.g if they're searching "google stocks" on the "google search engine it doesn't make sense to switch
                return new Pair<>(searchEngineToUse, searchTermToReturn);
            }
        }
        return new Pair<>(null, searchTerm);
    }

    /**
     * Crop string to length and add ... suffix
     * @param text
     * @param maxLength including suffix
     * @param suffix e.g. "..."
     * @return
     */
    public static String cropToLength(String text, int maxLength, String suffix) {
        if(text==null){
            return "";
        }
        suffix = toString(suffix);
        final int textLength = text.length();
        int suffixLength = suffix.length();
        // If the number of remaining characters after trimming is smaller than the
//        if(
//                suffixLength >= textLength || maxLength < suffixLength){
//            //         assertEquals("ABC", StringHelper.cropToLength("ABCDE", 3, "..."));
//            // skip the trimmed
//            suffix = "";
//            suffixLength = 0;
//        }
        // A...
        // AB...
        // ABC...
        if(textLength > maxLength){
            if(maxLength <= suffixLength){
                return text.substring(0, maxLength);
            }
            if((maxLength - suffixLength) >= suffixLength){
                // Only show the "..." suffix if there will be more charcters still visible than the suffix. e.g.
                // if cropping ABCDE to 4 chars it's probably nicer to show ABCD than A...
                return text.substring(0, maxLength-suffixLength).trim()+suffix;
            }else{
                return text.substring(0, maxLength).trim();
            }
        }
        return text;
    }
}
