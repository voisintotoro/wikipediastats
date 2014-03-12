package net.dumetier.wikipedia;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.dumetier.utils.Constants;

import com.google.code.externalsorting.ExternalSort;

public class WikipediaSortOutput {

  private static final String SORTED_FILES_SUFFIX = ".sorted.csv";

  public List<File> sortFile(List<File> files, final int startComparatorAt)
      throws IOException {
    // External Sort
    Comparator<String> comparator = new Comparator<String>() {
      @Override
      public int compare(String r1, String r2) {
        if (r1.length() > startComparatorAt && r2.length() > startComparatorAt) {
          String r1t = r1.substring(startComparatorAt);
          String r2t = r2.substring(startComparatorAt);
          return String.CASE_INSENSITIVE_ORDER.compare(r1t, r2t);
        } else {
          return r1.compareTo(r2);
        }
      }
    };
    List<File> sortedFiles = new ArrayList<File>(files.size());
    for (File file : files) {
      List<File> l = ExternalSort.sortInBatch(file, comparator, 512,
          Charset.forName(Constants.UTF_8), null);
      File outputSortedFile = new File(file.getCanonicalPath()
          + SORTED_FILES_SUFFIX);
      sortedFiles.add(outputSortedFile);
      ExternalSort.mergeSortedFiles(l, outputSortedFile, comparator,
          Charset.forName(Constants.UTF_8));
    }
    return sortedFiles;
  }

}
