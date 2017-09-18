package com.ape.sugarrequirement.smartkey;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;

import com.ape.sugarrequirement.R;
import com.ape.sugarrequirement.pks.PKSActivity;
import com.ape.sugarrequirement.util.Constant;
import com.ape.sugarrequirement.util.Utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;


public class SmartKeyService extends Service {

	private static boolean incomingFlag = false;
	private static boolean callingFlag = false;
	private static boolean PowerOffStatus= false;

	private static boolean hasDialog = false;

	boolean has_camera_activity=false;
	boolean has_mirror_activity=false;
	boolean has_torch_activity=false;
	boolean has_alarm_activity=false;
	boolean has_sound_recorder_activity=false;
	boolean has_launcher_camera_activity = false;

	private Sensor mFTSensor;
	private SensorManager mFTSensorManager;
	private float mFTDistance = 1.0f;
	private Handler mFTHandler = new Handler();

	public Intent smartKeyIntentCameraVideo;
	public Intent smartKeyIntentVoice;
	public Intent smartKeyIntentMirror;
	public Intent smartKeyIntentLight;
	public Intent smartKeyIntentCameraCapture;

	private int start_mode_status = -1;

	TelephonyManager mTelephonyManager;
	Handler mHandler;
	static Vibrator mVibrator;
	Intent mIntent;
	ComponentName mComponentName;
	private boolean smKeyRunIntime = false;  //ensure run once at most in a second

	private long mLastSmartKeyDown;

	PowerManager.WakeLock wakeLock;

	private PowerManager pm;

	private boolean mWeixinAvilible = false;
	private boolean mAlipayAvilible = false;
	private boolean mMirrorAvilible = false;
	private boolean mVoiceAssistAvilible = false;
	public static boolean isWeixinAvilible(Context context) {
		final PackageManager packageManager = context.getPackageManager();//
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);//
		Log.i(Constant.KEYTAG, "  isWeixinAvilible");
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName;
				if (pn.equals("com.tencent.mm")) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isAlipayAvilible(Context context) {
		final PackageManager packageManager = context.getPackageManager();//
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);//
		Log.i(Constant.KEYTAG, "  isAlipayAvilible");
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName;
				if (pn.equals("com.eg.android.AlipayGphone")) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isVoiceAssistAvilible(Context context) {
		final PackageManager packageManager = context.getPackageManager();//
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);//
		Log.i(Constant.KEYTAG, "  isVoiceAssistAvilible");
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName;
				if (pn.equals("com.myos.speechassist")) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isMirrorAvilible(Context context) {
		final PackageManager packageManager = context.getPackageManager();//
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);//
		Log.i(Constant.KEYTAG, "  isMirrorAvilible");
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName;
				if (pn.equals("com.ape.mirror")) {
					return true;
				}
			}
		}
		return false;
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mHandler = new Handler();
		mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		Log.i(Constant.KEYTAG, "SmartKeyService onCreate");

	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
	    Log.i(Constant.KEYTAG, "SmartKeyService onStartCommand");

		if(intent != null && intent.getExtras() != null) {
			int first_run = intent.getExtras().getInt("first_run");
			Log.i("key", "service first_run:" + first_run);

			if (first_run == 1) {
				Log.i("key", "first dialog");
				firstDialog();

			}

			try{
				int mm_login = intent.getExtras().getInt("mm_need_login");
				if(mm_login == 1){
					mmNeedLoginDialog();
				}
				int alipay_login = intent.getExtras().getInt("alipay_need_login");
				if(alipay_login == 1){
					alipayNeedLoginDialog();
				}
				boolean forPhone = intent.getBooleanExtra("for_phone",false);
				if(!forPhone) {
					return Service.START_STICKY;
				}
			}catch (Exception e){
				Log.i(Constant.KEYTAG, "SmartKeyService onStartCommand Exception");
				return Service.START_STICKY;
			}
		}

		sugarKeyInit();
		try{
			boolean forPhone = intent.getBooleanExtra("for_phone",false);
			Log.i(Constant.KEYTAG, "for_phone:" + forPhone);
			if(forPhone){
				if (!smKeyRunIntime) {
					smKeyRunIntime = true;
					Log.i(Constant.KEYTAG, "mSmartKeyRunnableForPhone  forPhone");
					mHandler.postDelayed(mSmartKeyRunnableForPhone, 1);
					mHandler.postDelayed(mSmartKeyEnable, 1000);
				}
			}else {
				if (!smKeyRunIntime) {
					smKeyRunIntime = true;
					Log.i(Constant.KEYTAG, "##########smKeyRunIntime == false  set to true  will not run agine in one second  mSmartKeyRunnable");
					mHandler.postDelayed(mSmartKeyRunnable, 1);
					mHandler.postDelayed(mSmartKeyEnable, 1000);
				}
			}
		}catch(Exception e){
			Log.i(Constant.KEYTAG, "SmartKeyService onStartCommand Exception1");
		}

        return Service.START_STICKY;
	}

	private void acquireWakeLock(){
		PowerManager pm =(PowerManager) getSystemService(POWER_SERVICE);
		wakeLock= pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK| PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
		wakeLock.acquire();

	}

	private void releaseWakeLock(){
		if(wakeLock != null){
			wakeLock.release();
			wakeLock = null;
		}
	}

	@Override
	public void onDestroy() {

		Log.i(Constant.KEYTAG, "SmartKeyService onDestroy");
		super.onDestroy();
	}

	public final Runnable mSmartKeyEnable = new Runnable() {
		@Override
		public void run() {
            if(smKeyRunIntime){
				Log.i(Constant.KEYTAG, "#################smKeyRunIntime set false  mSmartKeyEnable run");
				smKeyRunIntime = false;
			}
		}
	};
	public final Runnable mSmartKeyRunnable = new Runnable() {
		@Override
		public void run() {
			if(isSugarKeyWork()){
				if((Settings.System.getInt(getContentResolver(),Constant.FALSE_TOUCH_STATUS, 1)==1)
						/*&& SystemProperties.get("ro.pt.false_touch", "0").equals("1")*/){
					proximityRuned = false;
					mFTSensorManager = ((SensorManager)getSystemService(Context.SENSOR_SERVICE));
					mFTSensor = mFTSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
					mFTSensorManager.registerListener(mproximityEventListener,
							mFTSensor, mFTSensorManager.SENSOR_DELAY_NORMAL/*SENSOR_DELAY_FASTEST*/);
					Log.i(Constant.KEYTAG, " mFTSensor  registered !!!" );
					mFTHandler.postDelayed(new Runnable() {
						public void run() {
						    Log.d(Constant.KEYTAG, "unregisterPromixyListener!!!!!");
						    mFTSensorManager.unregisterListener(mproximityEventListener);
						}
					}, 500);
				}else{
					Log.i(Constant.KEYTAG, " FALSE_TOUCH disabled  runSugarKey()!!!" );
					runSugarKey();
				}
			}

		}
	};
	public final Runnable mSmartKeyRunnableForPhone = new Runnable() {
		@Override
		public void run() {
			runSugarKeyForPhone();

		}
	};


	private boolean isSugarKeyUseSupport(){
		boolean isBootComplete = (1 == Settings.Secure.getInt(getContentResolver(), "user_setup_complete", 0));
		//int isSugarKeyBeUseInAlarmMode = Settings.System.getInt(getContentResolver(), Settings.System.POWER_OFF_ALARM_MODE, 0);
		if(isBootComplete && !PowerOffStatus /*&& !ShutdownThread.bShutdownAnimation&&!(isSugarKeyBeUseInAlarmMode==1)*/
				&&(!has_camera_activity)&&(!has_launcher_camera_activity)/*&&(!callingFlag)*/){
			return true;
		}else{
			return false;
		}
	}

	private boolean isSugarKeyWork(){

		if((start_mode_status==0)&&(!has_sound_recorder_activity)&&(isSugarKeyUseSupport())){
			return true;
		} else if ((start_mode_status==1)&&(!has_sound_recorder_activity)&&/*(!callingFlag)&&*/(isSugarKeyUseSupport())){
			return true;
		} else if ((start_mode_status==2)&&(!has_mirror_activity)&&(isSugarKeyUseSupport())){
			return true;
		} else if ((start_mode_status==3)&&(isSugarKeyUseSupport())){
			return true;
		} else if ((start_mode_status==4)&&(!has_mirror_activity)&&(isSugarKeyUseSupport())){
			return true;
		} else if((start_mode_status==5) && isSugarKeyUseSupport()){
			return true;
		} else if((start_mode_status==6) && isSugarKeyUseSupport()){
			return true;
		} else if((start_mode_status==7) && isSugarKeyUseSupport()){
			return true;
		} else{
			return false;
		}

	}

	TelecomManager getTelecommService() {
		return (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
	}


	boolean isRinging(){
		TelecomManager dtelecomManager = getTelecommService();
		Class c;
		boolean ret = false;
		try {
			c = Class.forName("android.telecom.TelecomManager");

			Class<?> clazz = dtelecomManager.getClass();
			Method isRinging = clazz.getMethod("isRinging");
			Object boolRet = isRinging.invoke(dtelecomManager);
			ret = (boolean)boolRet;
			Log.i(Constant.KEYTAG, "called isRinging  ret:" + ret);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			Log.i(Constant.KEYTAG, "called isRinging 1  e:" + e);
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			Log.i(Constant.KEYTAG, "called isRinging 2  e:" + e);
			e.printStackTrace();
		} catch(Exception e){
			Log.i(Constant.KEYTAG, "called isRinging 3  e:" + e);

		}
		return ret;
	}

	void acceptRingingCall(){
		TelecomManager dtelecomManager = getTelecommService();
		Class c;
		try {
			c = Class.forName("android.telecom.TelecomManager");

			Class<?> clazz = dtelecomManager.getClass();
			Method acceptRingingCall = clazz.getMethod("acceptRingingCall");
			acceptRingingCall.invoke(dtelecomManager);
			Log.i(Constant.KEYTAG, "called acceptRingingCall");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			Log.i(Constant.KEYTAG, "called acceptRingingCall 1  e:" + e);
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			Log.i(Constant.KEYTAG, "called acceptRingingCall 2  e:" + e);
			e.printStackTrace();
		} catch(Exception e){
			Log.i(Constant.KEYTAG, "called acceptRingingCall 3  e:" + e);

		}
	}

	private void dismissKeyguard(){
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					Log.i(Constant.KEYTAG, "wm dismiss-keyguard ###############" );
					Runtime.getRuntime().exec("wm dismiss-keyguard");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}

	private void runSugarKeyForPhone(){
		Log.i(Constant.KEYTAG, "runSugarKeyForPhone");

		if(incomingFlag){
			Log.i(Constant.KEYTAG, "runSugarKey  incomingFlag");
			if( Settings.System.getInt(getContentResolver(),Constant.CLICK_ANSWER_THE_PHONE_STATUS, 1)==1) {
				TelecomManager dtelecomManager = getTelecommService();
				if (dtelecomManager != null) {
					if (isRinging()) {
						mVibrator.vibrate(200);
						acceptRingingCall();
						Log.i(Constant.KEYTAG, "Ramiel take phone");
					}
				}
			}
			return;
		}

		if(callingFlag){
			if(Settings.System.getInt(getContentResolver(),Constant.VOICE_RECORDING_STATUS, 1)==1) {
				Log.i(Constant.KEYTAG, "suagr key clicked, send a broadcast to Dialer for voice recording");
				Intent intent = new Intent();
				//intent.setPackage("com.android.dialer");
				intent.setAction("android.media.action.SUAGR_KEY_CLICKED");
				sendBroadcast(intent);
				//vibrate(200);
			}else{
				Log.i(Constant.KEYTAG, "runSugarKey  callingFlag return");
			}
			return;
		}
	}

	private void runSugarKey() {

		Log.i(Constant.KEYTAG, "runSugarKey");

		if(incomingFlag){
			Log.i(Constant.KEYTAG, "runSugarKey  incomingFlag");
			if( Settings.System.getInt(getContentResolver(),Constant.CLICK_ANSWER_THE_PHONE_STATUS, 1)==1) {
				TelecomManager dtelecomManager = getTelecommService();
				if (dtelecomManager != null) {
					if (isRinging()) {
						mVibrator.vibrate(200);
						acceptRingingCall();
						Log.i(Constant.KEYTAG, "Ramiel take phone");
					}
				}
			}
			return;
		}

		vibrate(200);

		if(callingFlag){
			if(Settings.System.getInt(getContentResolver(),Constant.VOICE_RECORDING_STATUS, 1)==1) {
				Log.i(Constant.KEYTAG, "suagr key clicked, send a broadcast to Dialer for voice recording");
				Intent intent = new Intent();
				//intent.setPackage("com.android.dialer");
				intent.setAction("android.media.action.SUAGR_KEY_CLICKED");
				sendBroadcast(intent);
			}else{
				Log.i(Constant.KEYTAG, "runSugarKey  callingFlag return");
			}
			return;
		}
		if (start_mode_status != 4 && start_mode_status != 1) {
			acquireWakeLock();
			//wakeUp(SystemClock.uptimeMillis(), mAllowTheaterModeWakeFromWakeGesture,"android.policy:GESTURE");
			dismissKeyguard();
		}
		if(start_mode_status == 1){
			acquireWakeLock();
		}

		Log.i(Constant.KEYTAG, "Keyguard status : "+isKeyguardLocked() + "   start_mode_status:" + start_mode_status);
		try {
			if ((start_mode_status == 0)) {
				Log.i(Constant.KEYTAG, "runSugarKey  start camera video");
				startCameraVideo();

			} else if ((start_mode_status == 1)) {
				Log.i(Constant.KEYTAG, "runSugarKey  start sound record");
				startSoundRecord();

			} else if ((start_mode_status == 2)) {
				Log.i(Constant.KEYTAG, "runSugarKey  start mirror");
				startMirror();

			} else if ((start_mode_status == 3)) {
				Log.i(Constant.KEYTAG, "runSugarKey  start camera photo");
				startCameraPhoto();

			} else if ((start_mode_status == 4)) {
				Log.i(Constant.KEYTAG, "runSugarKey  start torch");
				startTorch();

			} else if ((start_mode_status == 5)) {
				Log.i(Constant.KEYTAG, "runSugarKey  start qrcode");
				startQRCode();

			}else if ((start_mode_status == 6)) {
				Log.i(Constant.KEYTAG, "runSugarKey  start alipay");
				startAlipay();

			}else if ((start_mode_status == 7)) {
				Log.i(Constant.KEYTAG, "runSugarKey  start voice assistant");
				startVoiceAssistant();

			}
		}catch(ActivityNotFoundException exception){
			Log.i(Constant.KEYTAG,"activity not found  " + exception);
		}

		//mSmartKeyCameraHandled = true;
		if (start_mode_status != 4) {
			releaseWakeLock();
		}
	}

	private boolean isKeyguardLocked() {
		KeyguardManager kgm = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		return (kgm != null) && kgm.isKeyguardLocked();
	}

	private boolean isKeyguardSecureLocked() {
		KeyguardManager kgm = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		return (kgm != null) && kgm.isKeyguardLocked() && kgm.isKeyguardSecure();
	}


	private void startCameraVideo() {
		if(isKeyguardSecureLocked()){
			mIntent = new Intent("android.media.action.SHORTCUT_CAMERA_SECURE");
		}else{
			mIntent = new Intent("android.media.action.SHORTCUT_CAMERA");
		}
		mIntent.putExtra("android.intent.extras.CAMERA_MAIN_MODE","video");
		mIntent.putExtra("android.intent.extras.CAMERA_FACING",0);
		mIntent.putExtra("android.intent.extras.CAMERA_SUB_MODE","videobeauty");
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if(isKeyguardSecureLocked()){
			mIntent.putExtra("isscreenlock",true);
		}else{
			mIntent.putExtra("isscreenlock",false);
		}
		mIntent.putExtra("isSugarKey",true);
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(mIntent);
	}

	private void startSoundRecord() {
		mIntent = new Intent();
		mIntent.setAction("com.android.sugarkey");
		sendBroadcast(mIntent);
	}


	private void startMirror(){
		mMirrorAvilible = isMirrorAvilible(this);
		if(mMirrorAvilible){
			try{
				mComponentName = new ComponentName("com.ape.mirror","com.ape.mirror.fantasy.ui.activity.FantasyActivity");
				mIntent = new Intent();
				mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mIntent.setComponent(mComponentName);
				if(isKeyguardLocked()){
					mIntent.putExtra("isscreenlock",true);
				}else{
					mIntent.putExtra("isscreenlock",false);
				}
				mIntent.putExtra("isSugarKey",true);
				startActivity(mIntent);

			}catch(Exception e){
			}
		}else{
			if(!hasDialog) {
				mirrorNotExsistDialog();
			}
		}
	}

	private void startCameraPhoto() {
		if(isKeyguardSecureLocked()){
			mIntent = new Intent("android.media.action.SHORTCUT_CAMERA_SECURE");
		}else{
			mIntent = new Intent("android.media.action.SHORTCUT_CAMERA");
		}
		if(isKeyguardSecureLocked()){
			mIntent.putExtra("isscreenlock",true);
		}else{
			mIntent.putExtra("isscreenlock",false);
		}
		mIntent.putExtra("android.intent.extras.CAMERA_MAIN_MODE", "photo");
		mIntent.putExtra("android.intent.extras.CAMERA_SUB_MODE", "normal");
		mIntent.putExtra("android.intent.extras.CAMERA_FACING", 0);
		mIntent.putExtra("isSugarKey",true);
		mIntent.putExtra("android.intent.extra.fastcapture",true);
		//doubleclickIntentCameraCapture.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(mIntent);
	}

	private void startTorch() {
		mIntent = new Intent("com.android.PHONE_WINDOW_MANAGER_TORCH");
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if(isKeyguardLocked()){
			mIntent.putExtra("isscreenlock",true);
		}else{
			mIntent.putExtra("isscreenlock",false);
		}
		mIntent.putExtra("isSugarKey",true);
		sendBroadcast(mIntent);
	}

	private void startQRCode() {
		mWeixinAvilible = isWeixinAvilible(this);
		if(mWeixinAvilible){
			try{
				ComponentName qrcn = new ComponentName("com.tencent.mm","com.tencent.mm.plugin.scanner.ui.BaseScanUI");
				Intent qrintent = new Intent();
				qrintent.setComponent(qrcn);
				qrintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //
				startActivity(qrintent);
				mHandler.postDelayed(mQRCodeDisplay,2000);
			}catch(Exception e){
			}
		}else{
			/*Intent intent = new Intent();
			intent.setAction(Constant.MM_NOT_EXIST_ACTION);
			//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);*/
			if(!hasDialog) {
				mmNotExsistDialog();
			}
		}

	}

	private void startAlipay(){

		mAlipayAvilible = isAlipayAvilible(this);
		if(mAlipayAvilible){
			try{
				ComponentName alipay = new ComponentName("com.eg.android.AlipayGphone","com.alipay.mobile.scan.as.main.MainCaptureActivity");
				Intent alipayintent = new Intent();
				alipayintent.setComponent(alipay);
				alipayintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //
				startActivity(alipayintent);
				mHandler.postDelayed(mAlipayQRCodeDisplay,2000);
			}catch(Exception e){
			}
		}else{
			if(!hasDialog) {
				alipayNotExsistDialog();
			}
		}
	}

	private void startVoiceAssistant() {
		mVoiceAssistAvilible = isVoiceAssistAvilible(this);
		if(mVoiceAssistAvilible){
			try{
				Intent voiceIntent = new Intent("android.intent.action.SPEECH_WAKE_UP");
				voiceIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				Log.i(Constant.KEYTAG,"send broadcast android.intent.action.SPEECH_WAKE_UP");
				sendOrderedBroadcast(voiceIntent,null);
			}catch(Exception e){
			}
		}else{
			if(!hasDialog) {
				speechAssistNotExsistDialog();
			}
		}
	}

	protected void firstDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.sugarkeyshortcut_substance);
		builder.setTitle(R.string.sugarkeyshortcut_title);

		builder.setPositiveButton(R.string.sugarkeyshortcut_enter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int which) {

				hasDialog = false;
				Utils.setFirstRun(SmartKeyService.this);

				Intent intent = new Intent(SmartKeyService.this,SmartKeyActivity.class);
				startActivity(intent);
			}
		});


		builder.setNegativeButton(R.string.sugarkeyshortcut_exit, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int which) {

				hasDialog = false;
				dialog.dismiss();
			}
		});
		builder.setCancelable(false);
		Dialog dialog = builder.create();
		dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
		dialog.show();
		hasDialog = true;

	}

	public  void mmNotExsistDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.sugarkey_weichat_not_exist);
		builder.setTitle(R.string.sugarkey_weichat_need_login_title);

		builder.setPositiveButton(R.string.sugarkey_weichat_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int which) {
				dialog.dismiss();
				hasDialog = false;
			}
		});
		builder.setCancelable(false);
		Dialog dialog = builder.create();
		dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
		dialog.show();
		hasDialog = true;
	}

	public  void mmNeedLoginDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.sugarkey_weichat_need_login);
		builder.setTitle(R.string.sugarkey_weichat_need_login_title);

		builder.setPositiveButton(R.string.sugarkey_weichat_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int which) {
				dialog.dismiss();
				hasDialog = false;
			}
		});

		Dialog dialog = builder.create();
		dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
		dialog.show();
		hasDialog = true;

	}

	protected void speechAssistNotExsistDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.sugarkey_voice_not_exist);
		builder.setTitle(R.string.sugarkey_weichat_need_login_title);

		builder.setPositiveButton(R.string.sugarkey_weichat_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int which) {
				dialog.dismiss();
				hasDialog = false;
			}
		});

		Dialog dialog = builder.create();
		dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
		dialog.show();
		hasDialog = true;

	}

	protected void mirrorNotExsistDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.sugarkey_mirror_not_exist);
		builder.setTitle(R.string.sugarkey_weichat_need_login_title);

		builder.setPositiveButton(R.string.sugarkey_weichat_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int which) {
				dialog.dismiss();
				hasDialog = false;
			}
		});

		Dialog dialog = builder.create();
		dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
		dialog.show();
		hasDialog = true;

	}

	protected void alipayNotExsistDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.sugarkey_alipay_not_exist);
		builder.setTitle(R.string.sugarkey_weichat_need_login_title);

		builder.setPositiveButton(R.string.sugarkey_weichat_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int which) {
				dialog.dismiss();
				hasDialog = false;
			}
		});

		Dialog dialog = builder.create();
		dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
		dialog.show();
		hasDialog = true;

	}

	protected void alipayNeedLoginDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.sugarkey_alipay_need_login);
		builder.setTitle(R.string.sugarkey_weichat_need_login_title);

		builder.setPositiveButton(R.string.sugarkey_weichat_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int which) {
				dialog.dismiss();
				hasDialog = false;
			}
		});

		Dialog dialog = builder.create();
		dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
		dialog.show();
		hasDialog = true;

	}

	public final Runnable mQRCodeDisplay = new Runnable() {
		@Override
		public void run() {
			Log.i("Ramiel", "mQRCodeDisplay" );
			//current
			ActivityManager am1 = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
			ComponentName cn = am1.getRunningTasks(1).get(0).topActivity;
			Log.i("Ramiel", "Ramiel weixin isTopActivity = " + cn.getClassName());
			if(!cn.getClassName().equals("com.tencent.mm.plugin.scanner.ui.BaseScanUI")){
				try{
					ComponentName qrcn = new ComponentName("com.tencent.mm","com.tencent.mm.ui.LauncherUI");
					Intent launchermm = new Intent();
					launchermm.setComponent(qrcn);
					launchermm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //
					startActivity(launchermm);
				} catch (ActivityNotFoundException e) {
				}
			}
		}
	};

    public final Runnable mAlipayQRCodeDisplay = new Runnable() {
        @Override
        public void run() {
            Log.i("Ramiel", "mAlipayQRCodeDisplay" );
            //current
            ActivityManager am1 = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName cn = am1.getRunningTasks(1).get(0).topActivity;
            Log.i("Ramiel", "Ramiel Alipay isTopActivity = " + cn.getClassName());
            if(!cn.getClassName().equals("com.alipay.mobile.scan.as.main.MainCaptureActivity")){
              try{
                 ComponentName qrcn = new ComponentName("com.eg.android.AlipayGphone","com.alipay.mobile.scan.as.main.MainCaptureActivity");
                 Intent launcherAlipay = new Intent();
                 launcherAlipay.setComponent(qrcn);
                 launcherAlipay.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //
                 startActivity(launcherAlipay);
              } catch (ActivityNotFoundException e) {
              }
            }
        }
    };

	boolean proximityRuned = false;
	private SensorEventListener mproximityEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			mFTDistance = event.values[0];
			Log.i(Constant.KEYTAG, " onSensorChanged  mFTDistance :" + mFTDistance);
			if(mFTDistance == 0.0f && !proximityRuned){//防止重复执行，导致pks去不掉
				Log.i(Constant.KEYTAG, " onSensorChanged   createlockactivity!!");
				proximityRuned = true;
				if(!has_alarm_activity){
					//wakeUp(SystemClock.uptimeMillis(), mAllowTheaterModeWakeFromWakeGesture,"android.policy:GESTURE");
					createlockactivity();
					Log.i(Constant.KEYTAG,"distance 0  create pks  and wakeup");
				}
				return;
			}
			if( mFTDistance > 0.0f ) {
				Log.i(Constant.KEYTAG, "distance is far");
			}
			if(!proximityRuned && !PKSActivity.islockviewCreate){
				Log.i(Constant.KEYTAG, " onSensorChanged !runed  runSugarKey");
				proximityRuned = true;
				runSugarKey();
			}

		}
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};


	private void sugarKeyInit(){
		start_mode_status=Settings.System.getInt(getContentResolver(),Constant.SUGAR_KEY_START_MODE_STATUS, 0);

		mTelephonyManager =  getTelephonyManager();
		incomingFlag = false;
		callingFlag = false;
		switch (mTelephonyManager.getCallState()) {
			case TelephonyManager.CALL_STATE_RINGING:
				incomingFlag = true;
				Log.i(Constant.KEYTAG, "phone RINGING");
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				callingFlag= true;
				Log.i(Constant.KEYTAG, "phone OFFHOOK");
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				incomingFlag = false;
				callingFlag = false;
				Log.i(Constant.KEYTAG, "phone IDLE");
				break;
		}

		ActivityManager mAm;
		mAm = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		try{
			ComponentName cn = mAm.getRunningTasks(1).get(0).topActivity;

			if((cn.getClassName().equals("com.myos.camera.activity.SecureCameraActivity"))
					||(cn.getClassName().equals("com.myos.camera.activity.CameraShortCutActivity"))
					||(cn.getClassName().equals("com.myos.camera.activity.SecureCameraActivity")))
				has_camera_activity=true;
			else
				has_camera_activity=false;

			if(cn.getClassName().equals("com.ape.mirror.fantasy.ui.activity.FantasyActivity"))
				has_mirror_activity=true;
			else
				has_mirror_activity=false;

			if((cn.getClassName().equals("com.android.soundrecorder.SoundRecorder"))
					||(cn.getClassName().equals("com.android.soundrecorder.RecordingFileList"))
					||(cn.getClassName().equals("com.android.soundrecorder.RecordingFileListSugar")))
				has_sound_recorder_activity=true;
			else
				has_sound_recorder_activity=false;

			if(cn.getClassName().equals("com.sugar.led.LEDActivity"))
				has_torch_activity=true;
			else
				has_torch_activity=false;

			if (cn.getClassName().equals("com.myos.camera.activity.LauncherCameraActivity")
					|| cn.getClassName().equals("com.myos.camera.activity.CameraActivity")) {
				has_launcher_camera_activity = true;
			} else {
				has_launcher_camera_activity = false;
			}
			if(cn.getClassName().equals("com.android.deskclock.alarms.AlarmActivity")){
				has_alarm_activity= true;
			}else{
				has_alarm_activity= false;
			}
		}catch(Exception e){
			has_camera_activity=false;
			has_camera_activity=false;
			has_sound_recorder_activity=false;
			has_torch_activity=false;
			has_launcher_camera_activity=false;
			has_alarm_activity=false;
		}

	}

	TelephonyManager getTelephonyManager() {
		if (mTelephonyManager == null) {
			mTelephonyManager = (TelephonyManager)getSystemService(
					Context.TELEPHONY_SERVICE);
		}
		return mTelephonyManager;
	}

	void vibrate(final int time){
		mHandler.postDelayed(new Runnable() {
			public void run() {
				mVibrator.vibrate(time);
			}
		}, 200);

	}


	private void createlockactivity() {
		ComponentName cn = new ComponentName("com.ape.sugarrequirement",
				"com.ape.sugarrequirement.pks.PKSActivity");
		Intent intent = new Intent();
		intent.setComponent(cn);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //
		startActivity(intent);
	}

	private void sendDestroyBroadcast(){
		Intent intent2=new Intent("DESTROY");
		intent2.putExtra("msg","destroy");
		sendBroadcast(intent2);
	}

}
