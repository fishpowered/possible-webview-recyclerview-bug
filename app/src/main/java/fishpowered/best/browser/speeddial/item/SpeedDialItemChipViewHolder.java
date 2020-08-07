package fishpowered.best.browser.speeddial.item;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.db.Item;
import fishpowered.best.browser.db.ItemType;
import fishpowered.best.browser.utilities.FileHelper;
import fishpowered.best.browser.utilities.ThemeHelper;

/**
 * Represents clickable item in the speed dial list e.g. fave, history item etc
 */
public class SpeedDialItemChipViewHolder extends AbstractSpeedDialItemViewHolder {

    private Item currentItem;
    public SpeedDialItemChipViewHolder(Browser browser, ViewGroup parent) {
        super(browser, browser.getLayoutInflater().inflate(R.layout.chip_view, parent, false));
        itemView.setOnClickListener(onClickListener);
        //itemView.setOnTouchListener(onTouchListener);
        itemView.setOnLongClickListener(onLongClickListener);
    }

    @Override
    public void bindDataToView(Item item, int position, int listType) {
        currentItem = item;
        TextView chipView = (TextView) itemView.findViewById(R.id.chip_view);
        if(item.readLaterStatus!=null && item.readLaterStatus==Item.ITEM_IS_SAVED_FOR_LATER_AND_HAS_BEEN_READ){
            chipView.setText("✔ "+item.title);
        }else{
            chipView.setText(item.title);
        }
        if (item.typeId == ItemType.FAVE_QUOTE) {
            chipView.setSingleLine(false);
            chipView.setTypeface(chipView.getTypeface(), Typeface.ITALIC);
        }
        if (item.isShowMoreLink) {
            itemView.findViewById(R.id.chip_view_show_more_hint).setVisibility(View.VISIBLE);
        } else {
            itemView.findViewById(R.id.chip_view_show_more_hint).setVisibility(View.GONE);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View chipView) {
            onSpeedDialItemClicked(currentItem, browser, chipView);
        }
    };

    private void promptIfUserShouldReadOfflineVersion(final Runnable viewPageOffline, final Runnable viewPageOnline) {
        // todo check offline archive exists
        if(!FileHelper.fileExistsInFilesDirectory(browser, FileHelper.getFilePathForSavingWebArchive(currentItem.id, browser, false))){
            viewPageOnline.run();
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(browser);
        String message = "";
        builder.setTitle(browser.getString(R.string.view_offline_version)); // +" (#"+error.getPrimaryError()+")."
        builder.setMessage(message);
        builder.setNegativeButton(browser.getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewPageOffline.run();
            }
        });
        builder.setPositiveButton(browser.getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewPageOnline.run();
            }
        });
        final AlertDialog dialog = builder.create();
        ThemeHelper.styleAlertDialog(dialog, R.drawable.ic_offline_mode, browser);
        dialog.show();
    }

    View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            return onSpeedDialItemLongPressed(currentItem, browser, v);
        }
    };

    private final View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            return false;
        }
    };

}
