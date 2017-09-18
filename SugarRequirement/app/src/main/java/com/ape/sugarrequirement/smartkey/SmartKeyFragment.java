package com.ape.sugarrequirement.smartkey;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseBooleanArray;

import com.ape.sugarrequirement.R;
import com.ape.sugarrequirement.util.Constant;
import com.ape.sugarrequirement.widget.SugarListActivity;
import com.ape.sugarrequirement.widget.SugarListPreferenceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by android on 5/10/17.
 */

public class SmartKeyFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener,
                                                      Preference.OnPreferenceClickListener {
    private Context mContext;

    private static final String KEY_SMART_KEY_TOUCH_MODE = "smart_key_touch_mode";
    private static final String KEY_SMART_KEY_START_MODE = "smart_key_start_mode";

    private static final String KEY_RECORDING_ENABLE = "sugar_recording_enable";
    private static final String KEY_VOICE_RECORDING = "sugar_voice_recording";
    private static final String KEY_CLICK_ANSWER_THE_PHONE = "sugar_click_answer_the_phone";
    private static final String KEY_FALSE_TOUCH = "sugar_false_touch";

    private Preference mSugarKeyTouchModeList;
    private Preference mSugarKeyStartModeList;
    private SwitchPreference mFalseTouchPref;
    private SwitchPreference mAnswerPhonePref;
    private SwitchPreference mVoiceRecordPref;
    private SwitchPreference mRecordEnablePref;

    private static int touchModeIndex;
    private static int startModeIndex;

    private static final int touchModeRequest = 1;
    private static final int startModeRequest = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

        addPreferencesFromResource(R.xml.smart_key_prefs);

        ApplicationInfo app = new ApplicationInfo();
        String a = app.packageName;
        initUI();

    }
    private boolean isVoiceEnable(){
        /*Intent intent = new Intent();
        intent.setClassName("com.myos.speechassist", "com.myos.speechassist.view.MainActivity");
        return (mContext.getPackageManager().resolveActivity(intent, 0) != null);*/
        /*
        if(!SystemProperties.get("ro.project").equals("soap_cn")){
            return true;
        }else{
            return false;
        }
        */
        return false;
    }
    private boolean isTWproject(){
        if((SystemProperties.get("ro.project").equals("sug_tw")
                || SystemProperties.get("ro.project").equals("sug_hk"))){
            return true;
        }else{
            return false;
        }
    }
    private void initUI() {
        mSugarKeyTouchModeList = (Preference) findPreference(KEY_SMART_KEY_TOUCH_MODE);
        //mSugarKeyTouchModeList.setOnPreferenceChangeListener(this);
        mSugarKeyTouchModeList.setOnPreferenceClickListener(this);

        mSugarKeyStartModeList = (Preference) findPreference(KEY_SMART_KEY_START_MODE);
        //mSugarKeyStartModeList.setOnPreferenceChangeListener(this);
        mSugarKeyStartModeList.setOnPreferenceClickListener(this);

        if(Settings.System.getInt(getActivity().getContentResolver(),Constant.SUGAR_KEY_TOUCH_MODE_STATUS, 1)==0){
            touchModeIndex=0;
            mSugarKeyStartModeList.setEnabled(true);
        }else if(Settings.System.getInt(getActivity().getContentResolver(),Constant.SUGAR_KEY_TOUCH_MODE_STATUS, 1)==1){
            touchModeIndex=1;
            mSugarKeyStartModeList.setEnabled(true);
        }else if(Settings.System.getInt(getActivity().getContentResolver(),Constant.SUGAR_KEY_TOUCH_MODE_STATUS, 1)==2){
            touchModeIndex=2;
            mSugarKeyStartModeList.setEnabled(false);
        }
        //mSugarKeyTouchModeList.setValueIndex(touchModeIndex);
        final Resources res = getResources();
        String[] touchmode = res.getStringArray(R.array.sugar_key_mode_list);
        mSugarKeyTouchModeList.setSummary(String.format(res.getString(R.string.summary_double_touch),
                touchmode[touchModeIndex]));

        if(Settings.System.getInt(getActivity().getContentResolver(),Constant.SUGAR_KEY_START_MODE_STATUS, 0)==0){
            startModeIndex=0;
        }else if(Settings.System.getInt(getActivity().getContentResolver(),Constant.SUGAR_KEY_START_MODE_STATUS, 0)==1){
            startModeIndex=1;
        }else if(Settings.System.getInt(getActivity().getContentResolver(),Constant.SUGAR_KEY_START_MODE_STATUS, 0)==2){
            startModeIndex=2;
        }else if(Settings.System.getInt(getActivity().getContentResolver(),Constant.SUGAR_KEY_START_MODE_STATUS, 0)==3){
            startModeIndex=3;
        }else if(Settings.System.getInt(getActivity().getContentResolver(),Constant.SUGAR_KEY_START_MODE_STATUS, 0)==4){
            startModeIndex=4;
        }else if(Settings.System.getInt(getActivity().getContentResolver(),Constant.SUGAR_KEY_START_MODE_STATUS, 0)==5){
            startModeIndex=5;
        }else if(Settings.System.getInt(getActivity().getContentResolver(),Constant.SUGAR_KEY_START_MODE_STATUS, 0)==6){
            startModeIndex=6;
        }else if(Settings.System.getInt(getActivity().getContentResolver(),Constant.SUGAR_KEY_START_MODE_STATUS, 0)==7){
            startModeIndex=7;
        }

        //mSugarKeyStartModeList.setValueIndex(startModeIndex);
        String[] startmode = res.getStringArray(R.array.double_touch_list_alipay_voice);
        mSugarKeyStartModeList.setSummary(String.format(res.getString(R.string.summary_double_touch),
                startmode[startModeIndex]));
        Log.i(Constant.KEYTAG, "SUGAR_KEY_TOUCH_MODE_STATUS :" + touchModeIndex);
        Log.i(Constant.KEYTAG, "SUGAR_KEY_START_MODE_STATUS :" + startModeIndex);

        mFalseTouchPref = (SwitchPreference) findPreference(KEY_FALSE_TOUCH);
        if(Settings.System.getInt(getActivity().getContentResolver(),Constant.FALSE_TOUCH_STATUS, 1)==0){
            mFalseTouchPref.setChecked(false);
        }else{
            mFalseTouchPref.setChecked(true);
        }
        mAnswerPhonePref = (SwitchPreference) findPreference(KEY_CLICK_ANSWER_THE_PHONE);
        if(Settings.System.getInt(getActivity().getContentResolver(),Constant.CLICK_ANSWER_THE_PHONE_STATUS, 1)==0){
            mAnswerPhonePref.setChecked(false);
        }else{
            mAnswerPhonePref.setChecked(true);
        }
        mVoiceRecordPref = (SwitchPreference) findPreference(KEY_VOICE_RECORDING);
        if(Settings.System.getInt(getActivity().getContentResolver(),Constant.VOICE_RECORDING_STATUS, 1)==0){
            mVoiceRecordPref.setChecked(false);
        }else{
            mVoiceRecordPref.setChecked(true);
        }
        mRecordEnablePref = (SwitchPreference) findPreference(KEY_RECORDING_ENABLE);
        if(Settings.System.getInt(getActivity().getContentResolver(),Constant.RECORDING_ENABLE_STATUS, 1)==0){
            mRecordEnablePref.setChecked(false);
        }else{
            mRecordEnablePref.setChecked(true);
        }

        mAnswerPhonePref.setOnPreferenceChangeListener(this);
        mVoiceRecordPref.setOnPreferenceChangeListener(this);
        mRecordEnablePref.setOnPreferenceChangeListener(this);
        mFalseTouchPref.setOnPreferenceChangeListener(this);

    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        Log.i(Constant.KEYTAG, "onPreferenceChange changed:" + key);

        if (KEY_CLICK_ANSWER_THE_PHONE.equals(key)) {
            Log.i(Constant.KEYTAG, "KEY_CLICK_ANSWER_THE_PHONE changed");
            Settings.System.putInt(getActivity().getContentResolver(),Constant.CLICK_ANSWER_THE_PHONE_STATUS,
                                        mAnswerPhonePref.isChecked() ? 0 : 1);
        }
        if (KEY_VOICE_RECORDING.equals(key)) {
            Log.i(Constant.KEYTAG, "KEY_VOICE_RECORDING changed");
            Settings.System.putInt(getActivity().getContentResolver(),Constant.VOICE_RECORDING_STATUS,
                    mVoiceRecordPref.isChecked() ? 0 : 1);
        }
        if (KEY_RECORDING_ENABLE.equals(key)) {
            Log.i(Constant.KEYTAG, "KEY_RECORDING_ENABLE changed");
            Settings.System.putInt(getActivity().getContentResolver(),Constant.RECORDING_ENABLE_STATUS,
                    mRecordEnablePref.isChecked() ? 0 : 1);
        }
        if (KEY_FALSE_TOUCH.equals(key)) {
            Log.i(Constant.KEYTAG, "KEY_FALSE_TOUCH changed mFalseTouchPref.isChecked():" + mFalseTouchPref.isChecked());
            Settings.System.putInt(getActivity().getContentResolver(),Constant.FALSE_TOUCH_STATUS,
                    mFalseTouchPref.isChecked() ? 0 : 1);
        }


        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        final String key = preference.getKey();
        Log.i(Constant.KEYTAG, "onPreferenceClick key:" + key);
        if (preference.getKey().equals(KEY_SMART_KEY_TOUCH_MODE)) {

            SugarListPreferenceInfo info = new SugarListPreferenceInfo();
            SparseBooleanArray mCheckArray= new SparseBooleanArray();
            mCheckArray.append(touchModeIndex, true);
            info.setEntriesId(R.array.sugar_key_mode_list);
            info.setCheckArray(mCheckArray);
            info.setRequestCode(touchModeRequest);
            startFragment(info,false);

        }
        if (preference.getKey().equals(KEY_SMART_KEY_START_MODE)) {

            SugarListPreferenceInfo info = new SugarListPreferenceInfo();
            SparseBooleanArray mCheckArray= new SparseBooleanArray();
            mCheckArray.append(startModeIndex, true);
            if(!isTWproject()) {
                if (isVoiceEnable()) {
                    Log.i(Constant.KEYTAG, "onPreferenceClick com.myos.speechassist exist");
                    info.setEntriesId(R.array.double_touch_list_alipay_voice);
                } else {
                    Log.i(Constant.KEYTAG, "onPreferenceClick com.myos.speechassist not exist");
                    info.setEntriesId(R.array.double_touch_list_alipay);
                }
            }else{
                Log.i(Constant.KEYTAG, "onPreferenceClick tw version remove alipay mm");
                info.setEntriesId(R.array.double_touch_list_tw);
            }
            info.setCheckArray(mCheckArray);
            info.setRequestCode(startModeRequest);
            startFragment(info,false);

        }



        return false;
    }


    private void startFragment(SugarListPreferenceInfo info, boolean multiCheck) {
        List<String> list = info.getEntriesList();
        if (list != null && list.size() == 1 && list.get(0) =="") {
            return;
        }

        Bundle extras = new Bundle();
        extras.putParcelable(SugarListActivity.LIST_KEY, info);
        extras.putBoolean(SugarListActivity.MULTI_CHECK, multiCheck);

        Intent intent = new Intent("com.android.smart_key.list.choose");
        intent.putExtras(extras);
        startActivityForResult(intent,info.getRequestCode());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            ArrayList<Integer> resultPosition = data.getIntegerArrayListExtra(SugarListActivity.RESULT_CHECKED);
            Log.i(Constant.KEYTAG, "onActivityResult resultPosition:" + resultPosition);
            if(requestCode == touchModeRequest){
                final Resources res1 = getResources();
                String[] touchmode = res1.getStringArray(R.array.sugar_key_mode_list);
                Log.i(Constant.KEYTAG, "onActivityResult touchModeRequest  resultPosition:" + resultPosition.get(0));
                if(resultPosition.get(0) == 0 || resultPosition.get(0) == 1){
                    mSugarKeyStartModeList.setEnabled(true);
                }else if(resultPosition.get(0) == 2){
                    mSugarKeyStartModeList.setEnabled(false);
                }
                Settings.System.putInt(getActivity().getContentResolver(), Constant.SUGAR_KEY_TOUCH_MODE_STATUS, resultPosition.get(0));
                touchModeIndex = resultPosition.get(0);
                mSugarKeyTouchModeList.setSummary(String.format(res1.getString(R.string.summary_double_touch),
                        touchmode[touchModeIndex]));
            }else if(requestCode == startModeRequest){
                final Resources res1 = getResources();
                String[] startmode = res1.getStringArray(R.array.double_touch_list_alipay_voice);
                Log.i(Constant.KEYTAG, "onActivityResult startModeRequest  resultPosition:" + resultPosition.get(0));
                Settings.System.putInt(getActivity().getContentResolver(), Constant.SUGAR_KEY_START_MODE_STATUS, resultPosition.get(0));
                startModeIndex = resultPosition.get(0);
                mSugarKeyStartModeList.setSummary(String.format(res1.getString(R.string.summary_double_touch),
                        startmode[startModeIndex]));
                if(startModeIndex == 5){//weixin saoyisao
                    mmNeedLoginDialog();
                }
                if(startModeIndex == 6){//weixin saoyisao
                    alipayNeedLoginDialog();
                }
            }
        }

    }

    private void mmNeedLoginDialog(){
        Log.i("key","mmNedLoginDialog");
        Intent intent = new Intent(getActivity(),SmartKeyService.class);
        Bundle bundle = new Bundle();
        bundle.putInt("mm_need_login", 1);
        intent.putExtras(bundle);
        getActivity().startService(intent);
    }

    private void alipayNeedLoginDialog(){
        Log.i("key","alipayNeedLoginDialog");
        Intent intent = new Intent(getActivity(),SmartKeyService.class);
        Bundle bundle = new Bundle();
        bundle.putInt("alipay_need_login", 1);
        intent.putExtras(bundle);
        getActivity().startService(intent);
    }

}
