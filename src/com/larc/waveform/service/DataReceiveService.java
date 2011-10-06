package com.larc.waveform.service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.larc.bluetoothconnect.BluetoothService;

public class DataReceiveService extends BluetoothService {
	
	private static final byte SEPERATOR = 'd';
	private ArrayList<Integer> mDataArray = new ArrayList<Integer>();
	private WritingThread mWritingThread;
	
	public DataReceiveService(Context context, Handler handler) {
		super(context, handler);
	}

	@Override
	protected void onDataRead(int length, byte[] data) {
		int offset = 0;
		byte[] value = new byte[5];
		for(int i=0; i< length; i++){
			if(data[i] == SEPERATOR){
				int size = i - offset;
				if(size < 5){
					value = Arrays.copyOfRange(data, offset, i);
					String s = new String(value);
					int v = Integer.parseInt(s);
					mDataArray.add(v);
				}
				offset = i+1;
			}
		}
	}
	
	public int getCurrentValue(){
		int index = mDataArray.size();
		if(index > 0 ){
			return mDataArray.get(index - 1);
		} else {
			return 0;
		}
	}
	
	public synchronized void startSendingFakeData(){
		if(mWritingThread == null){
			mWritingThread = new WritingThread();
			Log.d("Test", "start sending data");
			mWritingThread.start();
		}
	}
	
	public synchronized void stopSendingFakeData(){
		if(mWritingThread != null){
			mWritingThread.stopRunning();
		}
	}
	
	private class WritingThread extends Thread{
		
		private boolean isRunning = true;
		String pattern = "000d010d020d040d050d040d020d010d000d-10d-20d-40d-50d-40d-20d-10d";
			
		@Override
		public void run(){
			isRunning = true;
			int patternLength = pattern.length();
			int offset = 0;
			while(isRunning){
				byte[] data = new byte[4];
				if(offset + 4 > patternLength){
					offset = 0;
				}
				String s = pattern.substring(offset, offset+4);
				data = s.getBytes(Charset.forName("ASCII"));
				if(data != null){
					DataReceiveService.this.write(data);
				}
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			mWritingThread = null;
		}

		public void stopRunning() {
			isRunning = false;
		}
	}
}
