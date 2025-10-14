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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

public class Messages {
	
    private static Properties props;

    private Messages() {
        // do not instantiate this class
    }

    public static String getString(String key) {
        String resourceName = "xmljava";
        try {
            if (props == null) {
                Locale locale = Locale.getDefault();
                String language = locale.getLanguage();
                String extension = "_" + language + ".properties";
                // check if there is a resource for full language code
                if (Messages.class.getResource(resourceName + extension) == null) {
                    // if not, check if there is a resource for language only
                    extension = "_" + language.substring(0, 2) + ".properties";
                }
                if (Messages.class.getResource(resourceName + extension) == null) {
                    // if not, use the default resource
                    extension = ".properties";
                }
                try (InputStream is = Messages.class.getResourceAsStream(resourceName + extension)) {
                    try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                        props = new Properties();
                        props.load(reader);
                    }
                }
            }
            return props.getProperty(key, '!' + key + '!');
        } catch (IOException | NullPointerException e) {
            return '!' + key + '!';
        }
    }
}
