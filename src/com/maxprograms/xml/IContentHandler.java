/*******************************************************************************
 * Copyright (c) 2022 - 2024 Maxprograms.
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

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

public interface IContentHandler extends ContentHandler, LexicalHandler {
	
	void setCatalog(Catalog catalog);
	Document getDocument();

}
