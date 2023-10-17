package com.example.puertacovid.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.puertacovid.Fragments.ChatFragment;
import com.example.puertacovid.Fragments.PeopleFragment;

public class MyviewPagerAdapter extends FragmentStateAdapter {
    public MyviewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position==0)
            return ChatFragment.getInstance();
        else
            return PeopleFragment.getInstance();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
