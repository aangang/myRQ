package com.ape.sugarrequirement.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ape.sugarrequirement.util.Constant;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(Constant.OTATAG, "BootReceiver  onReceive");

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i(Constant.OTATAG, "bootcomplete received");
            Intent intent1 = new Intent();
           // intent1.setAction("android.intent.action.OTAAPKSERVICE");
            intent1.setClassName(context, "com.ape.sugarrequirement.ota.OtaApksService");
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(intent1);

            Log.i(Constant.EYETAG, "bootcomplete received");
            Intent intent2 = new Intent();
            intent2.setClassName(context, "com.ape.sugarrequirement.service.SugarRequirementService");
            context.startService(intent2);
        } else if (Constant.SMART_KEY_EVENT.equals(intent.getAction())){
            Log.i(Constant.KEYTAG,"receive smart action");

        }else {
            Log.w(Constant.OTATAG, "onReceive: could not handle: " + intent);
        }
    }
}
