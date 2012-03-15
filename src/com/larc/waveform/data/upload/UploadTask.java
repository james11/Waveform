package com.larc.waveform.data.upload;

import java.io.File;
import java.io.Serializable;

import com.larc.bluetoothconnect.ServerApi;

public class UploadTask implements Serializable {

	private static final long serialVersionUID = 1L;
	private final File mFile;
	private final String mId;
	private final String mName;
	private final String mPhoneNumber;

	public UploadTask(File file, String id, String name, String phoneNumber) {

		mFile = file;
		mId = id;
		mName = name;
		mPhoneNumber = phoneNumber;
	}

	public boolean upload() {
		String response = ServerApi.uploadFile(mId, mName, mPhoneNumber, mFile);
		if (response == null) {
			return false;
		} else {
			return response.matches("Uploading Success");
		}
	}

}
