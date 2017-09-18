package com.ape.sugarrequirement.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.ape.sugarrequirement.smartkey.SmartKeyStartActivity;

/**
 * Created by android on 8/1/17.
 */

public class Utils {

    public static int isFirstRun(Context context){
        SharedPreferences mSharedPreferences;
        SharedPreferences.Editor mEditor;
        mSharedPreferences = context.getSharedPreferences(Constant.FRIST_SAVE, Activity.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        return mSharedPreferences.getInt(Constant.IS_FRIST_RUN,0);
    }

    public static void setFirstRun(Context context){
        SharedPreferences mSharedPreferences;
        SharedPreferences.Editor mEditor;
        mSharedPreferences = context.getSharedPreferences(Constant.FRIST_SAVE, Activity.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mEditor.putInt(Constant.IS_FRIST_RUN,1);
        mEditor.commit();
    }

}
