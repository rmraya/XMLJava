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

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

public class EntityHandler extends DefaultHandler2 {

	private Map<String, String> entities;
	private List<AttlistDecl> attlistDeclarations;

	public EntityHandler() {
		super();
		entities = new Hashtable<>();
		attlistDeclarations = new Vector<>();
	}

	@Override
	public void internalEntityDecl(String name, String value) throws SAXException {
		super.internalEntityDecl(name, value);
		if (!name.startsWith("%")) {
			entities.put(name, value);
		}
	}

	public Map<String, String> getEntities() {
		return entities;
	}

	@Override
	public void elementDecl(String name, String model) throws SAXException {
		// TODO: Handle element declarations if needed
	}

	@Override
	public void notationDecl(String name, String publicId, String systemId) throws SAXException {
		// TODO: Handle notation declarations if needed
	}

	@Override
	public void attributeDecl(String element, String attribute, String type, String mode, String value)
			throws SAXException {
		if (attribute.indexOf(':') != -1 || attribute.startsWith("xml:") || mode == null || type == null) {
			return;
		}

		// Find or create the corresponding AttlistDecl for the element
		AttlistDecl attlist = findOrCreateAttlistDecl(element);

		// Determine if the attribute is #FIXED
		boolean isFixed = "#FIXED".equals(mode);

		// TODO ensure compatibility with "AttributeDecl" implementation

		// Add the attribute to the AttlistDecl
		attlist.getAttributes().add(new AttributeDecl(attribute, type, value, isFixed));
	}

	private AttlistDecl findOrCreateAttlistDecl(String element) {
		for (AttlistDecl attlist : attlistDeclarations) {
			if (attlist.getListName().equals(element)) {
				return attlist;
			}
		}
		// Create a new AttlistDecl if none exists for the element
		AttlistDecl newAttlist = new AttlistDecl("<!ATTLIST " + element + ">");
		attlistDeclarations.add(newAttlist);
		return newAttlist;
	}

	public List<AttlistDecl> getAttlistDeclarations() {
		return attlistDeclarations;
	}
}