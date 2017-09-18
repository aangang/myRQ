package com.ape.sugarrequirement.pks;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ape.sugarrequirement.R;
import com.ape.sugarrequirement.util.Constant;

import java.io.InputStream;
import java.lang.reflect.Method;


public class PKSActivity extends Activity {
    private static final String TAG = "PKSActivity";

    private Handler mHandler = new Handler();
    private PowerManager pm;
    private WindowManager.LayoutParams wmParams;
    private WindowManager mWindowManager;
    private LayoutInflater inflater;
    private RelativeLayout mFloatLayout;
    private Bitmap bitmap;
    private View mFloatView;

    public static Boolean islockviewCreate = false;
    private Boolean istouched;

    private long mScreenMovedVolumeUpKeyTime;
    private long mScreenMovePowerKeyTime;
    private boolean mScreenMoveVolumeDownKeyTriggered;
    private boolean mScreenMoveVolumeUpKeyTriggered;
    private boolean mScreenMovePowerKeyTriggered;
    private static final long SCREENMOVE_DEBOUNCE_DELAY_MILLIS = 150;

    PowerManager.WakeLock wakeLock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);// 填充标题栏
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_pks);

        mScreenMovePowerKeyTriggered=false;
        mScreenMoveVolumeDownKeyTriggered=false;
        mScreenMoveVolumeUpKeyTriggered=false;

        istouched=false;            //屏幕被触摸
        islockviewCreate=false;     //锁屏层是否生成

        //灭屏或距离感应器离开监听
        IntentFilter destroyfilter = new IntentFilter();
        destroyfilter.addAction("DESTROY");
        registerReceiver(destroyActivityReceiver, destroyfilter);

        //home键监听
        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(homePressReceiver, homeFilter);

        //电源键监听
        final IntentFilter filter = new IntentFilter("PKSPowerKeyDown");
        registerReceiver(mPowerKeyDownReceiver, filter);

        //监听电源键和音量下键同时按下
        final IntentFilter PowerVolumedownfilter = new IntentFilter("PKSPowerKey and Volume_Down Down");
        //registerReceiver(mPowerVolumedownReceiver, PowerVolumedownfilter);

        //音量上键监听
        final IntentFilter VolumeUpfilter = new IntentFilter("PKSVolumeUpPress");
        registerReceiver(mVolumeUpReceiver, VolumeUpfilter);

        pm= (PowerManager)getSystemService(Context.POWER_SERVICE);

        Log.i(Constant.KEYTAG, "ActivityCreate");

        mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        inflater = LayoutInflater.from(getApplication());
        createLockScreen();
        screenOn();
    }

    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    private void screenOn(){
        PowerManager pm =(PowerManager) getSystemService(POWER_SERVICE);
        wakeLock= pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK| PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        wakeLock.acquire();

    }

    private void screenOff(){
        if(wakeLock != null){
            wakeLock.release();
            wakeLock = null;
        }

        Class c;
        try {
            c = Class.forName("android.os.PowerManager");

            Log.i(Constant.KEYTAG, "to call goToSleep");
            Class<?> clazz = pm.getClass();
            Method goToSleep = clazz.getMethod("goToSleep",long.class,int.class,int.class);
            goToSleep.invoke(pm,SystemClock.uptimeMillis(),0,0);
            Log.i(Constant.KEYTAG, "called goToSleep");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            Log.i(Constant.KEYTAG, "called goToSleep 1  e:" + e);
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            Log.i(Constant.KEYTAG, "called goToSleep 2  e:" + e);
            e.printStackTrace();
        } catch(Exception e){
            Log.i(Constant.KEYTAG, "called goToSleep 3  e:" + e);

        }
    }

    private final BroadcastReceiver destroyActivityReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals("DESTROY")){
                if(islockviewCreate){
                    Log.i(Constant.KEYTAG,"islockviewCreate true removeView");
                    mWindowManager.removeView(mFloatLayout);
                    islockviewCreate=false;
                    mFloatLayout=null;
                    mFloatView=null;
                }

                finish();

            }

        }
    };


    private final BroadcastReceiver homePressReceiver = new BroadcastReceiver() {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if(reason != null&& reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {


                }
            }
        }
    };


    private final BroadcastReceiver mPowerKeyDownReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals("PKSPowerKeyDown") &&(islockviewCreate)){
                Log.i(Constant.KEYTAG,"PowerKeyDownBroadcastReceived");
                mScreenMovePowerKeyTime=SystemClock.uptimeMillis();
                Log.i(Constant.KEYTAG,"Key power pressed at "+mScreenMovePowerKeyTime);
                mScreenMovePowerKeyTriggered=true;
                if(islockviewCreate){
                    moveLockedScreen();
                }

            }


        }
    };


    private final BroadcastReceiver mPowerVolumedownReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals("PKSPowerKey and Volume_Down Down")){
                if(islockviewCreate==false){
                    mWindowManager.removeView(mFloatLayout);
                    createLockScreen();
                    Log.i(Constant.KEYTAG,"LockScreen Create by key power and VoluneDown");
                }

            }


        }
    };


    private final BroadcastReceiver mVolumeUpReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals("PKSVolumeUpPress")){
                mScreenMovedVolumeUpKeyTime=SystemClock.uptimeMillis();
                Log.i(Constant.KEYTAG,"Key VolumeUp pressed at "+mScreenMovePowerKeyTime);
                mScreenMoveVolumeUpKeyTriggered=true;
                if(islockviewCreate){
                    moveLockedScreen();
                }
            }
        }
    };

    private void moveLockedScreen(){

        if( !mScreenMoveVolumeDownKeyTriggered
                && mScreenMoveVolumeUpKeyTriggered && mScreenMovePowerKeyTriggered ){
            final long now = SystemClock.uptimeMillis();

            if( now <= mScreenMovedVolumeUpKeyTime+SCREENMOVE_DEBOUNCE_DELAY_MILLIS
                    && now <= mScreenMovePowerKeyTime+SCREENMOVE_DEBOUNCE_DELAY_MILLIS){
                mScreenMovePowerKeyTriggered=false;
                if(islockviewCreate){
                    mWindowManager.removeView(mFloatLayout);
                    islockviewCreate=false;
                    mFloatLayout=null;
                    mFloatView=null;
                    finish();
                    Log.i(Constant.KEYTAG,"LockScreen destroy by key power and VoluneUp");

                }


            }

        }

    }



    private void createLockScreen(){

///*
        wmParams = new WindowManager.LayoutParams();
        //mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        Log.i(Constant.KEYTAG, "mWindowManager--->" + mWindowManager);

        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        //wmParams.alpha=0.9f;
        wmParams.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        //inflater = LayoutInflater.from(getApplication());
        mFloatLayout = (RelativeLayout) inflater.inflate(R.layout.lock_layout, null);
        ImageView image = (ImageView) mFloatLayout.findViewById(R.id.lockscreen);

        bitmap=readBitMap(PKSActivity.this,R.drawable.lockscreen);
        image.setImageBitmap(bitmap);
        mFloatLayout.setClickable(true);
        mFloatLayout.setFocusable(true);

        mWindowManager.addView(mFloatLayout, wmParams);
        mFloatView = (View) mFloatLayout.findViewById(R.id.lockscreen);

        mFloatView.setFocusableInTouchMode(true);

        mFloatView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        islockviewCreate=true;
        //*/

        Log.i(Constant.KEYTAG,"LockScreen Create");
        mFloatView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                final boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
                switch(keyCode){
                    case KeyEvent.KEYCODE_VOLUME_DOWN:
                        if (down) {
                            if (!mScreenMoveVolumeDownKeyTriggered) {
                                mScreenMoveVolumeDownKeyTriggered=true;
                            }
                        } else {
                            mScreenMoveVolumeDownKeyTriggered = false;
                        }
                        break;
                    /*case KeyEvent.KEYCODE_VOLUME_UP:
                        if (down) {

                            if (!mScreenMoveVolumeUpKeyTriggered  && (event.getFlags() & KeyEvent.FLAG_FALLBACK) == 0) {
                                mScreenMoveVolumeUpKeyTriggered=true;
                                mScreenMovedVolumeUpKeyTime= event.getDownTime();
                                Log.i(TAG,"Volume_up down at "+mScreenMovedVolumeUpKeyTime);
                                moveLockedScreen();
                            }
                        } else {
                                mScreenMoveVolumeUpKeyTriggered=false;

                        }
                        break;*/
                }
                return true;
            }
        });
        //if(istouched==false) {
        //   istouched=true;

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

         //           if(istouched==true){
                        //pm.goToSleep(SystemClock.uptimeMillis(), 0, 0);
                        screenOff();
                        Log.i(Constant.KEYTAG, "LockView screenOff()~~~~~");
         //           }

                }
            }, 3000);//3秒后执行Runnable中的run方法

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
          //          if(istouched==true){
                        Log.i(Constant.KEYTAG, "LockView remove  3050");
                        if(islockviewCreate){
                            Log.i(Constant.KEYTAG,"islockviewCreate true removeView  3050");
                            try{
                                mWindowManager.removeView(mFloatLayout);
                                islockviewCreate=false;
                                mFloatLayout=null;
                                mFloatView=null;
                            }catch(IllegalArgumentException e){
                                Log.i(Constant.KEYTAG, "e2 :" + e);
                            }
                        }

                        finish();
         //           }
                }
            }, 3050);
        //}

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void onDetachedFromWindow() {//防止重pks去不掉

        Log.i(Constant.KEYTAG, "onDetachedFromWindow ");
        if(islockviewCreate){
            Log.i(Constant.KEYTAG,"onDetachedFromWindow islockviewCreate true removeView");
            try{
                mWindowManager.removeView(mFloatLayout);
                islockviewCreate=false;
                mFloatLayout=null;
                mFloatView=null;
            }catch(IllegalArgumentException e){
                Log.i(Constant.KEYTAG, "onDetachedFromWindow  e1 :" + e);
            }
        }

        super.onDetachedFromWindow();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        istouched=false;
        unregisterReceiver(destroyActivityReceiver);
        unregisterReceiver(homePressReceiver);
        unregisterReceiver(mPowerKeyDownReceiver);
        unregisterReceiver(mVolumeUpReceiver);
        //unregisterReceiver(mPowerVolumedownReceiver);

        Log.i(Constant.KEYTAG,"Activitydestroy");
        mFloatLayout=null;
        mFloatView=null;

        //Runtime.getRuntime().gc();

    }


}

