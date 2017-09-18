package com.ape.sugarrequirement;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ape.sugarrequirement.util.Constant;

public class MainActivity extends Activity implements View.OnClickListener {

    Button eye;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intiViews();
    }

    private  void intiViews(){
        eye = (Button) findViewById(R.id.set_eyeprevent);
        eye.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if(v.getId() == R.id.set_eyeprevent ){
            Log.i(Constant.EYETAG, "set clicked");

            int ep_status = Settings.System.getInt(getContentResolver(), Constant.EYE_PREVENT_STATUS_QCOM, -1);
            if(ep_status == 1){
                Settings.System.putInt(this.getContentResolver(), Constant.EYE_PREVENT_STATUS_QCOM, 0);
                Log.i(Constant.EYETAG, "set EYE_PREVENT_STATUS_QCOM 0");
            }else{
                Settings.System.putInt(this.getContentResolver(), Constant.EYE_PREVENT_STATUS_QCOM, 1);
                Log.i(Constant.EYETAG, "set EYE_PREVENT_STATUS_QCOM 1");
            }
            //Intent intent = new Intent();
            //intent.setAction(Constant.QCOM_EP_ACTION);
            //startActivity(intent);
            Log.i(Constant.EYETAG, "Settings.System.EYE_PREVENT_STATUS_QCOM  set to 1");
        }
    }


}
