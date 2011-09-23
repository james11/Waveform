package com.larc.waveform;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import com.larc.waveform.R.color;

public class WaveformActivity2 extends Activity {
	/** Called when the activity is first created. */

	// private WaveformView mWaveformView1;
	// private WaveformView mWaveformView2;
	// private WaveformView mWaveformView3;
	// private WaveformView mWaveformView4;
	private WaveformView mWaveformView5;
	private WaveformView mWaveformView6;
	private WaveformView mWaveformView7;
	private WaveformView mWaveformView8;
	private Handler mHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// mWaveformView1 = (WaveformView) findViewById(R.id.waveformView1);
		// mWaveformView2 = (WaveformView) findViewById(R.id.waveformView2);
		// mWaveformView3 = (WaveformView) findViewById(R.id.waveformView3);
		// mWaveformView4 = (WaveformView) findViewById(R.id.waveformView4);
//		mWaveformView5 = (WaveformView) findViewById(R.id.waveformView5);
//		mWaveformView6 = (WaveformView) findViewById(R.id.waveformView6);
//		mWaveformView7 = (WaveformView) findViewById(R.id.waveformView7);
//		mWaveformView8 = (WaveformView) findViewById(R.id.waveformView8);

		mHandler = new Handler();
		mHandler.post(mPushDataRunnable);

		// mWaveformView.createNewDataSet(50);
		mWaveformView7.setLineColor(0, Color.GREEN);
		//
		// // mWaveformView.createNewDataSet(200);
		mWaveformView6.setLineColor(0, Color.BLUE);

		mWaveformView5.setLineColor(0, color.weak_yellow);

		// mWaveformView1.start();
		// mWaveformView2.start();
		// mWaveformView3.start();
		// mWaveformView4.start();
		mWaveformView5.start();
		mWaveformView6.start();
		mWaveformView7.start();
		mWaveformView8.start();

		// mButton1 = (Button) findViewById(R.id.myButton1);
		// mButton2 = (Button) findViewById(R.id.myButton2);
		// mTextView = (TextView) findViewById(R.id.myTextView);

		// Button mButton1 = (Button) findViewById(R.id.myButton1);

		Button mButtonPause2 = (Button) findViewById(R.id.buttonPause);
		mButtonPause2.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View view) {
				Intent intent = new Intent();
				intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				intent.setClass(WaveformActivity2.this, WaveformActivity2.class);
				startActivity(intent);
			}
		});

		Button mButton2 = (Button) findViewById(R.id.myButton2);
		mButton2.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View view) {

				Intent intent = new Intent();
				intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				intent.setClass(WaveformActivity2.this, WaveformActivity.class);
				startActivity(intent);
			}
		});

	}

	private Runnable mPushDataRunnable = new Runnable() {
		public void run() {
			int data = (int) (System.currentTimeMillis() % 100) - 50;
			// mWaveformView1.setCurrentData(0, data);
			// mWaveformView2.setCurrentData(0, -data);
			// mWaveformView3.setCurrentData(0, -data);
			// mWaveformView4.setCurrentData(0, data);
			mWaveformView5.setCurrentData(0, data);
			mWaveformView6.setCurrentData(0, -data);
			mWaveformView7.setCurrentData(0, -data);
			mWaveformView8.setCurrentData(0, data);
			mHandler.postDelayed(this, 10);
		}

	};

}
