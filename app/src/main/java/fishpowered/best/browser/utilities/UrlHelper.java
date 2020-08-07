package fishpowered.best.browser.utilities;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.TabViewHolder;
import fishpowered.best.browser.db.ItemRepository;
import fishpowered.best.browser.db.ItemType;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UrlHelper {

    /**
     * Pretend protocol for dealing with local/offline files without exposing internal file addresses
     */
    public static final String OFFLINE_URL_PROTOCOL = "offline://";

    public static String appendHttpProtocolToUrl(String address, boolean useHttps) {
        if(address==null){
            return null;
        }
        if(!address.startsWith("http://") && !address.startsWith("https://") && !address.startsWith("data:")){
            address = useHttps ? "https://"+address : "http://"+address;
        }
        return address; // todo is there a better way of doing this so we can use https if possible?
    }

    public static String generateFaveTitleBasedOnAddress(String address){
        address = getSimplifiedAddressForUseAsTitle(address, null, true);
        return address;
    }


    /**
     * Doesn't work for dynamic URL's like twitter images
     * @param uri
     * @param context
     * @return
     * @throws MalformedURLException
     */

    @NonNull
    private static String removeFileExtensionsThatAreIrrelevantToUser(String url) {
        if(url.endsWith(".htm") || url.endsWith(".php") || url.endsWith(".asp") || url.endsWith(".jsp")){
            return url.substring(0, url.length()-4);
        }
        if(url.endsWith(".html") || url.endsWith(".aspx") || url.endsWith(".phpx")){
            return url.substring(0, url.length()-5);
        }
        if(url.endsWith(".shtml")){
            return url.substring(0, url.length()-6);
        }
        return url;
    }

    @NonNull
    public static String removeMobilePrefixFromDomain(@NonNull String address) {
        if(address.startsWith("en.m.") || address.startsWith("fi.m.")) { // todo make locale dynamic and this can also be used for address hint
            address = address.substring(5);
        }else if(address.startsWith("m.")){
            address = address.substring(2);
        }else if(address.startsWith("mobile.")){
            address = address.substring(7);
        }else if(address.startsWith("mobi.")){
            address = address.substring(5);
        }
        return address;
    }

    /**
     * Get's key value pairs for params in a queryPart
     * @param queryPart Everything after ?
     * @return
     */
    public static Map<String, String> getQueryMap(String queryPart)
    {
        if(queryPart==null){
            queryPart = "";
        }
        String[] params = queryPart.split("&");
        Map<String, String> map = new HashMap<String, String>();
        String[] split;
        for (String param : params)
        {
            split = param.split("=");
            // @TODO anchor support!
            String name;
            String value;
            if(split.length>=2) {
                name = split[0].toLowerCase();
                value = split[1];
            }else{
                name = split[0];
                value = "";
            }
            map.put(name, value);
        }
        return map;
    }

    public static String getParamFromUrl(@NonNull String url, @NonNull String paramName){
        try{
            return getQueryMap(UrlHelper.getQueryStringFromUrl(url)).get(paramName);
        }catch(NullPointerException e){
            return null;
        }
    }

    /**
     * Get simplified address (e.g. without http, query strings etc)
     * @param url
     * @param pageTitleIfKnown
     * @param hideSearchEnginePrefixFromSearchAddresses
     * @return
     */
    public static String getSimplifiedAddressForUseAsTitle(String url, String pageTitleIfKnown, boolean hideSearchEnginePrefixFromSearchAddresses){
        if(url==null){
            return "";
        }
        return url;
    }

    @Nullable
    private static String simplifyUrlsForPopularSearchEngines(String url, boolean hideSearchEnginePrefixFromSearchAddresses) {
        // Simplify history on search pages to simply "search term"
        return null;
    }

    private static String getSearchStringFromSearchEngineUrl(@NonNull String paramName, String url, String resultPrefix){
        if(url==null || paramName.length()==0){
            return null;
        }
        try {
            paramName = paramName.toLowerCase();
            if(url.toLowerCase().contains(paramName +"=")) {
                String searchParam = getParamFromUrl(url, paramName);
                if (searchParam != null && searchParam.length() > 0) {
                    String decodedValue;
                    decodedValue = java.net.URLDecoder.decode(searchParam, "utf-8");
                    if(resultPrefix==null){
                        return "\"" + decodedValue.trim() + "\"";
                    }
                    return resultPrefix + ": \"" + decodedValue.trim() + "\"";
                }
            }
        } catch (Exception e) {
            //Log.e("FPBROWSER", "Unable to get paramname from url: "+url+" msg: "+e.getMessage());
        }
        return null;
    }

    public static String getQueryStringFromUrl(String url){
        if(url.indexOf("?")==-1){
            return null;
        }
        String queryString = url.substring(url.indexOf("?") + 1);
        if(queryString.indexOf("#")>-1){
            queryString = queryString.substring(0, queryString.indexOf("#"));
        }
        return queryString.trim().length()>0 ? queryString.trim() : null;
    }

    /**
     * Will return the structure of the address only e.g. remove https://, www. and any query strings
     * Good for comparing URL's in a way that won't be broken by every little anchor tag and such
     * e.g. autosport.com, reddit.com/r/formula1 etc
     * @param url
     * @return
     */
    public static String getTrustedAddress(String url){
        if(url==null){
            return "";
        }
        return url;
    }


    public static String encodeUrlValue(String urlValue){
        if(urlValue==null){
            return "";
        }
        try {
            urlValue = URLEncoder.encode(urlValue, "utf-8").trim();
        } catch (UnsupportedEncodingException e) {
            urlValue ="";
        }
        return urlValue;
    }

    public static String getSearchEngineUrl(String searchPhrase, Browser browser, String forceSearchEngine) {
        String searchEngineName;
        if(forceSearchEngine!=null && forceSearchEngine.length()>0){
            searchEngineName = forceSearchEngine;
        }else {
            final Pair<String, String> overriddenSearch = browser.preferences.getTextSearchEngine(searchPhrase, true);
            searchEngineName = overriddenSearch.first;
            searchPhrase = overriddenSearch.second;
        }
        String encodedSearch = null;
        if(searchPhrase!=null){
            encodedSearch = encodeUrlValue(searchPhrase);
        }
        final String client = Browser.searchEngineClientId;
        String domain;
        String params="";
        switch(searchEngineName){
            case StringHelper.SEARCH_ENGINE_DUCKDUCKGO:
                domain = "https://duckduckgo.com/";
                if(encodedSearch!=null && !encodedSearch.equals("")){
                    params = "?q="+encodedSearch+"&t=h_&ia="+client;
                }
                break;
            case StringHelper.SEARCH_ENGINE_ASK:
                domain = "https://www.ask.com/";
                if(encodedSearch!=null && !encodedSearch.equals("")){
                    params = "web?q="+encodedSearch+"&o=0&qo="+client;
                }
                break;
            case StringHelper.SEARCH_ENGINE_BAIDU:
                domain = "http://www.baidu.com/";
                if(encodedSearch!=null && !encodedSearch.equals("")){
                    params = "s?ie=utf-8&f=8&rsv_bp=1&rsv_idx=1&ch=&tn=baidu&bar=&wd="+encodedSearch+"&rn=&oq=&rsv_pq=9a00e1010000552f&rsv_t=666drELnVAS8Zn5T4O6j6Khk8ZjqYXysEiMl7PWaApn30%2F6OLIZhoPTBVpM&rqlang=cn";
                }
                break;
            case StringHelper.SEARCH_ENGINE_BING:
                domain = "https://www.bing.com/";
                if(encodedSearch!=null && !encodedSearch.equals("")){
                    params = "search?q="+encodedSearch+"&qs=n&form=QBLH&sp=-1&pq="+encodedSearch+"&sc=8-4&sk=&cvid=0024AABDB137409CA5FA8A5670A9D65F";
                }
                break;
            case StringHelper.SEARCH_ENGINE_ECOSIA:
                domain = "https://www.ecosia.org";
                if(encodedSearch!=null && !encodedSearch.equals("")){
                    params = "/search?q="+encodedSearch;
                }
                break;
            case StringHelper.SEARCH_ENGINE_WOLFRAMALPHA:
                domain = "https://www.wolframalpha.com/";
                if(encodedSearch!=null && !encodedSearch.equals("")){
                    params = "input/?i="+encodedSearch;
                }
                break;
            case StringHelper.SEARCH_ENGINE_YANDEX:
                domain ="https://yandex.ru/";
                if(encodedSearch!=null && !encodedSearch.equals("")){
                    params = "search/?lr=155421&oprnd=7512662919&text="+encodedSearch;
                }
                break;
            case StringHelper.SEARCH_ENGINE_GOOGLE:
            default:
                if(Browser.safeMode){
                    domain = "https://encrypted.google.com/"; // this should prevent AMP being used
                }else {
                    domain = "https://www.google.com/";
                }
                if(encodedSearch!=null && !encodedSearch.equals("")){
                    params = "search?client=" + client + "&q=" +encodedSearch+ "&sourceid=" + client + "&ie=UTF-8&oe=UTF-8";
                }
                break;
        }
        return domain+params;
    }

    public static String getImageSearchEngineUrl(String imageUrl) {
        if(imageUrl==null){
            imageUrl = "";
        }
        final String client = Browser.searchEngineClientId;
        imageUrl = URLEncoder.encode(imageUrl).trim();
        return "https://www.google.com/searchbyimage?client=" + client+ "&sourceid=" + client + "&ie=UTF-8&oe=UTF-8&image_url=" + imageUrl ;
    }

    public static String getDictionaryUrl(String s, Browser browser) {
        return null;
    }

    public static String getTranslationUrl(String translateText, Browser browser) {
        return null;
    }


    /**
     * Has the user *attempted* to type a url e.g. autosport.com should return true even though it's
     * missing the protocol
     * @param url
     * @return
     */
    public static boolean isUrlAttempt(String url) {
        // IMPORTANT. DON'T EVER ADD APP LINKS TO THIS e.g. intent:// without parameterising it. See CustomWebViewClient.shouldOverrideUrlCentralised
        boolean isUrl = false;
        if(url==null){
            return false;
        }
        if(url.startsWith("data:")){ // e.g. base64 encoded images
            return true;
        }
        url = url.trim().toLowerCase(); // might be slow on base 64 strings
        if(url.startsWith("www.")){
            return true;
        }
        if(url.startsWith("http://")){
            return true;
        }
        if(url.startsWith("https://")){
            return true;
        }
        if(url.startsWith("file://")){
            return false; // don't allow users any access to internal files directory and treat it like a search instead!
        }
        if(url.startsWith(OFFLINE_URL_PROTOCOL)){
            return true;
        }
        if(!url.contains(" ")){
            // This also needs to pick up user typed domains that aren't fully formed e.g. "autosport.com"
            for (String topLevelDomain : topLevelDomains) {
                if (doesUrlBelongToDomain(url, topLevelDomain)) { // this could be better
                    return true;
                }
            }
        }
        return false;
    }

    private static final ArrayList<String> imageExtensions = new ArrayList<>(Arrays.asList("jpg","png","gif","jpeg","jfif","tiff","bmp","ppm","exif",
            "ppm", "pgm", "pbm", "pnm", "webp"));

    public static boolean isImage(String url) {
        if(url==null){
            return false;
        }
        if(url.startsWith("data:image/")){
            return true;
        }
        String extension = getFileExtension(url);
        return imageExtensions.contains(extension);
    }

    public static boolean isExtensionForImage(String extension){
        return imageExtensions.contains(extension);
    }

    public static boolean isDirectVideoLink(String url){
        final String fileExtension = UrlHelper.getFileExtension(url);
        return fileExtension!=null && fileExtension.length() > 0 && isExtensionForVideoFile(fileExtension);
    }

    public static boolean isImageOrVideo(String url){
        final String fileExtension = UrlHelper.getFileExtension(url);
        return fileExtension!=null && fileExtension.length() > 0 && (isExtensionForImage(fileExtension) || isExtensionForVideoFile(fileExtension));
    }

    @NonNull
    public static String removeAnchor(String url) {
        if(url.indexOf("#")>=0){
            return url.substring(0, url.indexOf("#"));
        }
        return url;
    }

    @NonNull
    public static String removeQueryString(String url) {
        if(url.indexOf("?")>=0){
            return url.substring(0, url.indexOf("?"));
        }
        return url;
    }

    public static boolean doesUrlBelongToDomain(String url, String domain){
        if(url==null || domain==null){
            return false;
        }
        String baseUrl = getDomain(url, true, true, false);
        if(baseUrl==null){
            return false;
        }
        return (baseUrl.contains("." + domain)); // TODO this could be better :D
    }

    /**
     * I think hostname is the correct term for this e.g. autosport.com, nothing else
     * @param url Param must have the http://
     * @param removeMobilePrefix
     * @return String|null
     */
    public static String getDomain(String url, boolean removeWww, boolean removeProtocol, boolean removeMobilePrefix){
        if(url==null){
            return null;
        }
        url = url.trim();
        int endOfDomain;
        if(removeProtocol) {
            if (url.toLowerCase().startsWith("http://")) {
                url = url.substring(7);
            }
            if (url.toLowerCase().startsWith("https://")) {
                url = url.substring(8);
            }
            endOfDomain = url.indexOf("/");
        }else{
            endOfDomain = url.indexOf("/", 9);
        }

        if(endOfDomain > 0){
            url = url.substring(0, endOfDomain);
        }
        if (removeWww && url.startsWith("www.")) {
            url = url.substring(4);
        }
        if(removeMobilePrefix){
            url = UrlHelper.removeMobilePrefixFromDomain(url);
        }
        return url;
    }

    public static String getAddressHintText(String url, Context context){

        if(url == null){
            return "";
        }
        if(url.startsWith("file://")){
            url = ItemRepository.getSavedForLaterAddressBasedOnInternalFileUrl(url, context, false);
        }
        return getDomain(url, true, true, true);
    }

    public static final String[] englishDomains = {
        "com",
        "net",
        "org",
        "edu",
        "uk",
        "us",
        "info",
        "jobs",
        "au",
        "ag",
        "bs",
        "bb",
        "bz",
        "ca",
        "dm",
        "gd",
        "gy",
        "ie",
        "jm",
        "nz",
        "ks",
        "lc",
        "vc",
        "tt",
    };

    public static final String[] topLevelDomains = {
            "com",
            "net",
            "org",
            "co",
            "gov",
            "edu",
            "ac",
            "ad",
            "ae",
            "aero",
            "af",
            "ag",
            "ai",
            "al",
            "am",
            "an",
            "ao",
            "aq",
            "ar",
            "arpa",
            "as",
            "asia",
            "at",
            "au",
            "aw",
            "ax",
            "az",
            "ba",
            "bb",
            "bd",
            "be",
            "bf",
            "bg",
            "bh",
            "bi",
            "biz",
            "bj",
            "bl",
            "bm",
            "bn",
            "bo",
            "bq",
            "br",
            "bs",
            "bt",
            "bv",
            "bw",
            "by",
            "bz",
            "ca",
            "cat",
            "cc",
            "cd",
            "cf",
            "cg",
            "ch",
            "ci",
            "ck",
            "cl",
            "cm",
            "cn",
            "coop",
            "cr",
            "cs",
            "cu",
            "cv",
            "cw",
            "cx",
            "cy",
            "cz",
            "dd",
            "de",
            "dj",
            "dk",
            "dm",
            "do",
            "dz",
            "ec",
            "ee",
            "eg",
            "eh",
            "er",
            "es",
            "et",
            "eu",
            "fi",
            "fj",
            "fk",
            "fm",
            "fo",
            "fr",
            "ga",
            "gb",
            "gd",
            "ge",
            "gf",
            "gg",
            "gh",
            "gi",
            "gl",
            "gm",
            "gn",
            "gp",
            "gq",
            "gr",
            "gs",
            "gt",
            "gu",
            "gw",
            "gy",
            "hk",
            "hm",
            "hn",
            "hr",
            "ht",
            "hu",
            "id",
            "ie",
            "il",
            "im",
            "in",
            "info",
            "int",
            "io",
            "iq",
            "ir",
            "is",
            "it",
            "je",
            "jm",
            "jo",
            "jobs",
            "jp",
            "ke",
            "kg",
            "kh",
            "ki",
            "km",
            "kn",
            "kp",
            "kr",
            "kw",
            "ky",
            "kz",
            "la",
            "lb",
            "lc",
            "li",
            "lk",
            "local",
            "lr",
            "ls",
            "lt",
            "lu",
            "lv",
            "ly",
            "ma",
            "mc",
            "md",
            "me",
            "mf",
            "mg",
            "mh",
            "mil",
            "mk",
            "ml",
            "mm",
            "mn",
            "mo",
            "mobi",
            "mp",
            "mq",
            "mr",
            "ms",
            "mt",
            "mu",
            "museum",
            "mv",
            "mw",
            "mx",
            "my",
            "mz",
            "na",
            "name",
            "nato",
            "nc",
            "ne",
            "nf",
            "ng",
            "ni",
            "nl",
            "no",
            "np",
            "nr",
            "nu",
            "nz",
            "om",
            "onion",
            "pa",
            "pe",
            "pf",
            "pg",
            "ph",
            "pk",
            "pl",
            "pm",
            "pn",
            "pr",
            "pro",
            "ps",
            "pt",
            "pw",
            "py",
            "qa",
            "re",
            "ro",
            "rs",
            "ru",
            "rw",
            "sa",
            "sb",
            "sc",
            "sd",
            "se",
            "sg",
            "sh",
            "si",
            "sj",
            "sk",
            "sl",
            "sm",
            "sn",
            "so",
            "sr",
            "ss",
            "st",
            "su",
            "sv",
            "sx",
            "sy",
            "sz",
            "tc",
            "td",
            "tel",
            "tf",
            "tg",
            "th",
            "tj",
            "tk",
            "tl",
            "tm",
            "tn",
            "to",
            "tp",
            "tr",
            "travel",
            "tt",
            "tv",
            "tw",
            "tz",
            "ua",
            "ug",
            "uk",
            "um",
            "us",
            "uy",
            "uz",
            "va",
            "vc",
            "ve",
            "vg",
            "vi",
            "vn",
            "vu",
            "wf",
            "ws",
            "xxx",
            "ye",
            "yt",
            "yu",
            "za",
            "zm",
            "zr",
            "zw"
    };

    public static String getFileExtension(String downloadUrl) {
        // todo official way of doing it? -> MimeTypeMap.getFileExtensionFromUrl(url) trouble is i canät unit test this cos it complains about mocking
        if(downloadUrl==null){
            return null;
        }
        downloadUrl = removeQueryString(downloadUrl);
        downloadUrl = removeAnchor(downloadUrl);
        return downloadUrl.substring(downloadUrl.lastIndexOf(".")+1).toLowerCase();
    }

    public static String getSearchSuggestionUrl(String searchTextSoFar, Browser browser) throws UnsupportedEncodingException {
        String targetLanguage = Resources.getSystem().getConfiguration().locale.getLanguage();
        if(targetLanguage.equals("")){
            targetLanguage = "en";
        }
        return "https://suggestqueries.google.com/complete/search?output=toolbar&hl="+URLEncoder.encode(targetLanguage, "UTF-16")+"&q="+URLEncoder.encode(searchTextSoFar.trim(), "utf-16");
    }

    public static String getCalculatorUrl(String startingNumber, Browser browser) {
        if(startingNumber!=null && startingNumber.trim().length() > 0){
            startingNumber = "calculator "+startingNumber.trim()+" + 0";
        }else{
            startingNumber = "calculator";
        }
        return getSearchEngineUrl(startingNumber, browser, "google")+"#gsr";
    }

    public static int getFaveTypeFromAddress(String address, TabViewHolder tab) {
        if(isImage(address)){
            return ItemType.IMAGE;
        }
        return ItemType.WEB_SITE;
    }

    /**
     * Hack so we can highlight selected images (when u select an image we only get the full url
     * not what was used in the path)
     * @param url
     * @return
     */
    public static String removeUnnecessaryPartOfImageUrl(String url) {
        //http://foo.com/image.png?awdkjnawd
        //http://foo.com/?dynamic-image/afwkwm
        if(!url.contains("/")){
            return url;
        }
        String substring = url.substring(url.lastIndexOf("/"));
        if(substring.length()==0){
            return null;
        }
        if(substring.startsWith("/")){
            substring = substring.substring(1);
        }
        return substring;
    }

    public static String getAddressForSavingSiteSettingsAgainst(String url) {
        return getDomain(url, true, true, false);
    }

    /**
     * TODO get working again when find source for hash tags
     * @param browser
     * @return
     */
    public static String getTrendingHashTagProviderUrl(Browser browser) {

        String locationPref = browser.preferences.getTrendingSearchLocation(); // TODO returns name according to translations, needs switching to country code :/
        //noinspection ConstantConditions
        if(true){
            return null; // disabled for now
        }
        if(locationPref!=null && locationPref.toLowerCase().equals("disabled")) {
            return null;
        }
        if(locationPref==null || locationPref.toLowerCase().equals("automatic")) {
            locationPref = getCurrentCountryBasedOnSim(browser); // returns country code e.g. en_US
        }
        switch(locationPref.toLowerCase()){             // NEED TO SUPPORT COUNTRY NAMES AND COUNTRY CODES HERE AS THE PREFERENCE RETURNS NAME, THE SIM CODE RETURNS CODE
            case "ru_ru":
            case "russia":
                return "https://othersta.com/twitter-trends/russia";
            case "de_de":
            case "de_at":
            case "de_ch":
            case "germany":
                return "https://othersta.com/twitter-trends/germany";
            case "ja_jp":
            case "japan":
                return "https://othersta.com/twitter-trends/japan";
            case "es_es":
            case "spain":
                return "https://othersta.com/twitter-trends/spain";
            case "en_gb":
            case "united kingdom":
                return "https://othersta.com/twitter-trends/england";
            case "en_us":
            case "united states":
                return "https://othersta.com/twitter-trends/united-states";
            case "it_it":
            case "italy":
                return "https://othersta.com/twitter-trends/italy";
            case "en_ca":
            case "fr_ca":
            case "canada":
                return "https://othersta.com/twitter-trends/canada";
            case "global":
            default:
                return "https://othersta.com/twitter-trends";
        }
    }

    /**
     * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
     * @param context Context reference to get the TelephonyManager instance from
     * @return country code or null
     */
    public static String getCurrentCountryBasedOnSim(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if(tm==null){
                return null;
            }
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.US);
            }
            else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US);
                }
            }
        }
        catch (Exception e) { }
        return null;
    }

    public static boolean isPossibleVideoStreamingLink(String currentPageAddress, boolean returnTrueForDirectVideoDownloadLinks) {
        if(currentPageAddress==null){
            return false;
        }
        if(currentPageAddress.contains("youtube.com/") && UrlHelper.getParamFromUrl(currentPageAddress, "v")!=null){
            return true;
        }
        if(currentPageAddress.contains("liveleak.com/view")){
            return true;
        }
        if(currentPageAddress.contains("music.youtube.com") && !currentPageAddress.endsWith("music.youtube.com/")){
            return true;
        }
        if(currentPageAddress.contains("twitch.tv") && !currentPageAddress.endsWith("twitch.tv/")){
            return true;
        }
        if(currentPageAddress.contains("hulu.com") && (!currentPageAddress.endsWith("hulu.com/") && !currentPageAddress.endsWith("hulu.com/welcome"))){
            return true;
        }
        if(currentPageAddress.contains("vimeo.com") && (currentPageAddress.contains("/channels") || !currentPageAddress.endsWith("vimeo.com"))){
            return true;
        }
        if(currentPageAddress.contains("break.com/") && !currentPageAddress.endsWith("break.com/")){
            return true;
        }
        if(currentPageAddress.contains("/video/") || currentPageAddress.contains("/watch/")){ // e.g. dailymotion, veoh
            return true;
        }
        if(currentPageAddress.contains("instagram.com/p/")){
            return true;
        }
        if(returnTrueForDirectVideoDownloadLinks) {
            final String fileExtension = UrlHelper.getFileExtension(currentPageAddress);
            if (isExtensionForVideoFile(fileExtension)
            ) {
                return true;
            }
        }
        // facebook, twitter, instagram, reddit, gfycat, giphy, imgur
        return false;
    }

    public static boolean isVideoDownloaderBotSupportedLink(String currentPageAddress) {
        if(currentPageAddress==null){
            return false;
        }
        if(currentPageAddress.contains("youtube.com/") && UrlHelper.getParamFromUrl(currentPageAddress, "v")!=null){
            return true;
        }
        if(currentPageAddress.contains("instagram.com/p/")){
            return true;
        }
        // facebook, twitter, instagram, reddit, gfycat, giphy, imgur
        return false;
    }

    public static boolean isExtensionForVideoFile(String fileExtension) {
        if(fileExtension==null){
            return false;
        }
        return fileExtension.equals("ogg") || fileExtension.equals("gifv")
                || fileExtension.equals("mp4") || fileExtension.equals("webm") || fileExtension.equals("mpeg")
                || fileExtension.equals("mpg") || fileExtension.equals("avi") || fileExtension.equals("wmv");
    }

    public static boolean isExtensionForFileThatCouldBeImmediatelyOpenedByExternalAppWithoutPrompt(String fileExtension){
        if(fileExtension==null){
            return false;
        }
        return false;
        /*return fileExtension.equals("pdf") || fileExtension.equals("doc") Chrome doesn't attempt to open these even with MS Word installed
                || fileExtension.equals("docx") || fileExtension.equals("xls")
                || fileExtension.equals("odt") || fileExtension.equals("xlsx")
                || fileExtension.equals("ppt") || fileExtension.equals("ods")
                || fileExtension.equals("pptx") || fileExtension.equals("xps")
                || fileExtension.equals("odp");*/

    }

    /**
     * If it's a file extension that the browser could not handle on it's own e.g. word doc, we should return true
     * here so we can prevent them being saved to the tab state
     * @param url
     * @return
     */
    public static boolean looksLikeAFileDownloadUrl(String url) {
        final String fileExtension = getFileExtension(url);
        if(fileExtension==null || fileExtension.equals("")){
            return false;
        }
        // Can't seem to find a way to get this info dynamically
        if(fileExtension.equals("doc")
                || fileExtension.equals("docx") || fileExtension.equals("xls")
                || fileExtension.equals("odt") || fileExtension.equals("xlsx")
                || fileExtension.equals("ppt") || fileExtension.equals("ods")
                || fileExtension.equals("pptx") || fileExtension.equals("xps")
                || fileExtension.equals("odp")
                || fileExtension.equals("zip")
                || fileExtension.equals("7z")
                || fileExtension.equals("rar")
                || fileExtension.equals("pak")
                || fileExtension.equals("exe")
                || fileExtension.equals("apk")
                || fileExtension.equals("pdf")
                || fileExtension.equals("tar")
                || fileExtension.equals("gz")
                || fileExtension.equals("wpd")
                || fileExtension.equals("mp3")
                || fileExtension.equals("aif")
                || fileExtension.equals("cda")
                || fileExtension.equals("mid")
                || fileExtension.equals("midi")
                || fileExtension.equals("wav")
                || fileExtension.equals("wma")
                || fileExtension.equals("wpl")
                || fileExtension.equals("arj")
                || fileExtension.equals("deb")
                || fileExtension.equals("pkg")
                || fileExtension.equals("rpm")
                || fileExtension.equals("bin")
                || fileExtension.equals("iso")
                || fileExtension.equals("vcd")
                || fileExtension.equals("csv")
                || fileExtension.equals("dat")
                || fileExtension.equals("sav")
                || fileExtension.equals("sql")
                || fileExtension.equals("jar")
                || fileExtension.equals("wsf")
                || fileExtension.equals("fnt")
                || fileExtension.equals("fon")
                || fileExtension.equals("otf")
                || fileExtension.equals("ttf")
                || fileExtension.equals("bak")
                || fileExtension.equals("mov")
                || fileExtension.equals("wmv")
        ){
            return true;
        }
        if(url.startsWith("intent://") || url.startsWith("market://")){
            return true;
        }
        return false;
    }

    /**
     * Webview can handle certain file types like mp3's but letting it do so is a crappy experience
     * so for types like this we skip even attempting to load the page and force the download
     * @param fileExtension
     * @return
     */
    public static boolean shouldSkipPageLoadAndTriggerDownload(String fileExtension) {
        if(fileExtension==null){
            return false;
        }
        // Don't add other types here unless it's a type that natively gets handled by webview in a crappy way
        // otherwise you can have pages served with pdf extension that isn't actually a pdf
        if(fileExtension.equals("mp3")){
            return true;
        }
        return false;
    }

    public static String getFileNameFromUrl(String url, @Nullable String contentDisposition, @Nullable String mimeType){
        if(url.startsWith("data:") || url.startsWith("blob:")){
            if(mimeType==null && url.startsWith("data:")){
                mimeType = url.substring(5, url.indexOf(";"));
            }
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String extension = mimeTypeMap.getExtensionFromMimeType(mimeType);
            if(mimeType==null){
                mimeType = "";
            }
            if(extension==null){
                switch(mimeType){
                    case "plain/text":
                        extension = "txt";
                        break;
                    case "application/pdf": // probably the most likely use of browser generated blob urls
                        extension = "pdf";
                        break;
                    default:
                        extension = "unknown";
                        break;
                }
            }
            Date date = new Date();
            @SuppressLint("SimpleDateFormat") String formattedDate = new SimpleDateFormat("yyyyMMdd-hhmmss").format(date);
            return "download-"+formattedDate+"."+extension;
        }
        String guessedFileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
        return guessedFileName;
    }

    public static String getAddressFromGoogleAmp(String currentPageAddress) {
        if(currentPageAddress==null){
            return null;
        }
        if(currentPageAddress.contains(".google.") && currentPageAddress.contains("/amp/s/")){
            try {
                return "https://"+ URLDecoder.decode(currentPageAddress.substring(currentPageAddress.indexOf("/amp/s/")+7), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * When viewing offline articles the only way to load them in the webview is using a file:// address
     * and we don't want to expose this to users (or
     * @param address
     * @param id
     * @return
     */
    public static String createOfflineAddress(String address, int id) {
        return OFFLINE_URL_PROTOCOL +id+"/"+address;
    }

    public static String getRealAddressFromOfflineUrl(String url, Context context) {
        if(url==null || !url.startsWith(OFFLINE_URL_PROTOCOL)  || url.length() < OFFLINE_URL_PROTOCOL.length()+2){
            Log.e("FPERROR", "Bad offline mode URL received: "+url);
            return url;
        }
        String[] parts = url.substring(OFFLINE_URL_PROTOCOL.length()).split("/", 2);
        if(parts.length==0){
            return null;
        }
        int offlineItemId = 0;
        if(parts[0].length() > 0) {
            offlineItemId = Integer.parseInt(parts[0]);
        }
        String fallbackUrl = null;
        if(parts.length > 1) {
            fallbackUrl = parts[1];
        }
        if(offlineItemId > 0) {
            final String offlinePageFileName = FileHelper.getFilePathForSavingWebArchive(offlineItemId, context, false);
            if(FileHelper.fileExistsInFilesDirectory(context, offlinePageFileName)) {
                return "file://" + context.getFilesDir().getAbsolutePath() + File.separator + offlinePageFileName;
            }
        }
        if(UrlHelper.isUrlAttempt(fallbackUrl)){
            // This might happen if the offline page archive has been deleted but there is still a tab open using the address
            // so fall back to online version
            return fallbackUrl;
        }
        return null;
    }

    public static boolean isFileUrlForFetchingOfflinePages(String url) {
        if(url==null){
            return false;
        }
        return url.startsWith("file://") && url.endsWith(".mht") && url.contains("readLater_");
    }

    public static String removeTopLevelDomains(String domain) {
        if(domain==null) {
            return "";
        }
        for (String topLevelDomain : topLevelDomains) {
            if(domain.endsWith("."+topLevelDomain)){
                domain = domain.substring(0, domain.length()-("."+topLevelDomain).length());
            }
        }
        return domain+"";
    }
}
