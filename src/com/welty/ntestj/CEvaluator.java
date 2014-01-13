package com.welty.ntestj;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: May 7, 2009
 * Time: 7:44:10 PM
 * To change this template use File | Settings | File Templates.
 */
class CEvaluatorInfo {
    private final char evaluatorType;
    private final char coeffSet;

    @Override public int hashCode() {
        return evaluatorType * 31 + coeffSet;
    }

    @Override public boolean equals(Object obj) {
        if (obj instanceof CEvaluatorInfo) {
            CEvaluatorInfo b = (CEvaluatorInfo) obj;
            return evaluatorType == b.evaluatorType && coeffSet == b.coeffSet;
        } else {
            return false;
        }
    }

    public CEvaluatorInfo(char evaluatorType, char coeffSet) {
        this.evaluatorType = evaluatorType;
        this.coeffSet = coeffSet;
    }
}

//////////////////////////////////
//// Pattern L
///////////////////////////////////
//
//// pattern L mappings
//enum { R1L, R2L, R3L, R4L, D8L, D7L, D6L, D5L, C4L, C2x4L, C2x5L, CR1XXL, C3x3L, M1L, M2L, PM1L, PM2L, PARL };	// map numbers
//
//final int	patternToMapL[]={
//	// rows & cols
//	R1L, R1L, R1L, R1L,
//	R2L, R2L, R2L, R2L,
//	R3L, R3L, R3L, R3L,
//	R4L, R4L, R4L, R4L,
//	// diagonals & corners
//	D8L, D8L,
//	D7L, D7L, D7L, D7L,
//	D6L, D6L, D6L, D6L,
//	D5L, D5L, D5L, D5L,
//	C4L, C4L, C4L, C4L,
//
//	// 2x4, 2x5, edge+2X
//	C2x4L, C2x4L, C2x4L, C2x4L, C2x4L, C2x4L, C2x4L, C2x4L,
//	C2x5L, C2x5L, C2x5L, C2x5L, C2x5L, C2x5L, C2x5L, C2x5L,
//	CR1XXL,CR1XXL,CR1XXL,CR1XXL,
//	C3x3L, C3x3L, C3x3L, C3x3L,
//
//	// mobility and parity
//	M1L, M2L,
//	PM1L, PM2L,
//	PARL
//};	// tells which patterns are valued the same.
//
//final int	nPatternsL=sizeof(patternToMapL)/sizeof(int);
//
//// pattern L descriptions
//final CMap	mapsL[]= {
//	{kORID,8}, {kORID,8}, {kORID,8}, {kORID,8}, // rows & cols
//	{kORID,8}, {kORID,7}, {kORID,6}, {kORID,5},  {kCRID, 10}, // diags
//	{kBase3, 8}, {kBase3, 10}, {kORID, 10},	{kR33ID,9},	// corner patterns: 2x4, 2x5, edge+2X, 3x3
//	{kNumber, 64}, {kNumber, 64},				// mobility
//	{kNumber, 64}, {kNumber, 64},				// pot. mobility
//	{kNumber, 2}								//  parity
//};
//
//final int	nMapsL=sizeof(mapsL)/sizeof(CMap);
//extern int coeffStartsL[nMapsL];
//extern int nCoeffsL;
//extern int *mobsL;
//
//// Copyright Chris Welty
////	All Rights Reserved
//// This file is distributed subject to GNU GPL version 2. See the files
//// Copying.txt and GPL.txt for details.
//
//// evaluator source code
//#include "Evaluator.h"
//#include "QPosition.h"
//#include "Debug.h"
//#include "Pos2All.h"
//#include "options.h"
//#include <sstream>
//
//#if defined(_DEBUG) && defined(_MSC_VER)
//#define _CRTDBG_MAP_ALLOC
//#include <crtdbg.h>
//#define new new(_NORMAL_BLOCK, __FILE__, __LINE__)
//#endif
//
//float multiplier=10*kStoneValue;
//
//extern CEvaluator* evaluator;
//
////////////////////////////////////////////////////////
//// Pattern J evaluator
////	Use 2x4, 2x5, edge+X patterns
////////////////////////////////////////////////////////
//
//}
