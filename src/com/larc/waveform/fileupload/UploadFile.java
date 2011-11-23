package com.larc.waveform.fileupload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class UploadFile {

	public String uploadFile(File file, String uri){
		String response = null;
		try {
			//initialize a new HTTP client
			//which means that it's a new session
			//For more details, google "HTTP session"
			DefaultHttpClient httpClient = new DefaultHttpClient();
			
			//setup the request command. we use HTTP post to upload a file
			//entity is the content of a HTTP request.
			/* * * Brief explanation of HTTP POST/GET * * *
			 * HTTP GET is a request without any content. 
			 * 	It's like sending a mail with a empty envelope(HTTP Header)
			 * HTTP POST is a request including contents(an entity).
			 * 	The contents can be various type, which is usually defined in header(content-type).
			 */
			HttpPost httpPost = new HttpPost(uri);
			FileEntity fileEntity = new FileEntity(file, "text/xml");
			httpPost.setEntity(fileEntity);
			
			HttpResponse httpResponse = httpClient.execute(httpPost);
			
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				response = httpResponse.getEntity().toString();
			} else {
				//other HTTP status. For example: 404 file not found
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
