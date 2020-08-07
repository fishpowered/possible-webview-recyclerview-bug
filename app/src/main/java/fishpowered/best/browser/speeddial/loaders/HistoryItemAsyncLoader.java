package fishpowered.best.browser.speeddial.loaders;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import fishpowered.best.browser.speeddial.itemadapters.AbstractItemAdapter;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.db.BrowsingHistoryRepository;
import fishpowered.best.browser.utilities.ItemList;

public class HistoryItemAsyncLoader extends AsyncTask<String, Void, ItemList> {
    private final WeakReference<Browser> browserRef;
    private final WeakReference<AbstractItemAdapter> itemAdapterRef;

    public HistoryItemAsyncLoader(Browser context, AbstractItemAdapter itemAdapter) {
        browserRef = new WeakReference<Browser>(context);
        itemAdapterRef = new WeakReference<AbstractItemAdapter>(itemAdapter);
    }

    @Override
    protected ItemList doInBackground(String... strings) {
        Browser browser = browserRef.get();
        if(browser==null){
            return new ItemList();
        }
        String searchFilter = strings[0];
        int dateFilterTimeStamp = 0;
        if(strings.length > 1) {
            dateFilterTimeStamp = Integer.parseInt(strings[1]);
        }
        String domainFilter = strings[2];

        /**
         * Use cases:
         * 1) Base history view (domain=null, date=null) = show grouped by date and domain
         * 2) Click on "..." from top level view (domain=null, date=12345678) = show grouped by domain for current date
         * 3) Click on date heading (domain="", date=12345678) = show breakdown for date
         * 4) Click on domain group (domain="eg.com", date=12345678) = show breakdown for domain
         */

        if((searchFilter!=null && !searchFilter.equals("")) || (domainFilter!=null && !domainFilter.equals("")) || (dateFilterTimeStamp > 0 && domainFilter!=null)){
            return BrowsingHistoryRepository.getHistoryByPageTitle(browser.db, browser, searchFilter, dateFilterTimeStamp, domainFilter);
        }
        return BrowsingHistoryRepository.getTopLevelHistoryGroupedByDateAndDomain(browser.db, browser, dateFilterTimeStamp, domainFilter);
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
