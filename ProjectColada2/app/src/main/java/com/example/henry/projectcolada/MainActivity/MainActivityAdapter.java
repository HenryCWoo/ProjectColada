package com.example.henry.projectcolada.MainActivity;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.henry.projectcolada.MainActivity.Recipe.RecipeFragment;

/**
 * Created by h3nry on 4/20/2018.
 */

public class MainActivityAdapter extends FragmentPagerAdapter {
    private Context mContext;

    public MainActivityAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    // determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
//        if(position == 0) {
//            return new PeopleFragment();
//        } else if (position == 1) {
//            return new RecipeFragment();
//        } else {
//            return new AIFragment();
//        }
        if (position == 0) {
            return new RecipeFragment();
        } else {
            return new AIFragment();
        }
    }

    // determines number of tabs
    @Override
    public int getCount() {
//        return 3;
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(position) {
//            case 0:
//                return "People";
//            case 1:
//                return "Recipes";
//            case 2:
//                return "AI";
            case 0:
                return "Recipes";
            case 1:
                return "AI";
            default: return null;
        }
    }
}
