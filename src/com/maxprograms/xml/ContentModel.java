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

import java.io.Serializable;

public class ContentModel implements Serializable {

    public static final String EMPTY = "EMPTY";
    public static final String ANY = "ANY";
    public static final String MIXED = "Mixed";
    public static final String PCDATA = "#PCDATA";

    // cardinality
    public static final int NONE = 0;
    public static final int OPTIONAL = 1; // ?
    public static final int ZEROMANY = 2; // *
    public static final int ONEMANY = 3; // +

    public ContentModel() {
    }

    public static ContentModel parse(String string) {
        // TODO
        return new ContentModel();
    }

}
