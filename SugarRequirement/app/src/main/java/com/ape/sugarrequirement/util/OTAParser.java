package com.ape.sugarrequirement.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;
import android.util.Xml;

import com.ape.sugarrequirement.ota.model.APKName;
import com.ape.sugarrequirement.ota.model.SettingsDBName;

public class OTAParser {

	public static List<APKName> parseAPP(File app_file) throws Exception {
		List<APKName> mList = null;
		APKName apk = null;
		
		if(app_file==null || !app_file.exists()){
			Log.i(Constant.OTATAG,"APKParser  app_file errors!");
			return null;
		}
		XmlPullParser xpp = Xml.newPullParser();
		InputStream is = new FileInputStream(app_file);
		xpp.setInput(is,"UTF-8"); 
	    int eventType = xpp.getEventType();  
	
	    while (eventType != XmlPullParser.END_DOCUMENT){  
	    	switch (eventType) {  
	    	    case XmlPullParser.START_DOCUMENT:  
	    	        mList = new ArrayList<APKName>();
	    	    break;  
	    	    case XmlPullParser.START_TAG:  
	    		    if (xpp.getName().equals("apk")) {
	    		        apk = new APKName();  
	    	    	}else if (xpp.getName().equals("name")) {  
	    		        eventType = xpp.next();
	    		    	apk.setAppName(xpp.getText());  
	    		    } 
	    		break;
	    		case XmlPullParser.END_TAG:  
	    		    if (xpp.getName().equals("apk")) { 
	    	           mList.add(apk);
	    		       apk = null;  
	    		    }  
	    		break; 
	    	}
	    	eventType = xpp.next(); 
	    }
	    return mList;
	}

	public static List<SettingsDBName> parseSettings(File db_file) throws Exception {
		List<SettingsDBName> mList = null;
        SettingsDBName setting = null;

		if(db_file==null || !db_file.exists()){
			Log.i(Constant.OTATAG,"OTAParser  db_file errors!");
			return null;
		}
		XmlPullParser xpp = Xml.newPullParser();
		InputStream is = new FileInputStream(db_file);
		xpp.setInput(is,"UTF-8");
		int eventType = xpp.getEventType();

		while (eventType != XmlPullParser.END_DOCUMENT){
			switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					mList = new ArrayList<SettingsDBName>();
					break;
				case XmlPullParser.START_TAG:
					if (xpp.getName().equals("setting")) {
                        setting = new SettingsDBName();
                        setting.setArea(xpp.getAttributeValue(0));
                        setting.setAppName(xpp.getAttributeValue(1));
                        setting.setValue(xpp.getAttributeValue(2));
					}
					break;
				case XmlPullParser.END_TAG:
					if (xpp.getName().equals("setting")) {
						mList.add(setting);
                        setting = null;
					}
					break;
			}
			eventType = xpp.next();
		}
		return mList;
	}
}
