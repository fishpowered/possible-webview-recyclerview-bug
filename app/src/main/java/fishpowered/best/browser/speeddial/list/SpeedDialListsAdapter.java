package fishpowered.best.browser.speeddial.list;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import fishpowered.best.browser.db.ItemType;
import fishpowered.best.browser.speeddial.itemadapters.AbstractItemAdapter;
import fishpowered.best.browser.speeddial.itemadapters.FaveItemAdapter;
import fishpowered.best.browser.speeddial.itemadapters.HistoryItemAdapter;
import fishpowered.best.browser.speeddial.itemadapters.WhatsHotItemAdapter;
import fishpowered.best.browser.utilities.ThemeHelper;
import fishpowered.best.browser.utilities.UrlHelper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;

import fishpowered.best.browser.speeddial.itemadapters.ReadLaterListItemAdapter;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Adapter for the lists/lists that make up the speed dial e.g. fetches the fave list, hot list, tab list, history list
 */
public class SpeedDialListsAdapter extends RecyclerView.Adapter<SpeedDialListViewHolder>{

    public final LinearLayoutManager linearLayoutManager;
    public final RecyclerView.RecycledViewPool itemViewPool;
    public final SpeedDialListsSnapHelper snapHelper;
    private ArrayList<SpeedDialListState> lists;
    public Browser browser;
    private SharedPreferences preferences;
    private SpeedDialListsRecyclerView speedDialListsRecyclerView;

    public SpeedDialListsAdapter(Browser browser, SharedPreferences preferences, SpeedDialListsRecyclerView speedDialListsRecyclerView){
        this.preferences = preferences;
        this.browser = browser;
        itemViewPool = new RecyclerView.RecycledViewPool();
        this.lists = new ArrayList<SpeedDialListState>();
        linearLayoutManager = new SpeedDialListsLinearLayoutManager(browser, LinearLayoutManager.HORIZONTAL, false);
        linearLayoutManager.setStackFromEnd(true);
        this.speedDialListsRecyclerView = speedDialListsRecyclerView;
        this.speedDialListsRecyclerView.setLayoutManager(linearLayoutManager);
        this.speedDialListsRecyclerView.setAdapter(this);
        this.speedDialListsRecyclerView.setItemViewCacheSize(1);
        this.speedDialListsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // add pager behavior
        //PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper = new SpeedDialListsSnapHelper();
        snapHelper.attachToRecyclerView(this.speedDialListsRecyclerView);

        //addOnItemTouchListener(onTouchListener); // use for swiping from edge check
        this.speedDialListsRecyclerView.setNestedScrollingEnabled(true);

        SpeedDialListState faveList = new SpeedDialListState();
        faveList.id = SpeedDialListViewHolder.FAVES_LIST;
        lists.add(faveList);
        SpeedDialListState whatsHotList = new SpeedDialListState();
        whatsHotList.id = SpeedDialListViewHolder.WHATS_HOT_LIST;
        lists.add(whatsHotList);
        SpeedDialListState readLaterList = new SpeedDialListState();
        readLaterList.id = SpeedDialListViewHolder.SAVED_FOR_LATER_LIST;
        lists.add(readLaterList);
        SpeedDialListState historyList = new SpeedDialListState();
        historyList.id = SpeedDialListViewHolder.HISTORY_LIST;
        lists.add(historyList);

        int startingList = browser.preferences.getInt(Browser.preference_lastUsedSpeedDialListPosition, SpeedDialListViewHolder.FAVES_LIST);
        if(startingList>=0){
            scrollToViewList(startingList, false);
        }
    }

    public AbstractItemAdapter getListItemAdapterById(int listId){
        for(SpeedDialListState list : lists){
            if(list.id==listId){
                return list.itemAdapter;
            }
        }
        return null;
    }

    public AbstractItemAdapter getListItemAdapterByPosition(int position){
        if(has(position)){
            return lists.get(position).itemAdapter;
        }
        return null;
    }

    public static int getListColour(int listType, Browser context) {
        switch(listType){
            case SpeedDialListViewHolder.FAVES_LIST:
                return 0xFF000000+ ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.speedDialListFaveTheme, context);
            case SpeedDialListViewHolder.WHATS_HOT_LIST:
                return 0xFF000000+ContextCompat.getColor(context, R.color.speedDialListWhatsHotTheme);
            case SpeedDialListViewHolder.SAVED_FOR_LATER_LIST:
                return 0xFF000000+ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.speedDialListSavedForLaterTheme, context);
            case SpeedDialListViewHolder.HISTORY_LIST:
                return 0xFF000000+ContextCompat.getColor(context, R.color.speedDialListHistoryTheme);
        }
        return 0;
    }

    public void scrollToViewList(int position, boolean animate){
        if(animate) {
            linearLayoutManager.smoothScrollToPosition(speedDialListsRecyclerView, null, position);
        }else{
            linearLayoutManager.scrollToPosition(position);
            switch(position){
                case SpeedDialListViewHolder.HISTORY_LIST:
                    browser.clearHistoryBtn.setAlpha(1);
                    browser.clearHistoryBtn.setVisibility(VISIBLE);
                    //browser.viewClosedTabsSpeedDialBtn.setAlpha(1);
                    //browser.viewClosedTabsSpeedDialBtn.setVisibility(VISIBLE);
                    browser.openDownloadsBtn.setAlpha(0);
                    browser.openDownloadsBtn.setVisibility(GONE );
                    break;
                case SpeedDialListViewHolder.SAVED_FOR_LATER_LIST:
                    browser.clearHistoryBtn.setAlpha(0);
                    browser.clearHistoryBtn.setVisibility(GONE);
                    //browser.viewClosedTabsSpeedDialBtn.setAlpha(0);
                    //browser.viewClosedTabsSpeedDialBtn.setVisibility(GONE);
                    browser.openDownloadsBtn.setAlpha(1);
                    browser.openDownloadsBtn.setVisibility(VISIBLE);
                    break;
                default:
                    browser.clearHistoryBtn.setAlpha(0);
                    browser.clearHistoryBtn.setVisibility(GONE);
                    //browser.viewClosedTabsSpeedDialBtn.setAlpha(0);
                    //browser.viewClosedTabsSpeedDialBtn.setVisibility(GONE);
                    browser.openDownloadsBtn.setAlpha(0);
                    browser.openDownloadsBtn.setVisibility(GONE);
                    break;
            }
        }
    }


    /**
     * Called when RecyclerView needs a new {@link SpeedDialListViewHolder} of the given type to represent
     * an item.
     */
    @NonNull
    @Override
    public SpeedDialListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.speed_dial_list_layout, parent, false);
        AbstractItemAdapter itemAdapter;
        // Init adapter for vertical list
        switch(viewType){
            case SpeedDialListViewHolder.WHATS_HOT_LIST:
                itemAdapter = new WhatsHotItemAdapter(browser, listLayout, viewType);
                break;
            case SpeedDialListViewHolder.SAVED_FOR_LATER_LIST:
                itemAdapter = new ReadLaterListItemAdapter(browser, listLayout, viewType);
                break;
            case SpeedDialListViewHolder.HISTORY_LIST:
                itemAdapter = new HistoryItemAdapter(browser, listLayout, viewType);
                break;
            case SpeedDialListViewHolder.FAVES_LIST:
                itemAdapter = new FaveItemAdapter(browser, listLayout, viewType);
                break;
            default:
                throw new IllegalStateException("Unrecognised viewtype for speed dial list adapter: "+viewType);
        }
        lists.get(viewType).itemAdapter = itemAdapter;
        return new SpeedDialListViewHolder(listLayout, browser);
    }

    @Override
    public void onBindViewHolder(@NonNull SpeedDialListViewHolder holder, final int position) {
        // Normally the data would be bound to the list item here
        // but because we are asynchronously loading in the items we have the adapter
        // trigger it instead
    }

    @Override
    public int getItemViewType(int position) {
        final SpeedDialListState speedDialHorizontalListState = lists.get(position); // this ensures new lists don't get recycled from old closed ones
        return speedDialHorizontalListState.id; // TODO this should probably return 0 as all lists are same type even if they load different things, then the list id will need to be passed to SpeedDialItemAdapter
    }


    public void reloadCurrentList(int showAllOfItemType, ArrayList<Integer> filterByTagIds, String domainFilter){
        if(showAllOfItemType==ItemType.TAG_FILTER && filterByTagIds!=null && filterByTagIds.size()==0){
            // if unselected all tags, go back to fave home
            showAllOfItemType = 0;
        }
        reloadList(getCurrentPosition(), showAllOfItemType, filterByTagIds, domainFilter);
        browser.updateSpeedDialBackButtonVisibility();
    }

    public int getCurrentPosition() {
        return linearLayoutManager.findFirstCompletelyVisibleItemPosition();
    }

    private void reloadList(int position, int showAllOfItemType, ArrayList<Integer> filterByTagIds, String domainFilter){
        String searchFilter;
        browser.isViewingExpandedSpeedDialListOfType = showAllOfItemType;
        browser.filterFavesByTagIds = filterByTagIds;
        browser.speedDialDomainFilter = domainFilter;
        if(browser.isSearchingSpeedDialDatabase(false) && browser.addressInput.getText()!=null) {
            searchFilter = browser.addressInput.getText().toString().trim();
            if(UrlHelper.isUrlAttempt(searchFilter)){
                searchFilter = UrlHelper.getTrustedAddress(searchFilter);
            }
        }else{
            searchFilter = "";
        }
        // Get our Loader by calling getLoader and passing the ID we specified
        if(!has(position)){
            return;
        }
        AbstractItemAdapter itemAdapter = lists.get(position).itemAdapter;
        if(itemAdapter!=null) {
            itemAdapter.getItemLayoutManager().scrollToPosition(0); // list gets glitchy if we don't reset scroll before reloading
            itemAdapter.reload(searchFilter, showAllOfItemType, filterByTagIds, domainFilter);
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return lists.size();
    }

    /**
     * Return the stable ID for the item at <code>position</code>. If {@link #hasStableIds()}
     * @param position Adapter position to query
     * @return the stable ID of the item at position
     */
    @Override
    public long getItemId(int position) {
        return lists.get(position).id;
    }

    public boolean has(int position) {
        try {
            lists.get(position);
            return true;
        }catch(IndexOutOfBoundsException e){
            return false;
        }
    }
}
