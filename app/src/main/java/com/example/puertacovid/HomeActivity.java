package com.example.puertacovid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.puertacovid.Adapter.MyviewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity {
    @BindView(R.id.tabDots)
    TabLayout tabLayout;

    @BindView(R.id.view_pager)
    ViewPager2 viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init2();
        setupViewpager();
    }

    private void setupViewpager() {
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(new MyviewPagerAdapter(getSupportFragmentManager(), new Lifecycle() {
            @Override
            public void addObserver(@NonNull @org.jetbrains.annotations.NotNull LifecycleObserver observer) {

            }

            @Override
            public void removeObserver(@NonNull @org.jetbrains.annotations.NotNull LifecycleObserver observer) {

            }

            @NonNull
            @org.jetbrains.annotations.NotNull
            @Override
            public State getCurrentState() {
                return null;
            }
        }));
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if(position==0)
                tab.setText("Chat");
            else

                tab.setText("People");
        }).attach();
    }

    private void init2() {
        ButterKnife.bind(this);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu2, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.inicio){
            Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
            startActivity(intent);
        }else if (id == R.id.salir){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.cuenta){
            Intent intent = new Intent(HomeActivity.this, Cuenta.class);
            startActivity(intent);
        }else if (id == R.id.videocall){
            Intent intent = new Intent(HomeActivity.this, Mensaje_advertencia.class);
            startActivity(intent);
        }else if (id == R.id.peopleday){
            Intent intent = new Intent(HomeActivity.this, Registro_dia.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}