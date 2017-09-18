package com.ape.sugarrequirement.ota.model;

import android.util.Log;

public class APKName {

	private String name;

	public String getAppName() {
		return name;
	}

	public void setAppName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		if(name != null)
			Log.i("otaapk", "app name:" + name);
		return super.toString();
	}
	
}
