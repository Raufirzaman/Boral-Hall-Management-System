package com.raufir.bauethms;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MealPagerAdapter extends FragmentStateAdapter {

    private DataLoadListener dataLoadListener;
    private boolean isDataLoaded = false;
    private final FragmentActivity fragmentActivity; // Store the activity reference

    public MealPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
        this.fragmentActivity = fa; // Initialize the activity reference
        loadData(); // Start loading data when the adapter is created
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Create a new fragment for each month (-1 = previous, 0 = current, 1 = next)
        return MealMonthFragment.newInstance(position - 1);
    }

    @Override
    public int getItemCount() {
        return 3;  // Number of pages: previous, current, and next month
    }

    public int getCurrentMonthIndex() {
        return 1;  // Return 1 for the current month (middle page)
    }

    public void setDataLoadListener(DataLoadListener listener) {
        this.dataLoadListener = listener;
        // Notify listener immediately if data is already loaded
        if (isDataLoaded && dataLoadListener != null) {
            dataLoadListener.onDataLoaded();
        }
    }

    private void loadData() {
        // Simulate data loading (this should be replaced with your actual data loading logic)
        new Thread(() -> {
            try {
                // Simulating a delay for data loading
                Thread.sleep(2000); // Simulate data loading delay
                isDataLoaded = true;

                // Notify that data has been loaded on the main thread
                if (dataLoadListener != null) {
                    fragmentActivity.runOnUiThread(() -> dataLoadListener.onDataLoaded());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public interface DataLoadListener {
        void onDataLoaded();
    }
}