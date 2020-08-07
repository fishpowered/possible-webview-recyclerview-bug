package fishpowered.best.browser.speeddial.loaders;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.db.ItemRepository;
import fishpowered.best.browser.db.ItemType;
import fishpowered.best.browser.speeddial.itemadapters.AbstractItemAdapter;
import fishpowered.best.browser.utilities.ItemList;

import fishpowered.best.browser.R;

/**
 * Asynchronously load read later items
 */
public class ReadLaterItemListAsyncLoader extends AsyncTask<String, Void, ItemList> {
    private final WeakReference<Browser> browserRef;
    private final WeakReference<AbstractItemAdapter> itemAdapterRef;

    public ReadLaterItemListAsyncLoader(Browser context, AbstractItemAdapter itemAdapter) {
        browserRef = new WeakReference<Browser>(context);
        itemAdapterRef = new WeakReference<AbstractItemAdapter>(itemAdapter);
    }

    @Override
    protected ItemList doInBackground(String... strings) {
        String searchFilter = strings[0];
        int itemTypeFilter = 0;
        if(strings.length > 1) {
            itemTypeFilter = Integer.parseInt(strings[1]);
        }
        Browser browser = browserRef.get();
        ItemList returnList = new ItemList(); // populated by reference
        if(browser==null){
            return returnList;
        }

        Integer readLaterListLimit = null; // itemTypeFilter==ItemType.READ_LATER_ITEMS ? null : 5
        if(itemTypeFilter==0 || itemTypeFilter== ItemType.READ_LATER_ITEMS) {
            // Read/watch later items
            ItemList readLaterItems = ItemRepository.getReadLaterItems(searchFilter, readLaterListLimit, browser, false);
            if(searchFilter.equals("") || readLaterItems.size() > 0) { // we want to always display this unless they are searching and there is no results
                returnList.add(ItemRepository.createListItemHeading(browser.getString(R.string.saved_for_later_heading)));
                if (readLaterItems.size() > 0) {
                    returnList.addAll(readLaterItems);
                } else {
                    returnList.add(ItemRepository.createListHint(browser.getString(R.string.read_later_intro_text)));
                }
            }
        }
        /*Integer previouslyReadListLimit = itemTypeFilter == ItemType.PREVIOUSLY_READ_ITEMS ? null : 3;
        if(itemTypeFilter==0 || itemTypeFilter== ItemType.PREVIOUSLY_READ_ITEMS) {
            // Read/watch later items
            ItemList readLaterItems = ItemRepository.getReadLaterItems(Item.ITEM_IS_READ_LATER_AND_HAS_BEEN_READ,
                    searchFilter, previouslyReadListLimit, browser);
            if (searchFilter.equals("") || readLaterItems.size() > 0 || itemTypeFilter== ItemType.PREVIOUSLY_READ_ITEMS) {
                ItemRepository.createListItemHeading(browser.getString(R.string.previously_read_watched));
                returnList.addAll(readLaterItems);
            }
        }*/
        return returnList; // see onPostExecute for what happens with the list
    }

    @Override
    protected void onPostExecute(ItemList items) {
        AbstractItemAdapter itemAdapter = itemAdapterRef.get();
        if(itemAdapter==null){
            return;
        }
        itemAdapter.listItemsFromDb = items;
        itemAdapter.notifyDataSetChanged();
    }
}
