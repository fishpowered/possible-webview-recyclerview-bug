package fishpowered.best.browser.speeddial.item;

import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import fishpowered.best.browser.db.ItemType;
import fishpowered.best.browser.utilities.StringHelper;
import fishpowered.best.browser.utilities.ThemeHelper;
import fishpowered.best.browser.utilities.UrlHelper;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.db.BrowsingHistoryRepository;
import fishpowered.best.browser.db.Item;

public class SpeedDialItemHistoryViewHolder extends AbstractSpeedDialItemViewHolder {
    private Item currentItem;

    public SpeedDialItemHistoryViewHolder(Browser browser, ViewGroup parent) {
        super(browser, browser.getLayoutInflater().inflate(R.layout.chip_history_link, parent, false));
        itemView.setOnClickListener(onClickListener);
        itemView.setOnLongClickListener(onLongClickListener);
    }
    @Override
    public void bindDataToView(Item item, int position, int listType) {
        currentItem = item;
        TextView titleText = (TextView) itemView.findViewById(R.id.history_link_text);
        titleText.setText(item.title);
        TextView timeText = (TextView) itemView.findViewById(R.id.history_link_time);
        timeText.setText(item.timePrefix);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            browser.addressInput.clearFocus();
            if(currentItem==null){
                return;
            }
            if(currentItem.typeId== ItemType.BROWSING_HISTORY_LINK_GROUP){
                browser.speedDialListAdapter.reloadCurrentList(ItemType.BROWSING_HISTORY_LINK, new ArrayList<Integer>(), currentItem.address);
            }else {
                browser.openAddressOrSearch(currentItem.address, true);
            }
            //browser.previewWindow.show(currentItem.address, "customurl", !browser.previewWindow.isOpen);
        }
    };

    View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if(currentItem==null){
                return true;
            }
            if(currentItem.address!=null && UrlHelper.isUrlAttempt(currentItem.address)){
                final String croppedAddress = StringHelper.cropToLength(currentItem.address, 150, "...");
                AlertDialog.Builder builder = new AlertDialog.Builder(browser);
                AlertDialog dialog = builder
                        .setMessage(croppedAddress)
                        .setNegativeButton(R.string.open,  new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                browser.openAddressOrSearch(currentItem.address, true);
                            }
                        })
                        .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                BrowsingHistoryRepository.deleteHistoryRecordById(browser, currentItem.id);
                                browser.speedDialListAdapter.reloadCurrentList(ItemType.BROWSING_HISTORY_LINK, new ArrayList<Integer>(), browser.speedDialDomainFilter);
                            }
                        })
                        .setPositiveButton(R.string.copy, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int id) {
                                Browser.copyToClipboard(browser, currentItem.address, String.format(browser.getString(R.string.address_copied)));
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
