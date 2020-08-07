package fishpowered.best.browser.speeddial.item;

import android.view.ViewGroup;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.db.Item;

/**
 * Represents clickable item in the speed dial list e.g. fave, history item etc
 */
public class SpeedDialItemLoadingViewHolder extends AbstractSpeedDialItemViewHolder{
    public SpeedDialItemLoadingViewHolder(Browser browser, ViewGroup parent) {
        super(browser, browser.getLayoutInflater().inflate(R.layout.speed_dial_loading_item, parent, false));
    }

    @Override
    public void bindDataToView(Item item, int position, int listType) {
        /*currentItem = item;
        TextView chipView = (TextView) itemView.findViewById(R.id.loading_item_text_view);
        chipView.setText(item.title);*/
    }
}
