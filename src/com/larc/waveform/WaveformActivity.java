package com.larc.waveform;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.larc.bluetoothconnect.BluetoothService;
import com.larc.bluetoothconnect.DeviceListActivity;
import com.larc.waveform.service.HealthDeviceBluetoothService;
import com.larc.waveform.ui.widget.WaveformView;
import com.larc.waveform.ui.widget.WaveformView.WaveformAdapter;

public class WaveformActivity extends Activity implements
		Button.OnClickListener, OnCheckedChangeListener {
	private static final int DEFAULT_SIZE = 1300; // Screen pixels number
	private static final int PLOTTING_OFFSET = 200; // Offset plotting line .
	private static final int WAVEFORM_COUNT = 1;
	private static final int HEART_RATE_UPDATE_PERIOD = 1000 * 10;
	private static final int TEXT_SIZE = 10;

	private static final int COLOR_TEXT_NORMAL = Color.GRAY;
	private static final int COLOR_TEXT_SELECTED = 0xFFFF9900;
	private static final int SAFE_RATE_COLOR = 0xFF00AA00;
	private static final int RATE_90_COLOE = 0xFFFF6600;
	private static final int RATE_110_COLOR = 0xFFCC0000;
	private static final int RATE_140_COLOR = 0xFF660066;

	private static final int SIGNAL_EEG = HealthDeviceBluetoothService.CHANNEL_EEG;
	private static final int SIGNAL_DBS = HealthDeviceBluetoothService.CHANNEL_DBS;

	// Codes for activity result
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	/*** These variable are for multiBio-channels ***/
	// private static final String TITLE_EEG = "4-Channel EEG Signals";
	// private static final String TITLE_DBS = "4-Channel DBS Signals";

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
	private TextView mIDTextView;
	private TextView mlongitudeTextView;
	private TextView mlatitudeTextView;
	private RadioGroup mRadioGroup;

	private int mSignal = SIGNAL_EEG;
	private boolean mIsPlaying = true;

	private Handler mRateRefreshHandler;

	private BluetoothAdapter mBluetoothAdapter = null;
	private HealthDeviceBluetoothService mHealthDeviceBluetoothService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_waveform);
		initView();
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		setSignal(SIGNAL_EEG);
		startDrawing();
	}

	// Setup views and widgets at the first time the activity is created
	// This should only be called in onCreate();
	private void initView() {
		// initialize view variables
		mButtonPause = (Button) findViewById(R.id.buttonPause);
		mButtonEEG = (Button) findViewById(R.id.buttonEEG);
		mButtonDBS = (Button) findViewById(R.id.buttonDBS);
		mIDTextView = (TextView) findViewById(R.id.myidTextView);
		mlongitudeTextView = (TextView) findViewById(R.id.mylongitudeTextView);
		mlatitudeTextView = (TextView) findViewById(R.id.mylatitudeTextView);
		mWaveformContainer = (LinearLayout) findViewById(R.id.waveformContainer);
		mChannelNameContainer = (LinearLayout) findViewById(R.id.channelNameBlock);
		mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);

		// uploadbar = (ProgressBar) this.findViewById(R.id.uploadBar);
		// resulView = (TextView) this.findViewById(R.id.result);

		mIDTextView.setText(R.string.Uniqui_ID);
		mIDTextView.setTextColor(COLOR_TEXT_NORMAL);

		mlongitudeTextView.setText(R.string.longitude);
		mlongitudeTextView.setTextColor(COLOR_TEXT_NORMAL);

		mlatitudeTextView.setText(R.string.latitude);
		mlatitudeTextView.setTextColor(COLOR_TEXT_NORMAL);

		mButtonDBS.setTextColor(COLOR_TEXT_NORMAL);
		mButtonEEG.setTextColor(COLOR_TEXT_NORMAL);
		mButtonPause.setTextColor(COLOR_TEXT_NORMAL);

		mButtonDBS.setTextSize(TEXT_SIZE);
		mButtonEEG.setTextSize(TEXT_SIZE);
		mButtonPause.setTextSize(TEXT_SIZE);

		// one button listener used by two buttons
		mButtonEEG.setOnClickListener(this);
		mButtonDBS.setOnClickListener(this);
		mButtonPause.setOnClickListener(this);

		mRadioGroup.setOnCheckedChangeListener(this);

		mRateRefreshHandler = new Handler();
		mRateRefreshHandler.post(mRateRefreshRunnable);
		// mEmergencyCheckHandler.post(mEmergencyCheckRunnable);

		// create reusable layout parameter for adding view
		LinearLayout.LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f);

		// create & add waveform view dynamically
		for (int i = 0; i < WAVEFORM_COUNT; i++) {
			mTextChannelNameArray[i] = new TextView(this);
			mTextChannelNameArray[i].setText("ECG Channel");// + (i + 1));
			mTextChannelNameArray[i].setTextSize(TEXT_SIZE);
			mTextChannelNameArray[i].setTextColor(0xFF00CC00);
			mTextChannelNameArray[i].setGravity(android.view.Gravity.CENTER);

			mWaveformArray[i] = new WaveformView(this);

			mChannelNameContainer.addView(mTextChannelNameArray[i], params);
			mWaveformContainer.addView(mWaveformArray[i], params);
			mWaveformArray[i].setAdapter(mWaveformAdapter);
		}

	}

	Runnable mRateRefreshRunnable = new Runnable() {
		public void run() {
			int Rate = getHeartRate();
			int deltaRate = getDeltaRate();
			String ID = getID();
			double longitude = getLongitude();
			double latitude = getLatitude();

			for (int i = 0; i < WAVEFORM_COUNT; i++) {
				if ((Rate >= 100) || deltaRate >= 45) {
					if ((Rate >= 125) || deltaRate >= 60) {
						if ((Rate >= 140) || deltaRate >= 70) {
							mTextChannelNameArray[i]
									.setTextColor(RATE_140_COLOR);
						}
						mTextChannelNameArray[i].setTextColor(RATE_110_COLOR);
					}
					mTextChannelNameArray[i].setTextColor(RATE_90_COLOE);
				} else {
					mTextChannelNameArray[i].setTextColor(SAFE_RATE_COLOR);
				}
				mTextChannelNameArray[i].setText("ECG Channel"
						+ "\nHeart Rate = " + Rate + " /min");

				mTextChannelNameArray[i].setText("ECG Channel"
						+ "\nHeart Rate = " + Rate + " /min");

				mIDTextView.setText("ID : " + ID);
				mIDTextView.setTextColor(COLOR_TEXT_NORMAL);
				mIDTextView.setTextSize(TEXT_SIZE);

				mlongitudeTextView.setText("longitude : " + longitude);
				mlongitudeTextView.setTextColor(COLOR_TEXT_NORMAL);
				mlongitudeTextView.setTextSize(TEXT_SIZE);

				mlatitudeTextView.setText("latitude : " + latitude);
				mlatitudeTextView.setTextColor(COLOR_TEXT_NORMAL);
				mlatitudeTextView.setTextSize(TEXT_SIZE);

			}
			mRateRefreshHandler.postDelayed(this, HEART_RATE_UPDATE_PERIOD);
		}

	};

	private String getID() {
		return mHealthDeviceBluetoothService.getID();
	}

	private double getLongitude() {
		return mHealthDeviceBluetoothService.getLongitude();
	}

	private double getLatitude() {
		return mHealthDeviceBluetoothService.getLatitude();
	}

	public int getHeartRate() {
		return mHealthDeviceBluetoothService.getHeartRate();
	}

	public int getDeltaRate() {
		return mHealthDeviceBluetoothService.getDeltaRate();
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
		// if (mHealthDeviceBluetoothService != null) {
		// mHealthDeviceBluetoothService.stop();
		// }
		stopDrawing();
	};

	/**
	 * This function would be called before screen orientation
	 */
	// @SuppressWarnings("deprecation")
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
			setSignal(SIGNAL_DBS);
			break;
		case R.id.buttonEEG:
			setSignal(SIGNAL_DBS);
			break;
		case R.id.buttonPause:
			pauseAndStartDrawing();
			break;
		default:
			break;
		}
	}

	private void refreshSignalButtonAndText() {
		switch (mSignal) {
		case SIGNAL_DBS:
			mButtonDBS.setTextColor(COLOR_TEXT_SELECTED);
			mButtonEEG.setTextColor(COLOR_TEXT_NORMAL);
			mButtonPause.setTextColor(COLOR_TEXT_NORMAL);
			// We still just have one channel ECG signal now .
			// mSignalTypeTextView.setText(TITLE_DBS);
			break;
		case SIGNAL_EEG:
			mButtonEEG.setTextColor(COLOR_TEXT_SELECTED);
			mButtonDBS.setTextColor(COLOR_TEXT_NORMAL);
			mButtonPause.setTextColor(COLOR_TEXT_NORMAL);
			// We still just have one channel ECG signal now .
			// mSignalTypeTextView.setText(TITLE_EEG);
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
			mWaveformArray[i].createNewDataSet(DEFAULT_SIZE, 1000, 0,
					PLOTTING_OFFSET);
			mWaveformArray[i].setLineColor(0, LINE_COLOR_ARRAY[i]);
		}
	}

	/**
	 * Override "WaveformAdapter" in "WaveformView.java" to have
	 * "getCurrentData()" function , which will return "getCurrentData()" in
	 * "HealthDeviceBluetoothService.java" .
	 **/
	private WaveformAdapter mWaveformAdapter = new WaveformAdapter() {
		@Override
		public int[] getCurrentData(int set, int preferedSize) {
			return mHealthDeviceBluetoothService.getCurrentData(mSignal,
					preferedSize);
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
		case R.id.locationable:
			ensureLocationable();
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

	private void ensureLocationable() {
		LocationManager status = (LocationManager) (this
				.getSystemService(Context.LOCATION_SERVICE));
		if (status.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
		} else {
			startActivity(new Intent(
					android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
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
		mHealthDeviceBluetoothService.connect(device, secure);
	}

	private void setupConnect() {
		// Initialize the DataReceiveService to perform bluetooth connections if
		// no DataReceive source .
		if (mHealthDeviceBluetoothService == null) {
			mHealthDeviceBluetoothService = HealthDeviceBluetoothService
					.getInstance(this);
			mHealthDeviceBluetoothService.setHandler(mBluetoothHandler);
			mHealthDeviceBluetoothService.start();
		}

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		int mode;
		switch (checkedId) {
		case R.id.radio0:
			mode = WaveformView.MODE_MOVING;
			break;
		default:
		case R.id.radio1:
			mode = WaveformView.MODE_STATIC;
			break;
		}
		for (WaveformView wave : mWaveformArray) {
			wave.setMode(mode);
		}
	}

	/** These functions below are for emergency SMS message sending . **/
	// Runnable mEmergencyCheckRunnable = new Runnable() {
	// public void run() {
	// mEmergency = mHealthDeviceBluetoothService.onHeartBeatStop();
	// if (mEmergency == true && mSMSSended == false && mConnectionCheck) {
	// // emergencyCall();
	// Log.v("Waveform", "Waveform detect emergency event");
	// } else {
	//
	// }
	// mRateRefreshHandler.postDelayed(this, EMERGENCY_CHECH_PERIOD);
	// }
	// };
	//
	// public String mSelfPhoneNumber;
	//
	// public String getSelfPhoneNumber() {
	// TelephonyManager phoneManager = (TelephonyManager)
	// getApplicationContext()
	// .getSystemService(Context.TELEPHONY_SERVICE);
	// mSelfPhoneNumber = phoneManager.getLine1Number();
	// Log.v("Waveform", "PhoneNumberGet = " + mSelfPhoneNumber);
	// return mSelfPhoneNumber;
	// }

	// private void emergencyCall() {
	// SELF_PHONE_NUMBER = getSelfPhoneNumber();
	// SmsManager smsManager = SmsManager.getDefault();
	// PendingIntent pendingIntent = PendingIntent.getBroadcast(
	// WaveformActivity.this, 0, new Intent(), 0);
	// smsManager.sendTextMessage(EMERGENCYC_CONNECTION_PHONE_NUMBER, null,
	// SMS_MESSEGE_CONTENT + " from " + SELF_PHONE_NUMBER,
	// pendingIntent, null);
	// mSMSSended = true;
	// }

}
