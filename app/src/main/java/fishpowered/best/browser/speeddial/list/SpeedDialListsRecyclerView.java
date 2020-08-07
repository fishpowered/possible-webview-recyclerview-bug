package fishpowered.best.browser.speeddial.list;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.ColorStateList;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import fishpowered.best.browser.utilities.ThemeHelper;

import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;

import fishpowered.best.browser.utilities.UnitHelper;

/**
 * Responsible for the horizontal swipeability of the speed dial lists
 */
public class SpeedDialListsRecyclerView extends RecyclerView {

    /**
     * changes the number of items loaded ahead, def 2
     */
    public final int speedDialListCacheSize = 2;
    private Browser browser;
    public int deletePositionOnSettle = -1;
    private boolean isVerticalScroll;
    private boolean isHorizontalScroll;
    private long touchDownTime;
    private final int MAX_TAP_DURATION = 100;
    private GestureDetector gestureDetector;

    public SpeedDialListsRecyclerView(Context context) {
        super(context);
    }

    public SpeedDialListsRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SpeedDialListsRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initRecyclerView(Browser browser){
        this.browser = browser;
        setNestedScrollingEnabled(false);
        //addOnItemTouchListener(onTouchListener);
        setItemViewCacheSize(1);
        setHasFixedSize(true);
        gestureDetector = new GestureDetector(browser, new VerticalScrollDetector());
        addOnScrollListener(horizontalScrollListener);
    }

    protected OnScrollListener horizontalScrollListener = new OnScrollListener(){

        /**
         * Callback method to be invoked when the RecyclerView has been scrolled. This will be
         * called after the scroll has completed.
         * <p>
         * This callback will also be called if visible item range changes after a layout
         * calculation. In that case, dx and dy will be 0.
         *
         * @param recyclerView The RecyclerView which scrolled.
         * @param dx The amount of horizontal scroll.
         * @param dy The amount of vertical scroll.
         */
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy){
            int offset = computeHorizontalScrollOffset();
            int pageWidth = computeHorizontalScrollExtent(); // 1280
            int totalWidth = computeHorizontalScrollRange(); // 5120
            final boolean isRtl = ThemeHelper.isRTL(browser);
            // each page must get the distance to the offset
            // this number can be capped by page width and turned to a percentage to be used in the gradient
            final float favePageOffset = UnitHelper.calculatePercentagePageIsSelected(isRtl ? 3 : 0, pageWidth, offset);
            final float whatsHotPageOffset = UnitHelper.calculatePercentagePageIsSelected(isRtl ? 2 : 1, pageWidth, offset);
            final float readLaterPageOffset = UnitHelper.calculatePercentagePageIsSelected(isRtl ? 1 : 2, pageWidth, offset);
            final float historyPageOffset = UnitHelper.calculatePercentagePageIsSelected(isRtl ? 0 : 3, pageWidth, offset);

            //ColorFilter cf = new PorterDuffColorFilter(Color.argb(255 * favePageOffset, 255f, 0, 0), PorterDuff.Mode.OVERLAY);

            final ArgbEvaluator argbEvaluator = new ArgbEvaluator();
            final int speedDialHeadingInactiveColour = ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.speedDialListHeadingInactive, browser);
            final int faveColour = ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.speedDialListFaveTheme, browser);
            final int readLaterTheme = ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.speedDialListSavedForLaterTheme, browser);
            final Integer faveBtnColour = 0xff000000 + (Integer) argbEvaluator.evaluate(favePageOffset, speedDialHeadingInactiveColour, faveColour);
            final Integer whatsHotBtnColour = 0xff000000 + (Integer) argbEvaluator.evaluate(whatsHotPageOffset, speedDialHeadingInactiveColour, ContextCompat.getColor(browser, R.color.speedDialListWhatsHotTheme));
            final Object readLaterColour = 0xff000000 + (Integer) argbEvaluator.evaluate(readLaterPageOffset, speedDialHeadingInactiveColour, readLaterTheme);
            final Object historyColour = 0xff000000 + (Integer) argbEvaluator.evaluate(historyPageOffset, speedDialHeadingInactiveColour, ContextCompat.getColor(browser, R.color.speedDialListHistoryTheme));
            // Three ways of doing the same thing in case there's problems with older versions with any of them...
            ImageViewCompat.setImageTintList(browser.faveListBtn, ColorStateList.valueOf(faveBtnColour));
            ImageViewCompat.setImageTintList(browser.whatsHotListBtn, ColorStateList.valueOf(whatsHotBtnColour));
            browser.readLaterListBtn.setColorFilter((Integer) readLaterColour, android.graphics.PorterDuff.Mode.SRC_IN);
            browser.historyListBtn.setColorFilter((Integer) historyColour, android.graphics.PorterDuff.Mode.SRC_IN);

            // Fade in/out clear history and close tabs button
            browser.clearHistoryBtn.setAlpha(historyPageOffset);
            browser.clearHistoryBtn.setVisibility(historyPageOffset < 0.05f ? GONE : VISIBLE); // otherwise it will be invisible but clickable
            //browser.viewClosedTabsSpeedDialBtn.setAlpha(historyPageOffset);
            //browser.viewClosedTabsSpeedDialBtn.setVisibility(historyPageOffset < 0.05f ? GONE : VISIBLE); // otherwise it will be invisible but clickable
            browser.openDownloadsBtn.setAlpha(readLaterPageOffset);
            browser.openDownloadsBtn.setVisibility(readLaterPageOffset < 0.05f ? GONE : VISIBLE); // otherwise it will be invisible but clickable
            //Log.e("DOOBY",favePageOffset+"  ,   "+ whatsHotPageOffset +"  ,   "+ openTabsPageOffset+"   ,   "+historyPageOffset+"   = "+faveBtnColour+" , "+whatsHotBtnColour);
        }
    };

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (gestureDetector.onTouchEvent(e)) { // (see VerticalScrollDetector) required to stop our horizontal recycler view mistakenly scrolling on vertical swipes
            return false;
        }
        return super.onInterceptTouchEvent(e);
    }

    public class VerticalScrollDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // required to stop our horizontal recycler view scrolling on vertical swipes
            return Math.abs(distanceX) < (Math.abs(distanceY) * 3);
        }

    }

    @Override
    public boolean hasOverlappingRendering() {
        return false; // remove override if this causes problems with alpha transparency
    }


}
