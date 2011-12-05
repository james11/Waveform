package com.larc.waveform;

import android.app.Application;

public class WaveformApplication extends Application{
	private static Application sIntance;
	
    public void onCreate(){
    	super.onCreate();
    	sIntance = this;
    }

    public static Application getInstance(){
    	return sIntance;
    }
}
