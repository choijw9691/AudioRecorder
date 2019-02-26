package com.ebook.epub.viewer;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.provider.Settings.Secure;

public class DeviceInfoUtil {
	
	public static String getDeviceInfo(){
		return Build.DEVICE;
	}
	
	public static String getDeviceModel(){
		return Build.MODEL;
	}
	
	public static String getOSVersion(){
		return Build.VERSION.RELEASE;
	}
	
	public static String getApplicationVersion(Context context) {
		PackageManager manager = context.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getDevideId(Context context){
		return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}
	
}
