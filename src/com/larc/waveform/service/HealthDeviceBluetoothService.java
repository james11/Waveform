package com.larc.waveform.service;

import android.content.Context;
import android.util.Log;

import com.larc.bluetoothconnect.BluetoothService;
import com.larc.waveform.data.DataFileManager;
import com.larc.waveform.data.EcgData;

public class HealthDeviceBluetoothService extends BluetoothService implements EcgData.EcgListener{

	public static final int CHANNEL_EEG = 0;
	public static final int CHANNEL_DBS = 1;
	private static final int ECG_SAMPLE_SIZE = 3;

	private static HealthDeviceBluetoothService mInstance;

	private DataFileManager mDataFileManager;
	private final EcgData mEcgData;
	
	private HealthDeviceBluetoothService(Context context) {
		super(context);
		mInstance = this;
		mEcgData = new EcgData();
		mEcgData.setListener(this);
		mDataFileManager = DataFileManager.getInstance();
		mDataFileManager.setId("");
		mDataFileManager.setName("");
		mDataFileManager.setPhoneNumber("");
	}

	public static HealthDeviceBluetoothService getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new HealthDeviceBluetoothService(context);
		}
		return mInstance;
	}

	@Override
	protected void onDataRead(int length, byte[] data) {
		super.onDataRead(length, data);
		mEcgData.write(length, data);
	}

	public int getHeartRate() {
		return mEcgData.getHeartRate();
	}

	public int[] getCurrentData(int channel, int preferedSize) {
		Log.v(TAG, "getCurrentData " + preferedSize);
		switch(channel){
		case CHANNEL_EEG:
			return mEcgData.readSampledData(ECG_SAMPLE_SIZE);
		case CHANNEL_DBS:
		default:
			return null;
		}
//		 int[] getData = new int[preferedSize];
//		 getData = mReceivedData.getLatestData(preferedSize, 1);
//		 int[] returnData = new int[preferedSize];
//		 int size = preferedSize;
//		 if (getData != null) {
//		 for (int i = 0; i < (size); i++) {
//		 if (i < 9)
//		 returnData[i] = getData[i];
//		 else {
//		 returnData[i] = (int) ((getData[i - 9] + getData[i - 8]
//		 + getData[i - 7] + getData[i - 6] + getData[i - 5]
//		 + getData[i - 4] + getData[i - 3] + getData[i - 2]
//		 + getData[i - 1] + getData[i]) / 10);
//		 }
//		 }
//		 }
//		 return returnData;
	}

	@Override
	public void onHeartBeatStop() {
		//TODO: send sms message?
	}

	@Override
	public void onEcgBufferFull(byte[] bufferData, int offset, int length) {
		//save data
		mDataFileManager.saveData(bufferData, offset, length);
	}
	
}
