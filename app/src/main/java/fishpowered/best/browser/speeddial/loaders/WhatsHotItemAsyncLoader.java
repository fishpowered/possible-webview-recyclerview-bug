package fishpowered.best.browser.speeddial.loaders;

import android.os.AsyncTask;
import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

import fishpowered.best.browser.speeddial.itemadapters.AbstractItemAdapter;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.db.BrowsingHistoryRepository;
import fishpowered.best.browser.db.ItemType;
import fishpowered.best.browser.db.SearchHistoryRepository;
import fishpowered.best.browser.utilities.ItemList;

/**
 * Asynchronously load whats hot items and return to
 * WhatsHotItemAdapter
 */
public class WhatsHotItemAsyncLoader extends AsyncTask<String, Void, ItemList> {
    private final WeakReference<Browser> browserRef;
    private final WeakReference<AbstractItemAdapter> itemAdapterRef;

    public WhatsHotItemAsyncLoader(Browser context, AbstractItemAdapter itemAdapter) {
        browserRef = new WeakReference<Browser>(context);
        itemAdapterRef = new WeakReference<AbstractItemAdapter>(itemAdapter);
    }

    @Override
    protected ItemList doInBackground(String... strings) {
        Browser browser = browserRef.get();
        ItemList itemList = new ItemList(); // populated by reference
        return itemList;
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