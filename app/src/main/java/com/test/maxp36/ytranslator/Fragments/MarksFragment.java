package com.test.maxp36.ytranslator.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.test.maxp36.ytranslator.PagerAdapter;
import com.test.maxp36.ytranslator.R;


public class MarksFragment extends Fragment {

    private PagerAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.marks_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("История"));
        tabLayout.addTab(tabLayout.newTab().setText("Избранное"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);

        final ViewPager viewPager = (ViewPager)getActivity().findViewById(R.id.pager);
        adapter = new PagerAdapter(getChildFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        AppCompatImageButton btnRemoveItems = (AppCompatImageButton)getActivity().findViewById(R.id.button_remove_items);
        btnRemoveItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() == 0) {
                    HistoryFragment fragment = (HistoryFragment) getChildFragmentManager().findFragmentByTag(adapter.getHistoryFragmentTag());
                    if (fragment != null) {
                        fragment.removeItems();
                    }
                } else {
                    FavoritesFragment fragment = (FavoritesFragment) getChildFragmentManager().findFragmentByTag(adapter.getFavoritesFragmentTag());
                    if (fragment != null) {
                        fragment.removeItems();
                    }
                }
            }
        });
    }

    public PagerAdapter getAdapter() {
        return adapter;
    }
}
