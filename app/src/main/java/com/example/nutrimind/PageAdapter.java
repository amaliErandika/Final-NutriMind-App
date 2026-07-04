package com.example.nutrimind;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import Fragments.Meditation;
import Fragments.Tips;

public class PageAdapter extends FragmentStateAdapter {
    public PageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Meditation();
            case 1:
                return new Tips();
            default:
                return new Meditation();


        }

    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

