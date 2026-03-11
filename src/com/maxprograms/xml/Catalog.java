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
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

public class Catalog implements EntityResolver2 {

    Logger logger = System.getLogger(Catalog.class.getName());

    private Map<String, String> systemCatalog;
    private Map<String, String> publicCatalog;
    private Map<String, String> uriCatalog;
    private Map<String, String> dtdCatalog;
    private Map<String, String> dtdPublicEntities;
    private Map<String, String> dtdSystemEntities;
    private Set<String> parsedDTDs;
    private List<String[]> uriRewrites;
    private List<String[]> systemRewrites;
    private List<String[]> systemSuffixes;
    private List<String[]> uriSuffixes;
    private List<String[]> delegatePublics;
    private List<String[]> delegateSystems;
    private List<String[]> delegateURIs;
    private Map<String, Catalog> delegateCatalogs;
    private String workDir;
    private String base = "";
    private String documentParent = "";
    private String prefer = "public";

    protected Catalog(String catalogFile)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
        File file = new File(catalogFile);
        if (!file.isAbsolute()) {
            String home = System.getenv("OpenXLIFF_HOME");
            if (home == null) {
                home = System.getProperty("user.dir");
            }
            String absolute = XMLUtils.getAbsolutePath(home, catalogFile);
            file = new File(absolute);
        }
        workDir = file.getParent();
        if (!workDir.endsWith(File.separator)) {
            workDir = workDir + File.separator;
        }

        systemCatalog = new Hashtable<>();
        publicCatalog = new Hashtable<>();
        dtdCatalog = new Hashtable<>();
        uriCatalog = new Hashtable<>();
        uriRewrites = new Vector<>();
        systemRewrites = new Vector<>();
        systemSuffixes = new Vector<>();
        uriSuffixes = new Vector<>();
        delegatePublics = new Vector<>();
        delegateSystems = new Vector<>();
        delegateURIs = new Vector<>();
        delegateCatalogs = new Hashtable<>();

        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(catalogFile);
        Element root = doc.getRootElement();
        if (root.hasAttribute("prefer")) {
            prefer = root.getAttributeValue("prefer");
        }
        recurse(root);
    }

    private void recurse(Element root)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
        List<Element> children = root.getChildren();
        Iterator<Element> i = children.iterator();
        while (i.hasNext()) {
            Element child = i.next();
            String currentBase = base;
            String currentPrefer = prefer;

            if (child.hasAttribute("prefer")) {
                prefer = child.getAttributeValue("prefer");
            }

            if (child.hasAttribute("xml:base")) {
                base = child.getAttributeValue("xml:base");
                File b = new File(base);
                if (!b.isAbsolute()) {
                    String absolute = XMLUtils.getAbsolutePath(workDir, base);
                    b = new File(absolute);
                }
                if (!b.exists()) {
                    throw new IOException("Invalid xml:base: " + b.toPath().toString());
                }
                base = XMLUtils.getAbsolutePath(workDir, base);
                if (!base.endsWith(File.separator)) {
                    base = base + File.separator;
                }
            }

            if (child.getName().equals("system") && !systemCatalog.containsKey(child.getAttributeValue("systemId"))) {
                String uri = makeAbsolute(child.getAttributeValue("uri"));
                if (validate(uri)) {
                    systemCatalog.put(child.getAttributeValue("systemId"), uri);
                    if (uri.endsWith(".dtd")) {
                        File dtd = new File(uri);
                        dtdCatalog.computeIfAbsent(dtd.getName(), k -> dtd.getAbsolutePath());
                    }
                }
            }
            if (child.getName().equals("public")) {
                String publicId = child.getAttributeValue("publicId");
                if (publicId.startsWith("urn:publicid:")) {
                    publicId = unwrapUrn(publicId);
                }
                if (!publicCatalog.containsKey(publicId)) {
                    String uri = makeAbsolute(child.getAttributeValue("uri"));
                    if (validate(uri)) {
                        publicCatalog.put(publicId, uri);
                        if (uri.endsWith(".dtd")) {
                            File dtd = new File(uri);
                            dtdCatalog.computeIfAbsent(dtd.getName(), k -> dtd.getAbsolutePath());
                        }
                    }
                }
            }
            if (child.getName().equals("uri") && !uriCatalog.containsKey(child.getAttributeValue("name"))) {
                String uri = makeAbsolute(child.getAttributeValue("uri"));
                if (validate(uri)) {
                    uriCatalog.put(child.getAttributeValue("name"), uri);
                    if (uri.endsWith(".dtd")) {
                        File dtd = new File(uri);
                        dtdCatalog.computeIfAbsent(dtd.getName(), k -> dtd.getAbsolutePath());
                    }
                }
            }
            if (child.getName().equals("nextCatalog")) {
                String nextCatalog = child.getAttributeValue("catalog");
                File f = new File(nextCatalog);
                if (!f.isAbsolute()) {
                    nextCatalog = base.isEmpty() ? XMLUtils.getAbsolutePath(workDir, nextCatalog)
                            : XMLUtils.getAbsolutePath(base, nextCatalog);
                }
                Catalog cat = new Catalog(nextCatalog);
                cat.getSystemCatalog().forEach(systemCatalog::putIfAbsent);
                cat.getPublicCatalog().forEach(publicCatalog::putIfAbsent);
                cat.getUriCatalog().forEach(uriCatalog::putIfAbsent);
                cat.getDtdCatalog().forEach(dtdCatalog::putIfAbsent);
                List<String[]> system = cat.getSystemRewrites();
                for (int h = 0; h < system.size(); h++) {
                    String[] pair = system.get(h);
                    if (!systemRewrites.contains(pair)) {
                        systemRewrites.add(pair);
                    }
                }
                List<String[]> uriList = cat.getUriRewrites();
                for (int h = 0; h < uriList.size(); h++) {
                    String[] pair = uriList.get(h);
                    if (!uriRewrites.contains(pair)) {
                        uriRewrites.add(pair);
                    }
                }
                List<String[]> sysSuffixes = cat.getSystemSuffixes();
                for (int h = 0; h < sysSuffixes.size(); h++) {
                    systemSuffixes.add(sysSuffixes.get(h));
                }
                List<String[]> uriSuffs = cat.getUriSuffixes();
                for (int h = 0; h < uriSuffs.size(); h++) {
                    uriSuffixes.add(uriSuffs.get(h));
                }
                List<String[]> delPubs = cat.getDelegatePublics();
                for (int h = 0; h < delPubs.size(); h++) {
                    delegatePublics.add(delPubs.get(h));
                }
                List<String[]> delSys = cat.getDelegateSystems();
                for (int h = 0; h < delSys.size(); h++) {
                    delegateSystems.add(delSys.get(h));
                }
                List<String[]> delURIs = cat.getDelegateURIs();
                for (int h = 0; h < delURIs.size(); h++) {
                    delegateURIs.add(delURIs.get(h));
                }
                Map<String, Catalog> delCats = cat.getDelegateCatalogs();
                for (Map.Entry<String, Catalog> entry : delCats.entrySet()) {
                    delegateCatalogs.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }
            if (child.getName().equals("systemSuffix")) {
                String suffix = child.getAttributeValue("systemIdSuffix");
                String uri = makeAbsolute(child.getAttributeValue("uri"));
                if (validate(uri)) {
                    systemSuffixes.add(new String[] { suffix, uri });
                }
            }
            if (child.getName().equals("uriSuffix")) {
                String suffix = child.getAttributeValue("uriSuffix");
                String uri = makeAbsolute(child.getAttributeValue("uri"));
                if (validate(uri)) {
                    uriSuffixes.add(new String[] { suffix, uri });
                }
            }
            if (child.getName().equals("delegatePublic")) {
                String prefix = child.getAttributeValue("publicIdStartString");
                String catalogRef = child.getAttributeValue("catalog");
                File fc = new File(catalogRef);
                if (!fc.isAbsolute()) {
                    catalogRef = base.isEmpty() ? XMLUtils.getAbsolutePath(workDir, catalogRef)
                            : XMLUtils.getAbsolutePath(base, catalogRef);
                }
                if (!delegateCatalogs.containsKey(catalogRef)) {
                    delegateCatalogs.put(catalogRef, new Catalog(catalogRef));
                }
                delegatePublics.add(new String[] { prefix, catalogRef });
            }
            if (child.getName().equals("delegateSystem")) {
                String prefix = child.getAttributeValue("systemIdStartString");
                String catalogRef = child.getAttributeValue("catalog");
                File fc = new File(catalogRef);
                if (!fc.isAbsolute()) {
                    catalogRef = base.isEmpty() ? XMLUtils.getAbsolutePath(workDir, catalogRef)
                            : XMLUtils.getAbsolutePath(base, catalogRef);
                }
                if (!delegateCatalogs.containsKey(catalogRef)) {
                    delegateCatalogs.put(catalogRef, new Catalog(catalogRef));
                }
                delegateSystems.add(new String[] { prefix, catalogRef });
            }
            if (child.getName().equals("delegateURI")) {
                String prefix = child.getAttributeValue("uriStartString");
                String catalogRef = child.getAttributeValue("catalog");
                File fc = new File(catalogRef);
                if (!fc.isAbsolute()) {
                    catalogRef = base.isEmpty() ? XMLUtils.getAbsolutePath(workDir, catalogRef)
                            : XMLUtils.getAbsolutePath(base, catalogRef);
                }
                if (!delegateCatalogs.containsKey(catalogRef)) {
                    delegateCatalogs.put(catalogRef, new Catalog(catalogRef));
                }
                delegateURIs.add(new String[] { prefix, catalogRef });
            }
            if (child.getName().equals("rewriteSystem")) {
                String uri = makeAbsolute(child.getAttributeValue("rewritePrefix"));
                String[] pair = new String[] { child.getAttributeValue("systemIdStartString"), uri };
                if (!systemRewrites.contains(pair)) {
                    systemRewrites.add(pair);
                }
            }
            if (child.getName().equals("rewriteURI")) {
                String uri = makeAbsolute(child.getAttributeValue("rewritePrefix"));
                String[] pair = new String[] { child.getAttributeValue("uriStartString"), uri };
                if (!uriRewrites.contains(pair)) {
                    uriRewrites.add(pair);
                }
            }
            recurse(child);
            base = currentBase;
            prefer = currentPrefer;
        }
    }

    private static boolean validate(String uri) {
        File file = new File(uri);
        return file.exists();
    }

    private String makeAbsolute(String uri) throws IOException {
        File f = new File(base + uri);
        if (!f.isAbsolute()) {
            if (!base.isEmpty()) {
                return XMLUtils.getAbsolutePath(base, uri);
            }
            return XMLUtils.getAbsolutePath(workDir, uri);
        }
        return base + uri;
    }

    private Map<String, String> getSystemCatalog() {
        return systemCatalog;
    }

    private Map<String, String> getPublicCatalog() {
        return publicCatalog;
    }

    private Map<String, String> getUriCatalog() {
        return uriCatalog;
    }

    private Map<String, String> getDtdCatalog() {
        return dtdCatalog;
    }

    private List<String[]> getSystemRewrites() {
        return systemRewrites;
    }

    private List<String[]> getUriRewrites() {
        return uriRewrites;
    }

    private List<String[]> getSystemSuffixes() {
        return systemSuffixes;
    }

    private List<String[]> getUriSuffixes() {
        return uriSuffixes;
    }

    private List<String[]> getDelegatePublics() {
        return delegatePublics;
    }

    private List<String[]> getDelegateSystems() {
        return delegateSystems;
    }

    private List<String[]> getDelegateURIs() {
        return delegateURIs;
    }

    private Map<String, Catalog> getDelegateCatalogs() {
        return delegateCatalogs;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if ("system".equals(prefer)) {
            String location = matchSystem(null, systemId);
            if (location != null) {
                return new InputSource(new FileInputStream(location));
            }
            if (publicId != null) {
                location = matchPublic(publicId);
                if (location != null) {
                    return new InputSource(new FileInputStream(location));
                }
            }
        } else {
            if (publicId != null) {
                String location = matchPublic(publicId);
                if (location != null) {
                    return new InputSource(new FileInputStream(location));
                }
            }
            String location = matchSystem(null, systemId);
            if (location != null) {
                return new InputSource(new FileInputStream(location));
            }
        }
        return null;
    }

    private static String unwrapUrn(String urn) {
        if (!urn.startsWith("urn:publicid:")) {
            return urn;
        }
        String publicId = urn.trim().substring("urn:publicid:".length());
        publicId = publicId.replaceAll("\\+", " ");
        publicId = publicId.replaceAll("\\:", "//");
        publicId = publicId.replace(";", "::");
        publicId = publicId.replace("%2B", "+");
        publicId = publicId.replace("%3A", ":");
        publicId = publicId.replace("%2F", "/");
        publicId = publicId.replace("%3B", ";");
        publicId = publicId.replace("%27", "'");
        publicId = publicId.replace("%3F", "?");
        publicId = publicId.replace("%23", "#");
        publicId = publicId.replace("%25", "%");
        return publicId;
    }

    @Override
    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
        return null;
    }

    @Override
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId)
            throws SAXException, IOException {
        if ("system".equals(prefer)) {
            String location = matchSystem(baseURI, systemId);
            if (location != null) {
                return new InputSource(new FileInputStream(location));
            }
            if (publicId != null) {
                location = matchPublic(publicId);
                if (location != null) {
                    return new InputSource(new FileInputStream(location));
                }
            }
        } else {
            if (publicId != null) {
                String location = matchPublic(publicId);
                if (location != null) {
                    return new InputSource(new FileInputStream(location));
                }
            }
            String location = matchSystem(baseURI, systemId);
            if (location != null) {
                return new InputSource(new FileInputStream(location));
            }
        }

        // This DTD is not in the catalog,
        // try to find it in the URL reported
        // by the document
        try {
            URI uri = new URI(baseURI != null ? baseURI : "").resolve(systemId).normalize();
            if (uri.toURL().getProtocol() != null) {
                if (uri.toURL().getProtocol().startsWith("http")) {
                    return resolveHttp(uri);
                }
                return new InputSource(uri.toURL().openStream());
            }
            return new InputSource(new FileInputStream(uri.toURL().toString()));
        } catch (IOException | URISyntaxException | IllegalArgumentException | NullPointerException e) {
            // ignore
        }
        if (dtdPublicEntities != null && publicId != null && dtdPublicEntities.containsKey(publicId)) {
            return new InputSource(new FileInputStream(dtdPublicEntities.get(publicId)));
        }
        if (dtdSystemEntities != null && systemId != null && dtdSystemEntities.containsKey(systemId)) {
            return new InputSource(new FileInputStream(dtdSystemEntities.get(systemId)));
        }
        Collection<String> values = systemCatalog.values();
        Iterator<String> it = values.iterator();
        while (it.hasNext()) {
            String value = it.next();
            if (value.endsWith(systemId)) {
                return new InputSource(new FileInputStream(value));
            }
        }
        return null;
    }

    private InputSource resolveHttp(URI uri) throws IOException {
        MessageFormat mf = new MessageFormat(Messages.getString("Catalog.3"));
        logger.log(Level.WARNING, mf.format(new String[] { uri.toURL().toString() }));
        URL url = uri.toURL();
        try {
            HttpURLConnection con = url.getProtocol().equals("https") ? (HttpsURLConnection) url.openConnection()
                    : (HttpURLConnection) url.openConnection();
            con.setReadTimeout(5000);
            con.connect();
            con.getResponseCode();
            if (con.getResponseCode() != 200) {
                mf = new MessageFormat(Messages.getString("Catalog.2"));
                throw new IOException(mf.format(new String[] { con.getResponseCode() + "", url.toString() }));
            }
            return new InputSource(con.getInputStream());
        } catch (IOException e) {
            mf = new MessageFormat(Messages.getString("Catalog.1"));
            throw new IOException(mf.format(new String[] { url.toString(), e.getMessage() }));
        }
    }

    public String matchPublic(String publicId) {
        if (publicId != null) {
            if (publicId.startsWith("urn:publicid:")) {
                publicId = unwrapUrn(publicId);
            }
            String bestPrefix = null;
            String bestCatalogFile = null;
            for (String[] entry : delegatePublics) {
                if (publicId.startsWith(entry[0])) {
                    if (bestPrefix == null || entry[0].length() > bestPrefix.length()) {
                        bestPrefix = entry[0];
                        bestCatalogFile = entry[1];
                    }
                }
            }
            if (bestCatalogFile != null) {
                Catalog delegate = delegateCatalogs.get(bestCatalogFile);
                return delegate != null ? delegate.matchPublic(publicId) : null;
            }
            if (publicCatalog.containsKey(publicId)) {
                return publicCatalog.get(publicId);
            }
        }
        return null;
    }

    public String matchSystem(String baseURI, String systemId) {
        if (systemId != null) {
            for (int i = 0; i < systemRewrites.size(); i++) {
                String[] pair = systemRewrites.get(i);
                if (systemId.startsWith(pair[0])) {
                    systemId = pair[1] + systemId.substring(pair[0].length());
                }
            }
            String bestPrefix = null;
            String bestCatalogFile = null;
            for (String[] entry : delegateSystems) {
                if (systemId.startsWith(entry[0])) {
                    if (bestPrefix == null || entry[0].length() > bestPrefix.length()) {
                        bestPrefix = entry[0];
                        bestCatalogFile = entry[1];
                    }
                }
            }
            if (bestCatalogFile != null) {
                Catalog delegate = delegateCatalogs.get(bestCatalogFile);
                return delegate != null ? delegate.matchSystem(baseURI, systemId) : null;
            }
            String bestSuffix = null;
            String bestSuffixUri = null;
            for (String[] entry : systemSuffixes) {
                if (systemId.endsWith(entry[0])) {
                    if (bestSuffix == null || entry[0].length() > bestSuffix.length()) {
                        bestSuffix = entry[0];
                        bestSuffixUri = entry[1];
                    }
                }
            }
            if (bestSuffixUri != null) {
                return bestSuffixUri;
            }
            if (systemCatalog.containsKey(systemId)) {
                return systemCatalog.get(systemId);
            }
            // this resource is not in catalog.

            if (!documentParent.isEmpty()) {
                // try to find the file in parent folder
                File f = new File(systemId);
                String name = f.getAbsolutePath();
                if (!f.isAbsolute()) {
                    File currentFolder = new File(System.getProperty("user.dir"));
                    if (name.startsWith(currentFolder.getAbsolutePath())) {
                        name = name.substring(currentFolder.getAbsolutePath().length());
                    } else {
                        name = f.getName();
                    }
                }
                File parent = new File(documentParent);
                File file = new File(parent, name);
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
            }
            try {
                URI u = new URI(baseURI != null ? baseURI : documentParent).resolve(systemId).normalize();
                File file = new File(u.toURL().toString());
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
            } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
                // ignore
            }
        }
        return null;
    }

    public String matchURI(String uri) {
        if (uri != null) {
            for (int i = 0; i < uriRewrites.size(); i++) {
                String[] pair = uriRewrites.get(i);
                if (uri.startsWith(pair[0])) {
                    uri = pair[1] + uri.substring(pair[0].length());
                }
            }
            String bestPrefix = null;
            String bestCatalogFile = null;
            for (String[] entry : delegateURIs) {
                if (uri.startsWith(entry[0])) {
                    if (bestPrefix == null || entry[0].length() > bestPrefix.length()) {
                        bestPrefix = entry[0];
                        bestCatalogFile = entry[1];
                    }
                }
            }
            if (bestCatalogFile != null) {
                Catalog delegate = delegateCatalogs.get(bestCatalogFile);
                return delegate != null ? delegate.matchURI(uri) : null;
            }
            String bestSuffix = null;
            String bestSuffixUri = null;
            for (String[] entry : uriSuffixes) {
                if (uri.endsWith(entry[0])) {
                    if (bestSuffix == null || entry[0].length() > bestSuffix.length()) {
                        bestSuffix = entry[0];
                        bestSuffixUri = entry[1];
                    }
                }
            }
            if (bestSuffixUri != null) {
                return bestSuffixUri;
            }
            if (uriCatalog.containsKey(uri)) {
                return uriCatalog.get(uri);
            }
            try {
                URI u = new URI(uri).normalize();
                if (u.isAbsolute() && u.toURL().getProtocol().startsWith("file")) {
                    return u.toString();
                }
            } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
                // ignore
            }
        }
        return null;
    }

    public void currentDocumentBase(String parentFile) {
        documentParent = parentFile;
    }

    public String getDTD(String name) {
        return name != null ? dtdCatalog.get(name) : null;
    }

    public void addDtdPublicEntity(String publicId, String path) {
        if (dtdPublicEntities == null) {
            dtdPublicEntities = new Hashtable<>();
        }
        dtdPublicEntities.computeIfAbsent(publicId, k -> path);
    }

    public void addDtdSystemEntity(String systemId, String path) {
        if (dtdSystemEntities == null) {
            dtdSystemEntities = new Hashtable<>();
        }
        dtdSystemEntities.computeIfAbsent(systemId, k -> path);
    }

    public void parseDTD(String publicId) {
        if (parsedDTDs == null) {
            parsedDTDs = new TreeSet<>();
        }
        String dtd = matchPublic(publicId);
        if (dtd != null && !parsedDTDs.contains(dtd)) {
            try {
                DTDParser parser = new DTDParser();
                File dtdFile = new File(dtd);
                Grammar grammar = parser.parse(dtdFile);
                List<EntityDecl> entities = grammar.getPublicEntities();
                Iterator<EntityDecl> it = entities.iterator();
                while (it.hasNext()) {
                    EntityDecl entity = it.next();
                    String path = XMLUtils.getAbsolutePath(dtdFile.getParentFile().getAbsolutePath(),
                            entity.getValue());
                    addDtdPublicEntity(entity.getPublicId(), path);
                }
                entities = grammar.getSytemEntities();
                it = entities.iterator();
                while (it.hasNext()) {
                    EntityDecl entity = it.next();
                    addDtdSystemEntity(entity.getValue(), entity.getSystemId());
                }
            } catch (IOException | SAXException e) {
                // do nothing
                MessageFormat mf = new MessageFormat(Messages.getString("Catalog.0"));
                logger.log(Level.WARNING, mf.format(new String[] { publicId }));
            }
            parsedDTDs.add(dtd);
        }
    }
}
