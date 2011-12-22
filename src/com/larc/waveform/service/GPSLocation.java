package com.larc.waveform.service;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.larc.waveform.WaveformApplication;
import com.larc.waveform.data.LocationData;

public class GPSLocation implements LocationListener {

	private static final String TAG = "GPS";
	private Context mContext;

	private int LOCATION_SERVICE_CHECK_PERIOD = 1000 * 10;

	private LocationManager mLocationManager;
	private LocationData mLocationData;
	private String bestProvider = LocationManager.GPS_PROVIDER;
	public double mlongitude;
	public double mlatitude;

	private static GPSLocation gInstance;

	public static GPSLocation getInstance() {
		if (gInstance == null) {
			gInstance = new GPSLocation();
		}
		return gInstance;
	}

	public Handler mLocationServiceHandler;

	public void setHandler() {
		mLocationServiceHandler = new Handler();
	}

	Runnable mLocationServiceRunnable = new Runnable() {
		public void run() {
			// Get system location service .
			mContext = WaveformApplication.getInstance();
			LocationManager status = (LocationManager) (mContext
					.getSystemService(Context.LOCATION_SERVICE));
			if (status.isProviderEnabled(LocationManager.GPS_PROVIDER)
					|| status
							.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

				mLocationManager = status; //

				// Call locationServiceInitial() to update location when GPS or
				// NETWORK provider is open .
				locationServiceInitial();
			} else {
				Toast.makeText(mContext, "請開啟定位服務", Toast.LENGTH_LONG).show();
				// Recall runnable until location provider is opened .
				mLocationServiceHandler.postDelayed(this,
						LOCATION_SERVICE_CHECK_PERIOD);
			}
		}
	};

	private void locationServiceInitial() {
		// lms = (LocationManager) getSystemService(LOCATION_SERVICE);

		Criteria criteria = new Criteria(); // 資訊提供者選取標準
		bestProvider = mLocationManager.getBestProvider(criteria, true); // 選擇精準度最高的提供者

		// Use Network to get location .
		Location location = mLocationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		mLocationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
		getLocation(location);
	}

	private void getLocation(Location location) {
		if (location != null) {
			mlongitude = location.getLongitude(); // getLongitude (經度)
			mlatitude = location.getLatitude(); // getLatitude (緯度)
			mLocationData = LocationData.getInstance();
			mLocationData.write(mlongitude);
			mLocationData.write(mlatitude);

			Log.v(TAG, "mlongitude = " + mlongitude);
			Log.v(TAG, "mlatitude = " + mlatitude);

		} else {
			Toast.makeText(mContext, "無法定位座標", Toast.LENGTH_LONG).show();
		}
	}

	public double getLongitude() {
		return mlongitude;
	}

	public double getLatitude() {
		return mlatitude;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		getLocation(location);

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}
