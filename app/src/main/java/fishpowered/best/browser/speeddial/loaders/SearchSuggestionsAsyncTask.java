package fishpowered.best.browser.speeddial.loaders;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.db.BrowsingHistoryRepository;
import fishpowered.best.browser.db.Item;
import fishpowered.best.browser.db.ItemType;
import fishpowered.best.browser.db.SearchHistoryRepository;
import fishpowered.best.browser.speeddial.itemadapters.AbstractItemAdapter;
import fishpowered.best.browser.speeddial.list.SpeedDialListViewHolder;
import fishpowered.best.browser.utilities.ItemList;
import fishpowered.best.browser.utilities.UrlHelper;

public class SearchSuggestionsAsyncTask extends AsyncTask<String, Void, ItemList> {
    private final WeakReference<Browser> browserRef;
    private final WeakReference<AbstractItemAdapter> itemAdapterRef;

    public SearchSuggestionsAsyncTask(AbstractItemAdapter itemAdapter, Browser browser){
        browserRef = new WeakReference<Browser>(browser);
        itemAdapterRef = new WeakReference<AbstractItemAdapter>(itemAdapter);
    }

    @Override
    protected ItemList doInBackground(String... params) {
        final String searchFilter = params[0];
        ItemList listItems = new ItemList();
        Browser browser = browserRef.get();
        AbstractItemAdapter itemAdapter = itemAdapterRef.get();
        if(browser==null || itemAdapter==null){
            return listItems;
        }

        if(searchFilter!=null && !searchFilter.equals("")) {

            // Search suggestions heading
            Item heading = new Item();
            heading.typeId = ItemType.HEADING_LABEL;
            heading.title = browser.getString(R.string.search_suggestions);
            listItems.add(heading);

            // Fetch top 5 history matches
            if(itemAdapter.getListType()!=SpeedDialListViewHolder.HISTORY_LIST && !searchFilter.contains(" ")) { // if space is typed they are probably doing a search and we shouldn't waste time fetching history
                ItemList historyItems = BrowsingHistoryRepository.getHistoryByDomainForSearchSuggestions(browser.db, browser, searchFilter, null);
                listItems.addAll(historyItems);
            }

            // Fetch up to 5 suggestions from search history
            if(listItems.size() <= 4) {
                SearchHistoryRepository.addSearchHistoryToSearchSuggestions(browser.db, browser, searchFilter, listItems);
            }

            // If no good matches, just show what was typed
            if(listItems.size()<=1){ // If only the heading
                Item searchAsTypedHint = new Item();
                searchAsTypedHint.typeId = ItemType.SEARCH_SUGGESTION_LINK;
                if(UrlHelper.isUrlAttempt(searchFilter)) {
                    searchAsTypedHint.title = searchFilter;
                }else{
                    searchAsTypedHint.title = "\"" + searchFilter + "\"";
                }
                searchAsTypedHint.address = searchFilter;
                listItems.add(searchAsTypedHint);
            }
        }
        return listItems;
    }

    @Override
    protected void onPostExecute(ItemList items) {
        AbstractItemAdapter itemAdapter = itemAdapterRef.get();
        if(itemAdapter==null){
            return;
        }
        itemAdapter.listItemsFromSuggestions = items;
        itemAdapter.notifyDataSetChanged();
        super.onPostExecute(items);
    }
}
