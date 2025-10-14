/*******************************************************************************
 * Copyright (c) 2022 - 2025 Maxprograms.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Commercial licenses are available at https://maxprograms.com/
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/
package com.maxprograms.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.StringTokenizer;

public class XMLUtils {

	public static final byte[] UTF8BOM = { -17, -69, -65 };
	public static final byte[] UTF16BEBOM  = { -1, -2 };
	public static final byte[] UTF16LEBOM = { -2, -1 }; 

	private XMLUtils() {
		// do not instantiate
	}

	public static String cleanText(String string) {
		if (string == null) {
			return null;
		}
		String result = string.replace("&", "&amp;");
		result = result.replace("<", "&lt;");
		return result.replace(">", "&gt;");
	}

	public static String validChars(String input) {
		// Valid: #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] |
		// [#x10000-#x10FFFF]
		// Discouraged: [#x7F-#x84], [#x86-#x9F], [#xFDD0-#xFDDF]
		//
		StringBuilder buffer = new StringBuilder();
		char c;
		int length = input.length();
		for (int i = 0; i < length; i++) {
			c = input.charAt(i);
			if (c == '\t' || c == '\n' || c == '\r' || c >= '\u0020' && c <= '\uD7DF'
					|| c >= '\uE000' && c <= '\uFFFD') {
				// normal character
				buffer.append(c);
			} else if (c >= '\u007F' && c <= '\u0084' || c >= '\u0086' && c <= '\u009F'
					|| c >= '\uFDD0' && c <= '\uFDDF') {
				// Control character
				buffer.append("&#x" + Integer.toHexString(c) + ";");
			} else if (c >= '\uDC00' && c <= '\uDFFF' || c >= '\uD800' && c <= '\uDBFF') {
				// Multiplane character
				buffer.append(input.substring(i, i + 1));
			}
		}
		return buffer.toString();
	}

	public static String uncleanText(String string) {
		String result = string.replace("&amp;", "&");
		result = result.replace("&lt;", "<");
		result = result.replace("&gt;", ">");
		result = result.replace("&quot;", "\"");
		return result.replace("&apos;", "\'");
	}

	public static String getAbsolutePath(String homeFile, String relative) throws IOException {
		File home = new File(homeFile);
		// If home is a file, get the parent
		File result;
		if (!home.isDirectory()) {
			home = home.getParentFile();
		}
		result = new File(home, relative);
		return result.getCanonicalPath();
	}

	public static boolean isXmlSpace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

	public static String getXMLEncoding(String fileName) {
		// return UTF-8 as default
		String result = StandardCharsets.UTF_8.name();
		try {
			// check if there is a BOM (byte order mark)
			// at the start of the document
			byte[] array = new byte[2];
			try (FileInputStream inputStream = new FileInputStream(fileName)) {
				int bytes = inputStream.read(array);
				if (bytes == -1) {
					MessageFormat mf = new MessageFormat(Messages.getString("XMLUtils.1"));
					throw new IOException(mf.format(new String[] { fileName }));
				}
			}
			byte[] lt = "<".getBytes();
			byte[] feff = { -1, -2 };
			byte[] fffe = { -2, -1 };
			if (array[0] != lt[0]) {
				// there is a BOM, now check the order
				if (array[0] == fffe[0] && array[1] == fffe[1]) {
					return StandardCharsets.UTF_16BE.name();
				}
				if (array[0] == feff[0] && array[1] == feff[1]) {
					return StandardCharsets.UTF_16LE.name();
				}
			}
			// check declared encoding
			String line = "";
			try (FileReader input = new FileReader(fileName); BufferedReader buffer = new BufferedReader(input)) {
				line = buffer.readLine();
			}
			if (line.startsWith("<?")) {
				line = line.substring(2, line.indexOf("?>"));
				line = line.replace("\'", "\"");
				StringTokenizer tokenizer = new StringTokenizer(line);
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					if (token.startsWith("encoding")) {
						result = token.substring(token.indexOf('\"') + 1, token.lastIndexOf('\"'));
					}
				}
			}
		} catch (Exception e) {
			Logger logger = System.getLogger(XMLUtils.class.getName());
			logger.log(Level.ERROR, e.getMessage(), e);
		}
		if (result.equalsIgnoreCase("utf-8")) {
			result = StandardCharsets.UTF_8.name();
		}
		return result;
	}
}
