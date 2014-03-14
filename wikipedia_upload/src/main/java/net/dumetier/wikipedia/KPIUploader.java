package net.dumetier.wikipedia;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

public class KPIUploader {
	public void doStuff() {
		URLConnection urlconnection = null;
		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;
		try {
			File file = new File("C:/test.txt");
			URL url = new URL("http://192.168.5.27/Test/test.txt");
			urlconnection = url.openConnection();
			urlconnection.setDoOutput(true);
			urlconnection.setDoInput(true);

			if (urlconnection instanceof HttpURLConnection) {
				try {
					((HttpURLConnection) urlconnection).setRequestMethod("PUT");
					((HttpURLConnection) urlconnection).setRequestProperty("Content-type", "text/html");
					((HttpURLConnection) urlconnection).connect();

				} catch (ProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			bos = new BufferedOutputStream(urlconnection.getOutputStream());
			bis = new BufferedInputStream(new FileInputStream(file));
			int i;
			// read byte by byte until end of stream
			while ((i = bis.read()) > 0) {
				bos.write(i);
			}
			System.out.println(((HttpURLConnection) urlconnection).getResponseMessage());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bis != null) {
					bis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (bos != null) {
						bos.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			InputStream inputStream;
			int responseCode = ((HttpURLConnection) urlconnection).getResponseCode();
			if ((responseCode >= 200) && (responseCode <= 202)) {
				inputStream = ((HttpURLConnection) urlconnection).getInputStream();
				int j;
				while ((j = inputStream.read()) >= 0) {
					System.out.println(j);
				}

			} else {
				inputStream = ((HttpURLConnection) urlconnection).getErrorStream();
			}
			((HttpURLConnection) urlconnection).disconnect();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
