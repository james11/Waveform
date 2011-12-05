package com.larc.waveform.data;


public class BufferedByteData {

//	private static final String TAG = "BufferedByteData";
//	private static final boolean VERBOSE = true;

	private static final int BUFFER_SIZE = 1024 * 400;

	private byte[] mDataBuffer = new byte[BUFFER_SIZE];

	private int mPointer = 0;
	private int mGetPointer = 0;

	public boolean mEmergencyEvent = false;

	public synchronized void write(int length, byte[] data) {

		int currentPosition = 0;
		currentPosition = mPointer;

		if (currentPosition + length <= BUFFER_SIZE) {
			// if mDataBuffer is full , save data to files and reset
			// mDataBuffer to null .
		} else {
			onBufferFull(mDataBuffer, 0, BUFFER_SIZE);
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

	public synchronized int[] readSampledData(int sampleSize) {

		int[] data = null;
		int start = 0;
		int end = 0;
		int avaliableSize = 0;
		start = mGetPointer;
		end = mPointer;
		end = (end + start) / 2;
		avaliableSize = end - start;

		if (avaliableSize <= 0 || start >= BUFFER_SIZE) {
			mGetPointer = 0;
			return null;
		} else {
			data = new int[avaliableSize / sampleSize];
			int j = 0;
			for (int i = 0; i <= avaliableSize - sampleSize; i += sampleSize) {
				// Convert Data form byte to integer .
				data[j] = (int) (mDataBuffer[start + i] & 0xFF);
				j++;
			}
			mGetPointer = end;
		}

		return data;
	}
	
	protected void onBufferFull(byte[] bufferData, final int offset, final int length){
		
	}
}
