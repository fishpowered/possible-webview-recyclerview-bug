package fishpowered.best.browser.speeddial.item;

import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import fishpowered.best.browser.speeddial.list.SpeedDialListViewHolder;
import fishpowered.best.browser.speeddial.list.SpeedDialListsAdapter;
import fishpowered.best.browser.utilities.DateTimeHelper;

import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.db.BrowsingHistoryRepository;
import fishpowered.best.browser.db.Item;
import fishpowered.best.browser.utilities.StringHelper;
import fishpowered.best.browser.utilities.ThemeHelper;
import fishpowered.best.browser.utilities.UnitHelper;
import fishpowered.best.browser.utilities.UrlHelper;

public class SpeedDialItemHeadingViewHolder extends AbstractSpeedDialItemViewHolder {

    private int headingNegativeMargin;
    private TextView label;
    private Item currentItem;

    public SpeedDialItemHeadingViewHolder(Browser browser, ViewGroup parent) {
        super(browser, browser.getLayoutInflater().inflate(R.layout.speed_dial_heading, parent, false));
        headingNegativeMargin = UnitHelper.convertDpToPx(-26f, browser);
        label = itemView.findViewById(R.id.speed_dial_heading_tv);
        label.setOnLongClickListener(onLongClickListener);
        label.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if(browser.isViewingExpandedSpeedDialListOfType!=0){
                browser.speedDialListAdapter.reloadCurrentList(0, new ArrayList<Integer>(), null);
            }else if(currentItem.address!=null && currentItem.address.length() > 9){
                int dateFilter = 0;
                try {
                    dateFilter = Integer.parseInt(currentItem.address);
                }catch(NumberFormatException e){}
                if(dateFilter>0){
                    browser.speedDialListAdapter.reloadCurrentList(dateFilter, new ArrayList<Integer>(), "");
                }
            }
        }
    };

    private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if(browser.speedDialListAdapter.getCurrentPosition()== SpeedDialListViewHolder.HISTORY_LIST) {
                final int dateTimeStamp = Integer.parseInt(currentItem.address);
                String friendlyDateFormat;
                if (DateUtils.isToday(dateTimeStamp * 1000L)) {
                    friendlyDateFormat = browser.getString(R.string.today).toLowerCase();
                } else if (DateUtils.isToday((dateTimeStamp * 1000L) + DateUtils.DAY_IN_MILLIS)) {
                    friendlyDateFormat = browser.getString(R.string.yesterday).toLowerCase();
                } else {
                    friendlyDateFormat = DateTimeHelper.convertTimeStampToLocalDate(dateTimeStamp, browser);
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(browser);
                final String domainToClear = UrlHelper.getDomain(StringHelper.toString(browser.speedDialDomainFilter), true, true, false);
                final String message = domainToClear.equals("")
                        ? String.format(browser.getString(R.string.remove_DATE_from_browsing_history), friendlyDateFormat)
                        : String.format(browser.getString(R.string.remove_browsing_history_on_WEBSITE_for_DATE), domainToClear, friendlyDateFormat);
                AlertDialog dialog = builder
                        .setMessage(message)
                        .setNegativeButton(R.string.yes_answer,  new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                BrowsingHistoryRepository.clearDate(dateTimeStamp, browser, browser.recentlyClosedTabAdapter, domainToClear);
                                browser.speedDialListAdapter.reloadCurrentList(browser.isViewingExpandedSpeedDialListOfType, browser.filterFavesByTagIds, domainToClear);
                                browser.showNotificationHint(browser.getString(R.string.deleted), null);
                            }
                        })
                        .setPositiveButton(R.string.no_answer, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        })
                        .create();
                ThemeHelper.styleAlertDialog(dialog, null, browser);
                dialog.show();
            }
            return true;
        }
    };


    @Override
    public void bindDataToView(Item item, int position, int listType) {
        currentItem = item;
            /*ViewGroup.LayoutParams lp = layout.getLayoutParams();
            if (lp instanceof FlexboxLayoutManager.LayoutParams) {
                // Make the header full width
                ((FlexboxLayoutManager.LayoutParams) lp).setFlexBasisPercent(100f);
            }*/
        /*if(position==0){
            solved with spacerHack (extra empty heading)
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) label.getLayoutParams();
            //params.setMargins(0, headingNegativeMargin, 0, 0);
            Log.e("NEGMARGIN", "APPLYING TO "+ item.title);
            label.setPadding(0,0, 0, 0);
            //label.setLayoutParams(0, 0,0, label.getPaddingBottom());
        }*/
        label.setTextColor(SpeedDialListsAdapter.getListColour(listType, browser));
        label.setText(item.title+"");
    }
}
