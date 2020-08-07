package fishpowered.best.browser.speeddial.item;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import fishpowered.best.browser.Browser;
import fishpowered.best.browser.R;
import fishpowered.best.browser.db.Item;
import fishpowered.best.browser.db.ItemType;
import fishpowered.best.browser.utilities.UrlHelper;


/**
 * Represents clickable item in the speed dial list e.g. fave, history item etc
 */
public class SpeedDialItemChipImageHolder extends AbstractSpeedDialItemViewHolder {

    private Item currentItem;
    public SpeedDialItemChipImageHolder(Browser browser, ViewGroup parent) {
        super(browser, browser.getLayoutInflater().inflate(R.layout.chip_image, parent, false));
        itemView.setOnClickListener(onClickListener);
        itemView.setOnLongClickListener(onLongClickListener);
    }

    @Override
    public void bindDataToView(Item item, int position, int listType) {
        currentItem = item;
        ImageView imageView = (ImageView) itemView.findViewById(R.id.chip_image);
        if(item.isShowMoreLink){
            itemView.findViewById(R.id.chip_image_show_more_hint).setVisibility(View.VISIBLE);
        }else{
            itemView.findViewById(R.id.chip_image_show_more_hint).setVisibility(View.GONE);
        }
        boolean imageExistsAndLoadAttemptMade = false;
        try {
            String imageAddress = item.thumbAddress;
            if((imageAddress==null || item.thumbAddress.length()==0) && (item.typeId==ItemType.IMAGE || UrlHelper.isImage(item.address))){
                imageAddress = item.address;
            }
            if(UrlHelper.isUrlAttempt(imageAddress)) {
                if (UrlHelper.isUrlAttempt(imageAddress)) {
                    RequestOptions requestOptions = new RequestOptions()
                            .sizeMultiplier(0.9f)// makes the image low res without affecting fit
                            .placeholder(R.drawable.ic_loading_image);
                    Glide.with(browser).applyDefaultRequestOptions(requestOptions).load(imageAddress).into(imageView);
                    imageExistsAndLoadAttemptMade = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(item.typeId==ItemType.VIDEO && item.title!=null && item.title.length()>0){
            final TextView titleTextView = itemView.findViewById(R.id.chip_view_video_title);
            final String titleText = item.title!=null && item.title.length()>0 ? item.title : UrlHelper.getSimplifiedAddressForUseAsTitle(item.address, null, false);
            titleTextView.setText(titleText);
            titleTextView.setVisibility(View.VISIBLE);
        }
        if(!imageExistsAndLoadAttemptMade){
            imageView.setVisibility(View.GONE);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View chipView) {
            onSpeedDialItemClicked(currentItem, browser, chipView);
        }
    };

    View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            return onSpeedDialItemLongPressed(currentItem, browser, v);
        }
    };

    // Programmatically update the button padding to look like itäs been clicked
    public void updatePadding(View chipView, boolean pressed) {
        /*final int defaultLeft = UnitHelper.convertDpToPx(14, browser);
        final int defaultTop = UnitHelper.convertDpToPx(11, browser);
        final int defaultRight = UnitHelper.convertDpToPx(14, browser);
        final int defaultBottom = UnitHelper.convertDpToPx(12, browser);
        final int buttonMoveOnClick = UnitHelper.convertDpToPx(2f, browser); // @see chip_view_selector.xml!!
        if(pressed) {
            //chipView.setPadding(defaultLeft, defaultTop + buttonMoveOnClick, defaultRight, defaultBottom);
        }else{
            //chipView.setPadding(defaultLeft, defaultTop, defaultRight, defaultBottom);
        }*/
    }
}
