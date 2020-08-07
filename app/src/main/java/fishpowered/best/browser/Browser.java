package fishpowered.best.browser;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognizerIntent;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import fishpowered.best.browser.db.DatabaseHelper;
import fishpowered.best.browser.db.BrowsingHistoryRepository;
import fishpowered.best.browser.db.Item;
import fishpowered.best.browser.db.ItemRepository;
import fishpowered.best.browser.db.ItemType;
import fishpowered.best.browser.services.IncognitoModeNotificationService;
import fishpowered.best.browser.speeddial.itemadapters.AbstractItemAdapter;
import fishpowered.best.browser.speeddial.list.SpeedDialListsAdapter;
import fishpowered.best.browser.speeddial.list.SpeedDialListsRecyclerView;
import fishpowered.best.browser.speeddial.list.SpeedDialListViewHolder;
import fishpowered.best.browser.utilities.DateTimeHelper;
import fishpowered.best.browser.utilities.DownloadDetails;
import fishpowered.best.browser.utilities.FileHelper;
import fishpowered.best.browser.utilities.StringHelper;
import fishpowered.best.browser.utilities.ThemeHelper;
import fishpowered.best.browser.utilities.UnitHelper;
import fishpowered.best.browser.utilities.UrlHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;


import static android.view.View.GONE;
import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
import static android.view.View.SYSTEM_UI_FLAG_LOW_PROFILE;

public class Browser extends Activity {

    /**
     * Navigation has the standard/original back, home and menu buttons
     */
    public final static int GESTURE_NAV_MODE_3_BUTTON = 0;

    /**
     * Navigation bar is skinny with a home button and back button. The bar can be swiped horizontally to change apps and vertically to open menus
     */
    public final static int GESTURE_NAV_MODE_2_BUTTON = 1;

    /**
     * Navigation bar is removed and swiping from the left edge navigates back, the right edge forwards, and the bottom edge.
     * The bar can be swiped horizontally to change apps and vertically to open menus
     */
    public final static int GESTURE_NAV_MODE_0_BUTTON = 2;
    public static final String EXIT_INCOGNITO_MODE_INTENT_ACTION = "fishpowered.best.browser.CLOSE_INCOGNITO_MODE";
    private static final int RECORD_VOICE_INTENT_RESULT = 453475236;
    public static boolean safeMode = false;
    public static String searchEngineClientId = "fishpoweredbrowser";
    public static final String packageNamespace = "fishpowered.best.browser";
    public static final String preference_savedOpenTabs = "savedTabs";
    public static final String preference_savedClosedTabs = "closedTabs";
    public static final String preference_savedTabPosition = "savedTabPosition";
    public static final String preference_lastUsedTabId = "lastTabId";
    public static final String preference_lastUsedSpeedDialListPosition = "lastSpeedDialListId";

    /**
     * 0 = no tutorials shown
     * 1 = starting tutorials shown
     * 2 =
     */
    public static String preference_tutorialsShown = "tutorialsShown";
    public static String preference_savedCredentialsList = "savedCredentialsList";
    private static HashSet<String> adBlockList;
    private static HashMap<String, Long> timers;

    // Browser views
    public TabRecyclerView tabRecyclerView;
    public TabAdapter tabAdapter;
    public TabLinearLayoutManager tabLayoutManager;
    public String lastClosedTabAddress;
    public int lastClosedTabPosition;
    public int lastClosedTabId;

    // Speed dial views
    public AddressInputEditText addressInput;
    public SQLiteDatabase db;
    /**
     * @deprecated
     */
    public String currentTag;
    public HashSet<String> domainsVisitedInPrivateMode = new HashSet<String>();
    public LinearLayout tagLinearLayout;
    public ConstraintLayout speedDialContainer;
    public PreviewWindow previewWindow;
    public Integer textToSpeechStartedOnTabId;
    public ArrayList<Integer> alreadyRestored = new ArrayList<Integer>();
    /**
     * Has the user enabled compact mode, or is it enabled by virtue of opening the preview window, multi-window etc
     */
    private boolean isInCompactMode = false;
    /**
     * Has the user opened the keyboard, is doing multi-window etc
     */
    public boolean isRestrictedOnVerticalSpaceInSpeedDial = false;
    public boolean canBrowserMenuButtonsBePressed = true;
    private View rootView;
    public ImageView faveListBtn;
    public ImageView readLaterListBtn;
    public ImageView whatsHotListBtn;
    public ImageView historyListBtn;
    public TextView clearHistoryBtn;
    public Preferences preferences;
    public DownloadDetails pendingDownload;
    public SpeedDialListsAdapter speedDialListAdapter;
    public RecentlyClosedTabAdapter recentlyClosedTabAdapter;
    private ImageView speedDialBackBtn;
    private ImageView speedDialForwardBtn;
    private ImageView speedDialSettingsBtn;
    /**
     * see browser.speedDialListAdapter.getCurrentPosition() and SpeedDialListViewHolder.XXXX_LIST for main list
     * and the value of this property (and ItemType.xxx) for filtering sub-lists/filters
     */
    public int isViewingExpandedSpeedDialListOfType = 0;
    public ArrayList<Integer> filterFavesByTagIds = new ArrayList<>();
    /**
     * Used for filtering history list by domain. Includes protocol
     */
    public String speedDialDomainFilter;
    public SQLiteDatabase database;
    private boolean isKeyboardOpen = false;
    private ConstraintLayout addressBarWrapper;
    private ConstraintLayout speedDialToolbarWrapper;
    private boolean backBtnShouldReturnToTabs;
    public ImageView toggleNightModeBtn;
    public View lockScreenMask;
    private boolean isVideoFullScreened;
    private int mOriginalSystemUiVisibility;
    public TextView viewClosedTabsSpeedDialBtn;
    public TextView openDownloadsBtn;
    private boolean minimisingKeyboardShouldHideSpeedDial = false;
    public boolean animatingSpeedDialFade = false;
    public boolean skipSavingTabStateWhilstClosing=false;
    public TextView onScreenDebug;
    public TextView onScreenDebug2;
    private TextView tabCounter;
    public TextView fortyPercentHintText;
    private static boolean rememberSaveForOfflinePreferenceDefault = true;
    private static Boolean saveForOfflineViewingSessionPreference = null;
    @SuppressWarnings("ConstantConditions")
    private static Boolean rememberedSaveForOfflinePreference = rememberSaveForOfflinePreferenceDefault;
    private TextView tabSwipeLockBtn;
    public TextView siteHomePageBtn;
    private boolean activityJustCalledOnCreate;

    public static void silentlyLogException(Throwable e, Context context) {
        if(e==null || context==null){
            return;
        }
        OutputStreamWriter fos = null;
        try {
            fos = new OutputStreamWriter(context.openFileOutput("error_log", Context.MODE_PRIVATE));
            fos.write(DateTimeHelper.convertTimeStampToLocalDate(DateTimeHelper.getCurrentTimeStamp(), context)+" "+e.toString()+" - "+e.getStackTrace()[0].getClassName()+":"+e.getStackTrace()[0].getLineNumber()); //writing parcel to file
            fos.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if(fos!=null){
                try {
                    fos.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityJustCalledOnCreate = true;
        Log.d("FPSAVINGSTUFF", "onCreate savedInstanceState="+(savedInstanceState==null?"null":"present"));
        /*Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                Browser.silentlyLogException(paramThrowable, Browser.this);
                finish();
            }
        });*/
        Intent intent = getIntent();
        String action = intent.getAction();
        preferences = new Preferences(this);
        final boolean initCrashReporting = preferences.getBoolean("sendCrashReports", true);
        database = DatabaseHelper.getInstance(this).getReadableDatabase();

        //hideAndroidNavigationBar();
        updateSystemNavBarAndTimeBarVisibility();
        if(!preferences.getBoolean("activityForceRecreated", false)){
            Log.d("FPOnBrowserClose", "Browser created, RUNNING HISTORY CLEAN UP");
            // Because we can't reliably detect when an app has been closed, we have to instead flag
            // when we are manually restarting it so we can do clean up when the app is properly created
            // and not just as a result of changing the theme or config.

            if(preferences.isPrivateBrowsingModeEnabled()) { // preferences.getPrivacyLevel(false).equals(Preferences.PRIVACY_LEVEL_SERIOUS) || preferences.getPrivacyLevel(false).equals(Preferences.PRIVACY_LEVEL_EXTREME)
                // Both the storage and cookies are global and it doesn't work reliably to try and selectively delete stuff from it
                // so decided it's best to nuke things completely when switching between private and non-private mode.
                // We don't need to touch the DB history as we simply just don't record when in private mode
                BrowsingHistoryRepository.clearStorage(this); // must be on ui thread
            }
            if(preferences.getClearCookiesOnClose()) {
                BrowsingHistoryRepository.clearAllCookies(this);
                /* maybe a bad idea to asynchronously close cookies on opening the app because we may be about to load a webview
                ClearHistoryAsync clearHistoryAsync = new ClearHistoryAsync(this);
                final String[] params = {};
                clearHistoryAsync.execute(params);*/
            }
            if(preferences.shouldClosingBrowserCloseAllTabs()){
                preferences.edit().putString(Browser.preference_savedOpenTabs, "[]").apply();
                savedInstanceState = null;
            }
        }else{
            Log.d("FPOnBrowserClose", "Browser created, skipping cleanup");
        }
        preferences.edit().putBoolean("activityForceRecreated", false).apply();
        boolean isPrivateBrowsingModeEnabled = preferences.isPrivateBrowsingModeEnabled();
        if(isPrivateBrowsingModeEnabled) {
            ThemeHelper.applyCustomTheme("privateMode", this);
            startIncognitoModeNotificationService();
        }else if(isNightModeEnabled()){
            ThemeHelper.applyCustomTheme("NightMode", this);
        }else {
            ThemeHelper.applyCurrentTheme(this, preferences);
        }
        setContentView(R.layout.activity_browser); // must be below theme setting
        rootView = findViewById(android.R.id.content);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //rootView.
            /*List<Rect> rects = new ArrayList<Rect>();
            rects.add(new Rect(0,0,rootView.getRight(), rootView.getBottom()));
            rootView.setSystemGestureExclusionRects(rects);*/
        }
        lockScreenMask = findViewById(R.id.screen_lock_black_mask);
        previewWindow = new PreviewWindow(this);
        onScreenDebug = rootView.findViewById(R.id.onScreenDebug);
        onScreenDebug2 = rootView.findViewById(R.id.onScreenDebug2);
        fortyPercentHintText = rootView.findViewById(R.id.forty_percent_hint_text);
        addressInput = (AddressInputEditText) findViewById(R.id.addressBarInput);
        tabSwipeLockBtn = (TextView) findViewById(R.id.tab_swipe_lock_btn);
        initSpeedDial();
        initBrowserTabs(savedInstanceState);
        addressInput.initAddressInputAndButtons(this);
        updateCompactMode();
    }

    private void startIncognitoModeNotificationService() {
        final Intent startNotificationServiceIntent = new Intent(this, IncognitoModeNotificationService.class);
        ContextCompat.startForegroundService(
                this,
                startNotificationServiceIntent
        );
    }

    /**
     * Is the user attempting to do a search or filter the results in the main address input
     * @return
     */
    public boolean isSearchingSpeedDialDatabase(boolean treatEmptyAddressAsSearch){
        final TabViewHolder currentTabViewHolder = getCurrentTabViewHolder();
        final String currentAddressInputValue = StringHelper.toString(addressInput.getText() != null ? addressInput.getText().toString().trim() : "");
        if(currentAddressInputValue.equals("")){
            return treatEmptyAddressAsSearch;
        }
        final String currentPageAddress = StringHelper.toString(currentTabViewHolder != null ? currentTabViewHolder.getCurrentPageAddress(null) : "").trim();
        if (
            currentAddressInputValue.startsWith("file://")
            //|| currentTypedAddress.startsWith(UrlHelper.OFFLINE_URL_PROTOCOL)
            || currentPageAddress.startsWith("data://")
            || currentPageAddress.equalsIgnoreCase(currentAddressInputValue)
        ) {
            return false;
        }
        return true;
    }

    public boolean isInCompactMode(){
        return isInCompactMode;
    }

    public boolean isKeyboardOpen(){
        return isKeyboardOpen;
    }

    /**
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @see #onRestoreInstanceState
     * @see #onRestart
     * @see #onPostResume
     * @see #onPause
     */
    @Override
    protected void onResume() {
        super.onResume();

        // We only want to restore from saved instance state if the activity got re-created and the
        // updated savedInstanceState got passed back into the activity through onCreate.
        // If the activity was just paused and resumed, then the state for everything already
        // exists and reloading it will reload stale data
        if(!activityJustCalledOnCreate) {
            alreadyRestored.clear();
            tabAdapter.savedInstanceState = null;
        }
        activityJustCalledOnCreate = false;

        updateSystemNavBarAndTimeBarVisibility();
        handleIntents();
        // Hack because the TabRecyclerView isn't always initialised with the tabs immediately on resume
        final int selectedTabPosition = getSelectedTabPosition(false);
        if(tabAdapter.has(selectedTabPosition)) {
            String urlOnResume = tabAdapter.getAddressAtPosition(selectedTabPosition);
            int tabId = tabAdapter.getTabIdAtPosition(selectedTabPosition);
            if (urlOnResume != null) {
                BrowsingHistoryRepository.startRecordingHistoryItem(this, null, DatabaseHelper.getInstance(this).getWritableDatabase(),
                        urlOnResume, tabId, "resume?");
            }
        }
        canBrowserMenuButtonsBePressed = true;
        if(tabAdapter.getItemCountOfUsedTabs()==0) {
            showSpeedDial(false,0, false,false,false,
                    "onResume, no used tabs found", false);
        }
    }

    private void handleIntents() {
        Intent intent = getIntent();
        String action = intent.getAction();
        // DON'T USE getCurrentTabViewHolder(), don't think the linearlayoutmanager has necessarily caught up by this point
        if(action!=null) {
            if(action.equals(Browser.EXIT_INCOGNITO_MODE_INTENT_ACTION)) {
                setIncognitoModeEnabled(false);
            }else if (action.equals("fishpowered.best.browser.new_tab")) {
                // App shortcut for "new tab". Don't bother trying to recycle unused tabs, they are cleaned up when minimised I think
                triggerOpenNewTab(false, Browser.TAB_OPENED_BY_SPEED_DIAL_OR_SHOULD_RETURN_THERE);
                backBtnShouldReturnToTabs = false;
            }else if (action.equals("fishpowered.best.browser.new_incognito_tab")) {
                if(preferences.isPrivateBrowsingModeEnabled()){
                    triggerOpenNewTab(false, Browser.TAB_OPENED_BY_SPEED_DIAL_OR_SHOULD_RETURN_THERE);
                    backBtnShouldReturnToTabs = false;
                }else{
                    setIncognitoModeEnabled(true);
                }
            }else if (action.equals(Intent.ACTION_VIEW)) {
                // Someone has clicked on a link in another app
                String urlToOpen = intent.getDataString();
                String tabSource;
                final String packageNameOfAppSendingIntent = intent.getComponent()!=null ? intent.getComponent().getPackageName() : null;
                if(packageNameOfAppSendingIntent!=null && packageNameOfAppSendingIntent.equals("fishpowered.best.browser")){
                    // This can happen when a link is clicked that can be opened in either an external app or Fishpowered and the user clicked Fishpowered
                    tabSource = TAB_OPENED_BY_OTHER_TAB;
                }else{
                    tabSource = TAB_OPENED_BY_EXTERNAL_APP;
                }
                openAddressOrSearchInNewTab(urlToOpen, true, tabSource, false);
                hideSpeedDial(false);
            } else if (action.equals(Intent.ACTION_WEB_SEARCH) || action.equals(Intent.ACTION_SEARCH)) {
                // Someone has clicked "web search" from a selection menu
                // https://developer.android.com/reference/android/content/Intent.html#ACTION_WEB_SEARCH
                openAddressOrSearchInNewTab(intent.getStringExtra(SearchManager.QUERY), true,
                        TAB_OPENED_BY_EXTERNAL_APP, false);
                hideSpeedDial(false);
            }else if (action.equals(Intent.ACTION_SEND) && intent.getStringExtra(Intent.EXTRA_TEXT)!=null) {
                // Someone has clicked "web search" from a selection menu
                // https://developer.android.com/reference/android/content/Intent.html#ACTION_WEB_SEARCH
                openAddressOrSearchInNewTab(intent.getStringExtra(Intent.EXTRA_TEXT), true,
                        TAB_OPENED_BY_EXTERNAL_APP, false);
                hideSpeedDial(false);
            }
        }
        // Prevent the intent to open a url being actioned multiple times
        Browser.clearIntent(intent);
    }




    public static void copyToClipboard(Context context, String textToCopy, String notificationText) {
    }

    private void initSpeedDial() {
        db = DatabaseHelper.getInstance(this).getWritableDatabase();
        speedDialContainer = findViewById(R.id.speed_dial_container);
        addressBarWrapper = (ConstraintLayout) speedDialContainer.findViewById(R.id.addressBarWrapper);
        speedDialToolbarWrapper = (ConstraintLayout) speedDialContainer.findViewById(R.id.speed_dial_toolBar_wrapper);
        faveListBtn = speedDialContainer.findViewById(R.id.speed_dial_faves_btn);
        readLaterListBtn = speedDialContainer.findViewById(R.id.speed_dial_read_later_list_btn);
        historyListBtn = speedDialContainer.findViewById(R.id.speed_dial_history_btn);
        whatsHotListBtn = speedDialContainer.findViewById(R.id.speed_dial_whats_hot_btn);
        clearHistoryBtn = findViewById(R.id.clearHistoryBtn);
        siteHomePageBtn = findViewById(R.id.siteHomePageBtn);
        openDownloadsBtn = findViewById(R.id.openDownloadsBtn);
        //viewClosedTabsSpeedDialBtn = findViewById(R.id.viewClosedTabsBtn);
        toggleNightModeBtn = (ImageView) speedDialContainer.findViewById(R.id.nightModeToggleBtn);
        updateNightModeIcon();
        tabCounter = speedDialContainer.findViewById(R.id.speed_dial_tab_counter);
        speedDialBackBtn = (ImageView) speedDialContainer.findViewById(R.id.speed_dial_back_btn);
        speedDialForwardBtn = (ImageView) speedDialContainer.findViewById(R.id.speed_dial_fwd_btn);
        speedDialSettingsBtn = (ImageView) speedDialContainer.findViewById(R.id.speed_dial_settings_btn);
        faveListBtn.setOnClickListener(this.onSpeedDialListHeadingClick);
        historyListBtn.setOnClickListener(this.onSpeedDialListHeadingClick);
        readLaterListBtn.setOnClickListener(this.onSpeedDialListHeadingClick);
        whatsHotListBtn.setOnClickListener(this.onSpeedDialListHeadingClick);
        clearHistoryBtn.setOnClickListener(this.onClearHistoryButtonClick);
        siteHomePageBtn.setOnClickListener(this.onSiteHomePageButtonClick);
        //viewClosedTabsSpeedDialBtn.setOnClickListener(this.onViewClosedTabsButtonClick);
        speedDialBackBtn.setOnClickListener(this.onSpeedDialBackButtonClick);
        speedDialForwardBtn.setOnClickListener(this.onSpeedDialForwardButtonClick);
        speedDialSettingsBtn.setOnClickListener(this.onSpeedDialSettingsButtonClick);
        SpeedDialListsRecyclerView speedDialListRecyclerView = findViewById(R.id.speed_dial_horizontal_list_recycler_view);
        speedDialListRecyclerView.initRecyclerView(this);
        speedDialListAdapter = new SpeedDialListsAdapter(this, preferences, speedDialListRecyclerView);
    }

    /**
     *
     * @return see GESTURE_NAV constants
     */
    public static int getSystemGestureNavigationMode(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("config_navBarInteractionMode", "integer", "android"); // possibly may break over time.. sigh, other thing to check could be WindowInsets.getMandatorySystemGestureInsets()
        if (resourceId > 0) {
            return resources.getInteger(resourceId);
        }
        return 0;
    }

    public boolean isNightModeEnabled(){
        return preferences.isNightModeEnabled();
    }

    private void updateNightModeIcon(){
        if(isNightModeEnabled()){
            toggleNightModeBtn.setImageResource(R.drawable.ic_nightmode_on);
        }else {
            toggleNightModeBtn.setImageResource(R.drawable.ic_nightmode_off);
        }
    }

    public TabViewHolder getCurrentTabViewHolder(){
        final int selectedTabPosition = getSelectedTabPosition(false);
        return getBrowserTabViewHolderAtPosition(selectedTabPosition);
    }

    public TabViewHolder getBrowserTabViewHolderAtPosition(int selectedTabIndex){
        return (TabViewHolder) tabRecyclerView.findViewHolderForAdapterPosition(selectedTabIndex); //.viewItem to get layout/view
    }

    private void initBrowserTabs(Bundle savedInstanceState) {
        tabLayoutManager = new TabLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        tabLayoutManager.setStackFromEnd(true);
        tabAdapter = new TabAdapter(this, preferences, savedInstanceState);
        tabAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                updateTabCounterHint();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                updateTabCounterHint();
            }
        });
        recentlyClosedTabAdapter = new RecentlyClosedTabAdapter(this, preferences);
        tabRecyclerView = findViewById(R.id.tab_recycler_view);
        tabRecyclerView.initRecyclerView(tabLayoutManager, tabAdapter, this);
        if(tabAdapter.getItemCountOfUsedTabs()==0){
            showSpeedDial(true, 0, false, false, true, "zero tabs on start up", false);
        }else{
            int savedTabPosition = preferences.getInt(Browser.preference_savedTabPosition, 0);
            if(tabAdapter.has(savedTabPosition)) {
                tabRecyclerView.scrollToPosition(savedTabPosition);
            }
        }
        //rootView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        updateTabCounterHint();
        tabSwipeLockBtn.setOnClickListener(onTabScrollLockBtnClick);
    }

    @SuppressWarnings("ConstantConditions")
    public void addNewTabAtPosition(int insertIndex, String addressOrSearchString, boolean switchToNewTab, int tabId, String tabOpenedBy, boolean smoothScroll, boolean restoreStateFromStorage) {
        //tabAdapter.removeUnusedTabs(); causes problems when creating a placeholder tab after closing tab by pressing back
        TabState newTabState = new TabState();
        newTabState.tabOpenedBy = tabOpenedBy;
        newTabState.restoreStateFromStorage = restoreStateFromStorage;
        // When opening a new tab, if we know the address we should just show it, otherwise make the tab invisible and slide to our new invisible tab revealing the speed dial
        if(addressOrSearchString == null){
            newTabState.address = null;
            backBtnShouldReturnToTabs = true;
            if(smoothScroll) {
                // Tab opened from custom nav btn
                showSpeedDial(true, 30, false, backBtnShouldReturnToTabs, true, "addNewTab (from "+tabOpenedBy+")", false); // keep the tabs visible so we can slide to the speed dial
            }else{
                // tab opened from tab thumbs list
                showSpeedDial(false, 0, false, backBtnShouldReturnToTabs, true, "addNewTab (from thumb overview - "+tabOpenedBy+")", false); // keep the tabs visible so we can slide to the speed dial
            }
        }else{
            newTabState.address = addressOrSearchString;
            hideSpeedDial(false); // we know the address we are going to, speed dial should not be visible
        }
        newTabState.tabId = tabId;
        tabAdapter.addTabToPosition(newTabState, insertIndex);

        if(switchToNewTab) {
            //TabLinearLayoutManager layout = (TabLinearLayoutManager) tabRecyclerView.getLayoutManager();
            if(smoothScroll) {
                tabRecyclerView.smoothScrollToPosition(insertIndex);
            }else{
                tabRecyclerView.scrollToPosition(insertIndex);
            }
        }
        // when the viewholder is created it creates the TabViewHolder class, when the data is bound (when scrolled to) it will send the webview
        // to the address above
    }

    /**
     * Open child tab (e.g. window.open)
     */
    public void openChildTabAtPosition(int insertIndex, boolean switchToNewTab, int tabId, WebView.WebViewTransport transport, WebView parentWebView, Message resultMsg, String targetUrl) {
        TabState newTabState = new TabState();
        newTabState.tabOpenedBy = TAB_OPENED_BY_OTHER_TAB;
        newTabState.transport = transport;
        newTabState.parentWebView = parentWebView;
        newTabState.resultMsg = resultMsg;
        //WebView.HitTestResult result = parentWebView.getHitTestResult();
        //String url = result.getExtra();
        //newTabState.address = url; // might be null if using window.open
        newTabState.address = targetUrl;
        newTabState.tabId = tabId;
        tabAdapter.addTabToPosition(newTabState, insertIndex);

        if(switchToNewTab) {
            //TabLinearLayoutManager layout = (TabLinearLayoutManager) tabRecyclerView.getLayoutManager();
            tabRecyclerView.smoothScrollToPosition(insertIndex);
        }
        // when the viewholder is created it creates the TabViewHolder class, when the data is bound (when scrolled to) it will send the webview
        // to the address above
    }

    public int getSelectedTabPosition(boolean requireCompletelyVisible) {
        if(requireCompletelyVisible) {
            return tabLayoutManager.findFirstCompletelyVisibleItemPosition();
        }
        return tabLayoutManager.findFirstVisibleItemPosition();
    }

    public void restoreRecentlyClosedTab(TabState recentlyClosedTabState){
    }

    public void removeTabAtPosition(int position, boolean allowUndo, boolean isBeingClosedFromTabThumbsOverview, boolean hasBeenClosedByNavigatingBackFromWv) {
        final TabViewHolder currentTabViewHolder = getCurrentTabViewHolder();
        //currentTabViewHolder.setIsRecyclable(false); // prevent the recyclerpool cache from kicking in if we restore the tab from *persistent storage* later (it can cause double history if both occurs). Commented out because it can cause the overlapping tab bug when closing a tab on back press (when not using persistent storage)
        lastClosedTabAddress = currentTabViewHolder.getWebView().getUrl();
        lastClosedTabPosition = position;
        final TabState tabState = tabAdapter.getTabStates().get(position);
        lastClosedTabId = tabState.tabId;
        final String tabBeingClosedOpenedBy = tabState.tabOpenedBy;
        // https://stackoverflow.com/questions/31367599/how-to-update-recyclerview-adapter-data excellent post about updating recyclerview lists
        if(!isBeingClosedFromTabThumbsOverview && tabBeingClosedOpenedBy.equals(TAB_OPENED_BY_EXTERNAL_APP)){
            // User has swiped to close a tab they just opened from an external app. Close tab and minimise browser
            minimiseBrowser();
            tabAdapter.removeTabAtPositionFromAdapter(position);
            //showSpeedDial(true, 0, false, false, true, false);
            addressInput.setText("");
            return;
        }

        if(hasBeenClosedByNavigatingBackFromWv){
            // User has pressed back enough that we want to close the tab and show the speed dial
            tabAdapter.removeTabAtPositionFromAdapter(position);
            return;
        }

        // Old way where speed dial was shown when closing tabs
        if(tabAdapter.getItemCount()==1
        ){
            // Final tab being closed...
            tabAdapter.removeTabAtPositionFromAdapter(position);
            addressInput.setText("");
            if(preferences.shouldClosingFinalTabCloseBrowser()) {
                minimiseBrowser();
            }else{
                showSpeedDial(true, 20, false, false, true, "final tab being closed", false);
            }
            //speedDialListAdapter.reloadCurrentList(isViewingExpandedSpeedDialListOfType); // todo does this trigger reload twice?
        }else{
            int nextTabToScrollTo = tabAdapter.has(position-1) ? position-1 : position+1; // if u open a link in a new tab, then close the tab, it makes sense to go back to the tab you were previously on
            tabRecyclerView.smoothScrollToPosition(nextTabToScrollTo);
            tabRecyclerView.deletePositionOnSettle = position;
            hideSpeedDial(false);
        }
        /* no longer needed but may be in the future...
        if(allowUndo) {
            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar
                    .make(speedDialContainer, String.format((String) getText(R.string.closed_xxxx), addressHint), Snackbar.LENGTH_SHORT);
            snackbar.setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (lastClosedTabAddress.length() > 0) {
                        // undo is selected, restore the deleted item
                        addNewTabAtPosition(lastClosedTabPosition, lastClosedTabAddress, true, lastClosedTabId, tabBeingClosedOpenedBy, true);
                        lastClosedTabAddress = null;
                        lastClosedTabPosition = 0;
                        if (isSpeedDialVisible()) {
                            hideSpeedDial(false);
                        }
                        updateTabCounterHint();
                    }
                }
            });
            snackbar.setActionTextColor(getResources().getColor(R.color.colorAccent));
            View view = snackbar.getView();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
            params.gravity = Gravity.TOP;
            params.setMargins(0,convertYScreenPercentageToPixels(50),0,0);
            view.setLayoutParams(params);
            // change snackbar text color
            int snackbarTextId = R.id.snackbar_text;
            TextView textView = (TextView)view.findViewById(snackbarTextId);
            textView.setTextColor(Color.WHITE);
            // change snackbar background
            view.setBackgroundColor(Color.DKGRAY);

            snackbar.show();
        }*/
        //tabAdapter.notifyItemRangeChanged(position, tabAdapter.tabs.size());
    }

    public void updateTabCounterHint() {
        final String text = getTabButtonCountText();
        tabCounter.setText(text); // itemCountOfUsedTabs > 0 ? ""+itemCountOfUsedTabs : ""
    }

    public String getTabButtonCountText() {
        int itemCountOfUsedTabs = tabAdapter.getItemCount();
        return (itemCountOfUsedTabs > 99) ? ":D" : String.valueOf(itemCountOfUsedTabs);
    }

    public void setIncognitoModeEnabled(final boolean privateBrowsingModeEnabled){
        if(privateBrowsingModeEnabled && !preferences.hasPrivacyWarningBeenIssued()){
            preferences.setPrivacyWarningIssued(true);
            String privacyDescription = "";
            String privacyTitle = "";
            switch(preferences.getPrivacyLevel(false)){
                case Preferences.PRIVACY_LEVEL_LITE:
                    privacyTitle = getString(R.string.entering_privacy_mode)+": "+getString(R.string.privacy_mode_lite).toLowerCase();
                    privacyDescription = getString(R.string.privacy_help_lite)
                            +"\n\n"+getString(R.string.privacy_mode_protection_can_be_changed_in_settings);
                    break;
                case Preferences.PRIVACY_LEVEL_STANDARD:
                    privacyTitle = getString(R.string.entering_privacy_mode)+": "+getString(R.string.privacy_mode_basic).toLowerCase();
                    privacyDescription = getString(R.string.privacy_help_standard)
                                            +"\n\n"+getString(R.string.privacy_mode_protection_can_be_changed_in_settings);
                    break;
                case Preferences.PRIVACY_LEVEL_SERIOUS:
                    privacyTitle = getString(R.string.entering_privacy_mode)+": "+getString(R.string.privacy_mode_serious).toLowerCase();
                    privacyDescription = getString(R.string.privacy_help_serious);
                    break;
                case Preferences.PRIVACY_LEVEL_EXTREME:
                    privacyTitle = getString(R.string.entering_privacy_mode)+": "+getString(R.string.privacy_mode_extreme).toLowerCase();
                    privacyDescription = getString(R.string.privacy_help_extreme);
                    break;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle(privacyTitle)
                    .setMessage(privacyDescription)
                    .setPositiveButton(R.string.dialog_continue,  new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            _switchPrivateBrowsingMode(privateBrowsingModeEnabled);
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,int id) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }else{
            _switchPrivateBrowsingMode(privateBrowsingModeEnabled);
        }
    }

    private void _switchPrivateBrowsingMode(boolean privateBrowsingModeEnabled) {
        if(privateBrowsingModeEnabled){
            int selectedColour = ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.privateModeIconColor, this);
            ThemeHelper.setIconTint((ImageView) findViewById(R.id.togglePrivateModeBtn), 0xff000000 + selectedColour);
            tabAdapter.saveFullTabWebViewStatesToStore(); // must occur before the privateMode pref is changed
            BrowsingHistoryRepository.endRecording(this, "private browsing start");
        }else{
            int selectedColour = ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.mainNavBarIconColour, this);
            ThemeHelper.setIconTint((ImageView) findViewById(R.id.togglePrivateModeBtn), 0xff000000 + selectedColour);
            if(!preferences.getPrivacyLevel(false).equals(Preferences.PRIVACY_LEVEL_LITE)) {
                BrowsingHistoryRepository.clearAllCookies(this);
            }
            tabAdapter.removeAllTabsFromAdapter(false);
            skipSavingTabStateWhilstClosing = true;
            //flushCookiesCollectedInPrivateMode(); not super reliable
        }
        addressInput.setAddressInputTextWithoutChangeEvent("");
        preferences.edit().putBoolean("privateMode", privateBrowsingModeEnabled).apply();
        recreate();
    }

    /**
     * Recreate the activity e.g. if the theme changes
     */
    @Override
    public void recreate() {
        preferences.edit().putBoolean("activityForceRecreated", true).apply();
        Log.d("FPOnBrowserClose", "Re-creating browser (theme change?)");
        super.recreate();
    }

    private void flushCookiesCollectedInPrivateMode() {
        // Doesn't work well if you only check for cookies on same domain, this would need revising
        String[] domains = new String[domainsVisitedInPrivateMode.size()];
        domainsVisitedInPrivateMode.toArray(domains);
        BrowsingHistoryRepository.clearCookiesForDomains(this, domains);
        domainsVisitedInPrivateMode.clear();
    }

    /**
     * If a tab is already open use that, otherwise use the address in a new tab
     * @param urlOrSearchString
     */
    public void openAddressOrSearch(String urlOrSearchString, boolean backReturnsToSpeedDial) {
        TabViewHolder currentTab = getCurrentTabViewHolder();
        if(currentTab!=null){
            currentTab.show();
            currentTab.goToAddressOrSearchString(urlOrSearchString);
        }else{
            addNewTabAtPosition(0, urlOrSearchString, true, tabAdapter.getNewTabId(), TAB_OPENED_BY_SPEED_DIAL_OR_SHOULD_RETURN_THERE, true, false);
        }
        hideSpeedDial(false);
        ItemRepository.recordTypedAddressAsFavouriteClick(urlOrSearchString, this);
    }

    public static final String TAB_OPENED_BY_EXTERNAL_APP = "externalApp";
    public static final String TAB_OPENED_BY_SPEED_DIAL_OR_SHOULD_RETURN_THERE = "speedDial";
    public static final String TAB_OPENED_BY_OTHER_TAB = "otherTab";
    public static final String TAB_OPENED_BY_BACK_CLOSING_EXISTING_TAB = "backClosingExistingTab"; // e.g. a placeholder tab is created so next address is opened in right spot and doesn't affect other tabs
    /**
     *
     */
    public int openAddressOrSearchInNewTab(String pageAddress, boolean switchToNewTab, String tabOpenedBy, boolean smoothScroll){
        tabAdapter.removeUnusedTabs();
        final int insertIndex = getSelectedTabPosition(false) + 1;
        final int newTabId = tabAdapter.getNewTabId();
        addNewTabAtPosition(insertIndex, pageAddress, switchToNewTab, newTabId,
                tabOpenedBy, smoothScroll, false);
        ItemRepository.recordTypedAddressAsFavouriteClick(pageAddress, this);
        return newTabId;
    }

    /**
     * Intercept phone back button press so we can hide the speed dial when it's opened
     */
    @Override
    public void onBackPressed() {
        Log.d("FPBACKBTN", "------------------------------------");
        Log.d("FPBACKBTN", "Initial tab state: "+tabAdapter.toString()+"\n selected position: "+getSelectedTabPosition(false));
        tabLayoutManager.clearSwipeTrackingForTabSwipeLock();
        addressInput.clearFocus(); // Whenever back is pressed, we want to ensure the keyboard is closed
        if(isSpeedDialVisible()){
            Log.d("FPBACKBTN", "Back pressed looking at speed dial (or invisible/empty tab)");
            handleBackPressOnSpeedDial();
        } else {
            Log.d("FPBACKBTN", "Back pressed looking at webview");
            handleBackPressOnWebView();
        }
        updateSpeedDialBackButtonVisibility();
        addressInput.updateAddressBarIcons();
        Log.d("FPBACKBTN", "Final tab state: "+tabAdapter.toString()+"\n selected position: "+getSelectedTabPosition(false));
        Log.d("FPBACKBTN", "------------------------------------");
    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (videoControls.isScreenLocked() && keyCode == KeyEvent.KEYCODE_BACK)
        {
            return true; doesn't actually prevent onHideCustomView from being called
        }
        return super.onKeyDown(keyCode, event);
    }*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Catch media button events on old phones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d("TTSMEDIAPLAYER", "onKeyDown MEDIA BUTTON!!! (LOLLIPOP+)"); // TODO TTS
            return super.onKeyDown(keyCode, event);
        }
        switch (keyCode) {
            default:
                Log.d("TTSMEDIAPLAYER", "onKeyDown MEDIA BUTTON!!! (PRE LOLLIPOP)"); // TODO TTS
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            showSpeedDial(false, 0, false, true, true, "back long press", false);
            speedDialListAdapter.scrollToViewList(SpeedDialListViewHolder.HISTORY_LIST, false);
            speedDialDomainFilter = null;
            addressInput.setText("");
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    private void handleBackPressOnWebView() {
        // Back button has been pressed whilst looking at a webview...
        TabViewHolder currentTab = getCurrentTabViewHolder();
        if(currentTab==null){ // shouldn't happen
            minimiseBrowser();
            return;
        }
        currentTab.getWebView().goBack();
    }

    private void handleBackPressOnSpeedDial() {
        String currentTabAddress="";
        final int selectedTabPosition = getSelectedTabPosition(false);
        try {
            currentTabAddress = tabAdapter.getAddressAtPosition(selectedTabPosition); // because the view may not be bound yet, get the address from the adapter state
            if(currentTabAddress==null){
                currentTabAddress = "";
            }
        }catch(IndexOutOfBoundsException e){
        }

        // A user clicked "..." on a speed dial list, and now they want to go back to the starting list
        if(isViewingExpandedSpeedDialListOfType != 0){
            AbstractItemAdapter itemAdapter = speedDialListAdapter.getListItemAdapterByPosition(speedDialListAdapter.getCurrentPosition());
            //speedDialListAdapter.linearLayoutManager.scrollToPosition(0);
            filterFavesByTagIds.clear();
            speedDialListAdapter.reloadCurrentList(0, new ArrayList<Integer>(), null);
            addressInput.setText(currentTabAddress);
            // Failed attempts to fix the rendering bug when you press back after scrolling down the image list
            //speedDialListAdapter.scrollToViewList(speedDialListAdapter.getCurrentPosition()+1, true);
            // speedDialListRecyclerView.invalidate();
            //speedDialListAdapter.notifyDataSetChanged();
            return;
        }

        if(backBtnShouldReturnToTabs){
            if(currentTabAddress.equals("")){
                hideSpeedDial(true);
                Log.d("FPBACKBTN", "Back should abort adding a new tab and smooth scroll back to previous");
                // We don't want to go back to an unused tab that was probably created as a result of
                // pressing the new tab button so make the tabs visible and animate back to the previous
                // one. The TabLinearLayoutManager will remove the unused tab on settle
                //getCurrentTabViewHolder().hide();
                tabRecyclerView.deletePositionOnSettle = selectedTabPosition;
                tabRecyclerView.smoothScrollToPosition(selectedTabPosition - 1);
            }else {
                Log.d("FPBACKBTN", "Back should return to existing tab, returning");
                // Address hint or a speed dial shortcut has been clicked. Pressing back should take them back to their tab
                hideSpeedDial(false);
            }
        }else{
            Log.d("FPBACKBTN", "Minimise browser");

            // No tabs exist or the user just pressed back to close a tab and return to speed dial
            // so presumably now they've pressed back again they want to go back to desktop/previous app
            minimiseBrowser();
            speedDialForwardBtn.setVisibility(GONE);
            //tabAdapter.removeUnusedTabs(); this might not work with code below
            if(selectedTabPosition > -1){
                // If they've pressed back to close the current tab, then pressed back again to minimise app,
                // let's assume they are done with that tab and remove it and replace it with a fresh placeholder tab
                //removeTabAtPosition(selectedTabPosition, false, false);
                //addNewTabAtPosition(selectedTabPosition, null, false, tabAdapter.getNewTabId(), TAB_OPENED_BY_BACK_CLOSING_EXISTING_TAB, true);
                this.backBtnShouldReturnToTabs = false;
            }
            return;
        }
/*
        if(tabAdapter.getItemCount()==0 || currentTabAddress.equals("")){
            // No tabs exist or the user just pressed back to close a tab and return to speed dial
            // so presumably now they've pressed back again they want to go back to desktop/previous app
            moveTaskToBack(true);
            return;

        }else if(tabAdapter.getItemCount() > 0){
            // Address hint or a speed dial shortcut has been clicked. Pressing back should take them back to their tab
            hideSpeedDial(true);
        }*/
    }

    public void minimiseBrowser() {
        moveTaskToBack(true);
    }

    public boolean shouldLoadPageInRestrictedMode(String url){
        return (safeMode && !ItemRepository.isTrustedAddress(url, db));
    }

    public boolean isSpeedDialVisible(){
        return (speedDialContainer!=null && speedDialContainer.getVisibility()==View.VISIBLE);
    }

    public void showSpeedDial(boolean fadeIn, long fadeInDelayInMs, boolean showKeyboard, boolean backBtnShouldReturnToTabs
            , boolean hideTabsOverview, String triggeredBy, boolean transparentBackground){
        Log.d("FPBACKBTN", "Showing speed dial ("+triggeredBy+"). back should return to tabs:"+(backBtnShouldReturnToTabs ? 1 : 0));
        hideTabSwipeLockBtn(true);
        if(showKeyboard) {
            tabRecyclerView.setVisibility(GONE); // if we are opening the keyboard it's expensive to leave the tab recycler view open in back as it has to recalculate layout
            addressInput.focusAddressInputAndShowKeyboard();
            scaleSpeedDialListHeaderIcons(true);
        }else{
            scaleSpeedDialListHeaderIcons(false);
        }
        /*if(transparentBackground){ had to disable this as it's expensive to recalculate layouts due to the keyboard opening
            might be able to achieve same effect with screenshot set to window.background (this way so it doesn't have to recalc) https://stackoverflow.com/questions/4287473/software-keyboard-resizes-background-image-on-android
            findViewById(R.id.speed_dial_list_header_container).setBackgroundColor(0xF1000000 + ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.speedDialBtnBgColour, this));
            findViewById(R.id.speed_dial_horizontal_list_recycler_view).setBackgroundColor(0xF1000000 + ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.speedDialBtnBgColour, this));
        }else{
            findViewById(R.id.speed_dial_list_header_container).setBackgroundColor(0xFF000000 + ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.speedDialBtnBgColour, this));
            findViewById(R.id.speed_dial_horizontal_list_recycler_view).setBackgroundColor(0xFF000000 + ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.speedDialBtnBgColour, this));
        }*/
        if(fadeIn) {
            _fadeSpeedDial(true, fadeInDelayInMs);
        }else{
            speedDialContainer.setAlpha(1f);
        }
        minimisingKeyboardShouldHideSpeedDial = showKeyboard;
        setSpeedDialContainerVisibilityInternal(View.VISIBLE, "showSpeedDialMethod");
        this.backBtnShouldReturnToTabs = backBtnShouldReturnToTabs;
        updateSpeedDialBackButtonVisibility();
        //canBrowserMenuButtonsBePressed = true; WAS CAUSING KEYBOARD TO OCCASIONALLY BE SHOWN WHEN QUICKLY CLOSING TABS AFTER FIRST LOAD, MAYBE TRY LOWER
        // This is an attempt to fix a glitch with the recycler view that doesn't restore it's last position properly when being recycled
        int listPosition = preferences.getInt(Browser.preference_lastUsedSpeedDialListPosition, SpeedDialListViewHolder.FAVES_LIST);
        speedDialListAdapter.scrollToViewList(listPosition, false);
        previewWindow.hide(false);

        // end attempt
        //if(hideTabs) {
            BrowsingHistoryRepository.endRecording(this, "show speed dial");
        //}
        addressInput.updateAddressBarIcons();
    }

    private boolean isShowingSmallSpeedDialHeaderIcons = false;
    private void scaleSpeedDialListHeaderIcons(boolean small) {
        Log.d("SCALEFP", "Requesting scale "+(small?"SMALL":"NORMAL"));
        if(small!=isShowingSmallSpeedDialHeaderIcons){
            isShowingSmallSpeedDialHeaderIcons = small;
            final int newWidthHeightInPx = UnitHelper.convertDpToPx(small ? 32 : 42, this);
            Log.d("SCALEFP", "CHANGING TO "+(small?"SMALL":"NORMAL")+" "+newWidthHeightInPx);
            ThemeHelper.setWidthAndHeightOfImageView(faveListBtn, newWidthHeightInPx, newWidthHeightInPx);
            ThemeHelper.setWidthAndHeightOfImageView(whatsHotListBtn, newWidthHeightInPx, newWidthHeightInPx);
            ThemeHelper.setWidthAndHeightOfImageView(readLaterListBtn, newWidthHeightInPx, newWidthHeightInPx);
            ThemeHelper.setWidthAndHeightOfImageView(historyListBtn, newWidthHeightInPx, newWidthHeightInPx);
            // Update speed dial toolbar wrapper
            final View headerWrapper = findViewById(R.id.speed_dial_list_header_container);
            if(small) {
                headerWrapper.setPadding(0, UnitHelper.convertDpToPx(10, this), 0, UnitHelper.convertDpToPx(3, this));
            }else{
                headerWrapper.setPadding(0, UnitHelper.convertDpToPx(25, this), 0, UnitHelper.convertDpToPx(15, this));
            }
        }
    }

    private void _fadeSpeedDial(final boolean fadeIn, final long delayBeforeStart) {
        final long animDuration = 250; // LONG not int!!
        if(animatingSpeedDialFade) {
            return; // let existing anim finish
        }
        if(fadeIn){
            speedDialContainer.setAlpha(0f);
        }else{
            speedDialContainer.setAlpha(1f);
        }
        animatingSpeedDialFade = true;
        final long totalDuration = animDuration + delayBeforeStart;
        CountDownTimer timer = new CountDownTimer(totalDuration, 1) {
            @Override
            public void onTick(long step) {
                Log.d("SDANIM", ""+(step));
                if(step < animDuration) {
                    float stepFraction = ((float) step / (float) animDuration);
                    float alpha;
                    alpha = stepFraction;
                    if(fadeIn) {
                        Log.d("SDANIM", ""+(1f - alpha));
                        speedDialContainer.setAlpha(1f - alpha);
                    }else{
                        speedDialContainer.setAlpha(alpha);
                    }
                }
            }
            @Override
            public void onFinish() {
                speedDialContainer.setAlpha(fadeIn ? 1f : 0f);
                if(!fadeIn){
                    setSpeedDialContainerVisibilityInternal(GONE, "end of fade out()");
                }
                animatingSpeedDialFade = false;
            }
        };
        timer.start();
    }

    public void updateSpeedDialBackButtonVisibility() {
        //if(isViewingExpandedSpeedDialListOfType != 0 || (tabAdapter.getItemCountOfUsedTabs()>0)){
            speedDialBackBtn.setVisibility(View.VISIBLE);
        //}else{
        //    speedDialBackBtn.setVisibility(View.GONE);
        //}
        if(speedDialDomainFilter==null || speedDialDomainFilter.equals("")){
            hideSiteHomePageBtn();
        }
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     *      * you started it with, the resultCode it returned, and any additional
     *      * data from it.
     *      TODO this could be used for catching setting changes and edit item changes by the sounds of it
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        TabViewHolder currentTab = getCurrentTabViewHolder();
//        if(requestCode==EditItem.RETURN_FROM_EDIT_ITEM_CODE) {
//            speedDialListAdapter.reloadCurrentList(0, new ArrayList<Integer>(), null);
//        }else if(requestCode==RECORD_VOICE_INTENT_RESULT){
//            // Catching voice input
//            if(intent!=null) {
//                ArrayList<String> result = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                if (result!=null && result.size() > 0) {
//                    openAddressOrSearch(result.get(0).toString(), true);
//                }
//            }
//        }else if(requestCode==BrowserSettingsActivity.SETTINGS_CHANGED_RETURN_KEY){
//            // Rebuild activity if the user has returned from the browser settings
//            // note: if making this conditional, the phone back button triggers onBackPressed but not the title bar back button!
//            recreate();
//        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // Android 5+
//            if (requestCode == CustomWebChromeClient.REQUEST_SELECT_FILE) {
//                if (currentTab==null || currentTab.getWebChromeClient().uploadMessage == null) {
//                    return;
//                }
//                currentTab.getWebChromeClient().uploadMessage.onReceiveValue(CustomWebChromeClient.FileChooserParams.parseResult(resultCode, intent));
//                currentTab.getWebChromeClient().uploadMessage = null;
//            }
//        } else if (requestCode == CustomWebChromeClient.FILECHOOSER_RESULTCODE) {
//            if (currentTab==null || currentTab.getWebChromeClient().mUploadMessage == null) {
//                return;
//            }
//            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
//            // Use RESULT_OK only if you're implementing WebView inside an Activity
//            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
//            currentTab.getWebChromeClient().mUploadMessage.onReceiveValue(result);
//            currentTab.getWebChromeClient().mUploadMessage = null;
//        } else {
//            Toast.makeText(this, R.string.failed_to_upload_file, Toast.LENGTH_LONG).show();
//        }
    }

    public void hideSpeedDial(boolean fadeOut){
        speedDialForwardBtn.setVisibility(GONE);
        Log.d("FPBACKBTN", "Hiding speed dial.");
        TabViewHolder tab = getCurrentTabViewHolder();
        if(tab!=null){
            Log.d("FPBACKBTN", "Showing current tab and removing unused:");
            backBtnShouldReturnToTabs = false;
            BrowsingHistoryRepository.startRecordingHistoryItem(this, tab, this.db, null, null, "show-tabs");
            if(previewWindow.pinned){
                previewWindow.show();
            }
        }
        tabRecyclerView.setVisibility(View.VISIBLE);
        if(fadeOut){
            _fadeSpeedDial(false, 0);
        } else {
            setSpeedDialContainerVisibilityInternal(GONE, "hideSpeedDial()");
        }
        updateSystemNavBarAndTimeBarVisibility();
    }

    public void setSpeedDialContainerVisibilityInternal(int visibility, String debugSrc){
        Log.d("speeddialvis", (visibility==View.VISIBLE ? "[SHOW] " : "[HIDE] ") +debugSrc);
        speedDialContainer.setVisibility(visibility);
    }

    @NonNull
    public String getClipboardContentsAsString() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipboard!=null) {
            ClipData clipData = clipboard.getPrimaryClip();
            if (clipData != null) {
                ClipData.Item item1 = clipData.getItemAt(0);
                //notificationHint.setText("Pasted "+item1.getText().toString());
                if (item1 != null && item1.getText()!=null) {
                    return item1.getText().toString();
                }
            }
        }
        return "";
    }


    public void hideBrowserForFullScreenVideo(){
        isVideoFullScreened = true;
        previewWindow.hide(false);
    }

    public void showBrowserAfterExitingFullScreenVideo(){
        isVideoFullScreened = false;
        if(previewWindow.pinned){
            previewWindow.show();
        }
    }

    // When someone clicks on a heading on the speed dial
    protected View.OnClickListener onSpeedDialListHeadingClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            bounceButton(v);
            isViewingExpandedSpeedDialListOfType = 0;
            filterFavesByTagIds.clear();
            //final String searchFilter = addressInput.getText().toString();
            int position = v.getId();
            if(position == R.id.speed_dial_faves_btn){
                speedDialListAdapter.scrollToViewList(SpeedDialListViewHolder.FAVES_LIST, true);
                if(speedDialListAdapter.getCurrentPosition()==SpeedDialListViewHolder.FAVES_LIST) {
                    speedDialListAdapter.reloadCurrentList(0, new ArrayList<Integer>(), null); // if the expanded list is being viewed and they pressed the same list heading take them back to the list home
                }
            }else if(position == R.id.speed_dial_history_btn) {
                speedDialListAdapter.scrollToViewList(SpeedDialListViewHolder.HISTORY_LIST, true);
                if(speedDialListAdapter.getCurrentPosition()==SpeedDialListViewHolder.HISTORY_LIST) {
                    speedDialListAdapter.reloadCurrentList(0, new ArrayList<Integer>(), null); // if the expanded list is being viewed and they pressed the same list heading take them back to the list home
                }
            }else if(position == R.id.speed_dial_whats_hot_btn) {
                speedDialListAdapter.scrollToViewList(SpeedDialListViewHolder.WHATS_HOT_LIST, true);
                if(speedDialListAdapter.getCurrentPosition()==SpeedDialListViewHolder.WHATS_HOT_LIST) {
                    speedDialListAdapter.reloadCurrentList(0, new ArrayList<Integer>(), null); // if the expanded list is being viewed and they pressed the same list heading take them back to the list home
                }
            }else if(position == R.id.speed_dial_read_later_list_btn) {
                speedDialListAdapter.scrollToViewList(SpeedDialListViewHolder.SAVED_FOR_LATER_LIST, true);
                if(speedDialListAdapter.getCurrentPosition()==SpeedDialListViewHolder.SAVED_FOR_LATER_LIST) {
                    speedDialListAdapter.reloadCurrentList(0, new ArrayList<Integer>(), null); // if the expanded list is being viewed and they pressed the same list heading take them back to the list home
                }
            }

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

//    protected View.OnClickListener onViewClosedTabsButtonClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            //bounceButton(v);
//            tabOverviewThumbs.show(0, true);
//        }
//    };

    // When someone clicks on a tag on the speed dial
    protected View.OnClickListener onClearHistoryButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //bounceButton(v);
            if(speedDialListAdapter.getCurrentPosition()==SpeedDialListViewHolder.HISTORY_LIST && speedDialDomainFilter!=null && !speedDialDomainFilter.equals("")){
                promptUserIfTheyWantToClearTheCurrentlyViewedDomain(speedDialDomainFilter);
            } else {
                promptUserHowMuchHistoryToClear(false);
            }
        }
    };

    // When someone clicks on a tag on the speed dial
    protected View.OnClickListener onSiteHomePageButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //bounceButton(v);
            if(speedDialListAdapter.getCurrentPosition()==SpeedDialListViewHolder.HISTORY_LIST && speedDialDomainFilter!=null && !speedDialDomainFilter.equals("")){
                openAddressOrSearch(speedDialDomainFilter, true);
            }
        }
    };

    private void promptUserIfTheyWantToClearTheCurrentlyViewedDomain(final String url){
        final String domainBeingViewed = UrlHelper.getDomain(url, true, true, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder
                //.setTitle(getString(R.string.clear_))
                .setMessage(String.format(getString(R.string.remove_DOMAIN_from_browsing_history), domainBeingViewed))
                .setNegativeButton(R.string.dialog_yes,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        BrowsingHistoryRepository.clearDomainHistory(domainBeingViewed, Browser.this, recentlyClosedTabAdapter);
                    }
                })
                .setNeutralButton(R.string.more_options,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        promptUserHowMuchHistoryToClear(true);
                    }
                })
                .setPositiveButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                }).create();
        ThemeHelper.styleAlertDialog(dialog, null, this);
        dialog.show();
    }

    private void promptUserHowMuchHistoryToClear(boolean emphasisOnClearingAll) {
        final String textDeleteAll = getString(R.string.clear_all_history_cookies_and_datax);
        final String textLast10Mins = getString(R.string.last_10_mins);
        final String textLast30Mins = getString(R.string.last_30_mins);
        final String textLastHour = getString(R.string.last_hour);
        final String textLast3Hours = getString(R.string.last_3_hours);
        final String textLast12Hours = getString(R.string.last_12_hours);
        final String textClearSpecificDomain = getString(R.string.on_specific_website);
        final String textClearSpecificDate = getString(R.string.on_specific_date);
        final CharSequence[] optionList;
        if(emphasisOnClearingAll){
            // They've ended up here after saying they don't want to clear history for a specific domain so we should only give them option to delete all
            optionList = new CharSequence[]{
                    textLast10Mins,
                    textLast30Mins,
                    textLastHour,
                    textLast3Hours,
                    textDeleteAll,
            };
        }else {
            optionList = new CharSequence[]{
                    textLast10Mins,
                    textLast30Mins,
                    textLastHour,
                    textLast3Hours,
                    textClearSpecificDate,
                    textClearSpecificDomain,
                    textDeleteAll,
            };
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(Browser.this);
        builder
                .setTitle(getString(emphasisOnClearingAll ? R.string.clear_ALL_browsing_history : R.string.clear_browsing_history))
                .setSingleChoiceItems(optionList, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        lastClosedTabAddress = null;
                        boolean showDeletedNotification = false;
                        if(optionList[item].equals(textLast10Mins)) {
                            BrowsingHistoryRepository.clearRecentHistory(0.166f, Browser.this, recentlyClosedTabAdapter);
                            speedDialListAdapter.reloadCurrentList(isViewingExpandedSpeedDialListOfType, new ArrayList<Integer>(), null);
                            addressInput.setAddressInputTextWithoutChangeEvent("");
                            showDeletedNotification = true;
                        }else if(optionList[item].equals(textLast30Mins)) {
                            BrowsingHistoryRepository.clearRecentHistory(0.5f, Browser.this, recentlyClosedTabAdapter);
                            speedDialListAdapter.reloadCurrentList(isViewingExpandedSpeedDialListOfType, new ArrayList<Integer>(), null);
                            addressInput.setAddressInputTextWithoutChangeEvent("");
                            showDeletedNotification = true;
                        }else if(optionList[item].equals(textLastHour)) {
                            BrowsingHistoryRepository.clearRecentHistory(1, Browser.this, recentlyClosedTabAdapter);
                            speedDialListAdapter.reloadCurrentList(isViewingExpandedSpeedDialListOfType, new ArrayList<Integer>(), null);
                            addressInput.setAddressInputTextWithoutChangeEvent("");
                            showDeletedNotification = true;
                        }else if(optionList[item].equals(textLast3Hours)) {
                            BrowsingHistoryRepository.clearRecentHistory(3, Browser.this, recentlyClosedTabAdapter);
                            speedDialListAdapter.reloadCurrentList(isViewingExpandedSpeedDialListOfType, new ArrayList<Integer>(), null);
                            addressInput.setAddressInputTextWithoutChangeEvent("");
                            showDeletedNotification = true;
                        }else if(optionList[item].equals(textLast12Hours)) {
                            BrowsingHistoryRepository.clearRecentHistory(12, Browser.this, recentlyClosedTabAdapter);
                            speedDialListAdapter.reloadCurrentList(isViewingExpandedSpeedDialListOfType, new ArrayList<Integer>(), null);
                            addressInput.setAddressInputTextWithoutChangeEvent("");
                            showDeletedNotification = true;
                        }else if(optionList[item].equals(textDeleteAll)){
                            BrowsingHistoryRepository.clearAllBrowsingData(Browser.this);
                            addressInput.setAddressInputTextWithoutChangeEvent("");
                            speedDialListAdapter.reloadCurrentList(0, new ArrayList<Integer>(), null);
                            showDeletedNotification = true;
                        }else if(optionList[item].equals(textClearSpecificDate)) {
                            showLongNotificationHint(getString(R.string.long_press_the_date_you_wish_to_clear), Gravity.CENTER);
                        }else if(optionList[item].equals(textClearSpecificDomain)) {
                            showLongNotificationHint(getString(R.string.long_press_an_item_from_the_website_you_wish_to_clear), Gravity.CENTER);
                        }
                        if(showDeletedNotification) {
                            BrowsingHistoryRepository.deferredHistoryUrl = null;
                            BrowsingHistoryRepository.deferredHistoryDomain = null;
                            BrowsingHistoryRepository.deferredHistoryTimeStamp = null;
                            BrowsingHistoryRepository.deferredHistoryTabId = null;
                            showNotificationHint(getString(R.string.deleted), Gravity.CENTER);
                        }
                        dialog.dismiss();// dismiss the alertbox after chose option
                    }
                }).create()
                .show();
    }

    protected View.OnClickListener onSpeedDialBackButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    public String getCurrentTabAddressIfExists(){
        final int selectedTabPosition = getSelectedTabPosition(false);
        if(selectedTabPosition > -1){
            try {
                final String addressAtPosition = tabAdapter.getAddressAtPosition(selectedTabPosition);
                return addressAtPosition != null && !addressAtPosition.equals("") ? addressAtPosition : null;
            }catch(IndexOutOfBoundsException e){
                return null;
            }
        }
        return null;
    }

    protected View.OnClickListener onSpeedDialForwardButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (lastClosedTabAddress!=null && lastClosedTabAddress.length() > 0 && getCurrentTabAddressIfExists()==null) {
                // restore the "closed" tab
                final boolean restoreStateFromStorage = false; // even though we are restoring the tab state, we don't need to request it from storage as we just closed it so it should automatically get restored from androids RV handling
                addNewTabAtPosition(lastClosedTabPosition, lastClosedTabAddress, true,
                        lastClosedTabId, Browser.TAB_OPENED_BY_SPEED_DIAL_OR_SHOULD_RETURN_THERE, false, restoreStateFromStorage);
                tabAdapter.removeUnusedTabs();
                lastClosedTabAddress = null;
                lastClosedTabPosition = 0;
                if (isSpeedDialVisible()) {
                    hideSpeedDial(false);
                }
            }
        }
    };

    public void startLoadingAnimation(){
        final View viewById = findViewById(R.id.loading_graphic);
        viewById.setVisibility(View.VISIBLE);
        AnimationDrawable anim = (AnimationDrawable) viewById.getBackground();
        anim.start();
    }

    public void stopLoadingAnimation(){
        final View viewById = findViewById(R.id.loading_graphic);
        AnimationDrawable anim = (AnimationDrawable) viewById.getBackground();
        if(anim.isRunning()) {
            anim.stop();
        }
        viewById.setVisibility(GONE);
    }

    protected View.OnClickListener onSpeedDialSettingsButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openSettingsActivity();
        }
    };

    public void openSettingsActivity() {
    }


    public void showNotificationHint(String text, Integer gravity) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        if(gravity!=null) {
            toast.setGravity(gravity, 0, 0);
        }
        toast.show();
    }
    public void showLongNotificationHint(String text, Integer gravity) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        if(gravity!=null) {
            toast.setGravity(gravity, 0, 0);
        }
        toast.show();
    }

    public void lockPhone(){
        //Lock device - doesn't work without admin permissions
//        DevicePolicyManager deviceManger = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
//        boolean active = deviceManger.isAdminActive(new ComponentName(this, Browser.class));
//        if (active) {
//            deviceManger.lockNow();
//        }
    }

    public static void clearIntent(Intent intent) {
        intent.replaceExtras(new Bundle());
        intent.setAction("");
        intent.setData(null);
        intent.setFlags(0);
    }

    /**
     * Important so new intents can be sent when resuming a paused activity i.e. bringing the browser back to the front
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    /**
     * @see #onResume
     * @see #onSaveInstanceState
     * @see #onStop
     */
    @Override
    protected void onPause() {
        BrowsingHistoryRepository.flushDeferredHistorySaving(null, null, this); // if saving address has been deferred to wait for page title we must ensure we flush any deferred items to db
        if(BrowsingHistoryRepository.currentHistory!=null) {
            BrowsingHistoryRepository.endRecording(this, "pause");
        }
        tabAdapter.clearStateThatAnyTabsWereOpenedExternally();
        recentlyClosedTabAdapter.removeOldItems();
        tabAdapter.saveFullTabWebViewStatesToStore();
        super.onPause();
    }

    public boolean isViewVisible(final View view) {
        if (view == null) {
            return false;
        }
        if (!view.isShown()) {
            return false;
        }
        final Rect viewPosition = new Rect();
        view.getGlobalVisibleRect(viewPosition);
        final Rect screen = new Rect(0, 0, getScreenWidth(), getScreenHeight());
        return view.getVisibility()==View.VISIBLE && screen.contains(viewPosition);
    }

    public int getScreenWidth(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    /**
     * Get screen height in pixels
     * @return
     */
    public int getScreenHeight(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public int getActionBarHeight() {
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        TypedArray a = obtainStyledAttributes(new TypedValue().data,  textSizeAttr);
        int height = a.getDimensionPixelSize(0, 0);
        a.recycle();
        return height;
    }

    public int contextCutActionId = 0;
    public int contextCopyActionId = 0;
    public int contextPasteActionId = 0;
    public int contextSelectAllActionId = 0;
    public int contextShareActionId = 0;
    public int contextWebSearchActionId = 0;
    public int contextAutoFillActionId = 0;
    public Menu systemSelectionMenu;

//    /**
//     * Triggered when text is selected in the webbview. (search for HitTestResult for when a link or image are selected)
//     * @param mode
//     */
//    @Override
//    public void onActionModeStarted(final ActionMode mode) {
//        Log.d("CTXMENU", "Action mode started");
//        super.onActionModeStarted(mode);
//        final TabViewHolder selectedBrowserTab = getCurrentTabViewHolder();
//        if(selectedBrowserTab==null) {
//            return;
//        }
//        if(selectedBrowserTab.sitePreferencesDialog.isOpen()){
//            return; // the js and css inputs need the context menu
//        }
//        if(isSpeedDialVisible()){
//            return; // allow users to paste into address bar using the normal context menu
//        }
//        WebSiteHelper.removeElementHighlights(selectedBrowserTab.webView); // Ensure any image selections are unselected
//
//        //Menu menus = mode.getMenu();
//        // WE HAVE TO HIDE BUT ALSO KEEP REFERENCE TO THE ORIGINAL SELECTION CONTEXT MENU ITEMS SO WE CAN MANUALLY
//        // TRIGGER THEM LATER AS YOU CANNOT RELIABLY DO STUFF LIKE CUT/COPY/SHARE IN JS DUE TO IFRAME SECURITY
//        clearSelectionCache();
//        systemSelectionMenu = mode.getMenu();
//        hideSystemContextMenuAndTrackBtns();
//
//        final boolean isContentEditable = contextPasteActionId > 0;
//        if(isContentEditable){
//            //selectedBrowserTab.dontScrollNavBar = true;
//            // ^ Bit of a hack due to opening/closing keyboard causing a lot of scrolling
//            // which can cause the nav bar to animate out of view, I tried stopping it from animating in
//            // the onWebViewScrolled method but it turned into a glitch fest because closing
//            // the keyboard causes a lot of scrolling. Simplest solution is just to disable the
//            // nav bar scrolling until tabs are next swiped (onActionModeFinished doesn't work either)
//        }
//
//        //ActionProvider foo = copyItem.getActionProvider(); null
//        //Intent intent = copyItem.getIntent();null
//        //item.setOnMenuItemClickListener
//
//        // Hide the main part of the nav bar
//        //selectedBrowserTab.constrainWebViewBottomToNavBar();
//        //Log.d("NAVBARANIM", "SETTING VISIBLE AS ACTION MODE STARTED");
//        //selectedBrowserTab.setNavBarTranslationYAsFraction(TabViewHolder.NAV_BAR_SCROLL_FRACTION_VISIBLE);
//
//        selectedBrowserTab.webView.evaluateJavascript("(function(){" +
//                "var fpContentEditable = 0;" +
//                "var isIFrame=0;" +
//                "if(document.activeElement){" +
//                    "var el = document.activeElement;" +
//                    "var nodeName = el.nodeName.toLowerCase();\n" +
//                    "if (el.nodeType == 1 && (el.isContentEditable || nodeName == 'textarea' ||\n" +
//                    "    (nodeName == 'input' && /^(?:text|email|number|search|tel|url|password)$/i.test(el.type)))) {\n" +
//                    "    fpContentEditable = 1;" +
//                    "}" +
//                    "if(window.self !== window.top){isIframe=1;}" + // not sure isIframe works
//                "}" +
//                "return window.getSelection().type+'_'+fpContentEditable+'_'+isIFrame+'_'+window.getSelection().toString();" +
//            "})()",
//            new ValueCallback<String>(){
//                @Override
//                public void onReceiveValue(String selectedTypeAndText){
//                    //noinspection ConstantConditions
//                    if(selectedTypeAndText!=null && selectedTypeAndText.length() >= 0) {
//                        selectedTypeAndText = selectedTypeAndText.substring(1, selectedTypeAndText.length()-1); // remove quotes enclosing selection
//                        String[] parts = selectedTypeAndText.split("_", 4);
//                        if(parts.length == 4) {
//                            String selectedString = parts[3];
//                            selectedBrowserTab.webViewJavascriptHooks.setSelectedItem(parts[0], selectedString, null);
//                            selectedBrowserTab.selectionContextMenu.showContextMenu("new", selectedBrowserTab);
//
//                            //final boolean isIFrame = parts[2].equals("1"); // doesn't work
//                            //mode.getMenu().clear(); // hide the original/system context menu
//
//                            // Probably selected range
//                            /*final String selectionType = parts[0].toLowerCase();
//                            if (selectionType.equalsIgnoreCase(SelectionContextMenu.CONTEXT_NONE)) {
//                                selectedBrowserTab.selectionContextMenu.hideContextMenu();
//                            }
//                            if (!previewWindow.isOpen) {
//                                selectedBrowserTab.selectionContextMenu.showContextMenu(selectionType, selectedBrowserTab, isContentEditable);
//                            }*/
//                        }
//                    }
//                }
//            }
//        );
//        // this isn't instant so handle button visibility on event @see WebViewJavascriptHooks
//
//    }

    public void hideSystemContextMenuAndTrackBtns() {
        if(systemSelectionMenu==null){
            return;
        }
        for(int n=0;n < systemSelectionMenu.size();n++){
            MenuItem item = systemSelectionMenu.getItem(n);
            item.setVisible(false); // Hide menu item from system context menu
            //item.getActionView().setVisibility(View.GONE); errors
            switch(item.getAlphabeticShortcut()){
                case 'c': // Copy
                    contextCopyActionId = item.getItemId();
                    break;
                case 'x': // Cut
                    contextCutActionId = item.getItemId();
                    break;
                case 'v': // Paste
                    contextPasteActionId = item.getItemId();
                    break;
                case 'a': // Select all
                    contextSelectAllActionId = item.getItemId();
                    break;
                // todo "AUTOFILL"?! - long press in a search box
                default: // Web search
                    if(item.getTitle().equals("Web search")) { // TODO won't work in other languages
                        contextWebSearchActionId = item.getItemId();
                    }else if(item.getTitle().equals("Share")) {
                        contextShareActionId = item.getItemId();
                    }else if(item.getTitle().equals("Autofill")) {
                        contextAutoFillActionId = item.getItemId();
                    }else{
                        //item.setVisible(true);
                        Log.d("FPCONTEXTMENU", "Unhandled context menu item: "+item.getItemId()+" = "+item.getTitle());
                    }
            }
        }
    }

    /**
     * When selecting stuff in the webview we can get the most information
     * from JS window.getSelection() but this won't work in iframes so we also
     * have to keep track of what the default system context menu would have shown.
     * This clears all these caches
     */
    public void clearSelectionCache() {
        contextCutActionId = 0;
        contextCopyActionId = 0;
        contextPasteActionId = 0;
        contextSelectAllActionId = 0;
        contextShareActionId = 0;
        contextWebSearchActionId = 0;
        systemSelectionMenu = null;
        final TabViewHolder selectedBrowserTab = getCurrentTabViewHolder();
    }

    /**
     * Called when you are no longer visible to the user.  You will next
     * receive either {@link #onRestart}, {@link #onDestroy}, or nothing,
     * depending on later user activity.
     *
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @see #onRestart
     * @see #onResume
     * @see #onSaveInstanceState
     * @see #onDestroy
     */
    @Override
    protected void onStop() {
        // NOTE: THIS CANNOT BE GUARANTEED TO BE CALLED, BUT WILL MOSTLY BE CALLED
        //flushCookiesCollectedInPrivateMode();
        BrowsingHistoryRepository.pruneHistory(5 * 24, DatabaseHelper.getInstance(getApplicationContext()).getWritableDatabase(), recentlyClosedTabAdapter);
        ArrayList<Integer> usedTabIds = new ArrayList<>();
        for(TabState state : tabAdapter.getTabStates()){
            usedTabIds.add(state.tabId);
        }
        for(TabState state : recentlyClosedTabAdapter.getClosedTabList()){
            usedTabIds.add(state.tabId);
        }
        //CustomWebView.clearUnusedWebViewStatePermanentStorage(this, usedTabIds);

        Log.d("FPOnBrowserClose", "onStop called");
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        TabViewHolder currentTab = getCurrentTabViewHolder();
        if(currentTab!=null && currentTab.webView!=null && !preferences.isPrivateBrowsingModeEnabled()){
            //currentTab.webView.saveState(outState);
            Log.d("FPSAVINGSTUFF", "Saving webviews...");
            for(TabState state : tabAdapter.getTabStates()){
                if(state.webView!=null){
                    Log.d("FPSAVINGSTUFF", "Saving tab id "+state.tabId);
                    Bundle bundle = new Bundle();
                    state.webView.saveState(bundle);
                    // TODO apparently this can get too large and cause TransactionTooLarge exception, saving it to a file is meant to circumvent that
                    outState.putBundle("tab_"+state.tabId, bundle);
                }
            }
            alreadyRestored.clear();
            Log.d("FPSAVINGSTUFF", "Saving done");
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * Close custom context/selection menu
     *
     * @param mode The action mode that just finished.
     */
//    @Override
//    public void onActionModeFinished(ActionMode mode) {
//        Log.d("CTXMENU", "Action mode finished");
//
//        TabViewHolder selectedBrowserTab = getCurrentTabViewHolder();
//        if(selectedBrowserTab!=null) {
//            clearSelectionCache();
//            selectedBrowserTab.pageContextMenu.hideContextMenu();
//            if(!previewWindow.isOpen) {
//                selectedBrowserTab.selectionContextMenu.hideContextMenu();
//            }
//        }
//        super.onActionModeFinished(mode);
//    }

    /*public void setCompactMode(boolean bool) {
        compactMode = bool;
        for(int i=0; i < tabAdapter.getItemCount(); i++) {
            TabViewHolder tab = (TabViewHolder) tabRecyclerView.findViewHolderForAdapterPosition(i);
            if(tab !=null){
                tab.setCompactMode(compactMode);
            }
        }
    }*/

    /**
     * @return
     */
    public int convertXScreenPercentageToPixels(float percentage){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return Math.round(size.x * (percentage / 100f));
    }

    /**
     * @param percentage e.g. 0 to 100
     * @return
     */
    public int convertYScreenPercentageToPixels(float percentage){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return Math.round(size.y * (percentage / 100f));
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm!=null) {
            try {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            }catch(SecurityException e){
                return true;
            }
        }
        return true;
    }

    public void updateCompactMode() {
        final boolean inMultiWindowMode;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            inMultiWindowMode = isInMultiWindowMode();
        }else{
            inMultiWindowMode = false;
        }
        // Note: checking screen width/height here won't work properly when rotating phone
        final boolean compactModeEnabledInSettings = preferences.isCompactModeEnabled();
        final boolean inLandscapeMode = isInLandscapeMode();
        boolean desiredCompactMode = false;
        if (previewWindow.isOpen || inMultiWindowMode || compactModeEnabledInSettings || inLandscapeMode) {
            desiredCompactMode = true;
        }
        setCompactMode(desiredCompactMode);
    }

    public boolean isInLandscapeMode() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public void setCompactMode(boolean desiredCompactMode) {
        if(this.isInCompactMode == desiredCompactMode){
            return;
        }
        Log.d("TESTX", "SETTING TO "+ desiredCompactMode);
        isInCompactMode = desiredCompactMode;
        TabViewHolder tab = getCurrentTabViewHolder();
        if(tab!=null) {
            tab.setCompactMode(desiredCompactMode, false);
        }
        // Update address bar wrapper height
        ViewGroup.LayoutParams wrapperParams = addressBarWrapper.getLayoutParams();
        wrapperParams.height = UnitHelper.getNavBarHeightInPx(desiredCompactMode, this);
        addressBarWrapper.setLayoutParams(wrapperParams);

        // Update speed dial toolbar wrapper
        ViewGroup.LayoutParams toolbarParams = speedDialToolbarWrapper.getLayoutParams();
        toolbarParams.height = UnitHelper.getSpeedDialToolBarHeightInPx(desiredCompactMode, this);
        speedDialToolbarWrapper.setLayoutParams(toolbarParams);

        // Update address bar margin
        ConstraintLayout.LayoutParams inputParams = (ConstraintLayout.LayoutParams) addressInput.getLayoutParams();
        final int marginsInPx = desiredCompactMode ? UnitHelper.convertDpToPx(7, this) : UnitHelper.convertDpToPx(10, this);
        inputParams.setMargins(marginsInPx, marginsInPx, marginsInPx, marginsInPx);
        addressInput.setLayoutParams(inputParams);
        // Update speed dial list headers
        //ViewGroup.LayoutParams faveBtnParams = faveListBtn.getLayoutParams();
        //faveListBtn.setMaxHeight(UnitHelper.convertDpToPx(16, this));
        //wrapperParams.height = UnitHelper.getNavBarHeightInPx(compactMode, this);
        //addressBarWrapper.setLayoutParams(wrapperParams);
    }

    /**
     * e.g. screen has been rotated
     * @param newConfig The new device configuration.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            setCompactMode(true);
        }else{
            updateCompactMode();
        }
        int position = preferences.getInt(Browser.preference_lastUsedSpeedDialListPosition, SpeedDialListViewHolder.FAVES_LIST);
        speedDialListAdapter.scrollToViewList(position, false); // fixes list glitch when rotating device
    }

    public boolean isVideoFullScreened(){
        return isVideoFullScreened;
    }

    // Hack to detect keyboard showing/hiding
    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if(isVideoFullScreened){
                Log.d("VIDVIDX", "FULLSCREENED;"); // this could be used to show
                return;
            }
            // IMPORTANT - I READ THIS METHOD MIGHT BE QUITE BAD FOR PERFORMANCE. TRY DISABLING IF NOTICING PROBLEMS
            // TODO THIS GETS CALLED A LOT. HAVEN'T FOUND A BETTER WAY TO CHECK FOR KEYBOARD USAGE SO NEED TO KEEP THIS PERFORMANT
            TabViewHolder tab = getCurrentTabViewHolder();
            if(tab!=null && tab.isAnimatingNavBar){
                return; // I tried profiling before and after and it does seem this method costs performance a bit so going to try disabling while animating
            }
            Rect r = new Rect();
            try {
                rootView.getWindowVisibleDisplayFrame(r);
            }catch(SecurityException e){
                // Another error that I can't reproduce but apparently happens on some devices occasionally (might be related to parcel writing)
                return;
            }
            int screenHeight = rootView.getRootView().getHeight();

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            int keypadHeight = screenHeight - r.bottom;
            final boolean keyboardWasOpen = isKeyboardOpen;
            isKeyboardOpen = keypadHeight > screenHeight * 0.2; // hacky way to determine if keyboard was opened, not super reliable
            final boolean keyboardOpenStateHasChanged = keyboardWasOpen != isKeyboardOpen;
            if(isKeyboardOpen && tab!=null && tab.hasScreenBeenPulledDown()){
                tab.removeEasyReachOffset(false);
            }
            if(!keyboardOpenStateHasChanged){
                return;
            }
            updateCompactMode();
            //Log.d(TAG, "keypadHeight = " + keypadHeight);
            if(tab!=null) {
                if (isKeyboardOpen) {
                    //tabBulletHintList.hide(true, "keyboard open");
                    // keyboard is opened
                    isKeyboardOpen = true;
                    //tab.constrainWebViewBottomToWindowBottom();
                    //Log.d("NAVBARANIM", "KEYBOARD OPEN, HIDING");
                    if(isSpeedDialVisible()){
                        addressInput.updateAddressBarIcons();
                    }else {
                        tab.setNavBarVisibilityWithoutAnimation(false);
                    }
                } else {
                    // keyboard is being closed
                    if(keyboardWasOpen) {
                        //Log.d("NAVBARANIM", "KEYBOARD CLOSED, SHOWING");
                        tab.setNavBarVisibilityWithoutAnimation(true);
                    }
                    updateSystemNavBarAndTimeBarVisibility();
                    if(isSpeedDialVisible() && minimisingKeyboardShouldHideSpeedDial && isViewingExpandedSpeedDialListOfType==0){
                        // speed dial has been opened to edit curren tab address.. assuming there is a page to fall back to
                        // minimising the keyboard should just close the speed dial at this point
                       /* final TabState currentTabState = tabAdapter.getTabStateAtPosition(getSelectedTabPosition(false));
                        if(currentTabState!=null && currentTabState.address!=null && !currentTabState.address.equals("")){
                            // if the keyboard is open and they minimise it
                            hideSpeedDial(false);
                        }*/
                        addressInput.updateAddressBarIcons();
                    }
                }
            }
        }
    };

    public void updateSystemNavBarAndTimeBarVisibility() {
        if(preferences.shouldHideSystemNavBar()) { // we only need to worry about showing/hiding these if the full screen mode is on. If it's not don't waste cpu triggering layout changes
            if (shouldHideSystemNavBarForCurrentView()) {
                hideSystemNavigationBar(getWindow(), true);
            } else {
                showSystemNavigationBar(getWindow());
            }
        }
    }

    private boolean shouldHideSystemNavBarForCurrentView() {
        return (preferences.shouldHideSystemNavBar() && !isSpeedDialVisible());
    }

    public void bounceButton(View button) {
        if(button==null){
            return;
        }
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.buttton_bounce);
        button.startAnimation(myAnim);
        // Use bounce interpolator with amplitude 0.2 and frequency 20
        ButtonBounceInterpolator interpolator = new ButtonBounceInterpolator(50, 10);
        myAnim.setInterpolator(interpolator);
        button.startAnimation(myAnim);
    }


    public void bounceButtonIfMakesSense(View button, String action){
        switch(action){
            case "newTab":
            case "viewFaves":
            case "showPageMenu":
            case "viewTabsOverview":
            case "viewWhatsHot":
            case "viewHistory":
            case "searchOrEnterAddress":
                break;

            default:
                bounceButton(button);
        }
    }

    /**
     * Called when user grants permission to download
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasGrantedPermissions = hasAllPermissionsGranted(grantResults);
        if(hasGrantedPermissions && requestCode== FileHelper.STORAGE_PERMISSIONS_REQUEST_CODE){
            if(pendingDownload!=null) {
                FileHelper.downloadFile(pendingDownload, this);
            }else{
                // We don't have a pending download which might mean we had to ask the page for the mime type (hack), so just ask them to start again
                showLongNotificationHint(getString(R.string.permission_granted_you_can_now_start_download), null);
            }
        }else if(!hasGrantedPermissions && requestCode == FileHelper.STORAGE_PERMISSIONS_REQUEST_CODE){
            showLongNotificationHint(getString(R.string.download_cancelled), null);
            pendingDownload = null;
        }
    }

    public boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public boolean hasLocationPermission()
    {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;//"android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public void openCalculator(Double parsedNumber) {
        // In browser solution would be best if I had a good calculator..
        //previewWindow.show(parsedNumber.toString(), "calculator", true);
        // APP SOLUTION THAT I CAN GET TO OPEN THE CALCULATOR BUT NOT POPULATE IT WITH THE SELECTED NUMBER
        if(parsedNumber!=null) {
            String copyText = parsedNumber.toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("", copyText);
            if(clipboard!=null) {
                clipboard.setPrimaryClip(clipData);
                showNotificationHint(String.format(getString(R.string.copied_xxxx), copyText), null);
            }
        }
        try {
            // Note: this didn't even work on vanilla android oreo but maybe it works on some version?
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_CALCULATOR);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }catch(ActivityNotFoundException e){
            try {
                // Hack method to sniff packages with calculator in the name
                ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
                final PackageManager pm = getPackageManager();
                List<PackageInfo> packs = pm.getInstalledPackages(0);
                for (PackageInfo pi : packs) {
                    if (pi.packageName!=null && pi.packageName.toString().toLowerCase().contains("calcul")) {
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put("appName", pi.applicationInfo.loadLabel(pm));
                        map.put("packageName", pi.packageName);
                        items.add(map);
                    }
                }
                if (items.size() >= 1) {
                    String packageName = (String) items.get(0).get("packageName");
                    if(packageName!=null) {
                        Intent i = pm.getLaunchIntentForPackage(packageName);
                        if (i != null)
                            startActivity(i);
                    }
                } else {
                    // Application not found
                }
            }catch(Exception e2){
                if(parsedNumber!=null) {
                    previewWindow.show(parsedNumber.toString(), "calculator", true);
                }
            }
        }
    }

    public Item toggleFaveStateOfAddress(View button, String address, String faveTitle, @NonNull String iconContext,
                                        Integer faveTypeIfKnown, Item existingItemToSave, String thumbAddress) {
        final TabViewHolder currentTabViewHolder = getCurrentTabViewHolder();
        if (faveTypeIfKnown == null){
            faveTypeIfKnown = UrlHelper.getFaveTypeFromAddress(address, currentTabViewHolder);
        }
        int bottomMarginForTagDialogInPx = UnitHelper.getNavBarHeightInPx(isInCompactMode(), this);
        if(iconContext.equals("contextMenu")){
            bottomMarginForTagDialogInPx += UnitHelper.convertDpToPx(52, this);
        }else if(iconContext.equals("pageContextMenu")){
            bottomMarginForTagDialogInPx += UnitHelper.convertDpToPx(70, this);
        }
        Item item = existingItemToSave!=null ? existingItemToSave : ItemRepository.getItemBasedOnAddressOrSelectedText(address, faveTypeIfKnown, database, false);
        int itemId;
        SQLiteDatabase db = DatabaseHelper.getInstance(this).getWritableDatabase();
        if(item!=null && item.id>0){
            itemId = item.id;
            if(item.isFavourite==1){
                // Unfave but leave in the DB so we keep it's preferences
                if(button!=null) {
                    ThemeHelper.updateFaveIconVisualState(false, (ImageView) button, this, iconContext);
                }
                ItemRepository.unfaveItem(db, itemId);
                item.isFavourite = 0;
            }else {
                // Recycle a previously unfaved item
                if(button!=null) {
                    ThemeHelper.updateFaveIconVisualState(true, (ImageView) button, this, iconContext);
                }
                this.bounceButton(button);
                ItemRepository.faveItem(db, itemId);
                item.isFavourite = 1;
                if(preferences.showTagFaveDialogWhenPressingSave()){
                }
            }
            speedDialListAdapter.reloadCurrentList(isViewingExpandedSpeedDialListOfType, filterFavesByTagIds, null);
            return item;
        } else {
            // Doesn't exist as a fave at all, create as new
            if(faveTypeIfKnown== ItemType.VIDEO){
                if(thumbAddress==null || thumbAddress.length()==0){
                }
                if(currentTabViewHolder!=null && (faveTitle==null || faveTitle.length()==0)){
                    faveTitle = currentTabViewHolder.getCurrentPageTitle();
                }
            }
            if(button!=null) {
                ThemeHelper.updateFaveIconVisualState(true, (ImageView) button, this, iconContext);
            }
            this.bounceButton(button);
            Item savedItem = ItemRepository.save(this, 0, address, faveTitle,
                    0, true, true, faveTypeIfKnown, thumbAddress, db, null);
            if(button!=null) {
                ThemeHelper.updateFaveIconVisualState((savedItem != null && savedItem.isFavourite()), (ImageView) button, this, iconContext);
            }
            itemId = savedItem!=null ? savedItem.id : -1;
            if(itemId > -1){
                String faveMessage;
                switch (faveTypeIfKnown) {
                    case ItemType.IMAGE:
                        faveMessage = getString(R.string.added_to_fave_images);
                        break;
                    case ItemType.VIDEO:
                        faveMessage = getString(R.string.added_to_fave_videos);
                        break;
                    case ItemType.FAVE_QUOTE:
                        faveMessage = getString(R.string.added_to_fave_quotes);
                        break;
                    default:
                    case ItemType.WEB_SITE:
                        faveMessage = String.format(getString(R.string.xxx_added_to_fave_pages), savedItem.title);
                        break;
                }
                if(preferences.showTagFaveDialogWhenPressingSave()){
                } else {
                    showNotificationHint(faveMessage, null);
                }
            }
            speedDialListAdapter.reloadCurrentList(isViewingExpandedSpeedDialListOfType, filterFavesByTagIds, null);
            return savedItem;
        }
    }

    public void performVoiceSearch() {
        try {
            showNotificationHint(getString(R.string.voice_search), null);
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            //if(voiceCommandMode && !recording){
            //    intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak a command to be executed...");
            //}else{
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_search_prompt));
            //}
            startActivityForResult(intent, RECORD_VOICE_INTENT_RESULT);
        }catch(ActivityNotFoundException e){
            showLongNotificationHint("Voice recognition app not found. Try this...", null);
            String appPackageName = "com.google.android.googlequicksearchbox";
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }
    }

    public void onTogglePrivateModeBtnClick(View view) {
        setIncognitoModeEnabled(!preferences.isPrivateBrowsingModeEnabled());
    }

    public void shareImageDownloadedToTempCache() {
        // Load image from cache
        File imagePath = new File(this.getCacheDir(), "images");
        File newFile = new File(imagePath, "image.png"); // uses this image for cacheevery time
        Uri contentUri = FileProvider.getUriForFile(this, "fishpowered.best.browser.fileprovider", newFile);

        if (contentUri != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)));
        }
    }

    public void onToggleNightModeBtnClick(View view) {
        final boolean newNightModeSetting = !isNightModeEnabled();
        setNightMode(newNightModeSetting);
    }

    public void setNightMode(boolean newNightModeSetting) {
        preferences.edit().putBoolean("nightMode", newNightModeSetting).apply();
        updateNightModeIcon();
        recreate();
    }

    /**
     * Called by site preferences dialog
     * @param view
     */
    public void onOpenBrowserSettingsBtnClick(View view) {
        openSettingsActivity();
    }

    public void onHideSitePreferencesDialogMaskClick(View view) {
        TabViewHolder tab = getCurrentTabViewHolder();
        if(tab==null){
            return;
        }
    }

    public void triggerOpenNewTab(boolean triggeredFromViewingThumbList, String tabOpenedBy) {
        final int insertIndex = getSelectedTabPosition(false) + 1;

        //final int insertIndex = getSelectedTabPosition(false) + 1;
        addressInput.setText("");
        addNewTabAtPosition(insertIndex, null, true, tabAdapter.getNewTabId(), tabOpenedBy, !triggeredFromViewingThumbList, false);
    }

    private int getMostUsedSpeedDialList() {
        return SpeedDialListViewHolder.FAVES_LIST; // todo unhardcode
    }

    public void removeFullScreenFlags() {
        getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
    }

    /**
     * Hide system navigation bar for the purpose of maximising app space under normal viewing
     * (not the same as full-screening for video/maps etc)
     */
    public static void hideSystemNavigationBar(Window window, boolean hideTimeBar){
        Log.d("HIDESYSNAVBAR", "Hiding");
        int flags = SYSTEM_UI_FLAG_IMMERSIVE_STICKY | SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if(hideTimeBar){
            flags |= SYSTEM_UI_FLAG_FULLSCREEN;
//            flags |= SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN; this results in the keyboard going over thge layout which is no good as then u can't see the address bar
        }
        window.getDecorView().setSystemUiVisibility(flags);
    }

    /**
     * Hide system navigation bar for the purpose of maximising app space under normal viewing
     * (not the same as full-screening for video/maps etc)
     */
    public static void showSystemNavigationBar(Window window){
        Log.d("HIDESYSNAVBAR", "Hiding");
        int flags = 0;
        window.getDecorView().setSystemUiVisibility(flags);
    }

    /**
     * These flags will tell android to hide the navigation bar and time bar for viewing full screen content
     *
     */
    public void addFullScreenFlags() {
        this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
        int flags = SYSTEM_UI_FLAG_LOW_PROFILE | SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        flags |= SYSTEM_UI_FLAG_FULLSCREEN;
        flags |= SYSTEM_UI_FLAG_LAYOUT_STABLE;
        flags |= SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        flags |= SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        flags |= SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    public void openDownloadManagerActivity(View view) {
        Intent intent = new Intent();
        intent.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
        try {
            startActivity(intent);
        }catch(ActivityNotFoundException e){
            showLongNotificationHint("Download manager not found", null);
        }
    }

    public ServiceConnection textToSpeechMediaServiceConnection= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //serviceInstance = ((TextToSpeechMediaControllerService) iBinder).getInstance();
            // now you have the instance of service.
            Log.d("TTSMEDIAPLAYER", "Service connected!");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //serviceInstance = null;
            Log.d("TTSMEDIAPLAYER", "Service DISconnected!");
        }
    };

    public boolean isConnectedToWiFi(){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connMgr==null){
            return false;
        }
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        return (activeInfo != null && activeInfo.isConnected() && activeInfo.getType() == ConnectivityManager.TYPE_WIFI);
    }


    public void onViewOpenTabsBtnClick(View view) {
    }


    public void forceCloseKeyboard(View editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if(imm==null) return;
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }


    private View.OnClickListener onTabScrollLockBtnClick = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            toggleTabSwipeLock();
        }
    };

    private String isAnimatingTabSwipeBtn = null;
    public void showTabSwipeLockBtn(String debugReason){
        Log.d("TABSWIPELOCK", "Showing: "+debugReason);
        hideTabSwipeLockBtnAfterDelay(); // will also reset existing hide after delay timer
        updateTabSwipeLockBtn();
        if("showing".equals(isAnimatingTabSwipeBtn)){
            // Already animating to visible
            //Log.d("ANDROIDISPOO", "Skipping show() because already "+isAnimatingTabSwipeBtn);
            return;
        }
        isAnimatingTabSwipeBtn = "showing";
        final float currentAlpha = tabSwipeLockBtn.getAlpha();
        //Log.d("ANDROIDISPOO", "Current alpha "+currentAlpha+" "+isAnimatingTabSwipeBtn);
        tabSwipeLockBtn.animate().cancel();
        ViewPropertyAnimator animator = tabSwipeLockBtn.animate().alpha(1f);
        animator.setDuration(250);
        animator.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                tabSwipeLockBtn.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animator animator) {
                isAnimatingTabSwipeBtn = null;
            }
            @Override
            public void onAnimationCancel(Animator animator) {}
            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
        animator.start();
    }

    public void hideTabSwipeLockBtn(boolean instant){
        hideTabSwipeLockBtnCountDownTimer.cancel();
        if(instant){
            tabSwipeLockBtn.setVisibility(GONE);
            return;
        }
        if("hiding".equals(isAnimatingTabSwipeBtn) || tabSwipeLockBtn.getVisibility()== GONE){
            // Already being hidden
            //Log.d("ANDROIDISPOO", "Skipping hide() because already "+isAnimatingTabSwipeBtn);
            return;
        }
        isAnimatingTabSwipeBtn = "hiding";
        final float currentAlpha = tabSwipeLockBtn.getAlpha();
        //Log.d("ANDROIDISPOO", "Current alpha "+currentAlpha+" "+isAnimatingTabSwipeBtn);
        final ObjectAnimator animation = ObjectAnimator.ofFloat(tabSwipeLockBtn, "alpha", 0f);
        animation.setDuration(500);
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}
            @Override
            public void onAnimationEnd(Animator animator) {
                isAnimatingTabSwipeBtn = null;
                tabSwipeLockBtn.setVisibility(GONE);
            }
            @Override
            public void onAnimationCancel(Animator animator) {}
            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
        animation.start();
    }

    public void toggleTabSwipeLock() {
        TabViewHolder currentTab = getCurrentTabViewHolder();
        final boolean wasPreviouslyLocked = currentTab.isTabSwipingLocked();
        final boolean isNowLocked = !wasPreviouslyLocked;
        currentTab.setTabSwipingLocked(isNowLocked);
        showTabSwipeLockBtn("Lock toggled");
    }

    private Boolean knownTabSwipeBtnState = null;
    public void updateTabSwipeLockBtn() {
        TabViewHolder currentTab = getCurrentTabViewHolder();
        if(currentTab==null){
            return;
        }
        final boolean tabSwipingLocked = currentTab.isTabSwipingLocked();
        if(knownTabSwipeBtnState!=null && tabSwipingLocked==knownTabSwipeBtnState){
            return;
        }
        knownTabSwipeBtnState = tabSwipingLocked;
        if(tabSwipingLocked){
            tabSwipeLockBtn.setText(R.string.locked);
            tabSwipeLockBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_video_screen_locked, 0, 0, 0);
            tabSwipeLockBtn.setTypeface(null, Typeface.BOLD);
        }else{
            tabSwipeLockBtn.setText(R.string.tab_scroll);
            tabSwipeLockBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_video_screen_unlocked, 0, 0, 0);
            tabSwipeLockBtn.setTypeface(null, Typeface.NORMAL);
        }
        tabSwipeLockBtn.setCompoundDrawablePadding(UnitHelper.convertDpToPx(5f, this));
        ThemeHelper.setTextViewDrawableTint(tabSwipeLockBtn, ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.contextMenuIconColour, this));
    }

    /**
     * Note it can be "VISIBLE" but nearly fully transparent
     * @return
     */
    public boolean isTabSwipeBtnVisible(){
        return tabSwipeLockBtn.getVisibility()==View.VISIBLE;
    }

    private void hideTabSwipeLockBtnAfterDelay() {
        hideTabSwipeLockBtnCountDownTimer.cancel();
        hideTabSwipeLockBtnCountDownTimer.start();
    }

    public CountDownTimer hideTabSwipeLockBtnCountDownTimer = new CountDownTimer(1700, 1) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            hideTabSwipeLockBtn(false);
        }
    };

    public void showSiteHomePageBtn(String domain) {
        String cleanDomain = UrlHelper.getDomain(domain, false, true, false);
        if(cleanDomain==null){
            siteHomePageBtn.setVisibility(View.GONE);
            return;
        }
        if(cleanDomain.length() > 23){
            cleanDomain = cleanDomain.substring(0, 20)+"...";
        }
        siteHomePageBtn.setText(cleanDomain);
        siteHomePageBtn.setVisibility(View.VISIBLE);
    }

    public void hideSiteHomePageBtn() {
        siteHomePageBtn.setVisibility(View.GONE);
    }
}

