package net.dumetier.wikipedia;

import java.io.Closeable;
import java.io.InputStream;

public class FileInfo {

	private final InputStream inputStream;
	private final long contentLength;
	private final Closeable connection;

	public FileInfo(InputStream inputStream, long contentLength, Closeable connection) {
		super();
		this.inputStream = inputStream;
		this.contentLength = contentLength;
		this.connection = connection;
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

	public Closeable getConnection() {
		return connection;
	}

}
