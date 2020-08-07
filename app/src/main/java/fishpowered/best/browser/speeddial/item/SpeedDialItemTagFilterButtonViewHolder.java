package fishpowered.best.browser.speeddial.item;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.db.Item;
import fishpowered.best.browser.db.ItemType;
import fishpowered.best.browser.db.TagRepository;
import fishpowered.best.browser.utilities.ArrayListHelper;
import fishpowered.best.browser.utilities.ThemeHelper;

/**
 * Button for showing tag name and filtering faves by tag when clicked
 */
public class SpeedDialItemTagFilterButtonViewHolder extends AbstractSpeedDialItemViewHolder {

    private int tagId = 0;
    TextView label;
    private String tagName;

    public SpeedDialItemTagFilterButtonViewHolder(Browser browser, ViewGroup parent) {
        super(browser, browser.getLayoutInflater().inflate(R.layout.speed_dial_list_tag_filter_button, parent, false));
        itemView.setOnClickListener(this.onClickListener);
        itemView.setOnLongClickListener(this.onLongClickListener);
        label = itemView.findViewById(R.id.tag_filter_btn_text_view);
    }

    @Override
    public void bindDataToView(Item item, int position, int listType) {
        label.setText(item.title);
        this.tagId = Integer.parseInt(item.address+"");
        this.tagName = item.title;
        if(item.isSelected){
            label.setBackgroundColor(ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.speedDialListFaveTheme, browser));
        }else{
            label.setBackgroundColor(ThemeHelper.getColourFromCurrentThemeAttribute(R.attr.speedDialTagBackground, browser));
        }
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            browser.addressInput.clearFocus();

            ArrayList<Integer> filterByTagIds = new ArrayList<Integer>();
            if(browser.isViewingExpandedSpeedDialListOfType!=0){
                filterByTagIds = browser.filterFavesByTagIds;
            }
            if(filterByTagIds.contains(tagId)){
                ArrayListHelper.removeIntValue(filterByTagIds, tagId);
            }else{
                filterByTagIds.add(tagId);
            }
            final int listFilter = browser.isViewingExpandedSpeedDialListOfType !=0 ? browser.isViewingExpandedSpeedDialListOfType : ItemType.TAG_FILTER;
            browser.speedDialListAdapter.reloadCurrentList(listFilter, filterByTagIds, null);
        }
    };

    private final View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            browser.addressInput.clearFocus();
            Runnable updateListOnSuccess = new Runnable() {
                @Override
                public void run() {
                    browser.speedDialListAdapter.reloadCurrentList(0, new ArrayList<Integer>(), null);
                }
            };
            TagRepository.askToEditOrRemoveTag(browser, tagId, String.format(browser.getString(R.string.edit_tag_xxxx), label.getText()), updateListOnSuccess, label);
            return true; // consume long click
        }
    };
}
