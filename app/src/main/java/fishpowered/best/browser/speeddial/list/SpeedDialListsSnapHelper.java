package fishpowered.best.browser.speeddial.list;

import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.viewpager.widget.ViewPager;

/**
 * Implementation of the {@link SnapHelper} supporting pager style snapping in either vertical or
 * horizontal orientation.
 *
 * <p>
 *
 * PagerSnapHelper can help achieve a similar behavior to {@link ViewPager}.
 * Set both {@link RecyclerView} and the items of the
 * {@link RecyclerView.Adapter} to have
 * {@link android.view.ViewGroup.LayoutParams#MATCH_PARENT} height and width and then attach
 * PagerSnapHelper to the {@link RecyclerView} using {@link #attachToRecyclerView(RecyclerView)}.
 */
public class SpeedDialListsSnapHelper extends PagerSnapHelper {
    private static final int MAX_SCROLL_ON_FLING_DURATION = 500; // ms

}

