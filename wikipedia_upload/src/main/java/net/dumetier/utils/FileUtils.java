package net.dumetier.utils;

import java.io.File;
import java.io.IOException;

public class FileUtils {
	/**
	 * The Unix separator character.
	 */
	private static final char UNIX_SEPARATOR = '/';

	/**
	 * The Windows separator character.
	 */
	private static final char WINDOWS_SEPARATOR = '\\';

	/**
	 * The extension separator character.
	 * 
	 * @since Commons IO 1.4
	 */
	public static final char EXTENSION_SEPARATOR = '.';

	/**
	 * The extension separator String.
	 * 
	 * @since Commons IO 1.4
	 */
	public static final String EXTENSION_SEPARATOR_STR = Character.toString(EXTENSION_SEPARATOR);

	public static boolean isWritableDirectory(File directory) {
		boolean response = false;
		if (directory != null) {
			if (directory.isAbsolute()) {
				if (directory.canWrite()) {
					response = true;
				}
			}
		}
		return response;
	}

	/**
	 * Try to rename a file by adding a number in parenthesis at the end of filename. Exemple : /mypath/myfile(1).txt or
	 * /mypath/myOtherFile(1) when no extension
	 * 
	 * @param fileNameWithFullPath
	 * @param maxTry the number of try be
	 * @return
	 */
	public static boolean renameTo(File fileNameWithFullPath, int maxTry, String suffixToRemove) {
		boolean response = false;
		String fileNamePattern = null;
		try {
			fileNamePattern = fileNameWithFullPath.getCanonicalPath();
		} catch (IOException e) {
			// no-op
		}
		if (fileNameWithFullPath == null || StringUtils.isBlank(fileNamePattern) || maxTry <= 0 || fileNameWithFullPath.isDirectory()) {
			return response;
		}
		if (suffixToRemove != null) {
			if (fileNamePattern.lastIndexOf(suffixToRemove) >= 0) {
				fileNamePattern = fileNamePattern.substring(0, fileNamePattern.lastIndexOf(suffixToRemove));
			}
		}

		String name = getBaseName(fileNamePattern);
		int position = fileNamePattern.lastIndexOf(name);

		int i = 1;
		File dest = createNewFileName(fileNamePattern, position, i);
		while (!fileNameWithFullPath.renameTo(dest) || i > maxTry) {
			i++;
			dest = createNewFileName(fileNamePattern, position, i);
		}
		return response;
	}

	private static File createNewFileName(String fileNamePattern, int position, int maxTry) {
		return new File(new StringBuilder(fileNamePattern).insert(position, new StringBuilder().append('(').append(maxTry).append(')'))
				.toString());
	}

	public static String getBaseName(String filename) {
		return removeExtension(getName(filename));
	}

	public static String removeExtension(String filename) {
		if (filename == null) {
			return null;
		}
		int index = indexOfExtension(filename);
		if (index == -1) {
			return filename;
		} else {
			return filename.substring(0, index);
		}
	}

	public static int indexOfExtension(String filename) {
		if (filename == null) {
			return -1;
		}
		int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
		int lastSeparator = indexOfLastSeparator(filename);
		return (lastSeparator > extensionPos ? -1 : extensionPos);
	}

	public static String getName(String filename) {
		if (filename == null) {
			return null;
		}
		int index = indexOfLastSeparator(filename);
		return filename.substring(index + 1);
	}

	public static int indexOfLastSeparator(String filename) {
		if (filename == null) {
			return -1;
		}
		int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
		int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
		return Math.max(lastUnixPos, lastWindowsPos);
	}
}
