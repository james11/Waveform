package com.larc.waveform.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import android.os.Environment;

public class ReceivedDataSaver {
	private static final String FILE_PATH = "/Larc/Waveform/";
	public static File mOutputFile;
	public static int mFileCount = 0;
	public static File[] mFile;

	public static class DataHeader {

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

	public static void saveData(final byte[] inputData, final int offset,
			final int length) {
		mFileCount = +1;
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
				String dirPath = dir + FILE_PATH;
				String fullPath = dir.getAbsolutePath() + FILE_PATH + mfileName;
				File dirFile = new File(dirPath);
				mOutputFile = new File(fullPath);
				try {
					dirFile.mkdirs();
					mOutputFile.createNewFile();
				} catch (IOException e2) {
					e2.printStackTrace();
				}

				DataHeader header = new DataHeader();
				header.time = date.getTime();
				header.type = 0;
				header.length = data.length;

				try {
					FileOutputStream fos = new FileOutputStream(mOutputFile);
					fos.write(header.toByte());
					// fos.write(builder.toString().getBytes());
					fos.write(data, offset, length);
					fos.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
		listFile();

	}

	public static void listFile() {
		mFile[mFileCount] = mOutputFile;
	}

	public File getFile(int i) {
		return mFile[i];
	}

	public int getFileCount() {
		return mFileCount;
	}

//	public boolean CheckUpload() {
//		if (mFileCount >= 10) {
//			return true;
//		} else {
//			return false;
//		}
//	}

	/** format the input date into the form (yyyyMMddHHmmss) we want **/
	private static String generateFileName(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		return dateFormat.format(date);
	}
}
