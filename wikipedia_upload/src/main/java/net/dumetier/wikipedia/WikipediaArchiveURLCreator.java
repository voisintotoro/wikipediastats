package net.dumetier.wikipedia;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class WikipediaArchiveURLCreator {
  /*
   * Example :
   * http://dumps.wikimedia.org/other/pagecounts-raw/2011/2011-01/pagecounts
   * -20110101-000000.gz
   */
  private final String urlPattern = "http://dumps.wikimedia.org/other/pagecounts-raw/%1$tY/%1$tY-%1$tm/"
      + WikipediaArchiveDownloader.FILENAME_PATTERN;

  public URL createURL(Date date) {
    try {
      return new URL(String.format(urlPattern, date));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
