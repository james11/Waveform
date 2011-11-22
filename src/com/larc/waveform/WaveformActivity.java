package com.larc.waveform;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.telephony.gsm.SmsManager;
import android.util.Log;
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
import com.larc.waveform.service.DataReceiveService;
import com.larc.waveform.ui.widget.WaveformView;
import com.larc.waveform.ui.widget.WaveformView.WaveformAdapter;

public class WaveformActivity extends Activity implements
		Button.OnClickListener {
	private static final int DEFAULT_SIZE = 5000; // Screen pixels number
	private static final int WAVEFORM_COUNT = 1;

	private static final int COLOR_TEXT_NORMAL = Color.GRAY;
	private static final int COLOR_TEXT_SELECTED = 0xFFFF9900;

	private static final int SIGNAL_EEG = DataReceiveService.CHANNEL_EEG;
	private static final int SIGNAL_DBS = DataReceiveService.CHANNEL_DBS;

	// Codes for activity result
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	private static final String TITLE_EEG = "4-Channel EEG Signals";
	private static final String TITLE_DBS = "4-Channel DBS Signals";

	private static final int[] LINE_COLOR_ARRAY = { Color.RED, Color.BLUE,
			Color.GREEN, Color.YELLOW };

	/** Called when the activity is first created. */

	private WaveformView[] mWaveformArray = new WaveformView[WAVEFORM_COUNT];
	private TextView[] mTextChannelNameArray = new TextView[WAVEFORM_COUNT];

	private LinearLayout mWaveformContainer;
	private LinearLayout mChannelNameContainer;

	private Button mButtonPause;
	private Button mButtonEEG;
	private Button mButtonDBS;
	private TextView mTextView;

	private int mSignal = SIGNAL_EEG;
	private boolean mIsPlaying = true;

	private int mRateUpdatePeriod = 10000;
	private int mRate = 110;
	private int mLast20SecRate = 0;
	private int mLast10SecRate = 120;
	private Handler mRateRefreshHandler;

	private String EMERGENCYC_CONNECTION_PHONE_NUMBER = "0918183964";
	private String SELF_PHONE_NUMBER;
	private String SMS_MESSEGE_CONTENT = "Emergency Event";
	private boolean mEmergency = false;
	private boolean mConnectionCheck = false;
	private boolean mSMSSended = false;

	private BluetoothAdapter mBluetoothAdapter = null;
	private DataReceiveService mDataReceiveService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_waveform);
		initView();
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		setSignal(SIGNAL_EEG);
		setPhoneNumber();
		startDrawing();
	}

	// Setup views and widgets at the first time the activity is created
	// This should only be called in onCreate();
	private void initView() {
		// initialize view variables
		mButtonPause = (Button) findViewById(R.id.buttonPause);
		mButtonEEG = (Button) findViewById(R.id.buttonEEG);
		mButtonDBS = (Button) findViewById(R.id.buttonDBS);
		mTextView = (TextView) findViewById(R.id.myTextView);
		mWaveformContainer = (LinearLayout) findViewById(R.id.waveformContainer);
		mChannelNameContainer = (LinearLayout) findViewById(R.id.channelNameBlock);

		mTextView.setText("4-Channel DBS Signals");
		mTextView.setTextColor(COLOR_TEXT_NORMAL);

		mButtonDBS.setTextColor(COLOR_TEXT_NORMAL);
		mButtonEEG.setTextColor(COLOR_TEXT_NORMAL);
		mButtonPause.setTextColor(COLOR_TEXT_NORMAL);

		// one button listener used by two buttons
		mButtonEEG.setOnClickListener(this);
		mButtonDBS.setOnClickListener(this);
		mButtonPause.setOnClickListener(this);

		mRateRefreshHandler = new Handler();
		mRateRefreshHandler.post(mRateRefreshRunnable);

		// create reusable layout parameter for adding view
		LinearLayout.LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f);

		// create & add waveform view dynamically
		for (int i = 0; i < WAVEFORM_COUNT; i++) {
			mTextChannelNameArray[i] = new TextView(this);
			mTextChannelNameArray[i].setText("ECG Channel");// + (i + 1));
			mTextChannelNameArray[i].setTextSize(20);
			mTextChannelNameArray[i].setTextColor(0xffff9900);
			mTextChannelNameArray[i].setGravity(android.view.Gravity.CENTER);

			mWaveformArray[i] = new WaveformView(this);

			mChannelNameContainer.addView(mTextChannelNameArray[i], params);
			mWaveformContainer.addView(mWaveformArray[i], params);
			mWaveformArray[i].setAdapter(mWaveformAdapter);
		}

	}

	Runnable mRateRefreshRunnable = new Runnable() {
		public void run() {
			energencyEventCheck();
			mLast20SecRate = mLast10SecRate;
			mLast10SecRate = mRate;
			mRate = getRate();
			int Rate = (2 * mRate + 3 * mLast10SecRate + 4 * mLast20SecRate) / 9;
			for (int i = 0; i < WAVEFORM_COUNT; i++) {
				mTextChannelNameArray[i].setText("ECG Channel"
						+ "\nHeart Rate = " + Rate + " /min");
			}
			mRateRefreshHandler.postDelayed(this, mRateUpdatePeriod);
		}
	};

	public int getRate() {
		return mDataReceiveService.getHeartRate();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setupConnect();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mDataReceiveService != null) {
			mDataReceiveService.stop();
		}
		stopDrawing();
	};

	/**
	 * This function would be called before screen orientation
	 */
	@Override
	public Object onRetainNonConfigurationInstance() {
		stopDrawing();
		return super.onRetainNonConfigurationInstance();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		switch (id) {
		case R.id.buttonDBS:
			mSignal = SIGNAL_DBS;
			break;
		case R.id.buttonEEG:
			mSignal = SIGNAL_EEG;
			break;
		case R.id.buttonPause:
			pauseAndStartDrawing();
			break;
		default:
			break;
		}

	}

	public void setCurrentSignal(int signal) {
		mSignal = signal;
		refreshSignalButtonAndText();
	}

	private void refreshSignalButtonAndText() {
		switch (mSignal) {
		case SIGNAL_DBS:
			mButtonDBS.setTextColor(COLOR_TEXT_SELECTED);
			mButtonEEG.setTextColor(COLOR_TEXT_NORMAL);
			mButtonPause.setTextColor(COLOR_TEXT_NORMAL);
			mTextView.setText(TITLE_DBS);
			break;
		case SIGNAL_EEG:
			mButtonEEG.setTextColor(COLOR_TEXT_SELECTED);
			mButtonDBS.setTextColor(COLOR_TEXT_NORMAL);
			mButtonPause.setTextColor(COLOR_TEXT_NORMAL);
			mTextView.setText(TITLE_EEG);
			break;
		}
	}

	public void pauseAndStartDrawing() {
		if (mIsPlaying) {
			stopDrawing();
		} else {
			startDrawing();
		}
	}

	public void startDrawing() {
		mIsPlaying = true;
		for (WaveformView wave : mWaveformArray) {
			wave.start();
		}
		onStateChanged();
	}

	public void stopDrawing() {
		mIsPlaying = false;
		for (WaveformView wave : mWaveformArray) {
			wave.stop();
		}
		onStateChanged();
	}

	protected void onStateChanged() {
		if (mIsPlaying) {
			mButtonPause.setTextColor(COLOR_TEXT_NORMAL);
		} else {
			mButtonPause.setTextColor(COLOR_TEXT_SELECTED);
		}
	}

	public void setSignal(int signal) {

		mSignal = signal;
		resetWaveformViewData();
		refreshSignalButtonAndText();
	}

	public void resetWaveformViewData() {
		for (int i = 0; i < mWaveformArray.length; i++) {
			mWaveformArray[i].removeAllDataSet();
			mWaveformArray[i].createNewDataSet(DEFAULT_SIZE);
			mWaveformArray[i].setLineColor(0, LINE_COLOR_ARRAY[i]);
		}
	}

	private WaveformAdapter mWaveformAdapter = new WaveformAdapter() {
		@Override
		public int[] getCurrentData(int set, int preferedSize) {
			return mDataReceiveService.getCurrentData(mSignal, preferedSize);
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * Define the functionality of the buttons on ActionBar or Menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.secure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			// Start Activity "serverIntent"(DeviceListActivity) ,and return to
			// onActivityResult with requestCode "REQUEST_CONNECT_DEVICE_SECURE"
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			return true;
		case R.id.insecure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			// Start Activity "serverIntent"(DeviceListActivity) ,and return to
			// onActivityResult with requestCode
			// "REQUEST_CONNECT_DEVICE_INSECURE" .
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent,
					REQUEST_CONNECT_DEVICE_INSECURE);
			return true;
		case R.id.discoverable:
			ensureDiscoverable();
			return true;
		case R.id.quitAPP:
			finish();
			return true;
		}
		return false;
	}

	private BluetoothService.BluetoothEventHandler mBluetoothHandler = new BluetoothService.BluetoothEventHandler() {

		@Override
		public void onStateChange(int state) {
			String text = "";
			switch (state) {
			case BluetoothService.STATE_CONNECTED:
				text = "connected";
				mConnectionCheck = true;
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

	};

	private void ensureDiscoverable() {
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			// make the Bluetooth device discoverable for 300 second
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
				mButtonPause.setTextColor(COLOR_TEXT_NORMAL);
				mIsPlaying = true;
			}
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				connectDevice(address, false);
				mButtonPause.setTextColor(COLOR_TEXT_NORMAL);
				mIsPlaying = true;

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
		// Attempt to connect to the device
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);
		setupConnect();
		mDataReceiveService.connect(device, secure);
	}

	private void setupConnect() {
		// Initialize the DataReceiveService to perform bluetooth connections if
		// no DataReceive source .
		if (mDataReceiveService == null) {
			mDataReceiveService = DataReceiveService.getInstance(this);
			mDataReceiveService.setHandler(mBluetoothHandler);
			mDataReceiveService.start();
		}

	}

	private void energencyEventCheck() {
		mEmergency = mDataReceiveService.emergencyEventCheck();
		if (mEmergency == true && mConnectionCheck == true
				&& mSMSSended == false) {
			emergencyCall();
		} else {

		}
	}

	private void setPhoneNumber() {
		TelephonyManager phoneManager = (TelephonyManager) getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		SELF_PHONE_NUMBER = phoneManager.getLine1Number();
	}

	@SuppressWarnings("deprecation")
	private void emergencyCall() {
		SmsManager smsManager = SmsManager.getDefault();
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				WaveformActivity.this, 0, new Intent(), 0);
		smsManager.sendTextMessage(EMERGENCYC_CONNECTION_PHONE_NUMBER, null,
				SMS_MESSEGE_CONTENT + " from " + SELF_PHONE_NUMBER,
				pendingIntent, null);
		Log.v("Waveform", "Emergency  ");
		mSMSSended = true;

	}
}
