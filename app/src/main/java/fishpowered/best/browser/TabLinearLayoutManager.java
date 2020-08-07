package fishpowered.best.browser;

import android.graphics.PointF;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import fishpowered.best.browser.db.BrowsingHistoryRepository;
import fishpowered.best.browser.db.DatabaseHelper;
import fishpowered.best.browser.utilities.DateTimeHelper;

import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;

import static android.view.View.VISIBLE;

public class TabLinearLayoutManager extends LinearLayoutManager {
    private final Browser browser;
    public boolean ignoreNextSettle = false;

    /**
     * Based on current context e.g. scrolling web view or not
     */
    private boolean isScrollEnabled = true;
    private static final float SCROLL_SPEED_MILLISECONDS_PER_INCH = 42f; // tried to balance performance vs speed of opening. Note: if you make it slower it can be easy to hit the invisible tab whilst still animating

    /**
     * see RecyclerView.SCROLL_STATE
     */
    public int scrollState = 0;
    private int displayedCount=0;

    private int lastSettledPosition = -1;
    private int settledPositionBeforeLast = -1;
    public Long lastSettledTime=0L;

    /**
     * @param context       Current context, will be used to access resources.
     * @param orientation   Layout orientation. Should be {@link #HORIZONTAL} or {@link
     *                      #VERTICAL}.
     * @param reverseLayout When set to true, layouts from end to start.
     */
    public TabLinearLayoutManager(Browser context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        this.browser = context;
    }

    /**
     * Set whether the tabs can be scrolled or not, we toggle this on and off depending on what the user is doing
     * @param flag
     */
    public void setScrollEnabled(boolean flag) {
        /*DEBUG scrollEnabled..TabViewHolder tab=browser.getCurrentTabViewHolder();
        if(tab!=null){
            tab.navButton1.setVisibility(flag ? View.VISIBLE : View.INVISIBLE);
        }*/
        this.isScrollEnabled = flag;
    }

    public boolean getScrollEnabled(){
        return this.isScrollEnabled;
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

        // Update any visible tab counts if we need to
        //if(displayedCount!=browser.tabAdapter.getItemCountOfUsedTabs()) {
//            for (int childCount = browser.tabRecyclerView.getChildCount(), i = 0; i < childCount; ++i) {
//                Log.d("TABBYCOUNT", "Updating position "+i+" with "+browser.tabAdapter.getItemCountOfUsedTabs());
//                final TabViewHolder holder = (TabViewHolder) browser.tabRecyclerView.getChildViewHolder(browser.tabRecyclerView.getChildAt(i));
//                holder.updateTabButtonIconCount();
//            }
//            displayedCount = browser.tabAdapter.getItemCountOfUsedTabs();
       // }

        if(scrollState == RecyclerView.SCROLL_STATE_IDLE){
            if(ignoreNextSettle){
                ignoreNextSettle = false;
                return;
            }
            int offsetFromEdgeSnap = browser.tabRecyclerView.computeHorizontalScrollOffset() % browser.getScreenWidth();
            browser.tabRecyclerView.currentlyScrollingToPosition = -1;

            // If a blank tab has been opened we have scrolled to an invisible tab to show the speed dial
            // once scrolling has finished we can hide the recyclerView so you can actually use the speed dial
            int currentPosition = browser.tabLayoutManager.findFirstCompletelyVisibleItemPosition();
            if(offsetFromEdgeSnap!=0){
                // if u release mid drag it can register briefly as idle
                browser.showTabSwipeLockBtn("aborted drag");
                return;
            }


            //Log.d("TABBIN", "settled on "+currentPosition);

            String currentAddress;
            TabState tabState;
            boolean isOpeningOrHasAlreadyOpenedAddress = false;
            try {
                tabState = browser.tabAdapter.getTabStates().get(currentPosition);
                isOpeningOrHasAlreadyOpenedAddress = (tabState.address!=null || tabState.transport!=null);
                currentAddress = tabState.address;
                if(!tabState.tabOpenedBy.equals(Browser.TAB_OPENED_BY_EXTERNAL_APP)){
                    // If you open a tab from whatsapp, pressing back or immediately closing that tab
                    // should return to whatsapp BUT if you switch tabs first then it's likely that
                    // you forgot they were opened externally and pressing back can just take u back to speed dial
                    browser.tabAdapter.clearStateThatAnyTabsWereOpenedExternally();
                }
            }catch(IndexOutOfBoundsException e){
                currentAddress = null;
                currentPosition = 0;
            }
            if (isOpeningOrHasAlreadyOpenedAddress) {
                //Log.d("TABBIN", "hiding speed dial after settle");
                browser.hideSpeedDial(false);
            } else {
                //Log.d("TABBIN", "showing speed dial after settle");
                // tab rv has settled on a blank/empty/placeholder tab, show the speed dial
                browser.showSpeedDial(false, 0, false, true, true, "settled on empty tab", false);
            }
            TabViewHolder currentTab = browser.getCurrentTabViewHolder();

            // When removing a tab and if it's not the last tab, we should move to the closest tab before actually doing the delete
            if(browser.tabRecyclerView.deletePositionOnSettle >= 0){
                int positionToRemove = browser.tabRecyclerView.deletePositionOnSettle;
                //Log.d("TABBIN", "settled now remove "+positionToRemove);
                browser.tabRecyclerView.deletePositionOnSettle = -1; // must be before line below in case there is delay
                browser.tabAdapter.removeTabAtPositionFromAdapter(positionToRemove);
            }else if(currentTab!=null){
                //currentTab.webView.resumeVideosAndStuff();
                currentTab.setScrollXClamped(true);
            }
            final int prevPosition = browser.preferences.getInt(Browser.preference_savedTabPosition, 0);
            final boolean hasSwitchedTabs = prevPosition!=currentPosition;
            // Store the state of which tab is being viewed in case we need to restore later
            browser.preferences.edit().putInt(Browser.preference_savedTabPosition, currentPosition).apply();
            browser.updateCompactMode();
            if(currentTab!=null && currentTab.webView!=null) {
                currentTab.removeEasyReachOffset(true);
                currentTab.dontScrollNavBar = false;
                BrowsingHistoryRepository.startRecordingHistoryItem(browser, currentTab, DatabaseHelper.getInstance(browser).getWritableDatabase(),
                        null, null,"tabsettle");

                //Log.d("NAVBARANIM", "TAB CURRENTPOSITION: "+currentPosition);
                currentTab.setCompactMode(browser.isInCompactMode(), false); // this is required if compact mode is enabled on a tab, then swipe to another tab and disable it, we can't change it on unloaded tabs
                if(hasSwitchedTabs) {
                    //Log.d("NAVBARANIM", "POPUP DUE TO SWITCHING TAB");
                    currentTab.animateNavBarToVisible(true);
                }

                if(currentAddress!=null && !currentAddress.equals("")){
                    // We know if we have settled on a valid tab it's safe to remove unused tabs.
                    // Use case: if you have a couple of tabs open, press back to speed dial on one
                    // (so the other is now an unused tab) and then open the other tab from the tab list
                    browser.tabAdapter.removeUnusedTabs();
                }

                browser.updateTabSwipeLockBtn();
                final Long currentTimeStampInMilliseconds = DateTimeHelper.getCurrentTimeStampInMilliseconds();
                if(currentTab.isTabSwipingLocked()){
                    // Scroll lock should be remembered and reminded if they change tab via the thumb list and change back
                    browser.showTabSwipeLockBtn("Previously locked");
                }else{
                    if(currentPosition==settledPositionBeforeLast && lastSettledPosition!=currentPosition
                            && (currentTimeStampInMilliseconds-currentTab.timeLastTouchedInMs) < 1500){
                        // If the user swipes to one tab and quickly back to previous tab, maybe they didn't intend to switch
                        browser.showTabSwipeLockBtn("Swipe to next tab and quickly swipe back");
                    }
                }
                // NOTE THESE CAN BE CLEARED BY HITTING BACK BUTTON
                settledPositionBeforeLast = lastSettledPosition;
                lastSettledPosition = currentPosition;
                lastSettledTime = currentTimeStampInMilliseconds;
            }
            //Log.d("TABBIN", "Enabling button presses from idle scrollstate");
            browser.canBrowserMenuButtonsBePressed = true;

            //Log.d("TABBIN", "Disabling swiping");
            // only if we know we have fully settled on a tab should we disable scrolling again (otherwise it can get stuck)
            setScrollEnabled(false);
            return;
            //browser.tabAdapter.saveAllTabWebViewStatesToStore(); might increase likeliness to crash switching tabs
        }
        super.onScrollStateChanged(state);
    }

    /**
     * Valid reasons for changing tab should not trigger the swipe lock button to appear
     */
    public void clearSwipeTrackingForTabSwipeLock() {
        settledPositionBeforeLast = -1;
        lastSettledPosition = -1;
        lastSettledTime = 0L;
    }

    public static void updateTabButtonTabCount(TextView buttonTextOverlay, String text) {
        if(buttonTextOverlay.getVisibility()==VISIBLE){
            buttonTextOverlay.setText(text);
        }
    }

    @Override
    public boolean canScrollVertically() {
        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
        return isScrollEnabled && super.canScrollVertically();
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
     */
    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        final LinearSmoothScroller linearSmoothScroller =
                new LinearSmoothScroller(recyclerView.getContext()) {

                    @Override
                    public PointF computeScrollVectorForPosition(int targetPosition) {
                        return TabLinearLayoutManager.this
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
            Browser.silentlyLogException(e, browser);
        }
    }

    /**
     * Recyclerview has been overscrolled horizontally
     *
     * @param dx
     * @param recycler
     * @param state
     */
    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scrollRange = super.scrollHorizontallyBy(dx, recycler, state);
        int overScroll = dx - scrollRange;
        if (overScroll < 0 || overScroll > 0) {
            // Horizontal over-scroll has occurred. Show the tab count hint so the user knows where they are
            browser.tabLayoutManager.setScrollEnabled(false); // required so if the user then tries to vertically scroll in the webview they can
        }
        return scrollRange;
    }

    public boolean isScrollEnabled() {
        return isScrollEnabled;
    }
}
