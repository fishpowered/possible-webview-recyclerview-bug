package fishpowered.best.browser.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;

import fishpowered.best.browser.utilities.DateTimeHelper;
import fishpowered.best.browser.utilities.StringHelper;
import fishpowered.best.browser.utilities.UrlHelper;

import android.text.format.DateUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebStorage;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.RecentlyClosedTabAdapter;

import fishpowered.best.browser.R;
import fishpowered.best.browser.TabViewHolder;
import fishpowered.best.browser.utilities.ItemList;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

public class BrowsingHistoryRepository {

    public static final String tableName = "History";
    public static final String fieldId = "id";
    public static final String fieldTitle = "title";
    public static final String fieldAddress = "address";
    public static final String fieldDomain = "domain";
    public static final String fieldTabId = "tabId"; // currently not used but this might be useful if we ever want to group history by tab
    public static final String fieldBeginTimeStamp = "beginTimeStamp";
    /**
     * @deprecated Not used since 1.1.9
     */
    public static final String fieldEndTimeStamp = "endTimeStamp";
    public static HistoryItem currentHistory;

    /**
     * @deprecated Might repurpose later for responsible usage controls
     * @param browser
     * @param tab
     * @param db
     * @param forceUrl
     * @param forceTabId
     * @param descriptionDebug
     * @return
     */
    public static HistoryItem startRecordingHistoryItem(Browser browser, TabViewHolder tab, SQLiteDatabase db, String forceUrl,
                                                        Integer forceTabId,
                                                        String descriptionDebug){
        return null;
        // Keep for parental controls but needs changing so the duration per site isn't stored in history
        // Keep for parental controls but needs changing so the duration per site isn't stored in history
        /*if(tab==null || !tab.getBrowser().isViewVisible(tab.webView)){
            //Log.d("FPHISTORY", "[SKIPPED-"+descriptionDebug+"] "+webView.getUrl());
            return null;
        }
        if(browser.preferences.isPrivateBrowsingModeEnabled()){
            return null;
        }
        String urlToLog = forceUrl!=null ? forceUrl : tab.webView.getUrl(); // forceUrl!=null ? forceUrl :  weirdly gives old url sometimes (this comes from the pageStart/Finish method)
        if(currentHistory!=null && currentHistory.address!=null){
            if(currentHistory.address.equalsIgnoreCase(urlToLog)){
                return currentHistory;
            } else {
                BrowsingHistoryRepository.endRecording(browser, descriptionDebug);
            }
        }
        currentHistory = new HistoryItem();
        //currentHistory.title = webView.getTitle(); see CustomWebChromeClient::onReceivedTitle
        currentHistory.address = urlToLog;
        currentHistory.beginTimeStamp = DateTimeHelper.getCurrentTimeStamp();
        final int tabId = tab.getTabId();
        currentHistory.tabId = forceTabId!=null ? forceTabId : tabId;
        Log.d("FPHISTORY", "[START-"+descriptionDebug+"] "+urlToLog);
        return currentHistory;*/
    }

    /**
     * @deprecated Might repurpose later for responsible usage controls
     * @param browser
     * @param descriptionDebug
     */
    public static void endRecording(Browser browser, String descriptionDebug){
        // Keep for parental controls but needs changing so the duration per site isn't stored in history
        /*if(browser.preferences.isPrivateBrowsingModeEnabled()) {
            currentHistory = null;
            return;
        }
        if(currentHistory==null){
            Log.d("FPHISTORY", "[ERROR] history not started before stopping ");
            return;
            //throw new IllegalStateException("History recording wasn't started before attempting to be ended");
        }
        if(currentHistory.address==null || currentHistory.address.length()==0){
            Log.d("FPHISTORY", "[SKIPPED] Missing address in history recording");
            return;
        }
        final String lowercaseAddress = currentHistory.address.toLowerCase();
        if(!lowercaseAddress.startsWith("http://") && !lowercaseAddress.startsWith("https://") && !lowercaseAddress.startsWith(UrlHelper.OFFLINE_URL_PROTOCOL)) {
            return; // don't record history for link intents like mailto:xxx, data etc
        }
        currentHistory.endTimeStamp = DateTimeHelper.getCurrentTimeStamp();

        ContentValues values = new ContentValues();
        values.put(BrowsingHistoryRepository.fieldTitle, currentHistory.title); // todo trim length?
        values.put(BrowsingHistoryRepository.fieldAddress, currentHistory.address);
        values.put(BrowsingHistoryRepository.fieldDomain, UrlHelper.getDomain(currentHistory.address, true, true, false));
        values.put(BrowsingHistoryRepository.fieldTabId, currentHistory.tabId);
        values.put(BrowsingHistoryRepository.fieldBeginTimeStamp, currentHistory.beginTimeStamp);
        values.put(BrowsingHistoryRepository.fieldEndTimeStamp, currentHistory.endTimeStamp);
        if (currentHistory.getDurationInSeconds() >= 1) { // should stop redirects being caught
            Log.d("FPHISTORY", "[STOP] " + currentHistory.address);
            browser.db.insert(BrowsingHistoryRepository.tableName, null, values);
        } else {
            Log.d("FPHISTORY", "[SKIP] " + currentHistory.address);
        }
        currentHistory = null;
        return;*/
    }

    public static ItemList getTopLevelHistoryGroupedByDateAndDomain(SQLiteDatabase readonlyDB, Browser browser,
                                                                    int dateFilterTimeStamp, String domainFilter){
        ItemList historyItems = new ItemList();
        ArrayList<String> queryParams = new ArrayList<String>();
        String sql = "SELECT "
                + BrowsingHistoryRepository.fieldBeginTimeStamp+", "
                + BrowsingHistoryRepository.fieldDomain+", "
                + BrowsingHistoryRepository.fieldAddress; // hack so we can get whether it's http or https
        sql += " FROM " + BrowsingHistoryRepository.tableName
                + " WHERE 1=1";
        if(dateFilterTimeStamp > 0){
            sql += " AND date(datetime(beginTimeStamp, 'unixepoch', 'localtime')) = ?";
            String dateString = DateTimeHelper.convertTimeStampToDateTimeFormat(dateFilterTimeStamp);
            dateString = dateString.substring(0, 4) + "-" + dateString.substring(5, 7) + "-" + dateString.substring(8, 10);
            queryParams.add(dateString);
        }
        if(domainFilter!=null && !domainFilter.equals("")){
            sql += " AND "+ BrowsingHistoryRepository.fieldDomain+" = ?";
            queryParams.add(domainFilter);
        }
        sql += " GROUP BY date(datetime(beginTimeStamp, 'unixepoch', 'localtime')), " + BrowsingHistoryRepository.fieldDomain+"";
        sql += " ORDER BY " + BrowsingHistoryRepository.fieldId + " DESC LIMIT 10000";
        String[] paramArr = new String[queryParams.size()];
        paramArr = queryParams.toArray(paramArr);
        Cursor c = readonlyDB.rawQuery(sql, paramArr);
        final int maxVisibleItemsPerDay = dateFilterTimeStamp > 0 ? 10000 : 9;
        int countPerDay = 0;
        boolean addedShowMoreHint = false;
        if(c.moveToFirst()){
            int beginTimeStamp;
            String headingTitle;
            ArrayList<String> datesListed = new ArrayList<String>();
            do{
                beginTimeStamp = c.getInt(c.getColumnIndex(BrowsingHistoryRepository.fieldBeginTimeStamp));
                String beginDate = DateTimeHelper.convertTimeStampToLocalDate(beginTimeStamp, browser);
                if (DateUtils.isToday(beginTimeStamp * 1000L)) {
                    headingTitle = browser.getString(R.string.today);
                } else if (DateUtils.isToday((beginTimeStamp * 1000L) + DateUtils.DAY_IN_MILLIS)) {
                    headingTitle = browser.getString(R.string.yesterday);
                } else {
                    headingTitle = beginDate;
                }
                if (headingTitle.length() > 0 && !datesListed.contains(headingTitle)) {
                    // Add date heading
                    countPerDay = 0;
                    addedShowMoreHint = false;
                    Item historyHeading = new Item();
                    historyHeading.id = 0;//c.getInt(c.getColumnIndex(HistoryRepository.fieldId));
                    historyHeading.address = ""+beginTimeStamp; // used for deleting dates on long press of heading
                    historyHeading.title = headingTitle;
                    historyHeading.typeId = ItemType.HEADING_LABEL;

                    if(datesListed.size()>0){
                        historyItems.add(ItemRepository.getListSpacingHack(browser.isRestrictedOnVerticalSpaceInSpeedDial));
                    }
                    historyItems.add(historyHeading);
                    datesListed.add(headingTitle);
                }
                if(countPerDay < maxVisibleItemsPerDay) {
                    String domain;
                    domain = c.getString(c.getColumnIndex(BrowsingHistoryRepository.fieldDomain));
                    String exampleUrl = c.getString(c.getColumnIndex(BrowsingHistoryRepository.fieldAddress));
                    Item historyItem = new Item();
                    historyItem.id = c.getInt(c.getColumnIndex(BrowsingHistoryRepository.fieldDomain));
                    final boolean isHttps = exampleUrl.startsWith("https:");
                    final boolean isHttp = exampleUrl.startsWith("http:");
                    if(isHttp || isHttps) {
                        historyItem.address = isHttps ? "https://" + domain : "http://" + domain;
                        historyItem.title = "• "+domain;
                    }else{
                        continue;
                    }
                    historyItem.typeId = ItemType.BROWSING_HISTORY_LINK_GROUP;
                    countPerDay++;
                    historyItems.add(historyItem);
                }else if(!addedShowMoreHint){
                    historyItems.add(ItemRepository.createInlineShowMoreButton(beginTimeStamp));
                    addedShowMoreHint = true;
                }
            }while(c.moveToNext());
        }else{
            Item historyHeading = new Item();
            historyHeading.id = 0;//c.getInt(c.getColumnIndex(HistoryRepository.fieldId));
            historyHeading.address = null;
            historyHeading.title = browser.getString(R.string.history);
            historyHeading.typeId = ItemType.HEADING_LABEL;
            historyItems.add(historyHeading);

            Item addFaveTip = new Item();
            addFaveTip.title = domainFilter==null ? browser.getString(R.string.your_browsing_history_will_appear_here) : browser.getString(R.string.none_found);
            addFaveTip.typeId = ItemType.LIST_HINT;
            historyItems.add(addFaveTip);
        }
        return historyItems;
    }

    public static ItemList getHistoryByPageTitle(SQLiteDatabase readonlyDB, Browser browser,
                                                 String searchFilter, int dateFilterTimeStamp, String domainFilter){
        domainFilter = UrlHelper.getDomain(domainFilter, true, true, false); // domainFilter is sent with https:// or http://
        ItemList historyItems = new ItemList();
        ArrayList<String> queryParams = new ArrayList<String>();
        final boolean shouldSearchHistory = searchFilter != null && searchFilter.length() > 0;
        String sql = "SELECT "
                + BrowsingHistoryRepository.fieldId + ", "
                + BrowsingHistoryRepository.fieldAddress + ", "
                + BrowsingHistoryRepository.fieldTitle+ ", "
                + BrowsingHistoryRepository.fieldBeginTimeStamp;
        sql += " FROM " + BrowsingHistoryRepository.tableName
                + " WHERE 1=1";
        if(shouldSearchHistory) {
            final String searchStringEscaped = '%' + searchFilter.replace("%", "") + '%';
            sql += " AND ( " + BrowsingHistoryRepository.fieldAddress + " LIKE ? OR " + BrowsingHistoryRepository.fieldTitle + " LIKE ? )";
            queryParams.add(searchStringEscaped);
            queryParams.add(searchStringEscaped);
        }
        if(dateFilterTimeStamp > 0){
            sql += " AND date(datetime(beginTimeStamp, 'unixepoch', 'localtime')) = ?";
            String dateString = DateTimeHelper.convertTimeStampToDateTimeFormat(dateFilterTimeStamp);
            dateString = dateString.substring(0, 4) + "-" + dateString.substring(5, 7) + "-" + dateString.substring(8, 10);
            queryParams.add(dateString);
        }
        final boolean hasDomainFilter = domainFilter != null && !domainFilter.equals("");
        if(hasDomainFilter){
            sql += " AND "+ BrowsingHistoryRepository.fieldDomain+" = ?";
            queryParams.add(domainFilter);
        }
        if(shouldSearchHistory){
            sql += " GROUP BY " + BrowsingHistoryRepository.fieldAddress+" ";
        }else{
            sql += " GROUP BY date(datetime(beginTimeStamp, 'unixepoch', 'localtime')), " + BrowsingHistoryRepository.fieldAddress+"";
        }
        sql += " ORDER BY " + BrowsingHistoryRepository.fieldId + " DESC LIMIT 10000";
        String[] paramArr = new String[queryParams.size()];
        paramArr = queryParams.toArray(paramArr);
        Cursor c = readonlyDB.rawQuery(sql, paramArr);
        final int maxVisibleItemsPerDay = dateFilterTimeStamp > 0 || hasDomainFilter ? 10000 : 5;
        int countPerDay = 0;
        boolean addedShowMoreHint = false;
        if(hasDomainFilter){
            Item historyHeading = new Item();
            historyHeading.id = 0;//c.getInt(c.getColumnIndex(HistoryRepository.fieldId));
            historyHeading.address = null;
            historyHeading.title =  browser.getString(R.string.history)+" > "+domainFilter;
            historyHeading.typeId = ItemType.HEADING_LABEL;
            historyItems.add(historyHeading);
        }else if(!shouldSearchHistory && dateFilterTimeStamp > 0){
            Item historyHeading = new Item();
            historyHeading.id = 0;//c.getInt(c.getColumnIndex(HistoryRepository.fieldId));
            historyHeading.address = null;
            //String dateString = DateTimeHelper.convertTimeStampToLocalDate(dateFilterTimeStamp, browser);
            historyHeading.title =  browser.getString(R.string.history); // +" > "+ dateString;
            historyHeading.typeId = ItemType.HEADING_LABEL;
            historyItems.add(historyHeading);
        }
        if(c.moveToFirst()){
            String historyAddress;
            int beginTimeStamp;
            String headingTitle;
            String pageTitle;
            ArrayList<String> datesListed = new ArrayList<String>();
            do{
                beginTimeStamp = c.getInt(c.getColumnIndex(BrowsingHistoryRepository.fieldBeginTimeStamp));
                String beginDate = DateTimeHelper.convertTimeStampToLocalDate(beginTimeStamp, browser);
                String beginTime = DateTimeHelper.convertTimeStampToHHMM(beginTimeStamp, browser);
                if (DateUtils.isToday(beginTimeStamp * 1000L)) {
                    headingTitle = browser.getString(R.string.today); // hasDomainFilter ? "" :
                } else if (DateUtils.isToday((beginTimeStamp * 1000L) + DateUtils.DAY_IN_MILLIS)) {
                    headingTitle = browser.getString(R.string.yesterday);
                } else {
                    headingTitle = beginDate;
                }
                if (headingTitle.length() > 0 && !datesListed.contains(headingTitle)) {
                    // Add date heading
                    countPerDay = 0;
                    addedShowMoreHint = false;
                    Item historyHeading = new Item();
                    historyHeading.id = 0;//c.getInt(c.getColumnIndex(HistoryRepository.fieldId));
                    historyHeading.address = ""+beginTimeStamp; // used for deleting dates on long press of heading
                    historyHeading.title = headingTitle;
                    historyHeading.typeId = ItemType.HEADING_LABEL;

                    if(datesListed.size()>0){
                        historyItems.add(ItemRepository.getListSpacingHack(browser.isRestrictedOnVerticalSpaceInSpeedDial));
                    }
                    historyItems.add(historyHeading);
                    datesListed.add(headingTitle);
                }
                if(countPerDay < maxVisibleItemsPerDay) {
                    historyAddress = c.getString(c.getColumnIndex(BrowsingHistoryRepository.fieldAddress));
                    pageTitle = c.getString(c.getColumnIndex(BrowsingHistoryRepository.fieldTitle));
                    String historyTitle;
                    historyTitle = getHistoryItemChipViewTitle(historyAddress, pageTitle, beginTime, !hasDomainFilter);
                    Item historyItem = new Item();
                    historyItem.id = c.getInt(c.getColumnIndex(BrowsingHistoryRepository.fieldId));
                    historyItem.address = historyAddress;
                    historyItem.title = historyTitle;
                    historyItem.typeId = ItemType.BROWSING_HISTORY_LINK;
                    historyItem.timePrefix = beginTime;
                    countPerDay++;
                    historyItems.add(historyItem);
                }else if(!addedShowMoreHint){
                    historyItems.add(ItemRepository.createInlineShowMoreButton(beginTimeStamp));
                    addedShowMoreHint = true;
                }
            }while(c.moveToNext());
        }else{
            // If they are
//            Item historyHeading = new Item();
//            historyHeading.id = 0;//c.getInt(c.getColumnIndex(HistoryRepository.fieldId));
//            historyHeading.address = null;
//            historyHeading.title =  browser.getString(R.string.history);
//            historyHeading.typeId = ItemType.HEADING_LABEL;
//            historyItems.add(historyHeading);
//
            Item addFaveTip = new Item();
            addFaveTip.title = browser.getString(R.string.none_found);
            addFaveTip.typeId = ItemType.LIST_HINT;
            historyItems.add(addFaveTip);
        }
        return historyItems;
    }

    private static String getHistoryItemChipViewTitle(String historyAddress, String pageTitle, String beginTime, boolean includeDomain) {
        String historyTitle = "";
        if(pageTitle!=null && !pageTitle.equals("") && !historyAddress.contains("reddit.com/")){
            // todo this should probabaly be moved into getSimplifiedAddressForUseAsTitle and parameterised as "preferTitle"
            final String domainLowerCase = UrlHelper.getDomain(historyAddress, true, true, true).toLowerCase();
            final String domainWithoutTopLevelDomains = UrlHelper.removeTopLevelDomains(domainLowerCase).toLowerCase();
            String pageTitleWithDomainRemoved = pageTitle;
            final String pageTitleLowerCase = pageTitle.toLowerCase();
            if(pageTitleLowerCase.startsWith(domainLowerCase) || pageTitleLowerCase.endsWith(domainLowerCase)) {
                pageTitleWithDomainRemoved = StringHelper.uppercaseFirst(pageTitleWithDomainRemoved.replaceAll("(?i)"+ Pattern.quote(domainLowerCase), ""));
            }
            if(pageTitleLowerCase.startsWith(domainWithoutTopLevelDomains+": ")){
                pageTitleWithDomainRemoved = StringHelper.uppercaseFirst(pageTitleWithDomainRemoved.replaceAll("(?i)"+ Pattern.quote(domainWithoutTopLevelDomains+": "), ""));
            }
            if(pageTitleLowerCase.startsWith(domainWithoutTopLevelDomains+" - ")){
                pageTitleWithDomainRemoved = StringHelper.uppercaseFirst(pageTitleWithDomainRemoved.replaceAll("(?i)"+ Pattern.quote(domainWithoutTopLevelDomains+" - "), ""));
            }
            if(pageTitleLowerCase.endsWith("- "+domainWithoutTopLevelDomains)){
                pageTitleWithDomainRemoved = StringHelper.uppercaseFirst(pageTitleWithDomainRemoved.replaceAll("(?i)"+ Pattern.quote("- "+domainWithoutTopLevelDomains), ""));
            }
            if(includeDomain){
                historyTitle += domainWithoutTopLevelDomains + ": " + pageTitleWithDomainRemoved;
            }else {
                historyTitle += pageTitleWithDomainRemoved;
            }
        } else {
            historyTitle += UrlHelper.getSimplifiedAddressForUseAsTitle(historyAddress, pageTitle, false);
        }
        return historyTitle;
    }

    // not ideal because it doesn't count separate visits but every single page click on that domain
    public static ItemList getHistoryByDomainForSearchSuggestions(SQLiteDatabase readonlyDB, Browser browser,
                                                                  String searchFilter, ItemList historyItems){
        if(historyItems==null) {
            historyItems = new ItemList();
        }
        ArrayList<String> queryParams = new ArrayList<String>();
        final boolean shouldSearchHistory = searchFilter != null && searchFilter.length() > 0;
        if(shouldSearchHistory){
            queryParams.add('%'+searchFilter.replace("%", "")+'%');
        }
        String sql = "SELECT "
                + BrowsingHistoryRepository.fieldId + ", "
                + BrowsingHistoryRepository.fieldAddress + ", "
                + BrowsingHistoryRepository.fieldBeginTimeStamp;
        sql += ", COUNT(*) as mostVisited";
        sql += " FROM " + BrowsingHistoryRepository.tableName
                + " WHERE 1=1";
        if(shouldSearchHistory) {
            sql += " AND " + BrowsingHistoryRepository.fieldDomain + " LIKE ?";
        }
        sql += " AND "+ BrowsingHistoryRepository.fieldBeginTimeStamp + " > ?";
        queryParams.add(String.valueOf(DateTimeHelper.getCurrentTimeStamp() - (48 * 3600)));
        sql += " GROUP BY " + BrowsingHistoryRepository.fieldDomain;
        sql += " ORDER BY mostVisited DESC LIMIT 10";
        String[] paramArr = new String[queryParams.size()];
        paramArr = queryParams.toArray(paramArr);
        Cursor c = readonlyDB.rawQuery(sql, paramArr);
        if(c.moveToFirst()){
            String historyAddress;
            int beginTimeStamp;
            String headingTitle;
            ArrayList<String> headingsListed = new ArrayList<String>();

            /*headingTitle = browser.getString(R.string.most_visited_last_48h);//browser.getString(R.string.most_popular);
            Item historyHeading = new Item();
            historyHeading.id = c.getInt(c.getColumnIndex(BrowsingHistoryRepository.fieldId));
            historyHeading.address = null;
            historyHeading.title = headingTitle;
            historyHeading.typeId = ItemType.HEADING_LABEL;
            historyItems.add(historyHeading);
            headingsListed.add(headingTitle);*/
            do{
                historyAddress = c.getString(c.getColumnIndex(BrowsingHistoryRepository.fieldAddress));
                String historyTitle;
                historyTitle = UrlHelper.getDomain(historyAddress, true, true, true);
                historyAddress = UrlHelper.getDomain(historyAddress, false, false, false);
                Item historyItem       = new Item();
                historyItem.id         = c.getInt(c.getColumnIndex(BrowsingHistoryRepository.fieldId));
                historyItem.address    = historyAddress;
                historyItem.title      = historyTitle;
                historyItem.typeId     = ItemType.SEARCH_SUGGESTION_LINK;
                historyItems.add(historyItem);
            }while(c.moveToNext());
        }
        return historyItems;
    }

    public static ItemList getWhatsHotList(SQLiteDatabase readonlyDB, Browser browser,
                                                  String searchFilter, ItemList listItems, int pageItemLimit){
        ArrayList<String> queryParams = new ArrayList<String>();
        final boolean isSearch = searchFilter != null && searchFilter.length() > 0;
        boolean isOnlyShowingFirstPage = pageItemLimit > -1;
        String sql =
        "SELECT COUNT(*) as mostVisits, "
                + BrowsingHistoryRepository.fieldDomain + ", "+BrowsingHistoryRepository.fieldAddress;
        sql += " FROM " + BrowsingHistoryRepository.tableName;
        sql += " WHERE "+ BrowsingHistoryRepository.fieldBeginTimeStamp + " > ?" +
                " AND "+BrowsingHistoryRepository.fieldDomain+"!= ? ";
        queryParams.add(String.valueOf(DateTimeHelper.getCurrentTimeStamp() - (48 * 3600)));
        queryParams.add("fishpowered.net");
        if(isSearch) {
            sql += " AND " + BrowsingHistoryRepository.fieldDomain + " LIKE ?";
        }
        if(isSearch){
            queryParams.add('%'+searchFilter.replace("%", "")+'%');
        }
        sql += " GROUP BY " + BrowsingHistoryRepository.fieldDomain
            + " ORDER BY mostVisits DESC";
        if(isOnlyShowingFirstPage){
            sql += " LIMIT "+(pageItemLimit +1);
        }
        String[] paramArr = new String[queryParams.size()];
        paramArr = queryParams.toArray(paramArr);
        Cursor c = readonlyDB.rawQuery(sql, paramArr);
        String historyAddress;
        String headingTitle;
        ArrayList<String> headingsListed = new ArrayList<String>();

        Item whatsHotHeading = getWhatsHotHeading(browser);
        boolean hasResultsHiddenByLimit = false;
        int count=0;
        if(c.moveToFirst()){
            listItems.add(whatsHotHeading);
            do{
                if(count < pageItemLimit || !isOnlyShowingFirstPage) {
                    historyAddress = c.getString(c.getColumnIndex(BrowsingHistoryRepository.fieldAddress));
                    String historyTitle;
                    historyTitle = UrlHelper.getDomain(historyAddress, true, true, true);
                    historyAddress = UrlHelper.getDomain(historyAddress, false, false, false);
                    Item historyItem       = new Item();
                    //historyItem.id         = c.getInt(c.getColumnIndex(HistoryRepository.fieldId));
                    historyItem.address    = historyAddress;
                    historyItem.title      = historyTitle; // debug: " "+(c.getInt(c.getColumnIndex("mostVisits")) / 60)+"mins = "+
                    historyItem.typeId     = ItemType.WHATS_HOT_LINK;
                    listItems.add(historyItem);
                }else{
                    hasResultsHiddenByLimit = true;
                    break;
                }
                count++;
            }while(c.moveToNext());
        }else{
            if(!isSearch) {
                listItems.add(whatsHotHeading);
                Item addFaveTip = ItemRepository.createListHint(browser.getString(R.string.sites_you_recently_visited_will_appear_here));
                listItems.add(addFaveTip);
            }
        }
        if(hasResultsHiddenByLimit && pageItemLimit > 1){
            Item fave = ItemRepository.createInlineShowMoreButton(ItemType.WHATS_HOT_LINK);
            listItems.add(fave);
        }
        return listItems;
    }

    private static Item getWhatsHotHeading(Browser browser) {
        String headingTitle = browser.getString(R.string.most_time_spent_last_48h);
        Item historyHeading = new Item();
        //c.getInt(c.getColumnIndex(HistoryRepository.fieldId));
        historyHeading.address = null;
        historyHeading.title = headingTitle;
        historyHeading.typeId = ItemType.HEADING_LABEL;
        return historyHeading;
    }

    public static void clearCookiesForWebsitesVisitedInTimeRange(Browser browser, String beginDateTime, String endDateTime){
        Log.d("FPHISTORY", "Clearing cookines between "+beginDateTime+" and "+endDateTime);
        String[] domains = BrowsingHistoryRepository.getDomainsVisitedForTimePeriod(browser.db, beginDateTime, endDateTime);
        Log.d("FPHISTORY", "Found "+domains.length+" domains:");
        try {
            Log.d("FPHISTORY", "..." + domains[0]+ ", "+ domains[1]+ ", "+ domains[2]+ " etc");
        }catch(Exception e){}
        clearCookiesForDomains(browser, domains);
    }

    private static String[] getDomainsVisitedForTimePeriod(SQLiteDatabase db, String beginDateTime, String endDateTime) {
        ArrayList<String> queryParams = new ArrayList<String>();
        String sql = "SELECT "
                + BrowsingHistoryRepository.fieldDomain;
        sql += " FROM " + BrowsingHistoryRepository.tableName
                + " WHERE 1=1";
        sql += " AND datetime(beginTimeStamp, 'unixepoch', 'localtime') >= ?";
        sql += " AND datetime(beginTimeStamp, 'unixepoch', 'localtime') <= ?";
        queryParams.add(beginDateTime);
        queryParams.add(endDateTime);
        sql += " GROUP BY " + BrowsingHistoryRepository.fieldDomain + " ";
        String[] paramArr = new String[queryParams.size()];
        paramArr = queryParams.toArray(paramArr);
        Cursor c = db.rawQuery(sql, paramArr);
        HashSet<String> domains = new HashSet<>();
        String domain;
        if (c.moveToFirst()) {
            do {
                domain = c.getString(0);
                if(domain!=null){
                    domains.add(domain);
                }

            }while (c.moveToNext());
        }
        String[] noDupArray = new String[domains.size()];
        return domains.toArray(noDupArray);
    }

    public static void clearCookiesForDomains(Browser browser, String[] domains) {
        CookieSyncManager.createInstance(browser);
        CookieManager cookieManager = CookieManager.getInstance();
        String domain;
        for(int n=0; n<domains.length; n++) {
            domain = domains[n];
            String cookieString = cookieManager.getCookie(domain);
            if(cookieString!=null) {
                String[] cookies = cookieString.split(";");

                for (int i = 0; i < cookies.length; i++) {
                    String[] cookieParts = cookies[i].split("=");
                    Log.d("FPHISTORY", "Clearing cookies from '" + domain + "': " + cookieParts[0].trim());
                    cookieManager.setCookie(domain, cookieParts[0].trim() + "=; Expires=Wed, 31 Dec 2010 23:59:59 GMT"); // in the past will clear the cookie
                }
            }
        }
        CookieSyncManager.getInstance().sync();
    }

    public static void pruneHistory(int hours, SQLiteDatabase writeableDB, RecentlyClosedTabAdapter recentlyClosedTabAdapter){
        int pruneBeforeTimeStamp = DateTimeHelper.getCurrentTimeStamp() - (hours * 3600);
        Log.d("FPHISTORY", "Pruning history for last "+hours+" hours");
        String[] queryParams = new String[]{String.valueOf(pruneBeforeTimeStamp)};
        String sql = "DELETE FROM " + BrowsingHistoryRepository.tableName + " WHERE "+BrowsingHistoryRepository.fieldBeginTimeStamp+" < ?";
        Cursor c = writeableDB.rawQuery(sql, queryParams);
        c.moveToFirst();
    }

    public static void clearRecentHistory(float hours, Browser browser, RecentlyClosedTabAdapter recentlyClosedTabAdapter){
        Log.d("FPHISTORY", "Clearing history for past "+hours+" hours");
        String beginDateTime = DateTimeHelper.convertTimeStampToDateTimeFormat((int) (DateTimeHelper.getCurrentTimeStamp() - (hours * 3600)));
        String endDateTime = DateTimeHelper.convertTimeStampToDateTimeFormat((int) (DateTimeHelper.getCurrentTimeStamp() + (hours * 3600)));
        clearCookiesForWebsitesVisitedInTimeRange(browser, beginDateTime, endDateTime);
        int pruneBeforeTimeStamp = DateTimeHelper.getCurrentTimeStamp() - (int) (hours * 3600);
        recentlyClosedTabAdapter.removeAll();
        SearchHistoryRepository.clearRecentHistory(hours, browser.db, recentlyClosedTabAdapter);
        String[] queryParams = new String[]{String.valueOf(pruneBeforeTimeStamp)};
        String sql = "DELETE FROM " + BrowsingHistoryRepository.tableName + " WHERE "+BrowsingHistoryRepository.fieldBeginTimeStamp+" >= ?";
        Cursor c = browser.db.rawQuery(sql, queryParams);
        c.moveToFirst();
        BrowsingHistoryRepository.currentHistory = null;
    }

    public static void clearDomainHistory(String domain, Browser browser, RecentlyClosedTabAdapter recentlyClosedTabAdapter){
        Log.d("FPHISTORY", "Cleaing history for domain: "+domain);
        SQLiteDatabase db = DatabaseHelper.getInstance(browser).getWritableDatabase();
        String[] queryParams = new String[]{StringHelper.toString(domain)};
        recentlyClosedTabAdapter.removeAll();
        String sql = "DELETE FROM " + BrowsingHistoryRepository.tableName + " WHERE "+BrowsingHistoryRepository.fieldDomain+" = ?";
        Cursor c = db.rawQuery(sql, queryParams);
        SearchHistoryRepository.clearDomain(domain, browser.db);
        c.moveToFirst();
        if(BrowsingHistoryRepository.currentHistory!=null && BrowsingHistoryRepository.currentHistory.address!=null
                && BrowsingHistoryRepository.currentHistory.address.contains(domain)){
            BrowsingHistoryRepository.currentHistory = null;
        }
        clearCookiesForDomains(browser, new String[]{domain});
        browser.showNotificationHint(String.format(browser.getString(R.string.xxxx_removed_from_history), domain), null);
        browser.speedDialListAdapter.reloadCurrentList(browser.isViewingExpandedSpeedDialListOfType, browser.filterFavesByTagIds, browser.speedDialDomainFilter);
    }

    public static void clearDate(int dateTimeStamp, Browser browser, RecentlyClosedTabAdapter recentlyClosedTabAdapter, @NonNull String domainToClear) {
        String date = DateTimeHelper.convertTimeStampDateFormat(dateTimeStamp, "yyyy-MM-dd");
        clearCookiesForWebsitesVisitedInTimeRange(browser, date+" 00:00:00", date+" 23:59:59");
        recentlyClosedTabAdapter.removeAll();
        String where = "date(datetime(" + BrowsingHistoryRepository.fieldBeginTimeStamp+ ", 'unixepoch', 'localtime')) = ?";
        final String[] queryParams;
        if(domainToClear.length()>0){
            where += " AND "+BrowsingHistoryRepository.fieldDomain+" = ?";
            queryParams = new String[]{String.valueOf(date), domainToClear};
        }else{
            queryParams = new String[]{String.valueOf(date)};
        }
        browser.db.delete(BrowsingHistoryRepository.tableName, where, queryParams);
        SearchHistoryRepository.clearDate(date, browser.db);
    }

    public static void clearAllCookies(Browser context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().removeSessionCookies(null);
            CookieManager.getInstance().flush();
        } else
        {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
            cookieSyncManager.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncManager.stopSync();
            cookieSyncManager.sync();
        }
    }

    public static void clearStorage(Context context) {
        final WebView mockWebView = new WebView(context);
        mockWebView.clearCache(true);
        mockWebView.clearFormData();
        mockWebView.clearHistory();
        mockWebView.clearSslPreferences();
        WebStorage.getInstance().deleteAllData();
    }

    public static void clearAllBrowsingData(Browser browser) {
        browser.lastClosedTabAddress = null;
        browser.lastClosedTabId = -1;
        browser.lastClosedTabPosition = -1;
        BrowsingHistoryRepository.currentHistory = null;

        BrowsingHistoryRepository.clearStorage(browser);
        BrowsingHistoryRepository.clearAllCookies(browser);
        SQLiteDatabase writableDatabase = DatabaseHelper.getInstance(browser).getWritableDatabase();
        BrowsingHistoryRepository.pruneHistory(-1, writableDatabase, browser.recentlyClosedTabAdapter);
        SearchHistoryRepository.pruneHistory(-1, writableDatabase, browser.recentlyClosedTabAdapter);
        browser.recentlyClosedTabAdapter.removeAll();
    }

    public static String deferredHistoryUrl = null;
    public static String deferredHistoryDomain = null;
    public static Integer deferredHistoryTimeStamp = null;
    public static Integer deferredHistoryTabId = null;

    /**
     * Save a visited page history to the history table.
     * Note: we won't necessarily have the page title until the page is partly loaded so we defer
     * saving of the history record until we either get the page title or receive the next URL to save
     * @param pageTitleIfKnown
     * @param url
     * @param browser
     */
    public static void saveHistoryRecord(final String pageTitleIfKnown, final String url, final Integer tabId, Browser browser) {
        if(!UrlHelper.isUrlAttempt(url) || browser.preferences.isPrivateBrowsingModeEnabled()) {
            return;
        }
        final String lowercaseAddress = url.toLowerCase();
        if(!lowercaseAddress.startsWith("http://") && !lowercaseAddress.startsWith("https://") && !lowercaseAddress.startsWith(UrlHelper.OFFLINE_URL_PROTOCOL)) {
            return; // don't record history for link intents like mailto:xxx, data:// etc
        }

        // Ensure we finish any previously deferred history before saving/deferring the new recording
        flushDeferredHistorySaving(null, null, browser);

        final String domain = UrlHelper.getDomain(url, true, true, false);
        int timeStamp = DateTimeHelper.getCurrentTimeStamp();
        deferredHistoryUrl = url;
        deferredHistoryTimeStamp = timeStamp;
        deferredHistoryDomain = domain;
        deferredHistoryTabId = tabId;
        if(pageTitleIfKnown !=null){
            // If we already know the page title, we have everything we need to save the history
            flushDeferredHistorySaving(pageTitleIfKnown, url, browser);
        }
    }

    /**
     * We won't necessarily have the page title until the page is partly loaded so we defer
     * saving of the history record until we either get the page title or receive the next URL to save
     * @param pageTitleIfExists
     * @param urlOfPageTitle
     * @param context
     */
    public static void flushDeferredHistorySaving(String pageTitleIfExists, String urlOfPageTitle, Context context){
        if(pageTitleIfExists!=null && urlOfPageTitle!=null){
            if(!urlOfPageTitle.equals(deferredHistoryUrl)) {
                // We loaded a URL without a page title so deferred recording, then maybe it redirected to a different URL
                // or user switched tab while it was loading
                pageTitleIfExists = null;
            }else if(pageTitleIfExists.equals(urlOfPageTitle)){
                // Oddly onReceivedTitle can pass the URL as the page title *sometimes*
                pageTitleIfExists = null;
            }
        }
        if(deferredHistoryUrl==null || deferredHistoryUrl.equals("")){
            return;
        }
        InsertHistoryRecordAsync insertHistoryAsync = new InsertHistoryRecordAsync(DatabaseHelper.getInstance(context).getWritableDatabase());
        // Todo maybe saving page keywords would be useful for searching as well...
        insertHistoryAsync.execute(pageTitleIfExists, deferredHistoryUrl, deferredHistoryDomain, ""+deferredHistoryTimeStamp, deferredHistoryTabId!=null ? String.valueOf(deferredHistoryTabId) : null);
        // Clear so we don't flush the same info twice
        deferredHistoryUrl = null;
        deferredHistoryTimeStamp = null;
        deferredHistoryDomain = null;
        deferredHistoryTabId = null;
    }

    public static void deleteHistoryRecordById(Context context, int id) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        db.delete(BrowsingHistoryRepository.tableName, "id=?", new String[]{String.valueOf(id)});
    }

    private static class InsertHistoryRecordAsync extends AsyncTask<String, Void, Void>{
        private final WeakReference<SQLiteDatabase> dbRef;
        public InsertHistoryRecordAsync(SQLiteDatabase db){
            dbRef = new WeakReference<>(db);
        }

        @Override
        protected Void doInBackground(String... params) {
            SQLiteDatabase db = dbRef.get();
            if(db==null){
                return null;
            }
            if(params.length!=5 || params[1]==null){
                throw new RuntimeException("Insufficient history saving params");
            }
            ContentValues values = new ContentValues();
            Log.d("FPHISTORYNEW", "Saving "+params[0]+" - "+params[1]+" - "+params[2]+" - "+params[3]);
            final int maxTitleLength = 120; // gotta keep an eye on those SEO tricksters
            final String croppedTitle = params[0]!=null && params[0].length() > maxTitleLength ? params[0].substring(0, maxTitleLength) : params[0];
            values.put(BrowsingHistoryRepository.fieldTitle, croppedTitle);
            values.put(BrowsingHistoryRepository.fieldAddress, params[1]);
            values.put(BrowsingHistoryRepository.fieldDomain, params[2]);
            values.put(BrowsingHistoryRepository.fieldBeginTimeStamp, Integer.parseInt(params[3]));
            values.put(BrowsingHistoryRepository.fieldTabId, Integer.parseInt(params[4]));
            long insertId = db.insertOrThrow(BrowsingHistoryRepository.tableName, null, values);
            return null;
        }
    }
}
