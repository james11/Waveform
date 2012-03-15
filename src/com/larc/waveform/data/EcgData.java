package com.larc.waveform.data;

import android.util.Log;

public class EcgData extends BufferedByteData {

	public EcgData() {
		super(BufferedByteData.BUFFER_SIZE = ECG_BUFFER_SIZE);
		mPeakDetector = new PeakDetector();
	}

	private static final String TAG = "EcgData";

	private static final int ECG_BUFFER_SIZE = 1024 * 30;
	// private static final int SATURATION_REGION = 20;

	// private static final int BASE = 128;
	// private long mLastPeakTime = 0;
	// private int mMaxData = 0;
	// private int mMinData = 255;
	// private int mDetectionRegionSize = 0;
	// private int mPeakCnt = 0;
	// private boolean mSaturation = false;
	// private boolean mIncrease = true;
	// private boolean mLastIncrease = true;
	// private boolean mSlopZero = false;

	private int mRate;
	private int mLast10SecRate = 90;
	// private int mLast20SecRate = 70;
	// private boolean mChecking = false;

	public int mDeltaRate;
	public int mHeartRate;
	public EcgListener mListener;

	private final PeakDetector mPeakDetector;

	/**
	 * Create an interface "EcgListener" which contain function
	 * "onHeartBeatStop()" and "onEcgBufferFull()"
	 **/
	public interface EcgListener {
		public void onHeartBeatStop();

		public void onEcgBufferFull(byte[] bufferData, int offset, int length);
	}

	/** Count mHeartRate using detected AverageInterval in PeakDetector.java . **/
	public int getHeartRate() {
		long interval = mPeakDetector.getAverageInterval();
		Log.v(TAG + ": Heart", "interval: " + interval);
		mPeakDetector.clear();
		if (interval > 0) {
			int rate = (int) (60000 / interval);
			// if (rate <= 30 && mChecking == true) {
			// onHeartBeatStop();
			// }
			// mChecking = true;
			return rate;
		} else {
			return 0;
		}

		// if (mIncrease == true) {
		// mRate = (mPeakCnt * 6) - 3;
		// } else {
		// mRate = (mPeakCnt * 6) + 3;
		// }
		// Log.v(TAG, "mRate = " + mRate);
		// // Log.v(TAG, "mMaxData = " + mMaxData);
		// // Log.v(TAG, "mMinData = " + mMinData);
		//
		// // In case of counting error at the beginning of peak value
		// // detecting .
		// if (mLast10SecRate - mRate >= 48) {
		// mRate = mLast10SecRate;
		// // Ignore violent rate delta during first 10 second .
		// mDeltaRate = 0;
		// }
		// Log.v(TAG, "mRateCheck = " + mRate);
		//
		// // Count the displayed HeartRate .
		// mHeartRate = (4 * mRate + 3 * mLast10SecRate + 2 * mLast20SecRate) /
		// 9;
		//
		// // Reset mPeakCnt for another new detection period .
		// mPeakCnt = 0;
		// return mHeartRate;

	}

	/** detect mDeltaRate using counted mHeartRate . **/
	public int getDeltaRate() {
		mDeltaRate = java.lang.Math.abs(mRate - mLast10SecRate);
		return mDeltaRate;
	}

	public void setListener(EcgListener listener) {
		mListener = listener;
	}

	/** Override "write()" to do write received data into buffer. **/
	@Override
	public synchronized void write(int length, byte[] data) {
		super.write(length, data);

		mPeakDetector.putData(data, 0, length);
		// Log.v("Heart", "length "+length+" put d: "+(data[0]&0xFF));

		// for (int i = 0; i < length; i++) {
		// if (i % 128 == 0){
		// }

		/*
		 * // Check Saturation int DeltaY = java.lang.Math.abs((data[i] & 0xFF)
		 * - BASE); if (DeltaY >= (256 - (BASE + SATURATION_REGION))) {
		 * mSaturation = true; } else if ((data[i] & 0xFF) == BASE) {
		 * mSaturation = false; } // Detect the Maximum or Minimum value of
		 * signal . if ((data[i] & 0xFF) >= mMaxData) { if (mSaturation ==
		 * false) { mMaxData = (int) data[i] & 0xFF; } else { mMaxData = 0;
		 * mMinData = 255; } } else if ((data[i] & 0xFF) <= mMinData) { if
		 * (mSaturation == false) { mMinData = (int) data[i] & 0xFF; } else {
		 * mMaxData = 0; mMinData = 255; } } // Measure the Slop of signal . if
		 * ((data[i] & 0xFF) >= (mLastData[i] & 0xFF)) { mIncrease = true; if
		 * (mIncrease != mLastIncrease) { mSlopZero = true; } else { mSlopZero =
		 * false; } } else if ((data[i] & 0xFF) <= (mLastData[i] & 0xFF)) {
		 * mIncrease = false; if (mIncrease != mLastIncrease) { mSlopZero =
		 * true; } else { mSlopZero = false; } }
		 * 
		 * long CurrentTime = System.currentTimeMillis(); int PeakPeriod = (int)
		 * (CurrentTime - mLastPeakTime); // Emergency detection . if
		 * (PeakPeriod >= 3000 && mLastPeakTime > 0) { onHeartBeatStop(); } int
		 * regionLowerBound = mMinData; int regionUppererBound = mMaxData;
		 * 
		 * mDetectionRegionSize = 3 * (regionUppererBound - regionLowerBound) /
		 * 4; // In case of counting continuous peak value in error , add
		 * another // condition (delta time between two peak) to count #peak .
		 * if ((data[i] & 0xFF) >= (mMaxData - mDetectionRegionSize) &&
		 * (mSlopZero == true) && (PeakPeriod >= 300)) { mPeakCnt += 1;
		 * Log.v(TAG, "HeartBeatCount"); mLastPeakTime = CurrentTime; } //
		 * Decrease detection region for more sensitive detection . if
		 * ((PeakPeriod >= 1200 || PeakPeriod <= 500) && mLastPeakTime > 0) {
		 * mMaxData = 160; mMinData = 110; } mLastData[i] = data[i];
		 * mLastIncrease = mIncrease;
		 */
		// }

	}

	/**
	 * Override "readSampledData()" . The sampleSize will be decided by
	 * "getCurrentData()" in "HealthDeviceBluetoothService.java" .
	 **/
	@Override
	public synchronized int[] readSampledData(int sampleSize) {
		return super.readSampledData(sampleSize);
	}

	/**
	 * This function should be called when we detect that heart beat is stopped.
	 * This function might be called by "getHeartRate()" , and this will execute
	 * function "onHeartBeatStop()" in "HealthDeviceBluetoothService.java"(using
	 * interface "EcgListener") to broadcast an emergency call .
	 */
	protected void onHeartBeatStop() {
		if (mListener != null) {
			mListener.onHeartBeatStop();
		}
	}

	/**
	 * Override the function "onBufferFull()" by adding function
	 * "onEcgBufferFull()" in "HealthDeviceBluetoothService.java" (using
	 * interface "EcgListener") to save the EcgData when DataBuffer is full .
	 **/
	@Override
	protected void onBufferFull(byte[] bufferData, int offset, int length) {
		// when Listener exist , do onEcgBufferFull(bufferData, offset,
		// length) in Listener .
		if (mListener != null) {
			mListener.onEcgBufferFull(bufferData, offset, length);
		}
	}

}
