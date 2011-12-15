package com.larc.waveform.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import android.os.Environment;

import com.larc.waveform.WaveformApplication;
import com.larc.waveform.data.upload.UploadTask;
import com.larc.waveform.data.upload.WaveformUploadService;

public class DataFileManager {
	private static final String OUTPUT_FILE_PATH = "/Larc/Waveform/";
	private static DataFileManager sInstance;

	public static class DataFileHeader {

		public long time;
		public int type;
		public int length;

		public byte[] toByte() {
			ByteBuffer buffer = ByteBuffer.allocate(16);
			buffer.putLong(time);
			buffer.putInt(type);
			buffer.putInt(length);
			return buffer.array();
		}
	}

	public final ArrayList<File> mSavedFileList = new ArrayList<File>();
	private String mId = "xxx";
	private String mName = "Larc";
	private String mPhoneNumber = "09xxxxxxxx";

	public static DataFileManager getInstance() {
		if (sInstance == null) {
			sInstance = new DataFileManager();
		}
		return sInstance;
	}

	public void saveData(final byte[] inputData, final int offset,
			final int length) {
		final byte[] data = Arrays.copyOfRange(inputData, offset, offset
				+ length);

		Thread thread = new Thread() {
			@Override
			public void run() {

				// StringBuilder builder = new StringBuilder(length * 3);
				// for (int i = 0; i < length; i++) {
				// int value = (int) data[i] & 0xFF;
				// builder.append(String.format("%03d,", value));
				// }

				Date date = new Date();
				String mfileName = generateFileName(date) + ".LaRC.txt";
				File dir = Environment.getExternalStorageDirectory();
				String dirPath = dir + OUTPUT_FILE_PATH;
				String fullPath = dir.getAbsolutePath() + OUTPUT_FILE_PATH
						+ mfileName;
				File dirFile = new File(dirPath);
				File outputFile = new File(fullPath);
				try {
					dirFile.mkdirs();
					outputFile.createNewFile();
				} catch (IOException e2) {
					e2.printStackTrace();
				}

				DataFileHeader header = new DataFileHeader();
				header.time = date.getTime();
				header.type = 0;
				header.length = data.length;

				try {
					FileOutputStream fos = new FileOutputStream(outputFile);
					fos.write(header.toByte());
					// fos.write(builder.toString().getBytes());
					fos.write(data, offset, length);
					fos.close();

					onFileSaved(outputFile);

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		thread.start();

	}

	protected void onFileSaved(File savedFile) {
		mSavedFileList.add(savedFile);
		if (mSavedFileList.size() > 2) {
			uploadSavedFiles();
		}
	}

	private void uploadSavedFiles() {
		for (int i = 0; i < mSavedFileList.size(); i++) {
			File file = mSavedFileList.get(i);
			WaveformUploadService.startUploadData(WaveformApplication
					.getInstance(), new UploadTask(file, mId, mName,
					mPhoneNumber));
			mSavedFileList.clear();
		}
	}

	/** format the input date into the form (yyyyMMddHHmmss) we want **/
	private static String generateFileName(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		return dateFormat.format(date);
	}

	public void setId(String id) {
		mId = id;
	}

	public void setName(String name) {
		mName = name;
	}

	public void setPhoneNumber(String phoneNumber) {
		mPhoneNumber = phoneNumber;
	}
}
