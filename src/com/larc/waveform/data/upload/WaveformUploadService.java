package com.larc.waveform.data.upload;

import java.util.Vector;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

/***
 * This class be called by function "uploadSavedFiles()" in
 * "DataFileManager.java" . uploadSavedFiles()---> listUploadTask(){
 * context.startService(intent) : start WaveformUploadService which extend
 * IntentService } ---> UploadTask ---> ServerAPI .
 ***/
public class WaveformUploadService extends IntentService {

	private static final String TAG = "WaveformUploadService";

	private static Vector<UploadTask> sUploadList = new Vector<UploadTask>();
	private static Vector<UploadTask> sFailedList = new Vector<UploadTask>();

	public WaveformUploadService(String name) {
		super(TAG);
	}

	public WaveformUploadService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (sUploadList.size() > 0) {
			boolean result = false;
			UploadTask uploadTask = sUploadList.get(0);
			result = uploadTask.upload();
			if (!result) {
				sFailedList.add(uploadTask);
			}
			sUploadList.remove(0);
		}
	}

	public static void listUploadTask(Context context, UploadTask uploadTask) {
		sUploadList.add(uploadTask);
		// Set Service Intent .
		Intent intent = new Intent(context, WaveformUploadService.class);
		context.startService(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (sFailedList.size() > 0) {
			// TODO: backup upload list and restart in the future
		}
	}

}
