package com.larc.waveform.data;

public class BufferedByteData {
	BufferedByteData() {
		this(BUFFER_SIZE);
	}

	BufferedByteData(int bufferSize) {
		mDataBuffer = new byte[bufferSize];
	}

	// private static final String TAG = "BufferedByteData";
	// private static final boolean VERBOSE = true;

	private final byte[] mDataBuffer;
	protected static int BUFFER_SIZE = 1024 * 400;
	private int mPointer = 0;
	private int mGetPointer = 0;

	/**
	 * Write the received data (which are from Bluetooth Antenna) into
	 * mDataBuffer .
	 **/
	public synchronized void write(int length, byte[] data) {

		int currentPosition = 0;
		currentPosition = mPointer;

		// Check if Buffer us full .
		if (currentPosition + length <= BUFFER_SIZE) {

		} else {
			onBufferFull(mDataBuffer, 0, BUFFER_SIZE);
			// reset Buffer's pointer .
			mPointer = 0;
			mGetPointer = 0;
			currentPosition = 0;
		}

		// Put received Data into mDataBuffer .
		for (int i = 0; i < length; i++) {
			mDataBuffer[currentPosition + i] = data[i];
		}

		mPointer += length;
	}

	/** Sample the data in Buffer and return the sampling . **/
	public synchronized int[] readSampledData(int sampleSize) {

		int[] data = null;
		int start = 0;
		int end = 0;
		int avaliableSize = 0;
		start = mGetPointer;
		end = mPointer;
		end = (end + start) / 2;
		avaliableSize = end - start;

		// return null to in case of error .
		if (avaliableSize <= 0 || start >= BUFFER_SIZE) {
			mGetPointer = 0;
			return null;
		} else {
			data = new int[avaliableSize / sampleSize];
			int j = 0;
			// Be care of the variables in this for() . (count them carefully)
			for (int i = 0; i <= avaliableSize - sampleSize; i += sampleSize) {
				// Convert Data form byte to integer .
				data[j] = (int) (mDataBuffer[start + i] & 0xFF);
				j++;
			}
			mGetPointer = end;
		}

		return data;
	}

	/** Do different function under different case when Buffer is full . **/
	protected void onBufferFull(byte[] bufferData, final int offset,
			final int length) {

	}
}
