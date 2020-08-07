package fishpowered.best.browser;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import fishpowered.best.browser.db.Item;

import fishpowered.best.browser.utilities.StringHelper;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper class to fetch settings/preferences
 */
public class Preferences implements SharedPreferences {
    public static final String PRIVACY_LEVEL_LITE = "lite";
    public static final String PRIVACY_LEVEL_STANDARD = "basic"; // dont change string as it's saved in prefs
    public static final String PRIVACY_LEVEL_SERIOUS = "serious";
    public static final String PRIVACY_LEVEL_EXTREME = "extreme";
    private final SharedPreferences sharedPreferences;
    private final Context context;

    public Preferences(Context context){
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isNewTabButtonBoundToNavBar(){
        return isButtonActionVisibleOnNavBar("newTab");
    }

    public boolean shouldClosingFinalTabCloseBrowser(){
        return sharedPreferences.getBoolean("closingLastTabClosesBrowser", context.getString(R.string.defaultClosingLastTabClosesBrowser).equals("true"));
    }

    public boolean shouldClosingBrowserCloseAllTabs(){
        return sharedPreferences.getBoolean("closeAllTabsOnExit", context.getString(R.string.defaultCloseAllTabsOnExit).equals("true"));
    }

    public boolean showTagFaveDialogWhenPressingSave(){
        return true;
    }

    @NonNull
    public boolean isButtonActionVisibleOnNavBar(String action){
        return(
                getNavButton1QuickPressAction().equalsIgnoreCase(action)
                        || getNavButton2QuickPressAction().equalsIgnoreCase(action)
                        || getNavButton3QuickPressAction().equalsIgnoreCase(action)
                        || getNavButton4QuickPressAction().equalsIgnoreCase(action)
        );
    }

    public boolean shouldAutoHideNavBar(String currentUrl){
        if(currentUrl!=null && currentUrl.contains("how-to-close-tabs.html")){
            return false; // hardcoded for tutorial so it's not so confusing
        }
        return sharedPreferences.getBoolean("autoHideNavBar", context.getString(R.string.defaultAutoHideNavBar).equals("true"));
    }

    public boolean isActionBoundToNavBar(ArrayList<String> speedDialOptions){
        return(
                speedDialOptions.contains(getNavButton1QuickPressAction())
                        || speedDialOptions.contains(getNavButton2QuickPressAction())
                        || speedDialOptions.contains(getNavButton3QuickPressAction())
                        || speedDialOptions.contains(getNavButton4QuickPressAction())
                        || speedDialOptions.contains(getNavAddressHintQuickPressAction())
        );
    }

    public boolean isSpeedDialBoundToNavBar(){
        ArrayList<String> speedDialOptions = new ArrayList<String>();
        speedDialOptions.add("searchOrEnterAddress");
        speedDialOptions.add("newTab");
        speedDialOptions.add("viewFaves");
        speedDialOptions.add("viewWhatsHot");
        //speedDialOptions.add("viewTabs");
        speedDialOptions.add("viewHistory");
        speedDialOptions.add("viewHistory");
        speedDialOptions.add("closeAllTabs");
        return isActionBoundToNavBar(speedDialOptions);
    }

    public boolean isPageMenuBoundToNavBar(){
        ArrayList<String> speedDialOptions = new ArrayList<String>();
        speedDialOptions.add("showPageMenu");
        return isActionBoundToNavBar(speedDialOptions);
    }

    /**
     * @link Preferences.isPrivateBrowsingModeEnabled
     * @return "standard"|"high"|"extreme"
     * @param onlyReturnLevelIfPrivacyModeEnabled
     */
    public String getPrivacyLevel(boolean onlyReturnLevelIfPrivacyModeEnabled){
        if(onlyReturnLevelIfPrivacyModeEnabled && !isPrivateBrowsingModeEnabled()){
            return "off";
        }
        return sharedPreferences.getString("privacyModeLevel", context.getString(R.string.defaultPrivacyModeLevel));
    }

    /**
     * Retrieve all values from the preferences.
     *
     * <p>Note that you <em>must not</em> modify the collection returned
     * by this method, or alter any of its contents.  The consistency of your
     * stored data is not guaranteed if you do.
     *
     * @return Returns a map containing a list of pairs key/value representing
     * the preferences.
     * @throws NullPointerException
     */
    @Override
    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }

    /**
     * Retrieve a String value from the preferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a String.
     * @throws ClassCastException
     */
    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    /**
     * Retrieve a set of String values from the preferences.
     *
     * <p>Note that you <em>must not</em> modify the set instance returned
     * by this call.  The consistency of the stored data is not guaranteed
     * if you do, nor is your ability to modify the instance at all.
     *
     * @param key       The name of the preference to retrieve.
     * @param defValues Values to return if this preference does not exist.
     * @return Returns the preference values if they exist, or defValues.
     * Throws ClassCastException if there is a preference with this name
     * that is not a Set.
     * @throws ClassCastException
     */
    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return sharedPreferences.getStringSet(key, defValues);
    }

    /**
     * Retrieve an int value from the preferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * an int.
     * @throws ClassCastException
     */
    @Override
    public int getInt(String key, int defValue) {
        return sharedPreferences.getInt(key, defValue);
    }

    /**
     * Retrieve a long value from the preferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a long.
     * @throws ClassCastException
     */
    @Override
    public long getLong(String key, long defValue) {
        return sharedPreferences.getLong(key, defValue);
    }

    /**
     * Retrieve a float value from the preferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a float.
     * @throws ClassCastException
     */
    @Override
    public float getFloat(String key, float defValue) {
        return sharedPreferences.getFloat(key, defValue);
    }

    /**
     * Retrieve a boolean value from the preferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a boolean.
     * @throws ClassCastException
     */
    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    /**
     * Checks whether the preferences contains a preference.
     *
     * @param key The name of the preference to check.
     * @return Returns true if the preference exists in the preferences,
     * otherwise false.
     */
    @Override
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    /**
     * Create a new Editor for these preferences, through which you can make
     * modifications to the data in the preferences and atomically commit those
     * changes back to the SharedPreferences object.
     *
     * <p>Note that you <em>must</em> call {@link Editor#commit} to have any
     * changes you perform in the Editor actually show up in the
     * SharedPreferences.
     *
     * @return Returns a new instance of the {@link Editor} interface, allowing
     * you to modify the values in this SharedPreferences object.
     */
    @Override
    public Editor edit() {
        return sharedPreferences.edit();
    }

    /**
     * Registers a callback to be invoked when a change happens to a preference.
     *
     * <p class="caution"><strong>Caution:</strong> The preference manager does
     * not currently store a strong reference to the listener. You must store a
     * strong reference to the listener, or it will be susceptible to garbage
     * collection. We recommend you keep a reference to the listener in the
     * instance data of an object that will exist as long as you need the
     * listener.</p>
     *
     * @param listener The callback that will run.
     * @see #unregisterOnSharedPreferenceChangeListener
     */
    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Unregisters a previous callback.
     *
     * @param listener The callback that should be unregistered.
     * @see #registerOnSharedPreferenceChangeListener
     */
    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        unregisterOnSharedPreferenceChangeListener(listener);
    }

    public String getNavButton1QuickPressAction() {
        return sharedPreferences.getString("quickPressNavBtn1Action", context.getString(R.string.defaultNavButton1Action));
    }

    public String getNavButton2QuickPressAction() {
        return sharedPreferences.getString("quickPressNavBtn2Action", context.getString(R.string.defaultNavButton2Action));
    }

    public String getNavButton3QuickPressAction() {
        return sharedPreferences.getString("quickPressNavBtn3Action", context.getString(R.string.defaultNavButton3Action));
    }

    public String getNavButton4QuickPressAction() {
        return sharedPreferences.getString("quickPressNavBtn4Action", context.getString(R.string.defaultNavButton4Action));
    }

    public String getNavButton1LongPressAction() {
        return sharedPreferences.getString("longPressNavBtn1Action", context.getString(R.string.defaultNavButton1LongPressAction));
    }

    public String getNavButton2LongPressAction() {
        return sharedPreferences.getString("longPressNavBtn2Action", context.getString(R.string.defaultNavButton2LongPressAction));
    }

    public String getNavButton3LongPressAction() {
        return sharedPreferences.getString("longPressNavBtn3Action", context.getString(R.string.defaultNavButton3LongPressAction));
    }

    public String getNavButton4LongPressAction() {
        return sharedPreferences.getString("longPressNavBtn4Action", context.getString(R.string.defaultNavButton4LongPressAction));
    }

    public String getNavAddressHintQuickPressAction() {
        return sharedPreferences.getString("quickPressAddressHintAction", context.getString(R.string.defaultAddressHintAction));
    }

    public String getNavAddressHintLongPressAction() {
        return sharedPreferences.getString("longPressAddressHintAction", context.getString(R.string.defaultAddressHintLongPressAction));
    }

    public boolean swipingNavBarUpClosesTab(){
        return true; // todo all the closing tab hints need to be removed if doing this
    }

    public boolean isCustomHomePageBoundToNavBar(){
        return      getNavButton1QuickPressAction().equals("goToCustomHomePage")
                ||  getNavButton2QuickPressAction().equals("goToCustomHomePage")
                ||  getNavButton3QuickPressAction().equals("goToCustomHomePage")
                ||  getNavButton4QuickPressAction().equals("goToCustomHomePage")
                ||  getNavAddressHintQuickPressAction().equals("goToCustomHomePage")
                ||  getNavAddressHintLongPressAction().equals("goToCustomHomePage")
                ||  getNavButton1LongPressAction().equals("goToCustomHomePage")
                ||  getNavButton2LongPressAction().equals("goToCustomHomePage")
                ||  getNavButton3LongPressAction().equals("goToCustomHomePage")
                ||  getNavButton4LongPressAction().equals("goToCustomHomePage");
    }

    /**
     * @param searchTerm Used to pick up on search shortcuts to target a specific search engine
     * @return <String searchEngine (see StringHelper.SEARCH_ENGINE_xxx), String newSearchTerm e.g. with search shortcuts removed if required>
     */
    public Pair<String, String> getTextSearchEngine(String searchTerm, final boolean allowExactMatchOnSearchTerm) {
        String defaultSearchEngine = sharedPreferences.getString("defaultTextSearchEngine", context.getString(R.string.defaultTextSearchEngineValue));
        final boolean isPrivacyMode = getPrivacyLevel(true).equals(PRIVACY_LEVEL_STANDARD) || getPrivacyLevel(true).equals(PRIVACY_LEVEL_SERIOUS) || getPrivacyLevel(true).equals(PRIVACY_LEVEL_EXTREME);
        if(isPrivacyMode){
            defaultSearchEngine = StringHelper.SEARCH_ENGINE_DUCKDUCKGO;
        }
        if(defaultSearchEngine==null){
            defaultSearchEngine = context.getString(R.string.defaultTextSearchEngineValue);
        }
        if(searchTerm!=null) {
            final Pair<String, String> overriddenSearchFromSearchString = StringHelper.getOverriddenSearchFromSearchString(searchTerm, defaultSearchEngine, allowExactMatchOnSearchTerm);
            if (overriddenSearchFromSearchString.first != null) {
                final String searchTermWithShortcutRemoved = overriddenSearchFromSearchString.second;
                return new Pair<>(overriddenSearchFromSearchString.first, searchTermWithShortcutRemoved);
            }
        }
        return new Pair<>(defaultSearchEngine, searchTerm);
    }

    public boolean isPrivateBrowsingModeEnabled(){
        return sharedPreferences.getBoolean("privateMode", false);
    }

    public int getMinFontSize(){
        String pref = sharedPreferences.getString("webContentMinTextSize", "8");
        if(pref==null){
            return 8;
        }
        try {
            return Integer.parseInt(pref);
        }catch(NumberFormatException e){
            return 8;
        }
    }

    public boolean hasPrivacyWarningBeenIssued(){
        return sharedPreferences.getBoolean("privacyModeWarning", false);
    }

    public void setPrivacyWarningIssued(boolean bool){
        sharedPreferences.edit().putBoolean("privacyModeWarning", bool).apply();
    }

    public boolean hasVideoDownloadWarningBeenIssued(){
        return sharedPreferences.getBoolean("vidDlWarning", false);
    }

    public void setVideoDownloadWarningIssued(boolean bool){
        sharedPreferences.edit().putBoolean("vidDlWarning", bool).apply();
    }

    public boolean getClearCookiesOnClose(){
        if(isPrivateBrowsingModeEnabled()){
            return true;
        }
        return sharedPreferences.getBoolean("clearCookiesOnClose", false);
    }

    public boolean isCompactModeEnabled(){
        return sharedPreferences.getBoolean("compactNavBar", false);
    }

    public boolean shouldBlockAds(Item sitePreferences) {
        if(sitePreferences!=null && sitePreferences.blockAds!=null){
            return sitePreferences.blockAds.equals(1);
        }
        return sharedPreferences.getBoolean("blockAds", true)
                || isPrivateBrowsingModeEnabled();
    }

    public boolean shouldShowDesktopMode(Item sitePreferences) {
        if(sitePreferences!=null && sitePreferences.desktopMode!=null){
            return sitePreferences.desktopMode.equals(1);
        }
        return false;
    }

    public boolean shouldBlockTrackingCookies() {
        if(isPrivateBrowsingModeEnabled()){
            return true;
        }
        /*if (sitePreferences != null && sitePreferences.blockAds != null) {
            return sitePreferences.blockAds.equals(1);
        }*/
        return sharedPreferences.getBoolean("blockTrackingCookies", true);
    }

    public boolean shouldBlockGdprWarnings(Item sitePreferences) {
        if(sitePreferences!=null && sitePreferences.blockGdprWarnings!=null){
            // If we have domain specific settings use those, otherwise fall back to browser prefs
            return sitePreferences.blockGdprWarnings.equals(1);
        }
        return getBoolean("hideGdprWarnings", true);
    }

    public boolean shouldBlockGps(Item sitePreferences) {
        if(sitePreferences!=null && sitePreferences.allowGps!=null){
            // If we have domain specific settings use those, otherwise fall back to browser prefs
            return !sitePreferences.allowGps.equals(1);
        }
        return true;
    }

    public String getTrendingSearchLocation() {
        return getString("trendingSearchLocation", null);
    }

    /**
     * !!!THIS GETS SENT FROM CLIENT TO US SO MUST NOT HAVE SENSITIVE DATA IN IT!!!
     * Used for helping with bug reports...
     * @return
     */
    @NonNull
    public String getKeyPreferencesForErrorReporting() {
        String ret;
        ret = "theme:"+getString("themeName", "default")+", ";
        ret += "navBtn1:"+getNavButton1QuickPressAction()+" ("+getNavButton1LongPressAction()+"), ";
        ret += "navBtn2:"+getNavButton2QuickPressAction()+" ("+getNavButton2LongPressAction()+"), ";
        ret += "navBtn3:"+getNavButton3QuickPressAction()+" ("+getNavButton3LongPressAction()+"), ";
        ret += "navBtn4:"+getNavButton4QuickPressAction()+" ("+getNavButton4LongPressAction()+"), ";
        ret += "addressHint:"+getNavAddressHintQuickPressAction()+" ("+getNavAddressHintLongPressAction()+"), ";
        ret += "compactUi:"+(isCompactModeEnabled()?"Y":"N")+", ";
        ret += "autoHideNav:"+(shouldAutoHideNavBar(null)?"Y":"N")+", ";
        ret += "blockAds:"+(shouldBlockAds(null)?"Y":"N")+", ";
        ret += "blockGdprWarnings:"+(shouldBlockGdprWarnings(null)?"Y":"N")+", ";
        ret += "clearCookiesOnClose:"+(getClearCookiesOnClose()?"Y":"N")+", ";
        ret += "privacyMode:"+getPrivacyLevel(false)+", ";
        ret += "privacyModeEnabled:"+(isPrivateBrowsingModeEnabled()?"Y":"N")+", ";
        ret += "nightMode:"+(isNightModeEnabled()?"Y":"N")+", ";
        return ret;
    }

    public boolean isNightModeEnabled() {
        return getBoolean("nightMode", false);
    }

    public int getTextSizePercent() {
        String scaleStr = getString("webContentTextSize", "100");
        if(scaleStr==null){
            scaleStr = "100";
        }
        int scale = 100;
        try{
            scale = Integer.valueOf(scaleStr);
        }catch (Exception e){}
        if(scale > 1000 || scale < 10){
            scale = 100;
        }
        return scale;
    }

    public String getCustomHomePageAddress(String defaultVal) {
        return getString("customHomePage", defaultVal);
    }

    /**
     * What should happen if you swipe up from the nav bar
     */
    public String getGestureSwipeUpNavBar() {
        return getString("gestureSwipeUpNavBar", context.getString(R.string.defaultGestureSwipeUpNavBar));
    }

    /**
     * What should happen if you swipe down when the webview is scrolled to top of the page
     */
    public String getGestureSwipeDownWhenAtTop() {
        return getString("gestureSwipeDownWhenAtTop", context.getString(R.string.defaultGestureSwipeDownWhenAtTop));
    }

    /**
     * Percentage of width of screen used for edge swipe threshold
     * e.g. with a 10% setting it'll register as a swipe action if u start dragging between 0-10% or 90-100%
     */
    public float getGestureSwipeFromEdgeThreshold(final boolean isZoomedIntoPage){
        final String defaultStr = context.getString(R.string.defaultGestureEdgeSwipePercentage);
        String val = getString("gestureSwipeFromEdgeThreshold", defaultStr);
        if(val==null){
            val = defaultStr;
        }
        val = val.trim();
        if(val.endsWith("%")){
            val = val.substring(0, val.length()-1);
        }
        float returnFloat = 9f;
        try{
            returnFloat = Float.valueOf(val);
        } catch (NumberFormatException e){
            // ignore
        }
        if(returnFloat < 0.01f){
            returnFloat = 0f;
        }
        if(returnFloat >= 50f){
            returnFloat = 49.999f;
        }
        if(isZoomedIntoPage && returnFloat >= 5f){
            returnFloat = 3f;
        }
        return returnFloat;
    }

    public String getGestureSwipeFromLeftEdge() {
        if(getGestureSwipeFromEdgeThreshold(false) < 0.1f){
            return "disabled";
        }
        return getString("gestureSwipeFromLeftEdgeV2", context.getString(R.string.defaultGestureSwipeFromLeftEdgeV2));
    }

    public String getGestureSwipeFromRightEdge() {
        if(getGestureSwipeFromEdgeThreshold(false) < 0.1f){
            return "disabled";
        }
        return getString("gestureSwipeFromRightEdgeV2", context.getString(R.string.defaultGestureSwipeFromRightEdgeV2));
    }

    /**
     * By default should the SYSTEM navigation bar be hidden
     * @return
     */
    public boolean shouldHideSystemNavBar() {
        return getBoolean("hideSystemNavBar", context.getString(R.string.defaultHideSystemNavBar).equals("true"));
    }

    /**
     * URL's like tel://, whatsapp:// etc we kinda have no choice but to attempt to open in the app
     * as a browser cannot handle them, but links like https://maps.google.com are regular links
     * and can be viewed in browser or in app. This setting states the default preference for those sorta links
     * @return
     */
    public boolean shouldOpenRegularLinksInAppIfPossible() {
        return getBoolean("openRegularLinksInApp", context.getString(R.string.defaultShouldOpenRegularLinksInAppIfPossible).equals("true"));
    }

    public String shouldDownloadImages() {
        String pref = getString("downloadImages", context.getString(R.string.defaultDownloadImages));
        return pref;
    }

    public float getGestureSwipeToChangeTabsBottomThreshold() {
        String val = getString("gestureSwipeToChangeTabsBottomThreshold", context.getString(R.string.defaultGestureSwipeToChangeTabsBottomThreshold));
        if(val==null){
            return 0f;
        }
        val = val.trim();
        if(val.endsWith("%")){
            val = val.substring(0, val.length()-1);
        }
        float returnFloat = 40f;
        try{
            returnFloat = Float.parseFloat(val);
        } catch (NumberFormatException e){
            // ignore
        }
        if(returnFloat < 0.01f){
            returnFloat = 0f;
        }
        if(returnFloat >= 100f){
            returnFloat = 100f;
        }
        return returnFloat;
    }
}
