package com.larc.waveform.service;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.larc.bluetoothconnect.BluetoothService;
import com.larc.waveform.data.DataFileManager;
import com.larc.waveform.data.EcgData;

public class HealthDeviceBluetoothService extends BluetoothService implements
		EcgData.EcgListener {

	// private static final String TAG = "HealthDeviceBluetoothService";

	public static final int CHANNEL_EEG = 0;
	public static final int CHANNEL_DBS = 1;
	
	/** Modify here between 115200 & 19200 boud rate **/
	private static final int ECG_SAMPLE_SIZE = 3;
	/**************************************************/

	private static HealthDeviceBluetoothService mInstance;

	private EmergencyCallService mEmergencyCallService;
	private DataFileManager mDataFileManager;
	private final GPSLocationService mGPSLocation;
	private final EcgData mEcgData;

	public static String uniqueID = null;
	private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

	private HealthDeviceBluetoothService(Context context) {
		super(context);
		mInstance = this;
		mEcgData = new EcgData();
		mEcgData.setListener(this);

		mGPSLocation = GPSLocationService.getInstance();
		mGPSLocation.setHandler();
		mGPSLocation.mLocationServiceHandler
				.post(mGPSLocation.mLocationServiceRunnable);

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

	// UniqueID of android device get() .
	public synchronized static String id(Context context) {
		if (uniqueID == null) {
			SharedPreferences sp = context.getSharedPreferences(PREF_UNIQUE_ID,
					Context.MODE_PRIVATE);
			uniqueID = sp.getString(PREF_UNIQUE_ID, null);
			if (uniqueID == null) {
				uniqueID = UUID.randomUUID().toString();
				Editor editor = sp.edit();
				editor.putString(PREF_UNIQUE_ID, uniqueID);
				editor.commit();
			}
		}
		return uniqueID;
	}

	/**
	 * Override the function "onDataRead()" in "BluetoothService.java" to
	 * receive data from Bluetooth Antenna, and "write()" into "EcgData.jave" .
	 **/
	@Override
	protected void onDataRead(int length, byte[] data) {
		super.onDataRead(length, data);
		mEcgData.write(length, data);
	}

	public String getID() {
		return uniqueID;
	}

	public double getLongitude() {
		return mGPSLocation.getLongitude();
	}

	public double getLatitude() {
		return mGPSLocation.getLatitude();
	}

	// return to WaveformActivity .
	public int getHeartRate() {
		return mEcgData.getHeartRate();
	}

	// return to WaveformActivity .
	public int getDeltaRate() {
		return mEcgData.getDeltaRate();
	}

	/**
	 * return data to "WaveformActivity.java" to Override WaveformAdapter of
	 * WaveformView.java .
	 **/
	public int[] getCurrentData(int channel, int preferedSize) {
		// Log.v(TAG, "getCurrentData " + preferedSize);
		switch (channel) {
		case CHANNEL_EEG:
			return mEcgData.readSampledData(ECG_SAMPLE_SIZE);
		case CHANNEL_DBS:
		default:
			return null;
		}

		/** Averaging Filter code **/
		// int[] getData = new int[preferedSize];
		// getData = mReceivedData.getLatestData(preferedSize, 1);
		// int[] returnData = new int[preferedSize];
		// int size = preferedSize;
		// if (getData != null) {
		// for (int i = 0; i < (size); i++) {
		// if (i < 9)
		// returnData[i] = getData[i];
		// else {
		// returnData[i] = (int) ((getData[i - 9] + getData[i - 8]
		// + getData[i - 7] + getData[i - 6] + getData[i - 5]
		// + getData[i - 4] + getData[i - 3] + getData[i - 2]
		// + getData[i - 1] + getData[i]) / 10);
		// }
		// }
		// }
		// return returnData;
	}

	/** Function used by listener in "EcgData.java" . **/
	@Override
	public void onHeartBeatStop() {
		// send emergency SMS .
		mEmergencyCallService.emergencyEventCheck();
	}

	/** Function used by listener in "EcgData.java" . **/
	@Override
	public void onEcgBufferFull(byte[] bufferData, int offset, int length) {
		// save data .
		mDataFileManager.saveByteData(bufferData, offset, length);
	}

}
