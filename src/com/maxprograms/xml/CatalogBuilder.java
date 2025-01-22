/*******************************************************************************
 * Copyright (c) 2022 - 2025 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/

package com.maxprograms.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class CatalogBuilder {

    private static Map<String,Catalog> map = new Hashtable<>();

    private CatalogBuilder() {
        // Do not instantiate
    }

    public static Catalog getCatalog(String file) throws SAXException, IOException, ParserConfigurationException, URISyntaxException{
        if (!map.containsKey(file)) {
            map.put(file, new Catalog(file));
        }
        return map.get(file);
    }
}
