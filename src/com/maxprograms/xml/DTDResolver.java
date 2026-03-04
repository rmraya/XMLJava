/*******************************************************************************
 * Copyright (c) 2022-2026 Maxprograms. All rights reserved.
 *
 * This software is the proprietary property of Maxprograms.
 * Use, modification, and distribution are subject to the terms of the 
 * Software License Agreement found in the root of this distribution 
 *
 * Unauthorized redistribution or commercial use is strictly prohibited.
 *******************************************************************************/
package com.maxprograms.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class DTDResolver implements EntityResolver {

	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws IOException {
		try {
			URI u = new URI("").resolve(systemId).normalize();
			File file = new File(u.toURL().toString());
			if (file.exists()) {
				return new InputSource(new FileInputStream(file));
			}
		} catch (URISyntaxException e) {
			// ignore
		}
		return null;
	}

}
