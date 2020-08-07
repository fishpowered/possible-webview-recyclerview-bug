package fishpowered.best.browser.speeddial.list;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import fishpowered.best.browser.Browser;

/**
 * Represents the view for the list in the speed dial e.g. fave list, open tab list, history list etc
 */
public class SpeedDialListViewHolder extends RecyclerView.ViewHolder {

    public static final int FAVES_LIST = 0;
    public static final int WHATS_HOT_LIST = 1;
    public static final int SAVED_FOR_LATER_LIST = 2;
    public static final int HISTORY_LIST = 3;

    SpeedDialListViewHolder(View itemView, Browser browser) {
        super(itemView);
    }
}
