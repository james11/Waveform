package com.larc.waveform;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.larc.waveform.data.ReceivedData;

public class WaveformActivity extends Activity {
	private static final int DEFAULT_SIZE = 50;
	private static final int WAVEFORM_COUNT = 4;

	/** Called when the activity is first created. */

	private WaveformView[] mWaveformArray;

	private LinearLayout mWaveformContainer;
<<<<<<< HEAD

=======
	
>>>>>>> parent of 1ef337e... Revert 97bf37c5f70f6b1bd02e5b71737f7c8e76d40492^..HEAD
	private Handler mHandler;
	private Button mButtonPause;
	private Button mButtonEEG;
	private Button mButtonDBS;

	private ReceivedData[] mDbsData = new ReceivedData[WAVEFORM_COUNT];
	private ReceivedData[] mEegData = new ReceivedData[WAVEFORM_COUNT];

	private int mEegShown = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_waveform);
		mDbsData[0] = new ReceivedData();
		mEegData[0] = new ReceivedData();

		mButtonPause = (Button) findViewById(R.id.buttonPause);
		mButtonEEG = (Button) findViewById(R.id.buttonEEG);
		mButtonDBS = (Button) findViewById(R.id.buttonDBS);

		mWaveformArray = new WaveformView[WAVEFORM_COUNT];
		mWaveformContainer = (LinearLayout) findViewById(R.id.waveformContainer);

		LinearLayout.LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f);

		for (int i = 0; i < WAVEFORM_COUNT; i++) {
			mWaveformArray[i] = new WaveformView(this);
			mWaveformContainer.addView(mWaveformArray[i], params);
		}

		for (WaveformView wave : mWaveformArray) {

			mWaveformArray[1].setLineColor(0, Color.GREEN);

			mWaveformArray[2].setLineColor(0, Color.BLUE);

			mWaveformArray[3].setLineColor(0, R.color.weak_yellow);

			wave.start();
		}

		mButtonPause.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View view) {
				for (WaveformView wave : mWaveformArray) {
					int on = 1;
					if (on == 1) {
						wave.stop();
						on = 0;
					} else {
						wave.start();
						on = 1;
					}
				}
			}
		});

		mButtonEEG.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View view) {
				if (mEegShown == 0) {
					showEEG();
				}
			}
		});

		mButtonDBS.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {
				if (mEegShown == 1) {
					showDBS();
				}
			}
		});
<<<<<<< HEAD

=======
>>>>>>> parent of 1ef337e... Revert 97bf37c5f70f6b1bd02e5b71737f7c8e76d40492^..HEAD
		mHandler = new Handler();
		mHandler.post(mPushDataRunnable);
	}

	public void showDBS() {
		mEegShown = 0;
		for (WaveformView wave : mWaveformArray) {
			wave.removeAllDataSet();
			wave.createNewDataSet(DEFAULT_SIZE);
			int[] dataArray = mDbsData[0].getData(DEFAULT_SIZE);
			wave.setData(0, dataArray);
		}
		mWaveformArray[1].setLineColor(0, Color.GREEN);

		mWaveformArray[2].setLineColor(0, Color.BLUE);

		mWaveformArray[3].setLineColor(0, R.color.weak_yellow);
	}

	public void showEEG() {
		mEegShown = 1;
		for (WaveformView wave : mWaveformArray) {
			wave.removeAllDataSet();
			wave.createNewDataSet(DEFAULT_SIZE);
			int[] dataArray = mEegData[0].getData(DEFAULT_SIZE);
			wave.setData(0, dataArray);
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
