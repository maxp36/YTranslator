package com.development.maxp36.ytranslator;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.development.maxp36.ytranslator.Fragments.FavoritesFragment;
import com.development.maxp36.ytranslator.Fragments.HistoryFragment;


public class PagerAdapter extends FragmentPagerAdapter {

    private int mNumOfTabs;

    private String historyFragmentTag;
    private String favoritesFragmentTag;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new HistoryFragment();
            case 1:
                return new FavoritesFragment();
            default:
                return null;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);

        switch (position) {
            case 0:
                historyFragmentTag = createdFragment.getTag();
                break;
            case 1:
                favoritesFragmentTag = createdFragment.getTag();
                break;
        }
        return createdFragment;
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }


    public String getHistoryFragmentTag() {
        return historyFragmentTag;
    }
    public String getFavoritesFragmentTag() {
        return favoritesFragmentTag;
    }
}
