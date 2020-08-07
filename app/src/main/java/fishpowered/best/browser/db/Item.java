package fishpowered.best.browser.db;

import android.database.Cursor;

/**
 * Represents an item from the web or an item that gets displayed in a speed dial list.
 * This can be favourites, domains that we store settings against, history etc.
 * See ItemType for available types
 */
public class Item {

    public int id;
    public String title;
    public String address;
    public String trustedAddress;
    public String thumbAddress;
    public int typeId;
    public int isFavourite;
    public int isTrusted;
    public int dailyUsageLimit;
    public boolean isShowMoreLink = false;
    /**
     * @var Integer|null Null fetches from preferences
     */
    public Integer blockGdprWarnings;
    /**
     * @var Integer|null Null fetches from preferences
     */
    public Integer blockAds;
    /**
     * @var Integer|null Null fetches from preferences
     */
    public Integer desktopMode;
    /**
     * @var Integer|null Null fetches from preferences
     */
    public Integer allowGps;

    /**
     * @var Integer|null null see ITEM_IS_SAVED_FOR_xxxx
     */
    public Integer readLaterStatus;

    public static final int ITEM_IS_SAVED_FOR_LATER = 1;
    public static final int ITEM_IS_SAVED_FOR_LATER_AND_HAS_BEEN_READ = 2;

    /**
     * Not saved in DB but useful if selecting multiple tags or whatever
     */
    public boolean isSelected = false;

    /**
     * Custom JS to be run on site
     */
    public String customJs;

    /**
     * Custom CSS to be run on site
     */
    public String customCss;

    /**
     * Used just for outputting times next to links on speed dial
     */
    public String timePrefix;

    public void bindDbRecord(Cursor c) {
        final int idIndex = c.getColumnIndex(ItemRepository.fieldId);
        if(idIndex >-1) {
            id = c.getInt(idIndex);
        }
        final int typeIndex = c.getColumnIndex(ItemRepository.fieldTypeId);
        if(typeIndex>-1) {
            typeId = c.getInt(typeIndex);
        }
        final int titleIndex = c.getColumnIndex(ItemRepository.fieldTitle);
        if(titleIndex>-1) {
            title = c.getString(titleIndex);
        }
        final int addressIndex = c.getColumnIndex(ItemRepository.fieldAddress);
        if(addressIndex>-1) {
            address = c.getString(addressIndex);
        }
        final int thumbAddressIndex = c.getColumnIndex(ItemRepository.fieldThumbAddress);
        if(thumbAddressIndex>-1) {
            thumbAddress = c.getString(thumbAddressIndex);
        }
        final int dailyUsageIndex = c.getColumnIndex(ItemRepository.fieldDailyUsageLimit);
        if(dailyUsageIndex>-1) {
            dailyUsageLimit = c.getInt(dailyUsageIndex);
        }
        final int isFavIndex = c.getColumnIndex(ItemRepository.fieldIsFavourite);
        if(isFavIndex>-1) {
            isFavourite = c.getInt(isFavIndex);
        }
        final int readLaterIndex = c.getColumnIndex(ItemRepository.fieldReadLaterStatus);
        if(readLaterIndex>-1) {
            readLaterStatus = c.getInt(readLaterIndex);
        }
        final int isTrustedIndex = c.getColumnIndex(ItemRepository.fieldIsTrusted);
        if(isTrustedIndex>-1) {
            isTrusted = c.getInt(isTrustedIndex);
        }
        final int trustedAddressIndex = c.getColumnIndex(ItemRepository.fieldTrustedAddress);
        if(trustedAddressIndex>-1) {
            trustedAddress = c.getString(trustedAddressIndex);
        }
        // !!! REMEMBER TO CHECK c.ISNULL FOR NULLABLE VALUES...
        final int blockGdprWarningsIndex = c.getColumnIndex(ItemRepository.fieldBlockGdprWarnings);
        if(blockGdprWarningsIndex>-1 && !c.isNull(blockGdprWarningsIndex)){
            blockGdprWarnings = c.getInt(blockGdprWarningsIndex);
        }
        final int blockAdsIndex = c.getColumnIndex(ItemRepository.fieldBlockAds);
        if(blockAdsIndex>-1 && !c.isNull(blockAdsIndex)){
            blockAds = c.getInt(blockAdsIndex);
        }
        final int desktopModeIndex = c.getColumnIndex(ItemRepository.fieldDesktopMode);
        if(desktopModeIndex>-1 && !c.isNull(desktopModeIndex)){
            desktopMode = c.getInt(desktopModeIndex);
        }
        // !!! REMEMBER TO CHECK c.ISNULL FOR NULLABLE VALUES...
        final int allowGpsIndex = c.getColumnIndex(ItemRepository.fieldAllowGps);
        if(allowGpsIndex>-1 && !c.isNull(allowGpsIndex)){
            allowGps = c.getInt(allowGpsIndex);
        }
        final int customCssIndex = c.getColumnIndex(ItemRepository.fieldCustomCss);
        if(customCssIndex>-1 && !c.isNull(customCssIndex)){
            customCss = c.getString(customCssIndex);
        }
        final int customJsIndex = c.getColumnIndex(ItemRepository.fieldCustomJs);
        if(customJsIndex>-1 && !c.isNull(customJsIndex)){
            customJs = c.getString(customJsIndex);
        }
    }

    public boolean isFavourite() {
        return isFavourite==1;
    }

    /**
     * If the item was only being used for saved for later we can delete it, but if it's also
     * site prefs or a favourite then we can't just delete it if it's removed from saved for later
     * @return
     */
    public boolean isMoreThanJustSavedForLater() {
        return isFavourite()
                || blockGdprWarnings!=null
                || blockAds!=null
                || desktopMode!=null
                || allowGps!=null
                || customJs!=null
                || customCss!=null;
    }

    public boolean isFlaggedToBeReadLaterOrAlreadyRead(){
        return readLaterStatus!=null && readLaterStatus > 0;
    }
}
