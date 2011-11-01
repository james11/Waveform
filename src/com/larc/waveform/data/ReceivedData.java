package com.larc.waveform.data;

import java.util.Arrays;

import android.util.Log;


public class ReceivedData {
	
	private static final String TAG = "ReceivedData";
	private static final boolean VERBOSE = true;
	
	private static final int BUFFER_SIZE = 1024*100;
	private static final byte DEFAULT_VALUE = (byte) 0x80;
	
	
	private byte[] mDataBuffer = new byte[BUFFER_SIZE];
	
	private Object mLock = new Object();
	private int mPointer = 0;
	private int mGetPointer = 0;

	public void putData(int length, byte[] data){
//		if (VERBOSE)
//			Log.d(TAG, "putData: size = "+length);
		
		int currentPosition = 0;
//		synchronized(mLock){
			currentPosition = mPointer;
//		}
		
		if(currentPosition + length <= BUFFER_SIZE ){
		
		} else {
			ReceivedDataSaver.saveData(mDataBuffer, 0, currentPosition);
			
			
			Arrays.fill(mDataBuffer, DEFAULT_VALUE);
//			synchronized(mLock){
				mPointer = 0;
				mGetPointer = 0;
				currentPosition = 0;
//			}
		}
		
		for(int i=0; i< length; i++){
			mDataBuffer[i+currentPosition] = data[i];
		}
		
//		synchronized(mLock){
			mPointer += length;
//		}
	}
	
	
	public int[] getLatestData(int preferedSize, int sampleFactor){
		
		
		int[] data = null;
		int start = 0;
		int end = 0;
		int avaliableSize = 0;
//		synchronized(mLock){
			start = mGetPointer;
			end = mPointer;
			end = (end + start ) / 2;
			avaliableSize = end - start;
//		}
//		if (VERBOSE)
//			Log.v(TAG, "getLatestData: avaliableSize = " + avaliableSize);

		
		if(avaliableSize <= 0 || start >= BUFFER_SIZE ){
			Log.v("Waveform", "no data avaliable");
			mGetPointer = 0;
			return null;
		} else if (preferedSize >= avaliableSize){
			data = new int[avaliableSize];
			for (int i=0 ; i< avaliableSize; i++){
				data[i] = (int) mDataBuffer[start + i] & 0xFF;
			}
			mGetPointer = end;
		} else {
			// availableSize > preferedSize
			int space = 1;
			do{
				space ++;
			}while (space*preferedSize < avaliableSize);
			space --;
			
			int actualSize = preferedSize;
			data = new int[actualSize];
			for (int i=0 ; i< actualSize; i++){
				data[i] = (int) mDataBuffer[start + i*space] & 0xFF;
			}
			mGetPointer = end;
		}
//		if (VERBOSE && data != null)
//			Log.d(TAG, "getLatestData: size = " + data.length);
		return data;
	}
}
