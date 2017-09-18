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
import com.ape.sugarrequirement.util.Utils;

public class SmartKeyStartActivity extends Activity{


    static int int_temp=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constant.KEYTAG,"onCreate");

        int_temp= Utils.isFirstRun(this);

        if(getReferrer() != null){
            Log.i(Constant.KEYTAG, "sugar key settings  from   getReferrer():" + getReferrer().toString());
        }
        if(int_temp!= 1 && getReferrer() != null && !getReferrer().toString().contains("com.android.settings")){
            dialog();
        }else{
            Intent intent = new Intent(SmartKeyStartActivity.this,SmartKeyActivity.class);
            startActivity(intent);

        }
        this.finish();


    }

    private void dialog(){
        Log.i("key","dialog");
        Intent intent = new Intent(SmartKeyStartActivity.this,SmartKeyService.class);
        Bundle bundle = new Bundle();
        bundle.putInt("first_run", 1);
        intent.putExtras(bundle);
        startService(intent);
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
