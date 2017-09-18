package com.ape.sugarrequirement.camera.settings;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import com.ape.sugarrequirement.R;

/**
 * Created by android on 5/10/17.
 */

public class CameraSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{
    private Context mContext;
    private static final String KEY_FACEBEAUTY = "facebeauty";
    private SwitchPreference mFacebeautyPreference;

    private static final String KEY_WATERMARK = "watermark";
    private SwitchPreference mWatermarkPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        addPreferencesFromResource(R.xml.settings);

        mFacebeautyPreference = (SwitchPreference) findPreference(KEY_FACEBEAUTY);
        if (null != mFacebeautyPreference) {
            mFacebeautyPreference.setOnPreferenceChangeListener(this);
            mFacebeautyPreference.setChecked(SystemProperties.get("persist.sys.camera.facebeauty").equals("1"));
            if(!SystemProperties.get("ro.pt.facebeauty.enable").equals("yes")){

                getPreferenceScreen().removePreference(mFacebeautyPreference);
            }
        }
        mWatermarkPreference = (SwitchPreference) findPreference(KEY_WATERMARK);
        if (null != mWatermarkPreference) {
            mWatermarkPreference.setOnPreferenceChangeListener(this);
            mWatermarkPreference.setChecked(SystemProperties.get("persist.sys.camera.watermark").equals("1"));
            if(!SystemProperties.get("ro.pt.watermark.enable").equals("yes")){

                getPreferenceScreen().removePreference(mWatermarkPreference);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {

        if (preference == mFacebeautyPreference) {
            if ((boolean) objValue) {
                SystemProperties.set("persist.sys.camera.facebeauty","1");
            } else {
                SystemProperties.set("persist.sys.camera.facebeauty","0");
            }
        }
        if (preference == mWatermarkPreference) {
            if ((boolean) objValue) {
                SystemProperties.set("persist.sys.camera.watermark","1");
            } else {
                SystemProperties.set("persist.sys.camera.watermark","0");
            }
        }


        return true;
    }

}
