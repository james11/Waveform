package com.larc.waveform.data;

import android.content.Context;
import android.util.Log;

public class LocationData {

	private static final String TAG = "LocationData";

	private final double[] mLocationDataBuffer = new double[BUFFER_SIZE];
	protected static int BUFFER_SIZE = 4;
	private int mPointer = 0;
	private static LocationData lInstance;
	private DataFileManager mDataFileManager;

	private LocationData() {
		super();
		mDataFileManager = DataFileManager.getInstance();
		mDataFileManager.setId("");
		mDataFileManager.setName("");
		mDataFileManager.setPhoneNumber("");
	}

	public static LocationData getInstance() {
		if (lInstance == null) {
			lInstance = new LocationData();
		}
		return lInstance;
	}

	public synchronized void write(double data) {

		int currentPosition = 0;
		currentPosition = mPointer;

		// Check if Buffer us full .
		if (currentPosition + 1 <= BUFFER_SIZE - 1) {

		} else {
			onBufferFull(mLocationDataBuffer, 0, BUFFER_SIZE);
			// reset Buffer's pointer .
			mPointer = 0;
			currentPosition = 0;
		}

		// Put received Data into mDataBuffer .
		mLocationDataBuffer[currentPosition + 1] = data;

		mPointer += 1;
	}

	/** Do different function under different case when Buffer is full . **/
	protected void onBufferFull(double[] bufferData, final int offset,
			final int length) {
		mDataFileManager.saveDoubleData(bufferData, offset, length);
		Log.v(TAG, "saveDoubleData = ");
	}
}
