package fishpowered.best.browser;

/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.viewpager.widget.ViewPager;

/**
 * Implementation of the {@link SnapHelper} supporting pager style snapping in either vertical or
 * horizontal orientation.
 *
 * <p>
 *
 * PagerSnapHelper can help achieve a similar behavior to {@link ViewPager}.
 * Set both {@link RecyclerView} and the items of the
 * {@link RecyclerView.Adapter} to have
 * {@link android.view.ViewGroup.LayoutParams#MATCH_PARENT} height and width and then attach
 * PagerSnapHelper to the {@link RecyclerView} using {@link #attachToRecyclerView(RecyclerView)}.
 */
public class TabSnapHelper extends PagerSnapHelper {
    private static final int MAX_SCROLL_ON_FLING_DURATION = 500; // ms

    @Override
    public boolean onFling(int velocityX, int velocityY) {
        return super.onFling(velocityX, velocityY);
    }

}

