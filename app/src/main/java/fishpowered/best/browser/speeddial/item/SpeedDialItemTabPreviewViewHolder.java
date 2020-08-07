package fishpowered.best.browser.speeddial.item;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.TabViewHolder;
import fishpowered.best.browser.db.Item;

/**
 * Represents clickable item in the speed dial list e.g. fave, history item etc
 */
public class SpeedDialItemTabPreviewViewHolder extends AbstractSpeedDialItemViewHolder {

    private Item currentItem;

    public SpeedDialItemTabPreviewViewHolder(Browser browser, ViewGroup parent) {
        super(browser, browser.getLayoutInflater().inflate(R.layout.chip_view, parent, false));
        itemView.setOnClickListener(onClickListener);
        itemView.setOnLongClickListener(onLongClickListener);
    }
    @Override
    public void bindDataToView(Item item, int position, int listType) {
        currentItem = item;
        TextView chipView = (TextView) itemView.findViewById(R.id.chip_view);
        chipView.setText(item.title);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(currentItem==null){
                return;
            }
            browser.addressInput.clearFocus();
            browser.hideSpeedDial(false);
            int clickedTabPosition = browser.tabAdapter.getPositionForTabId(currentItem.id);
            //Log.d("FOOMAN", "Opening tab id "+currentItem.id+" at position "+clickedTabPosition);
            if (clickedTabPosition >= 0) {
                final TabViewHolder currentTab = browser.getCurrentTabViewHolder();
                if(currentTab!=null && (currentTab.getCurrentPageAddress(null)==null || currentTab.getCurrentPageAddress(null).equals(""))){
                    // If the user was about to open a new tab before deciding to visit an existing one, we should clean up
                    browser.tabRecyclerView.deletePositionOnSettle = browser.getSelectedTabPosition(false);
                }

                // Scroll to tab the user wants to view
                //browser.tabRecyclerView.smoothScrollToPosition(clickedTabPosition);
                browser.tabRecyclerView.scrollToPosition(clickedTabPosition); // smooth scroll here is a bit buggy so jump straight to pos
            }
            //browser.tabAdapter.removeUnusedTabs();
        }
    };

    View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            int currentTabPosition = browser.tabAdapter.getPositionForTabId(currentItem.id);
            browser.tabAdapter.removeTabAtPositionFromAdapter(currentTabPosition);
            browser.addressInput.setText("");
            //browser.speedDialListAdapter.reloadCurrentList(browser.isViewingExpandedSpeedDialListOfType);
            return true; // consume
        }
    };

}
