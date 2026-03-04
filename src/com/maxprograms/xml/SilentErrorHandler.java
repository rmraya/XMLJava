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

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SilentErrorHandler implements org.xml.sax.ErrorHandler {

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		// keep quiet
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		throw new SAXException("[Error] " + exception.getLineNumber() + ":" + exception.getColumnNumber() + " "
				+ exception.getMessage());
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		throw new SAXException("[Fatal Error] " + exception.getLineNumber() + ":" + exception.getColumnNumber() + " "
				+ exception.getMessage());
	}

}
