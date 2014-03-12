package net.dumetier;

import java.util.Date;

import net.dumetier.wikipedia.WikipediaArchiveDownloader;

public class WikipediaArchiveDownloaderTester {

  public static void main(String[] args) {
    new WikipediaArchiveDownloaderTester().testDownload();
  }

  public void testDownload() {
    WikipediaArchiveDownloader wikipediaArchiveDownloader = new WikipediaArchiveDownloader(
        "/temp/");
    wikipediaArchiveDownloader.downloadAllBetween(new Date(new Date().getTime()
        - (5 * 60 * 60 * 1000)), new Date(new Date().getTime()
        - (3 * 60 * 60 * 1000)));
  }
}
