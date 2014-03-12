package net.dumetier.wikipedia;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import net.dumetier.utils.Constants;
import net.dumetier.utils.IOUtils;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

public class WikipediaFiltreOutput {

  private final String[] languages;
  private final String[] languagePrefix;
  private final String[] outputPrefix;
  private final Map<String, Writer> writersPerLang;
  private static final String MD5_ALGO = "MD5";
  private static final String INPUT_COLUMN_DELIMITER = " ";
  private static final char COLUMN_SEPARATOR = ',';
  private static final char LINE_SEPARATOR_LF = '\n';
  private static final char LINE_SEPARATOR_CR = '\r';
  private static final char SPACE = ' ';
  private static final char UNDERSCORE = '_';
  private static final char CSV_QUOTE = '\"';
  private static final char[] BAD_CHARACTERS = { COLUMN_SEPARATOR, CSV_QUOTE,
      LINE_SEPARATOR_CR, LINE_SEPARATOR_LF, SPACE, '\u0000', '\u0001',
      '\u0002', '\u0003', '\u0004', '\u0005', '\u0006', '\u0007', '\u0008',
      '\u0009', '\u000B', '\u000C', '\u000E', '\u000F', '\u0010', '\u0011',
      '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017', '\u0018',
      '\u0019', '\u001A', '\u001B', '\u001C', '\u001D', '\u001E', '\u001F' };

  private static final String FORMAT_UNICODE = "%04x";

  private static final String[] IGNORED_SPECIALS = new String[] { "File:",
      "Page:", "Special:", "Spécial:", "Talk:", "Template:", "Topic:",
      "User%20talk:", "User:", "Image%", "Image:", "Category:", "category:",
      "Fichier:", "fichier:", "Modèle:", "Utilisateur:", "user:", "cache/",
      "Catégorie:", "Discussion:", "/Talk:", "/Template_talk:",
      "/Wikipedia_talk:", " ", "User_talk:", "Wikipedia_talk:", "Wikipedia:",
      "Wikibooks:", "Projet:", "Discussion_", "Discuter:", ":Catégorie:",
      "wiki/", "Wiki/" };

  /*-
  private static final char[] CSV_CHARACTERS = { LINE_SEPARATOR_LF,
      LINE_SEPARATOR_CR, CSV_QUOTE, COLUMN_SEPARATOR };
   */

  static {
    Arrays.sort(BAD_CHARACTERS);
    Arrays.sort(IGNORED_SPECIALS);
  }

  public WikipediaFiltreOutput(String... languageFilters) {
    if (languageFilters == null || languageFilters.length == 0) {
      throw new IllegalArgumentException(
          "La langue de filtrage est mal renseignée.");
    }
    this.languages = languageFilters;
    this.languagePrefix = new String[languageFilters.length];
    this.outputPrefix = new String[languageFilters.length];
    System.arraycopy(languageFilters, 0, this.languagePrefix, 0,
        languageFilters.length);
    System.arraycopy(languageFilters, 0, this.outputPrefix, 0,
        languageFilters.length);
    for (int i = 0; i < languagePrefix.length; i++) {
      languagePrefix[i] += " ";
      outputPrefix[i] += "-";
    }
    this.writersPerLang = new HashMap<String, Writer>();
  }

  private String getMD5Hash(String lang, String url) throws IOException,
      NoSuchAlgorithmException {
    String toHash = new StringBuilder().append(lang).append(COLUMN_SEPARATOR)
        .append(url).toString();
    toHash = safeToLowercase(toHash);

    byte[] bytesOfMessage = toHash.getBytes(Constants.UTF_8);

    MessageDigest md = MessageDigest.getInstance(MD5_ALGO);
    byte[] thedigest = md.digest(bytesOfMessage);
    String suffix = new BigInteger(1, thedigest).toString(16);
    StringBuilder output = new StringBuilder(36);
    if (url.length() > 0) {
      output.append(String.format(FORMAT_UNICODE, (int) url.charAt(0)));
    }
    output.append(suffix);
    return output.toString();
  }

  private String safeToLowercase(String input) {
    StringBuilder out = new StringBuilder(input.length());
    for (int i = 0; i < input.length(); i++) {
      char character = input.charAt(i);
      if (Character.isLowerCase(character)) {
        out.append(character);
      } else {
        char characterInLowerCase = Character.toLowerCase(character);
        char characterBackToUppercase = Character
            .toUpperCase(characterInLowerCase);
        if (characterBackToUppercase == character) {
          out.append(characterInLowerCase);
        } else {
          out.append(character);
        }
      }
    }
    return out.toString();
  }

  private boolean shouldIgnore(String url) throws IOException {
    boolean response = false;
    for (int i = 0; i < IGNORED_SPECIALS.length; i++) {
      String toIgnore = IGNORED_SPECIALS[i];
      if (url.startsWith(toIgnore)) {
        return true;
      }
    }
    if (isBlank(url)) {
      return true;
    }
    if (url.length() < 2) {
      response = url.contains("\\");
      if (!response) {

      }
    }
    return response;
  }

  private String startsWith(String line) {
    for (int i = 0; i < languagePrefix.length; i++) {
      if (line.startsWith(languagePrefix[i])) {
        return languages[i];
      }
    }
    return null;
  }

  public List<File> filter(File compressedfile, String outputDirectory,
      int skipLines, String date) throws IOException, NoSuchAlgorithmException {
    BufferedReader reader = null;
    List<File> outputFiles = new ArrayList<File>(languages.length);

    try {
      if (!outputDirectory.endsWith(File.separator)) {
        outputDirectory += File.separator;
      }
      if (compressedfile.getName().contains(".gz")) {
        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(
            new BufferedInputStream(new FileInputStream(compressedfile),
                Constants.BUFFER_SIZE)), Constants.UTF_8),
            Constants.BUFFER_SIZE);
      } else if (compressedfile.getName().contains(".bz2")) {
        reader = new BufferedReader(new InputStreamReader(
            new BZip2CompressorInputStream(new BufferedInputStream(
                new FileInputStream(compressedfile), Constants.BUFFER_SIZE),
                true), Constants.UTF_8), Constants.BUFFER_SIZE);
      } else {
        throw new RuntimeException(
            "Le format de compression n'a pas été reconnu (Formats accepté .gz ou .bz2).");
      }

      for (int i = 0; i < outputPrefix.length; i++) {
        String prefix = outputPrefix[i];
        File outputFile = new File(outputDirectory + prefix
            + compressedfile.getName() + ".txt");
        outputFiles.add(outputFile);
        writersPerLang.put(languages[i], new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(outputFile),
                Constants.UTF_8)));

      }

      String lang = null;
      String previousLang = null;
      Writer writer = null;
      String line = null;
      long lineRead = 0;
      while ((line = reader.readLine()) != null) {
        lineRead++;
        if (lineRead <= skipLines) {
          continue;
        }
        if ((lineRead % 100000) == 0) {
          System.out.println("Lignes lues : "
              + lineRead
              + " "
              + DateFormat.getDateTimeInstance(DateFormat.LONG,
                  DateFormat.LONG, Locale.FRANCE).format(new Date()));
        }

        if ((lang = startsWith(line)) != null) {
          StringTokenizer tokenizer = new StringTokenizer(line,
              INPUT_COLUMN_DELIMITER);
          // 1- On saute le premier élément qui contient la langue
          if (!tokenizer.hasMoreTokens()) {
            continue;
          }
          tokenizer.nextToken();

          // 2- URL
          if (!tokenizer.hasMoreTokens()) {
            continue;
          }
          String url = tokenizer.nextToken();
          // 3- nbVisites
          if (!tokenizer.hasMoreTokens()) {
            continue;
          }
          String decodedUrl = URLDecoderSafer.decode(url);
          if (!shouldIgnore(decodedUrl)) {
            String nbVisite = tokenizer.nextToken();
            decodedUrl = cleanURL(decodedUrl);
            // decodedUrl = escapeCSV(decodedUrl);

            // On fait ici un test d'égalité au lieu d'un equals pour gagner du
            // temps
            if (lang != previousLang) {
              previousLang = lang;
              writer = writersPerLang.get(lang);
            }

            StringBuilder outputLine = new StringBuilder();
            outputLine.append(date);
            outputLine.append(COLUMN_SEPARATOR);
            outputLine.append(decodedUrl);
            outputLine.append(COLUMN_SEPARATOR);
            // 30002a4c6c33e73894fb1fb130955ffdecb6
            String hash = getMD5Hash(lang, decodedUrl);
            outputLine.append(hash);
            outputLine.append(COLUMN_SEPARATOR);
            outputLine.append(nbVisite);
            outputLine.append(LINE_SEPARATOR_LF);
            writer.write(outputLine.toString());
          }
        }
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeWritersSafely(writersPerLang.values().toArray(
          new Writer[writersPerLang.values().size()]));
      if (reader != null) {
        reader.close();
      }
    }

    return outputFiles;
  }

  private String cleanURL(String decodedUrl) {
    for (int i = 0; i < decodedUrl.length(); i++) {
      char character = decodedUrl.charAt(i);
      int pos = Arrays.binarySearch(BAD_CHARACTERS, character);
      if (pos < 0 || BAD_CHARACTERS[pos] != character) {
        continue;
      }
      decodedUrl = decodedUrl.replace(character, UNDERSCORE);
    }
    return decodedUrl;
  }

  public static boolean isBlank(String str) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
      return true;
    }
    for (int i = 0; i < strLen; i++) {
      if ((Character.isWhitespace(str.charAt(i)) == false)) {
        return false;
      }
    }
    return true;
  }
  /*-
   private String escapeCSV(String decodedURL) {
   StringWriter out = new StringWriter();
   if (containsNone(decodedURL, CSV_CHARACTERS)) {
   if (decodedURL != null) {
   out.write(decodedURL);
   }
   return out.toString();
   }
   out.write(CSV_QUOTE);
   for (int i = 0; i < decodedURL.length(); i++) {
   char c = decodedURL.charAt(i);
   if (c == CSV_QUOTE) {
   out.write(CSV_QUOTE); // escape double quote
   }
   out.write(c);
   }
   out.write(CSV_QUOTE);
   return out.toString();
   }

   public static boolean containsNone(String str, char[] invalidChars) {
   if (str == null || invalidChars == null) {
   return true;
   }
   int strSize = str.length();
   int validSize = invalidChars.length;
   for (int i = 0; i < strSize; i++) {
   char ch = str.charAt(i);
   for (int j = 0; j < validSize; j++) {
   if (invalidChars[j] == ch) {
   return false;
   }
   }
   }
   return true;
   }
   */
}
