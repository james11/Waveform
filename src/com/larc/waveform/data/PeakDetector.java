package com.larc.waveform.data;

import java.util.ArrayList;

import com.larc.waveform.service.EmergencyCallService;

public class PeakDetector {
	// private static final String TAG = "Heart";

	/** Modify here between 115200 & 19200 boud rate **/
	private static final int DETECTOR_SIZE = 6;
	private static final int PEAK_POS = DETECTOR_SIZE / 2;

	public static final float BASE_UPDATE_FACTOR = 0.03f;
	public static final float AMP_UPDATE_FACTOR = 0.2f;
	private static final int MIN_DISTANCE = 3;

	private ArrayList<Long> mPeakTimeList = new ArrayList<Long>();

	private int mIndex = 0;
	private final int[] mBuffer = new int[DETECTOR_SIZE];
	private final long[] mTime = new long[DETECTOR_SIZE];

	private float mBase = 0;
	private float mAmplitudeRange = 60;
	private int mDistance = 0;

	private EmergencyCallService mEmergencyCallService;

	private void putData(int value) {
		mBuffer[mIndex] = value;
		mTime[mIndex] = System.currentTimeMillis();

		if (mPeakTimeList.size() > 2) {
			int size = mPeakTimeList.size();
			if (mTime[mIndex] - mPeakTimeList.get(size - 1) >= 3000) {
				mEmergencyCallService = EmergencyCallService.getInstance();
				mEmergencyCallService.emergencyEventCheck();
			}
		}

		// Index +1 for next putData() .
		nextValue();
		if (mDistance <= 0) {
			if (checkPeak()) {
				mDistance = MIN_DISTANCE;
			}
		} else {
			mDistance--;
		}
	}

	public boolean checkPeak() {
		boolean hasPeak = true;
		int index = mIndex; // Save index
		int slope;
		int v1, v2;
		int max, min;
		v1 = nextValue();
		max = v1;
		min = v1;

		// Check if data form mBuffer[mIndex+2] to mBuffer[mIndex+32] present an
		// up arrow shape
		for (int i = 1; i < DETECTOR_SIZE; i++) {
			v2 = nextValue();
			slope = v2 - v1;
			v1 = v2;
			if (v1 > max) {
				max = v1;
			}
			if (v1 < min) {
				min = v1;
			}

			if (i <= PEAK_POS) {
				if (slope < -0) {
					hasPeak = false;
				}
			} else {
				if (slope > 0) {
					hasPeak = false;
				}
			}
		}

		mIndex = index; // Revert index

		if (hasPeak && (max - mBase) > mAmplitudeRange * 0.7) {
			mPeakTimeList.add(mTime[mIndex]);
			updateRange(max - min);
			// Log.v(TAG, "beat: amplitude=" + (max - mBase) + " base=" + mBase
			// + " diff=" + (max - min));
			return true;
		} else {
			updateBase(min);
			return false;
		}
	}

	private void updateBase(float newValue) {
		mBase = newValue * BASE_UPDATE_FACTOR + (1 - BASE_UPDATE_FACTOR)
				* mBase;
	}

	private void updateRange(float newValue) {
		mAmplitudeRange = newValue * AMP_UPDATE_FACTOR
				+ (1 - AMP_UPDATE_FACTOR) * mAmplitudeRange;
	}

	private int nextValue() {
		mIndex++;
		if (mIndex >= DETECTOR_SIZE) {
			mIndex = 0;
		}
		return mBuffer[mIndex];
	}

	public void clear() {
		mPeakTimeList.clear();
	}

	public long getAverageInterval() {
		if (mPeakTimeList.size() <= 1) {
			return 0;
		} else {
			int size = mPeakTimeList.size();
			long sum = 0;
			for (int i = 1; i < size; i++) {
				sum += (mPeakTimeList.get(i) - mPeakTimeList.get(i - 1));
			}
			return sum / (size - 1);
		}
	}

	public void putData(byte[] data, int offset, int length) {
		for (int i = offset; i < length - 9; i += 10) {
			putData(data[i] & 0xFF);
		}
	}
}
