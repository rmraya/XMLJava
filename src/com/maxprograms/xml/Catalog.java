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
    private String workDir;
    private String base = "";
    private String documentParent = "";

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

        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(catalogFile);
        Element root = doc.getRootElement();
        recurse(root);
    }

    private void recurse(Element root)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
        List<Element> children = root.getChildren();
        Iterator<Element> i = children.iterator();
        while (i.hasNext()) {
            Element child = i.next();
            String currentBase = base;

            if (!child.getAttributeValue("xml:base").isEmpty()) {
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
                        if (!dtdCatalog.containsKey(dtd.getName())) {
                            dtdCatalog.put(dtd.getName(), dtd.getAbsolutePath());
                        }
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
                            if (!dtdCatalog.containsKey(dtd.getName())) {
                                dtdCatalog.put(dtd.getName(), dtd.getAbsolutePath());
                            }
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
                        if (!dtdCatalog.containsKey(dtd.getName())) {
                            dtdCatalog.put(dtd.getName(), dtd.getAbsolutePath());
                        }
                    }
                }
            }
            if (child.getName().equals("nextCatalog")) {
                String nextCatalog = child.getAttributeValue("catalog");
                File f = new File(nextCatalog);
                if (!f.isAbsolute()) {
                    nextCatalog = XMLUtils.getAbsolutePath(workDir, nextCatalog);
                }
                Catalog cat = new Catalog(nextCatalog);
                Map<String, String> table = cat.getSystemCatalog();
                Iterator<String> it = table.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    if (!systemCatalog.containsKey(key)) {
                        String value = table.get(key);
                        systemCatalog.put(key, value);
                    }
                }
                table = cat.getPublicCatalog();
                it = table.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    if (!publicCatalog.containsKey(key)) {
                        String value = table.get(key);
                        publicCatalog.put(key, value);
                    }
                }
                table = cat.getUriCatalog();
                it = table.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    if (!uriCatalog.containsKey(key)) {
                        String value = table.get(key);
                        uriCatalog.put(key, value);
                    }
                }
                table = cat.getDtdCatalog();
                it = table.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    if (!dtdCatalog.containsKey(key)) {
                        String value = table.get(key);
                        dtdCatalog.put(key, value);
                    }
                }
                List<String[]> system = cat.getSystemRewrites();
                for (int h = 0; h < system.size(); h++) {
                    String[] pair = system.get(h);
                    if (!systemRewrites.contains(pair)) {
                        systemRewrites.add(pair);
                    }
                }
                List<String[]> uris = cat.getUriRewrites();
                for (int h = 0; h < uris.size(); h++) {
                    String[] pair = uris.get(h);
                    if (!uriRewrites.contains(pair)) {
                        uriRewrites.add(pair);
                    }
                }
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
        return uriCatalog;
    }

    private List<String[]> getSystemRewrites() {
        return systemRewrites;
    }

    private List<String[]> getUriRewrites() {
        return uriRewrites;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
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
            if (uriCatalog.containsKey(uri)) {
                return uriCatalog.get(uri);
            }
            try {
                URI u = new URI(uri).normalize();
                if (u.toURL().getProtocol().startsWith("file")) {
                    return u.toString();
                }
            } catch (URISyntaxException | MalformedURLException e) {
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
