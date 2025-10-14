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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class SAXBuilder {

	private EntityResolver resolver = null;
	private ErrorHandler errorHandler = null;
	private boolean validating;
	private boolean preserveAttributes = false;
	private IContentHandler contentHandler;

	public SAXBuilder() {
		validating = false;
	}

	public SAXBuilder(boolean validating) {
		this.validating = validating;
	}

	public Document build(String filename) throws SAXException, IOException, ParserConfigurationException {
		File f = new File(filename);
		if (!f.exists()) {
			MessageFormat mf = new MessageFormat(Messages.getString("SAXBuilder.1"));
			throw new IOException(mf.format(new String[] { filename }));
		}
		return build(f.toURI().toURL());
	}

	public Document build(URI uri) throws SAXException, IOException, ParserConfigurationException {
		return build(uri.toURL());
	}

	public Document build(File file) throws SAXException, IOException, ParserConfigurationException {
		if (!file.exists()) {
			MessageFormat mf = new MessageFormat(Messages.getString("SAXBuilder.1"));
			throw new IOException(mf.format(new String[] { file.getAbsolutePath() }));
		}
		return build(file.toURI().toURL());
	}

	public Document build(ByteArrayInputStream stream) throws SAXException, IOException, ParserConfigurationException {
		XMLReader parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		parser.setFeature("http://xml.org/sax/features/namespaces", true);
		if (validating) {
			parser.setFeature("http://xml.org/sax/features/validation", true);
			parser.setFeature("http://apache.org/xml/features/validation/schema", true);
			parser.setFeature("http://apache.org/xml/features/validation/dynamic", true);
		}
		parser.setProperty("http://www.oracle.com/xml/jaxp/properties/totalEntitySizeLimit", 0);
		boolean clearHandler = false;
		if (contentHandler == null) {
			contentHandler = new CustomContentHandler();
			clearHandler = true;
		}
		parser.setContentHandler(contentHandler);
		if (resolver == null) {
			resolver = new DTDResolver();
		}
		parser.setEntityResolver(resolver);
		if (errorHandler != null) {
			parser.setErrorHandler(errorHandler);
		} else {
			parser.setErrorHandler(new CustomErrorHandler());
		}
		parser.setProperty("http://xml.org/sax/properties/lexical-handler", contentHandler);

		EntityHandler declhandler = new EntityHandler();
		parser.setProperty("http://xml.org/sax/properties/declaration-handler", declhandler);

		parser.parse(new InputSource(stream));
		Document doc = contentHandler.getDocument();

		Map<String, String> entities = declhandler.getEntities();
		if (entities.size() > 0) {
			doc.setEntities(entities);
		}
		if (clearHandler) {
			contentHandler = null;
		}
		return doc;
	}

	public void setContentHandler(IContentHandler handler) {
		contentHandler = handler;
	}

	public void setEntityResolver(EntityResolver res) {
		resolver = res;
	}

	public void setErrorHandler(ErrorHandler handler) {
		errorHandler = handler;
	}

	public void setValidating(boolean value) {
		validating = value;
	}

	public Document build(URL url) throws SAXException, IOException, ParserConfigurationException {
		if ("file".equals(url.getProtocol()) && resolver instanceof Catalog catalog) {
			File f = new File(url.toString());
			String parent = f.getParentFile().getAbsolutePath();
			if (parent.lastIndexOf("file:") != -1) {
				parent = parent.substring(parent.lastIndexOf("file:") + 5);
			}
			catalog.currentDocumentBase(parent);
		}
		XMLReader parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		parser.setFeature("http://xml.org/sax/features/namespaces", true);
		if (validating) {
			parser.setFeature("http://xml.org/sax/features/validation", true);
			parser.setFeature("http://apache.org/xml/features/validation/schema", true);
			parser.setFeature("http://apache.org/xml/features/validation/dynamic", true);
		}
		parser.setProperty("http://www.oracle.com/xml/jaxp/properties/totalEntitySizeLimit", 0);
		boolean clearHandler = false;
		if (contentHandler == null) {
			contentHandler = new CustomContentHandler();
			if (resolver instanceof Catalog catalog) {
				contentHandler.setCatalog(catalog);
			}
			clearHandler = true;
		}
		parser.setContentHandler(contentHandler);
		if (resolver == null) {
			resolver = new DTDResolver();
		}
		parser.setEntityResolver(resolver);
		if (errorHandler != null) {
			parser.setErrorHandler(errorHandler);
		} else {
			parser.setErrorHandler(new CustomErrorHandler());
		}
		parser.setProperty("http://xml.org/sax/properties/lexical-handler", contentHandler);
		parser.setFeature("http://xml.org/sax/features/namespaces", true);
		parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

		EntityHandler declhandler = new EntityHandler();
		parser.setProperty("http://xml.org/sax/properties/declaration-handler", declhandler);

		parser.parse(new InputSource(url.openStream()));
		Document doc = contentHandler.getDocument();
		if (doc != null) {
			Map<String, String> entities = declhandler.getEntities();
			if (entities != null && entities.size() > 0) {
				doc.setEntities(entities);
			}
			List<AttlistDecl> attlistDeclarations = declhandler.getAttlistDeclarations();
			if (attlistDeclarations != null && preserveAttributes && hasCustomAttributes(url, doc.getEncoding())) {
				Set<String> namespaces = getRootNamespaces(doc.getRootElement());
				doc.setAttlistDeclarations(filterAttlistDeclarations(attlistDeclarations, namespaces));
			}
		}
		if (clearHandler) {
			contentHandler = null;
		}
		return doc;
	}

	private List<AttlistDecl> filterAttlistDeclarations(List<AttlistDecl> declarations, Set<String> namespaces) {
		List<AttlistDecl> result = new Vector<>();
		for (AttlistDecl attlist : declarations) {
			AttlistDecl filteredAttlist = new AttlistDecl("<!ATTLIST " + attlist.getListName() + ">");
			for (AttributeDecl attribute : attlist.getAttributes()) {
				String aName = attribute.getName();
				if (aName.indexOf(':') != -1) {
					String prefix = aName.substring(0, aName.indexOf(':'));
					if (namespaces.contains(prefix)) {
						filteredAttlist.getAttributes().add(attribute);
					}
				} else {
					filteredAttlist.getAttributes().add(attribute);
				}
			}
			if (!filteredAttlist.getAttributes().isEmpty()) {
				result.add(filteredAttlist);
			}
		}
		return result;
	}

	private Set<String> getRootNamespaces(Element root) {
		Set<String> namespaces = new TreeSet<>();
		List<Attribute> attributes = root.getAttributes();
		Iterator<Attribute> it = attributes.iterator();
		while (it.hasNext()) {
			Attribute a = it.next();
			if (a.getName().startsWith("xmlns:")) {
				namespaces.add(a.getName().substring("xmlns:".length()));
			}
		}
		return namespaces;
	}

	private static boolean hasCustomAttributes(URL url, Charset charset) throws IOException {
		byte[] array = new byte[2048];
		try (InputStream source = url.openStream()) {
			source.read(array, 0, 2048);
		}
		String string = new String(array, charset);
		return string.indexOf("<!ATTLIST") != -1;
	}

	public void preserveCustomAttributes(boolean value) {
		preserveAttributes = value;
	}

}
