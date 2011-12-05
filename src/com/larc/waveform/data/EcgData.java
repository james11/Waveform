package com.larc.waveform.data;



public class EcgData extends BufferedByteData{

	public interface EcgListener{
		public void onHeartBeatStop();
		public void onEcgBufferFull(byte[] bufferData, int offset, int length);
	}
	
	public int mHeartRate;
	public EcgListener mListener;
	
	public int getHeartRate() {
		return mHeartRate;
	}
	
	public void setListener(EcgListener listener){
		mListener = listener;
	}

	@Override
	public synchronized void write(int length, byte[] data) {
		super.write(length, data);
		// TODO: some operation to analyze data
	}

	@Override
	public synchronized int[] readSampledData(int sampleSize) {
		return super.readSampledData(sampleSize);
	}
	
	/**
	 * This function should be called when we detect that heart beat is stopped.
	 */
	protected void onHeartBeatStop(){
		if(mListener != null){
			mListener.onHeartBeatStop();
		}
	}

	@Override
	protected void onBufferFull(byte[] bufferData, int offset, int length) {
		if (mListener != null){
			mListener.onEcgBufferFull(bufferData, offset, length);
		}
	}
	
	
}
