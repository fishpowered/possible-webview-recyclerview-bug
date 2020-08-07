package fishpowered.best.browser;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import androidx.appcompat.widget.AppCompatEditText;
import fishpowered.best.browser.db.Item;
import fishpowered.best.browser.db.ItemRepository;
import fishpowered.best.browser.db.ItemType;
import fishpowered.best.browser.speeddial.list.SpeedDialListViewHolder;
import fishpowered.best.browser.utilities.ThemeHelper;
import fishpowered.best.browser.utilities.UnitHelper;
import fishpowered.best.browser.utilities.UrlHelper;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.MetricAffectingSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import static android.view.inputmethod.EditorInfo.IME_ACTION_GO;
import static androidx.core.view.inputmethod.EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING;

/**
 * Handles the address bar events, address input buttons and all of their events
 */
public class AddressInputEditText extends AppCompatEditText {
    private int addressInputTextFieldRightPadding;
    private Browser browser;
    private ImageView clearAddressAndMicBtn;
    private ImageView addressFaveBtn;
    private ImageView addressSearchEngineBtn;
    private ImageView goToAddressBtn;
    private ImageView addressBarReturnBtn;
    protected CharSequence searchTextBefore;

    public AddressInputEditText(Context context) {
        super(context);
    }

    public AddressInputEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AddressInputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initAddressInputAndButtons(Browser browser){
        if(getText()==null){
            return;
        }

        this.browser = browser;
        goToAddressBtn = browser.speedDialContainer.findViewById(R.id.go_to_address_btn);
        addressBarReturnBtn = browser.speedDialContainer.findViewById(R.id.address_bar_back);
        addressFaveBtn = browser.speedDialContainer.findViewById(R.id.add_fave_from_address_btn);
        addressSearchEngineBtn = browser.speedDialContainer.findViewById(R.id.address_search_eng_hint_btn);
        clearAddressAndMicBtn = (ImageView) browser.speedDialContainer.findViewById(R.id.clear_address_and_mic_btn);
        addressInputTextFieldRightPadding = getPaddingRight();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //setImportantForAutofill(IMPORTANT_FOR_AUTOFILL_NO);
        }
        setSelectAllOnFocus(true);
        setOnFocusChangeListener(this.onAddressBarLosesFocus);
        setOnEditorActionListener(this.onAddressBarAction);
        addTextChangedListener(this.onAddressInputChange);
        addressSearchEngineBtn.setOnClickListener(onAddressSearchEngineBtnClick);
        goToAddressBtn.setOnClickListener(onGoToAddressButtonClick);
        addressBarReturnBtn.setOnClickListener(onGoToAddressButtonClick);
        clearAddressAndMicBtn.setOnClickListener(onClearAddressOrMicOrCopyButtonClick);
        addressFaveBtn.setOnClickListener(onFaveButtonClick);
        if(browser.preferences.isPrivateBrowsingModeEnabled()){
            setImeOptions(IME_ACTION_GO | IME_FLAG_NO_PERSONALIZED_LEARNING);
        }else{
            setImeOptions(IME_ACTION_GO);
        }
        updateAddressBarIcons();
    }

    public void updateAddressBarIcons() {
        if(getText()==null || clearAddressAndMicBtn==null){ // not initialised yet
            return;
        }
        final String addressOrSearch = getText().toString()+"";
        final boolean isUrlAttempt = UrlHelper.isUrlAttempt(addressOrSearch);
        if (addressOrSearch.length() > 0) {
            if(isUrlAttempt && isTextFullySelected() && browser.isKeyboardOpen()){
                clearAddressAndMicBtn.setImageResource(R.drawable.ic_address_bar_copy);
                clearAddressAndMicBtn.setTag("copy");
            } else {
                clearAddressAndMicBtn.setImageResource(R.drawable.ic_nav_bar_clear_address_18dp);
                clearAddressAndMicBtn.setTag("clear");
            }
        }else{
            clearAddressAndMicBtn.setImageResource(R.drawable.ic_address_bar_mic);
            clearAddressAndMicBtn.setTag("mic");
        }
        final int faveBtnPadding = UnitHelper.convertDpToPx(38, browser);
        if(isUrlAttempt) {
            // TODO could be made async?
            TabViewHolder tabForFetchingMeta = isAddressBarCurrentAddress() ? browser.getCurrentTabViewHolder() : null;
            Item item = ItemRepository.getItemBasedOnAddressOrSelectedText(addressOrSearch, UrlHelper.getFaveTypeFromAddress(addressOrSearch, tabForFetchingMeta), browser.database, false);
            boolean isFave = (item!=null && item.isFavourite==1);
            addressFaveBtn.setVisibility(View.VISIBLE);
            setPadding(
                    getPaddingLeft(),
                    getPaddingTop(),
                    addressInputTextFieldRightPadding + faveBtnPadding,
                    getPaddingBottom()
            );
            ThemeHelper.updateFaveIconVisualState(
                    isFave,
                    addressFaveBtn,
                    browser,
                    "addressInput"
            );
        }else{
            addressFaveBtn.setVisibility(View.GONE);
            setPadding(
                    getPaddingLeft(),
                    getPaddingTop(),
                    addressInputTextFieldRightPadding,
                    getPaddingBottom()
            );
        }
        updateLeftMostIcon(isUrlAttempt);
    }

    private void updateLeftMostIcon(final boolean isUrlAttempt) {
        if(isAddressBarCurrentAddress()) {
            goToAddressBtn.setVisibility(View.GONE);
            addressBarReturnBtn.setVisibility(View.VISIBLE);
            addressSearchEngineBtn.setVisibility(View.GONE);
        }else if(getText()!=null && isUrlAttempt) {
            goToAddressBtn.setVisibility(View.VISIBLE);
            addressBarReturnBtn.setVisibility(View.GONE);
            addressSearchEngineBtn.setVisibility(View.GONE);
        }else{
            goToAddressBtn.setVisibility(View.GONE);
            addressBarReturnBtn.setVisibility(View.GONE);
            addressSearchEngineBtn.setVisibility(View.VISIBLE);
            final Pair<String, String> textSearchEngine = browser.preferences.getTextSearchEngine(getText().toString(), false);
            int searchEngineDrawableResource = ThemeHelper.getSearchEngineIcon(textSearchEngine.first, "addressInput");
            addressSearchEngineBtn.setImageResource(searchEngineDrawableResource);
        }
    }

    public void setAddressInputTextWithoutChangeEvent(String text){
        removeTextChangedListener(onAddressInputChange);
        setText(text);
        addTextChangedListener(this.onAddressInputChange);
        updateAddressBarIcons();
    }

    /**
     * A fave button has been pressed in the address bar, toggle the is fave status
     */
    public OnClickListener  onFaveButtonClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(getText()==null) return;
            String address = getText().toString().trim();
            if(!UrlHelper.isUrlAttempt(address)){
                ThemeHelper.updateFaveIconVisualState(false, (ImageView) v, browser, "addressInput");
                return;
            }
            browser.toggleFaveStateOfAddress(v, address, null, "addressInput", ItemType.WEB_SITE, null, null);
        }
    };

    /**
     * Events for when a user is typing into the addressInput
     */
    protected TextWatcher onAddressInputChange = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(getText()==null) return;
            String trimmedAddress = getText().toString().trim();
            //noinspection DuplicateExpressions
            if((getText().toString().endsWith(" /") || getText().toString().endsWith(" ?"))
                    && trimmedAddress.length() >= 4 && UrlHelper.isUrlAttempt(trimmedAddress.substring(0, trimmedAddress.length()-2))){
                // DISCOVERED THERE IS NO RELIABLE WAY TO DETECT SPACE INSERTED VIA KEY PRESS OR FROM AUTOCOMPLETE DUE TO KEYBOARDS WORKING DIFFERENTLY
                // IMPORTANT: native android keyboard doesn't auto add a space when touching a word suggestion, swift does. So if removing
                // spaces from native keyboard u are likely to remove a space that has been manually added so just fix " /" instead
                //noinspection DuplicateExpressions
                setText(trimmedAddress.substring(0, trimmedAddress.length()-2)+trimmedAddress.substring(trimmedAddress.length()-1));
                setSelection(getText().length());
            }
            boolean reloadSpeedDial = true;
            if(UrlHelper.isFileUrlForFetchingOfflinePages(getText().toString())){
                setText(ItemRepository.getSavedForLaterAddressBasedOnInternalFileUrl(getText().toString(), browser, true));
                reloadSpeedDial = false;
            }
            //if(browser.getCurrentFocus() == AddressInputEditText.this){ // prevents .setText from triggering it
                //    Browser.setIsSearchingSpeedDial(true);
            //}
            searchTextBefore = s.toString();
            if(reloadSpeedDial) {
                browser.speedDialListAdapter.reloadCurrentList(browser.isViewingExpandedSpeedDialListOfType, browser.filterFavesByTagIds, browser.speedDialDomainFilter);
            }
            updateAddressBarIcons();
            //showHistory(getText().toString().trim());
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            searchTextBefore = s.toString(); // DON'T TRIM HERE
        }

        @Override
        public void afterTextChanged(Editable string) {
            // Strip out any pasted formatting
            CharacterStyle[] toBeRemovedSpans = string.getSpans(0, string.length(), MetricAffectingSpan.class);
            for (int index = 0; index < toBeRemovedSpans.length; index++){
                string.removeSpan(toBeRemovedSpans[index]);
            }
            // Hide the clear history button as it can get in the way when searching
            if(browser.speedDialListAdapter.getCurrentPosition()==SpeedDialListViewHolder.HISTORY_LIST){
                browser.clearHistoryBtn.setVisibility(browser.isSearchingSpeedDialDatabase(false) ? GONE : VISIBLE);
            }
        }
    };

    public OnClickListener onAddressSearchEngineBtnClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(getText()==null) return;
            browser.openAddressOrSearch(getText().toString().trim(), true);
        }
    };

    public OnClickListener onGoToAddressButtonClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(getText()==null) return;
            if(isAddressBarCurrentAddress()){
                browser.onBackPressed();
            }else{
                browser.openAddressOrSearch(getText().toString(), true);
            }
        }
    };

    public boolean isAddressBarCurrentAddress() {
        if(getText()==null){
            return false;
        }
        if (browser.getCurrentTabViewHolder() == null) {
            return false;
        }
        final String currentPageAddress = browser.getCurrentTabViewHolder().getCurrentPageAddress(null);
        return currentPageAddress !=null && (
            currentPageAddress.equals(getText().toString())
            || currentPageAddress.startsWith("file://")
            || currentPageAddress.startsWith("data://")
        );
    }

    /**
     * Hide the keyboard if the address bar loses focus
     */
    protected TextView.OnFocusChangeListener onAddressBarLosesFocus = new TextView.OnFocusChangeListener(){

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(v.getId() == R.id.addressBarInput && !hasFocus) {
                // Hide keyboard if the address bar loses focus
                browser.forceCloseKeyboard(AddressInputEditText.this);
            }
        }
    };

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if(browser!=null) { // initButtons might not have been initialised yet
            updateAddressBarIcons();
        }
        super.onSelectionChanged(selStart, selEnd);
    }

    /**
     * Event for when the enter button is pressed when editing the address input
     */
    protected TextView.OnEditorActionListener onAddressBarAction = new TextView.OnEditorActionListener(){
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            boolean handled = false;
            if(getText()==null) return false;
            //Log.d("DEBUG", "ACTION ID="+actionId);
            // If user submits address box we can load the page or do a search
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_SEARCH){
                String urlOrSearchString = getText().toString().trim();
                //goToAddressOrSearchString(urlOrSearchString); TODO MUST LAUNCH BROWSER ACTIVITY AND LOAD TAB
                //clearFocus();

                // Hide keyboard
                browser.forceCloseKeyboard(AddressInputEditText.this);

                // Open browser
                browser.openAddressOrSearch(urlOrSearchString, true);
                handled = true;
            }
            return handled;
        }
    };

    public OnClickListener onClearAddressOrMicOrCopyButtonClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(getText()==null) return;
            switch((String) clearAddressAndMicBtn.getTag()){
                case "copy":
                    ClipboardManager clipboard = (ClipboardManager) browser.getSystemService(Context.CLIPBOARD_SERVICE);
                    final String copyText = getText().toString();
                    ClipData clipData = ClipData.newPlainText(copyText, copyText);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clipData);
                        browser.showNotificationHint(browser.getString(R.string.address_copied), null);
                    }
                    break;
                case "paste":
                    setText(browser.getClipboardContentsAsString());
                    if (!browser.isKeyboardOpen()) {
                        focusAddressInputAndShowKeyboard();
                    }
                    break;
                case "clear":
                    // Clear address button has been clicked
                    setText("");
                    if (!browser.isKeyboardOpen()) {
                        focusAddressInputAndShowKeyboard();
                    }
                    break;
                case "mic":
                    // Mic button clicked
                    browser.performVoiceSearch();
                    break;
            }
            updateAddressBarIcons();
        }
    };

    public void focusAddressInputAndShowKeyboard() {
        //Log.d("TABBIN", "SHOWING KEYBOARD");
        // Focus address input and show keyboard
        requestFocus();
        selectAll();
        InputMethodManager imm = (InputMethodManager) browser.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm==null) return;
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private boolean isTextFullySelected() {
        final int addressLength = length();
        //showLongNotificationHint("start:"+getSelectionStart()+" end:"+getSelectionEnd()+" len:"+addressLength);
        return (getSelectionStart()==0
                && getSelectionEnd()==addressLength
                ); // && browser.isKeyboardOpen()
    }
}
