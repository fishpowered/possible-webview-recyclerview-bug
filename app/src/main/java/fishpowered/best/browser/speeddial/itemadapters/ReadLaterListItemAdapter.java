package fishpowered.best.browser.speeddial.itemadapters;

import androidx.annotation.NonNull;
import fishpowered.best.browser.Browser;
import fishpowered.best.browser.speeddial.loaders.ReadLaterItemListAsyncLoader;
import fishpowered.best.browser.speeddial.loaders.SearchSuggestionsAsyncTask;

import android.view.View;

import java.util.ArrayList;

/**
 * Adapter for the items that make up a speed dial list e.g. fetches read/watch later items AND already read/watched
 */
public class ReadLaterListItemAdapter extends AbstractItemAdapter {

    public ReadLaterListItemAdapter(Browser context, View listLayout, int listType) {
        super(context, listLayout, listType);
    }

    /**
     * Reload the list by querying the DB and search suggestions
     */
    @Override
    public void reload(@NonNull String searchFilter, int showAllOfItemType, ArrayList<Integer> filterByTagIds, String domainFilter) {
    }
}
