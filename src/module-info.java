/*******************************************************************************
 * Copyright (c) 2022-2026 Maxprograms. All rights reserved.
 *
 * This software is the proprietary property of Maxprograms.
 * Use, modification, and distribution are subject to the terms of the 
 * Software License Agreement found in the root of this distribution 
 *
 * Unauthorized redistribution or commercial use is strictly prohibited.
 *******************************************************************************/
module xmljava {
	
	exports com.maxprograms.xml;

	opens com.maxprograms.xml to mapdb;
	
	requires java.base;
	requires transitive java.xml;
}
