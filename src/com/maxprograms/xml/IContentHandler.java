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

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

public interface IContentHandler extends ContentHandler, LexicalHandler {
	
	void setCatalog(Catalog catalog);
	Document getDocument();

}
