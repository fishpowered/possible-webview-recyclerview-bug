package fishpowered.best.browser.speeddial.item;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.db.BrowsingHistoryRepository;
import fishpowered.best.browser.db.DatabaseHelper;
import fishpowered.best.browser.db.Item;
import fishpowered.best.browser.db.ItemType;
import fishpowered.best.browser.utilities.ThemeHelper;
import fishpowered.best.browser.utilities.UrlHelper;

public class SpeedDialItemHistoryGroupViewHolder extends AbstractSpeedDialItemViewHolder {
    private Item currentItem;

    public SpeedDialItemHistoryGroupViewHolder(Browser browser, ViewGroup parent) {
        super(browser, browser.getLayoutInflater().inflate(R.layout.chip_history_group, parent, false));
        itemView.setOnClickListener(onClickListener);
        itemView.setOnLongClickListener(onLongClickListener);
    }
    @Override
    public void bindDataToView(Item item, int position, int listType) {
        currentItem = item;
        TextView chipView = (TextView) itemView.findViewById(R.id.chip_view_history_group_tv);
        switch(item.typeId){
            /*case ItemType.BROWSING_HISTORY_LINK_GROUP: << moved to dedicated vh todo cleanup
                final int rDotDrawable = R.drawable.ic_list_hint_mini;
                final String titleWithIcon = "$LIST_ICON$ " + item.title;
                SpannableString spannableString = ThemeHelper.insertDrawableIntoTextString(rDotDrawable, titleWithIcon, "$LIST_ICON$", browser);
                chipView.setText(spannableString);
                //chipView.setDrawable
                break;*/
            default:
                chipView.setText(item.title);
                break;
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            browser.addressInput.clearFocus();
            if(currentItem==null){
                return;
            }
            final String domain = currentItem.address;
            browser.showSiteHomePageBtn(domain);
            browser.speedDialListAdapter.reloadCurrentList(ItemType.BROWSING_HISTORY_LINK, new ArrayList<Integer>(), domain);
            //browser.previewWindow.show(currentItem.address, "customurl", !browser.previewWindow.isOpen);
        }
    };

    View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            // todo centralise with HistoryViewHolder
            if(currentItem==null){
                return true;
            }
            if(currentItem.address!=null && UrlHelper.isUrlAttempt(currentItem.address)){
                final String domain = UrlHelper.getDomain(currentItem.address,true, true, false);
                AlertDialog.Builder builder = new AlertDialog.Builder(browser);
                AlertDialog dialog = builder
                        .setMessage(String.format(browser.getString(R.string.remove_DOMAIN_from_browsing_history), domain))
                        .setNegativeButton(R.string.yes_answer,  new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                BrowsingHistoryRepository.clearDomainHistory(domain, browser, browser.recentlyClosedTabAdapter);
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
            return true; // consume click
        }
    };

}
