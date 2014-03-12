package net.dumetier.utils;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.dumetier.wikipedia.FileInfo;

public class JDKURLFileRetriever {

  private static Logger logger = Logger.getLogger(JDKURLFileRetriever.class
      .getCanonicalName());

  public FileInfo retrieve(URL url) throws FileNotFoundException {

    BufferedInputStream in = null;
    int contentLength = -1;
    try {
      contentLength = url.openConnection().getContentLength();
      in = new BufferedInputStream(url.openStream());
    } catch (IOException e) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine(String.format(
            "Une erreur est survenue lors de la récupération du fichier %s.",
            url.toExternalForm())
            + "; " + e.getMessage());
      }
      throw new FileNotFoundException(String.format(
          "Impossible de récupérer le fichier %s.", url.toExternalForm()));
    }
    return new FileInfo(in, contentLength);
  }
}
