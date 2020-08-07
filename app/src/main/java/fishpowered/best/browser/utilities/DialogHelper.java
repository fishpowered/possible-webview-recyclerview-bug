package fishpowered.best.browser.utilities;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;

public class DialogHelper {

    public static void showSnackBarNotificationWithActionBtn(Browser browser, String text, int iconResourceId, int percentageFromBottom,
                                                             int lengthToDisplay, String actionBtnText, final Runnable actionBtnRunnable){
        final Snackbar snackbar = Snackbar
                .make(browser.speedDialContainer, text, lengthToDisplay);
        if(lengthToDisplay== Snackbar.LENGTH_INDEFINITE && actionBtnText==null) {
            snackbar.setAction(R.string.dialog_ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackbar.dismiss();
                }
            });
        }else if (actionBtnText!=null && actionBtnRunnable!=null){
            snackbar.setAction(actionBtnText, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    actionBtnRunnable.run();
                    snackbar.dismiss();
                }
            });
        }
        snackbar.setActionTextColor(ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.colorAccent, browser));
        ViewGroup snackBarLayout = (ViewGroup) snackbar.getView();

        if(iconResourceId > 0) {
            TextView snackBarTextView = snackBarLayout.findViewById(com.google.android.material.R.id.snackbar_text);
            if (snackBarTextView != null) {
                // Try and set an icon to indicate the download was successful
                snackBarTextView.setCompoundDrawablesWithIntrinsicBounds(iconResourceId, 0, 0, 0);
                snackBarTextView.setCompoundDrawablePadding(UnitHelper.convertDpToPx(14, browser));
            }
        }

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackBarLayout.getLayoutParams();
        params.gravity = Gravity.BOTTOM;
        params.setMargins(0, 0,0, browser.convertYScreenPercentageToPixels(percentageFromBottom));
        snackBarLayout.setLayoutParams(params);
        // change snack bar text+bg color
        int snackbarTextId = R.id.snackbar_text;
        TextView textView = (TextView) snackBarLayout.findViewById(snackbarTextId);
        if(textView!=null) {
            textView.setTextColor(Color.WHITE);
            snackBarLayout.setBackgroundColor(Color.DKGRAY);
        }
        snackbar.show();
    }

    /**
     *
     * @param browser
     * @param text
     * @param iconResourceId
     * @param percentageFromBottom
     * @param lengthToDisplay if Snackbar.LENGTH_INDEFINITE is used then an ok button will be shown to dismiss
     */
    public static void showSnackBarNotification(Browser browser, String text, int iconResourceId, int percentageFromBottom, int lengthToDisplay){
        showSnackBarNotificationWithActionBtn(browser, text, iconResourceId, percentageFromBottom, lengthToDisplay, null, null);
    }
}
