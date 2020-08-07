package fishpowered.best.browser.speeddial.item;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.db.Item;
import fishpowered.best.browser.speeddial.list.SpeedDialListsAdapter;

/**
 * The inline dedicated button for showing more of a list e.g. "...", not to be confused with chipviews
 * that have ... as an overlay
 */
public class SpeedDialItemInlineShowMoreViewHolder extends AbstractSpeedDialItemViewHolder {

    private int itemType = 0;
    TextView label;

    public SpeedDialItemInlineShowMoreViewHolder(Browser browser, ViewGroup parent) {
        super(browser, browser.getLayoutInflater().inflate(R.layout.speed_dial_list_show_more, parent, false));
        itemView.setOnClickListener(this.onClickListener);
        label = itemView.findViewById(R.id.speed_dial_list_hint_tv);
    }

    @Override
    public void bindDataToView(Item item, int position, int listType) {
        label.setTextColor(SpeedDialListsAdapter.getListColour(listType, browser));
        this.itemType = Integer.parseInt(item.address+"");
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            browser.addressInput.clearFocus();
            browser.speedDialListAdapter.reloadCurrentList(itemType, new ArrayList<Integer>(), null);
        }
    };
}
