package com.larc.waveform.service;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.os.Handler;

import com.larc.bluetoothconnect.BluetoothService;

public class DataReceiveService extends BluetoothService {
	
	private static final byte SEPERATOR = 'd';
	private ArrayList<Integer> mDataArray = new ArrayList<Integer>();
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
	
}
