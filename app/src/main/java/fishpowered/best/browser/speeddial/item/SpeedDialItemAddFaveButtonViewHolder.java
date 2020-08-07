package fishpowered.best.browser.speeddial.item;

/**

public class SpeedDialItemAddFaveButtonViewHolder extends AbstractSpeedDialItemViewHolder{
    public SpeedDialItemAddFaveButtonViewHolder(Browser browser, ViewGroup parent) {
        super(browser, browser.getLayoutInflater().inflate(R.layout.speed_dial_add_fave_button, parent, false));
        itemView.setOnClickListener(onAddButtonClick);
        itemView.setOnLongClickListener(onAddButtonLongClick);
    }

    @Override
    public void bindDataToView(Item item, int position, int listType) {
        TextView chipView = itemView.findViewById(R.id.speed_dial_fave_add_button);
        chipView.setText(item.title);
        //chipView.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_add_circle_green_16dp, 0, 0, 0);
    }

    private View.OnClickListener onAddButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String url;
            if(browser.addressInput.getText()==null){
                return;
            }
            url = browser.addressInput.getText().toString().trim();
            browser.addressInput.clearFocus();
            if(browser.isSearchingSpeedDialDatabase() && url.length() > 0){
                ItemRepository.save(browser, -1, url, null, 0,
                        true, true, ItemType.WEB_SITE, null, null, null);
                browser.speedDialListAdapter.reloadCurrentList(0, new ArrayList<Integer>(), null);
            } else {
                Intent newFavourite = new Intent(browser, EditItem.class);
                newFavourite.putExtra(EditItem.intentFaveAddress, url);
                newFavourite.putExtra(EditItem.intentFaveTag, "faves");
                browser.startActivity(newFavourite);
            }
        }
    };

    private View.OnLongClickListener onAddButtonLongClick = new View.OnLongClickListener() {

        /**
         * @param v The view that was clicked and held.
         * @return true if the callback consumed the long click, false otherwise.
         *
        @Override
        public boolean onLongClick(View v) {
            if(browser.addressInput.getText()==null){
                return true;
            }
            String url = browser.addressInput.getText().toString().trim();
            browser.addressInput.clearFocus();
            Intent newFavourite = new Intent(browser, EditItem.class);
            newFavourite.putExtra(EditItem.intentFaveAddress, url);
            newFavourite.putExtra(EditItem.intentFaveTag, "faves");
            browser.startActivity(newFavourite);
            return true;
        }
    };
}*/
