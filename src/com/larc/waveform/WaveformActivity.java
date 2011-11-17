package com.larc.waveform;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.larc.bluetoothconnect.BluetoothService;
import com.larc.bluetoothconnect.DeviceListActivity;
import com.larc.waveform.data.ReceivedData;
import com.larc.waveform.service.DataReceiveService;
import com.larc.waveform.ui.widget.WaveformView;
import com.larc.waveform.ui.widget.WaveformView.WaveformAdapter;

public class WaveformActivity extends Activity {
	private static final int DEFAULT_SIZE = 5000 * 2; // Screen pixels number
	private static final int WAVEFORM_COUNT = 4;

	private static final int COLOR_TEXT_NORMAL = Color.GRAY;
	private static final int COLOR_TEXT_SELECTED = 0xFFFF9900;

	private static final int SIGNAL_EEG = 0;
	private static final int SIGNAL_DBS = 1;

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	/** Called when the activity is first created. */

	private WaveformView[] mWaveformArray = new WaveformView[WAVEFORM_COUNT];
	private TextView[] mChannelNameArray = new TextView[WAVEFORM_COUNT];

	private LinearLayout mWaveformContainer;
	private LinearLayout mChannelNameContainer;

	private Button mButtonPause;
	private Button mButtonEEG;
	private Button mButtonDBS;
	private TextView mTextView;

	// private ReceivedData[] mDbsData = new ReceivedData[WAVEFORM_COUNT];
	// private ReceivedData[] mEegData = new ReceivedData[WAVEFORM_COUNT];

	private int mSignal = SIGNAL_EEG;
	private int mSignalCheck = SIGNAL_DBS;
	private boolean mPause = true;

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	private DataReceiveService mDataReceiveService;
	private ReceivedData mReceivedData;
	private BluetoothService mBluetoothService;

	private int mRate = 0;
	private int mUpdatePeriod = 500;
	private Handler mRateRefreshHandler;
	

	// public BluetoothService mService;
	// private Handler mServiceHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_waveform);
		// for (int i = 0; i < WAVEFORM_COUNT; i++) {
		// mDbsData[i] = new ReceivedData(); // Receive data from
		// // ReceivedData.java .
		// mEegData[i] = new ReceivedData();
		// }
//		mRateRefreshHandler = new Handler(); 
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		mButtonPause = (Button) findViewById(R.id.buttonPause);
		mButtonEEG = (Button) findViewById(R.id.buttonEEG);
		mButtonDBS = (Button) findViewById(R.id.buttonDBS);
		mTextView = (TextView) findViewById(R.id.myTextView);

		mWaveformContainer = (LinearLayout) findViewById(R.id.waveformContainer);
		mChannelNameContainer = (LinearLayout) findViewById(R.id.channelNameBlock);

		// Set size of Views in LineearLayout .
		LinearLayout.LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f);
		// Orientation of views in Layout
		mWaveformContainer.setOrientation(LinearLayout.VERTICAL);
		mChannelNameContainer.setOrientation(LinearLayout.VERTICAL);

		for (int i = 0; i < WAVEFORM_COUNT; i++) {
			// prepare views for adding
			mChannelNameArray[i] = new TextView(this);
			mChannelNameArray[i].setText("CH" + (i + 1));
			mChannelNameArray[i].setTextSize(20);
			mChannelNameArray[i].setTextColor(0xffff9900);
			mChannelNameArray[i].setGravity(android.view.Gravity.CENTER);
			mWaveformArray[i] = new WaveformView(this);
			// add views
			// mChannelNameContainer.addView(mChannelNameArray[i], params);
			// mWaveformContainer.addView(mWaveformArray[i],params);
		}

		mChannelNameArray[0].setText("ECG Channel" + "\nHeart Rate = "+ mRate + "/min"); // ****
		mChannelNameContainer.addView(mChannelNameArray[0], params); // ****
		mWaveformContainer.addView(mWaveformArray[0], params); // ****
		

		mWaveformArray[0].setAdapter(mWaveformAdapter); // Link to WaveformView
														// . Set
														// mWaveformAdapter to
														// mAdapter in
														// WaveformView.java
//		mRateRefreshHandler.post(mRateRefreshRunnable);
		// mWaveformArray[1].setAdapter(mWaveformAdapter);
		// mWaveformArray[2].setAdapter(mWaveformAdapter);
		// mWaveformArray[3].setAdapter(mWaveformAdapter);

		mWaveformArray[1].setLineColor(0, Color.GREEN);
		mWaveformArray[2].setLineColor(0, Color.BLUE);
		mWaveformArray[3].setLineColor(0, R.color.weak_yellow);

		mTextView.setText("4-Channel DBS Signals");
		mTextView.setTextColor(COLOR_TEXT_NORMAL);
		mButtonDBS.setTextColor(COLOR_TEXT_SELECTED);
		mButtonEEG.setTextColor(COLOR_TEXT_NORMAL);

		mButtonPause.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View view) {
				pauseAndStart();
			}
		});

		// one button listener used by two buttons
		mButtonEEG.setOnClickListener(mButtonClickListener); // mButtonClickListener
																// will be
																// declared
																// below .
		mButtonDBS.setOnClickListener(mButtonClickListener);

		// mHandler = new Handler();
		// mHandler.post(mPushDataRunnable);

		refreshSignal();
		pauseAndStart(); // Start() at initial as we OnCreat .
	}

	@Override
	protected void onResume() { // ??
		super.onResume();
		setupConnect();
	}
	
//	Runnable mRateRefreshRunnable = new Runnable() {
//		public void run() {
//			mRate = getRate();
//			mChannelNameArray[0].setText("ECG Channel" + "\nHeart Rate = "+ mRate + "/min"); // ****
//			mRateRefreshHandler.postDelayed(this, mUpdatePeriod);
//			}
//	};

//	public int getRate() {
//		return mReceivedData.getRate();
//	}

	// Declare Button.OnClickListener "mButtonClickListener" .
	private Button.OnClickListener mButtonClickListener = new Button.OnClickListener() {

		public void onClick(View v) {
			int id = v.getId();

			switch (id) {
			case R.id.buttonDBS:
				mSignal = SIGNAL_DBS;
				break;
			case R.id.buttonEEG:
			default:
				mSignal = SIGNAL_EEG;
				break;
			}
			if (mSignal != mSignalCheck) {
				refreshSignal();
			}

		}
	};

	private void refreshSignal() {
		mSignalCheck = mSignal;
		switch (mSignal) {
		case SIGNAL_DBS:
			showDBS();
			mButtonDBS.setTextColor(COLOR_TEXT_SELECTED);
			mButtonEEG.setTextColor(COLOR_TEXT_NORMAL);
			mTextView.setText("4-Channel DBS Signals");
			break;
		case SIGNAL_EEG:
			showEEG();
			mButtonEEG.setTextColor(COLOR_TEXT_SELECTED);
			mButtonDBS.setTextColor(COLOR_TEXT_NORMAL);
			mTextView.setText("4-Channel EEG Signals");
			break;
		}

	}

	public void pauseAndStart() {
		if (!mPause) {
			for (WaveformView wave : mWaveformArray) {
				mButtonPause.setTextColor(COLOR_TEXT_SELECTED);
				wave.stop();
			}
			mPause = true;
		} else {
			mPause = false;
			for (WaveformView wave : mWaveformArray) {
				mButtonPause.setTextColor(COLOR_TEXT_NORMAL);
				// wave.start();
			}
			mWaveformArray[0].start();
			// mWaveformArray[1].start();
			// mWaveformArray[2].start();
			// mWaveformArray[3].start();
		}
	}

	public void showDBS() {
		for (WaveformView wave : mWaveformArray) { // All WaveformView (which
													// are called wave here)
													// which are in
													// mWaveformArray .
			wave.removeAllDataSet();
			wave.createNewDataSet(DEFAULT_SIZE);
			// for (int i = 0; i < WAVEFORM_COUNT; i++) {
			// int[] dataArray = mDbsData[0].getData(DEFAULT_SIZE + 50);
			// wave.setData(0, dataArray);
			// }
		}
		mWaveformArray[1].setLineColor(0, Color.GREEN);

		mWaveformArray[2].setLineColor(0, Color.BLUE);

		mWaveformArray[3].setLineColor(0, R.color.weak_yellow);
	}

	public void showEEG() {
		for (WaveformView wave : mWaveformArray) {
			wave.removeAllDataSet();
			wave.createNewDataSet(DEFAULT_SIZE);
			// for (int i = 0; i < WAVEFORM_COUNT; i++) {
			// int[] dataArray = mEegData[0].getData(DEFAULT_SIZE);
			// wave.setData(0, dataArray);
			// }
		}
		mWaveformArray[1].setLineColor(0, Color.GREEN);

		mWaveformArray[2].setLineColor(0, Color.BLUE);

		mWaveformArray[3].setLineColor(0, R.color.weak_yellow);
	}

	// Set mWaveformAdapter , which is a member of WaveformAdapter in
	// WaveformView.java .
	private WaveformAdapter mWaveformAdapter = new WaveformAdapter() {
		// And @Override it here : from return "null" to return
		// "CurrentData from BluetoothService" .
		@Override
		public int[] getCurrentData(int set) {
			return mDataReceiveService.getCurrentData();
		}
	};

	// private Runnable mPushDataRunnable = new Runnable() {
	// public void run() {
	// if (mBluetoothService != null) {
	// int[] data = mBluetoothService.getCurrentData();
	// // int size = data.length;
	// // int[] sampleData = new int[3];
	// // if(size >=3){
	// // sampleData[0] = data[0];
	// // sampleData[1] = data[size / 2];
	// // sampleData[2] = data[size -1];
	// // }
	// //// int value = mBluetoothService.getCurrentValue();
	// for (WaveformView wave : mWaveformArray) {
	// // wave.setCurrentData(0, value);
	// wave.setData(0, data);
	// }
	// }
	// // mHandler.postDelayed(this, 10); //Set Delay for getting Data
	// }
	//
	// };

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.secure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			// Start Activity "serverIntent"(DeviceListActivity) ,and return to
			// onActivityResult with requestCode "REQUEST_CONNECT_DEVICE_SECURE"
			// .
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			return true;
		case R.id.insecure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			// Start Activity "serverIntent"(DeviceListActivity) ,and return to
			// onActivityResult with requestCode
			// "REQUEST_CONNECT_DEVICE_INSECURE" .
			startActivityForResult(serverIntent,
					REQUEST_CONNECT_DEVICE_INSECURE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		case R.id.quitAPP:
			// Ensure this device is discoverable by others
			quit();
			return true;
		}
		return false;
	}

	private void quit() {
		finish();
		if (mDataReceiveService != null) {
			mDataReceiveService.stop();
		}
		for (int i = 0; i < mWaveformArray.length; i++) {
			mWaveformArray[i].stop();
		}
	};

	@Override
	public Object onRetainNonConfigurationInstance() {
		for (int i = 0; i < mWaveformArray.length; i++) {
			mWaveformArray[i].stop();
		}
		return super.onRetainNonConfigurationInstance();
	}

	private BluetoothService.BluetoothEventHandler mBluetoothHandler = new BluetoothService.BluetoothEventHandler() {

		@Override
		public void onStateChange(int state) {
			String text = "";
			switch (state) {
			case BluetoothService.STATE_CONNECTED:
				text = "connected";
				break;
			case BluetoothService.STATE_CONNECTING:
				text = "connecting";
				break;
			case BluetoothService.STATE_LISTENING:
				text = "listening";
				break;
			default:
			case BluetoothService.STATE_NONE:
				text = "none";
				break;
			}
			Toast.makeText(WaveformActivity.this, text, Toast.LENGTH_SHORT)
					.show();
		}

		// @Override
		// public void onMessageWrite(byte[] data) {
		// }
		//
		// @Override
		// public void onMessageRead(byte[] data) {
		// }

	};

	private void ensureDiscoverable() {
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			// add extended data to the intent .(make bluetooth discoverable for
			// 300 second)
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) { // RESULT_OK is from
													// "mDeviceClickListener" in
													// DeviceListActivity.java
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				connectDevice(address, true);
			}
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				connectDevice(address, false);
			}
			break;
		case REQUEST_ENABLE_BT:
			/** Not declare here **/
			// When we request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupConnect();
			} else {
				// User did not enable Bluetooth or an error occurred
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void connectDevice(String macAddress, boolean secure) {
		// Get the BluetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);
		// Attempt to connect to the device
		setupConnect();
		mDataReceiveService.connect(device, secure);
	}

	// in case there is no DataReceiveService for our Bluetooth connection .
	private void setupConnect() {
		// Initialize the DataReceiveService to perform bluetooth connections if
		// no DataReceive source .
		if (mDataReceiveService == null) {
			mDataReceiveService = DataReceiveService.getInstance(this);
			mDataReceiveService.setHandler(mBluetoothHandler);
			/** ??? **/
			mDataReceiveService.start();
		}

	}

}
