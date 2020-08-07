package fishpowered.best.browser.speeddial.itemadapters;

import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;

import java.util.ArrayList;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.speeddial.loaders.FaveItemAsyncLoader;
import fishpowered.best.browser.speeddial.loaders.SearchSuggestionsAsyncTask;

/**
 * Adapter for the items that make up a speed dial list e.g. fetches the fave items, history items etc
 */
public class FaveItemAdapter extends AbstractItemAdapter {


    public FaveItemAdapter(Browser context, View listLayout, int listType) {
        super(context, listLayout, listType);
    }

    /**
     * Reload the list by querying the DB and search suggestions
     */
    @Override
    public void reload(@NonNull String searchFilter, int showAllOfItemType, ArrayList<Integer> filterByTagIds, String domaginFilter) {
    }
}
