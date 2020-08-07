package fishpowered.best.browser.speeddial.item;

import android.text.SpannableString;
import android.view.ViewGroup;
import android.widget.TextView;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.db.Item;
import fishpowered.best.browser.utilities.ThemeHelper;

public class SpeedDialItemListHintViewHolder extends AbstractSpeedDialItemViewHolder {

    public SpeedDialItemListHintViewHolder(Browser browser, ViewGroup parent) {
        super(browser, browser.getLayoutInflater().inflate(R.layout.speed_dial_list_hint, parent, false));
    }

    @Override
    public void bindDataToView(Item item, int position, int listType) {
        TextView label = itemView.findViewById(R.id.speed_dial_list_hint_tv);
            /*ViewGroup.LayoutParams lp = layout.getLayoutParams();
            if (lp instanceof FlexboxLayoutManager.LayoutParams) {
                // Make the header full width
                ((FlexboxLayoutManager.LayoutParams) lp).setFlexBasisPercent(100f);
            }*/

        final String text = item.title + "";
        if(item.title!=null && item.title.contains("$ADD_TO_READ_LATER_ICON$")) {
            // Insert icon into string
            final int rDotDrawable = R.drawable.ic_context_menu_read_later_add_hint;
            SpannableString spannableString = ThemeHelper.insertDrawableIntoTextString(rDotDrawable, text, "$ADD_TO_READ_LATER_ICON$", browser);
            label.setText(spannableString);
        }else if(item.title!=null && item.title.contains("$ADD_TO_FAVES_ICON$")){
            // Insert icon into string
            final int rDotDrawable = R.drawable.ic_add_fave_hint;
            SpannableString spannableString = ThemeHelper.insertDrawableIntoTextString(rDotDrawable, text, "$ADD_TO_FAVES_ICON$", browser);
            label.setText(spannableString);
        }else {
            label.setText(text);
        }
    }
}
