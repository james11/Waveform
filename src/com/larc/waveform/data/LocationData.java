package com.larc.waveform.data;

import android.os.Handler;
import android.util.Log;

public class LocationData {

	private static final String TAG = "LocationData";

	// Adjust variable .
	protected static int BUFFER_SIZE = 50;
	public int LOCATION_BUFFER_SAVE_PERIOD = 1000 * 60;

	private final double[] mLocationDataBuffer = new double[BUFFER_SIZE + 1];
	private int mPointer = 0;
	private int mCurrentPosition = 0;
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

		mCurrentPosition = 0;
		mCurrentPosition = mPointer;

		// Check if Buffer us full .
		if (mCurrentPosition + 1 <= BUFFER_SIZE) {

		} else {
			onBufferFull(mLocationDataBuffer, 1, BUFFER_SIZE);
			// reset Buffer's pointer .
			mPointer = 0;
			mCurrentPosition = 0;
		}

		// Put received Data into mDataBuffer .
		mLocationDataBuffer[mCurrentPosition + 1] = data;

		mPointer += 1;
	}

	public Handler mLocationDataHandler;

	public void setHandler() {
		mLocationDataHandler = new Handler();
	}

	public Runnable mLocationDataRunnable = new Runnable() {
		public void run() {

			mDataFileManager
					.saveDoubleData(mLocationDataBuffer, 1, BUFFER_SIZE);
			mPointer = 0;
			mCurrentPosition = 0;
			Log.v(TAG, "save Location Data By Runnable");

			mLocationDataHandler.postDelayed(this, LOCATION_BUFFER_SAVE_PERIOD);

		}
	};

	/** Do different function under different case when Buffer is full . **/
	protected void onBufferFull(double[] bufferData, final int offset,
			final int length) {
		mDataFileManager.saveDoubleData(bufferData, offset, length);
		Log.v(TAG, "save Location Data By Full Buffer");
	}
}