package com.larc.waveform.fileupload;

import java.io.File;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.larc.waveform.R;

public class UploadActivity extends Activity {
	private EditText filenameText;
	private TextView resulView;
	private ProgressBar uploadbar;
	private UploadLogService logService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		logService = new UploadLogService(this);
//		filenameText = (EditText) this.findViewById(R.id.filename);
//		uploadbar = (ProgressBar) this.findViewById(R.id.uploadbar);
//		resulView = (TextView) this.findViewById(R.id.result);
//		Button button = (Button) this.findViewById(R.id.button);
//		button.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
				String filename = filenameText.getText().toString();
				// Check if SDCard is exist
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					// get SDCard directory
					File uploadFile = new File(Environment
							.getExternalStorageDirectory(), filename);
					if (uploadFile.exists()) {
						uploadFile(uploadFile);
					} else {
						Toast.makeText(UploadActivity.this,
								R.string.filenotexsit, 1).show();
					}
				} else {
					Toast.makeText(UploadActivity.this, R.string.sdcarderror, 1)
							.show();
				}
//			}
//		});
	}

	/**
	 * user Handler to send message to the Thread that creats this Handler.
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// get uploading progress report
			int length = msg.getData().getInt("size");
			uploadbar.setProgress(length);
			float num = (float) uploadbar.getProgress()
					/ (float) uploadbar.getMax();
			int result = (int) (num * 100);
			// set report result
			resulView.setText(result + "%");
			// uploading success
			if (uploadbar.getProgress() == uploadbar.getMax()) {
				Toast.makeText(UploadActivity.this, R.string.success, 1).show();
			}
		}
	};

	/**
	 * Create a Threat to uploading files.
	 * use Handler to avoid UI Thread ANR error.
	 * 
	 * @param final uploadFile
	 */
	private void uploadFile(final File uploadFile) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// set maximum length of files
					uploadbar.setMax((int) uploadFile.length());
					// Check if file has been uploaded before
					String souceid = logService.getBindId(uploadFile);
					// set header
					String head = "Content-Length=" + uploadFile.length()
							+ ";filename=" + uploadFile.getName()
							+ ";sourceid=" + (souceid == null ? "" : souceid)
							+ "/r/n";
					// Create socket an IOstream
					Socket socket = new Socket("192.168.1.100", 7878);
					OutputStream outStream = socket.getOutputStream();
					outStream.write(head.getBytes());

					PushbackInputStream inStream = new PushbackInputStream(
							socket.getInputStream());
					// get id and position of byte[]
					String response = StreamTool.readLine(inStream);
					String[] items = response.split(";");
					String responseid = items[0].substring(items[0]
							.indexOf("=") + 1);
					String position = items[1].substring(items[1].indexOf("=") + 1);
					// if file has not been uploaded before , create a bindID in database
					if (souceid == null) {
						logService.save(responseid, uploadFile);
					}
					RandomAccessFile fileOutStream = new RandomAccessFile(
							uploadFile, "r");
					fileOutStream.seek(Integer.valueOf(position));
					byte[] buffer = new byte[1024];
					int len = -1;
					// initialize ªø¶Ç data length 
					int length = Integer.valueOf(position);
					while ((len = fileOutStream.read(buffer)) != -1) {
						outStream.write(buffer, 0, len);
						// set data length
						length += len;
						Message msg = new Message();
						msg.getData().putInt("size", length);
						handler.sendMessage(msg);
					}
					fileOutStream.close();
					outStream.close();
					inStream.close();
					socket.close();
					// delete data after uploading has done
					if (length == uploadFile.length())
						logService.delete(uploadFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}