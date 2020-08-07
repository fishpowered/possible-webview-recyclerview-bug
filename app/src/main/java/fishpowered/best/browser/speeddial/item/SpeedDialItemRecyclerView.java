package fishpowered.best.browser.speeddial.item;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import fishpowered.best.browser.speeddial.itemadapters.AbstractItemAdapter;

import android.util.AttributeSet;

import com.google.android.flexbox.FlexboxLayoutManager;
import fishpowered.best.browser.Browser;

/**
 * Responsible for the vertical scrolling+loading of the speed dial lists
 */
public class SpeedDialItemRecyclerView extends RecyclerView {

    /**
     * changes the number of items loaded ahead, def 2
     */
    public final int speedDialListCacheSize = 2;
    private Browser browser;

    public SpeedDialItemRecyclerView(Context context) {
        super(context);
    }

    public SpeedDialItemRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SpeedDialItemRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false; // remove override if this causes problems with alpha transparency
    }

    public void initRecyclerView(final FlexboxLayoutManager speedDialListLayoutManager, AbstractItemAdapter abstractSpeedDialListAdapter, Browser browser){
        this.browser = browser;
        setLayoutManager(speedDialListLayoutManager);
        setAdapter(abstractSpeedDialListAdapter);
        setItemViewCacheSize(speedDialListCacheSize);
        setItemAnimator(new DefaultItemAnimator());

        // add pager behavior
        //PagerSnapHelper snapHelper = new PagerSnapHelper();
        //SpeedDialListSnapHelper snapHelper = new SpeedDialListSnapHelper();
        //snapHelper.attachToRecyclerView(this);
        //addOnItemTouchListener(onTouchListener); // use for swiping from edge check
    }
}
