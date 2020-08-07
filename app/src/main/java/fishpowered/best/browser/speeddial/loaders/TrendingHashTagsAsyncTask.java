package fishpowered.best.browser.speeddial.loaders;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.db.ItemType;
import fishpowered.best.browser.speeddial.itemadapters.AbstractItemAdapter;
import fishpowered.best.browser.utilities.ItemList;

public class TrendingHashTagsAsyncTask extends AsyncTask<String, Void, ItemList> {

    private final WeakReference<Browser> browserRef;
    private final WeakReference<AbstractItemAdapter> itemAdapterRef;

    public TrendingHashTagsAsyncTask(AbstractItemAdapter itemAdapter, Browser browser){
        browserRef = new WeakReference<Browser>(browser);
        itemAdapterRef = new WeakReference<AbstractItemAdapter>(itemAdapter);
    }

    @Override
    protected ItemList doInBackground(String... params) {
        final String searchFilter = params[0];
        ItemList listItems = new ItemList();
        //Log.d("WEBSEARCH", "AsyncTask loaded "+searchFilter);

        if(searchFilter!=null && !searchFilter.equals("")) {
            return listItems;
        }
        Browser browser = browserRef.get();
        if(browser==null){
            return listItems;
        }
        //Log.d("WEBSEARCH", "AsyncTask proceeding "+searchFilter);

        // Fetch rest of suggestions from internet
        final boolean showFullList = browser.isViewingExpandedSpeedDialListOfType == ItemType.TRENDING_HASH_TAG_LINK;
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
