package net.dumetier.wikipedia;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;

public class WikipediaTransform {
  private static final String T_DATE_DELIMITER = "T";
  private static final String DATE_SUFFIX = "0000.000Z";

  private final String[] languageFilters;

  public WikipediaTransform(String... languageFilters) {
    this.languageFilters = languageFilters;
  }

  public void transform(File gzfile, String outputDirectory)
      throws NoSuchAlgorithmException, IOException {
    // Format de la date
    String date = getDateAsString(gzfile.getName());
    int skipLines = evaluateSkipLines(date);

    // Filtre
    WikipediaFiltreOutput wikipediaFiltreOutput = new WikipediaFiltreOutput(
        languageFilters);
    List<File> filteredFiles = wikipediaFiltreOutput.filter(gzfile,
        outputDirectory, skipLines, date);
    wikipediaFiltreOutput = null;

    // Sort
    List<File> sortedFiles = new WikipediaSortOutput().sortFile(filteredFiles,
        (date.length() + 1));
    deleteFiles(filteredFiles);

    // Gzip
    // List<File> gzippedFiles =
    new WikipediaAggregateGZIPOutput().aggregationStats(sortedFiles);
    deleteFiles(sortedFiles);
  }

  private int evaluateSkipLines(String date) {
    int response = 0;

    if (date.length() == 8) {
      // Format de date pagecounts-2011-08-all_ge5 donc 20110801; il faut sauter
      // l'entête de 32 lignes
      response = 32;
    }
    return response;
  }

  private void deleteFiles(Collection<File> files) {
    for (File file : files) {
      file.delete();
    }
  }

  private String getDateAsString(String fileName) {
    StringBuilder date;
    if (fileName.length() > 15 && fileName.charAt(15) == '-') {
      // Format de date pagecounts-2011-08-all_ge5
      date = new StringBuilder(8);
      date.append(fileName.substring(11, 15));
      date.append(fileName.substring(16, 18));
      date.append("01");
    } else {
      // Format de date pagecounts-20110101-080000.gz
      date = new StringBuilder(20);
      date.append(fileName.substring(11, 22));
      date.replace(8, 9, T_DATE_DELIMITER);
      date.append(DATE_SUFFIX);
    }
    return date.toString();
  }
}
