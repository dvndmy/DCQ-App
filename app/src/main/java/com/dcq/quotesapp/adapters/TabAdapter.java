package com.dcq.quotesapp.adapters;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.dcq.quotesapp.AddQuoteFragment;
import com.dcq.quotesapp.CategoryFragment;

public class TabAdapter extends FragmentPagerAdapter {

    Context context;
    int totalTabs;

    public TabAdapter(Context c, FragmentManager fm, int totalTabs) {
        super(fm);
        context = c;
        this.totalTabs = totalTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                CategoryFragment homeFragment = new CategoryFragment();
                return homeFragment;
            case 1:
                AddQuoteFragment photoFragment = new AddQuoteFragment();
                return photoFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return totalTabs;
    }
}
