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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class Grammar {

    private Map<String, ElementDecl> elementDeclMap;
    private Map<String, AttlistDecl> attributeListMap;
    private Map<String, EntityDecl> entitiesMap;
    private Map<String, NotationDecl> notationsMap;

    public Grammar(Map<String, ElementDecl> elementDeclMap, Map<String, AttlistDecl> attributeListMap,
            Map<String, EntityDecl> entitiesMap, Map<String, NotationDecl> notationsMap) {
        this.elementDeclMap = elementDeclMap;
        this.attributeListMap = attributeListMap;
        this.entitiesMap = entitiesMap;
        this.notationsMap = notationsMap;
    }

    public List<EntityDecl> getSytemEntities() {
        List<EntityDecl> result = new Vector<>();
        Set<String> keys = entitiesMap.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            EntityDecl entity = entitiesMap.get(it.next());
            if (EntityDecl.SYSTEM.equals(entity.getType())) {
                result.add(entity);
            }
        }
        return result;
    }

    public List<EntityDecl> getPublicEntities() {
        List<EntityDecl> result = new Vector<>();
        Set<String> keys = entitiesMap.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            EntityDecl entity = entitiesMap.get(it.next());
            if (EntityDecl.PUBLIC.equals(entity.getType())) {
                result.add(entity);
            }
        }
        return result;
    }

    public List<EntityDecl> getEntities() {
        List<EntityDecl> result = new Vector<>();
        result.addAll(entitiesMap.values());
        return result;
    }

    public List<ElementDecl> getElements() {
        List<ElementDecl> result = new Vector<>();
        Set<String> keys = elementDeclMap.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            result.add(elementDeclMap.get(it.next()));
        }
        return result;
    }

    public Map<String, ElementDecl> getElementDeclMap() {
        return elementDeclMap;
    }

    public Map<String, AttlistDecl> getAttributeListMap() {
        return attributeListMap;
    }

    public Map<String, EntityDecl> getEntitiesMap() {
        return entitiesMap;
    }

    public Map<String, NotationDecl> getNotationsMap() {
        return notationsMap;
    }

    public String getRootElement() {
        Set<String> discarded = new TreeSet<>();
        Set<String> keys = elementDeclMap.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            ElementDecl elementDecl = elementDeclMap.get(key);
            ContentModel model = elementDecl.getModel();
            Set<String> children = model.getChildren();
            discarded.addAll(children);
        }
        if (keys.size() - discarded.size() != 1) {
            return "";
        }
        it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (!discarded.contains(key)) {
                ElementDecl elementDecl = elementDeclMap.get(key);
                ContentModel model = elementDecl.getModel();
                if (model.getType().equals(ContentModel.EMPTY)) {
                    return "";
                }
                return key;
            }
        }
        return "";
    }
}
