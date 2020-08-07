package fishpowered.best.browser.speeddial.item;

import android.view.View;
import android.view.ViewGroup;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.db.Item;

/**
 * Represents clickable item in the speed dial list e.g. fave, history item etc
 * @deprecated Currently not in use
 */
public class SpeedDialItemNewTabButtonViewHolder extends AbstractSpeedDialItemViewHolder{
    public SpeedDialItemNewTabButtonViewHolder(Browser browser, ViewGroup parent) {
        super(browser, browser.getLayoutInflater().inflate(R.layout.new_tab_chip_view, parent, false));
        itemView.setOnClickListener(onNewTabButtonClick);
        //itemView.setOnLongClickListener(onAddButtonLongClick);
    }

    @Override
    public void bindDataToView(Item item, int position, int listType) {
        //TextView chipView = itemView.findViewById(R.id.new_tab_chip_btn);
        //chipView.setText(item.title);
        //chipView.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_add_circle_green_16dp, 0, 0, 0);
    }

    private View.OnClickListener onNewTabButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String defaultIfHomePageNotSet = ""; // i.e. empty search
            browser.openAddressOrSearchInNewTab(browser.preferences.getCustomHomePageAddress(defaultIfHomePageNotSet), true, Browser.TAB_OPENED_BY_SPEED_DIAL_OR_SHOULD_RETURN_THERE, true);
        }
    };
}
