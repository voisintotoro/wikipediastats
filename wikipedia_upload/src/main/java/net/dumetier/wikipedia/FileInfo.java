package net.dumetier.wikipedia;

import java.io.InputStream;

public class FileInfo {

	private final InputStream inputStream;
	private final long contentLength;

	public FileInfo(InputStream inputStream, long contentLength) {
		super();
		this.inputStream = inputStream;
		this.contentLength = contentLength;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * Return the file size in bytes
	 * 
	 * @return -1 when file size is unknown
	 */
	public long getContentLength() {
		return contentLength;
	}

}
