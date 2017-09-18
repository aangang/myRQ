package com.ape.sugarrequirement.eyeprevent;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.ape.sugarrequirement.util.Constant;
import com.qti.snapdragon.sdk.display.ColorManager;
import com.qti.snapdragon.sdk.display.ColorManager.ColorManagerListener;
import com.qti.snapdragon.sdk.display.ColorManager.DCM_DISPLAY_TYPE;
import com.qti.snapdragon.sdk.display.ColorManager.MODE_TYPE;
import com.qti.snapdragon.sdk.display.ModeInfo;

public class EyePreventCameraService extends Service {

	int ep_status = -1;
	ColorManager cmgr;
    int colorTemp;
    boolean mConnected = false;
    int index = -1;
    ColorManagerListener colorinterface;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i(Constant.EYETAG, "service onCreate");
                //getNVMode();
                //Log.i("eyeprevent", "service onCreate  index:" + index);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if(intent != null && intent.getAction() != null && intent.getAction().endsWith(Constant.QCOM_EP_ACTION_SERVICE)){
			Log.i(Constant.EYETAG, "service onStartCommand   QCOM_EP_ACTION_SERVICE");
			
			final boolean open = intent.getBooleanExtra("open", true);
			Log.i(Constant.EYETAG, "EyePreventService   open:" + open);
	        colorinterface = new ColorManagerListener() {
	            @Override
	            public void onConnected() {
	        		ep_status = Settings.System.getInt(getContentResolver(), Constant.EYE_PREVENT_STATUS_QCOM, -1);
	        		new Thread(){
	        			public void run() {
	        				Log.i(Constant.EYETAG, "EyePreventService   Thread.run");
	        				//open camera and system in ep mode
	        				if(open && ep_status == 1){
	        					setEyePreventMode(0);
	        					//close camera and system in ep mode
	        				}else if(!open && ep_status == 1){
	        					setEyePreventMode(1);
	        				}
	        				
	        			};
	        		}.start();
	            }
	        };
                getNVMode();
                Log.i(Constant.EYETAG, "service onCreate  index:" + index);
	        int retVal = ColorManager.connect(this, colorinterface);
	        if(!mConnected){
	        	retVal = ColorManager.connect(this, colorinterface);
	        	Log.e(Constant.EYETAG, "!mConnection");
	        }
			
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
        void getNVMode(){
              index = Settings.Global.getInt(getContentResolver(), "normal_mode", 0);
              //gang.an, DATE20160830, eyeprevent crash because of qdcm_mobile start
              if(index <0 || index >2){
                  Log.i(Constant.EYETAG,"index:" + index + "   set index 0");
                  index = 0;
              }
              //gang.an, DATE20160830, eyeprevent crash because of qdcm_mobile end
              Log.i(Constant.EYETAG,"getNVMode  index:" + index);
        }

	/*
	 * this function to set EyePrevent Mode on/off
	 * settings database item: EYE_PREVENT_STATUS_QCOM 
	 * param:status EyePrevent Mode
	 */
	protected void setEyePreventMode(int status) {
		
        if (cmgr == null)
            cmgr = ColorManager.getInstance(getApplication(), this,DCM_DISPLAY_TYPE.DISP_PRIMARY);

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
        modeDataArray = cmgr.getModes(MODE_TYPE.MODE_ALL);
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
	
	

}
