/*******************************************************************************
 * Copyright (c) 2022-2026 Maxprograms. All rights reserved.
 *
 * This software is the proprietary property of Maxprograms.
 * Use, modification, and distribution are subject to the terms of the 
 * Software License Agreement found in the root of this distribution 
 * and at http://www.maxprograms.com/
 *
 * Unauthorized redistribution or commercial use is strictly prohibited.
 *******************************************************************************/

package com.maxprograms.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class CatalogBuilder {

    private static Map<String, Catalog> map = new Hashtable<>();

    private CatalogBuilder() {
        // Do not instantiate
    }

    public static Catalog getCatalog(String file)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
        if (!map.containsKey(file)) {
            map.put(file, new Catalog(file));
        }
        return map.get(file);
    }
}
