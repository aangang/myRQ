package com.ape.sugarrequirement.camera.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.View;

import com.ape.sugarrequirement.R;

public class SettingsActivity extends Activity{

    private ActionBar mActionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("cam","onCreate");
        setContentView(R.layout.activity_settings);
        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
    @Override
    public boolean onNavigateUp() {
        this.finish();
        return super.onNavigateUp();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
