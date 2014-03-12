package net.dumetier;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.dumetier.wikipedia.WikipediaTransform;

public class Filter {

  /**
   * @param args
   * @throws ParseException
   */
  public static void main(String[] args) throws ParseException {
    if (args == null || args.length < 3) {
      System.out
          .println("Usage : java -jar xxxx.jar inputFile outputdirectory <lang1> <lang2> <langN>");
      return;
    }
    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.FINEST);
    String[] langs = new String[args.length - 2];
    System.arraycopy(args, 2, langs, 0, args.length - 2);
    new Filter().doFilter(args[0], args[1], langs);
  }

  public void doFilter(String pathToInputFile, String outputDirectory,
      String... langs) throws ParseException {
    File inputFile = new File(pathToInputFile);
    if (!inputFile.exists()) {
      throw new IllegalArgumentException("Le fichier en entrée n existe pas : "
          + pathToInputFile);
    }
    List<File> filesToProcess = new ArrayList<File>();
    if (inputFile.isFile()) {
      filesToProcess.add(inputFile);
    }
    if (inputFile.isDirectory()) {
      File[] listOfFiles = inputFile.listFiles();
      filesToProcess.addAll(Arrays.asList(listOfFiles));
    }

    for (File file : filesToProcess) {
      try {
        new WikipediaTransform(langs).transform(file, outputDirectory);
        file.renameTo(new File(file.getCanonicalPath() + ".done"));
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }
}
