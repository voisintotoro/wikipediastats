package net.dumetier.wikipedia;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.dumetier.utils.JDKURLFileRetriever;

public class WikipediaArchiveDownloader {

	private static final Logger logger = Logger.getLogger(WikipediaArchiveDownloader.class.getCanonicalName());
	public static final String FILENAME_PATTERN = "pagecounts-%1$tY%1$tm%1$td-%1$tH00%1$tS.gz";

	private final File outputDirectory;

	public WikipediaArchiveDownloader(String outputDirectory) {

		this.outputDirectory = new File(outputDirectory);
		if (!this.outputDirectory.isDirectory()) {
			throw new RuntimeException("The outputdirectory is not a directory");
		}
	}

	public List<URL> getAlreadyDownloadedFile() {
		return new ArrayList<URL>();
	}

	public List<URL> getMissingFiles() {
		return new ArrayList<URL>();
	}

	public void downloadAllBetween(Date from, Date to) {
		long resteFrom = from.getTime() % (60L * 60L * 1000L);
		long resteTo = to.getTime() % (60L * 60L * 1000L);
		from = new Date(from.getTime() - resteFrom);
		to = new Date(to.getTime() - resteTo);

		System.out.println("from:" + from);
		System.out.println("to:" + to);

		WikipediaArchiveURLCreator urlCreator = new WikipediaArchiveURLCreator();
		for (Date current = from; current.getTime() < to.getTime(); current = new Date(current.getTime() + (60 * 60 * 1000))) {
			System.out.println(current);
			try {
				doNothing();
				doDownloadAndStore(current, urlCreator);
			} catch (IOException e) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Le fichier n'a pas été trouvé : " + e.getMessage());
				}
				// Parfois l'horodatage est décalé d'une seconde
				try {
					doDownloadAndStore(new Date(current.getTime() + (1 * 1000)), urlCreator);
				} catch (IOException e2) {
					if (logger.isLoggable(Level.FINE)) {
						logger.fine("Le fichier décallé d'une seconde n'a pas été trouvé : " + e2.getMessage());
					}
				}
			}
		}
	}

	private void doNothing() throws FileNotFoundException {
	}

	private void doDownloadAndStore(Date current, WikipediaArchiveURLCreator urlCreator) throws IOException {
		URL url = urlCreator.createURL(current);
		String fileName = String.format(FILENAME_PATTERN, current);
		downloadAndStoreAs(url, fileName);

	}

	public void downloadAndStoreAs(URL url, String fileName) throws IOException {
		FileInfo fileInfo = new JDKURLFileRetriever().retrieve(url);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("Taille fichier en octets : " + fileInfo.getContentLength() + " ; en Mo" + fileInfo.getContentLength()
					/ (1024L * 1024));
		}
		long byteCopied = -1;
		try {
			byteCopied = new CopyStreamToDisk(outputDirectory).processInputStream(fileInfo.getInputStream(), fileName);
		} finally {
			fileInfo.getConnection().close();
		}
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("Copie complete ?  " + (fileInfo.getContentLength() == byteCopied));
			logger.finer("Taille sauvegardée en octets : " + fileInfo.getContentLength() + " ; en Mo" + fileInfo.getContentLength()
					/ (1024L * 1024));
		}

	}
}
