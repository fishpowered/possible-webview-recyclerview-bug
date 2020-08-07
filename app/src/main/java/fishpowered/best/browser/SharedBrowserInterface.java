package fishpowered.best.browser;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * The browser tab and inspection window share a lot of the same UI
 */
interface SharedBrowserInterface {

    public Browser getBrowser();

    public TextView getAddressHintView();

    public ProgressBar getLoadingBarView();
    public ImageView getLoadingMaskView();

    public WebView getWebView();
    public WebViewClient getWebViewClient();

    /**
     * @return ImageButton|null Null if the refresh hasn't been bound to the menu as an icon
     */
    public ImageButton getStopRefreshForwardButton();

    /**
     * @return ImageButton|null Null if the back button hasn't been bound to the menu as an icon
     */
    public ImageButton getBackButton();

    /**
     *
     * @return TabViewHolder|null can be null for preview windows
     */
    public TabViewHolder getTabViewHolder();

    public boolean isPreviewWindow();
}
