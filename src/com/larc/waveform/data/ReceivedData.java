package com.larc.waveform.data;

import java.util.Arrays;

import com.larc.waveform.WaveformActivity;

import android.util.Log;

public class ReceivedData {

	private static final String TAG = "ReceivedData";
	private static final boolean VERBOSE = true;

	private static final int BUFFER_SIZE = 1024 * 400;
	private static final byte DEFAULT_VALUE = (byte) 0x80;

	private byte[] mDataBuffer = new byte[BUFFER_SIZE];

	private Object mLock = new Object();
	private int mPointer = 0;
	private int mGetPointer = 0;

	public int mMaxData = 0;
	public int mMinData = 0;
	public int mPeakCnt = 0;
	private boolean mIncrease = true;
	private boolean mLastIncrease = true;
	private boolean mSlopZero = false;

	long mLastqTime = 0;
	public int mHeartRate;
	private WaveformActivity mWaveformActivity;
	private byte[] mLastData = new byte[BUFFER_SIZE];

	public void putData(int length, byte[] data) {
		// if (VERBOSE)
		// Log.d(TAG, "putData: size = "+length);

		int currentPosition = 0;
		// Only one Thread is allowed to entry synchronized{} block at one time
		// synchronized(mLock){
		currentPosition = mPointer;
		// }

		if (currentPosition + length <= BUFFER_SIZE) {

			// if mDataBuffer is filled , save data to files and reset
			// mDataBuffer to null .
		} else {
			ReceivedDataSaver.saveData(mDataBuffer, 0, currentPosition);

//			Arrays.fill(mDataBuffer, DEFAULT_VALUE);
			// synchronized(mLock){
			mPointer = 0;
			mGetPointer = 0;
			currentPosition = 0;
			// }
		}

		// Arrays.fill(mLastData, DEFAULT_VALUE);
		// Put received Data into mDataBuffer .
		for (int i = 0; i < length; i++) {
			mDataBuffer[i] = data[i];
		}
		

		// if ((data[i] & 0xFF) >= (mLastData[i] & 0xFF)) {
		// mMaxData = (int) data[i] & 0xFF;
		// mIncrease = true;
		// }
		// // if ((data[i] & 0xFF) <= (data[i - 1] & 0xFF)) {
		// else {
		// mMinData = (int) data[i] & 0xFF;
		// mIncrease = false;
		// }
		//
		// if (mIncrease != mLastIncrease) {
		// mSlopZero = true;
		// } else {
		// mSlopZero = false;
		// }
		//
		// if (mMaxData >= 180 && mSlopZero == true) {
		// long qTime = System.currentTimeMillis();
		// mHeartRate = (int) (60 * (1 / (qTime - mLastqTime)));
		// // mWaveformActivity.mRate = mHeartRate;
		// mLastqTime = qTime;
		// mMaxData = 0;
		// }
		// mLastData[i] = data[i];
		// mLastIncrease = mIncrease;
		// }
		//
		// // synchronized(mLock){
		// mPointer += length;
		// // }
	}

//	public int getRate() {
//		return mHeartRate;
//	}

	public int[] getLatestData(int preferedSize, int sampleFactor) {

		int[] data = null;
		int start = 0;
		int end = 0;
		int avaliableSize = 0;
		// synchronized(mLock){
		start = mGetPointer;
		end = mPointer;
		end = (end + start) / 2;
		avaliableSize = end - start;
		// }
		// if (VERBOSE)
		// Log.v(TAG, "getLatestData: avaliableSize = " + avaliableSize);

		if (avaliableSize <= 0 || start >= BUFFER_SIZE) {
			Log.v("Waveform", "no data avaliable");
			mGetPointer = 0;
			return null;
		} else if (preferedSize >= avaliableSize) {
			data = new int[avaliableSize];
			// Convert Data form byte to integer .
			for (int i = 0; i < avaliableSize; i++) {
				data[i] = (int) mDataBuffer[start + i] & 0xFF;
			}
			mGetPointer = end;
		} else {
			// availableSize > preferedSize
			int space = 1;
			// if we have more data then we actually need , we choose the
			// numbers we need "normalized" from we have .
			do {
				space++;
			} while (space * preferedSize < avaliableSize);
			space--;

			int actualSize = preferedSize;
			data = new int[actualSize];
			for (int i = 0; i < actualSize; i++) {
				data[i] = (int) mDataBuffer[start + i * space] & 0xFF;
			}

			mGetPointer = end;
		}
		// if (VERBOSE && data != null)
		// Log.d(TAG, "getLatestData: size = " + data.length);
		return data;
	}
}
