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

import java.lang.System.Logger.Level;
import java.lang.System.Logger;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class CustomErrorHandler implements org.xml.sax.ErrorHandler {

	private static Logger logger = System.getLogger(CustomErrorHandler.class.getName());

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		logger.log(Level.WARNING,
				exception.getLineNumber() + ":" + exception.getColumnNumber() + " " + exception.getMessage());
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		logger.log(Level.ERROR,
				exception.getLineNumber() + ":" + exception.getColumnNumber() + " " + exception.getMessage());
		throw new SAXException("[Error] " + exception.getLineNumber() + ":" + exception.getColumnNumber() + " "
				+ exception.getMessage());
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		logger.log(Level.ERROR,
				exception.getLineNumber() + ":" + exception.getColumnNumber() + " " + exception.getMessage());
		throw new SAXException("[Fatal Error] " + exception.getLineNumber() + ":" + exception.getColumnNumber() + " "
				+ exception.getMessage());
	}

}
