package net.dumetier.wikipedia;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import net.dumetier.utils.FileUtils;

public class CopyStreamToDisk {

  private final File outputDirectory;

  private static final String TEMP_SUFFIX = ".tmp";
  private static final int MAX_RENAMMING_RETRY = 500;
  /**
   * The default buffer size to use for
   * {@link #copyLarge(InputStream, OutputStream)} and
   * {@link #copyLarge(Reader, Writer)}
   */
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

  public CopyStreamToDisk(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public long processInputStream(InputStream in, String fileName) {

    File outputFile = new File(outputDirectory, new StringBuilder()
        .append(fileName).append(TEMP_SUFFIX).toString());
    try {
      outputFile.createNewFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    OutputStream out = null;
    long byteCopied = 0;
    try {
      out = new FileOutputStream(outputFile);
      byteCopied = copyLarge(in, out);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        if (out != null) {
          out.close();
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }

    String finalFileName = outputFile.getAbsolutePath();
    if (finalFileName.lastIndexOf(TEMP_SUFFIX) >= 0) {
      finalFileName = finalFileName.substring(0,
          finalFileName.lastIndexOf(TEMP_SUFFIX));
    }

    if (!outputFile.renameTo(new File(finalFileName))) {
      FileUtils.renameTo(outputFile, MAX_RENAMMING_RETRY, TEMP_SUFFIX);
    }
    return byteCopied;

  }

  private long copyLarge(InputStream input, OutputStream output)
      throws IOException {
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    long count = 0;
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
    }
    return count;
  }
}
