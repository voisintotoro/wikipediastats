package net.dumetier.utils;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.dumetier.wikipedia.FileInfo;

public class JDKURLFileRetriever {

	private static Logger logger = Logger.getLogger(JDKURLFileRetriever.class.getCanonicalName());

	public FileInfo retrieve(URL url) throws FileNotFoundException {

		BufferedInputStream in = null;
		int contentLength = -1;
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			contentLength = conn.getContentLength();

			in = new BufferedInputStream(url.openStream());
		} catch (IOException e) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine(String.format("Une erreur est survenue lors de la récupération du fichier %s.", url.toExternalForm()) + "; "
						+ e.getMessage());
			}
			throw new FileNotFoundException(String.format("Impossible de récupérer le fichier %s.", url.toExternalForm()));
		}
		return new FileInfo(in, contentLength, new ConnectionWrapper(conn));
	}

	private static class ConnectionWrapper implements Closeable {

		private final HttpURLConnection conn;

		public ConnectionWrapper(HttpURLConnection conn) {
			this.conn = conn;
		}

		@Override
		public void close() throws IOException {
			conn.disconnect();
		}

	}
}
