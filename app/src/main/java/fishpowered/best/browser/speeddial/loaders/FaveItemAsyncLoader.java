package fishpowered.best.browser.speeddial.loaders;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import fishpowered.best.browser.db.TagRepository;
import fishpowered.best.browser.speeddial.itemadapters.AbstractItemAdapter;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.db.ItemRepository;
import fishpowered.best.browser.db.ItemType;
import fishpowered.best.browser.utilities.ItemList;

/**
 * Asynchronously load fave items and return to
 * FaveItemAdapter
 */
public class FaveItemAsyncLoader extends AsyncTask<String, Void, ItemList> { // @TODO **ALL** AsyncTasks run on the same thread in a queue
    private final WeakReference<Browser> browserRef;
    private final WeakReference<AbstractItemAdapter> itemAdapterRef;

    public FaveItemAsyncLoader(Browser context, AbstractItemAdapter itemAdapter) {
        browserRef = new WeakReference<Browser>(context); // Weak ref
        itemAdapterRef = new WeakReference<AbstractItemAdapter>(itemAdapter);
    }

    @Override
    protected ItemList doInBackground(String... strings) {
        ItemList itemList = new ItemList(); // populated by reference
        return itemList;
    }

    @Override
    protected void onPostExecute(final ItemList items) {
        AbstractItemAdapter itemAdapter = itemAdapterRef.get();
        if(itemAdapter==null){
            return;
        }
        itemAdapter.listItemsFromDb = items;
        itemAdapter.notifyDataSetChanged();
    }
}
