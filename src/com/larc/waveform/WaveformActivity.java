package com.larc.waveform;

import java.io.File;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.larc.bluetoothconnect.BluetoothService;
import com.larc.bluetoothconnect.DeviceListActivity;
import com.larc.waveform.data.ReceivedDataSaver;
import com.larc.waveform.fileupload.UploadFile;
import com.larc.waveform.service.DataReceiveService;
import com.larc.waveform.ui.widget.WaveformView;
import com.larc.waveform.ui.widget.WaveformView.WaveformAdapter;

public class WaveformActivity extends Activity implements
		Button.OnClickListener {
	private static final int DEFAULT_SIZE = 5000; // Screen pixels number
	private static final int WAVEFORM_COUNT = 1;

	private static final int COLOR_TEXT_NORMAL = Color.GRAY;
	private static final int COLOR_TEXT_SELECTED = 0xFFFF9900;
	private static final int SAFE_RATE_COLOR = 0xFF00AA00;
	private static final int RATE_90_COLOE = 0xFFFF6600;
	private static final int RATE_110_COLOR = 0xFFCC0000;
	private static final int RATE_140_COLOR = 0xFF660066;

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

	private Handler mRateRefreshHandler;
	private int mRateUpdatePeriod = 10000;
	private int mRate = 30;
	private int mLast20SecRate = 0;
	private int mLast10SecRate = 90;

	private String EMERGENCYC_CONNECTION_PHONE_NUMBER = "0918183964";
	private String SELF_PHONE_NUMBER;
	private String SMS_MESSEGE_CONTENT = "Emergency";
	private String mSelfPhoneNumber;
	private boolean mEmergency = false;
	private boolean mConnectionCheck = false;
	private boolean mSMSSended = false;

	private Handler mUploadHandler;
	private long mUploadPeriod = 1000 * 60 * 1 / 3;
	private UploadFile mUploadFile;
	private File[] mFile = new File[1024];

	private TextView resulView;
	private ProgressBar uploadbar;
	private ReceivedDataSaver mReceivedDataSaver;
	private String mfilenameText;

	private BluetoothAdapter mBluetoothAdapter = null;
	private DataReceiveService mDataReceiveService;

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
		mTextView = (TextView) findViewById(R.id.myTextView);
		mWaveformContainer = (LinearLayout) findViewById(R.id.waveformContainer);
		mChannelNameContainer = (LinearLayout) findViewById(R.id.channelNameBlock);

		// uploadbar = (ProgressBar) this.findViewById(R.id.uploadBar);
		// resulView = (TextView) this.findViewById(R.id.result);

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

//		mUploadHandler = new Handler();
//		mUploadHandler.postDelayed(mUploadRunnable, mUploadPeriod);

		// create reusable layout parameter for adding view
		LinearLayout.LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f);

		// create & add waveform view dynamically
		for (int i = 0; i < WAVEFORM_COUNT; i++) {
			mTextChannelNameArray[i] = new TextView(this);
			mTextChannelNameArray[i].setText("ECG Channel");// + (i + 1));
			mTextChannelNameArray[i].setTextSize(20);
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
			energencyEventCheck();
			mRate = getRate();
			// Ignore error count , when heart rate count during 1 min >= 150
			// or <= 50
			int deltaRate = java.lang.Math.abs(mRate - mLast10SecRate);
			if (deltaRate >= 40) {
				mRate = mLast10SecRate;
			}
			Log.v("Waveform", "mRate = " + mRate);

			int Rate = (4 * mRate + 3 * mLast10SecRate + 2 * mLast20SecRate) / 9;
			for (int i = 0; i < WAVEFORM_COUNT; i++) {

				if ((mRate >= 100)) {
					if ((mRate >= 110)) {
						if ((mRate >= 130)) {
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
			}
			mLast20SecRate = mLast10SecRate;
			mLast10SecRate = mRate;
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

	private String getSelfPhoneNumber() {
		TelephonyManager phoneManager = (TelephonyManager) getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		mSelfPhoneNumber = phoneManager.getLine1Number();
		Log.v("Waveform", "PhoneNumberGet = " + mSelfPhoneNumber);
		return mSelfPhoneNumber;
	}

	private void energencyEventCheck() {
		mEmergency = mDataReceiveService.emergencyEventCheck();
		if (mEmergency == true && mConnectionCheck == true
				&& mSMSSended == false) {
//			emergencyCall();
		} else {

		}
	}

	@SuppressWarnings("deprecation")
	private void emergencyCall() {
		SELF_PHONE_NUMBER = getSelfPhoneNumber();
		SmsManager smsManager = SmsManager.getDefault();
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				WaveformActivity.this, 0, new Intent(), 0);
		smsManager.sendTextMessage(EMERGENCYC_CONNECTION_PHONE_NUMBER, null,
				SMS_MESSEGE_CONTENT + " from " + SELF_PHONE_NUMBER,
				pendingIntent, null);
		Log.v("Waveform", "Emergency  ");
		mSMSSended = true;
	}

	Runnable mUploadRunnable = new Runnable() {
		public void run() {

			String uri = "http://140.114.14.63/httpPost.php";
			int FileCount = mReceivedDataSaver.getFileCount();

			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				for (int i = 1; i < FileCount; i++) {
					mFile[i - 1] = mReceivedDataSaver.getFile(i);
					mUploadFile.uploadFile(mFile[i - 1], uri);
				}
				mReceivedDataSaver.setFileCount(1);
				mRateRefreshHandler.postDelayed(this, mUploadPeriod);
			} else {
				Toast.makeText(WaveformActivity.this, R.string.sdcarderror, 1)
						.show();
			}
			Log.v("Waveform", "UploadFileRunnable");
		}
	};

	/**
	 * user Handler to send message to the Thread that creates this Handler.
	 */
	// private Handler handler = new Handler() {
	// @Override
	// public void handleMessage(Message msg) {
	// // get uploading progress report
	// int length = msg.getData().getInt("size");
	// uploadbar.setProgress(length);
	// float num = (float) uploadbar.getProgress()
	// / (float) uploadbar.getMax();
	// int result = (int) (num * 100);
	// // set report result
	// resulView.setText(result + "%");
	// // uploading success
	// if (uploadbar.getProgress() == uploadbar.getMax()) {
	// Toast.makeText(WaveformActivity.this, R.string.success, 1)
	// .show();
	// }
	// }
	// };

	/**
	 * Create a Threat to uploading files. use Handler to avoid UI Thread ANR
	 * error.
	 * 
	 * @param final uploadFile
	 */
	// private void uploadFile(final File uploadFile) {
	// new Thread(new Runnable() {
	// @Override
	// public void run() {
	// try {
	// // set maximum length of files
	// uploadbar.setMax((int) uploadFile.length());
	// // Check if file has been uploaded before
	// String souceid = logService.getBindId(uploadFile);
	// // set header
	// String head = "Content-Length=" + uploadFile.length()
	// + ";filename=" + uploadFile.getName()
	// + ";sourceid=" + (souceid == null ? "" : souceid)
	// + "/r/n";
	// // Create socket an IOstream
	// Socket socket = new Socket("192.168.1.100", 7878);
	// OutputStream outStream = socket.getOutputStream();
	// outStream.write(head.getBytes());
	//
	// PushbackInputStream inStream = new PushbackInputStream(
	// socket.getInputStream());
	// // get id and position of byte[]
	// String response = StreamTool.readLine(inStream);
	// String[] items = response.split(";");
	// String responseid = items[0].substring(items[0]
	// .indexOf("=") + 1);
	// String position = items[1].substring(items[1].indexOf("=") + 1);
	// // if file has not been uploaded before , create a bindID in
	// // database
	// if (souceid == null) {
	// logService.save(responseid, uploadFile);
	// }
	// RandomAccessFile fileOutStream = new RandomAccessFile(
	// uploadFile, "r");
	// fileOutStream.seek(Integer.valueOf(position));
	// byte[] buffer = new byte[1024];
	// int len = -1;
	// // initialize ªø¶Ç data length
	// int length = Integer.valueOf(position);
	// while ((len = fileOutStream.read(buffer)) != -1) {
	// outStream.write(buffer, 0, len);
	// // set data length
	// length += len;
	// Message msg = new Message();
	// msg.getData().putInt("size", length);
	// handler.sendMessage(msg);
	// }
	// fileOutStream.close();
	// outStream.close();
	// inStream.close();
	// socket.close();
	// // delete data after uploading has done
	// if (length == uploadFile.length())
	// logService.delete(uploadFile);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }).start();
	// }
}
