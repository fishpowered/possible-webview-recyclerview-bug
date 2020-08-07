package fishpowered.best.browser.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import fishpowered.best.browser.utilities.ArrayListHelper;
import fishpowered.best.browser.utilities.FileHelper;
import fishpowered.best.browser.utilities.ItemTitleComparator;
import fishpowered.best.browser.utilities.ThemeHelper;
import fishpowered.best.browser.utilities.UrlHelper;

import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.utilities.ItemList;

public class ItemRepository {
    public static final String tableName = "Item";
    public static final String fieldId = "id";
    public static final String fieldTitle = "title";
    public static final String fieldAddress = "address";
    public static final String fieldTrustedAddress = "trustedAddress";
    public static final String fieldThumbAddress = "thumbAddress";
    public static final String fieldTypeId = "typeId"; // See ItemType.xx
    public static final String fieldIsFavourite = "isFavourite";
    public static final String fieldIsTrusted = "isTrusted";
    public static final String fieldReadLaterStatus = "readLaterStatus"; // 1=read later, 2=has been read
    public static final String fieldClickCount = "clickCount";
    public static final String fieldDailyUsageLimit = "dailyUsageLimit";
    public static final String fieldBlockGdprWarnings = "blockGdprWarnings";
    public static final String fieldBlockAds = "blockAds";
    public static final String fieldAllowGps = "allowGps";
    public static final String fieldDesktopMode = "desktopMode";
    public static final String fieldCustomCss = "customCss";
    public static final String fieldCustomJs = "customJs";
    // @TODO add ordering column

    public static boolean isTrustedAddress(String url, SQLiteDatabase readonlyDb) {
        String [] columns = {fieldIsTrusted};
        final String visitedUrl = UrlHelper.getTrustedAddress(url);
        // if visted url contains trusted address at the beginning...
        String q = "SELECT id FROM "+tableName
                +" WHERE "+fieldIsTrusted+" = 1 AND substr(?, 1, length("+fieldTrustedAddress+"))  = "+fieldTrustedAddress
                +" LIMIT 1";
        String [] conditionParams = {visitedUrl}; //  i.e. if reddit.com is trusted then reddit.com/r/whatever is trusted, if only reddit.com/r/whatever is trusted then reddit.com/r/foo is NOT trusted
        Cursor cursor = readonlyDb.rawQuery(q, conditionParams);
        boolean response;
        try {
            response = (cursor.getCount() > 0);
        }catch(CursorIndexOutOfBoundsException e){
            response = false; // query found nothing
        }finally {
            if (cursor != null)
                cursor.close();
            //db.close();
        }
        //Log.e("PAGELOADING", (response ? "Found trusted address " : "Found UNTRUSTED address ")+visitedUrl);
        return response;
    }

    /**
     * When storing info against a site (isFave, isTrusted, blockStuff etc) we need to be able to look it
     * up based on it's address (if it's a page or image), or title (if it's selected text)
     * @param urlOrSelectedText
     * @param typeId
     * @param readonlyDb
     * @param returnSitePreferenceMatchOnly
     * @return Item|null
     */
    public static Item getItemBasedOnAddressOrSelectedText(String urlOrSelectedText, int typeId, SQLiteDatabase readonlyDb, boolean returnSitePreferenceMatchOnly) {
        if(urlOrSelectedText ==null || urlOrSelectedText.length()==0){
            return null;
        }
        String [] columns = {fieldId, fieldIsFavourite, fieldReadLaterStatus, fieldTypeId, fieldIsTrusted, fieldTitle, fieldTrustedAddress, fieldThumbAddress,
                fieldAddress, fieldBlockAds, fieldBlockGdprWarnings, fieldAllowGps, fieldDesktopMode, fieldCustomCss, fieldCustomJs};
        String condition;
        String searchParam;
        if(typeId==ItemType.FAVE_QUOTE) {
            condition = "title = ? and typeId = ?";
            searchParam = urlOrSelectedText.trim();
        }else {
            if(returnSitePreferenceMatchOnly){
                condition = "trustedAddress = ? and typeId = ?";
            } else {
                condition = "address = ? and typeId = ?";
            }
            searchParam = urlOrSelectedText.trim();
        }
        String[] conditionParams = new String[]{searchParam, String.valueOf(typeId)};
        final String limit = "1";
        Item item = null;
        Cursor cursor = null;
        try {
            cursor = readonlyDb.query(tableName, columns,  condition, conditionParams, null, null, null, limit);
            if(cursor.moveToNext()) {
                item = new Item();
                item.bindDbRecord(cursor);
            }
        }catch(CursorIndexOutOfBoundsException e) {
            Log.e("FPERROR", "foooo "+e.getMessage());
            //Browser.silentlyLogException(e, );
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return item!=null && item.id>0 ? item : null;
    }

    public static void deleteItem(SQLiteDatabase db, int itemId){
        if(itemId > 0) {
            String[] conditionParams = {String.valueOf(itemId)};
            db.delete(ItemRepository.tableName, "id = ?", conditionParams);
        }
    }

    public static void deleteSavedForLaterItem(Context context, int itemId){
        if(itemId > 0) {
            SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
            deleteItem(db, itemId);
            FileHelper.removeOfflineWebArchive(itemId, context); // todo on bg thread
        }
    }

    public static void deleteAllFinishedSavedForLaterItems(Context context){
        ItemList items = getReadLaterItems(null, null, context, true);
        for(Item finishedItem : items){
            // Delete individually because we also need to clean up offline content
            deleteSavedForLaterItem(context, finishedItem.id);
        }
    }

    public static void unflagSavedForLaterItem(Context context, int itemId){
        if(itemId > 0) {
            SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
            String[] conditionParams = {String.valueOf(itemId)};
            db.execSQL("UPDATE "+ItemRepository.tableName+" SET "+ItemRepository.fieldReadLaterStatus+ " = NULL WHERE id=?", conditionParams);
            FileHelper.removeOfflineWebArchive(itemId, context); // todo on bg thread
        }
    }

    public static void unfaveItem(SQLiteDatabase db, int itemId){
        if(itemId > 0) {
            String[] conditionParams = {String.valueOf(itemId)};
            db.execSQL("UPDATE "+ItemRepository.tableName+" SET "+ItemRepository.fieldIsFavourite+ " = 0 WHERE id=?", conditionParams);
        }
    }

    public static void faveItem(SQLiteDatabase db, int itemId){
        if(itemId > 0) {
            String[] conditionParams = {String.valueOf(itemId)};
            db.execSQL("UPDATE "+ItemRepository.tableName+" SET "+ItemRepository.fieldIsFavourite+ " = 1 WHERE id=?", conditionParams);
        }
    }

    public static void recordFavouriteClick(int itemId, Context context){
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        String[] conditionParams = {String.valueOf(itemId)};
        db.execSQL("UPDATE "+ItemRepository.tableName+" SET "+ItemRepository.fieldClickCount+ " = 1 + "+ItemRepository.fieldClickCount+" WHERE id=?", conditionParams);
    }

    /**
     * If the user typed an address that's also a favourite, we should record it as a click so that fave will rise up the list
     * @param address
     */
    public static void recordTypedAddressAsFavouriteClick(String address, Browser browser){
        if(UrlHelper.isUrlAttempt(address)) {
            // todo make asynchronous
            Item item = getItemBasedOnAddressOrSelectedText(address, ItemType.WEB_SITE, browser.db, false);
            if(item!=null && item.id > 0){
                recordFavouriteClick(item.id, browser);
            }
        }
    }

    public static ItemList getFavourites(Browser browser, String searchFilter, int pageItemLimit,
                                         ItemList faves, int typeFilter,
                                         String title, boolean showIfNoResults, boolean isFirstHeading, ArrayList<Integer> filterByTagIds){
        if(searchFilter==null){
            searchFilter = "";
        }else{
            searchFilter = searchFilter.trim();
        }
        boolean isShowingOnlyFirstPage = pageItemLimit > 0;
        int queryLimit = isShowingOnlyFirstPage ? pageItemLimit +1 : 100000; // +1 so we can see if number of results has been limited by paging
        String sql = "SELECT "
                + "itm."+ItemRepository.fieldId + ", "
                + ItemRepository.fieldTitle + ", "
                + ItemRepository.fieldAddress + ", "
                + ItemRepository.fieldThumbAddress + ", "
                + ItemRepository.fieldTypeId + " "
                + " FROM " + ItemRepository.tableName+" as itm";

        ArrayList<String> queryParams = new ArrayList<>();

        if(filterByTagIds!=null && filterByTagIds.size()>0){
            String item2TagAlias;
            for(int q=0;q<filterByTagIds.size(); q++) {
                item2TagAlias = "i2t"+q;
                // We want to show only results that match *all* selected tags
                sql += " INNER JOIN " + Item2TagRepository.tableName + " as "+item2TagAlias+" ON itm.id="+item2TagAlias+".itemId " +
                        "AND "+item2TagAlias+".tagId = ?";
                queryParams.add(filterByTagIds.get(q)+"");
            }
        }
        sql += " WHERE " + ItemRepository.fieldIsFavourite + " = 1 ";
        if(typeFilter!=0) {
            sql += " AND " + ItemRepository.fieldTypeId + " = ? ";
            queryParams.add("" + typeFilter);
        }
        if(searchFilter.length() > 0){
            sql += " AND ("+ItemRepository.fieldTrustedAddress+" LIKE ? or "+ItemRepository.fieldTitle+" LIKE ?)";
            queryParams.add('%'+searchFilter.replace("%", "")+'%');
            queryParams.add('%'+searchFilter.replace("%", "")+'%');
        }
        if(filterByTagIds!=null && filterByTagIds.size()>0){
            sql+= " GROUP BY itm.id";
        }
        boolean shouldSortAlphabetically = true;
        if(isShowingOnlyFirstPage) {
            switch(typeFilter) {
                case ItemType.WEB_SITE:
                    // At query result level we want to get most clicked faves, we can later take those top 10 and sort them alphabetically
                    sql += " ORDER BY " + ItemRepository.fieldClickCount + " DESC, itm." + ItemRepository.fieldId + " ASC";
                    break;
                case ItemType.IMAGE:
                case ItemType.FAVE_QUOTE:
                case ItemType.VIDEO:
                    sql += " ORDER BY itm."+ItemRepository.fieldId + " DESC";
                    shouldSortAlphabetically = false;
                    break;
            }
        }else{
            switch(typeFilter) {
                case ItemType.WEB_SITE:
                case ItemType.FAVE_QUOTE:
                    sql += " ORDER BY " + ItemRepository.fieldTitle + " COLLATE NOCASE ASC";
                    break;
                case ItemType.IMAGE:
                case ItemType.VIDEO:
                    sql += " ORDER BY itm."+ItemRepository.fieldId + " DESC";
                    break;
                case 0: // showing tag results probably with all sorts of types
                    if(filterByTagIds!=null && filterByTagIds.size()>0) {
                        sql += " ORDER BY " + ItemRepository.fieldTitle + " COLLATE NOCASE ASC";
                    }
                    break;
            }
        }
        sql += " LIMIT ?";
        queryParams.add(""+queryLimit);
        SQLiteDatabase readOnlyDB = DatabaseHelper.getInstance(browser).getReadableDatabase();

        final String[] stringParams = ArrayListHelper.convertToStringArray(queryParams);
        Cursor c = readOnlyDB.rawQuery(sql, stringParams);

        Item listHeading = title!=null ? createListItemHeading(title) : null;
        final boolean isUrlAttempt = UrlHelper.isUrlAttempt(searchFilter);
        int count = 0;
        boolean hasResultsHiddenByLimit = false;
        if(c.moveToFirst()){
            if(!isFirstHeading) {
                // Add the spacer before every list heading that has results except the first list
                faves.add(getListSpacingHack(browser.isRestrictedOnVerticalSpaceInSpeedDial));
            }

            if(listHeading!=null) {
                faves.add(listHeading);
            }
            if(filterByTagIds!=null) {
                boolean addedTags = TagRepository.addTagsToItemList(faves, browser, filterByTagIds, null, false);
                if(addedTags) {
                    faves.add(ItemRepository.getListSpacingHack(false));
                }
            }
            ItemList sortedResultList = new ItemList();
            do{
                if(count < pageItemLimit || !isShowingOnlyFirstPage) {
                    Item fave = new Item();
                    fave.id = c.getInt(c.getColumnIndex(ItemRepository.fieldId));
                    fave.address = c.getString(c.getColumnIndex(ItemRepository.fieldAddress));
                    fave.thumbAddress = c.getString(c.getColumnIndex(ItemRepository.fieldThumbAddress));
                    fave.title = c.getString(c.getColumnIndex(ItemRepository.fieldTitle));
                    fave.typeId = c.getInt(c.getColumnIndex(ItemRepository.fieldTypeId));
                    sortedResultList.add(fave);
                }else{
                    hasResultsHiddenByLimit = true;
                    break;
                }
                count++;
            }while(c.moveToNext());
            if(pageItemLimit==1 && hasResultsHiddenByLimit){
                // e.g. if we're on the fave page showing a single image/quote, let's just make it the show more link
                sortedResultList.get(0).isShowMoreLink = true;
            }
            if (isShowingOnlyFirstPage && shouldSortAlphabetically && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // The query picked the top 10 results by click count but then we should sort that to make it easier to read
                // If a search has been provided this will also sort the results that match the start first
                sortedResultList.sort(new ItemTitleComparator(searchFilter));
            }else{
                // todo sort for older versions
            }
            faves.addAll(sortedResultList);
        }else if(showIfNoResults){
            if(searchFilter.length()==0) {
                if(listHeading!=null) {
                    faves.add(listHeading);
                }
                if(filterByTagIds!=null) {
                    boolean hasTags = TagRepository.addTagsToItemList(faves, browser, filterByTagIds, null, false);
                    if(hasTags) {
                        faves.add(ItemRepository.getListSpacingHack(false));
                    }
                }
                String text;
                if(filterByTagIds!=null && filterByTagIds.size()>0){
                    text = browser.getString(R.string.none_found);
                }else{
                    text = browser.getString(R.string.type_an_address_below_to_start_adding_faves);
                }
                Item addFaveTip = createListHint(text);
                faves.add(addFaveTip);
            }
            //if(Browser.isSearchingSpeedDial()) {
            /*}else{
                Item addFaveTip = new Item();
                addFaveTip.title = browser.getString(R.string.quickly_add_favourites_tip);
                addFaveTip.typeId = ItemType.LIST_HINT;
                faves.add(addFaveTip);
            }*/
        }
        // Include an add button

        if(hasResultsHiddenByLimit && pageItemLimit > 1){
            Item fave = createInlineShowMoreButton(typeFilter);
            faves.add(fave);
        }
        return faves;
    }

    @NonNull
    public static Item createListHint(String text) {
        Item addFaveTip = new Item();
        addFaveTip.title = text;
        addFaveTip.typeId = ItemType.LIST_HINT;
        return addFaveTip;
    }

    @NonNull
    public static Item createListItemHeading(String title) {
        Item listHeading = new Item();
        listHeading.id = (int) (Math.random() * -100000f);
        listHeading.title = title;
        listHeading.typeId = ItemType.HEADING_LABEL;
        return listHeading;
    }

    @NonNull
    public static Item createInlineShowMoreButton(int typeFilter) {
        Item fave = new Item();
        fave.id = (int) (Math.random() * -100000f);
        fave.address = ""+typeFilter;
        fave.title = "...";
        fave.typeId = ItemType.SHOW_MORE_INLINE_BTN;
        fave.isShowMoreLink = true;
        return fave;
    }

    /**
     * The top heading in the list requires no padding/margin. To do this programmatically worked
     * only some of the time due to some weird recycling issue so instead just use an empty list item
     * as a spacer above headings that need it
     */
    public static Item getListSpacingHack(boolean isRestrictedOnVerticalSpaceInSpeedDial) {
        if(isRestrictedOnVerticalSpaceInSpeedDial){
            return null;
        }
        Item spacer = new Item();
        spacer.title = " ";
        spacer.typeId = ItemType.HEADING_LABEL;
        spacer.id = (int) (Math.random() * -100000f);
        return spacer;
    }

    public static Item selectItemById(int id, SQLiteDatabase db) {
        String [] columns = {"*"};
        String condition = "id = ?";
        String [] conditionParams = {String.valueOf(id)}; //  i.e. if reddit.com is trusted then reddit.com/r/whatever is trusted, if only reddit.com/r/whatever is trusted then reddit.com/r/foo is NOT trusted
        Cursor c = db.query(tableName, columns,  condition, conditionParams, null, null, null, null);
        if(c ==null){
            return null;
        }
        if(c.moveToNext()){
            Item item = new Item();
            item.bindDbRecord(c);
            return item;
        }else{
            return null;
        }
    }

    public static Item save(Context context, int editId, String address, String title, int dailyUsageLimit,
                            boolean isFave, boolean isTrusted, int typeId, String thumbAddress, SQLiteDatabase db, Integer readLaterStatus) {
        // TODO this isn't clear how it works on update. It would be better to pass in Item so then if it's existing u know exactly what will be saved
        if(title == null){
            title = UrlHelper.generateFaveTitleBasedOnAddress(address);
        }
        address = UrlHelper.appendHttpProtocolToUrl(address, true);
        if(db==null) {
            db = DatabaseHelper.getInstance(context).getWritableDatabase();
        }
        ContentValues values = new ContentValues();
        Item item = new Item();
        values.put(ItemRepository.fieldAddress, address);
        item.address = address;
        final String trustedAddress = UrlHelper.getTrustedAddress(address);
        values.put(ItemRepository.fieldTrustedAddress, trustedAddress);
        item.trustedAddress = trustedAddress;
        values.put(ItemRepository.fieldTitle, title);
        item.title = title;
        if(dailyUsageLimit > 0) {
            values.put(ItemRepository.fieldDailyUsageLimit, dailyUsageLimit);
            item.dailyUsageLimit = dailyUsageLimit;
        }
        if(thumbAddress!=null) {
            values.put(ItemRepository.fieldThumbAddress, !thumbAddress.trim().equals("") ? thumbAddress : null);
        }
        final int isFaveInt = isFave ? 1 : 0;
        values.put(ItemRepository.fieldIsFavourite, isFaveInt);
        item.isFavourite = isFaveInt;
        final int isTrustedInt = isTrusted ? 1 : 0;
        values.put(ItemRepository.fieldIsTrusted, isTrustedInt);
        item.isTrusted = isTrustedInt;
        values.put(ItemRepository.fieldTypeId, typeId);
        item.typeId = typeId;
        item.readLaterStatus = readLaterStatus;
        if(readLaterStatus!=null){
            values.put(ItemRepository.fieldReadLaterStatus, readLaterStatus);
        }
        int returnId = -1;
        try {
            if (editId > 0) {
                db.update(ItemRepository.tableName, values, "id=?", new String[]{String.valueOf(editId)});
                returnId = editId;
            } else {
                returnId = (int) db.insertOrThrow(ItemRepository.tableName, null, values);
            }
        }catch(Exception e){
            Toast.makeText(context, context.getString(R.string.error_saving_please_try_again), Toast.LENGTH_SHORT).show();
            Browser.silentlyLogException(e, context);
        }
        if(returnId>-1){
            item.id = returnId;
            return item;
        }else{
            return null;
        }
    }

    @NonNull
    public static void updateSitePreferences(Item sitePreferencesItemIfExists, SQLiteDatabase db) {
        if(sitePreferencesItemIfExists==null || sitePreferencesItemIfExists.id==0){
            return;
        }
        ContentValues values = new ContentValues();
        values.put(ItemRepository.fieldBlockAds, sitePreferencesItemIfExists.blockAds);
        values.put(ItemRepository.fieldDesktopMode, sitePreferencesItemIfExists.desktopMode);
        values.put(ItemRepository.fieldBlockGdprWarnings, sitePreferencesItemIfExists.blockGdprWarnings);
        values.put(ItemRepository.fieldAllowGps, sitePreferencesItemIfExists.allowGps);
        values.put(ItemRepository.fieldCustomCss, sitePreferencesItemIfExists.customCss);
        values.put(ItemRepository.fieldCustomJs, sitePreferencesItemIfExists.customJs);
        db.update(ItemRepository.tableName, values, "id=?", new String[]{String.valueOf(sitePreferencesItemIfExists.id)});
    }



    /**
     * Count of "Saved for later" items that have already been read
     * @param context
     * @return
     */
    private static int getCountOfFinishedSavedForLaterItems(Context context) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getReadableDatabase();
        String [] columns = {"count(*)"};
        String [] conditionParams = {String.valueOf(Item.ITEM_IS_SAVED_FOR_LATER_AND_HAS_BEEN_READ)};
        Cursor cursor = null;
        try {
            cursor = db.query(ItemRepository.tableName, columns,  ItemRepository.fieldReadLaterStatus+"=?", conditionParams, null, null, null, null);
            cursor.moveToNext();
            return cursor.getInt(0);
        }catch(CursorIndexOutOfBoundsException e){

        } finally {
            if (cursor != null)
                cursor.close();
        }
        return 0;
    }

    public static Item getItemToSaveSitePrefsAndSettingsAgainst(String fullPageUrl, boolean createIfDoesntExist, Browser browser) {
        final String domainForSitePrefs = UrlHelper.getAddressForSavingSiteSettingsAgainst(fullPageUrl);
        Item existingPrefs = ItemRepository.getItemBasedOnAddressOrSelectedText(domainForSitePrefs, ItemType.WEB_SITE, browser.db, true);
        if(existingPrefs==null && createIfDoesntExist){
            final String fullDomain = UrlHelper.getDomain(fullPageUrl, false, false, false);
            existingPrefs = ItemRepository.save(browser, 0, fullDomain, null, 0, false, false, ItemType.WEB_SITE, null, browser.db, null);
        }
        return existingPrefs;
    }

    public static Item getReadLaterItem(String currentPageAddress, SQLiteDatabase readonlyDb) {
        if(currentPageAddress ==null || currentPageAddress.length()==0){
            return null;
        }
        String [] columns = {fieldId, fieldIsFavourite, fieldTypeId, fieldIsTrusted, fieldTitle, fieldTrustedAddress, fieldThumbAddress,
                fieldAddress, fieldIsFavourite, fieldReadLaterStatus, fieldBlockAds, fieldBlockGdprWarnings, fieldAllowGps, fieldDesktopMode};
        String condition;
        String searchParam;
        condition = ItemRepository.fieldAddress+" = ? and "+ItemRepository.fieldReadLaterStatus+" > 0";
        searchParam = currentPageAddress.trim();
        String[] conditionParams = new String[]{searchParam};
        final String limit = "1";
        Item item = null;
        Cursor cursor = null;
        try {
            cursor = readonlyDb.query(tableName, columns,  condition, conditionParams, null, null, null, limit);
            cursor.moveToNext();
            item = new Item();
            item.bindDbRecord(cursor);
        }catch(CursorIndexOutOfBoundsException e){

        } finally {
            if (cursor != null)
                cursor.close();
        }
        return item!=null && item.id>0 ? item : null;
    }

    public static Item addItemToReadLaterList(String address, String title, String thumbAddress, @NonNull Integer itemType, Context context) {
        if(address==null || address.length()==0){
            return null;
        }
        title = title!=null && title.length()>0 ?  title.trim() : UrlHelper.generateFaveTitleBasedOnAddress(address);
        return save(context, 0, address, title, -1, false, false, itemType, thumbAddress,
                DatabaseHelper.getInstance(context).getWritableDatabase(), Item.ITEM_IS_SAVED_FOR_LATER);
    }

    public static void markReadLaterItemAsRead(SQLiteDatabase db, int itemId){
        if(itemId > 0) {
            String [] conditionParams = {String.valueOf(Item.ITEM_IS_SAVED_FOR_LATER_AND_HAS_BEEN_READ), String.valueOf(itemId)};
            db.execSQL("UPDATE " + ItemRepository.tableName + " SET " + ItemRepository.fieldReadLaterStatus + " = ? "
                       + " WHERE id=?", conditionParams);
        }
    }

    public static ItemList getReadLaterItems(String searchFilter, Integer itemLimit, Context context, final boolean finishedItemsOnly) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getReadableDatabase();
        String [] columns = {ItemRepository.fieldId, ItemRepository.fieldTitle, ItemRepository.fieldReadLaterStatus,
        ItemRepository.fieldAddress, ItemRepository.fieldThumbAddress, ItemRepository.fieldTypeId};
        String condition = ItemRepository.fieldReadLaterStatus + " >= ?";
        String [] conditionParams;
        final int savedForLaterStatus = finishedItemsOnly ? Item.ITEM_IS_SAVED_FOR_LATER_AND_HAS_BEEN_READ : Item.ITEM_IS_SAVED_FOR_LATER;
        if(searchFilter!=null && searchFilter.trim().length()>0){
            final String escapedSearch = searchFilter.replace("%", "").trim();
            conditionParams = new String[]{
                String.valueOf(savedForLaterStatus),
                ('%'+ escapedSearch +'%'),
                ('%'+ escapedSearch +'%')
            };
            condition += " AND ("+ItemRepository.fieldTrustedAddress+" LIKE ? or "+ItemRepository.fieldTitle+" LIKE ?)";
        }else{
            conditionParams = new String[]{String.valueOf(savedForLaterStatus)};
        }
        String queryLimit = itemLimit!=null ? String.valueOf(itemLimit+1) : null; // +1 so we can detect if there are more results that are hidden by the limit
        final String orderBy = ItemRepository.fieldId+" DESC";
        Cursor c = db.query(tableName, columns,  condition, conditionParams, null, null, orderBy, queryLimit);
        ItemList itemList = new ItemList();
        int itemCount = 0;
        while(c.moveToNext()){
            itemCount++;
            if(itemLimit!=null && itemCount > itemLimit){
                itemList.add(ItemRepository.createInlineShowMoreButton(ItemType.READ_LATER_ITEMS));
                break;
            }else {
                Item item = new Item();
                item.bindDbRecord(c);
                itemList.add(item);
                if(item.thumbAddress !=null && item.thumbAddress.length()>0 && itemLimit!=null){
                    itemLimit = (int) itemLimit / 2; // thumbs take up more space
                }
            }
        }
        return itemList;
    }

    public static void updateTitleField(int itemIdToUpdateTitle, String title, Context context) {
        if(itemIdToUpdateTitle > 0) {
            SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
            String[] params = new String[]{title, String.valueOf(itemIdToUpdateTitle)};
            db.execSQL("UPDATE " + ItemRepository.tableName + " SET " + ItemRepository.fieldTitle + " = ? "
                    + " WHERE id = ?", params);
        }
    }

    public static String getSavedForLaterAddressBasedOnInternalFileUrl(String url, Context context, boolean returnFakeOfflineAddress) {
        if(url==null){
            return null;
        }
        // e.g. internal File address for saved for later item: file:///data/user/0/fishpowered.best.browser/files/readLater_30.mht
        String[] parts = url.substring(0, url.length()-4).split("/readLater_", 2);
        if(parts.length==2){
            int id = Integer.parseInt(parts[1]);
            Item savedForLaterItem = selectItemById(id, DatabaseHelper.getInstance(context).getReadableDatabase());
            if(savedForLaterItem!=null && !savedForLaterItem.address.equals("")){
                return returnFakeOfflineAddress
                    ? UrlHelper.createOfflineAddress(savedForLaterItem.address, savedForLaterItem.id)
                    : savedForLaterItem.address;
            }
        }
        return null;
    }
}
