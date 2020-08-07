package fishpowered.best.browser.speeddial.item;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.view.Gravity;
import android.view.View;

import java.util.ArrayList;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.TabViewHolder;
import fishpowered.best.browser.db.DatabaseHelper;
import fishpowered.best.browser.db.Item;
import fishpowered.best.browser.db.ItemRepository;
import fishpowered.best.browser.db.ItemType;
import fishpowered.best.browser.db.SearchHistoryRepository;
import fishpowered.best.browser.utilities.FileHelper;
import fishpowered.best.browser.utilities.UrlHelper;

abstract public class AbstractSpeedDialItemViewHolder extends RecyclerView.ViewHolder {

    protected final Browser browser;

    AbstractSpeedDialItemViewHolder(Browser browser, View itemView) {
        super(itemView);
        this.browser = browser;
    }

    abstract public void bindDataToView(Item item, int position, int listType);

    public static void onSpeedDialItemClicked(final Item item, final Browser browser, View chipView) {
    }

    /**
     *
     * @param item
     * @param browser
     * @param chipView
     * @return boolean, true if the long press event should be consumed
     */
    public boolean onSpeedDialItemLongPressed(final Item item, final Browser browser, View chipView) {
        return true; // consume click
    }
}
