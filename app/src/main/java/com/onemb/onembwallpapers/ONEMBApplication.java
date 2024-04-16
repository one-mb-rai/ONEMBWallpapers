package com.onemb.onembwallpapers;

import android.app.Application;

import androidx.lifecycle.ViewModelProvider;

import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel;

public class ONEMBApplication extends Application {
    private static ONEMBApplication instance;
    private WallpaperViewModel myViewModel;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        myViewModel = new ViewModelProvider.AndroidViewModelFactory(getInstance()).create(WallpaperViewModel.class);
    }

    public static ONEMBApplication getInstance() {
        return instance;
    }

    public WallpaperViewModel getWallpaperViewModel() {
        return myViewModel;
    }
}
