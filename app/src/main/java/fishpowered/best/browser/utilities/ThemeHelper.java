package fishpowered.best.browser.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.TabViewHolder;

public class ThemeHelper {

    /**
     * Style the alert dialog/prompt with coloured action buttons and optional icon
     * @param dialog
     * @param iconResource
     * @param browser
     */
    public static void styleAlertDialog(final AlertDialog dialog, @Nullable final Integer iconResource, final Browser browser){
        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                if(dialog.getButton(AlertDialog.BUTTON_POSITIVE)!=null) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.colorAccent, browser));
                }
                if(dialog.getButton(AlertDialog.BUTTON_NEGATIVE)!=null) {
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.colorAccent, browser));
                }
                if(dialog.getButton(AlertDialog.BUTTON_NEUTRAL)!=null) {
                    dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.colorAccent, browser));
                }
                if(iconResource!=null) {
                    TextView viewToAddIconTo = dialog.findViewById(R.id.alertTitle);
                    if (viewToAddIconTo == null || (viewToAddIconTo.getText()==null || viewToAddIconTo.getText().toString().equals(""))) {
                        viewToAddIconTo = dialog.findViewById(android.R.id.message);
                    }
                    if(viewToAddIconTo!=null) {
                        viewToAddIconTo.setCompoundDrawablesWithIntrinsicBounds(iconResource, 0, 0, 0);
                        ThemeHelper.setTextViewDrawableTint(viewToAddIconTo, ThemeHelper.getColourFromRDotColor(R.color.black, browser));
                        viewToAddIconTo.setCompoundDrawablePadding(UnitHelper.convertDpToPx(15, browser));
                    }
                }
            }
        });
    }

    /**
     *
     * @param themeAttribute R.attr.xxx
     * @param activity
     * @return
     */
    public static int getColourFromCurrentThemeAttribute(int themeAttribute, Context activity){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = activity.getTheme();
        theme.resolveAttribute(themeAttribute, typedValue, true);
        return typedValue.data;
    }

    public static int getColourFromRDotColor(int rDotColor, Context context){
        return ContextCompat.getColor(context, rDotColor);
    }

    public static void notify(int messageId, Activity activity, int length){
        Toast toast = Toast.makeText(activity, messageId, Toast.LENGTH_SHORT);
        //TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        //v.setTextColor(Color.WHITE);
        //v.setBackgroundColor(Color.DKGRAY);
        toast.show();
    }

    public static int getSearchEngineIcon(String searchEngine, String context) {
        int resourceId;
        switch (searchEngine){
            case StringHelper.SEARCH_ENGINE_GOOGLE:
                if(context.equals("navBar")){
                    resourceId = R.drawable.ic_nav_bar_google_24;
                }else {
                    resourceId = R.drawable.ic_context_menu_search_google_24;
                }
                break;
            case StringHelper.SEARCH_ENGINE_DUCKDUCKGO:
                if(context.equals("navBar")){
                    resourceId = R.drawable.ic_nav_bar_search_duck_24;
                }else {
                    resourceId = R.drawable.ic_context_menu_search_duck_24;
                }
                break;
            case StringHelper.SEARCH_ENGINE_BING:
                resourceId = R.drawable.ic_context_menu_search_bing_24dp;
                break;
            case StringHelper.SEARCH_ENGINE_YANDEX:
                resourceId = R.drawable.ic_context_menu_search_yandex_24dp;
                break;
            case StringHelper.SEARCH_ENGINE_BAIDU:
                resourceId = R.drawable.ic_context_menu_search_baidu_24dp;
                break;
            case StringHelper.SEARCH_ENGINE_ECOSIA:
                resourceId = R.drawable.ic_context_menu_search_ecosia_24dp;
                break;
            case StringHelper.SEARCH_ENGINE_ASK:
            case StringHelper.SEARCH_ENGINE_WOLFRAMALPHA:
            default:
                resourceId = R.drawable.ic_context_menu_search_generic_24dp;
                break;
        }
        return resourceId;
    }

    public static void applyCurrentTheme(Context context, SharedPreferences sharedPreferences) {
        final String themeName = sharedPreferences.getString("themeName", context.getString(R.string.defaultThemeName));
        final int themeResourceId = context.getResources().getIdentifier(themeName, "style", context.getPackageName());
        context.setTheme(themeResourceId);
    }

    public static void applyCustomTheme(@NonNull String themeName, Context context) {
        final int themeResourceId = context.getResources().getIdentifier(themeName, "style", context.getPackageName());
        context.setTheme(themeResourceId);
    }

    public static void updateFaveIconVisualState(boolean isFave, ImageView icon, Activity context,
                                                 String iconContext) {
        icon.setImageResource(isFave ? R.drawable.ic_favorite_selected_red_24dp : R.drawable.ic_context_menu_favorite_unselected_24dp);
        int unselectedColour = 0;
        int selectedColour = 0;
        switch(iconContext){
            case "addressInput":
                unselectedColour = getColourFromCurrentThemeAttribute(R.attr.mainNavBarIconColour, context);
                selectedColour = getColourFromCurrentThemeAttribute(R.attr.mainNavBarIconColour, context);
                break;
            case "contextMenu":
            case "pageContextMenu":
                unselectedColour = getColourFromCurrentThemeAttribute(R.attr.contextMenuIconColour, context);
                selectedColour = getColourFromCurrentThemeAttribute(R.attr.speedDialListFaveTheme, context);
                break;
            case "videoControls":
                unselectedColour = getColourFromRDotColor(R.color.video_controls, context);
                selectedColour = getColourFromCurrentThemeAttribute(R.attr.speedDialListFaveTheme, context);
                break;
            default:
                return;
        }
        final int faveColour = isFave
                ? selectedColour
                : unselectedColour;
        icon.setColorFilter(0xff000000 + faveColour);
    }

    public static void updateSavedForLaterIconVisualState(boolean isOnReadLaterList, ImageView icon, Activity context,
                                                          String iconContext) {
        icon.setImageResource(isOnReadLaterList ? R.drawable.ic_context_menu_read_later_already_added : R.drawable.ic_context_menu_read_later_add);
        int unselectedColour = 0;
        int selectedColour = 0;
        switch(iconContext){
            case "contextMenu":
                unselectedColour = getColourFromCurrentThemeAttribute(R.attr.contextMenuIconColour, context);
                selectedColour = getColourFromCurrentThemeAttribute(R.attr.speedDialListSavedForLaterTheme, context);
                break;
            default:
                return;
        }
        final int colour = isOnReadLaterList
                ? selectedColour
                : unselectedColour;
        icon.setColorFilter(0xff000000 + colour);
    }
    public static void updateSaveForLaterCaption(String currentPageAddress, TextView text, boolean isReadLater, TabViewHolder tabViewHolder) {
        if(isReadLater){
            text.setText(tabViewHolder.browser.getString(R.string.saved));
            text.setTextColor(ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.speedDialListSavedForLaterTheme, tabViewHolder.browser));
        }else{
            int hint = UrlHelper.isPossibleVideoStreamingLink(currentPageAddress, true) ? R.string.watch_later_abbr : R.string.read_later_abbr;
            text.setText(tabViewHolder.browser.getString(hint));
            text.setTextColor(ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.contextMenuIconColour, tabViewHolder.browser));
        }
    }


    public static void updateFaveCaption(TextView text, boolean isFave, TabViewHolder tabViewHolder) {
        if(isFave){
            text.setTextColor(ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.speedDialListFaveTheme, tabViewHolder.browser));
            text.setText(tabViewHolder.browser.getString(R.string.faved_abbr));
        }else{
            text.setTextColor(ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.contextMenuIconColour, tabViewHolder.browser));
            text.setText(tabViewHolder.browser.getString(R.string.fave_abbr));
        }
    }

    public static void setNavButtonIconBasedOnAction(String action, ImageButton button, String defaultSearchEngine, String context) {
        //final int navIconColor = ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.mainNavBarIconColour, browser);
        //boolean setIconColor = false;
        button.setVisibility(View.VISIBLE);
        switch(action){
            case "showPageMenu":
                button.setImageResource(R.drawable.ic_nav_bar_menu_button_24dp);
                break;
            case "goToCats":
                button.setImageResource(R.drawable.ic_nav_bar_cat_footprint);
                break;
            case "navigateBack":
                button.setImageResource(R.drawable.ic_nav_bar_back_29dp);
                break;
            case "openBrowserSettings":
                button.setImageResource(R.drawable.ic_speed_dial_settings_24dp);
                break;
            case "refresh":
                button.setImageResource(R.drawable.ic_nav_bar_refresh_24dp);
                break;
            case "newTab":
                button.setImageResource(R.drawable.ic_nav_bar_add_tab_28dp);
                break;
            case "goToCustomHomePage":
                button.setImageResource(R.drawable.ic_nav_bar_home_24dp);
                break;
            case "navigateForward":
                button.setImageResource(R.drawable.ic_nav_bar_forward_29dp);
                break;
            case "searchOrEnterAddress":
            case "showSpeedDial":
                button.setImageResource(R.drawable.ic_nav_bar_show_speed_dial);
                break;
            case "pasteToSearchOrGo":
                button.setImageResource(R.drawable.ic_context_menu_paste_24dp);
                break;
            case "pinToWindow":
                button.setImageResource(R.drawable.ic_context_menu_pin_24dp);
                break;
            case "pullTabDown":
                button.setImageResource(R.drawable.ic_nav_bar_pull_down);
                break;
            case "copyPageUrl":
                button.setImageResource(R.drawable.ic_context_menu_copy_24dp);
                break;
            case "addPageToFaves":
                button.setImageResource(R.drawable.ic_context_menu_favorite_unselected_24dp);
                break;
            case "closeAllTabs":
                button.setImageResource(R.drawable.ic_close_open_tabs);
                break;
            case "goToDefaultSearchEngine":
                final int searchEngineIcon = getSearchEngineIcon(defaultSearchEngine, "navBar");
                button.setImageResource(searchEngineIcon);
                break;
            case "viewFaves":
                //button.setImageResource(R.drawable.ic_nav_bar_view_faves_list);
                //button.setImageResource(R.drawable.ic_nav_bar_open_fave_list);
                button.setImageResource(R.drawable.ic_nav_bar_view_fave_list_alt);
                break;
            case "viewWhatsHot":
                button.setImageResource(R.drawable.ic_nav_bar_whats_hot);
                break;
            case "viewSavedForLater":
                button.setImageResource(R.drawable.ic_speed_dial_read_later_head);
                break;
            case "viewTabs": // old pref i think
            case "viewTabsOverview":
            case "viewClosedTabsOverview":
                button.setImageResource(R.drawable.ic_nav_bar_tab_icon_numeric);
                break;
            case "viewHistory":
                button.setImageResource(R.drawable.ic_speed_dial_history);
                break;
            case "sharePageUrl":
                button.setImageResource(R.drawable.ic_nav_bar_share);
                break;
            case "voiceSearch":
                button.setImageResource(R.drawable.ic_nav_bar_mic);
                break;
            case "togglePrivateMode":
                button.setImageResource(R.drawable.ic_private_mode_mask);
                break;
            case "noAction":
                //if(context.equals("settings")){
                    button.setImageResource(R.drawable.ic_nav_bar_no_action);
                //}else {
                //    button.setVisibility(View.GONE);
                //}
                break;
        }
        /*if(setIconColor){
            DrawableCompat.setTint(contextBtnWebSearch.getDrawable(),
                    inspectionType.equals("search") ? highlightColour : tabViewHolder.originalContextIconColour);
        }*/
    }

    public static void highlightContextMenuIcon(Context context, ImageView contextBtn, TextView caption){
        final int highlightColour = ContextCompat.getColor(context, R.color.colorAccentSecondary);
        contextBtn.setColorFilter(0xff000000 + highlightColour);
        if(caption!=null){
            caption.setTextColor(highlightColour);
        }
    }

    public static void unhighlightContextMenuIcon(Activity activity, ImageView contextBtn, TextView caption){
        final int normalColour = ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.contextMenuIconColour, activity);
        contextBtn.setColorFilter(0xff000000 + normalColour);
        if(caption!=null){
            caption.setTextColor(normalColour);
        }
    }

    public static boolean isRTL(Context context){
        Configuration config = context.getResources().getConfiguration();
        return (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }

    public static void setIconTint(ImageView icon, int selectedColour) {
        icon.setColorFilter(selectedColour);
    }

    /**
     * "android:DrawableTint" isn't supported in old android versions so...
     * @param textView
     * @param color Use ThemeHelper.getColourFromRDotColor(textView.getContext(), color) if itäs a R.color.xxx or ThemeHelper.getColourFromCurrentThemeAttribute if it's R.attr.xxx
     */
    public static void setTextViewDrawableTint(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            }
        }
    }

    /**
     * Allows drawables to be inserted into text. Returns a SpannableString that can be used in a textview
     * with setText(spannableString)
     * @param rDotDrawable
     * @param text
     * @param wildCard
     * @param context
     * @return
     */
    @NonNull
    public static SpannableString insertDrawableIntoTextString(int rDotDrawable, String text, String wildCard, Context context) {
        ImageSpan imageSpan = new ImageSpan(context, rDotDrawable);
        SpannableString spannableString = new SpannableString(text);
        int start = text.indexOf(wildCard);
        int end = start + wildCard.length();
        int flag = 0;
        spannableString.setSpan(imageSpan, start, end, flag);
        return spannableString;
    }

    public static void setWidthAndHeightOfImageView(ImageView imageView, int newWidthInPx, int newHeightInPx) {
        imageView.getLayoutParams().width = newWidthInPx;
        imageView.getLayoutParams().height = newHeightInPx;
    }
}

