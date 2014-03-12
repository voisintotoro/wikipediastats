package net.dumetier;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.dumetier.wikipedia.WikipediaArchiveDownloader;

public class Downloader {

  /**
   * @param args
   * @throws ParseException
   */
  public static void main(String[] args) throws ParseException {
    if (args == null || args.length != 3) {
      System.out
          .println("Usage : java -jar xxxx.jar outputdirectory from<yyyymmjjThhmmss> to<yyyymmjjThhmmss> ");
      return;
    }
    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.FINEST);
    new Downloader().doDownload(args[1], args[2], args[0]);
  }

  public void doDownload(String from, String to, String outputDirectory)
      throws ParseException {
    Date dateFrom = DateFormat.getDateTimeInstance(DateFormat.SHORT,
        DateFormat.SHORT, Locale.FRANCE).parse(from);
    Date dateTo = DateFormat.getDateTimeInstance(DateFormat.SHORT,
        DateFormat.SHORT, Locale.FRANCE).parse(to);
    new WikipediaArchiveDownloader(outputDirectory).downloadAllBetween(
        dateFrom, dateTo);

  }

}
