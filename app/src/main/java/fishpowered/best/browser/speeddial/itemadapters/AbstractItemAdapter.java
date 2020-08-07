package fishpowered.best.browser.speeddial.itemadapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.ArrayList;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.db.Item;
import fishpowered.best.browser.db.ItemType;
import fishpowered.best.browser.speeddial.item.AbstractSpeedDialItemViewHolder;
import fishpowered.best.browser.speeddial.item.SpeedDialItemChipImageHolder;
import fishpowered.best.browser.speeddial.item.SpeedDialItemChipViewHolder;
import fishpowered.best.browser.speeddial.item.SpeedDialItemHeadingViewHolder;
import fishpowered.best.browser.speeddial.item.SpeedDialItemHistoryGroupViewHolder;
import fishpowered.best.browser.speeddial.item.SpeedDialItemHistoryViewHolder;
import fishpowered.best.browser.speeddial.item.SpeedDialItemListHintViewHolder;
import fishpowered.best.browser.speeddial.item.SpeedDialItemLoadingViewHolder;
import fishpowered.best.browser.speeddial.item.SpeedDialItemNewTabButtonViewHolder;
import fishpowered.best.browser.speeddial.item.SpeedDialItemRecyclerView;
import fishpowered.best.browser.speeddial.item.SpeedDialItemReopenClosedTabViewHolder;
import fishpowered.best.browser.speeddial.item.SpeedDialItemInlineShowMoreViewHolder;
import fishpowered.best.browser.speeddial.item.SpeedDialItemTabPreviewViewHolder;
import fishpowered.best.browser.speeddial.item.SpeedDialItemTagFilterButtonViewHolder;
import fishpowered.best.browser.utilities.ItemList;

/**
 * Adapter for the items that make up a speed dial list e.g. fetches the fave items, history items etc
 */
abstract public class AbstractItemAdapter extends RecyclerView.Adapter<AbstractSpeedDialItemViewHolder>
{

    private final int listType;
    public Browser browser;
    protected final LayoutInflater layoutInflater;
    public ItemList listItemsFromDb = new ItemList();
    public ItemList listItemsFromSuggestions = new ItemList();
    private FlexboxLayoutManager itemLayoutManager;

    public AbstractItemAdapter(Browser context, View listLayout, int listType) {
        browser = context;
        this.listType = listType;
        layoutInflater = LayoutInflater.from(context);

        // Init list with the "loading" item to beginwith
        Item item = new Item();
        item.id = 0; // must be stable id for the recycle shiz to work
        //item.address = n+") some address";
        item.typeId = ItemType.LOADING;
        this.listItemsFromDb.add(item);
        this.initItemAdapter(listLayout);
        reload("", 0, null, null); // todo check this should actually be here
    }

    /**
     * Reload the list by querying the DB and search suggestions
     */
    abstract public void reload(String searchFilter, int showAllOfItemType, ArrayList<Integer> filterByTagIds, String domainFilter);

   /* @Override
    public void onLoadFinished(android.content.Loader<ItemList> loader, ItemList data) {
        listItemsFromDb = data;
        notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(android.content.Loader<ItemList> loader) {
        listItemsFromDb = new ItemList();
    }*/

    /**
     * Called when RecyclerView needs a new {@link SpeedDialItemChipViewHolder} of the given type to represent
     * an item.
     */
    @NonNull
    @Override
    public AbstractSpeedDialItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch(viewType){
            case ItemType.HEADING_LABEL:
                return new SpeedDialItemHeadingViewHolder(browser, parent);
            case ItemType.LIST_HINT:
                return new SpeedDialItemListHintViewHolder(browser, parent);
            case ItemType.SHOW_MORE_INLINE_BTN:
                return new SpeedDialItemInlineShowMoreViewHolder(browser, parent);
            case ItemType.TAG_FILTER:
                return new SpeedDialItemTagFilterButtonViewHolder(browser, parent);
            case ItemType.LOADING:
                return new SpeedDialItemLoadingViewHolder(browser, parent);
           // case ItemType.FAVE_ADD_BUTTON:
                //return new SpeedDialItemAddFaveButtonViewHolder(browser, parent);
            case ItemType.OPEN_NEW_TAB_BUTTON:
                return new SpeedDialItemNewTabButtonViewHolder(browser, parent);
            case ItemType.GO_TO_OPEN_TAB_LINK:
                return new SpeedDialItemTabPreviewViewHolder(browser, parent);
            case ItemType.REOPEN_CLOSED_TAB_LINK:
                return new SpeedDialItemReopenClosedTabViewHolder(browser, parent);
            case ItemType.IMAGE:
            case ItemType.VIDEO:
                return new SpeedDialItemChipImageHolder(browser, parent);
            case ItemType.BROWSING_HISTORY_LINK:
                return new SpeedDialItemHistoryViewHolder(browser, parent);
            case ItemType.BROWSING_HISTORY_LINK_GROUP:
                return new SpeedDialItemHistoryGroupViewHolder(browser, parent);
            case ItemType.WEB_SITE:
            case ItemType.FAVE_QUOTE:
            case ItemType.WHATS_HOT_LINK:
            default:
                return new SpeedDialItemChipViewHolder(browser, parent);
        }
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link SpeedDialItemChipViewHolder#itemView} to reflect the item at the given
     * position.
     */
    @Override
    public void onBindViewHolder(@NonNull AbstractSpeedDialItemViewHolder holder, final int position) {
        //final FrameLayout layout = (FrameLayout) holder.itemView;
        // We want to combine two separately loaded lists from the DB and search results
        final Item item = getItemFromCombinedList(position);
        holder.bindDataToView(item, position, listType);
    }

    /**
     * Items from DB and search suggestions from Google have to be loaded independently because they take vastly different
     * times to load. But the recyclerview wants to query a single list so this method can be used to fetch items as if they
     * come from a single list
     */
    private Item getItemFromCombinedList(@NonNull int position){
        try {
            return listItemsFromDb.get(position);
        }catch(IndexOutOfBoundsException e){
            return listItemsFromSuggestions.get(position - listItemsFromDb.size());
        }
    }

    public void initItemAdapter(View listLayout) {
        SpeedDialItemRecyclerView speedDialVerticalListRecyclerView = listLayout.findViewById(R.id.speed_dial_vertical_list_recycler_view);
        speedDialVerticalListRecyclerView.setNestedScrollingEnabled(false);
        speedDialVerticalListRecyclerView.setRecycledViewPool(browser.speedDialListAdapter.itemViewPool); // apparently good for performance https://proandroiddev.com/optimizing-nested-recyclerview-a9b7830a4ba7
        itemLayoutManager = new FlexboxLayoutManager(browser);
        itemLayoutManager.setFlexWrap(FlexWrap.WRAP);
        itemLayoutManager.setAlignItems(AlignItems.FLEX_START);
        speedDialVerticalListRecyclerView.initRecyclerView(itemLayoutManager, this, browser);
        speedDialVerticalListRecyclerView.setNestedScrollingEnabled(true); // needed? already nested..
    }

    public FlexboxLayoutManager getItemLayoutManager() {
        return itemLayoutManager;
    }

    /**
     * @return int
     */
    public int getListType(){
        return listType;
    }

    // e.g. is it a header, a favourite,
    @Override
    public int getItemViewType(int position) {
        final Item item = getItemFromCombinedList(position); // this ensures new listItemsFromDb don't get recycled from old closed ones
        return item.typeId;
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return listItemsFromDb.size() + listItemsFromSuggestions.size();
    }

    /**
     * Return the stable ID for the item at <code>position</code>. If {@link #hasStableIds()}
     * @param position Adapter position to query
     * @return the stable ID of the item at position
     */
    @Override
    public long getItemId(int position) {
        return getItemFromCombinedList(position).id;
    }

    /*public boolean has(int position) {
        try {
            listItemsFromDb.get(position);
            return true;
        }catch(IndexOutOfBoundsException e){
            return false;
        }
    }*/
}
