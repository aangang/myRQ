package com.ape.sugarrequirement.ota.model;

import android.util.Log;

public class SettingsDBName {

	private String name;
	private String area;
	private String value;

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}


	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}


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
			Log.i("otaapk", "app name:" + name + " area:" + area + " value:" + value);
		return super.toString();
	}
	
}
