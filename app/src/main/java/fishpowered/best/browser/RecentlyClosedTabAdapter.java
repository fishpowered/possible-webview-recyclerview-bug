package fishpowered.best.browser;

import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.WebView;

import fishpowered.best.browser.utilities.DateTimeHelper;
import fishpowered.best.browser.utilities.FileHelper;
import fishpowered.best.browser.utilities.UrlHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Loads and stores tabs that have been closed
 */
public class RecentlyClosedTabAdapter {

    private boolean hasLoaded = false;
    /**
     * This gets lazy loaded so use getClosedTabList() to ensure it's always loaded
     */
    private ArrayList<TabState> _closedTabList;
    public Browser browser;
    private SharedPreferences preferences;

    public RecentlyClosedTabAdapter(Browser context, SharedPreferences preferences){
        this.preferences = preferences;
        this._closedTabList = new ArrayList<TabState>();
        // Initialise starting tabs from any stored to memory
        browser = context;
    }

    public ArrayList<TabState> getClosedTabList() {
        if(!hasLoaded){
            this.loadTabStateFromStore();
        }
        return _closedTabList;
    }

    public ArrayList<TabState> getFilteredClosedTabList(String searchFilter) {
        if(!hasLoaded){
            this.loadTabStateFromStore();
        }
        return TabState.filterList(_closedTabList, searchFilter);
    }

    public void add(TabState closedTabState){
        if(!hasLoaded){
            this.loadTabStateFromStore();
        }
        if(closedTabState==null || closedTabState.address==null || browser.preferences.isPrivateBrowsingModeEnabled()){
            return;
        }
        if(!UrlHelper.isUrlAttempt(closedTabState.address)) {
            return;
        }
        closedTabState.tabClosedTimeStamp = DateTimeHelper.getCurrentTimeStamp();
        for(int i=0; i<getClosedTabList().size(); i++){
            // if any other closed tabs have the same address we should remove them first
            if(closedTabState.address.equalsIgnoreCase(getClosedTabList().get(i).address)){
                getClosedTabList().remove(i);
            }
        }
        // then add the new one to the start of the list
        getClosedTabList().add(0, closedTabState);
        if(getClosedTabList().size() > 8){
            getClosedTabList().remove(getClosedTabList().size() - 1);
        }
        saveTabStateToStore();
    }

    public void loadTabStateFromStore() {
        hasLoaded = true;
        String savedTabDataJsonString = preferences.getString(Browser.preference_savedClosedTabs, "[]");
        _closedTabList = new ArrayList<>();
        //Log.e("CLOSEDFOOMAN", "Restoring closed: "+savedTabDataJsonString);
        try {
            JSONArray tabListJson = new JSONArray(savedTabDataJsonString);
            for(int i=0; i<tabListJson.length(); i++){
                TabState tabState = TabState.loadFromJson(tabListJson.getJSONObject(i));
                if(tabState!=null && tabState.address!=null && !tabState.address.equals("")) {
                    _closedTabList.add(tabState);
                }
            }
        } catch (JSONException e) {
            // Invalid/out of date JSON? might be due to update. Clear the saved tabs and start again
            Log.e("FPERROR", "Error loading tab state from JSON: "+e.getMessage()+". Clearing saved tabs.");
            preferences.edit().putString(Browser.preference_savedClosedTabs, "[]").apply();
        }
    }

    public void saveTabStateToStore(){
        if(!hasLoaded){
            this.loadTabStateFromStore();
        }
        JSONArray jsonTabList = new JSONArray();
        for (TabState state : getClosedTabList()) {
            try {
                JSONObject jsonTabState = state.toJSON();
                if(jsonTabState!=null && !browser.preferences.isPrivateBrowsingModeEnabled()) {
                    jsonTabList.put(jsonTabState);
                    //Log.e("CLOSEDFOOMAN", "Added: "+state.address);
                }
            } catch (JSONException e) {
                //Log.e("CLOSEDFOOMAN", "Unable to serialise tab info: "+e.getMessage());
            }
        }
        preferences.edit().putString(Browser.preference_savedClosedTabs, jsonTabList.toString()).apply();
       // Log.e("CLOSEDFOOMAN", "Saving: "+jsonTabList.toString());

    }

    /**
     * @return int
     */
    public int getPositionForTabId(int tabId){
        if(!hasLoaded){
            this.loadTabStateFromStore();
        }
        int n = 0;
        Iterator iterator = getClosedTabList().iterator();
        while(iterator.hasNext()) { // need to use iterator for concurrency in case items are being removed while iterated
            TabState tab = (TabState) iterator.next();
            if(tab.tabId==tabId){
                return n;
            }
            n++;
        }
        return -1;
    }

    public boolean has(int position) {
        try {
            getClosedTabList().get(position);
            return true;
        }catch(IndexOutOfBoundsException e){
            return false;
        }
    }

    public void removeItemById(int tabId, boolean saveToStore) {
        int index = getPositionForTabId(tabId);
        FileHelper.removeTabThumb(tabId, browser);
        getClosedTabList().remove(index);
        if(saveToStore) {
            saveTabStateToStore();
        }
    }

    public void removeAll(){
        for (TabState tabState : getClosedTabList()) {
            FileHelper.removeTabThumb(tabState.tabId, browser);
        }
        getClosedTabList().clear();
        saveTabStateToStore();
    }

    public WebView getWebViewForTabId(int tabId) {
        try {
            TabState tabState = getClosedTabList().get(getPositionForTabId(tabId));
            return tabState.webView;
        }catch (Exception e){
        }
        return null;
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    public int getItemCount() {
        if(!hasLoaded){
            this.loadTabStateFromStore();
        }
        return _closedTabList.size();
    }

    /**
     * It doesn't make sense to keep a history of tabs closed several weeks ago so this will clean
     * up tabs in the recycle bin or simply reduce the count if we have too many
     */
    public void removeOldItems() {
        int n = 0;
        final int maxListLength = 10;
        final int maxAge = 3600 * 24;
        final Iterator iterator = getClosedTabList().iterator();
        ArrayList<TabState> newClosedTabList = new ArrayList<>();
        while (iterator.hasNext()) {  // need to use iterator for concurrency in case items are being removed while iterated
            TabState tabState = (TabState) iterator.next();
            if(n >= maxListLength || tabState.tabClosedTimeStamp < (DateTimeHelper.getCurrentTimeStamp()-maxAge)) {
                Log.d("FAPPPP", "REMOVING "+tabState.tabId+" "+tabState.address);
                FileHelper.removeTabThumb(tabState.tabId, browser);
                //removeItemById(tabState.tabId, false); causes concurrent editing errors
            }else{
                newClosedTabList.add(tabState);
            }
            n++;
        }
        _closedTabList = newClosedTabList;
        saveTabStateToStore();
    }
}
