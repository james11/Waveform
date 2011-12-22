package com.larc.waveform.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.larc.waveform.WaveformApplication;

public class EmergencyCallService {

	private static EmergencyCallService eInstance;

	private String EMERGENCYC_CONNECTION_PHONE_NUMBER = "0916961459";
	private String SELF_PHONE_NUMBER;
	private String SMS_MESSEGE_CONTENT = "Emergency";
	private String mSelfPhoneNumber;
	private boolean mSMSSended = false;

	private Context mContext;

	public static EmergencyCallService getInstance() {
		if (eInstance == null) {
			eInstance = new EmergencyCallService();
		}
		return eInstance;
	}

	private String getSelfPhoneNumber() {
		TelephonyManager phoneManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		mSelfPhoneNumber = phoneManager.getLine1Number();
		Log.v("Waveform", "PhoneNumberGet = " + mSelfPhoneNumber);
		return mSelfPhoneNumber;
	}

	private void emergencyCall() {
		SELF_PHONE_NUMBER = getSelfPhoneNumber();
		SmsManager smsManager = SmsManager.getDefault();
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0,
				new Intent(), 0);
		smsManager.sendTextMessage(EMERGENCYC_CONNECTION_PHONE_NUMBER, null,
				SMS_MESSEGE_CONTENT + " from " + SELF_PHONE_NUMBER,
				pendingIntent, null);
		mSMSSended = true;
	}

	public void emergencyEventCheck() {
		if (mSMSSended == false) {
			mContext = WaveformApplication.getInstance();
			Log.v("EmergencyCallService", "Waveform detect emergency event");
			// emergencyCall();
			mSMSSended = true;
		}
	}
}
