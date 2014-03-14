package net.dumetier.wikipedia;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import net.dumetier.utils.StringUtils;

public class URLDecoderSafer {

	private static final String UTF_8 = "UTF-8";
	private static final char[] SPECIALS_CHARS_AS_CHAR = new char[] { ' ', '!', '"', '#', '$', '&', '\'', '(', ')', '*', '+', ',', '-',
			'.', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~' };
	private static String[] SPECIALS_CHARS;
	private static final String[] SPECIALS_CHARS_REPLACEMENT = new String[] { "%20", "%21", "%22", "%23", "%24", "%26", "%27", "%28",
			"%29", "%2A", "%2B", "%2C", "%2D", "%2E", "%2F", "%3A", "%3B", "%3C", "%3D", "%3E", "%3F", "%40", "%5B", "%5C", "%5D", "%5E",
			"%5F", "%60", "%7B", "%7C", "%7D", "%7E" };

	static {
		SPECIALS_CHARS = new String[SPECIALS_CHARS_AS_CHAR.length];
		for (int i = 0; i < SPECIALS_CHARS_AS_CHAR.length; i++) {
			SPECIALS_CHARS[i] = String.valueOf(SPECIALS_CHARS_AS_CHAR[i]);
		}
	}

	public static String decode(String url) throws UnsupportedEncodingException {
		String response = url;
		if (url != null) {
			String tmpUrl = url;
			try {
				tmpUrl = URLDecoder.decode(url, UTF_8);
			} catch (Exception e) {

				if (StringUtils.containsAny(tmpUrl, SPECIALS_CHARS_AS_CHAR)) {
					tmpUrl = StringUtils.replaceEach(tmpUrl, SPECIALS_CHARS, SPECIALS_CHARS_REPLACEMENT);
				}
				// TODO contrôler que l'URL ne contient pas des caractères unicode
				// encodé ainsi %u0254 à transformer en %02%54
				tmpUrl = encodeJavascriptUnicode(tmpUrl);

				// On encode les caractere % qui sont seuls
				tmpUrl = encodePercent(tmpUrl);

				try {
					tmpUrl = URLDecoder.decode(tmpUrl, UTF_8);
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (UnsupportedEncodingException e2) {
					// On soulève bien la première erreur.
					throw e2;
				}
			}
			response = tmpUrl;
		}
		return response;
	}

	/**
	 * Décode les %uxxxx en unicode
	 * 
	 * @param url
	 * @return
	 */
	private static String encodeJavascriptUnicode(String url) {
		String response = url;
		if (url.length() >= 6) {
			StringBuilder fixedUrl = new StringBuilder(url.length());
			int i = 0;
			for (; i <= url.length() - 6; i++) {
				char currentChar = url.charAt(i);
				if (currentChar == '%') {
					char nextChar = url.charAt(i + 1);
					if (nextChar == 'u' || nextChar == 'U') {
						String hexString = url.substring(i + 2, i + 6);
						try {
							int hex = Integer.parseInt(hexString, 16 /* radix */);
							char c = (char) hex;
							try {
								// TODO Optimisation possible ici
								fixedUrl.append(URLEncoder.encode(String.valueOf(c), UTF_8));
							} catch (UnsupportedEncodingException e) {
								fixedUrl = new StringBuilder(url);
								break;
							}
						} catch (NumberFormatException e) {
							fixedUrl = new StringBuilder(url);
							break;
						}
						// On avance le compteur pour sauter le %uxxxx
						i += 5;
					} else {
						fixedUrl.append(currentChar);
					}
				} else {
					fixedUrl.append(currentChar);
				}
			}
			if (i <= url.length() - 5) {
				fixedUrl.append(url.substring(url.length() - 5));
			}
			response = fixedUrl.toString();
		}
		return response;
	}

	private static String encodePercent(String url) {
		int followingChar = 0;
		StringBuilder fixedUrl = new StringBuilder(url.length());
		for (int i = url.length() - 1; i >= 0; i--) {
			char currentChar = url.charAt(i);
			if (currentChar == '%') {
				if (followingChar >= 2) {
					fixedUrl.insert(0, currentChar);
				} else {
					fixedUrl.insert(0, "%25");
				}
				followingChar = 0;
			} else {
				fixedUrl.insert(0, currentChar);
				followingChar++;
			}
		}
		return fixedUrl.toString();
	}
}
