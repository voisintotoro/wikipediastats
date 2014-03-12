package net.dumetier.wikipedia;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import net.dumetier.utils.Constants;
import net.dumetier.utils.IOUtils;

public class WikipediaAggregateGZIPOutput {

  private static final String EMPTY_STRING = "";
  private static final char COLUMN_SEPARATOR = ',';
  private static final String COLUMN_SEPARATOR_AS_STRING = String
      .valueOf(COLUMN_SEPARATOR);
  private static final char LINE_SEPARATOR = '\n';
  private static final String GZ_SUFFIX = ".gz";

  public WikipediaAggregateGZIPOutput(String... languageFilters) {
  }

  public List<File> aggregationStats(List<File> files) throws IOException {
    // Aggrégation des stats
    BufferedReader reader = null;
    List<File> aggregatesFiles = new ArrayList<File>(files.size());
    for (File file : files) {
      Writer writer = null;
      try {
        File outputGz = new File(file.getCanonicalPath() + GZ_SUFFIX);
        writer = new OutputStreamWriter(new GZIPOutputStream(
            new BufferedOutputStream(new FileOutputStream(outputGz),
                Constants.BUFFER_SIZE), Constants.BUFFER_SIZE) {
          {
            def.setLevel(Deflater.BEST_COMPRESSION);
          }
        }, Constants.UTF_8);
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(
            file), Constants.UTF_8), Constants.BUFFER_SIZE);
        aggregatesFiles.add(outputGz);

        String line = null;
        String previousLine = EMPTY_STRING;
        String previousHash = EMPTY_STRING;
        int previousVisites = 0;
        String previousUrl = EMPTY_STRING;
        while ((line = reader.readLine()) != null) {
          StringTokenizer tokenizer = new StringTokenizer(line,
              COLUMN_SEPARATOR_AS_STRING);
          // 1- On saute la date
          if (!tokenizer.hasMoreTokens()) {
            continue;
          }
          tokenizer.nextToken();
          // 2- URL
          if (!tokenizer.hasMoreTokens()) {
            continue;
          }
          String url = tokenizer.nextToken();

          // 3- ID
          if (!tokenizer.hasMoreTokens()) {
            continue;
          }
          String hash = tokenizer.nextToken();

          // 4- NbVisites
          if (!tokenizer.hasMoreTokens()) {
            continue;
          }
          int nbVisites = Integer.parseInt(tokenizer.nextToken());
          if (previousUrl.equalsIgnoreCase(url)) {
            // La casse est différente (car les hash sont différents)
            if (!hash.equals(previousHash)) {
              // L'orthographe de la ligne courante a plus de visite on
              // préfère donc cet orthographe
              if (nbVisites > previousVisites) {
                previousLine = line;
              } else if (nbVisites == previousVisites) {
                for (int i = 0; i < previousUrl.length(); i++) {
                  if (url.charAt(i) != previousUrl.charAt(i)) {
                    if (Character.isUpperCase(url.charAt(i))) {
                      // On préfère la version avec des majuscules
                      previousLine = line;
                    }
                  }
                }
              }
            }
            previousVisites += nbVisites;

            // On met à jour la valeur des visite de la ligne précédente
            int lastSeparatorPosition = previousLine
                .lastIndexOf(COLUMN_SEPARATOR);
            previousLine = previousLine.substring(0, lastSeparatorPosition + 1);
            previousLine += previousVisites;
          } else {
            // On fait un test d'égalité au lieu de equals pour aller plus vite
            if (previousLine != EMPTY_STRING) {
              writer.write(previousLine);
              writer.write(LINE_SEPARATOR);
            }
            previousLine = line;
          }
          previousVisites = nbVisites;
          previousHash = hash;
          previousUrl = url;
        }
        writer.write(previousLine);
        writer.write(LINE_SEPARATOR);
      } finally {
        try {
          if (reader != null) {
            reader.close();
          }
        } finally {
          IOUtils.closeWritersSafely(new Writer[] { writer });
        }
      }
    }
    return aggregatesFiles;
  }

}
