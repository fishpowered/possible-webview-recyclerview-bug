package fishpowered.best.browser;

import android.webkit.WebView;
import android.os.Message;

import fishpowered.best.browser.utilities.UrlHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TabState {

    public String address;

    public String pageTitle;

    /**
     * Important to use a unique ID so tabs can be restored after being closed. Not the same as the adapter position
     */
    public int tabId;

    public String tabOpenedBy = Browser.TAB_OPENED_BY_SPEED_DIAL_OR_SHOULD_RETURN_THERE;

    /**
     * Used so when private mode is ending, we can flag all the open tabs that need their history clearing the user can't press back and see history they shouldn't
     */
    public boolean clearHistoryWhenVisible = false;

    /**
     * Saved when a tab is closed so we can clean up old tabs that were closed ages ago, -1=not closed
     */
    public int tabClosedTimeStamp = -1;

    /**
     * Used on window.open or target=_blank so a link can maintain relation with the tab that opened it
     */
    public WebView.WebViewTransport transport;
    public WebView parentWebView;
    public Message resultMsg;
    public WebView webView; // may not exist!

    /**
     * Restore webview state from file storage (if file exists). This flag gets set to false once restored
     */
    public boolean restoreStateFromStorage = false;

    public JSONObject toJSON() throws JSONException {
        if(address==null || address.trim().equals("")){
            return null;
        }
        JSONObject json = new JSONObject();
        json.put("addr", address.trim());
        json.put("id", tabId);
        //json.put("s", state);
        if(clearHistoryWhenVisible) {
            json.put("clrHis", true);
        }
        if(tabClosedTimeStamp > 0){
            json.put("clsd", tabClosedTimeStamp);
        }
        return json;
    }

    public static TabState loadFromJson(JSONObject json){
        TabState tabState = new TabState();
        try {
            tabState.address = json.getString("addr");
            tabState.tabId = json.getInt("id");
            try {
                tabState.clearHistoryWhenVisible = json.getBoolean("clrHis");
            }catch(JSONException e){
                tabState.clearHistoryWhenVisible = false;
            }
            try {
                tabState.tabClosedTimeStamp = json.getInt("clsd");
            }catch(JSONException e){
                tabState.tabClosedTimeStamp = -1;
            }
            //tabState.state = json.getString("s");
        } catch (JSONException e) {
            return null;
        }
        return tabState;
    }

    public static ArrayList<TabState> filterList(ArrayList<TabState> tabs, String searchFilter){
        if(searchFilter==null || searchFilter.trim().equals("")){
            return tabs;
        }
        String searchableAddress;
        if(searchFilter.trim().length() > 0){
            ArrayList<TabState> searchResults = new ArrayList<>();
            for(int i=0; i<tabs.size(); i++) {
                if(tabs.get(i).address!=null){
                    searchableAddress = UrlHelper.getTrustedAddress(tabs.get(i).address);
                    if(searchableAddress.contains(searchFilter)){
                        searchResults.add(tabs.get(i));
                    }
                }
            }
            return searchResults;
        } else {
            return tabs;
        }
    }
}
