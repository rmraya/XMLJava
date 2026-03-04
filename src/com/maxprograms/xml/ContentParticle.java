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

import java.util.List;

public interface ContentParticle {

    public static final int PCDATA = 0;
    public static final int NAME = 1;
    public static final int SEQUENCE = 2;
    public static final int CHOICE = 3;

    public int getType();

    public void addParticle(ContentParticle particle);

    public void setCardinality(int cardinality);

    public int getCardinality();

    public List<ContentParticle> getParticles();

    @Override
    public String toString();
}
