package net.dumetier;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.dumetier.wikipedia.WikipediaArchiveDownloader;

public class Downloader {

	private static final String DATE_FORMAT = "yyyyMMdd'T'HHmmss";
  /**
   * @param args
   * @throws ParseException
   */
  public static void main(String[] args) throws ParseException {
    if (args == null || args.length != 3) {
      System.out
.println("Usage : java -jar xxxx.jar outputdirectory from<yyyymmddThhmmss> to<yyyymmddThhmmss> ");
      return;
    }
    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.FINEST);
    new Downloader().doDownload(args[1], args[2], args[0]);
  }

  public void doDownload(String from, String to, String outputDirectory)
      throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		Date dateFrom = dateFormat.parse(from);
		Date dateTo = dateFormat.parse(to);
    new WikipediaArchiveDownloader(outputDirectory).downloadAllBetween(
        dateFrom, dateTo);

  }

}
