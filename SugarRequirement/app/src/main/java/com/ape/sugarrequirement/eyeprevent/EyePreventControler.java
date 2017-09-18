package com.ape.sugarrequirement.eyeprevent;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.ape.sugarrequirement.util.Constant;
import com.qti.snapdragon.sdk.display.ColorManager;
import com.qti.snapdragon.sdk.display.ModeInfo;

public class EyePreventControler extends Activity{

	int ep_status = -1;
	ColorManager cmgr;
    int colorTemp;
    boolean mConnected = false;
    ColorManager.ColorManagerListener colorinterface;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
        colorinterface = new ColorManager.ColorManagerListener() {
            @Override
            public void onConnected() {
            	
        		if(intent.getAction().endsWith(Constant.QCOM_EP_ACTION)){
        			Log.i(Constant.EYETAG, "EyePreventControler   action = " + intent.getAction());
        			ep_status = Settings.System.getInt(getContentResolver(), Constant.EYE_PREVENT_STATUS_QCOM, -1);

        			new Thread(){
        				public void run() {
        					Log.i(Constant.EYETAG, "EyePreventControler   Thread.run");
        					setEyePreventMode(ep_status);
        					
        				};
        			}.start();
        		}
            }
        };
        int retVal = ColorManager.connect(this, colorinterface);
        if (retVal != ColorManager.RET_SUCCESS) {
            Log.e(Constant.EYETAG, "Connection failed");
        }
		finish();
		
	}

	
	/*
	 * this function to set EyePrevent Mode on/off
	 * settings database item: EYE_PREVENT_STATUS_QCOM 
	 * param:status EyePrevent Mode
	 */
	protected void setEyePreventMode(int status) {
		
        if (cmgr == null)
            cmgr = ColorManager.getInstance(getApplication(), this, ColorManager.DCM_DISPLAY_TYPE.DISP_PRIMARY);

        if (cmgr != null) {
        	Log.i(Constant.EYETAG, "cmgr != null   setColorBalanceInstance  ");
            //setColorBalanceInstance(status);
        	setColorMode(status);
            
        }else{
        	Log.i(Constant.EYETAG, "cmgr == null");
        }
	}
	
	private void setColorMode(int satus){
		ModeInfo[] modeDataArray = null;
        if (cmgr == null) {
            Log.e(Constant.EYETAG, "Display SDK manager is null!");
            return ;
        }
        modeDataArray = cmgr.getModes(ColorManager.MODE_TYPE.MODE_ALL);
        if (modeDataArray != null) {
        	
        	int[] activeMode = cmgr.getActiveMode();
            int mDefaultModeID = cmgr.getDefaultMode();
            for(int i=0;i<activeMode.length;i++){
            	 Log.e(Constant.EYETAG, "activeMode[i]:" + activeMode[i]);
            }
            Log.i(Constant.EYETAG, "    mDefaultModeID:" + mDefaultModeID);

            if(satus == 0){
                Log.e(Constant.EYETAG, "cmgr.setActiveMode(0)");
                cmgr.setActiveMode(0);
                cmgr.setDefaultMode(0);

            }else{
                cmgr.setActiveMode(1);
                cmgr.setDefaultMode(1);
                Log.e(Constant.EYETAG, "cmgr.setActiveMode(1)");

            }
         }
	}
	
    private void setColorBalanceInstance(int status) {

        if (cmgr == null) {
            cmgr = ColorManager.getInstance(getApplication(), this,
                    ColorManager.DCM_DISPLAY_TYPE.DISP_PRIMARY);
        }

        if (cmgr != null) {
            boolean isSupport = false;
            isSupport = cmgr.isFeatureSupported(ColorManager.DCM_FEATURE.FEATURE_COLOR_BALANCE);
            if (isSupport == false) {
                Log.i(Constant.EYETAG, "isSupport == false  return");
                return;
            }
            
            /*int qcomEPMode = Settings.System.getInt(this.getContentResolver(),
                    Settings.System.EYE_PREVENT_STATUS_QCOM, 0);*/
            
            colorTemp = cmgr.getColorBalance();
            
            if(status == 1){
            	Log.i(Constant.EYETAG, "status == 1");
            	colorTemp = Constant.QCOM_STATUS_VALUE_ON;
            	
            }else if(status == 0){
            	Log.i(Constant.EYETAG, "status == 0");
            	colorTemp = Constant.QCOM_STATUS_VALUE_OFF;
            }
            
            if (colorTemp < ColorManager.COLOR_BALANCE_WARMTH_LOWER_BOUND || colorTemp > ColorManager.COLOR_BALANCE_WARMTH_UPPER_BOUND) 
            {
            	Log.i(Constant.EYETAG, "colorTemp = Contents.QCOM_STATUS_VALUE_OFF;");
                colorTemp = Constant.QCOM_STATUS_VALUE_OFF;
            }
            
            long startTime = System.nanoTime();
            cmgr.setColorBalance(colorTemp);		////////
            long endTime = System.nanoTime();
            long diff = endTime - startTime ;
            Log.i(Constant.EYETAG, "Elapsed microseconds: " + diff /1000);
            
            int modeID;
            //modeID = cmgr.createNewMode("7705default");
            //Log.i("eyeprevent", "ModeId=" + modeID);
            //if (modeID < 0) {
                int[] activeMode = cmgr.getActiveMode();
                modeID = activeMode[0];
                Log.i(Constant.EYETAG, "ModeId=" + modeID);
                cmgr.modifyMode(modeID, "8909default");
                Log.i(Constant.EYETAG, "cmgr.modifyMode(modeID, 8909default)");
            //}
            cmgr.setDefaultMode(modeID);
            Log.i(Constant.EYETAG, "cmgr.setDefaultMode(modeID)  modeID:" + modeID);
            
        }
    }
}
