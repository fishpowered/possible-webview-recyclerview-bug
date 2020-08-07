package fishpowered.best.browser;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import fishpowered.best.browser.utilities.UrlHelper;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import fishpowered.best.browser.utilities.UnitHelper;

import static android.view.MotionEvent.INVALID_POINTER_ID;

public class PreviewWindow implements SharedBrowserInterface {

    private FrameLayout inspectionWindowTemplate;
    public ConstraintLayout inspectionWindowContainer;
    private Browser browser;
    private WebView inspectionWebView;
    public boolean isOpen = false;
    private ImageButton closeWindowButton;
    private ImageButton openInTabButton;
    private ImageButton backButton;
    private ImageButton stopRefreshForwardButton;
    private TextView addressHint;
    private ImageView loadingView;
    private WebViewClient inspectionWebViewClient;
    public ImageView loadingMaskView;
    private ProgressBar loadingBarView;
    public String currentInspector;
    public boolean pinned = false;

    public PreviewWindow(Browser browser){
        this.browser = browser;
    }

    public void show(){
        browser.setCompactMode(true);
        if(inspectionWindowContainer ==null){
            initInspectionWindow();
        }else{
            inspectionWindowContainer.setVisibility(View.VISIBLE);
        }
        isOpen = true;

    }

    public void show(String inspectionText, String inspector, boolean clearExistingWebViewInPinnedWindow){
        browser.setCompactMode(true);
        pinned = true;
        if(inspectionText==null){
            inspectionText = "";
        }
        if(inspector==null && currentInspector!=null){
            inspector = currentInspector; // use previous inspector if no new one set
        }else if(inspector==null){
            inspector = "search";
        }
        //String[] singleWordInspectors = new String[] { "dictionary" }; if there's ever more than just dictionary
        if(inspector.equals("dictionary") && inspectionText.trim().split("\\s").length > 1){
            TabViewHolder currentTab = getBrowser().getCurrentTabViewHolder();
            String fallback = "translate";
            if(currentTab!=null) {
                // maybe multiple words were selected by accident so if it's a foreign site use translate site, otherwise show a search engine
                for (String domain : UrlHelper.englishDomains) {
                    if (UrlHelper.doesUrlBelongToDomain(currentTab.getCurrentPageAddress(null), domain)) { // this could be better
                        fallback = "search";
                        break;
                    }
                }
            }
            inspector = fallback;
            currentInspector = "dictionary";
        } else {
            currentInspector = inspector;
        }
        show();
        String targetUrl;
        switch (inspector){
            case "search":
                targetUrl = UrlHelper.getSearchEngineUrl(inspectionText.trim(), getBrowser(), null);
                break;
            case "imagesearch":
                targetUrl = UrlHelper.getImageSearchEngineUrl(inspectionText.trim());
                break;
            case "dictionary":
                targetUrl = UrlHelper.getDictionaryUrl(inspectionText.trim().toLowerCase(), getBrowser());
                break;
            case "translate":
                targetUrl = UrlHelper.getTranslationUrl(inspectionText.trim(), browser);
                break;
            case "calculator":
                targetUrl = UrlHelper.getCalculatorUrl(inspectionText.trim(), browser);
                break;
            case "customurl":
                targetUrl = inspectionText.trim();
                break;
            case "none": // use when we want to show the window but not load anything (required for target blank wiring)
            default:
                targetUrl = null;
        }
        if(targetUrl!=null) {
            inspectionWebView.loadUrl(targetUrl);
            WebSettings webSettings = inspectionWebView.getSettings();
            if(inspector.equals("calculator")){
                webSettings.setTextSize(WebSettings.TextSize.SMALLEST);
            } else {
                webSettings.setTextSize(WebSettings.TextSize.SMALLER); // SMALLEST is good for wikipedia, not great for other sites
            }
        }
    }

    private void initInspectionWindow() {
        LayoutInflater vi = (LayoutInflater) browser.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(vi==null) return;
        inspectionWindowTemplate = (FrameLayout) vi.inflate(R.layout.preview_window, null);

        // insert into main view
        ViewGroup insertPoint = (ViewGroup) browser.findViewById(R.id.browser_layout);
        insertPoint.addView(inspectionWindowTemplate, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        inspectionWindowTemplate.bringToFront();
        inspectionWindowContainer = (ConstraintLayout) inspectionWindowTemplate.findViewById(R.id.preview_window_container);

        inspectionWebView = (WebView) inspectionWindowContainer.findViewById(R.id.translation_webview);
        inspectionWebView.getSettings().setJavaScriptEnabled(true);
        inspectionWebViewClient = new WebViewClient();
        inspectionWebView.setWebViewClient(inspectionWebViewClient); // forces links to open in self, not OS default browser
        WebChromeClient webChromeClient = new WebChromeClient();
        inspectionWebView.setWebChromeClient(webChromeClient);
        loadingMaskView = inspectionWindowContainer.findViewById(R.id.loading_view_mask_inspection_window);
        loadingBarView = (ProgressBar) inspectionWindowContainer.findViewById(R.id.loadingBarInspectionWindow);

        closeWindowButton = (ImageButton) inspectionWindowContainer.findViewById(R.id.inspection_close_window);
        closeWindowButton.setOnClickListener(this.onCloseButtonClicked);

        openInTabButton = (ImageButton) inspectionWindowContainer.findViewById(R.id.inspection_open_in_tab);
        openInTabButton.setOnClickListener(this.onOpenInFullSizedTabButtonClicked);

        backButton = (ImageButton) inspectionWindowContainer.findViewById(R.id.inspection_back_button);
        backButton.setOnClickListener(onBackButtonClick);

        stopRefreshForwardButton = (ImageButton) inspectionWindowContainer.findViewById(R.id.inspection_stop_refresh_button);
        stopRefreshForwardButton.setOnClickListener(onStopRefreshButtonClicked);

        addressHint = (TextView) inspectionWindowContainer.findViewById(R.id.inspection_address_hint);
        ImageView resizeHandle = inspectionWindowContainer.findViewById(R.id.inspection_resize_handle);
        View inspectionTopBar = (View) inspectionWindowContainer.findViewById(R.id.inspection_top_toolbar);
        inspectionTopBar.setOnTouchListener(this.onDragWindowEvent);
        addressHint.setOnTouchListener(this.onDragWindowEvent);
        resizeHandle.setOnTouchListener(this.onResizeWindowEvent);

    }

    public void updateBackForwardRefreshButtonStatus() {
        if (inspectionWebView.canGoBack()) {
            backButton.setImageAlpha(255);
        } else {
            backButton.setImageAlpha(100);
        }
        if(inspectionWebView.getProgress() < 100){
            stopRefreshForwardButton.setImageResource(R.drawable.ic_nav_bar_stop_mini);
        }else if(inspectionWebView.canGoForward()){
            stopRefreshForwardButton.setImageResource(R.drawable.ic_nav_bar_forward_mini);
        }else{
            stopRefreshForwardButton.setImageResource(R.drawable.ic_nav_bar_refresh_mini);
        }
    }

    protected View.OnClickListener onCloseButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hide(true);
            TabViewHolder tab = getBrowser().getCurrentTabViewHolder();
            if(tab!=null){
            }
            getBrowser().updateCompactMode();
        }
    };

    protected View.OnClickListener onOpenInFullSizedTabButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String url = inspectionWebView.getUrl();
            hide(true);
            if(getBrowser().isSpeedDialVisible() && getBrowser().getCurrentTabViewHolder()!=null){
                TabViewHolder tab = getBrowser().getCurrentTabViewHolder();
                tab.goToAddressOrSearchString(url);
            } else {
                browser.openAddressOrSearchInNewTab(url, true, Browser.TAB_OPENED_BY_OTHER_TAB, true);
            }
            getBrowser().hideSpeedDial(false);
        }
    };

    public void hide(boolean unpin) {
        if(unpin){
            pinned = false;
        }
        if(inspectionWindowContainer !=null && isOpen){
            inspectionWindowContainer.setVisibility(View.GONE);
        }
        isOpen = false;
        browser.updateCompactMode();
    }

    /**
     * Browser Back button event
     */
    protected View.OnClickListener onBackButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            browser.bounceButton(getBackButton());
            inspectionWebView.goBack();
        }
    };

    protected View.OnClickListener onStopRefreshButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            browser.bounceButton(getStopRefreshForwardButton());
            inspectionWebView.reload();
        }
    };

    @Override
    public Browser getBrowser() {
        return browser;
    }

    @Override
    public TextView getAddressHintView() {
        return addressHint;
    }

    @Override
    public ProgressBar getLoadingBarView() {
        return loadingBarView;
    }

    @Override
    public ImageView getLoadingMaskView() {
        return loadingMaskView;
    }

    @Override
    public WebView getWebView() {
        return inspectionWebView;
    }

    @Override
    public ImageButton getStopRefreshForwardButton() {
        return stopRefreshForwardButton;
    }

    @Override
    public ImageButton getBackButton() {
        return backButton;
    }

    @Override
    public TabViewHolder getTabViewHolder() {
        return null;
    }

    @Override
    public boolean isPreviewWindow() {
        return true;
    }

    @Override
    public WebViewClient getWebViewClient() {
        return inspectionWebViewClient;
    }

    // The ‘active pointer’ is the one currently moving our object.
    private int mActivePointerId = INVALID_POINTER_ID;


    private View.OnTouchListener onDragWindowEvent = new View.OnTouchListener() {
        public float windowStartY;
        public static final float DRAG_THRESHOLD = 10f;
        public boolean isTap;
        public float startY;

        /**
         * Called when a touch event is dispatched to a view. This allows listeners to
         * get a chance to respond before the target view.
         * @return True if the listener has consumed the event, false otherwise.
         */
        @Override
        public boolean onTouch(View v, MotionEvent ev) {
            final int action = ev.getAction();
            float y = ev.getRawY();
            // TODO https://developer.android.com/training/gestures/scale add findPointerIndex to prevent multiple touches messing up drags
            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    // finger down, detect start of swipe/click
                    startY = y;
                    isTap = true;
                    windowStartY = inspectionWindowContainer.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isTap && (Math.abs(startY - y) > DRAG_THRESHOLD)) {
                        isTap = false;
                        return false; // allow the buttons on the bar to catch the event
                    }
                    if(isTap == false && startY > 0){
                        final float newY = windowStartY + (y - startY);
                        int barY = getBrowser().getScreenHeight();
                        if(newY > 0 && newY < (barY - UnitHelper.convertDpToPx(100, getBrowser()))){
                            // Ensure the window hasn't gone off the bottom of the screen
                            inspectionWindowContainer.setY(newY > 0 ? newY : 0);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP: // finger up
                        startY = 0;
            }
            return true;
        }
    };

    private View.OnTouchListener onResizeWindowEvent = new View.OnTouchListener() {
        public float windowStartHeight;
        public static final float DRAG_THRESHOLD = 10f;
        public boolean isTap;
        public float startY;

        /**
         * Called when a touch event is dispatched to a view. This allows listeners to
         * get a chance to respond before the target view.
         * @return True if the listener has consumed the event, false otherwise.
         */
        @Override
        public boolean onTouch(View v, MotionEvent ev) {
            final int action = ev.getAction();
            float y = ev.getRawY();
            // TODO https://developer.android.com/training/gestures/scale add findPointerIndex to prevent multiple touches messing up drags
            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    // finger down, detect start of swipe/click
                    startY = y;
                    isTap = true;
                    windowStartHeight = inspectionWindowContainer.getHeight();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isTap && (Math.abs(startY - y) > DRAG_THRESHOLD)) {
                        isTap = false;
                    }
                    if(!isTap && startY > 0){
                        final float newHeight = windowStartHeight + (y - startY);
                        if(newHeight > UnitHelper.convertDpToPx(60, getBrowser())
                            && newHeight < (getBrowser().getScreenHeight() - UnitHelper.convertDpToPx(80, getBrowser()) - inspectionWindowContainer.getY())
                           ){
                            ViewGroup.LayoutParams params = inspectionWindowContainer.getLayoutParams();
                            params.height = (int) newHeight;
                            inspectionWindowContainer.setLayoutParams(params);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP: // finger up
                    startY = 0;
            }
            return true;
        }
    };
}
