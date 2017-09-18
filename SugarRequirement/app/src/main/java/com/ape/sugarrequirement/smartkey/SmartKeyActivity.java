package com.ape.sugarrequirement.smartkey;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.ape.sugarrequirement.R;
import com.ape.sugarrequirement.util.Constant;

public class SmartKeyActivity extends Activity{

    private ActionBar mActionBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constant.KEYTAG,"onCreate");
        setContentView(R.layout.activity_smartkey);


        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getFragmentManager().beginTransaction().replace(R.id.sk_container,new SmartKeyFragment()).commit();

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
