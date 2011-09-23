package com.larc.waveform;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.larc.waveform.data.ReceivedData;

public class WaveformActivity extends Activity {
	private static final int DEFAULT_SIZE = 100;
	private static final int WAVEFORM_COUNT = 4;

	private static final int COLOR_TEXT_NORMAL = Color.BLACK;
	private static final int COLOR_TEXT_SELECTED = 0xFFFF9900;

	private static final int SIGNAL_EEG = 0;
	private static final int SIGNAL_DBS = 1;

	/** Called when the activity is first created. */

	private WaveformView[] mWaveformArray;
	private TextView[] mWaveformName;

	private LinearLayout mWaveformContainer;
	private LinearLayout mWaveformBlock;

	private Handler mHandler;
	private Button mButtonPause;
	private Button mButtonEEG;
	private Button mButtonDBS;
	private TextView mTextView;

	private ReceivedData[] mDbsData = new ReceivedData[WAVEFORM_COUNT];
	private ReceivedData[] mEegData = new ReceivedData[WAVEFORM_COUNT];

	private int mSignal = SIGNAL_EEG;
	private int mSignalCheck = SIGNAL_DBS;
	private boolean mPause = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_waveform);
		for (int i = 0; i < WAVEFORM_COUNT; i++) {
			mDbsData[i] = new ReceivedData();
			mEegData[i] = new ReceivedData();
		}

		mButtonPause = (Button) findViewById(R.id.buttonPause);
		mButtonEEG = (Button) findViewById(R.id.buttonEEG);
		mButtonDBS = (Button) findViewById(R.id.buttonDBS);
		mTextView = (TextView) findViewById(R.id.myTextView);

		mWaveformArray = new WaveformView[WAVEFORM_COUNT];
		mWaveformName = new TextView[WAVEFORM_COUNT];

		mWaveformContainer = (LinearLayout) findViewById(R.id.waveformContainer);
		mWaveformBlock = (LinearLayout) findViewById(R.id.waveformBlock);

		LinearLayout.LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f);

		for (int i = 0; i < WAVEFORM_COUNT; i++) {

			mWaveformName[i] = new TextView(this);
			mWaveformName[i].setText("CH"+(i+1));
			mWaveformName[i].setTextSize(40);
			mWaveformName[i].setTextColor(0xffff9900);
			mWaveformName[i].setGravity(android.view.Gravity.CENTER);

			mWaveformArray[i] = new WaveformView(this);
			
			 int HorizontalScrollView = 1;
			 mWaveformContainer.setOrientation(HorizontalScrollView);
			 mWaveformBlock.setOrientation(HorizontalScrollView);

			mWaveformContainer.addView(mWaveformArray[i], params);
			mWaveformBlock.addView(mWaveformName[i], params);

		}

		for (WaveformView wave : mWaveformArray) {

			mWaveformArray[1].setLineColor(0, Color.GREEN);

			mWaveformArray[2].setLineColor(0, Color.BLUE);

			mWaveformArray[3].setLineColor(0, R.color.weak_yellow);

			mTextView.setText("4-Channel DBS Signals");

			mButtonDBS.setTextColor(COLOR_TEXT_SELECTED);
			mButtonEEG.setTextColor(COLOR_TEXT_NORMAL);

			wave.start();
		}

		mButtonPause.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View view) {
				Log.v("Wave", "ButtonPauseClicked");
				pauseAndStart();
			}
		});

		mButtonEEG.setOnClickListener(mButtonClickListener);

		mButtonDBS.setOnClickListener(mButtonClickListener);

		mHandler = new Handler();
		mHandler.post(mPushDataRunnable);

		refreshSignal();
	}

	private Button.OnClickListener mButtonClickListener = new Button.OnClickListener() {
		// @Override
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
				wave.start();
			}
		}
	}

	public void showDBS() {
		mSignal = 0;
		for (WaveformView wave : mWaveformArray) {
			wave.removeAllDataSet();
			wave.createNewDataSet(DEFAULT_SIZE);
			// for (int i = 0; i < WAVEFORM_COUNT; i++) {
			int[] dataArray = mDbsData[0].getData(DEFAULT_SIZE + 50);
			wave.setData(0, dataArray);
			// }
		}
		mWaveformArray[1].setLineColor(0, Color.GREEN);

		mWaveformArray[2].setLineColor(0, Color.BLUE);

		mWaveformArray[3].setLineColor(0, R.color.weak_yellow);
	}

	public void showEEG() {
		mSignal = 1;
		for (WaveformView wave : mWaveformArray) {
			wave.removeAllDataSet();
			wave.createNewDataSet(DEFAULT_SIZE);
			// for (int i = 0; i < WAVEFORM_COUNT; i++) {
			int[] dataArray = mEegData[0].getData(DEFAULT_SIZE);
			wave.setData(0, dataArray);
			// }
		}
		mWaveformArray[1].setLineColor(0, Color.GREEN);

		mWaveformArray[2].setLineColor(0, Color.BLUE);

		mWaveformArray[3].setLineColor(0, R.color.weak_yellow);
	}

	private Runnable mPushDataRunnable = new Runnable() {
		public void run() {
			int data = (int) (System.currentTimeMillis() % 100) - 50;
			mDbsData[0].add(data);
			mEegData[0].add(-data);
			for (WaveformView wave : mWaveformArray) {
				wave.setCurrentData(0, data);
			}
			mHandler.postDelayed(this, 10);
		}

	};

}
