package com.larc.waveform.data;

import java.util.Arrays;

import android.util.Log;


public class ReceivedData {
	
	private static final int BUFFER_SIZE = 1024*1024*4;
	private static final byte DEFAULT_VALUE = (byte) 0x80;
	
	
	private byte[] mDataBuffer = new byte[BUFFER_SIZE];
	
	private Object mLock = new Object();
	private int mPointer = 0;
	private int mGetPointer = 0;

	public void putData(int length, byte[] data){
		
		int currentPosition = 0;
//		synchronized(mLock){
			currentPosition = mPointer;
//		}
		
		if(currentPosition + length <= BUFFER_SIZE ){
		
		} else {
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
	
	
	public int[] getLatestData(int size, int sampleFactor){
		
		int[] data;
		int start = 0;
		int end = 0;
//		synchronized(mLock){
			start = mGetPointer;
			end = mPointer;
			end = (end + start)/2;
			mGetPointer = end;
//		}
		
		if(end - start <= 0 || start >= BUFFER_SIZE ){
			Log.v("Waveform", "null");
			mGetPointer = 0;
			return null;
		} else {
			int validSize = Math.min(end - start, size);
			int space = 1;//size / validSize;
			data = new int[validSize];
			for (int i=0 ; i< validSize; i++){
				data[i] = (int) mDataBuffer[start + i*space] & 0xFF;
			}
			
			return data;
		}
//			data = new int[8];
//			data[0] = (int) mDataBuffer[start] & 0xFF;
//			data[1] = (int) mDataBuffer[start] & 0xFF;
//			data[2] = (int) mDataBuffer[start] & 0xFF;
//			data[3] = (int) mDataBuffer[start] & 0xFF;
//			data[4] = (int) mDataBuffer[start] & 0xFF;
//			data[5] = (int) mDataBuffer[start] & 0xFF;
//			data[6] = (int) mDataBuffer[start] & 0xFF;
//			data[7] = (int) mDataBuffer[start] & 0xFF;
//			return data;
	}
}
