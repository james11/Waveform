package com.larc.waveform.data;

import java.util.ArrayList;

public class ReceivedData {
	private ArrayList<Integer> mData = new ArrayList<Integer>();
	
	public ArrayList<Integer> getDataArray(){
		return mData;
	}
	
	public void add(int value){
		mData.add(value);
	}
	
	public int[] getData(int size){
		int[] data = new int[size];
		int offset = mData.size() - size;
		int bound = Math.min(size, mData.size());
		for(int i=0 ; i < bound; i++){
			if(i+offset<0){
				data[i] = 0;
			}else{
				data[i] = mData.get(i+offset);
			}
		}
		return data;
	}
}
