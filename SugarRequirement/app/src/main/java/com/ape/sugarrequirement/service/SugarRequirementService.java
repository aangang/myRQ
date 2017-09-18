package com.ape.sugarrequirement.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import com.ape.sugarrequirement.util.Constant;


public class SugarRequirementService extends Service {
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i(Constant.EYETAG, "SugarRequirementService onCreate");
        boolean ep_enable = SystemProperties.get("ro.pt.eye_prevent", "0").equals("1");
        if (ep_enable) {
            mSettingsObserver = new SettingsObserver(myHandler);
            mSettingsObserver.observe();
        }
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
	    Log.i(Constant.EYETAG, "SugarRequirementService onStartCommand");

		//return super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
	}

	// gang.an, DATE20160506, add eye prevent functions, start
	SettingsObserver mSettingsObserver;
	Handler myHandler = new Handler();
	class SettingsObserver extends ContentObserver {
		SettingsObserver(Handler handler) {
			super(handler);
			Log.i(Constant.EYETAG,"SettingsObserver  constructor");
		}

		void observe() {
			Log.i(Constant.EYETAG,"SettingsObserver  ovserve()");
			// Observe all users' changes
			ContentResolver resolver = getContentResolver();
			boolean ep_enable = SystemProperties.get("ro.pt.eye_prevent", "0").equals("1");
			if (ep_enable) {
				resolver.registerContentObserver(Settings.System.getUriFor(
						Constant.EYE_PREVENT_STATUS_QCOM), false, this);
			}
		}
		@Override public void onChange(boolean selfChange) {
			updateEPState();
		}
	}

	public void updateEPState() {
		ContentResolver resolver = getContentResolver();
		boolean ep_enable = SystemProperties.get("ro.pt.eye_prevent", "0").equals("1");
		if (ep_enable) {
			int qcomEPMode = Settings.System.getInt(resolver,Constant.EYE_PREVENT_STATUS_QCOM, 0);
			Log.i(Constant.EYETAG,"updateEPState qcomEPMode:" + qcomEPMode);
			Intent intent = new Intent();
			intent.setAction("android.intent.action.QCOM_EYEPREVENT");
			//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}
	// gang.an, DATE20160506, add eye prevent functions, end


}
