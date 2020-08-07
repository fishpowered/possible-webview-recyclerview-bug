package fishpowered.best.browser.utilities;
import fishpowered.best.browser.db.Item;
import java.util.Comparator;

/**
 * Useful for sorting array lists of Items by title
 */
public class ItemTitleComparator implements Comparator<Item>
{

    private final String searchFilter;

    public ItemTitleComparator(String searchFilter){

        this.searchFilter = searchFilter;
    }

    public int compare(Item left, Item right) {
        final String leftTitle = left!=null ? left.title : "";
        final String rightTitle = right!=null ? right.title : "";
        if(searchFilter!=null && searchFilter.length() > 0){
            final boolean leftHasSearchAtStart = leftTitle.toLowerCase().startsWith(searchFilter.toLowerCase());
            final boolean rightHasSearchAtStart = rightTitle.toLowerCase().startsWith(searchFilter.toLowerCase());
            if(leftHasSearchAtStart && !rightHasSearchAtStart){
                return -1;
            }
            if(!leftHasSearchAtStart && rightHasSearchAtStart){
                return 1;
            }
        }
        return leftTitle.compareToIgnoreCase(rightTitle);
    }
}
