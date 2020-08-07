package fishpowered.best.browser.speeddial.item;

import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.db.Item;

/**
 * Represents clickable item in the speed dial list e.g. fave, history item etc
 * @deprecated Currently not in use
 */
public class SpeedDialItemReopenClosedTabViewHolder extends AbstractSpeedDialItemViewHolder {

    private Item currentItem;

    public SpeedDialItemReopenClosedTabViewHolder(Browser browser, ViewGroup parent) {
        super(browser, browser.getLayoutInflater().inflate(R.layout.chip_view, parent, false));
        itemView.setOnClickListener(onClickListener);
        //itemView.setOnLongClickListener(onLongClickListener);
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
            browser.addressInput.clearFocus();
            if(currentItem==null){
                return;
            }
            // With a bit of luck, the tab ID will cause the webview to be recycled, otherwise it falls back to the address
            final int insertPosition = browser.tabAdapter.getItemCount();
            browser.addNewTabAtPosition(insertPosition, currentItem.address, true, currentItem.id, Browser.TAB_OPENED_BY_SPEED_DIAL_OR_SHOULD_RETURN_THERE, true, true);
            browser.hideSpeedDial(false);
            browser.recentlyClosedTabAdapter.removeItemById(currentItem.id, true);
            browser.tabRecyclerView.scrollToPosition(insertPosition);
        }
    };

    View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            return true;
        }
    };

}
