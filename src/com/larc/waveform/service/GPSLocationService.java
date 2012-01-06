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

public class GPSLocationService implements LocationListener {

	private static final String TAG = "GPS";

	private int LOCATION_SERVICE_CHECK_PERIOD = 1000 * 10;
	private int LOCATION_UPDATE_PERIOD = 1000 * 60 * 10;
	private int LOCATION_UPDATE_DISTANCE = 10;

	private Context mContext;
	private LocationManager mLocationManager;
	private LocationData mLocationData;

	private String bestProvider = LocationManager.GPS_PROVIDER;
	public double mlongitude;
	public double mlatitude;

	private static GPSLocationService gInstance;

	public static GPSLocationService getInstance() {
		if (gInstance == null) {
			gInstance = new GPSLocationService();
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
				Toast.makeText(mContext, "Please open Location Service",
						Toast.LENGTH_LONG).show();
				// Recall runnable every 10 seconds until location provider is
				// opened .
				mLocationServiceHandler.postDelayed(this,
						LOCATION_SERVICE_CHECK_PERIOD);
			}
		}
	};

	private void locationServiceInitial() {
		mLocationData = LocationData.getInstance();
		mLocationData.setHandler();
		mLocationData.mLocationDataHandler.postDelayed(
				mLocationData.mLocationDataRunnable, mLocationData.LOCATION_BUFFER_SAVE_PERIOD);
		mLocationManager.requestLocationUpdates(bestProvider,
				LOCATION_UPDATE_PERIOD, LOCATION_UPDATE_DISTANCE, this);

		Criteria criteria = new Criteria(); // Information provider standard .
		bestProvider = mLocationManager.getBestProvider(criteria, true); // Select
																			// most
																			// accurate
																			// provider
																			// .

		// Use Network to get location .
		Location location = mLocationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		getLocation(location);
	}

	private void getLocation(Location location) {

		if (location != null) {
			mlongitude = location.getLongitude(); // getLongitude
			mlatitude = location.getLatitude(); // getLatitude

			mLocationData.write(mlongitude);
			mLocationData.write(mlatitude);

			Log.v(TAG, "Longitude = " + mlongitude);
			Log.v(TAG, "Latitude = " + mlatitude);

		} else {
			Toast.makeText(mContext, "Can not get Location INFO",
					Toast.LENGTH_LONG).show();
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
