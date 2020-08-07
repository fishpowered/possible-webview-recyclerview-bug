package fishpowered.best.browser;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import fishpowered.best.browser.utilities.DateTimeHelper;

import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import fishpowered.best.browser.utilities.UnitHelper;

/**
 * Responsible for the swipeability of the tabs, both horizontally to change tab and vertically to remove the current tab
 */
public class TabRecyclerView extends RecyclerView {

    /**
     * changes the number of items loaded ahead, def 2
     */
    public final int tabCacheSize = 0;
    private Browser browser;
    public int deletePositionOnSettle = -1;
    private boolean isVerticalSwipe;
    private boolean isHorizontalSwipe;
    public long touchDownTime;
    TabLinearLayoutManager layoutManager;
    private View closeTabThresholdLine;
    public int currentlyScrollingToPosition = -1;
    public TabSnapHelper snapHelper;
    private TextView closeTabTextView;
    private String gestureSwipeUpNavBarPref ="";
    private float gestureSwipeFromEdgeThresholdPref;
    private String gestureSwipeFromLeftEdgePref="";
    private String gestureSwipeFromRightEdgePref="";
    private boolean hasHorizontalSwipeAlreadyTriggeredAction = false;
    private float gestureSwipeToChangeTabsBottomThresholdPref;
    public boolean hasScrolledInsidePageSinceTouchDown = false;
    private boolean multiTouchGesture = false;
    private static int minDistanceForHintingCloseTabSwipePx;
    private TabViewHolder currentTabViewHolderBasedOnTouchDown;
    private Boolean prevSwipeWasToRight;

    public TabRecyclerView(Context context) {
        super(context);
    }

    public TabRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TabRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false; // remove override if this causes problems with alpha transparency
    }

    public void initRecyclerView(final TabLinearLayoutManager tabLayoutManager, TabAdapter tabAdapter, final Browser browser){
        minDistanceForHintingCloseTabSwipePx = UnitHelper.convertDpToPx(20, browser);

        this.browser = browser;
        gestureSwipeUpNavBarPref = browser.preferences.getGestureSwipeUpNavBar();
        gestureSwipeFromEdgeThresholdPref = browser.preferences.getGestureSwipeFromEdgeThreshold(false);
        gestureSwipeFromLeftEdgePref = browser.preferences.getGestureSwipeFromLeftEdge();
        gestureSwipeFromRightEdgePref = browser.preferences.getGestureSwipeFromRightEdge();
        gestureSwipeToChangeTabsBottomThresholdPref = browser.preferences.getGestureSwipeToChangeTabsBottomThreshold();
        this.addOnScrollListener(this.horizontalScrollListener);
        setHasFixedSize(true);
        closeTabThresholdLine = browser.findViewById(R.id.close_tab_threshold_line);
        closeTabTextView = browser.findViewById(R.id.close_tab_textview);
        initTabSwipeUpGestureHints();
        setLayoutManager(tabLayoutManager);
        setAdapter(tabAdapter);
        setItemViewCacheSize(tabCacheSize);
        setItemAnimator(new DefaultItemAnimator());
        /*final int spacing = UnitHelper.convertDpToPx(20f, browser);
        addItemDecoration(new ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull State state) {
                super.onDraw(c, parent, state);
            }

            @Override
            public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull State state) {
                super.onDrawOver(c, parent, state);
            }

            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull State state) {
                outRect.left = spacing;
                outRect.right = spacing;
                outRect.top =0;
                outRect.bottom =0;
                //super.getItemOffsets(outRect, view, parent, state);
            }
        });*/
        layoutManager = tabLayoutManager;
        // add pager behavior
        //PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper = new TabSnapHelper();
        snapHelper.attachToRecyclerView(this);
        tabLayoutManager.setScrollEnabled(false); // start disabled, tab swiping should only be enabled if a side swipe from edges is detected
        addOnItemTouchListener(onTouchListener);
        //setOnFlingListener(new RecyclerView.OnFlingListener()); experimented with this, you can require a faster finger but the fling distance can still be tiny and can't differentiate between horizontal and vertical
    }

    public void initTabSwipeUpGestureHints() {
        String closeTabHintText = "";
        switch(gestureSwipeUpNavBarPref){
            case "closeTab":
                closeTabHintText = this.browser.getString(R.string.close_tab);
                break;
            case "refresh":
                closeTabHintText = this.browser.getString(R.string.title_refresh);
                break;
            case "minimiseTab":
                closeTabHintText = this.browser.getString(R.string.minimise_tab);
                break;
        }
        closeTabTextView.setText(closeTabHintText.toLowerCase());
    }


    @Override
    public void smoothScrollToPosition(int position) {
        if(position<0){
            position = 0;
        }
        if(browser.tabAdapter.getItemCount()<=1){
            return; // if the scroll request doesn't result in movement,
            // we shouldn't enable tab horizontal scrolling because it won't get cleared on settle
        }else {
            layoutManager.setScrollEnabled(true); // hopefully this helps nav button taps being misinterpreted as swipes
        }
        currentlyScrollingToPosition = position;
        super.smoothScrollToPosition(position);
    }

    protected OnScrollListener horizontalScrollListener = new OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            //Log.d("FPWEBVIEWTOUCHORDER", "TabRecyclerView.horizontalScrollListener");
            boolean showTabBulletHints = browser.tabAdapter.getItemCount() == browser.tabAdapter.getItemCountOfUsedTabs();
            /*if(currentlyScrollingToPosition > -1){ working animation based on position
                try {
                    // If we're scrolling to an empty tab, it's probably because the user just hit
                    // the + button and are expecting to be shown the speed dial so don't show the tab count
                    String targetAddress = browser.tabAdapter.getAddressAtPosition(currentlyScrollingToPosition);
                    float currentAlpha = browser.speedDialContainer.getAlpha();
                    float alphaOffset;
                    final int scrollCurrentOffset = computeHorizontalScrollOffset();
                    if (targetAddress == null || targetAddress.equals("")) {
                        // fade in as we are transitioning to a an empty tab
                        alphaOffset = ((float) (scrollCurrentOffset - scrollStartingOffset) / (float) tabWidth);
                    }else{
                        // fade out as we are transitioning to a non-empty tab
                        alphaOffset = 1f- ((float) (scrollStartingOffset - scrollCurrentOffset) / (float) tabWidth);
                    }
                    //Log.d("XPB", "dx = "+dx+" alpha:"+(alphaOffset));
                    alphaOffset = (alphaOffset<0.5f) ? 0f : (alphaOffset-0.5f) * 2f; // this pushes the fade to one end so it mostly only overlaps the empty tab
                    browser.speedDialContainer.setAlpha(alphaOffset);
                }catch(IndexOutOfBoundsException e){
                }
            }*/

        }
    };

    @NonNull
    private String sideSwipeStartedFromEdge = "";
    private boolean swipeStartedOverNavBar = false;
    private boolean swipeStartedInBottomSwipeThreshold = false;

    //private boolean sideSwipeContinuedOverToolBar = false;
    /**
     * Handles swipes/gestures/taps etc on the recycler view
     */
    private OnItemTouchListener onTouchListener = new OnItemTouchListener() {

        public static final float SWIPE_THRESHOLD = 30f;
        float startYpx;
        float startXpx;
        public boolean isTap = false;

        /**
         * Intercept/catch touch events on the recycler view so we can prevent the tabs swiping/being
         * closed only if swiping starts at the bottom. This also handles finishing off show/hide navbar animation
         * if the user lifts their finger mid scrolling
         *
         * ORDERING: THIS IS CALLED FIRST WHEN TOUCHING WHILST LOOKING AT THE TABS, THEN TabViewHolder.onWebViewTouch(down)
         *
         * @param rv
         * @param ev
         * @return true Should never return true unless you want to block the webview ever being told about the touch event. Blocks rv swipes as well
         */
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent ev) {
            //Log.d("FPWEBVIEWTOUCHORDER", "TabRecyclerView.onInterceptTouchEvent");
            //Log.d("STUTT", "isEnabled:"+browser.tabLayoutManager.getScrollEnabled());
            int currentTabPosition = browser.getSelectedTabPosition(false);

            float minDistanceForCloseTabSwipePx = UnitHelper.convertDpToPx(120, browser); //browser.convertYScreenPercentageToPixels(40f);
            final float toolbarHeightPx = UnitHelper.getNavBarHeightInPx(browser.isInCompactMode(), browser);
            final int action = ev.getAction();
            int yPx = Math.round(ev.getY());
            int xPx = Math.round(ev.getX());
            float viewHeightPx = getHeight();
            View currentTabItemView;
            final Long currentTimeStampInMs = DateTimeHelper.getCurrentTimeStampInMilliseconds();
            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    // First finger down, detect start of swipe/click
                    multiTouchGesture = false;
                    final int selectedTabPosition = browser.getSelectedTabPosition(true);
                    currentTabViewHolderBasedOnTouchDown = browser.getBrowserTabViewHolderAtPosition(selectedTabPosition);
                    //currentTabViewHolderBasedOnTouchDown = browser.getCurrentTabViewHolder();
                    if(!currentTabViewHolderBasedOnTouchDown.isScrollXClamped()){
                        // Prevents a single gesture doing webview window scrolling AND switch tab. User should always have to lift. "Do you even lift bro?!"
                        hasScrolledInsidePageSinceTouchDown = true;
                    }else{
                        hasScrolledInsidePageSinceTouchDown = false;
                    }
                    startYpx = yPx;
                    startXpx = xPx;
                    touchDownTime = currentTimeStampInMs;
                    isVerticalSwipe = false;
                    isHorizontalSwipe = false;
                    prevSwipeWasToRight = null;
                    hasHorizontalSwipeAlreadyTriggeredAction = false;
                    swipeStartedOverNavBar = !currentTabViewHolderBasedOnTouchDown.hasScreenBeenPulledDown()

                            && yPx > (viewHeightPx - toolbarHeightPx) && yPx < (viewHeightPx - UnitHelper.convertDpToPx(10, browser))
                            && !browser.isKeyboardOpen();
                    final boolean isNavBarHidden = currentTabViewHolderBasedOnTouchDown.navBarScrollFraction < 0.99f;
                    if(swipeStartedOverNavBar && isNavBarHidden){
                        swipeStartedOverNavBar = false;
                        return false;
                    }
                    swipeStartedInBottomSwipeThreshold = false;
                    if((!gestureSwipeFromLeftEdgePref.equals("disabled") && xPx < browser.convertXScreenPercentageToPixels(gestureSwipeFromEdgeThresholdPref))){
                        sideSwipeStartedFromEdge = "left";
                    }else if(!gestureSwipeFromRightEdgePref.equals("disabled") && (xPx > browser.convertXScreenPercentageToPixels(100f - (gestureSwipeFromEdgeThresholdPref)))){
                        sideSwipeStartedFromEdge = "right";
                    }else{
                        //Log.d("GESTUREFUNX3", " clamped "+(currentTabViewHolderBasedOnTouchDown.getOs?1:0)+" "
                        //        +" timeSinceLastChildScroll "+Math.abs(currentTabViewHolderBasedOnTouchDown.webViewJavascriptHooks.timeChildElementLastScrolled - DateTimeHelper.getCurrentTimeStampInMilliseconds()));
                        sideSwipeStartedFromEdge = "";
                        if(!currentTabViewHolderBasedOnTouchDown.isTabSwipingLocked() && gestureSwipeToChangeTabsBottomThresholdPref > 0f
                                && yPx > (browser.getScreenHeight() - browser.convertYScreenPercentageToPixels(gestureSwipeToChangeTabsBottomThresholdPref))
                        ){
                            swipeStartedInBottomSwipeThreshold = true;
                        }
                    }

                    isTap = true; // note this is a tap anywhere on the tab, not just on the webview
                    //Log.d("TABBIN", "Enabling button presses from touch down");
                    browser.canBrowserMenuButtonsBePressed = true;
                    currentTabViewHolderBasedOnTouchDown.isFingerTouchingWebView = true;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    multiTouchGesture = true;
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    multiTouchGesture = false;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if(currentTabViewHolderBasedOnTouchDown==null){
                        return false; // can happen if swiping while already swiping
                    }
                    currentTabItemView = currentTabViewHolderBasedOnTouchDown.itemView;
                    final float xSwipeDistanceDp = UnitHelper.convertPxToDp((int) Math.abs(startXpx - xPx), browser);
                    final float ySwipeDistanceDp = UnitHelper.convertPxToDp((int) Math.abs(startYpx - yPx), browser);
                    if (isTap && (xSwipeDistanceDp > SWIPE_THRESHOLD || ySwipeDistanceDp > SWIPE_THRESHOLD)) {
                        isTap = false;
                    }
                    final boolean isSideSwipe = xSwipeDistanceDp > SWIPE_THRESHOLD && xSwipeDistanceDp > ySwipeDistanceDp;
                    boolean sideSwipeContinuedFromEdge = (!sideSwipeStartedFromEdge.equals("") && isSideSwipe);
                    //Log.e("SCROLLY",(sideSwipeStartedFromEdge ?1:0)+" Current X:"+xPx+" xSwipeDistanceDp "+xSwipeDistanceDp+" yDistance "+ySwipeDistanceDp+" = "+(sideSwipeContinuedFromEdge ?1:0));

                    // finger move/drag - if swipe started from the browser bar we can animate the tab disappearing
                    if(!isTap && swipeStartedOverNavBar
                            && !gestureSwipeUpNavBarPref.equals("disabled")
                            && (startYpx - yPx) > minDistanceForHintingCloseTabSwipePx
                            && layoutManager.scrollState == RecyclerView.SCROLL_STATE_IDLE){ // scroll state check prevents tabs being closed during scrolling/swiping to left/right
                        currentTabItemView.setY(yPx - startYpx);
                        float alpha = ((minDistanceForCloseTabSwipePx - (startYpx - yPx)) / minDistanceForCloseTabSwipePx);
                        alpha = Math.max(alpha, 0.2f);
                        if(gestureSwipeUpNavBarPref.equals("closeTab")) {
                            currentTabItemView.setAlpha(alpha); // 0 = transparent, 1 = opaque
                        }
                        closeTabThresholdLine.setAlpha(1f-alpha);
                        if(browser.tabAdapter.getItemCount()>1) {
                            closeTabTextView.setAlpha(1f - alpha);
                        }
                        //browser.speedDialContainer.setAlpha(1f-alpha);
                        layoutManager.setScrollEnabled(false); // prevent horizontal scrolling while dragging a tab up
                        //Log.d("TABBIN", "Disabling button presses for close drag");
                        browser.canBrowserMenuButtonsBePressed = false;
                        return false;
                    }
                    // Once we have established a primary swipe direction, we should not allow the user to change their mind mid swipe
                    // without first releasing their finger, the tabs can get stuck between pages otherwise
                    if(!isTap && !isHorizontalSwipe && ySwipeDistanceDp > xSwipeDistanceDp && ySwipeDistanceDp > UnitHelper.convertDpToPx(10, browser)){
                        // user is primarily swiping up, we should disable the side swiping
                        isVerticalSwipe = true;
                    }
                    final boolean xRatio = xSwipeDistanceDp > (ySwipeDistanceDp * 2);
                    final boolean xDistance = xSwipeDistanceDp > 50;

                    if(!isTap && !isVerticalSwipe && xRatio && xDistance && !swipeStartedOverNavBar){
                        // user is primarily swiping left/Right
                        isHorizontalSwipe = true;
                        final long timeSwiping = currentTimeStampInMs - touchDownTime;
                        if(timeSwiping > 600){
                            int offsetFromEdgeSnap = browser.tabRecyclerView.computeHorizontalScrollOffset() % browser.getScreenWidth();
                            if(offsetFromEdgeSnap!=0) {
                                // They have basically dragged a little and it has started to change tab but
                                // they still have their finger down for a long time so let's assume they
                                // were trying to scroll something inside the page
                                browser.showTabSwipeLockBtn("Long drag: " + timeSwiping);
                            }
                        }
                    }
                    final boolean isScrollingRight = startXpx > xPx;
                    boolean moreRelaxedPageScrollTest = false;
                    if(prevSwipeWasToRight!=null && (currentTimeStampInMs -browser.tabLayoutManager.lastSettledTime) < 400
                            && ((isScrollingRight && prevSwipeWasToRight) || (!isScrollingRight && !prevSwipeWasToRight) )){
                        // It can take a tiny bit of time to load the webview and if you want to fast
                        // scroll through a bunch of tabs quickly it's frustrating to have to wait for
                        // the stricter inner page scrolling check so providing they already have momentum
                        // in that direction, allow them an easier swipe.
                        //Log.d("BREWDOG", "RELAXED SCROLL CHECK isScrollingRight="+(isScrollingRight ? 1:0)+" prevScrollingRight="+(prevSwipeWasToRight ?1:0)+" = "+(currentTimeStampInMilliseconds-browser.tabLayoutManager.lastSettledTime));
                        moreRelaxedPageScrollTest = true;
                    }

                    if(!layoutManager.isScrollEnabled() && isHorizontalSwipe && (
                            swipeStartedOverNavBar
                            || (
                                swipeStartedInBottomSwipeThreshold && !hasScrolledInsidePageSinceTouchDown && sideSwipeStartedFromEdge.equals("")
                                && !multiTouchGesture && (
                                    moreRelaxedPageScrollTest
                                    || (currentTabViewHolderBasedOnTouchDown.isScrollXClamped() && !currentTabViewHolderBasedOnTouchDown.lastInnerScrollWasRecent(touchDownTime))
                                )
                            )
                        )
                    ){
                        if(isSwipingToTabThatExists(startXpx, xPx)) {
                            // We don't want to enable  tab scrolling unless there is actually a tab to scroll to
                            // otherwise it can break inner page faked scrolling such as twitter image lists
                            layoutManager.setScrollEnabled(true);
                        }else{
                            //browser.tabBulletHintList.layout.setVisibility(VISIBLE);
                        }
                    }else if(isHorizontalSwipe && (sideSwipeContinuedFromEdge)){
                        browser.fortyPercentHintText.setVisibility(View.GONE);  // enable RV to scroll sideways
                        if(sideSwipeStartedFromEdge.equals("left")){
                            triggerSwipeFromEdgeCustomAction(currentTabViewHolderBasedOnTouchDown, gestureSwipeFromLeftEdgePref);
                        }else if(sideSwipeStartedFromEdge.equals("right")){
                            triggerSwipeFromEdgeCustomAction(currentTabViewHolderBasedOnTouchDown, gestureSwipeFromRightEdgePref);
                        }
                        // It is disabled again on settle @see TabLinearLayoutManager.onScrollStateChanged
                    }else if(isHorizontalSwipe && currentTabViewHolderBasedOnTouchDown.isTabSwipingLocked() && !swipeStartedOverNavBar){
                        // Tab swiping is locked but they are side swiping so we should probably show the unlock button
                        browser.showTabSwipeLockBtn("Already locked and detected side swipe");
                    }
                    if(prevSwipeWasToRight==null && isHorizontalSwipe){
                        prevSwipeWasToRight = isScrollingRight;
                    }
                    break;
                case MotionEvent.ACTION_UP: // finger up
                case MotionEvent.ACTION_CANCEL: // finger up
                    if(currentTabViewHolderBasedOnTouchDown==null){
                        return false; // can happen if swiping while already swiping
                    }
                    //Log.d("FPWEBVIEWTOUCHORDER", "TabRecyclerView.onInterceptTouchEvent (FINGER UP)");
                    // IMPORTANT: currentTabViewHolder MAY NOT BE EXPECTED TAB IF HORIZONTAL SWIPING SO USE currentTabViewHolderBasedOnTouchDown

                    if(isTap && (currentTimeStampInMs - touchDownTime) > 500){
                        isTap = false;
                    }
                    currentTabItemView = currentTabViewHolderBasedOnTouchDown.itemView;
                    currentTabViewHolderBasedOnTouchDown.isFingerTouchingWebView = false;
                    currentTabViewHolderBasedOnTouchDown.distanceScrolledDpWithFingerPressed = 0;
                    currentTabViewHolderBasedOnTouchDown.timeLastTouchedInMs = currentTimeStampInMs;

                    isVerticalSwipe = false;
                    isHorizontalSwipe = false;
                    closeTabThresholdLine.setAlpha(0);
                    closeTabTextView.setAlpha(0);
                    if(isTap){
                        //Log.d("TAPPY", "TAP");

                        //Log.d("TABBIN", "Enabling button presses from isTap=true");
                        browser.canBrowserMenuButtonsBePressed = true;
                        // Tap as opposed to drag
                        if(!currentTabViewHolderBasedOnTouchDown.hasScreenBeenPulledDown()) {
                            currentTabItemView.setY(0); // just incase they dragged it a little
                        }
                        currentTabItemView.setAlpha(1); // 0 = transparent, 1 = opaque
                        return false;
                    } else if(swipeStartedOverNavBar) {
                        // End of drag
                        if (startYpx > yPx && (startYpx - yPx) > minDistanceForCloseTabSwipePx) {
                            switch(gestureSwipeUpNavBarPref){
                                case "closeTab":
                                    //Log.d("TABBIN", "REMOVING TAB "+currentTabPosition);
                                    currentTabItemView.setAlpha(0);
                                    currentTabViewHolderBasedOnTouchDown.hide();
                                    currentTabItemView.setY(0);
                                    if (browser.tabAdapter.getItemCountOfUsedTabs() > 1) {
                                        //browser.tabBulletHintList.showTabBeingRemovedFromPosition(currentTabPosition);
                                    }
                                    browser.removeTabAtPosition(currentTabPosition, false, false, false);
                                    break;
                                default:
                                    //Log.d("TABBIN", "TRIGGERING "+ gestureSwipeUpNavBarPref);
                                    currentTabViewHolderBasedOnTouchDown.triggerCustomAction(gestureSwipeUpNavBarPref);
                                    currentTabItemView.setY(0);
                            }
                            return false;
                        } else {
                            startYpx = 0;
                            if (gestureSwipeUpNavBarPref.equals("closeTab") && currentTabItemView.getAlpha() < 0.98f){
                                // Aborted drag
                                browser.showLongNotificationHint(browser.getString(R.string.drag_a_little_higher_to_close), null);
                            }
                            if(!currentTabViewHolderBasedOnTouchDown.hasScreenBeenPulledDown()) {
                                currentTabItemView.setY(0);
                            }
                            currentTabItemView.setAlpha(1); // 0 = transparent, 1 = opaque
                           // layoutManager.setScrollEnabled(true);
                            return false;
                        }
                    }else{
                        Log.d("TAPPY", "NO TAP");
                    }
            }
            //layoutManager.setScrollEnabled(isTap==false && !isVerticalScroll && (sideSwipeContinuedOverToolBar || sideSwipeContinuedFromEdge));
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            //Log.e("draggynewonTouchEvent", e.toString());
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            //Log.e("draggynewdisallowint", "foo:"+(disallowIntercept ? 1:0));
        }
    };

    private boolean isSwipingToTabThatExists(float startXpx, int currentX) {
        int offset = browser.getSelectedTabPosition(false);
//        Log.d("BLOCKEDSWIPE", "startXpx:"+startXpx+" currentX:"+currentX+" offset:"+offset+" cnt:"+((browser.tabAdapter.getItemCount()-1))+" < "+browser.tabRecyclerView.computeHorizontalScrollRange());
        if(startXpx < currentX && offset > 0){
//            Log.d("BLOCKEDSWIPE", "TAB EXISTS TO LEFT");
            return true;
        }
        if(startXpx > currentX && offset < (browser.tabAdapter.getItemCount()-1)){
//            Log.d("BLOCKEDSWIPE", "TAB EXISTS TO RIGHT");
            return true;
        }
        return false;
    }

    public void triggerSwipeFromEdgeCustomAction(TabViewHolder currentTabViewHolder, String swipeFromEdgeGesturePreference) {
        switch (swipeFromEdgeGesturePreference) {
            case "navigateBack":
                if (!hasHorizontalSwipeAlreadyTriggeredAction) {
                    currentTabViewHolder.triggerCustomAction("navigateBack");
                }
                break;
            case "navigateForward":
                if (!hasHorizontalSwipeAlreadyTriggeredAction) {
                    currentTabViewHolder.triggerCustomAction("navigateForward");
                }
                break;
            case "switchTabs":
            default:
                layoutManager.setScrollEnabled(true); // enable RV to scroll sideways
                break;
        }
        hasHorizontalSwipeAlreadyTriggeredAction = true; // so the same swipe doesn't trigger navigateBack a million times
    }

    @Override
    public void scrollToPosition(int position) {
        super.scrollToPosition(position);
    }
}
