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
	
	private Handler mmmHandler;
	private Button mButtonPause;
	private Button mButtonEEG;
	private Button mButtonDBS;

	private ReceivedData[] mDbsData = new ReceivedData[WAVEFORM_COUNT];
	private ReceivedData[] mEegData = new ReceivedData[WAVEFORM_COUNT];
	
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
		
		LinearLayout.LayoutParams params = 
				new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f);
		for(int i=0; i< WAVEFORM_COUNT ; i++){
			mWaveformArray[i] = new WaveformView(this);
			mWaveformContainer.addView(mWaveformArray[i], params);
		}
		

		for(WaveformView wave : mWaveformArray){
			wave.start();
		}
		
		mButtonPause.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View view) {
				for(WaveformView wave : mWaveformArray){
					wave.stop();
				}
			}
		});

		mButtonEEG.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View view) {
				showEEG();
			}
		});
		
		mButtonDBS.setOnClickListener(new Button.OnClickListener() {
			
			public void onClick(View v) {
				showDBS();
			}
		});
		mmmHandler = new Handler();
		mmmHandler.post(mPushDataRunnable);
	}
	
	private void showDBS(){
		for(WaveformView wave : mWaveformArray){
			wave.removeAllDataSet();
			wave.createNewDataSet(DEFAULT_SIZE);
			int [] dataArray = mDbsData[0].getData(DEFAULT_SIZE);
			wave.setData(0, dataArray);
		}
		mWaveformArray[1].setLineColor(0, Color.GREEN);

		mWaveformArray[2].setLineColor(0, Color.BLUE);

		mWaveformArray[3].setLineColor(0, R.color.weak_yellow);
	}
	
	public void showEEG(){
		for(WaveformView wave : mWaveformArray){
			wave.removeAllDataSet();
			wave.createNewDataSet(DEFAULT_SIZE);
			int [] dataArray = mEegData[0].getData(DEFAULT_SIZE);
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
			for(WaveformView wave : mWaveformArray){
				wave.setCurrentData(0, data);
			}
			mmmHandler.postDelayed(this, 10);
		}

	};

}
