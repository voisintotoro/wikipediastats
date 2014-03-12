package net.dumetier.utils;

import java.io.IOException;
import java.io.Writer;

public class IOUtils {
  public static void closeWritersSafely(Writer[] writers) {
    for (Writer writer : writers) {
      try {
        if (writer != null) {
          writer.flush();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        try {
          if (writer != null) {
            writer.close();
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
