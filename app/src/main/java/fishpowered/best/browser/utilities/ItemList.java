package fishpowered.best.browser.utilities;

import fishpowered.best.browser.db.Item;
import java.util.ArrayList;

/**
 * ArrayList for speed dial list items (faves, headings etc)
 */
public class ItemList extends ArrayList<Item> {

    @Override
    public boolean add(Item item) {
        if(item==null){
            return false;
        }
        return super.add(item);
    }

    /**
     * Appends the specified element to the end of this list (UNLESS IT ALREADY EXISTS IN THE LIST)
     */
    public boolean addIfAddressNotExists(Item item) {
        if(item==null || item.address==null){
            return false;
        }
        boolean alreadyExists = false;
        for(int n=0; n<this.size(); n++){
            Item compare = this.get(n);
            if(compare.address!=null && compare.address.trim().equalsIgnoreCase(item.address.trim())){
                alreadyExists = true;
                break;
            }
        }
        return !alreadyExists && super.add(item);
    }

    /**
     * Appends the specified element to the end of this list (UNLESS IT ALREADY EXISTS IN THE LIST)
     */
    public boolean addIfDomainNotExists(Item item) {
        if(item==null || item.address==null){
            return false;
        }
        boolean alreadyExists = false;
        String compareDomain;
        String itemDomain;
        for(int n=0; n<this.size(); n++){
            Item compare = this.get(n);
            if(compare.address!=null){
                compareDomain = UrlHelper.getDomain(compare.address, true, true, false);
                itemDomain = UrlHelper.getDomain(item.address, true, true, false);
                if(compareDomain!=null && compareDomain.equalsIgnoreCase(itemDomain)) {
                    alreadyExists = true;
                    break;
                }
            }
        }
        return !alreadyExists && super.add(item);
    }

    public boolean containsAddress(String address) {
        if(address==null){
            return false;
        }
        for(int n=0; n<this.size(); n++){
            Item compare = this.get(n);
            if(compare.address!=null){
                String compareAddress = UrlHelper.isUrlAttempt(compare.address)
                        ? UrlHelper.getTrustedAddress(compare.address)
                        : compare.address.trim();
                if(compareAddress.equalsIgnoreCase(address.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
}
