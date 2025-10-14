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
