package fishpowered.best.browser.speeddial.list;

import java.util.ArrayList;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import fishpowered.best.browser.Browser;

public class SpeedDialListsLinearLayoutManager extends LinearLayoutManager {
    private final Browser browser;
    private boolean isScrollEnabled = true;
    private static final float SCROLL_SPEED_MILLISECONDS_PER_INCH = 50f;

    /**
     * see RecyclerView.SCROLL_STATE
     */
    public int scrollState = 0;

    /**
     * @param context       Current context, will be used to access resources.
     * @param orientation   Layout orientation. Should be {@link #HORIZONTAL} or {@link
     *                      #VERTICAL}.
     * @param reverseLayout When set to true, layouts from end to start.
     */
    public SpeedDialListsLinearLayoutManager(Browser context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        this.browser = context;
    }

    public void setScrollEnabled(boolean flag) {
        this.isScrollEnabled = flag;
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        return false; // apparently this gives good performance https://stackoverflow.com/questions/44347883/loading-large-number-of-items-in-recycler-view
    }

    /**
     * RecyclerView calls this method to notify LayoutManager that scroll state has changed.
     *
     * @param state The new scroll state for RecyclerView
     */
    @Override
    public void onScrollStateChanged(int state) {
        scrollState = state;
        if(scrollState == RecyclerView.SCROLL_STATE_IDLE){
            int listPos = findFirstVisibleItemPosition();
            if(listPos >= 0){
                browser.speedDialListAdapter.reloadCurrentList(0, new ArrayList<Integer>(), null);
                int listType = browser.speedDialListAdapter.getItemViewType(listPos);
                /*switch(listType){
                    case SpeedDialListViewHolder.FAVES_LIST:
                    case SpeedDialListViewHolder.WHATS_HOT_LIST:*/
                        browser.preferences.edit().putInt(Browser.preference_lastUsedSpeedDialListPosition, listPos).apply();
                /*        break;
                    default:
                        // Don't think it makes sense to save that you were looking at the history or tabs list
                }*/
            }
        }
        super.onScrollStateChanged(state);
    }

    @Override
    public boolean canScrollVertically() {
        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
        return false; //isScrollEnabled && super.canScrollVertically();
    }

    @Override
    public boolean canScrollHorizontally() {
        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
        return isScrollEnabled && super.canScrollHorizontally();
    }

    /**
     * Override the smooth scroll to produce a slower scroll
     * @param recyclerView
     * @param state
     * @param position
    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        final LinearSmoothScroller linearSmoothScroller =
                new LinearSmoothScroller(recyclerView.getContext()) {

                    @Override
                    public PointF computeScrollVectorForPosition(int targetPosition) {
                        return SpeedDialListsLinearLayoutManager.this
                                .computeScrollVectorForPosition(targetPosition);
                    }

                    @Override
                    protected float calculateSpeedPerPixel
                            (DisplayMetrics displayMetrics) {
                        return SCROLL_SPEED_MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                    }
                };
        linearSmoothScroller.setTargetPosition(position);
        try {
            startSmoothScroll(linearSmoothScroller);
        }catch(Exception e){
            // if this fails for whatever reason, we don't want to crash the browser
        }
    }*/
}
