package com.larc.bluetoothconnect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class ServerApi {

	private static final String SERVER_CHARSET = "BIG5";

	private static final String API_UPLOAD_URL = "http://140.114.14.54/phpBB3/ECGServer/PHPCode/upload_ok.php";
	private static final String API_UPLOAD_FILE = "ufile";
	private static final String API_UPLOAD_ID = "uid";
	private static final String API_UPLOAD_NAME = "uname";
	private static final String API_UPLOAD_ADDRESS = "uaddress";

	public static String uploadFile(String id, String name, String address,
			File file) {
		String response = null;
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();

			HttpPost httpPost = new HttpPost(API_UPLOAD_URL);

			MultipartEntity multipartEntity = new MultipartEntity();

			multipartEntity.addPart(API_UPLOAD_ID, new StringBody(id));
			multipartEntity.addPart(API_UPLOAD_NAME, new StringBody(name));
			multipartEntity
					.addPart(API_UPLOAD_ADDRESS, new StringBody(address));
			multipartEntity.addPart(API_UPLOAD_FILE, new FileBody(file));

			httpPost.setEntity(multipartEntity);

			HttpResponse httpResponse = httpClient.execute(httpPost);

			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

				response = EntityUtils.toString(httpResponse.getEntity(),
						SERVER_CHARSET);
			} else {
				// other HTTP status. For example: 404 file not found
			}

		} catch (FileNotFoundException e) {
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return response;
	}
}
