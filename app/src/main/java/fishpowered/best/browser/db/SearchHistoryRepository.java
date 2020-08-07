package fishpowered.best.browser.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import android.util.Log;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.RecentlyClosedTabAdapter;
import fishpowered.best.browser.utilities.DateTimeHelper;
import fishpowered.best.browser.utilities.ItemList;
import fishpowered.best.browser.utilities.UrlHelper;

import java.util.ArrayList;

public class SearchHistoryRepository {

    public static final String tableName = "SearchHistory";
    public static final String fieldId = "id";
    public static final String fieldSearch = "search";
    public static final String fieldTimeStamp = "timeStamp";

    @NonNull
    public static void recordSearch(Browser browser, String search){
        if(search==null || search.trim().equals("") || browser.preferences.isPrivateBrowsingModeEnabled() || UrlHelper.isUrlAttempt(search)){
            return;
            //throw new IllegalStateException("History recording wasn't started before attempting to be ended");
        }
        ContentValues values = new ContentValues();
        values.put(SearchHistoryRepository.fieldSearch, search.trim());
        values.put(SearchHistoryRepository.fieldTimeStamp, DateTimeHelper.getCurrentTimeStamp());
        browser.db.insertOrThrow(SearchHistoryRepository.tableName, null, values);
    }

    @NonNull
    public static void clearSearchesBasedOnSearchTerm(SQLiteDatabase writeableDatabase, String term){
        Log.d("FPHISTORY", "Clearing search term: "+term);
        if(term==null || term.equals("")){
            return;
        }
        String[] queryParams = new String[]{term};
        String sql = "DELETE FROM " + SearchHistoryRepository.tableName + " " +
                "WHERE "+SearchHistoryRepository.fieldSearch+" = ?";
        Cursor c = writeableDatabase.rawQuery(sql, queryParams);
        c.moveToFirst();
    }

    public static ItemList getSearchHistory(SQLiteDatabase readonlyDB, Browser browser,
                                                   String searchFilter, ItemList listItems, int pageItemLimit
                                                    ){
        ArrayList<String> queryParams = new ArrayList<String>();
        boolean isOnlyShowingFirstPage = pageItemLimit > 0;
        final boolean shouldSearchHistory = searchFilter != null && searchFilter.length() > 0;
        if(shouldSearchHistory){
            queryParams.add('%'+searchFilter.replace("%", "")+'%');
        }

        String sql = "SELECT "
                + SearchHistoryRepository.fieldId + ", "
                + SearchHistoryRepository.fieldSearch + ", "
                + SearchHistoryRepository.fieldTimeStamp + ", "
                + "COUNT(*) as numberOfSearches";
        sql += " FROM " + SearchHistoryRepository.tableName;
        if(shouldSearchHistory) {
            sql += " WHERE "+SearchHistoryRepository.fieldSearch + " LIKE ?";
        }
        sql += " GROUP BY " + SearchHistoryRepository.fieldSearch +" ";
        sql += " ORDER BY " + SearchHistoryRepository.fieldTimeStamp + " DESC ";

        /*if(pageItemLimit >=0 ) {
            sql += " ORDER BY numberOfSearches DESC, " + SearchHistoryRepository.fieldTimeStamp + " DESC ";
            sql += " LIMIT "+(int) (pageItemLimit+1); // +1 so we can see if the limit has been reached for paging
        }else{
            sql += " ORDER BY " + SearchHistoryRepository.fieldSearch +" ASC";
        }*/
        String[] paramArr = new String[queryParams.size()];
        paramArr = queryParams.toArray(paramArr);
        Cursor c = readonlyDB.rawQuery(sql, paramArr);

        boolean hasResultsHiddenByLimit = false;
        int count=0;
        // Add the spacer before every list heading that has results except the first list
        if(isOnlyShowingFirstPage) {
            listItems.add(ItemRepository.getListSpacingHack(browser.isRestrictedOnVerticalSpaceInSpeedDial));
        }
        try {
            if(c!=null && c.moveToFirst()){
                Item historyHeading = ItemRepository.createListItemHeading(browser.getString(R.string.recent_searches));
                listItems.add(historyHeading);
                do{
                    if(count < pageItemLimit || !isOnlyShowingFirstPage) {
                        Item historyItem       = new Item();
                        historyItem.id         = c.getInt(c.getColumnIndex(SearchHistoryRepository.fieldId));
                        historyItem.address    = c.getString(c.getColumnIndex(SearchHistoryRepository.fieldSearch));
                        historyItem.title      = "\""+c.getString(c.getColumnIndex(SearchHistoryRepository.fieldSearch))+"\"";
                        historyItem.typeId     = ItemType.SEARCH_HISTORY_LINK;
                        listItems.add(historyItem);
                    }else{
                        hasResultsHiddenByLimit = true;
                        break;
                    }
                    count++;
                }while(c.moveToNext());
            }else{
                /*Item addFaveTip = new Item();
                addFaveTip.title = browser.getString(R.string.none_found);
                addFaveTip.typeId = ItemType.LIST_HINT;
                listItems.add(addFaveTip);*/
            }
            if(hasResultsHiddenByLimit && pageItemLimit > 1){
                Item fave = ItemRepository.createInlineShowMoreButton(ItemType.SEARCH_HISTORY_LINK);
                listItems.add(fave);
            }

        }catch(CursorIndexOutOfBoundsException e){
            c.close();
        }finally {
            if (c != null)
                c.close();
            //db.close();
        }
        return listItems;
    }


    /**
     * When searching from any speed dial list, we should include a few matches
     * from our search history
     * @param readonlyDB
     * @param browser
     * @param searchFilter
     * @param listItems
     * @return
     */
    public static ItemList addSearchHistoryToSearchSuggestions(SQLiteDatabase readonlyDB, Browser browser,
                                                               String searchFilter, ItemList listItems){
        ArrayList<String> queryParams = new ArrayList<String>();
        final boolean shouldSearchHistory = searchFilter != null && searchFilter.length() > 0;
        if(shouldSearchHistory){
            queryParams.add('%'+searchFilter.replace("%", "")+'%');
        }

        String sql = "SELECT "
                + SearchHistoryRepository.fieldId + ", "
                + SearchHistoryRepository.fieldSearch + ", "
                + SearchHistoryRepository.fieldTimeStamp + ", "
                + "COUNT(*) as numberOfSearches";
        sql += " FROM " + SearchHistoryRepository.tableName
                + " WHERE 1=1";
        if(shouldSearchHistory) {
            sql += " AND " + SearchHistoryRepository.fieldSearch + " LIKE ?";
        }
        sql += " GROUP BY " + SearchHistoryRepository.fieldSearch +" ";
        sql += " ORDER BY numberOfSearches DESC, " + SearchHistoryRepository.fieldTimeStamp + " DESC ";
        sql += " LIMIT 5";
        String[] paramArr = new String[queryParams.size()];
        paramArr = queryParams.toArray(paramArr);
        Cursor c = readonlyDB.rawQuery(sql, paramArr);
        int pageItemLimit =  5;
        int count=0;
        // Add the spacer before every list heading that has results except the first list
        if(c.moveToFirst()){
            do{
                if(count < pageItemLimit) {
                    Item historyItem       = new Item();
                    historyItem.id         = c.getInt(c.getColumnIndex(SearchHistoryRepository.fieldId));
                    historyItem.address    = c.getString(c.getColumnIndex(SearchHistoryRepository.fieldSearch));
                    historyItem.title      = "\""+c.getString(c.getColumnIndex(SearchHistoryRepository.fieldSearch))+"\"";
                    historyItem.typeId     = ItemType.SEARCH_HISTORY_LINK;
                    listItems.addIfDomainNotExists(historyItem);
                }else{
                    // hasResultsHiddenByLimit = true;
                    break;
                }
                count++;
            }while(c.moveToNext());
        }
        return listItems;
    }

    public static void pruneHistory(int days, SQLiteDatabase writeableDB, RecentlyClosedTabAdapter recentlyClosedTabAdapter){
        int pruneBeforeTimeStamp = DateTimeHelper.getCurrentTimeStamp() - (days * 86400);
        Log.d("FPHISTORY", "Pruning history older than "+days+" days");
        String[] queryParams = new String[]{String.valueOf(pruneBeforeTimeStamp)};
        String sql = "DELETE FROM " + SearchHistoryRepository.tableName + " WHERE "+SearchHistoryRepository.fieldTimeStamp+" < ?";
        Cursor c = writeableDB.rawQuery(sql, queryParams);
        c.moveToFirst();
    }

    public static void clearRecentHistory(float hours, SQLiteDatabase writeableDB, RecentlyClosedTabAdapter recentlyClosedTabAdapter){
        int pruneBeforeTimeStamp = DateTimeHelper.getCurrentTimeStamp() - (int) (hours * 3600f);
        Log.d("FPHISTORY", "Clearing search history in last "+hours+" hours");
        String[] queryParams = new String[]{String.valueOf(pruneBeforeTimeStamp)};
        String sql = "DELETE FROM " + SearchHistoryRepository.tableName + " WHERE "+SearchHistoryRepository.fieldTimeStamp+" >= ?";
        Cursor c = writeableDB.rawQuery(sql, queryParams);
        c.moveToFirst();
    }

    public static void clearDomain(String domain, SQLiteDatabase writeableDB) {
    }

    public static void clearDate(String date, SQLiteDatabase writeableDatabase) {
        Log.d("FPHISTORY", "Clearing search history for date "+date);
        String[] queryParams = new String[]{String.valueOf(date)};
        String sql = "DELETE FROM " + SearchHistoryRepository.tableName + " " +
                "WHERE date(datetime("+SearchHistoryRepository.fieldTimeStamp+", 'unixepoch', 'localtime')) = ?";
        Cursor c = writeableDatabase.rawQuery(sql, queryParams);
        c.moveToFirst();
    }
}
