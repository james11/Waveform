package com.larc.waveform.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class EmergencyCallService extends Activity {

	private String EMERGENCYC_CONNECTION_PHONE_NUMBER = "0972152898";
	private String SELF_PHONE_NUMBER;
	private String SMS_MESSEGE_CONTENT = "Emergency";
	private String mSelfPhoneNumber;
	private boolean mSMSSended = false;

	private String getSelfPhoneNumber() {
		TelephonyManager phoneManager = (TelephonyManager) getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		mSelfPhoneNumber = phoneManager.getLine1Number();
		Log.v("Waveform", "PhoneNumberGet = " + mSelfPhoneNumber);
		return mSelfPhoneNumber;
	}

	private void emergencyCall() {
		SELF_PHONE_NUMBER = getSelfPhoneNumber();
		SmsManager smsManager = SmsManager.getDefault();
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
				new Intent(), 0);
		smsManager.sendTextMessage(EMERGENCYC_CONNECTION_PHONE_NUMBER, null,
				SMS_MESSEGE_CONTENT + " from " + SELF_PHONE_NUMBER,
				pendingIntent, null);
		mSMSSended = true;
	}

	void emergencyEventCheck() {
		if (mSMSSended == false) {
			Log.v("Waveform", "Emergency  ");
			// emergencyCall();
		} else {

		}
	}

	// //
	// // private void sendSMS(String phoneNumber, String message) {
	// // String SENT = "SMS_SENT";
	// // String DELIVERED = "SMS_DELIVERED";
	// //
	// // PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
	// // SENT), 0);
	// //
	// // PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
	// // new Intent(DELIVERED), 0);
	// //
	// // // ---when the SMS has been sent---
	// // registerReceiver(new BroadcastReceiver() {
	// // @Override
	// // public void onReceive(Context arg0, Intent arg1) {
	// // switch (getResultCode()) {
	// // case Activity.RESULT_OK:
	// // Toast.makeText(getBaseContext(), "SMS sent",
	// // Toast.LENGTH_SHORT).show();
	// // break;
	// // case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
	// // Toast.makeText(getBaseContext(), "Generic failure",
	// // Toast.LENGTH_SHORT).show();
	// // break;
	// // case SmsManager.RESULT_ERROR_NO_SERVICE:
	// // Toast.makeText(getBaseContext(), "No service",
	// // Toast.LENGTH_SHORT).show();
	// // break;
	// // case SmsManager.RESULT_ERROR_NULL_PDU:
	// // Toast.makeText(getBaseContext(), "Null PDU",
	// // Toast.LENGTH_SHORT).show();
	// // break;
	// // case SmsManager.RESULT_ERROR_RADIO_OFF:
	// // Toast.makeText(getBaseContext(), "Radio off",
	// // Toast.LENGTH_SHORT).show();
	// // break;
	// // }
	// // }
	// // }, new IntentFilter(SENT));
	// //
	// // // ---when the SMS has been delivered---
	// // registerReceiver(new BroadcastReceiver() {
	// // @Override
	// // public void onReceive(Context arg0, Intent arg1) {
	// // switch (getResultCode()) {
	// // case Activity.RESULT_OK:
	// // Toast.makeText(getBaseContext(), "SMS delivered",
	// // Toast.LENGTH_SHORT).show();
	// // break;
	// // case Activity.RESULT_CANCELED:
	// // Toast.makeText(getBaseContext(), "SMS not delivered",
	// // Toast.LENGTH_SHORT).show();
	// // break;
	// // }
	// // }
	// // }, new IntentFilter(DELIVERED));
	// //
	// // SmsManager sms = SmsManager.getDefault();
	// // sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	// // }
}
