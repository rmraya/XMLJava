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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class RelaxNGParser {

    private Catalog catalog;
    private SAXBuilder builder;
    private String defaultNamespace;
    private String defaultPrefix;
    private Map<String, Element> definitions;
    private Document doc;
    private Element root;
    private List<Element> elements;
    private List<Element> attributes;
    private Set<String> visited;
    private boolean divsRemoved;
    private File baseURI;

    public RelaxNGParser(String file, Catalog catalog) throws SAXException, IOException, ParserConfigurationException {
        this.catalog = catalog;
        baseURI = new File(file).getParentFile();
        builder = new SAXBuilder();
        builder.setEntityResolver(catalog);
        doc = builder.build(file);
        root = doc.getRootElement();
        defaultPrefix = root.getNamespace();
        defaultNamespace = root.getAttributeValue("xmlns");
        if (defaultNamespace.isEmpty() && !defaultPrefix.isEmpty()) {
            defaultNamespace = root.getAttributeValue("xmlns:" + defaultPrefix);
        }
        if (XMLConstants.RELAXNG_NS_URI.equals(defaultNamespace)) {
            defaultNamespace = "";
        }
        removeForeign(root);
        replaceExternalRef(root);
        replaceIncludes(root);
        do {
            divsRemoved = false;
            removeDivs(root);
        } while (divsRemoved);
        nameAttribute(root);
    }

    public Map<String, Map<String, String>> getElements() {
        Map<String, Map<String, String>> result = new Hashtable<>();
        definitions = new HashMap<>();
        harvestDefinitions(root);
        elements = new Vector<>();
        harvestElements(root);
        Iterator<Element> it = elements.iterator();
        while (it.hasNext()) {
            Element e = it.next();
            attributes = new Vector<>();
            visited = new TreeSet<>();
            getAttributes(e);
            Map<String, String> map = new Hashtable<>();
            for (int i = 0; i < attributes.size(); i++) {
                Element attribute = attributes.get(i);
                String defaultVal = findDefaultValue(attribute);
                if (defaultVal != null) {
                    Element nameChild = getChildByLocalName(attribute, "name");
                    if (nameChild != null) {
                        String attrName = nameChild.getText().trim();
                        if (attrName.indexOf(':') == -1 || attrName.startsWith("xml:")) {
                            map.put(attrName, defaultVal);
                        }
                    }
                }
            }
            if (!map.isEmpty()) {
                Element nameChild = getChildByLocalName(e, "name");
                if (nameChild != null) {
                    String lexicalName = nameChild.getText().trim();
                    result.put(lexicalName, map);
                    int sepIdx = lexicalName.indexOf(':');
                    String localName = sepIdx == -1 ? lexicalName : lexicalName.substring(sepIdx + 1);
                    if (!result.containsKey(localName)) {
                        result.put(localName, map);
                    }
                    String ns = nameChild.getAttributeValue("ns");
                    if (!ns.isEmpty()) {
                        result.put(ns + "|" + localName, map);
                    }
                }
            }
        }
        return result;
    }

    private String findDefaultValue(Element attributeElement) {
        for (Attribute a : attributeElement.getAttributes()) {
            if ("defaultValue".equals(a.getLocalName())) {
                return a.getValue();
            }
        }
        return findDefaultValueInChildren(attributeElement);
    }

    private String findDefaultValueInChildren(Element element) {
        for (Element child : element.getChildren()) {
            if ("defaultValue".equals(child.getLocalName())) {
                return child.getText().trim();
            }
            String found = findDefaultValueInChildren(child);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private Element getChildByLocalName(Element element, String localName) {
        for (Element child : element.getChildren()) {
            if (localName.equals(child.getLocalName())) {
                return child;
            }
        }
        return null;
    }

    private void getAttributes(Element e) {
        String localName = e.getLocalName();
        if ("attribute".equals(localName)) {
            attributes.add(e);
            return;
        }
        if ("ref".equals(localName) || "parentRef".equals(localName)) {
            String name = e.getAttributeValue("name");
            if (!visited.contains(name)) {
                visited.add(name);
                Element definition = definitions.get(name);
                if (definition != null) {
                    getAttributes(definition);
                }
            }
            return;
        }
        List<Element> children = e.getChildren();
        Iterator<Element> it = children.iterator();
        while (it.hasNext()) {
            Element child = it.next();
            if ("element".equals(child.getLocalName())) {
                continue;
            }
            getAttributes(child);
        }
    }

    private void nameAttribute(Element e) {
        nameAttribute(e, new HashMap<>());
    }

    private void nameAttribute(Element e, Map<String, String> context) {
        Map<String, String> currentContext = augmentNamespaceContext(context, e);
        boolean isElementPattern = "element".equals(e.getLocalName());
        boolean isAttributePattern = "attribute".equals(e.getLocalName());
        if ((isElementPattern || isAttributePattern) && e.hasAttribute("name")) {
            Element nameEl = new Element("name");
            String value = e.getAttributeValue("name");
            nameEl.setText(value);
            if (e.hasAttribute("ns")) {
                nameEl.setAttribute("ns", e.getAttributeValue("ns"));
                e.removeAttribute("ns");
            } else {
                String resolvedNs = resolveNamespaceBinding(value, currentContext, isElementPattern, isAttributePattern);
                if (resolvedNs != null && !resolvedNs.isEmpty()) {
                    nameEl.setAttribute("ns", resolvedNs);
                }
            }
            e.removeAttribute("name");
            e.getContent().add(0, nameEl);
        }
        List<Element> children = e.getChildren();
        Iterator<Element> it = children.iterator();
        while (it.hasNext()) {
            nameAttribute(it.next(), currentContext);
        }
    }

    private Map<String, String> augmentNamespaceContext(Map<String, String> base, Element element) {
        Map<String, String> updated = new HashMap<>(base);
        for (Attribute attr : element.getAttributes()) {
            String attrName = attr.getName();
            if ("xmlns".equals(attrName)) {
                updated.put("", attr.getValue());
            } else if (attrName.startsWith("xmlns:")) {
                updated.put(attrName.substring(6), attr.getValue());
            }
        }
        updated.putIfAbsent("xml", "http://www.w3.org/XML/1998/namespace");
        return updated;
    }

    private String resolveNamespaceBinding(String lexicalName, Map<String, String> context, boolean isElementPattern,
            boolean isAttributePattern) {
        int separatorIndex = lexicalName.indexOf(':');
        if (separatorIndex == -1) {
            if (isElementPattern) {
                return context.get("");
            }
            return null;
        }
        String prefix = lexicalName.substring(0, separatorIndex);
        return context.get(prefix);
    }

    private Element getRootElement() {
        return root;
    }

    private boolean isRelaxNGElement(Element element) {
        String prefix = element.getNamespace();
        if (!defaultPrefix.equals(prefix)) {
            return false;
        }
        if (element.hasAttribute("xmlns")) {
            String ns = element.getAttributeValue("xmlns");
            if (!ns.isEmpty() && !ns.equals(defaultNamespace) && !XMLConstants.RELAXNG_NS_URI.equals(ns)) {
                return false;
            }
        }
        if (!defaultPrefix.isEmpty() && element.hasAttribute("xmlns:" + defaultPrefix)) {
            String ns = element.getAttributeValue("xmlns:" + defaultPrefix);
            if (!ns.isEmpty() && !ns.equals(defaultNamespace) && !XMLConstants.RELAXNG_NS_URI.equals(ns)) {
                return false;
            }
        }
        return true;
    }

    private boolean isCompatibilityAnnotation(Element element) {
        return "defaultValue".equals(element.getLocalName());
    }

    private void removeForeign(Element e) throws SAXException, IOException, ParserConfigurationException {
        List<XMLNode> newContent = new Vector<>();
        List<XMLNode> content = e.getContent();
        Iterator<XMLNode> it = content.iterator();
        while (it.hasNext()) {
            XMLNode node = it.next();
            if (node.getNodeType() == XMLNode.TEXT_NODE) {
                newContent.add(node);
                continue;
            }
            if (node.getNodeType() == XMLNode.PROCESSING_INSTRUCTION_NODE) {
                newContent.add(node);
                continue;
            }
            if (node.getNodeType() == XMLNode.ELEMENT_NODE) {
                Element child = (Element) node;
                if (!isRelaxNGElement(child)) {
                    if (isCompatibilityAnnotation(child)) {
                        newContent.add(child);
                    }
                    continue;
                }
                removeForeign(child);
                newContent.add(child);
            }
        }
        e.setContent(newContent);
    }

    private void replaceExternalRef(Element e) throws SAXException, IOException, ParserConfigurationException {
        List<XMLNode> newContent = new Vector<>();
        List<XMLNode> content = e.getContent();
        Iterator<XMLNode> it = content.iterator();
        while (it.hasNext()) {
            XMLNode node = it.next();
            if (node.getNodeType() == XMLNode.TEXT_NODE) {
                TextNode text = (TextNode) node;
                if (!text.toString().isBlank()) {
                    newContent.add(node);
                }
            }
            if (node.getNodeType() == XMLNode.PROCESSING_INSTRUCTION_NODE) {
                newContent.add(node);
            }
            if (node.getNodeType() == XMLNode.ELEMENT_NODE) {
                Element child = (Element) node;
                if ("externalRef".equals(child.getLocalName())) {
                    String href = child.getAttributeValue("href");
                    String system = null;
                    if (catalog != null) {
                        system = catalog.matchSystem(null, href);
                        if (system == null) {
                            system = catalog.matchURI(href);
                        }
                    }
                    if (system == null) {
                        File f = new File(baseURI, href);
                        if (f.exists()) {
                            system = f.getAbsolutePath();
                        }
                    }
                    if (system != null) {
                        RelaxNGParser parser = new RelaxNGParser(system, catalog);
                        newContent.add(parser.getRootElement());
                    } else {
                        MessageFormat mf = new MessageFormat(Messages.getString("RelaxNGParser.1"));
                        throw new SAXException(mf.format(new String[] { href }));
                    }
                    continue;
                }
                replaceIncludes(child);
                newContent.add(child);
            }
        }
        e.setContent(newContent);
    }

    private void replaceIncludes(Element e) throws SAXException, IOException, ParserConfigurationException {
        List<XMLNode> newContent = new Vector<>();
        List<XMLNode> content = e.getContent();
        Iterator<XMLNode> it = content.iterator();
        while (it.hasNext()) {
            XMLNode node = it.next();
            if (node.getNodeType() == XMLNode.TEXT_NODE) {
                TextNode text = (TextNode) node;
                if (!text.toString().isBlank()) {
                    newContent.add(node);
                }
            }
            if (node.getNodeType() == XMLNode.PROCESSING_INSTRUCTION_NODE) {
                newContent.add(node);
            }
            if (node.getNodeType() == XMLNode.ELEMENT_NODE) {
                Element child = (Element) node;
                if ("include".equals(child.getLocalName())) {
                    String href = child.getAttributeValue("href");
                    String system = null;
                    if (catalog != null) {
                        system = catalog.matchSystem(null, href);
                        if (system == null) {
                            system = catalog.matchURI(href);
                        }
                    }
                    if (system == null) {
                        File f = new File(baseURI, href);
                        if (f.exists()) {
                            system = f.getAbsolutePath();
                        }
                    }
                    if (system != null) {
                        RelaxNGParser parser = new RelaxNGParser(system, catalog);
                        Element div = new Element("div");
                        div.addContent(parser.getRootElement());
                        List<Element> children = child.getChildren();
                        for (int i = 0; i < children.size(); i++) {
                            div.addContent(children.get(i));
                        }
                        newContent.add(div);
                    } else {
                        MessageFormat mf = new MessageFormat(Messages.getString("RelaxNGParser.0"));
                        throw new SAXException(mf.format(new String[] { href }));
                    }
                    continue;
                }
                replaceIncludes(child);
                newContent.add(child);
            }
        }
        e.setContent(newContent);
    }

    private void harvestElements(Element e) {
        if ("element".equals(e.getLocalName()) && getChildByLocalName(e, "name") != null) {
            elements.add(e);
        }
        List<Element> children = e.getChildren();
        Iterator<Element> it = children.iterator();
        while (it.hasNext()) {
            harvestElements(it.next());
        }
    }

    private void harvestDefinitions(Element e) {
        if ("define".equals(e.getLocalName())) {
            String name = e.getAttributeValue("name");
            if (definitions.containsKey(name)) {
                Element old = definitions.get(name);
                old.addContent(e.getContent());
                definitions.put(name, old);
            } else {
                definitions.put(name, e);
            }
        }
        List<Element> children = e.getChildren();
        Iterator<Element> it = children.iterator();
        while (it.hasNext()) {
            harvestDefinitions(it.next());
        }
    }

    private void removeDivs(Element e) {
        List<XMLNode> newContent = new Vector<>();
        List<XMLNode> content = e.getContent();
        Iterator<XMLNode> it = content.iterator();
        while (it.hasNext()) {
            XMLNode node = it.next();
            if (node.getNodeType() == XMLNode.ELEMENT_NODE) {
                Element child = (Element) node;
                if ("div".equals(child.getLocalName())) {
                    newContent.addAll(child.getContent());
                    divsRemoved = true;
                } else {
                    newContent.add(node);
                }
            } else {
                newContent.add(node);
            }
        }
        e.setContent(newContent);
        List<Element> children = e.getChildren();
        Iterator<Element> tt = children.iterator();
        while (tt.hasNext()) {
            removeDivs(tt.next());
        }
    }
}
