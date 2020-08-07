package fishpowered.best.browser.db;

/**
 * Speed dial list item types
 */
public class ItemType {
    // Types that just appear in UI: headers, buttons,  etc
    public static final int BROWSING_HISTORY_LINK_GROUP = -17; // Chip view that when clicked shows full list of history for domain
    public static final int TAG_FILTER = -16;
    public static final int PREVIOUSLY_READ_ITEMS = -15;
    public static final int READ_LATER_ITEMS = -14;
    /**
     * @deprecated Currently not in use
     */
    public static final int REOPEN_CLOSED_TAB_LINK = -13;
    /**
     * @deprecated Currently not in use
     */
    public static final int OPEN_NEW_TAB_BUTTON = -12;
    public static final int TRENDING_HASH_TAG_LINK = -11;
    public static final int GO_TO_OPEN_TAB_LINK = -10;
    public static final int SEARCH_SUGGESTION_LINK = -9;
    public static final int SEARCH_HISTORY_LINK = -8;
    public static final int LIST_HINT = -7;
    public static final int SHOW_MORE_INLINE_BTN = -6;
    public static final int LOADING = -5;
    public static final int WHATS_HOT_LINK = -4;
    public static final int HEADING_LABEL = -3;
    public static final int BROWSING_HISTORY_LINK = -2;
    public static final int FAVE_ADD_BUTTON = -1;

    // Types that are saved in DB Item table... SO DO NOT CHANGE THESE NUMBERS
    // 0 is reserved for no type!!
    public static final int WEB_SITE = 1;
    public static final int IMAGE = 2;
    public static final int FAVE_QUOTE = 3;
    public static final int VIDEO = 4;
}
