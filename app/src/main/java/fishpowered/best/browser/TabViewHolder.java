package fishpowered.best.browser;

import android.graphics.Color;
import android.preference.PreferenceManager;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import fishpowered.best.browser.db.BrowsingHistoryRepository;
import fishpowered.best.browser.utilities.ThemeHelper;
import fishpowered.best.browser.utilities.UrlHelper;

import android.view.View;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import fishpowered.best.browser.utilities.UnitHelper;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Container for the tab layout used by the recyclerview pager
 */
public class TabViewHolder extends RecyclerView.ViewHolder implements SharedBrowserInterface {

    /**
     * Adds a bit of buffer before animating the nav bar down, this allows time for the layout to switch
     * 0 - 1 is the animating part
     */
    public static final float NAV_BAR_SCROLL_FRACTION_VISIBLE = 1f;
    public static final float NAV_BAR_SCROLL_FRACTION_HIDDEN = 0f;
    public final Browser browser;
    public final WebView webView;
    //public final TextView notificationHint;
    public final ImageButton navButton4;
    public final ImageButton navButton3;
    public final ImageButton navButton2;
    public final ImageButton navButton1;
    public final TextView navButtonTextOverlay4;
    public final TextView navButtonTextOverlay3;
    public final TextView navButtonTextOverlay2;
    public final TextView navButtonTextOverlay1;
    public final TextView addressHintView;
    public final ConstraintLayout tabLayout;
    private final View pageContextMenuHolder;
    private final View selectionContextMenuHolder;
    private final View findInPageMenuHolder;
    public View pageContextMenu;
    public final ImageView loadingMaskView;
    public final LinearLayout mainNavBarLayout;
    public final ProgressBar loadingBar;
    public boolean isFingerTouchingWebView = false;
    public boolean isScrollYClamped = false;
    public Long timeLastTouchedInMs = 0L;
    private boolean isScrollXClamped = true;
    public ArrayList<String> elementLayersThatCanBeHidden;
    public HashSet<Integer> elementLayersToHide;
    public int originalContextIconColour = Color.GRAY;
    private String pref_longPressNavBtn4Action;
    private String pref_longPressNavBtn3Action;
    private String pref_longPressNavBtn2Action;
    private String pref_longPressNavBtn1Action;
    private String pref_quickPressNavBtn4Action;
    private String pref_quickPressNavBtn3Action;
    private String pref_quickPressNavBtn2Action;
    private String pref_quickPressNavBtn1Action;
    public boolean isAnimatingNavBar = false;
    public boolean isNavBarUp = true;
    public boolean dontScrollNavBar = false;
    public boolean isFreshlyLoaded;
    private boolean hasScreenBeenPulledDown=false;
    private TabState tabState;

    /**
     * Has the scroll been locked for the tab by clicking on the scroll lock button
     */
    private boolean isTabSwipingLocked = false;
    private WebViewClient webViewClient;
    private WebChromeClient webChromeClient;

    public TabViewHolder(View tabLayout, Browser browser) {
        super(tabLayout);
        this.isFreshlyLoaded = true;
        this.browser = browser;
        this.tabLayout = (ConstraintLayout) tabLayout;
        webView                         = tabLayout.findViewById(R.id.activity_main_webview);

        //webView.tabViewHolder = this;
        //notificationHint                = tabLayout.findViewById(R.id.notificationHint);
        navButton3 = tabLayout.findViewById(R.id.navButton3);
        navButton2 = tabLayout.findViewById(R.id.navButton2);
        navButton1 = tabLayout.findViewById(R.id.navButton1);
        navButton4 = tabLayout.findViewById(R.id.navButton4);
        navButtonTextOverlay3 = tabLayout.findViewById(R.id.navButtonTextOverlay3);
        navButtonTextOverlay2 = tabLayout.findViewById(R.id.navButtonTextOverlay2);
        navButtonTextOverlay1 = tabLayout.findViewById(R.id.navButtonTextOverlay1);
        navButtonTextOverlay4 = tabLayout.findViewById(R.id.navButtonTextOverlay4);
        addressHintView = tabLayout.findViewById(R.id.address_hint);
        loadingMaskView = tabLayout.findViewById(R.id.loading_view_mask);
        //notificationHint.setVisibility(View.GONE);
        mainNavBarLayout = tabLayout.findViewById(R.id.main_nav_bar_layout);
        pageContextMenuHolder = tabLayout.findViewById(R.id.page_context_menu_holder);
        selectionContextMenuHolder = tabLayout.findViewById(R.id.selection_context_menu_holder);
        findInPageMenuHolder = tabLayout.findViewById(R.id.find_in_page_menu_holder);
        loadingBar = tabLayout.findViewById(R.id.loadingBar);
        pageContextMenu = null;
        originalContextIconColour = ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.contextMenuIconColour, browser);
        initNavBarIcons();
        initWebView();
        initNavBarEvents();
        //itemView.setOnFocusChangeListener(this.onFocusChange);
    }

    public int getScrollYOffset(){
        return 0;
    }

    // Sets the icons based on configured action preferences
    private void initNavBarIcons() {
        pref_quickPressNavBtn1Action = browser.preferences.getString(
                "quickPressNavBtn1Action",
                    browser.getString(R.string.defaultNavButton1Action)
        );
        pref_quickPressNavBtn2Action = browser.preferences.getString(
                "quickPressNavBtn2Action",
                browser.getString(R.string.defaultNavButton2Action)
        );
        pref_quickPressNavBtn3Action = browser.preferences.getString(
                "quickPressNavBtn3Action",
                browser.getString(R.string.defaultNavButton3Action)
        );
        pref_quickPressNavBtn4Action = browser.preferences.getString(
                "quickPressNavBtn4Action",
                browser.getString(R.string.defaultNavButton4Action)
        );
        pref_longPressNavBtn1Action = browser.preferences.getString(
                "longPressNavBtn1Action",
                browser.getString(R.string.defaultNavButton1LongPressAction)
        );
        pref_longPressNavBtn2Action = browser.preferences.getString(
                "longPressNavBtn2Action",
                browser.getString(R.string.defaultNavButton2LongPressAction)
        );
        pref_longPressNavBtn3Action = browser.preferences.getString(
                "longPressNavBtn3Action",
                browser.getString(R.string.defaultNavButton3LongPressAction)
        );
        pref_longPressNavBtn4Action = browser.preferences.getString(
                "longPressNavBtn4Action",
                browser.getString(R.string.defaultNavButton4LongPressAction)
        );
        String defaultTextSearchEngine = browser.preferences.getTextSearchEngine(null, false).first;

        updateTabButtonIconCount(0);
        float addressHintWeight = 0.4f;
        boolean setWeight = false;
    }

    private void initWebView() {
        webViewClient = new WebViewClient();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSavePassword(true);
        webView.getSettings().setSaveFormData(true);
        webView.setWebViewClient(webViewClient); // forces links to open in self, not OS default browser
    }


    /**
     * (goes above 1 to allow lag before we scroll the nav bar down)
     * >= 1 navbar fully open, 0=fully closed
     */
    float navBarScrollFraction = NAV_BAR_SCROLL_FRACTION_VISIBLE;
    private boolean webViewScrollingDown = false;
    private boolean prevWebViewScrollingDown = false;
    public int distanceScrolledWithContextOpen = 0;
    public float distanceScrolledDpWithFingerPressed = 0;



    public void animateNavBarToVisible(final boolean animateToUpAndVisible) {
    }

    /**
     * We don't want to hide the nav bar if we are within the navbar's height at the bottom or top of the page
     */
    public boolean hasSpaceToHideNavBarWithoutJudder() {
        return true;
//        int navBarHeight = UnitHelper.getNavBarHeightInPx(browser.isInCompactMode(), browser);
//        int bottomPadding = navBarHeight*3;
//        int contentHeight = (int) Math.floor(webView.getContentHeight_FIXED()); // Not sure if  * webView.getScaleY()  is needed with custom height method?
//        final boolean isScrolledAlmostToBottom = currentWebViewScrollY_px >= (contentHeight - webView.getHeight() - navBarHeight - bottomPadding);
//        //if(contentHeight - currentWebViewScrollY_px == webView.getHeight())
//
//        if(currentWebViewScrollY_px <= navBarHeight
//            || isScrolledAlmostToBottom
//        ){ // DISTANCE_FROM_TOP_BEFORE_NAV_BAR_SCROLLS
//                // Don't hide the navbar until we've scrolled past a certain distance. Always allow it to return though
//                // Current reasoning for having the min threshold the height of the navbar is because if you start scrolling
//                // before that but the page height is less than 68 then the layout will snap to the parent, removing
//                // any ability to scroll and so then you wouldn't be able to scroll back up to bring back the nav bar
//            //Log.d("NAVBARANIM", "Near to bottom/top, skip anim. cY:"+currentWebViewScrollY_px+"  - wH:"+webView.getHeight()+" cH:"+contentHeight+" ("+webView.getScaleY()+") ="+(isScrolledAlmostToBottom?"BOTTOM":"0"));
//            return false;
//        }
//        return true;
    }

    private float lastTranslationSet = 0f;
    private void setNavBarTranslationYAsFraction(float fraction, boolean force) {
      //  if(true) return;
        if(!force && (
            (fraction > 0.99f && navBarScrollFraction > 0.99f)
            || (fraction < 0.01f && navBarScrollFraction < 0.01f))
        ){
            //Log.d("NAVBARANIM", "SKIPPING animating to "+fraction);
            return;
        }
        if(navBarScrollFraction > NAV_BAR_SCROLL_FRACTION_VISIBLE){
            navBarScrollFraction = NAV_BAR_SCROLL_FRACTION_VISIBLE;
        }else if (navBarScrollFraction < NAV_BAR_SCROLL_FRACTION_HIDDEN){
            navBarScrollFraction = NAV_BAR_SCROLL_FRACTION_HIDDEN;
        }else {
            navBarScrollFraction = fraction;
        }
        float animationFraction = fraction;
        /*if(findInPageMenu.isOpen()){
            constrainWebViewBottomToFindInPageBar();
            animationFraction = 0f;
        }else*/ if(animationFraction > 0.95f){
            constrainWebViewBottomToNavBar();
            animationFraction = 1f;
        }else{
            constrainWebViewBottomToWindowBottom();
        }
        if(animationFraction < 0.015f){
            animationFraction = 0f;
        }
        final float navBarHeight = UnitHelper.getNavBarHeightInPx(browser.isInCompactMode(), browser);
        final float tY = navBarHeight - (navBarHeight * animationFraction);
        if(Math.abs(tY - lastTranslationSet) > 0.01f) {
            lastTranslationSet = tY;
            mainNavBarLayout.setTranslationY(tY);
            selectionContextMenuHolder.setTranslationY(tY);
            pageContextMenuHolder.setTranslationY(tY);
            findInPageMenuHolder.setTranslationY(tY);
        }
    }


    public void constrainWebViewBottomToWindowBottom() {
    }

    public void constrainWebViewBottomToNavBar() {
    }

    public void goToAddressOrSearchString(String urlOrSearchString) {
        if(urlOrSearchString==null){
            return;
        }else{
            urlOrSearchString = urlOrSearchString.trim();
        }
        boolean isUrl = UrlHelper.isUrlAttempt(urlOrSearchString);
        boolean recordSearch = false;
        String urlToLoad;
        //webView.stopLoading();
        if(isUrl) {
            String url = urlOrSearchString;
            if (!url.startsWith("data:") && !URLUtil.isValidUrl(url) && URLUtil.isValidUrl("http://" + url) && !url.startsWith(UrlHelper.OFFLINE_URL_PROTOCOL)) {
                url = UrlHelper.appendHttpProtocolToUrl(url, false);
            }
            urlToLoad = url;
        }else{
            String searchUrl = UrlHelper.getSearchEngineUrl(urlOrSearchString, getBrowser(), null);
            urlToLoad = searchUrl;
        }
        if(urlToLoad!=null) {
            webView.loadUrl(urlToLoad);
            final int tabPosition = getAdapterPosition();
            if (tabPosition >= 0) {
                browser.tabAdapter.updateAddressAtPosition(tabPosition, urlToLoad);
            }
        }
    }

    protected void initNavBarEvents(){
        navButton1.setOnClickListener(this.onNavButton1Click);
        navButton2.setOnClickListener(this.onNavButton2Click);
        navButton3.setOnClickListener(this.onNavButton3Click);
        navButton4.setOnClickListener(this.onNavButton4Click);

        addressHintView.setOnClickListener(this.onAddressHintClick);
        addressHintView.setOnLongClickListener(this.onAddressHintLongClick);

        navButton1.setOnLongClickListener(this.onNavButton1LongClick);
        navButton2.setOnLongClickListener(this.onNavButton2LongClick);
        navButton3.setOnLongClickListener(this.onNavButton3LongClick);
        navButton4.setOnLongClickListener(this.onNavButton4LongClick);
    }

    // ---------------------------------------------- QUICK PRESS BUTTONS.....
    protected View.OnClickListener onNavButton1Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onNavButtonClick(pref_quickPressNavBtn1Action);
        }
    };
    protected View.OnClickListener onNavButton2Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onNavButtonClick(pref_quickPressNavBtn2Action);
        }
    };

    protected View.OnClickListener onNavButton3Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onNavButtonClick(pref_quickPressNavBtn3Action);
        }
    };

    protected View.OnClickListener onNavButton4Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onNavButtonClick(pref_quickPressNavBtn4Action);
        }
    };

    // ---------------------------------------------- LONG PRESS BUTTONS.....
    protected View.OnLongClickListener onNavButton1LongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            return onNavButtonClick(pref_longPressNavBtn1Action);
        }
    };
    protected View.OnLongClickListener onNavButton2LongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            return onNavButtonClick(pref_longPressNavBtn2Action);
        }
    };
    protected View.OnLongClickListener onNavButton3LongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            return onNavButtonClick(pref_longPressNavBtn3Action);
        }
    };
    protected View.OnLongClickListener onNavButton4LongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            return onNavButtonClick(pref_longPressNavBtn4Action);
        }
    };

    private boolean onNavButtonClick(String action) {
        if(browser.canBrowserMenuButtonsBePressed) {
            browser.bounceButtonIfMakesSense(getNavButtonBasedOnAction(action), action);
            triggerCustomAction(action);
            return true;
        }
        return false;
    }


    // trigger action based on a button press that is configured to do something @todo move to Browser
    protected void triggerCustomAction(String actionType){
        switch(actionType){
            case "noAction":
            case "disabled":
                break;
            case "pinToWindow":
                getBrowser().previewWindow.show(getCurrentPageAddress("getOfflineUrl"), "customurl", true);
                break;
            case "navigateBack":
                browser.onBackPressed();
                break;
            case "navigateForward":
                webView.goForward();
                break;
            case "refresh":
                webView.reload();
                break;
            default:
                triggerShowSpeedDial(true, -1);
                break;
        }
    }

    private void triggerShowSpeedDial(boolean openKeyboard, int jumpToList) {
        final String currentUrl = webView.getUrl();
        browser.addressInput.setText(currentUrl);
        browser.showSpeedDial(false, 0, openKeyboard, true, true, "triggerShowSpeedDIal", false);
        if(jumpToList >= 0){
            browser.speedDialListAdapter.scrollToViewList(jumpToList, false);
        }
    }


    /**
     * Has the user clicked the scroll lock button to lock tab scrolling so they can focus on page scrolling
     * @param flag
     */
    public void setTabSwipingLocked(boolean flag){
        this.isTabSwipingLocked = flag;
        browser.updateTabSwipeLockBtn();
    }

    /**
     * Has the user clicked the scroll lock button to lock tab scrolling so they can focus on page scrolling
     */
    public boolean isTabSwipingLocked(){
        return isTabSwipingLocked;
    }

    /**
     * Trigger an action (based on settings) when the address hint is quick clicked
     */
    protected View.OnClickListener onAddressHintClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            BrowsingHistoryRepository.clearAllBrowsingData(browser);
            webView.reload();
        }
    };

    /**
     * Trigger an action (based on settings) when the address hint is long clicked
     */
    protected View.OnLongClickListener onAddressHintLongClick = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            return true;
        }
    };

    /**
     * The tab view is being created/recycled and now we have to update it with the latest data
     */
    public void bindDataToTab(TabState tabState, boolean isChildWebView) {
        this.tabState = tabState;
        String address = tabState.address;
        if (address != null && !address.equalsIgnoreCase(webView.getUrl())) {
            // we are updating the tab adapter state everytime a page is clicked but
            // we don't want to trigger going to the address on bind unless it's not already there
            // otherwise the view should be restored how it was before
            goToAddressOrSearchString(address);
        }
    }

    public void updateTabButtonIconCount(int plus) {
        int itemCountOfUsedTabs =browser.tabAdapter.getItemCount();
        TabLinearLayoutManager.updateTabButtonTabCount(navButtonTextOverlay1, String.valueOf(itemCountOfUsedTabs));
        TabLinearLayoutManager.updateTabButtonTabCount(navButtonTextOverlay2, String.valueOf(itemCountOfUsedTabs));
        TabLinearLayoutManager.updateTabButtonTabCount(navButtonTextOverlay3, String.valueOf(itemCountOfUsedTabs));
        TabLinearLayoutManager.updateTabButtonTabCount(navButtonTextOverlay4, String.valueOf(itemCountOfUsedTabs));
    }

    /**
     * Show tab
     */
    public void show(){
        itemView.setVisibility(View.VISIBLE);
        itemView.setAlpha(1f); // if the view has been restored, it might be invisible still if it was cleared previously
       // webView.resumeVideosAndStuff();
    }


    /**
     * Hide tab
     */
    public void hide(){
        //webView.pauseVideosAndStuff(); this can cause pages to get stuck sometimes
        itemView.setVisibility(View.GONE);
    }

    private boolean isTabInCompactMode = false;
    public void setCompactMode(boolean bool, boolean force) {
    }

    /*private View.OnFocusChangeListener onFocusChange = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Log.e("XISTORY", (hasFocus ? "[STARTED]" : "[STOPPED]")+" "+webView.getUrl()); doesn't work but cool idea
        }
    };*/


    @Override
    public Browser getBrowser() {
        return browser;
    }

    @Override
    public TextView getAddressHintView() {
        return addressHintView;
    }

    @Override
    public ProgressBar getLoadingBarView() {
        return loadingBar;
    }

    @Override
    public WebView getWebView() {
       return webView;
    }

    @Override
    public WebViewClient getWebViewClient() {
        return webViewClient;
    }

    public WebChromeClient getWebChromeClient() {
        return webChromeClient;
    }

    @Override
    public ImageButton getStopRefreshForwardButton() {
        return getNavButtonBasedOnAction("refresh");
    }

    private ImageButton getNavButtonBasedOnAction(String action) {
        if(pref_quickPressNavBtn1Action.equals(action)){
            return navButton1;
        }else if(pref_quickPressNavBtn2Action.equals(action)){
            return navButton2;
        }else if(pref_quickPressNavBtn3Action.equals(action)){
            return navButton3;
        }else if(pref_quickPressNavBtn4Action.equals(action)){
            return navButton4;
        }
        return null;
    }

    @Override
    public ImageButton getBackButton() {
        return getNavButtonBasedOnAction("navigateBack");
    }

    @Override
    public TabViewHolder getTabViewHolder() {
        return this;
    }

    @Override
    public boolean isPreviewWindow() {
        return false;
    }

    @Override
    public ImageView getLoadingMaskView() {
        return loadingMaskView;
    }

    /**
     * @param offlineUrlInstructions If you are viewing a saved for later page should the offline or online URL be returned.
     *                               Note: a db query may be involved if fetching offline/online URL's
     * @return
     */
    public String getCurrentPageAddress(String offlineUrlInstructions) {
        String realUrl = getWebView().getUrl();
        return realUrl;
    }

    public String getCurrentPageTitle() {
        return "";
    }


    public int getTabId() {
        return this.tabState.tabId;
    }

    public void setNavBarVisibilityWithoutAnimation(boolean setAsUpAndVisible) {
        isNavBarUp = setAsUpAndVisible;
        setNavBarTranslationYAsFraction(setAsUpAndVisible ? 1f : 0f, false);
    }

    public TabState getTabState(){
        return tabState;
    }

    public boolean hasScreenBeenPulledDown(){
        return hasScreenBeenPulledDown;
    }


    public void removeEasyReachOffset(boolean animate){
    }

    public boolean lastInnerScrollWasRecent(Long fingerDownTimeMs) {
        return true;
    }

    public void setScrollXClamped(boolean clamped) {
        isScrollXClamped = clamped;
    }

    public boolean isScrollXClamped(){
        return isScrollXClamped;
    }
}
