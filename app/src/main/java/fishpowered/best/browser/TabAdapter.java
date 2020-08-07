package fishpowered.best.browser;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import fishpowered.best.browser.db.BrowsingHistoryRepository;
import fishpowered.best.browser.utilities.FileHelper;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebBackForwardList;

import fishpowered.best.browser.utilities.UrlHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Data adapter for the browser tab recyclerview i.e. loads and keeps track of tabs
 * Not to be confused with the OpenTabAdapter which keeps track of open and closed tabs
 */
public class TabAdapter extends RecyclerView.Adapter<TabViewHolder>{

    private ArrayList<TabState> tabList;
    public Browser browser;
    private Preferences preferences;
    public Bundle savedInstanceState;

    public TabAdapter(Browser context, Preferences preferences, Bundle savedInstanceState){
        this.preferences = preferences;
        this.savedInstanceState = savedInstanceState;
        this.tabList = new ArrayList<TabState>();
        // Initialise starting tabs from any stored to memory
        browser = context;
        loadTabStateFromStore();
    }

    /**
     * Called when RecyclerView needs a new {@link TabViewHolder} of the given type to represent
     * an item.
     */
    @NonNull
    @Override
    public TabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View browserTabLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.browser_tab_layout, parent, false);
        TabViewHolder tab = new TabViewHolder(browserTabLayout, browser);
        return tab;
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link TabViewHolder#itemView} to reflect the item at the given
     * position.
     */
    @Override
    public void onBindViewHolder(@NonNull TabViewHolder holder, final int position) {
        final TabState tabState = tabList.get(position);
        boolean isChildWebView = tabState.parentWebView!=null; // e.g. launched by js window.open
        holder.bindDataToTab(tabState, isChildWebView);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return tabList.size();
    }

    public void addTabToPosition(TabState tabState, int insertIndex) {
        try {
            tabList.add(insertIndex, tabState);
        }catch(IndexOutOfBoundsException e){
            insertIndex = tabList.size();
            tabList.add(insertIndex, tabState);
            Browser.silentlyLogException(e, browser);
        }
        notifyItemInserted(insertIndex); // @see TabAdapter.onCreateViewHolder
    }

    /**
     * Return the stable ID for the item at <code>position</code>. If {@link #hasStableIds()}
     * @param position Adapter position to query
     * @return the stable ID of the item at position
     */
    @Override
    public long getItemId(int position) {
        return tabList.get(position).tabId;
    }

    /**
     * From state, return page address
     * @param position
     * @return String|null
     * @throws IndexOutOfBoundsException
     */
    public String getAddressAtPosition(int position){
        TabState tab = tabList.get(position);
        return tab.address;
    }

    /**
     * @param position
     * @return
     * @throws IndexOutOfBoundsException
     */
    public int getTabIdAtPosition(int position){
        TabState tab = tabList.get(position);
        return tab.tabId;
    }

    /**
     * @return int
     */
    public int getPositionForTabId(int tabId){
        for(int n = 0; n< tabList.size(); n++) {
            TabState tab = tabList.get(n);
            if(tab.tabId==tabId){
                return n;
            }
        }
        return -1;
    }

    public void updateAddressAtPosition(int position, String url) {
        try {
            TabState tab = tabList.get(position);
            tab.address = url;
            saveBasicTabViewStatesToStore(); // good to call this on address change so if there's a crash we don't lose the users current state, only back history
        }catch(Exception e){
            Log.e("FPERROR", "Error updating address at position");
            Browser.silentlyLogException(e, browser);
        }
    }

    public boolean has(int position) {
        try {
            tabList.get(position);
            return true;
        }catch(IndexOutOfBoundsException e){
            return false;
        }
    }

    public ArrayList<TabState> getTabStates() {
        return tabList;
    }

    public void removeTabAtPositionFromAdapter(int position) {
        if(!has(position)){
            Log.e("FISHPOWERED", "No tab found at position: "+position);
            return;
        }
        TabState tabState = getTabStates().get(position);
        if(browser.preferences.isPrivateBrowsingModeEnabled()){
            // If we are not storing recently closed tabs in private mode then we must clean up the tab thumbs immediately. Otherwise leave it to the recently closed tab adapter
            FileHelper.removeTabThumb(tabState.tabId, browser);
        } else {
            browser.recentlyClosedTabAdapter.add(tabState);
        }
        tabList.remove(position);
        notifyItemRemoved(position);
        //saveAllTabWebViewStatesToStore();
        saveBasicTabViewStatesToStore();
    }

    public void removeAllTabsFromAdapter(boolean notify) {
        for(TabState closingTab : getTabStates()) {
            if(browser.preferences.isPrivateBrowsingModeEnabled()){
                // If we are not storing recently closed tabs in private mode then we must clean up the tab thumbs immediately. Otherwise leave it to the recently closed tab adapter
                FileHelper.removeTabThumb(closingTab.tabId, browser);
            } else {
                browser.recentlyClosedTabAdapter.add(closingTab);
            }
        }
        int total = tabList.size();
        tabList.clear();
        notifyItemRangeRemoved(0, total);
        if(BrowsingHistoryRepository.currentHistory!=null) {
            BrowsingHistoryRepository.endRecording(browser, "closealltabs");
        }
        if(notify) {
            browser.showNotificationHint(browser.getString(R.string.all_tabs_closed), null);
        }
        saveBasicTabViewStatesToStore();
    }

    public void removeUnusedTabs() {
        for(int n=0; n<getItemCount(); n++){
            TabState tabState = getTabStates().get(n);
            if(tabState.address==null){
                removeTabAtPositionFromAdapter(n);
                FileHelper.removeTabThumb(tabState.tabId, browser);
            }
        }
    }

    public int getItemCountOfUsedTabs() {
        int usedTabCount = 0;
        for(int n=0; n<getItemCount(); n++){
            TabState tabState = getTabStates().get(n);
            if(tabState.address!=null && !tabState.address.equals("")){
                usedTabCount++;
            }
        }
        return usedTabCount;
    }

    public void clearStateThatAnyTabsWereOpenedExternally() {
        // If you open a tab from whatsapp, pressing back or immediately closing that tab
        // should return to whatsapp BUT if you switch tabs first then it's likely that
        // you forgot the opened tab was opened externally and pressing back can just take u back to speed dial
        for(TabState tabState : getTabStates()){
            if(tabState.tabOpenedBy.equals(Browser.TAB_OPENED_BY_EXTERNAL_APP)){
                Log.d("FPBACKBTN", "Clearing state that tab was opened externally for address: "+tabState.address);
                tabState.tabOpenedBy = Browser.TAB_OPENED_BY_SPEED_DIAL_OR_SHOULD_RETURN_THERE;
            }
        }
    }

    public void loadTabStateFromStore() {
        if(preferences.isPrivateBrowsingModeEnabled()){
            return;
        }
        if(preferences.getInt(Browser.preference_tutorialsShown, 0) < 1){
            // If the tutorial pages haven't been shown, show them first
            preferences.edit()
                    .putString(Browser.preference_savedOpenTabs, "[]")
                    .putInt(Browser.preference_tutorialsShown, 1)
                    .putInt(Browser.preference_savedTabPosition, 1)
                    .apply();
            TabState tutorialP0 = new TabState();
            tutorialP0.address = "https://facebook.com"; // Privacy policy
            tutorialP0.tabId = getNewTabId();
            tabList.add(tutorialP0);
            TabState tutorialP1 = new TabState();
            tutorialP1.address = "https://instagram.com"; // Welcome page
            tutorialP1.tabId = getNewTabId();
            tabList.add(tutorialP1);
            TabState tutorialP2 = new TabState();
            tutorialP2.address = "https://instagram.com"; // Close tab page
            tutorialP2.tabId = getNewTabId();
            tabList.add(tutorialP2);
            TabState tutorialP3 = new TabState();
            tutorialP3.address = "https://facebook.com";
            tutorialP3.tabId = getNewTabId();
            tabList.add(tutorialP3);
            TabState tutorialP4 = new TabState();
            tutorialP4.address = "https://instagram.com";
            tutorialP4.tabId = getNewTabId();
            tabList.add(tutorialP4);
        }else {
            String savedTabDataJsonString = preferences.getString(Browser.preference_savedOpenTabs, "[]");
            try {
                JSONArray tabListJson = new JSONArray(savedTabDataJsonString);
                int tabsAddedCount = 0;
                for (int i = 0; i < tabListJson.length(); i++) {
                    TabState tabState = TabState.loadFromJson(tabListJson.getJSONObject(i));
                    if (tabState!=null && tabState.address != null && !tabState.address.equals("")) {
                        tabState.restoreStateFromStorage = true;
                        tabList.add(tabState);
                        tabsAddedCount++;
                    }
                }
            } catch (JSONException e) {
                Log.e("FPBROWSER", "UNABLE TO RESTORE TABS, WIPING: " + savedTabDataJsonString);
                // Invalid/out of date JSON? might be due to update. Clear the saved tabs and start again
                preferences.edit().putString(Browser.preference_savedOpenTabs, "[]").apply();
                Browser.silentlyLogException(e, browser);
            }
        }
    }

    // It is important we keep a unique ID for every tab that gets created (reopened ones should use their previous id), so the memory recycling works correctly
    public int getNewTabId() {
        final int lastId = preferences.getInt(Browser.preference_lastUsedTabId, 0);
        final int newId = lastId + 1;
        preferences.edit().putInt(Browser.preference_lastUsedTabId, newId).apply();
        //Log.e("FOOMAN", "Creating new tab ID "+newId);
        return newId;
    }

    /**
     * Fast way to save current tab state that can be called often (in case of crash) but doesn't save back/forward history
     */
    public void saveBasicTabViewStatesToStore(){
        if(preferences.isPrivateBrowsingModeEnabled() || browser.skipSavingTabStateWhilstClosing){
            return;
        }
        // First save just the current URL's and tab IDs into preferences. This is our quick store we update often
        JSONArray jsonTabList = new JSONArray();
        for(TabState state : tabList){
            try {
                JSONObject jsonTabState = state.toJSON();
                if(jsonTabState!=null) {
                    jsonTabList.put(jsonTabState);
                }
            } catch (JSONException e) {
                Log.e("FISHPOWERED", "Unable to serialise tab info: "+e.getMessage());
                Browser.silentlyLogException(e, browser);
            }
        }
        preferences.edit().putString(Browser.preference_savedOpenTabs, jsonTabList.toString()).apply();
    }

    /**
     * Slow store of each tabs full back/forward history and current addresses
     */
    public void saveFullTabWebViewStatesToStore(){
        if(preferences.isPrivateBrowsingModeEnabled() || browser.skipSavingTabStateWhilstClosing){
            return;
        }
        saveBasicTabViewStatesToStore();
        for(TabState state : tabList){
            if(state.webView!=null && state.address!=null && !state.address.equals("")){
            }
        }
    }
}
