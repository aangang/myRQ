package com.ape.sugarrequirement.ota;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import android.os.Environment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import com.ape.sugarrequirement.ota.model.APKName;
import com.ape.sugarrequirement.ota.model.SettingsDBName;
import com.ape.sugarrequirement.util.Constant;
import com.ape.sugarrequirement.util.OTAParser;

import android.provider.Settings;

public class OtaApksService extends Service {
	
	private Context mContext;
	private boolean doInstall = false;
    @Override
    public IBinder onBind(Intent intent) {
            // TODO Auto-generated method stub
            return null;
    }
    
    @Override
    public void onCreate() {
            // TODO Auto-generated method stub
            super.onCreate();
            Log.i(Constant.OTATAG, "service onCreate");
            mContext = this;


    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            // TODO Auto-generated method stub
            Log.i(Constant.OTATAG, "service onStartCommand  ");

            String internal_build_version = android.os.SystemProperties.get("ro.internal.build.version","");
            String internal_build_version_last = android.os.SystemProperties.get("persist.sys.ota.lastversion","");
            if(!android.os.SystemProperties.get("ro.pt.first_add_run_ota","").equals("yes")){
                if(internal_build_version_last == null || internal_build_version_last == ""){
                    Log.i(Constant.OTATAG,"first boot up  return and set persist.sys.ota.lastversion internal_build_version:" + internal_build_version);
                    android.os.SystemProperties.set("persist.sys.ota.lastversion",internal_build_version);
                    return super.onStartCommand(intent, flags, startId);
                }
            }

            if(internal_build_version.equals(internal_build_version_last)){
                Log.i(Constant.OTATAG,"version equal return with out doing anything");
                return super.onStartCommand(intent, flags, startId);
            }
            if(!internal_build_version.equals(internal_build_version_last) || android.os.SystemProperties.get("ro.pt.first_add_run_ota","").equals("yes")){
                //loadOtaSettings(mContext,Constant.PARTNER_SETTINGS_OTA_PATH);
                     Thread settingsThread = new Thread() {
                        @Override
                        public void run() {
                              loadOtaSettings(mContext,Constant.PARTNER_SETTINGS_OTA_PATH);
                        }
                     };
                     settingsThread.start();
                //ota apks
                doInstall = true;
            }else{
                Log.i(Constant.OTATAG,"no need to ota apk and settings return");
                return super.onStartCommand(intent, flags, startId);
            }


            File app_file = new File(Constant.APP_CONFIG);
            
            List<APKName> apkList = null;
            try {
            	if(app_file.exists()){
            		apkList = OTAParser.parseAPP(app_file);
            	}else{
            		Log.i(Constant.OTATAG,"app_config.xml not exist");
            		return super.onStartCommand(intent, flags, startId);
            	}
	        } catch (Exception e1) {
				e1.printStackTrace();
				Log.i(Constant.OTATAG,"app_config.xml parse fail");
				return super.onStartCommand(intent, flags, startId);
	        }
            
            if(doInstall){
            	for(final APKName apk: apkList){
            		Log.i(Constant.OTATAG, "apk:" + apk.getAppName());
                    Thread thread = new Thread() {
                        @Override 
                        public void run() {
                          try {
                            String apkName = Constant.PRE_INSTALL_DIR + apk.getAppName() + "/" + apk.getAppName() + ".apk";
                            Log.i(Constant.OTATAG, "pm  install apk :" + apkName);
                            //Runtime.getRuntime().exec("pm install -r -f /system/pre-install/Baofeng/Baofeng.apk");
                            //Runtime.getRuntime().exec("pm install -r -f " + apkName);

                              //Runtime.getRuntime().exec("pm install -i com.ape.sugarrequirement -r -f -user 0 " + apkName);
                              Runtime.getRuntime().exec("pm install -i com.ape.sugarrequirement -r -f --user 0 " + apkName);
                          } catch (IOException e) {
                            e.printStackTrace();
                          }
                        }
                     };
                     thread.start();
            	}
            }

            android.os.SystemProperties.set("persist.sys.ota.lastversion",internal_build_version);           
            return super.onStartCommand(intent, flags, startId);
    }

    public void loadOtaSettings(Context context, String ota_file){
        Log.w(Constant.OTATAG, "loadOtaSettings ");

        final File settingsFile = new File(Environment.getRootDirectory(), ota_file);
        List<SettingsDBName> dbList = null;
        try {
            if(settingsFile.exists()){
                dbList = OTAParser.parseSettings(settingsFile);
            }else{
                Log.i(Constant.OTATAG,"settings-config-ota.xml not exist");
            }
            for (SettingsDBName db: dbList) {

                Log.w(Constant.OTATAG, "    <setting type=\"" + db.getArea() + "\" name=\"" + db.getAppName() + "\" value=\"" + db.getValue() + "\"/>");
               if("secure".equals(db.getArea())) {
                   Settings.Secure.putString(context.getContentResolver(), db.getAppName(), db.getValue());
               } else if("global".equals(db.getArea())) {
                   Settings.Global.putString(context.getContentResolver(), db.getAppName(), db.getValue());
               } else {
                   Settings.System.putString(context.getContentResolver(), db.getAppName(), db.getValue());
               }
            }
        } catch (XmlPullParserException e) {
            Log.w(Constant.OTATAG, "Exception in settings-conf parser " + e);
        } catch (IOException e) {
            Log.w(Constant.OTATAG, "", e);
        } catch (Exception e){
            Log.w(Constant.OTATAG, "", e);
        }

    }

    
    final static long LLIMTSIZE = 1024*1024*10;
    public static boolean copyFile2(File srcFile, File rootDir, String newFileName) {
        boolean result = true;

        if (!srcFile.exists()) {
            System.out.println("src not exist");
            result = false;
        } else if (!rootDir.exists()) {
            System.out.println("dir not exist");
            result = false;
        } else if (newFileName == null) {
            System.out.println("file name is null");
            result = false;
        } else {
            try {

                File fileDir;
                int index = newFileName.lastIndexOf(".apk");
                Log.i(Constant.OTATAG, "yolo index=" + index);
                if (index > 0) {
                    String fileName = newFileName.substring(0, index);
                    fileDir = new File(rootDir, fileName);
                    boolean ret = fileDir.mkdirs();
                } else {
                    fileDir = rootDir;
                }
                Log.i(Constant.OTATAG, "yolo fileDir=" + fileDir);
                Log.i(Constant.OTATAG, "yolo newFileName=" + newFileName);

                File file = new File(fileDir, newFileName);
                if (!file.exists())
                    file.createNewFile();
                FileChannel fcin = new FileInputStream(srcFile).getChannel();
                FileOutputStream fcoutstream = new FileOutputStream(file)    ;
                FileDescriptor fcoutfd = fcoutstream.getFD();
             FileChannel fcout = fcoutstream.getChannel();

             long size = fcin.size();
                Log.i(Constant.OTATAG, "yolo fcin size=" + size);

             ByteBuffer bb = ByteBuffer.allocateDirect((int)LLIMTSIZE);
             if(size > LLIMTSIZE) {
                 while(fcin.read(bb) != -1) {
                     bb.flip();
                     fcout.write(bb);
                     bb.clear();
                }
            } else {
                fcin.transferTo(0,fcin.size(),fcout);
            }

               fcoutfd.sync();

            // fcout.transferFrom(fcin,0,fcin.size());  
            Log.i(Constant.OTATAG, "yolo fcout size=" + fcout.size());

               if(size != fcout.size()){
             result = false;
               }
            fcin.close();
            fcout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
               result = false;
        } catch (IOException e) {
            e.printStackTrace();
               result = false;
        }
    }
    return result;
 }

}
