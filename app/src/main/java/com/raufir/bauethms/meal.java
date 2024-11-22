package com.raufir.bauethms;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

public class meal extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MealPagerAdapter pagerAdapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal);

        // Initialize Views
        viewPager = findViewById(R.id.viewPager);
        progressBar = findViewById(R.id.progressBar);

        // Show ProgressBar while loading
        showLoading();

        // Set up ViewPager2 for swiping between months
        pagerAdapter = new MealPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Set current month (index 1) to be displayed initially
        viewPager.setCurrentItem(pagerAdapter.getCurrentMonthIndex());

        // Hide ProgressBar after data is set in the adapter
        pagerAdapter.setDataLoadListener(new MealPagerAdapter.DataLoadListener() {
            @Override
            public void onDataLoaded() {
                hideLoading();
            }
        });

        // Handle edge-to-edge window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }
}