package com.larc.waveform.service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.larc.bluetoothconnect.BluetoothService;
import com.larc.waveform.data.ReceivedData;

public class DataReceiveService extends BluetoothService {
	
	public static final int CHANNEL_EEG = 0;
	public static final int CHANNEL_DBS = 1;
	
	private static final byte SEPERATOR = 'd';

	private static DataReceiveService mInstance;
	
	private ArrayList<Integer> mDataArray = new ArrayList<Integer>();
	private ArrayList<int[]> mIntArrayList = new ArrayList<int[]>();
//	private WritingThread mWritingThread;
	private int mCurrentValue;
	
	private ArrayList<Byte> mByteArray = new ArrayList<Byte>();
	private ByteArrayBuffer byteBuffer = new ByteArrayBuffer(4);
	private ReceivedData mReceivedData = new ReceivedData();
	
	
	private DataReceiveService(Context context, Handler handler) {
		super(context, handler);
		mInstance  = this;
	}
	
	private DataReceiveService(Context context) {
		super(context);
		mInstance = this;
	}
	
	public static DataReceiveService getInstance(Context context){
		if(mInstance == null){
			mInstance = new DataReceiveService(context);
		}
		return mInstance;
	}
	
	@Override
	protected void onDataRead(int length, byte[] data) {
		super.onDataRead(length, data);
		mReceivedData.putData(length, data);
	}
	
	/**
	 * This function will be called periodically.
	 * 
	 * @return
	 */
	public int[] getCurrentData(int channel, int preferedSize){
		Log.v(TAG, "getCurrentData "+preferedSize);
//		int[] data = new int[preferedSize];
//		Arrays.fill(data, new Random().nextInt() % 256);
//		return data;
		return mReceivedData.getLatestData(preferedSize ,1);
	}
	
	/****    acturally used block   ****/
	
//
//	
//	private void decodeData(byte[] data, int length){
//		int offset = 0;
//		int value = 0;
//		
//		byte[] temp = new byte[5];
//		for(int i=0; i< length; i++){
//			if(data[i] == SEPERATOR){
//				int size = i - offset;
//				if(size < 5){
//					temp = Arrays.copyOfRange(data, offset, i);
//					String s = new String(temp);
//					try {
//						value = Integer.parseInt(s);
//					} catch (NumberFormatException e) {
//						e.printStackTrace();
//					}
//				}
//				offset = i+1;
//			}
//		}
//		
//		mDataArray.add(value);
//	}
	
//	public int getCurrentValue(){
//		return mCurrentValue;
//	}
	
//	public synchronized void startSendingFakeData(){
//		if(mWritingThread == null){
//			mWritingThread = new WritingThread();
//			Log.d("Test", "start sending data");
//			mWritingThread.start();
//		}
//	}
//	
//	public synchronized void stopSendingFakeData(){
//		if(mWritingThread != null){
//			mWritingThread.stopRunning();
//		}
//	}
//	
//	private class WritingThread extends Thread{
//		
//		private boolean isRunning = true;
//		String pattern = "000d010d020d040d050d040d020d010d000d-10d-20d-40d-50d-40d-20d-10d";
//			
//		@Override
//		public void run(){
//			isRunning = true;
//			int patternLength = pattern.length();
//			int offset = 0;
//			
//			
//			
//			while(isRunning){
//				long currentTime = System.currentTimeMillis()/100;
//				double v = Math.sin((double)currentTime) * 50;
//				int value = (int) v;
//				String s = value + "d";
//				
//				byte[] data = new byte[4];
//				if(offset + 4 > patternLength){
//					offset = 0;
//				}
////				String s = pattern.substring(offset, offset+4);
//				
//				
//				
//				data = s.getBytes(Charset.forName("ASCII"));
//				if(data != null){
//					DataReceiveService.this.write(data);
////					Log.v("Test","write: "+new String(data) );
//				}
//				offset +=4;
////				try {
//////					Thread.sleep(10);
////				} catch (InterruptedException e) {
////					e.printStackTrace();
////				}
//			}
//			mWritingThread = null;
//		}
//
//		public void stopRunning() {
//			isRunning = false;
//		}
//	}
}
