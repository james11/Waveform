package com.larc.waveform.util;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

public class EnvironmentUtils {
	private static final String KEY_USE_EXT_CACHE = "useExternalCache";
	
	public static File getExtAppDir(String packageName){
		String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
		String appPath = sdpath + "/Android/data/"+packageName+"/files/";
		File dir = new File(appPath);
		if(!dir.exists()){
			dir.mkdirs();
		}
		return dir;
	}
	public static File getExtAppCacheDir(String packageName){
		String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
		String appPath = sdpath + "/Android/data/"+packageName+"/files/cache/";
		File dir = new File(appPath);
		if(!dir.exists()){
			dir.mkdirs();
		}
		return dir;
	}
	
	public static File getDefaultAppCacheDir(Context context){
		SharedPreferences settings = 
        	PreferenceManager.getDefaultSharedPreferences(context);
		boolean ext = settings.getBoolean(KEY_USE_EXT_CACHE, true);
		if(isExternalStorageWriteable() && ext){
			return getExtAppCacheDir(context.getPackageName());
		}else{
			return context.getCacheDir();
		}		
	}
	
	public static boolean isExternalStorageAvailable(){
		boolean isExternalStorageAvailable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    isExternalStorageAvailable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    isExternalStorageAvailable = true;
		    //mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    isExternalStorageAvailable = false;
		}
		return isExternalStorageAvailable;
	}
	public static boolean isExternalStorageWriteable(){
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    mExternalStorageWriteable = true;
		} else {
		    mExternalStorageWriteable = false;
		}
		return mExternalStorageWriteable;
	}
	
}
