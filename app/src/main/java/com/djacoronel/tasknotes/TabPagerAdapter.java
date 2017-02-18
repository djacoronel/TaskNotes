package com.djacoronel.tasknotes;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.view.ViewGroup;

class TabPagerAdapter extends FragmentPagerAdapter {
    private int tabCount;
    private Tab1Fragment tab1 = new Tab1Fragment();
    private Tab2Fragment tab2 = new Tab2Fragment();

    TabPagerAdapter(FragmentManager fm, int numberOfTabs) {
        super(fm);
        this.tabCount = numberOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return tab1;
            case 1:
                return tab2;
            default:
                return null;
        }
    }

    Tab1Fragment getTab1() {
        return tab1;
    }

    Tab2Fragment getTab2() {
        return tab2;
    }

    @Override
    public int getCount() {
        return tabCount;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }
}
