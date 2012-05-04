package com.larc.waveform;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

public class AppVersionInfoActivity extends Activity {
	// private static final String TAG = "AppVersionInfo";
	// private static final boolean D = true;

	private TextView mAppInfoTextview;
	private TextView mVersionTextview;
	private TextView mDateTextview;
	private TextView mIntroductionTextview;
	private TextView mEditorTextview;
	private TextView mDepartmentTextview;

	private static final int COLOR_TEXT_TITLE = 0xFF0033FF;
	private static final int COLOR_TEXT_NORMAL = Color.BLACK;
	private static final int COLOR_TEXT_Editor_DEPARTMENT = 0xFF777777;
	private static final int COLOR_TEXT_HIGHLIGHT = 0xFFFF9900;

	private static final int INFO_TEXT_SIZE = 20; // Tablet:35, SmartPhone: 20
	private static final int VERSION_TEXT_SIZE = 20; // Tablet:35, SmartPhone:
														// 20
	private static final int DATE_TEXT_SIZE = 15;// Tablet:20, SmartPhone: 15
	private static final int INTRODUCTION_TEXT_SIZE = 15;// Tablet:25,
															// SmartPhone: 15
	private static final int EDITOR_TEXT_SIZE = 20;// Tablet:30, SmartPhone: 20
	private static final int DEPARTMENT_TEXT_SIZE = 20;// Tablet:30, SmartPhone:
														// 20

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Setup the window
		setContentView(R.layout.activity_app_info);

		// Set result CANCELED in case the user backs out
		setResult(Activity.RESULT_CANCELED);

		mAppInfoTextview = (TextView) findViewById(R.id.myAppInfoTextView);
		mAppInfoTextview
				.setText("WaveformActivity Application Version Information\n");
		mAppInfoTextview.setTextColor(COLOR_TEXT_TITLE);
		mAppInfoTextview.setTextSize(INFO_TEXT_SIZE);

		mVersionTextview = (TextView) findViewById(R.id.myVersionTextView);
		mVersionTextview.setText("Version : 1.1");
		mVersionTextview.setTextColor(COLOR_TEXT_HIGHLIGHT);
		mVersionTextview.setTextSize(VERSION_TEXT_SIZE);

		mDateTextview = (TextView) findViewById(R.id.myDateTextView);
		mDateTextview.setText("\t\t\t(Updated at 2012/04/29)\n");
		mDateTextview.setTextColor(COLOR_TEXT_NORMAL);
		mDateTextview.setTextSize(DATE_TEXT_SIZE);

		mIntroductionTextview = (TextView) findViewById(R.id.myIntroductionTextView);
		mIntroductionTextview
				.setText("Introduction:\n"
						+ "\t1. For 19200 boud rate 'Tablet'\n"
						+ "\t2. Google unique ID added\n"
						+ "\t3. Version control added\n"
						+ "\t4. Heart rate count Fixed\n"
						+ "\t5. Buttons redesigned\n");
		mIntroductionTextview.setTextColor(COLOR_TEXT_NORMAL);
		mIntroductionTextview.setTextSize(INTRODUCTION_TEXT_SIZE);

		mEditorTextview = (TextView) findViewById(R.id.myEditorTextView);
		mEditorTextview.setText("Editor : \n" + "\t\tChun-Chieh Chan \n");
		mEditorTextview.setTextColor(COLOR_TEXT_Editor_DEPARTMENT);
		mEditorTextview.setTextSize(EDITOR_TEXT_SIZE);

		mDepartmentTextview = (TextView) findViewById(R.id.myDepartmentTextView);
		mDepartmentTextview.setText("Department : \n"
				+ "\t\tNTHU EE LaRC Hp_group");
		mDepartmentTextview.setTextColor(COLOR_TEXT_Editor_DEPARTMENT);
		mDepartmentTextview.setTextSize(DEPARTMENT_TEXT_SIZE);

		// Set result CANCELED in case the user backs out
		setResult(Activity.RESULT_CANCELED);

	}

}
