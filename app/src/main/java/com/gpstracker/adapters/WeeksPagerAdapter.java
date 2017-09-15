package com.gpstracker.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.gpstracker.R;
import com.gpstracker.fragments.WeekFragment;

/**
 * Created by RGarai on 1.9.2016.
 */
public class WeeksPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;
    private String tabTitles[];
    private Context context;

    public WeeksPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        this.tabTitles  = new String[] {
                context.getString(R.string.week_all),
                context.getString(R.string.week_this),
                context.getString(R.string.week_past)
        };

    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {

        return WeekFragment.newInstance(position + 1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
